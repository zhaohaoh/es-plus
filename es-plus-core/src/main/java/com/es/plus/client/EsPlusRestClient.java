package com.es.plus.client;


import com.es.plus.core.process.ReindexObjectProcess;
import com.es.plus.core.ScrollHandler;
import com.es.plus.core.wrapper.aggregation.EsAggregationWrapper;
import com.es.plus.core.wrapper.core.EsParamWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.aggregation.EsLamdaAggregationWrapper;
import com.es.plus.exception.EsException;
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
import java.util.stream.Collectors;

import static com.es.plus.config.GlobalConfigCache.GLOBAL_CONFIG;
import static com.es.plus.constant.EsConstant.DEFAULT_CONFLICTS;
import static com.es.plus.util.ResolveUtils.isCommonDataType;
import static com.es.plus.util.ResolveUtils.isWrapClass;


/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsPlusRestClient implements EsPlusClient {
    private static final Logger log = LoggerFactory.getLogger(EsPlusRestClient.class);
    private final RestHighLevelClient restHighLevelClient;
    private ReindexObjectProcess reindexObjectHandlerImpl;

    public void setReindexObjectHandlerImpl(ReindexObjectProcess reindexObjectHandlerImpl) {
        this.reindexObjectHandlerImpl = reindexObjectHandlerImpl;
    }

    public EsPlusRestClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }


    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());

        boolean enabled = ReindexObjectProcess.ENABLED;
        boolean lock = false;
        try {
            if (enabled) {
                lock = reindexObjectHandlerImpl.lock(index);
                if (lock) {
                    esDataList = esDataList.stream().map(e -> handlerUpdateParamter(e)).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Object esData : esDataList) {
                UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GLOBAL_CONFIG.getMaxRetries());
                updateRequest.setRefreshPolicy(GLOBAL_CONFIG.getRefreshPolicy());
                // 如果没有文档则新增
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
                reindexObjectHandlerImpl.unLock(index);
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
     * 保存批量
     */
    @Override
    public List<BulkItemResponse> saveBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        boolean enabled = ReindexObjectProcess.ENABLED;
        boolean lock = false;

        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
        try {
            if (enabled) {
                lock = reindexObjectHandlerImpl.lock(index);
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
                reindexObjectHandlerImpl.unLock(index);
            }
        }
        return failBulkItemResponses;
    }

    /**
     * 保存
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
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    @Override
    public boolean update(String index, Object esData) {
        boolean enabled = ReindexObjectProcess.ENABLED;
        boolean lock = false;
        boolean childIndex = isChildIndex(esData);
        try {
            if (enabled) {
                lock = reindexObjectHandlerImpl.lock(index);
                if (lock) {
                    esData = handlerUpdateParamter(esData);
                }
            }

            UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            //乐观锁重试次数
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
                //noop标识没有数据改变。前后的值相同
                return false;
            } else {
                printInfoLog("update success index={} data={}", index, JsonUtils.toJsonStr(esData));
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch update io error", e);
        } catch (ElasticsearchException e) {
            //版本冲突
            if (e.status() == RestStatus.CONFLICT) {
                throw new EsException("elasticsearch update error  version conflict");
            }
            //找不到
            if (e.status() == RestStatus.NOT_FOUND) {
                printErrorLog("es update index={} data={}  error reason:  not found doc", index, JsonUtils.toJsonStr(esData));
                throw new ElasticsearchException(e);
            }
        } catch (Exception e) {
            throw new EsException("update error", e);
        } finally {
            if (lock) {
                reindexObjectHandlerImpl.unLock(index);
            }
        }
        return true;
    }


    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(String index, Collection<?> esDataList) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        boolean enabled = ReindexObjectProcess.ENABLED;
        boolean lock = false;

        boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
        try {
            if (enabled) {
                lock = reindexObjectHandlerImpl.lock(index);
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
                reindexObjectHandlerImpl.unLock(index);
            }
        }
        return responses;
    }


    /**
     * 更新包装
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
            //构建scipt语句
            StringBuilder sb = new StringBuilder();
            for (EsUpdateField.Field field : fields) {
                String name = field.getName();
                //除了基本类型和字符串。日期的对象需要进行转化
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
                //list直接覆盖 丢进去 无需再特殊处理
                params.put(name, value);
                sb.append("ctx._source.");
                sb.append(name).append(" = params.").append(name).append(";");
            }
            if (ReindexObjectProcess.ENABLED) {
                lock = reindexObjectHandlerImpl.lock(index);
                // 自定义字段处理
                if (lock) {
                    handleObjectScript(sb, params);
                }
            }
            scipt = sb.toString();
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //版本号不匹配更新失败不停止
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
                reindexObjectHandlerImpl.unLock(index);
            }
        }
    }

    @Override
    public <T> BulkByScrollResponse increment(String index, EsUpdateWrapper<T> esUpdateWrapper) {
        boolean lock = false;
        List<EsUpdateField.Field> fields = esUpdateWrapper.getEsUpdateField().getIncrementFields();
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        StringBuilder script = new StringBuilder();
        for (EsUpdateField.Field field : fields) {
            String name = field.getName();
            Long value = (Long) field.getValue();
            params.put(name, value);
            script.append("ctx._source.");
            script.append(name).append(" += params.").append(name).append(";");
        }
        if (ReindexObjectProcess.ENABLED) {
            lock = reindexObjectHandlerImpl.lock(index);
            // 自定义字段处理
            if (lock) {
                handleObjectScript(script, params);
            }
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //版本号不匹配更新失败不停止
            request.setConflicts(DEFAULT_CONFLICTS);
            request.setQuery(esUpdateWrapper.getQueryBuilder());
            // 一次批处理的大小.因为是滚动处理的
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
                reindexObjectHandlerImpl.unLock(index);
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
        // 更新最大文档数
        request.setMaxDocs(GLOBAL_CONFIG.getMaxDocs());
        request.setMaxRetries(GLOBAL_CONFIG.getMaxRetries());
        request.setBatchSize(GLOBAL_CONFIG.getBatchSize());
        // 刷新索引
        request.setRefresh(true);
        // 使用滚动参数来控制“搜索上下文”存活的时间
        request.setScroll(TimeValue.timeValueMinutes(30));
        // 超时
        request.setTimeout(TimeValue.timeValueMinutes(30));
        // 更新时版本冲突
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
     * 删除所有
     */
    public void deleteAll(String index) {
        if (index.endsWith("_pro")) {
            throw new EsException("禁止删除");
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

    //统计
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
            //调用scroll处理
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

    // 聚合
    @Override
    public <T> EsAggregationsResponse<T> aggregations(String index, EsQueryWrapper<T> esQueryWrapper) {
        SearchRequest searchRequest = new SearchRequest();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(0);
        populateGroupField(esQueryWrapper, sourceBuilder);
        //设置索引
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        //查询
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
        //查询条件组合
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        EsSelect esSelect = esParamWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
                sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            }
        }
        sourceBuilder.size(GLOBAL_CONFIG.getSearchSize());
        //是否需要分页查询
        if (pageInfo != null) {
            //设置分页属性
            sourceBuilder.from((int) ((pageInfo.getPage() - 1) * pageInfo.getSize()));
            sourceBuilder.size((int) pageInfo.getSize());
        }
        //设置高亮
        if (esParamWrapper.getEsHighLights() != null) {
            List<EsHighLight> esHighLight = esParamWrapper.getEsHighLights();
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置为0获取全部内容
            highlightBuilder.numOfFragments(0);
            for (EsHighLight highLight : esHighLight) {
                //高亮字段
                highlightBuilder.field(highLight.getField());
                //高亮前后缀
                highlightBuilder.preTags(highLight.getPreTag())
                        .postTags(highLight.getPostTag())
                        .fragmentSize(highLight.getFragmentSize());
                sourceBuilder.highlighter(highlightBuilder);
            }
        }
        //超过1万条加了才能返回
        if (GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(true);
        }
        //排序
        if (!CollectionUtils.isEmpty(esParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esParamWrapper.getEsOrderList();
            orderFields.forEach(order -> {
                sourceBuilder.sort(new FieldSortBuilder(order.getName()).order(SortOrder.valueOf(order.getSort())));
            });
        }
        populateGroupField(esQueryWrapper, sourceBuilder);
        //设置索引
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        if (esParamWrapper.getSearchType() != null) {
            searchRequest.searchType();
        }
        //查询
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
        //获取结果集
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = new ArrayList<>();
        if (esParamWrapper.getEsHighLights() != null) {
            for (SearchHit hit : hitArray) {
                //获取高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                //将Json数据转化为实体对象
                Map<String, Object> map = hit.getSourceAsMap();
                if (highlightFields != null) {
                    highlightFields.forEach((k, v) -> {
                                Text[] texts = v.fragments();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (Text text : texts) {
                                    stringBuilder.append(text);
                                }
                                //高亮字段重新put进去
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
        return new EsResponse<T>(result, hits.getTotalHits().value, esAggregationsReponse);
    }


    //填充分组字段
    private void populateGroupField(EsQueryWrapper<?> esQueryWrapper, SearchSourceBuilder sourceBuilder) {
        EsLamdaAggregationWrapper<?> esLamdaAggregationWrapper = esQueryWrapper.esLamdaAggregationWrapper();
        EsAggregationWrapper<?> esAggregationWrapper = esQueryWrapper.esAggregationWrapper();
        if (esLamdaAggregationWrapper.getAggregationBuilder() != null) {
            for (BaseAggregationBuilder aggregation : esLamdaAggregationWrapper.getAggregationBuilder()) {
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
        EsUpdateField.Field updateFill = reindexObjectHandlerImpl.updateFill();
        sb.append("ctx._source." + updateFill.getName()).append(" = params.").append(updateFill.getName()).append(";");
        params.put(updateFill.getName(), updateFill.getValue());
    }

    protected Object handlerSaveParamter(Object esData) {
        esData = reindexObjectHandlerImpl.setInsertFeild(esData);
        return esData;
    }

    protected Object handlerUpdateParamter(Object esData) {
        esData = reindexObjectHandlerImpl.setUpdateFeild(esData);
        return esData;
    }

}
