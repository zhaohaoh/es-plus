package com.es.plus.es7.client;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.adapter.params.*;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.adapter.util.BeanUtils;
import com.es.plus.adapter.util.FieldUtils;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.adapter.util.ResolveUtils;
import com.es.plus.constant.EsConstant;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.es.plus.constant.EsConstant.PAINLESS;


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
    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

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
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(String index, String type, Collection<?> esDataList) {
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
                UpdateRequest updateRequest = new UpdateRequest(index, type, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
                updateRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
                // 如果没有文档则新增
                updateRequest.upsert(JsonUtils.toJsonStr(esData), XContentType.JSON);
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(updateRequest);
            }
            bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res = null;
            printInfoLog(index, "saveOrUpdateBatch data:{} hasFailures={}", JsonUtils.toJsonStr(esDataList));
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog(index, "saveOrUpdateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
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
        if (esIndexParam != null && esIndexParam.getChildClass() != null && esIndexParam.getChildClass().equals(clazz)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存批量
     */
    @Override
    public List<BulkItemResponse> saveBatch(String index, String type, Collection<?> esDataList) {
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
            bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res;

            printInfoLog(index, "saveBatch");
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    printErrorLog(index, "save error " + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
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
     * 保存
     */
    @Override
    public boolean save(String index, String type, Object esData) {
        List<BulkItemResponse> bulkItemResponses = saveBatch(index, type, Collections.singletonList(esData));
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
    public boolean update(String index, String type, Object esData) {

        boolean lock = false;
        boolean childIndex = isChildIndex(esData);
        try {
            if (reindexState) {
                lock = lock(index);
                if (lock) {
                    esData = handlerUpdateParamter(esData);
                }
            }

            UpdateRequest updateRequest = new UpdateRequest(index, type, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            //乐观锁重试次数
            updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
            updateRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            if (childIndex) {
                updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
            }
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                printErrorLog(index, "update data={}  error reason: doc  deleted", JsonUtils.toJsonStr(esData));
                return false;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                //noop标识没有数据改变。前后的值相同
                return false;
            } else {
                printInfoLog(index, "update success data={}", JsonUtils.toJsonStr(esData));
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
                printErrorLog(index, "es update data={}  error reason:  not found doc", JsonUtils.toJsonStr(esData));
                throw new ElasticsearchException(e);
            }
            throw e;
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
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(String index, String type, Collection<?> esDataList) {
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
                UpdateRequest updateRequest = new UpdateRequest(index, type, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(updateRequest);
            }
            bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            BulkResponse res = null;
            printInfoLog("updateBatch index={} data:{} hasFailures={}", index, JsonUtils.toJsonStr(esDataList));
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog(index, "updateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
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
     * 更新包装
     */
    @Override
    public <T> BulkByScrollResponse updateByWrapper(String index, String type, EsParamWrapper<T> esParamWrapper) {
        EsUpdateField esUpdateField = esParamWrapper.getEsUpdateField();
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
                } else if (!ResolveUtils.isCommonDataType(value.getClass()) && !ResolveUtils.isWrapClass(value.getClass())) {
                    value = BeanUtils.beanToMap(value);
                }
                //list直接覆盖 丢进去 无需再特殊处理
                params.put(name, value);
                sb.append("ctx._source.");
                sb.append(name).append(" = params.").append(name).append(";");
            }
            if (reindexState) {
                lock = lock(index);
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
            request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
            request.setQuery(esParamWrapper.getQueryBuilder());
            request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            //请求完成后立即刷新索引，保证读一致性
            request.setRefresh(true);
            //分片多线程执行任务
//            request.setSlices(2)
            String[] routings = esParamWrapper.getEsQueryParamWrapper().getRoutings();
            if (routings != null) {
                request.setRouting(routings[0]);
            }
            request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
            //一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
            //查询到数据后
            request.setRequestsPerSecond(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            Script painless = new Script(ScriptType.INLINE, PAINLESS, scipt, params);
            request.setScript(painless);
            printInfoLog("updateByWrapper index:{} requst: script:{},params={}", index, scipt, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("updateByWrapper index:{} response:{} update count={}", index, bulkResponse, bulkResponse.getUpdated());
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
    public <T> BulkByScrollResponse increment(String index, String type, EsParamWrapper<T> esParamWrapper) {
        boolean lock = false;
        List<EsUpdateField.Field> fields = esParamWrapper.getEsUpdateField().getIncrementFields();
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
        if (reindexState) {
            lock = lock(index);
            // 自定义字段处理
            if (lock) {
                handleObjectScript(script, params);
            }
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //版本号不匹配更新失败不停止
            request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
            request.setQuery(esParamWrapper.getQueryBuilder());
            // 一次批处理的大小.因为是滚动处理的 这里才是这是的批处理查询数据量
            request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            String[] routings = esParamWrapper.getEsQueryParamWrapper().getRoutings();
            if (routings != null) {
                request.setRouting(routings[0]);
            }
            request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
            //一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
            request.setRequestsPerSecond(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());

            Script painless = new Script(ScriptType.INLINE, PAINLESS, script.toString(), params);
            request.setScript(painless);

            printInfoLog(index, "updateByWrapper increment requst: script:{},params={}", script, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(index, "updateByWrapper increment response:{} update count=", bulkResponse);
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
    public boolean delete(String index, String type, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
        try {
            deleteRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            printInfoLog("delete index={}", index);
        } catch (IOException e) {
            throw new EsException("delete error", e);
        }
        return true;
    }

    @Override
    public <T> BulkByScrollResponse deleteByQuery(String index, String type, EsParamWrapper<T> esParamWrapper) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(esParamWrapper.getQueryBuilder());
        // 更新最大文档数
//        request.setMaxDocs(GlobalConfigCache.GLOBAL_CONFIG.getMaxDocs());
        request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        // 刷新索引
        request.setRefresh(true);
        // 使用滚动参数来控制“搜索上下文”存活的时间
        request.setScroll(TimeValue.timeValueMinutes(30));
        // 超时
        request.setTimeout(TimeValue.timeValueMinutes(30));
        // 更新时版本冲突
        request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        String[] routings = esParamWrapper.getEsQueryParamWrapper().getRoutings();
        if (routings != null) {
            request.setRouting(routings[0]);
        }

        try {
            SearchSourceBuilder source = request.getSearchRequest().source();
            printInfoLog(index, "delete body:" + source.toString());
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
        request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        request.setQuery(new MatchAllQueryBuilder());
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("deleteAll index={}", index);
        } catch (IOException e) {
            throw new EsException("delete error", e);
        }
    }

    @Override
    public boolean deleteBatch(String index, String type, Collection<String> esDataList) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return false;
        }
        log.info("Es deleteBatch index={} ids={}", index, esDataList);
        BulkRequest bulkRequest = new BulkRequest();
        esDataList.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            bulkRequest.add(deleteRequest);
        });

        bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
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
    public <T> long count(String index, String type, EsParamWrapper<T> esParamWrapper) {
        CountRequest countRequest = new CountRequest();
        SearchSourceBuilder query = SearchSourceBuilder.searchSource().query(esParamWrapper.getQueryBuilder());
        countRequest.source(query);
        countRequest.indices(index);
        CountResponse count = null;
        try {
            printInfoLog("count index=:{} body:{}", index, JsonUtils.toJsonStr(esParamWrapper.getQueryBuilder()));
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
    public <T> EsResponse<T> searchByWrapper(String index, String type, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        return search(null, esParamWrapper, tClass, index);
    }

    @Override
    public <T> EsResponse<T> searchPageByWrapper(String index, String type, PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        return search(pageInfo, esParamWrapper, tClass, index);
    }

    /**
     * 滚动包装器
     *
     * @param esParamWrapper es参数包装器
     * @param tClass         t类
     * @param index          索引
     * @param size           大小
     * @param keepTime       保持时间
     * @param scrollId       滚动id
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public <T> EsResponse<T> scrollByWrapper(String index, String type, EsParamWrapper<T> esParamWrapper, Class<T> tClass, int size, Duration keepTime, String scrollId) {
        SearchResponse searchResponse;
        SearchHit[] searchHits = null;
        List<T> result = new ArrayList<>();
        final Scroll scroll = new Scroll(TimeValue.timeValueMillis(keepTime.toMillis()));
        try {
            if (StringUtils.isNotBlank(scrollId)) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            } else {
                SearchRequest searchRequest = new SearchRequest(index);
                searchRequest.scroll(scroll);
                SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(null, size, esParamWrapper);
                searchRequest.source(searchSourceBuilder);
                //调用scroll处理
                searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }
            EsResponse<T> esResponse = gettEsResponse(tClass, esParamWrapper.getEsQueryParamWrapper(), searchResponse);
            if (searchHits == null || searchHits.length <= 0) {
                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
                boolean succeeded = clearScrollResponse.isSucceeded();
            }
            return esResponse;
        } catch (Exception e) {
            throw new EsException("scroll error", e);
        }
    }

    /**
     * 聚合
     */
    @Override
    public <T> EsAggResponse<T> aggregations(String index, String type, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        SearchRequest searchRequest = new SearchRequest();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esParamWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(0);
        populateGroupField(esParamWrapper, sourceBuilder);
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
            printInfoLog(index, "aggregations Time={}", end - start);
        } catch (Exception e) {
            throw new EsException("aggregations error", e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch aggregations error");
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlusAggregations<T> esAggregationReponse = new EsPlusAggregations<>();
        esAggregationReponse.setAggregations(aggregations);
        esAggregationReponse.settClass(tClass);
        return esAggregationReponse;
    }

    /**
     * 搜索后
     *
     * @param pageInfo       页面信息
     * @param esParamWrapper es参数包装器
     * @param tClass         t类
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public <T> EsResponse<T> searchAfter(String index, String type, PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        SearchRequest searchRequest = new SearchRequest();

        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();

        //获取查询语句源数据
        SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(pageInfo == null ? null : pageInfo.getPage(), pageInfo == null ? null : pageInfo.getSize(), esParamWrapper);

        if (pageInfo != null && pageInfo.getSearchAfterValues() != null) {
            sourceBuilder.searchAfter(pageInfo.getSearchAfterValues());
        }

        //设置查询语句源数据
        searchRequest.source(sourceBuilder);

        //设置索引
        searchRequest.indices(index);


        if (esQueryParamWrapper.getSearchType() != null) {
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

        EsResponse<T> esResponse = gettEsResponse(tClass, esQueryParamWrapper, searchResponse);

        return esResponse;
    }


    private <T> EsResponse<T> search(PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index) {
        SearchRequest searchRequest = new SearchRequest();

        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();

        //获取查询语句源数据
        SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(pageInfo == null ? null : pageInfo.getPage(), pageInfo == null ? null : pageInfo.getSize(), esParamWrapper);

        //设置查询语句源数据
        searchRequest.source(sourceBuilder);

        //设置索引
        searchRequest.indices(index);


        if (esQueryParamWrapper.getSearchType() != null) {
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
        EsResponse<T> esResponse = gettEsResponse(tClass, esQueryParamWrapper, searchResponse);
        return esResponse;
    }

    private <T> EsResponse<T> gettEsResponse(Class<T> tClass, EsQueryParamWrapper esQueryParamWrapper, SearchResponse searchResponse) {
        //获取结果集
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = new ArrayList<>();
        if (hitArray != null && hitArray.length > 0) {
            if (esQueryParamWrapper.getEsHighLights() != null) {
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
        }

        //设置聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlusAggregations<T> esAggsResponse = new EsPlusAggregations<>();
        esAggsResponse.setAggregations(aggregations);
        esAggsResponse.settClass(tClass);

        //设置返回结果
        EsResponse<T> esResponse = new EsResponse<>(result, hits.getTotalHits().value, esAggsResponse);
        esResponse.setShardFailures(searchResponse.getShardFailures());
        esResponse.setSkippedShards(searchResponse.getSkippedShards());
        esResponse.setTookInMillis(searchResponse.getTook().getMillis());
        esResponse.setSuccessfulShards(searchResponse.getSuccessfulShards());
        esResponse.setTotalShards(searchResponse.getTotalShards());
        esResponse.setScrollId(searchResponse.getScrollId());
        if (ArrayUtils.isNotEmpty(hitArray)) {
            esResponse.setFirstSortValues(hitArray[0].getSortValues());
            esResponse.setTailSortValues(hitArray[hitArray.length - 1].getSortValues());
        }

        //profile是性能分析类似mysql的explain
        if (esQueryParamWrapper.isProfile()) {
            Map<String, ProfileShardResult> profileResults = searchResponse.getProfileResults();
            esResponse.setProfileResults(profileResults);
        }
        return esResponse;
    }

    private <T> SearchSourceBuilder getSearchSourceBuilder(Integer page, Integer size, EsParamWrapper<T> esParamWrapper) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esParamWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        EsSelect esSelect = esQueryParamWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
                sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            }
        }
        boolean profile = esQueryParamWrapper.isProfile();
        if (profile) {
            sourceBuilder.profile(profile);
        }

        //是否需要分页查询
        if (page != null || size != null) {
            //设置分页属性
            if (size != null) {
                sourceBuilder.size(size);
                if (page != null) {
                    sourceBuilder.from(((page - 1) * size));
                }
            }
        } else {
            sourceBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getSearchSize());
        }

        //设置高亮
        if (esQueryParamWrapper.getEsHighLights() != null) {
            List<EsHighLight> esHighLight = esQueryParamWrapper.getEsHighLights();
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
        if (GlobalConfigCache.GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(true);
        }
        //排序
        if (!CollectionUtils.isEmpty(esQueryParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esQueryParamWrapper.getEsOrderList();
            orderFields.forEach(order -> {
                sourceBuilder.sort(new FieldSortBuilder(order.getName()).order(SortOrder.valueOf(order.getSort())));
            });
        }
        populateGroupField(esParamWrapper, sourceBuilder);
        return sourceBuilder;
    }

    //填充分组字段
    private <T> void populateGroupField(EsParamWrapper<T> esParamWrapper, SearchSourceBuilder sourceBuilder) {
        List<BaseAggregationBuilder> aggregationBuilders = esParamWrapper.getAggregationBuilder();
        if (aggregationBuilders != null) {
            for (BaseAggregationBuilder aggregation : aggregationBuilders) {
                if (aggregation instanceof AggregationBuilder) {
                    sourceBuilder.aggregation((AggregationBuilder) aggregation);
                } else {
                    sourceBuilder.aggregation((PipelineAggregationBuilder) aggregation);
                }
            }
        }
    }


    /**
     * 打印信息日志
     *
     * @param format 格式
     * @param params 参数个数
     */
    private void printInfoLog(String format, Object... params) {
        log.info("es-plus " + format, params);
    }

    /**
     * 打印错误日志
     *
     * @param format 格式
     * @param params 参数个数
     */
    private void printErrorLog(String index, String format, Object... params) {
        log.error("es-plus " + index + " " + format, params);
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
        return new EsUpdateField.Field(EsConstant.REINDEX_TIME_FILED, System.currentTimeMillis());
    }

    /**
     * 锁
     */
    public boolean lock(String index) {
        // 是在reindex索引重建.则获取更新锁  此处加上读写锁的原因如下
        // 1如果只有下面的锁的isLocked判断.那么判断还有锁.执行后续代码.但是刚好重建索引结束那么mappins就变了.并发问题,
        //  2如果对操作全加锁则并发度低.
        Lock readLock = esLockFactory.getReadWrtieLock(index + EsConstant.REINDEX_UPDATE_LOCK).readLock();
        boolean success = false;
        try {
            success = readLock.tryLock(3, TimeUnit.SECONDS);

            //获取reindex的锁
            boolean isLock = esLockFactory.getLock(index + EsConstant.REINDEX_LOCK_SUFFIX).isLocked();

            //如果不是锁定的直接释放
            if (!isLock) {
                //已经执行完reindex操作 那么释放更新锁
                reindexState = false;
                log.info("set enabledReindex false");
                if (success) {
                    readLock.unlock();
                }
                return false;
            }

            //如果获取锁失败并且是锁定的直接抛异常
            if (!success) {
                throw new EsException("index:" + index + " tryLock:" + EsConstant.REINDEX_UPDATE_LOCK + " fail");
            }
        } catch (InterruptedException ignored) {
        }
        return success;
    }

    //释放reindex锁
    public void unLock(String index) {
        Lock readLock = esLockFactory.getReadWrtieLock(index + EsConstant.REINDEX_UPDATE_LOCK).readLock();
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
