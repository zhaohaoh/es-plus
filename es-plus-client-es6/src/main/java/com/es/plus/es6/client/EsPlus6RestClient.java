package com.es.plus.es6.client;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.adapter.params.*;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.BeanUtils;
import com.es.plus.adapter.util.FieldUtils;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.adapter.util.ResolveUtils;
import com.es.plus.constant.EsConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
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
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
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
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.es.plus.constant.EsConstant.*;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsPlus6RestClient implements EsPlusClient {
    private static final Logger log = LoggerFactory.getLogger(EsPlus6RestClient.class);
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


    public EsPlus6RestClient(RestHighLevelClient restHighLevelClient, EsLockFactory esLockFactory) {
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
                    esDataList = esDataList.stream().map(this::handlerUpdateParamter).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Object esData : esDataList) {
                UpdateRequest updateRequest = new UpdateRequest(index, type, GlobalParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
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
            long start = System.currentTimeMillis();
            BulkResponse res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "saveOrUpdateBatch body:{} time:{}", JsonUtils.toJsonStr(esDataList), System.currentTimeMillis() - start);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog(index, "saveOrUpdateBatch one error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                }
            }
        } catch (Exception e) {
            printErrorLog(index, "saveOrUpdateBatch body:" + JsonUtils.toJsonStr(esDataList), e);
            throw new EsException("saveOrUpdateBatch Exception ", e);
        } finally {
            if (lock) {
                unLock(index);
            }
        }
        return responses;
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
                    esDataList = esDataList.stream().map(this::handlerSaveParamter).collect(Collectors.toList());
                    esDataList = esDataList.stream().map(this::handlerUpdateParamter).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();

            for (Object esData : esDataList) {
                IndexRequest indexRequest = new IndexRequest(index, type);
                indexRequest.id(GlobalParamHolder.getDocId(esData)).source(JsonUtils.toJsonStr(esData), XContentType.JSON);
                if (childIndex) {
                    indexRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(indexRequest);
            }
            bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());


            long start = System.currentTimeMillis();
            BulkResponse res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "saveBatch body:{} time:{}", JsonUtils.toJsonStr(esDataList), System.currentTimeMillis() - start);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    printErrorLog(index, "saveBatch error " + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                    failBulkItemResponses.add(bulkItemResponse);
                }
            }
        } catch (Exception e) {
            printErrorLog(index, "saveBatch  body:" + JsonUtils.toJsonStr(esDataList), e);
            throw new EsException("saveBatch Exception ", e);
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

            UpdateRequest updateRequest = new UpdateRequest(index, type, GlobalParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            //乐观锁重试次数
            updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
            updateRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
            if (childIndex) {
                updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
            }
            long start = System.currentTimeMillis();
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                printErrorLog(index, "update body={}  error reason: doc  deleted", JsonUtils.toJsonStr(esData));
                return false;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                //noop标识没有数据改变。前后的值相同
                return false;
            } else {
                printInfoLog(index, "update body={} time:{}", JsonUtils.toJsonStr(esData), System.currentTimeMillis() - start);
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
                printErrorLog(index, "es update body={}  error reason:  not found doc", JsonUtils.toJsonStr(esData));
                throw new ElasticsearchException(e);
            }
            throw e;
        } catch (Exception e) {
            printErrorLog(index, "update  body:" + JsonUtils.toJsonStr(esData), e);
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
                    esDataList = esDataList.stream().map(this::handlerUpdateParamter).collect(Collectors.toList());
                }
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Object esData : esDataList) {
                UpdateRequest updateRequest = new UpdateRequest(index, type, GlobalParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
                updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                bulkRequest.add(updateRequest);
            }
            bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());

            long start = System.currentTimeMillis();
            BulkResponse res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "updateBatch body:{} time:{}", JsonUtils.toJsonStr(esDataList), System.currentTimeMillis() - start);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    printErrorLog(index, "updateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                }
            }
        } catch (Exception e) {
            printErrorLog(index, "updateBatch body:" + JsonUtils.toJsonStr(esDataList), e);
            throw new EsException("updateBatch Exception", e);
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
        String script = esUpdateField.getScipt();
        Map<String, Object> params = esUpdateField.getSciptParams();
        boolean lock = false;
        if (StringUtils.isBlank(script)) {
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
            script = sb.toString();
        }
        try {
            UpdateByQueryRequest request = getUpdateByQueryRequest(index, type, params, script, esParamWrapper);
            long start = System.currentTimeMillis();
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(index, "updateByWrapper script:{},params={} response:{} update count={} time:{}", script, params, bulkResponse, bulkResponse.getUpdated(),
                    System.currentTimeMillis() - start);
            return bulkResponse;
        } catch (Exception e) {
            printErrorLog(index, "updateByWrapper script:" + script + " params:" + params + " queryBuilder:" + esParamWrapper.getQueryBuilder(), e);
            throw new EsException("updateByWrapper Exception", e);
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
            UpdateByQueryRequest request = getUpdateByQueryRequest(index, type, params, script.toString(), esParamWrapper);
            printInfoLog(index, "increment requst: script:{},params={}", script, params);
            long start = System.currentTimeMillis();
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(index, "increment response:{} time:{}", bulkResponse, System.currentTimeMillis() - start);
            return bulkResponse;
        } catch (Exception e) {
            printErrorLog(index, "increment  script:" + script + "params:" + params, e);
            throw new EsException("increment Exception", e);
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
            long start = System.currentTimeMillis();
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "delete id:{}", id, System.currentTimeMillis() - start);
        } catch (Exception e) {
            printErrorLog(index, "delete  id:" + id, e);
            throw new EsException("delete error", e);
        }
        return true;
    }

    /**
     * 删除根据查询
     *
     * @param index          索引
     * @param esParamWrapper es参数包装器
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public <T> BulkByScrollResponse deleteByQuery(String index, String type, EsParamWrapper<T> esParamWrapper) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index, type);
        request.setQuery(esParamWrapper.getQueryBuilder());
        // 更新最大文档数
        request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        // 刷新索引
        request.setRefresh(true);
        // 使用滚动参数来控制“搜索上下文”存活的时间
        request.setScroll(TimeValue.timeValueMinutes(1));
        // 超时
        request.setTimeout(TimeValue.timeValueMinutes(1));
        // 更新时版本冲突
        request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        String[] routings = esParamWrapper.getEsQueryParamWrapper().getRoutings();
        if (routings != null) {
            request.setRouting(routings[0]);
        }
        SearchSourceBuilder source = request.getSearchRequest().source();
        try {
            long start = System.currentTimeMillis();
            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(index, "deleteByQuery body:{} time:{}" + source.toString(), System.currentTimeMillis() - start);
            return bulkByScrollResponse;
        } catch (Exception e) {
            printErrorLog(index, "deleteByQuery  body:" + source, e);
            throw new EsException("deleteByQuery error", e);
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
            long start = System.currentTimeMillis();
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(index, "deleteAll time:{}", System.currentTimeMillis() - start);
        } catch (Exception e) {
            throw new EsException("deleteAll error", e);
        }
    }

    /**
     * 删除批处理
     *
     * @param index      索引
     * @param type       类型
     * @param esDataList es数据列表
     * @return boolean
     */
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
            long start = System.currentTimeMillis();
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "deleteBatch body:{} time:{}", JsonUtils.toJsonStr(esDataList), System.currentTimeMillis() - start);
            BulkItemResponse[] items = bulkResponse.getItems();
            for (BulkItemResponse item : items) {
                if (item.isFailed()) {
                    printErrorLog(index, "deleteBatch id={} FailureMessage=:{}", item.getId(), item.getFailureMessage());
                }
            }
        } catch (Exception e) {
            printErrorLog(index, "deleteBatch body:" + JsonUtils.toJsonStr(esDataList), e);
            throw new EsException("es deleteBatch error", e);
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
        countRequest.types(type);
        CountResponse count = null;
        try {
            long start = System.currentTimeMillis();
            count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "count body:{} time:{}", JsonUtils.toJsonStr(esParamWrapper.getQueryBuilder()), System.currentTimeMillis() - start);
        } catch (Exception e) {
            printErrorLog(index, "count body:" + esParamWrapper.getQueryBuilder(), e);
            throw new EsException("es-plus count error ", e);
        }
        if (count != null) {
            return count.getCount();
        }
        return 0;
    }

    @Override
    public <T> EsResponse<T> search(String index, String type, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        SearchRequest searchRequest = new SearchRequest();

        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();

        //获取查询语句源数据
        SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(esParamWrapper);

        populateSearchRequest(index, type, searchRequest, esQueryParamWrapper, sourceBuilder);

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
        EsResponse<T> esResponse = getEsResponse(tClass, esQueryParamWrapper, searchResponse);
        return esResponse;
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
    public <T> EsResponse<T> scroll(String index, String type, EsParamWrapper<T> esParamWrapper, Class<T> tClass, int size, Duration keepTime, String scrollId) {
        SearchResponse searchResponse;
        SearchHit[] searchHits;
        final Scroll scroll = new Scroll(TimeValue.timeValueMillis(keepTime.toMillis()));
        try {
            long start = System.currentTimeMillis();
            if (StringUtils.isNotBlank(scrollId)) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            } else {
                SearchRequest searchRequest = new SearchRequest(index);
                searchRequest.types(type);
                searchRequest.scroll(scroll);
                SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(esParamWrapper);
                searchRequest.source(searchSourceBuilder);
                //调用scroll处理
                searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }
            EsResponse<T> esResponse = getEsResponse(tClass, esParamWrapper.getEsQueryParamWrapper(), searchResponse);
            if (searchHits == null || searchHits.length <= 0) {
                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
                boolean succeeded = clearScrollResponse.isSucceeded();
            }
            printInfoLog(index, "scrollByWrapper   scrollId:{} esParamWrapper:{} time:{}", scrollId, esParamWrapper.toString(), System.currentTimeMillis() - start);
            return esResponse;
        } catch (Exception e) {
            printErrorLog(index, "scrollByWrapper body:" + esParamWrapper.getQueryBuilder() + " scrollId:" + scrollId, e);
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
        searchRequest.types(type);
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            printInfoLog(index, "aggregations body:{} time:{}", sourceBuilder, System.currentTimeMillis() - start);
        } catch (Exception e) {
            printErrorLog(index, "aggregations body:" + sourceBuilder, e);
            throw new EsException("aggregations error", e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch aggregations error");
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlus6Aggregations<T> esAggregationReponse = new EsPlus6Aggregations<>();
        esAggregationReponse.setAggregations(aggregations);
        esAggregationReponse.settClass(tClass);
        return esAggregationReponse;
    }


    @Override
    public String executeDSL(String dsl, String indexName) {
        Request request = new Request("get", indexName + "_search");
        request.setJsonEntity(dsl);
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("executeDSL", e);
        }
        return null;
    }


    private <T> EsResponse<T> getEsResponse(Class<T> tClass, EsQueryParamWrapper esQueryParamWrapper, SearchResponse searchResponse) {
        //获取结果集
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = new ArrayList<>();
        if (hitArray != null && hitArray.length > 0) {
            Arrays.stream(hitArray)
                    .filter(hit -> StringUtils.isNotBlank(hit.getSourceAsString()))
                    .map(hit -> {
                        T bean = JsonUtils.toBean(hit.getSourceAsString(), tClass);
                        if (tClass.equals(Map.class)) {
                            return bean;
                        }
                        //设置高亮
                        setHighLishtField(hit, bean);
                        //设置分数
                        setScore(hit, bean);
                        return bean;
                    }).forEach(result::add);
        }
        EsHits esHits = setInnerHits(hits);


        //设置聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlus6Aggregations<T> esAggsResponse = new EsPlus6Aggregations<>();
        esAggsResponse.setAggregations(aggregations);
        esAggsResponse.settClass(tClass);

        //设置返回结果
        EsResponse<T> esResponse = new EsResponse<>(result, hits.getTotalHits(), esAggsResponse);
        esResponse.setShardFailures(searchResponse.getShardFailures());
        esResponse.setSkippedShards(searchResponse.getSkippedShards());
        esResponse.setTookInMillis(searchResponse.getTook().getMillis());
        esResponse.setSuccessfulShards(searchResponse.getSuccessfulShards());
        esResponse.setTotalShards(searchResponse.getTotalShards());
        esResponse.setScrollId(searchResponse.getScrollId());
        esResponse.setInnerHits(esHits);
        // 设置最小和最大的排序字段值
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

    private EsHits setInnerHits(SearchHits hits) {
        if (hits == null||ArrayUtils.isEmpty(hits.getHits())){
            return null;
        }
        long totalHits = hits.getTotalHits();
        EsHits esHits = new EsHits();
        esHits.setTotal(totalHits);
        List<EsHit> esHitList = new ArrayList<>();
        esHits.setEsHitList(esHitList);
        for (SearchHit searchHit : hits.getHits()) {
            EsHit esHit = new EsHit();
            esHit.setData(searchHit.getSourceAsString());
            esHitList.add(esHit);
            Map<String, SearchHits> innerHits = searchHit.getInnerHits();

            // 填充innerHits
            if (CollectionUtils.isEmpty(innerHits)) {
                Map<String, EsHits> esHitsMap = new HashMap<>();
                innerHits.forEach((k, v) -> {
                    EsHits eshits = setInnerHits(v);
                    esHitsMap.put(k, eshits);
                });
                esHit.setInnerHitsMap(esHitsMap);
            }
        }
        return esHits;
    }


    private <T> SearchSourceBuilder getSearchSourceBuilder(EsParamWrapper<T> esParamWrapper) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        Integer page = esQueryParamWrapper.getPage();
        Integer size = esQueryParamWrapper.getSize();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esParamWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);

        EsSelect esSelect = esQueryParamWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
                sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            }
            if (esSelect.getFetch() != null) {
                sourceBuilder.fetchSource(esSelect.getFetch());
            }
            if (esSelect.getMinScope() != null) {
                sourceBuilder.minScore(esSelect.getMinScope());
            }
            if (esSelect.getTrackScores() != null) {
                sourceBuilder.trackScores(esSelect.getTrackScores());
            }
        }
        boolean profile = esQueryParamWrapper.isProfile();
        if (profile) {
            sourceBuilder.profile(profile);
        }
        //searchAfter
        if (esQueryParamWrapper.getSearchAfterValues() != null) {
            sourceBuilder.searchAfter(esQueryParamWrapper.getSearchAfterValues());
            sourceBuilder.size(size);
        }
        //是否需要分页查询
        else if (page != null || size != null) {
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
     * 获取更新根据查询请求
     *
     * @param index          索引
     * @param params         参数个数
     * @param script         脚本
     * @param esParamWrapper es参数包装器
     * @return {@link UpdateByQueryRequest}
     */
    private <T> UpdateByQueryRequest getUpdateByQueryRequest(String index, String type, Map<String, Object> params, String script, EsParamWrapper<T> esParamWrapper) {
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        request.setDocTypes(type);
        //版本号不匹配更新失败不停止
        request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        request.setQuery(esParamWrapper.getQueryBuilder());
        request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        //请求完成后立即刷新索引，保证读一致性
        request.setRefresh(true);
        request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        //一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
        request.setRequestsPerSecond(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        String[] routings = esParamWrapper.getEsQueryParamWrapper().getRoutings();
        if (routings != null) {
            request.setRouting(routings[0]);
        }
        Script painless = new Script(ScriptType.INLINE, PAINLESS, script, params);
        request.setScript(painless);
        return request;
    }

    /**
     * 打印信息日志
     *
     * @param format 格式
     * @param params 参数个数
     */
    private void printInfoLog(String index, String format, Object... params) {
        boolean enableSearchLog = GlobalConfigCache.GLOBAL_CONFIG.isEnableSearchLog();
        if (enableSearchLog) {
            log.info("es-plus " + index + " " + format, params);
        }
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

    private void printErrorLog(String index, String format, Exception e) {
        log.error("es-plus " + index + " " + format, e);
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

    /**
     * 释放锁
     *
     * @param index 索引
     *///释放reindex锁
    private void unLock(String index) {
        Lock readLock = esLockFactory.getReadWrtieLock(index + EsConstant.REINDEX_UPDATE_LOCK).readLock();
        readLock.unlock();
    }

    /**
     * 设置更新行业
     *
     * @param object 对象
     * @return {@link Object}
     */
    private Object setUpdateFeild(Object object) {
        EsUpdateField.Field updateFill = updateFill();
        if (updateFill == null) {
            return object;
        }
        Map<String, Object> beanToMap = BeanUtils.beanToMap(object);
        beanToMap.put(updateFill.getName(), updateFill.getValue());
        return beanToMap;
    }

    /**
     * 是孩子索引
     *
     * @param esData es数据
     * @return boolean
     */
    private boolean isChildIndex(Object esData) {
        Class<?> clazz = esData.getClass();
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(clazz);
        if (esIndexParam != null && esIndexParam.getChildClass() != null && esIndexParam.getChildClass().equals(clazz)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置分数
     */
    private <T> void setScore(SearchHit hit, T bean) {
        float score = hit.getScore();
        if (!Float.isNaN(score)) {
            EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(bean.getClass());
            try {
                Field field = bean.getClass().getDeclaredField(esIndexParam.getScoreField());
                field.setAccessible(true);
                field.set(bean, score);
            } catch (Exception e) {
                log.error("setScore ", e);
            }
        }
    }

    /**
     * 设置高亮
     *
     * @param hit  打击
     * @param bean 豆
     */
    private <T> void setHighLishtField(SearchHit hit, T bean) {
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        if (highlightFields != null && highlightFields.size() > 0) {
            highlightFields.forEach((k, v) -> {
                        Text[] texts = v.fragments();
                        StringBuilder highlightStr = new StringBuilder();
                        for (Text text : texts) {
                            highlightStr.append(text);
                        }
                        try {
                            //高亮字段重新put进去
                            Field field = bean.getClass().getDeclaredField(k);
                            field.setAccessible(true);
                            field.set(bean, highlightStr.toString());
                        } catch (Exception e) {
                            log.error("es-plus HighlightFields Exception", e);
                        }
                    }
            );

        }
    }

    /**
     * 填充搜索请求
     *
     * @param index               索引
     * @param searchRequest       搜索请求
     * @param esQueryParamWrapper es查询参数包装器
     * @param sourceBuilder       源构建器
     */
    private void populateSearchRequest(String index, String type, SearchRequest searchRequest, EsQueryParamWrapper esQueryParamWrapper, SearchSourceBuilder sourceBuilder) {
        //设置查询语句源数据
        searchRequest.source(sourceBuilder);

        //设置索引
        searchRequest.indices(index);
        searchRequest.types(type);

        //设置偏好
        searchRequest.preference(esQueryParamWrapper.getPreference());

        if (esQueryParamWrapper.getSearchType() != null) {
            searchRequest.searchType();
        }
    }
}
