package com.es.plus.client;


import com.es.plus.core.ScrollHandler;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.core.EsParamWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.exception.EsException;
import com.es.plus.lock.EsLockFactory;
import com.es.plus.pojo.*;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.util.BeanUtils;
import com.es.plus.util.FieldUtils;
import com.es.plus.util.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.es.plus.config.GlobalConfigCache.GLOBAL_CONFIG;
import static com.es.plus.constant.EsConstant.*;
import static com.es.plus.constant.EsConstant.REINDEX_UPDATE_LOCK;
import static com.es.plus.util.ResolveUtils.isCommonDataType;
import static com.es.plus.util.ResolveUtils.isWrapClass;


/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsPlusRestClient implements EsPlusClient {
    private static final Logger log = LoggerFactory.getLogger(EsPlusRestClient.class);
    private final RestHighLevelClient restHighLevelClient;
    private boolean reindexState = false;
    private final EsLockFactory esLockFactory;

    @Override
    public boolean getReindexState() {
        return reindexState;
    }

    @Override
    public void setReindexState(boolean reindexState) {
        this.reindexState = reindexState;
    }


    public EsPlusRestClient(RestHighLevelClient restHighLevelClient, EsLockFactory esLockFactory) {
        this.restHighLevelClient = restHighLevelClient;
        this.esLockFactory = esLockFactory;
    }


    /**
     * ??????????????? ??????????????????
     *
     * @param index ??????
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());

        boolean lock = false;
        try {
            if (reindexState) {
                lock = lock(index);
                if (lock) {
                    esDataList = esDataList.stream().map(e -> handlerUpdateParamter(e)).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Object esData : esDataList) {
                UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GLOBAL_CONFIG.getMaxRetries());
                updateRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
                // ???????????????????????????
                updateRequest.upsert(JsonUtils.toJsonStr(esData), XContentType.JSON);
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(updateRequest);
            }
            bulkRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res = null;
            printInfoLog("saveOrUpdateBatch index={} data:{} hasFailures={}", index, JsonUtils.toJsonStr(esDataList));
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog("saveOrUpdateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new EsException("saveOrUpdateBatch IOException", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
        return responses;
    }

    private boolean isChildIndex(Object esData) {
        Class<?> clazz = esData.getClass();
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(clazz);
        if (esIndexParam.getChildClass() != null && esIndexParam.getChildClass().equals(clazz)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ????????????
     */
    @Override
    public List<BulkItemResponse> saveBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }

        boolean lock = false;

        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
        try {
            if (reindexState) {
                lock = lock(index);
                if (lock) {
                    esDataList = esDataList.stream().map(e -> handlerSaveParamter(e)).collect(Collectors.toList());
                    esDataList = esDataList.stream().map(e -> handlerUpdateParamter(e)).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();

            for (Object esData : esDataList) {
                IndexRequest indexRequest = new IndexRequest(index);
                indexRequest.id(EsParamHolder.getDocId(esData)).source(JsonUtils.toJsonStr(esData), XContentType.JSON);
                if (childIndex) {
                    indexRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(indexRequest);
            }
            bulkRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res;

            printInfoLog("saveBatch index={}", index);
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    printErrorLog("save error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                    failBulkItemResponses.add(bulkItemResponse);
                }
            }
        } catch (IOException e) {
            throw new EsException("SaveBatch IOException", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
        return failBulkItemResponses;
    }

    /**
     * ??????
     */
    @Override
    public boolean save(String index, Object esData) {
        List<BulkItemResponse> bulkItemResponses = saveBatch(index, Collections.singletonList(esData));
        if (CollectionUtils.isEmpty(bulkItemResponses)) {
            return true;
        }
        return false;
    }

    /**
     * ??????Es??????
     *
     * @param esData Es????????????
     * @return
     * @throws Exception
     */
    @Override
    public boolean update(String index, Object esData) {

        boolean lock = false;
        boolean childIndex = isChildIndex(esData);
        try {
            if (reindexState) {
                lock = lock(index);
                if (lock) {
                    esData = handlerUpdateParamter(esData);
                }
            }

            UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            //?????????????????????
            updateRequest.retryOnConflict(GLOBAL_CONFIG.getMaxRetries());
            updateRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
            if (childIndex) {
                updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
            }
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                printErrorLog("update index={} data={}  error reason: doc  deleted", index, JsonUtils.toJsonStr(esData));
                return false;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                //noop?????????????????????????????????????????????
                return false;
            } else {
                printInfoLog("update success index={} data={}", index, JsonUtils.toJsonStr(esData));
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch update io error", e);
        } catch (ElasticsearchException e) {
            //????????????
            if (e.status() == RestStatus.CONFLICT) {
                throw new EsException("elasticsearch update error  version conflict");
            }
            //?????????
            if (e.status() == RestStatus.NOT_FOUND) {
                printErrorLog("es update index={} data={}  error reason:  not found doc", index, JsonUtils.toJsonStr(esData));
                throw new ElasticsearchException(e);
            }
        } catch (Exception e) {
            throw new EsException("update error", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
        return true;
    }


    /**
     * ??????????????? ??????????????????
     *
     * @param index ??????
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }

        boolean lock = false;

        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
        try {
            if (reindexState) {
                lock = lock(index);
                if (lock) {
                    esDataList = esDataList.stream().map(e -> handlerUpdateParamter(e)).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Object esData : esDataList) {
                UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GLOBAL_CONFIG.getMaxRetries());
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(updateRequest);
            }
            bulkRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res = null;
            printInfoLog("updateBatch index={} data:{} hasFailures={}", index, JsonUtils.toJsonStr(esDataList));
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog("updateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new EsException("updateBatch IOException", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
        return responses;
    }


    /**
     * ????????????
     */
    @Override
    public <T> BulkByScrollResponse updateByWrapper(String index, EsUpdateWrapper<T> esUpdateWrapper) {
        EsUpdateField esUpdateField = esUpdateWrapper.getEsUpdateField();
        List<EsUpdateField.Field> fields = esUpdateField.getFields();
        String scipt = esUpdateField.getScipt();
        Map<String, Object> params = esUpdateField.getSciptParams();
        boolean lock = false;
        if (StringUtils.isBlank(scipt)) {
            params = new HashMap<>();
            //??????scipt??????
            StringBuilder sb = new StringBuilder();
            for (EsUpdateField.Field field : fields) {
                String name = field.getName();
                //??????????????????????????????????????????????????????????????????
                Object value = field.getValue();
                if (value instanceof Date) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = (Date) value;
                    value = simpleDateFormat.format(date);
                } else if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else if (value instanceof LocalDate) {
                    value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else if (value instanceof List) {
                } else if (!isCommonDataType(value.getClass()) && !isWrapClass(value.getClass())) {
                    value = BeanUtils.beanToMap(value);
                }
                //list???????????? ????????? ?????????????????????
                params.put(name, value);
                sb.append("ctx._source.");
                sb.append(name).append(" = params.").append(name).append(";");
            }
            if (reindexState) {
                lock = lock(index);
                // ?????????????????????
                if (lock) {
                    handleObjectScript(sb, params);
                }
            }
            scipt = sb.toString();
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //???????????????????????????????????????
            request.setConflicts(DEFAULT_CONFLICTS);
            request.setQuery(esUpdateWrapper.getQueryBuilder());
            request.setBatchSize(GLOBAL_CONFIG.getBatchSize());
            String[] routings = esUpdateWrapper.getEsParamWrapper().getRoutings();
            if (routings != null) {
                request.setRouting(routings[0]);
            }

            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            Script painless = new Script(ScriptType.INLINE, "painless", scipt, params);
            request.setScript(painless);
            printInfoLog("updateByWrapper requst: script:{},params={}", scipt, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("updateByWrapper response:{} update count=", bulkResponse);
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper IOException", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
    }

    @Override
    public <T> BulkByScrollResponse increment(String index, EsUpdateWrapper<T> esUpdateWrapper) {
        boolean lock = false;
        List<EsUpdateField.Field> fields = esUpdateWrapper.getEsUpdateField().getIncrementFields();
        Map<String, Object> params = new HashMap<>();
        //??????scipt??????
        StringBuilder script = new StringBuilder();
        for (EsUpdateField.Field field : fields) {
            String name = field.getName();
            Long value = (Long) field.getValue();
            params.put(name, value);
            script.append("ctx._source.");
            script.append(name).append(" += params.").append(name).append(";");
        }
        if (reindexState) {
            lock = lock(index);
            // ?????????????????????
            if (lock) {
                handleObjectScript(script, params);
            }
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //???????????????????????????????????????
            request.setConflicts(DEFAULT_CONFLICTS);
            request.setQuery(esUpdateWrapper.getQueryBuilder());
            // ????????????????????????.????????????????????????
            request.setBatchSize(GLOBAL_CONFIG.getBatchSize());
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            String[] routings = esUpdateWrapper.getEsParamWrapper().getRoutings();
            if (routings != null) {
                request.setRouting(routings[0]);
            }

            Script painless = new Script(ScriptType.INLINE, "painless", script.toString(), params);
            request.setScript(painless);

            printInfoLog("updateByWrapper increment requst: script:{},params={}", script, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("updateByWrapper increment response:{} update count=", bulkResponse);
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper increment IOException", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
    }

    @Override
    public boolean delete(String index, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(index, id);
        try {
            deleteRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            printInfoLog("delete index={}", index);
        } catch (IOException e) {
            throw new EsException("delete error", e);
        }
        return true;
    }

    @Override
    public <T> BulkByScrollResponse deleteByQuery(String index, EsUpdateWrapper<T> esUpdateWrapper) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(esUpdateWrapper.getQueryBuilder());
        // ?????????????????????
        request.setMaxDocs(GLOBAL_CONFIG.getMaxDocs());
        request.setMaxRetries(GLOBAL_CONFIG.getMaxRetries());
        request.setBatchSize(GLOBAL_CONFIG.getBatchSize());
        // ????????????
        request.setRefresh(true);
        // ???????????????????????????????????????????????????????????????
        request.setScroll(TimeValue.timeValueMinutes(30));
        // ??????
        request.setTimeout(TimeValue.timeValueMinutes(30));
        // ?????????????????????
        request.setConflicts(DEFAULT_CONFLICTS);
        String[] routings = esUpdateWrapper.getEsParamWrapper().getRoutings();
        if (routings != null) {
            request.setRouting(routings[0]);
        }

        try {
            SearchSourceBuilder source = request.getSearchRequest().source();
            printInfoLog("delete body:" + source.toString());
            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            return bulkByScrollResponse;
        } catch (Exception e) {
            throw new EsException("es-plus delete error", e);
        }
    }

    /**
     * ????????????
     */
    public void deleteAll(String index) {
        if (index.endsWith("_pro")) {
            throw new EsException("????????????");
        }
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setMaxRetries(GLOBAL_CONFIG.getMaxRetries());
        request.setQuery(new MatchAllQueryBuilder());
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("deleteAll index={}", index);
        } catch (IOException e) {
            throw new EsException("delete error", e);
        }
    }

    @Override
    public boolean deleteBatch(String index, Collection<String> esDataList) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return false;
        }
        log.info("Es deleteBatch index={} ids={}", index, esDataList);
        BulkRequest bulkRequest = new BulkRequest();
        esDataList.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            bulkRequest.add(deleteRequest);
        });

        bulkRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            BulkItemResponse[] items = bulkResponse.getItems();
            for (BulkItemResponse item : items) {
                if (item.isFailed()) {
                    printErrorLog("deleteBatch index={} id={} FailureMessage=:{}", index, item.getId(), item.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new EsException("es delete error", e);
        }
        return true;
    }

    //??????
    @Override
    public <T> long count(EsQueryWrapper<T> esQueryWrapper, String index) {
        CountRequest countRequest = new CountRequest();
        countRequest.query(esQueryWrapper.getQueryBuilder());
        countRequest.indices(index);
        CountResponse count = null;
        try {
            printInfoLog("count index=:{} body:{}", index, esQueryWrapper.getQueryBuilder().toString());
            count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("es-plus count error ", e);
        }
        if (count != null) {
            return count.getCount();
        }
        return 0;
    }

    @Override
    public <T> EsResponse<T> searchByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        return search(null, esQueryWrapper, tClass, index);
    }

    @Override
    public <T> EsResponse<T> searchPageByWrapper(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        return search(pageInfo, esQueryWrapper, tClass, index);
    }

    @Override
    public <T> void scrollByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index, int size, int keepTime, ScrollHandler<T> scrollHandler) {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(keepTime));
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(esQueryWrapper.getQueryBuilder());
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            //??????scroll??????
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<T> result = new ArrayList<>();
            while (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    T t = JsonUtils.toBean(searchHit.getSourceAsString(), tClass);
                    result.add(t);
                }
                scrollHandler.handler(result);
                result.clear();
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();
        } catch (Exception e) {
            throw new EsException("scroll error", e);
        }
    }

    // ??????
    @Override
    public <T> EsAggregationsResponse<T> aggregations(String index, EsQueryWrapper<T> esQueryWrapper) {
        SearchRequest searchRequest = new SearchRequest();
        //??????????????????
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(0);
        populateGroupField(esQueryWrapper, sourceBuilder);
        //????????????
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        //??????
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            printInfoLog("aggregations index={} body:{}", index, sourceBuilder);
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            printInfoLog("aggregations Time={}", end - start);
        } catch (Exception e) {
            throw new EsException("aggregations error", e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch aggregations error");
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsAggregationsResponse<T> esAggregationReponse = new EsAggregationsResponse<>();
        esAggregationReponse.setAggregations(aggregations);
        esAggregationReponse.settClass(esQueryWrapper.gettClass());
        return esAggregationReponse;
    }


    private <T> EsResponse<T> search(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        SearchRequest searchRequest = new SearchRequest();
        EsParamWrapper esParamWrapper = esQueryWrapper.getEsParamWrapper();
        //??????????????????
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        EsSelect esSelect = esParamWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
                sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            }
        }
        boolean profile = esQueryWrapper.getEsParamWrapper().isProfile();
        if (profile) {
            sourceBuilder.profile(profile);
        }
        sourceBuilder.size(GLOBAL_CONFIG.getSearchSize());
        //????????????????????????
        if (pageInfo != null) {
            //??????????????????
            sourceBuilder.from((int) ((pageInfo.getPage() - 1) * pageInfo.getSize()));
            sourceBuilder.size((int) pageInfo.getSize());
        }
        //????????????
        if (esParamWrapper.getEsHighLights() != null) {
            List<EsHighLight> esHighLight = esParamWrapper.getEsHighLights();
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //?????????0??????????????????
            highlightBuilder.numOfFragments(0);
            for (EsHighLight highLight : esHighLight) {
                //????????????
                highlightBuilder.field(highLight.getField());
                //???????????????
                highlightBuilder.preTags(highLight.getPreTag())
                        .postTags(highLight.getPostTag())
                        .fragmentSize(highLight.getFragmentSize());
                sourceBuilder.highlighter(highlightBuilder);
            }
        }
        //??????1????????????????????????
        if (GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(true);
        }
        //??????
        if (!CollectionUtils.isEmpty(esParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esParamWrapper.getEsOrderList();
            orderFields.forEach(order -> {
                sourceBuilder.sort(new FieldSortBuilder(order.getName()).order(SortOrder.valueOf(order.getSort())));
            });
        }
        populateGroupField(esQueryWrapper, sourceBuilder);
        //????????????
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        if (esParamWrapper.getSearchType() != null) {
            searchRequest.searchType();
        }
        //??????
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            printInfoLog("search index={} body:{} Time={}", index, sourceBuilder, end - start);
        } catch (Exception e) {
            throw new EsException("es-plus search body=" + sourceBuilder, e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("es-plus search error:" + searchResponse.status().getStatus());
        }
        //???????????????
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = new ArrayList<>();
        if (esParamWrapper.getEsHighLights() != null) {
            for (SearchHit hit : hitArray) {
                //??????????????????
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                //???Json???????????????????????????
                Map<String, Object> map = hit.getSourceAsMap();
                if (highlightFields != null) {
                    highlightFields.forEach((k, v) -> {
                                Text[] texts = v.fragments();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (Text text : texts) {
                                    stringBuilder.append(text);
                                }
                                //??????????????????put??????
                                map.put(k, stringBuilder.toString());
                            }
                    );

                }
                T t = BeanUtils.mapToBean(map, tClass);
                result.add(t);
            }
        } else {
            for (SearchHit hit : hitArray) {
                result.add(JsonUtils.toBean(hit.getSourceAsString(), tClass));
            }
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsAggregationsResponse<T> esAggregationsReponse = new EsAggregationsResponse<>();
        esAggregationsReponse.setAggregations(aggregations);
        esAggregationsReponse.settClass(esQueryWrapper.gettClass());
        EsResponse<T> tEsResponse = new EsResponse<>(result, hits.getTotalHits().value, esAggregationsReponse);
        tEsResponse.setShardFailures(searchResponse.getShardFailures());
        tEsResponse.setSkippedShards(searchResponse.getSkippedShards());
        tEsResponse.setTookInMillis(searchResponse.getTook().getMillis());
        tEsResponse.setSuccessfulShards(searchResponse.getSuccessfulShards());
        tEsResponse.setTotalShards(searchResponse.getTotalShards());
        if (profile) {
            Map<String, ProfileShardResult> profileResults = searchResponse.getProfileResults();
            tEsResponse.setProfileResults(profileResults);
        }
        return tEsResponse;
    }


    //??????????????????
    private void populateGroupField(EsQueryWrapper<?> esQueryWrapper, SearchSourceBuilder sourceBuilder) {
        EsLambdaAggWrapper<?> esLambdaAggWrapper = esQueryWrapper.esLambdaAggWrapper();
        EsAggWrapper<?> esAggregationWrapper = esQueryWrapper.esAggWrapper();
        if (esLambdaAggWrapper.getAggregationBuilder() != null) {
            for (BaseAggregationBuilder aggregation : esLambdaAggWrapper.getAggregationBuilder()) {
                if (aggregation instanceof AggregationBuilder) {
                    sourceBuilder.aggregation((AggregationBuilder) aggregation);
                } else {
                    sourceBuilder.aggregation((PipelineAggregationBuilder) aggregation);
                }
            }
        } else {
            for (BaseAggregationBuilder aggregation : esAggregationWrapper.getAggregationBuilder()) {
                if (aggregation instanceof AggregationBuilder) {
                    sourceBuilder.aggregation((AggregationBuilder) aggregation);
                } else {
                    sourceBuilder.aggregation((PipelineAggregationBuilder) aggregation);
                }
            }
        }
    }


    private void printInfoLog(String format, Object... params) {
        log.info("es-plus " + format, params);
    }

    private void printErrorLog(String format, Object... params) {
        log.error("es-plus " + format, params);
    }

    protected void handleObjectScript(StringBuilder sb, Map<String, Object> params) {
        EsUpdateField.Field updateFill = updateFill();
        sb.append("ctx._source." + updateFill.getName()).append(" = params.").append(updateFill.getName()).append(";");
        params.put(updateFill.getName(), updateFill.getValue());
    }

    protected Object handlerSaveParamter(Object esData) {
        return esData;
    }

    protected Object handlerUpdateParamter(Object esData) {
        esData = setUpdateFeild(esData);
        return esData;
    }

    public EsUpdateField.Field updateFill() {
        return new EsUpdateField.Field(REINDEX_TIME_FILED, System.currentTimeMillis());
    }

    public boolean lock(String index) {
        // ??????reindex????????????.??????????????????  ????????????????????????????????????
        // 1???????????????????????????isLocked??????.?????????????????????.??????????????????.????????????????????????????????????mappins?????????.????????????,
        //  2???????????????????????????????????????.
        Lock readLock = esLockFactory.getReadWrtieLock(index + REINDEX_UPDATE_LOCK).readLock();
        boolean success = false;
        try {
            success = readLock.tryLock(3, TimeUnit.SECONDS);

            //??????reindex??????
            boolean isLock = esLockFactory.getLock(index + REINDEX_LOCK_SUFFIX).isLocked();

            //?????????????????????????????????
            if (!isLock) {
                //???????????????reindex?????? ?????????????????????
                reindexState = false;
                log.info("enabledReindex = false");
                if (success) {
                    readLock.unlock();
                }
                return false;
            }

            //??????????????????????????????????????????????????????
            if (!success) {
                throw new EsException("index:" + index + " tryLock:" + REINDEX_UPDATE_LOCK + " fail");
            }
        } catch (InterruptedException ignored) {
        }
        return success;
    }

    //??????reindex???
    public void unLock(String index) {
        Lock readLock = esLockFactory.getReadWrtieLock(index + REINDEX_UPDATE_LOCK).readLock();
        readLock.unlock();
    }

    public Object setUpdateFeild(Object object) {
        EsUpdateField.Field updateFill = updateFill();
        if (updateFill == null) {
            return object;
        }
        Map<String, Object> beanToMap = BeanUtils.beanToMap(object);
        beanToMap.put(updateFill.getName(), updateFill.getValue());
        return beanToMap;
    }

}
