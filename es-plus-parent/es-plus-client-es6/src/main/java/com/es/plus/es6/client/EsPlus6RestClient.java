package com.es.plus.es6.client;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.interceptor.EsUpdateField;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsHighLight;
import com.es.plus.adapter.params.EsHit;
import com.es.plus.adapter.params.EsHits;
import com.es.plus.adapter.params.EsOrder;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsSelect;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.BeanUtils;
import com.es.plus.adapter.util.FieldUtils;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.adapter.util.ResolveUtils;
import com.es.plus.adapter.util.SearchHitsUtil;
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
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.es.plus.constant.EsConstant.PAINLESS;


/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsPlus6RestClient implements EsPlusClient {
    
    private static final Logger log = LoggerFactory.getLogger(EsPlus6RestClient.class);
    
    private final RestHighLevelClient restHighLevelClient;
    
    private final EsLockFactory esLockFactory;
    
    @Override
    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
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
    public List<BulkItemResponse> saveOrUpdateBatch(String type, Collection<?> esDataList, String... indexs) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            
            boolean lock = false;
            try {
                BulkRequest bulkRequest = new BulkRequest();
                for (Object esData : esDataList) {
                    UpdateRequest updateRequest = new UpdateRequest(index, type,
                            GlobalParamHolder.getDocId(index, esData)).doc(JsonUtils.toJsonStr(esData),
                            XContentType.JSON);
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
                printInfoLog(" {} saveOrUpdateBatch data:{} hasFailures={}", index, JsonUtils.toJsonStr(esDataList));
                res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                for (BulkItemResponse bulkItemResponse : res.getItems()) {
                    if (bulkItemResponse.isFailed()) {
                        responses.add(bulkItemResponse);
                        printErrorLog(" {} saveOrUpdateBatch error" + bulkItemResponse.getId() + " message:"
                                + bulkItemResponse.getFailureMessage(), index);
                    }
                }
            } catch (IOException e) {
                throw new EsException("saveOrUpdateBatch IOException", e);
            }
        }
        
        return responses;
    }
    
    private boolean isChildIndex(Object esData) {
        Class<?> clazz = esData.getClass();
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(clazz);
        if (esIndexParam != null && esIndexParam.getChildClass() != null && esIndexParam.getChildClass()
                .equals(clazz)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 保存批量
     */
    @Override
    public List<BulkItemResponse> saveBatch(String type, Collection<?> esDataList, String... indexs) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        for (String index : indexs) {
            boolean lock = false;
            
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            try {
                BulkRequest bulkRequest = new BulkRequest();
                
                for (Object esData : esDataList) {
                    IndexRequest indexRequest = new IndexRequest(index);
                    String source = JsonUtils.toJsonStr(esData);
                    indexRequest.id(GlobalParamHolder.getDocId(index, esData)).source(source, XContentType.JSON);
                    if (childIndex) {
                        indexRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                    }
                    bulkRequest.add(indexRequest);
                }
                bulkRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
                BulkResponse res;
                
                printInfoLog("saveBatch {}", index);
                res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                for (BulkItemResponse bulkItemResponse : res.getItems()) {
                    if (bulkItemResponse.isFailed()) {
                        printErrorLog(" {} save error " + bulkItemResponse.getId() + " message:"
                                + bulkItemResponse.getFailureMessage(), index);
                        failBulkItemResponses.add(bulkItemResponse);
                    }
                }
            } catch (IOException e) {
                throw new EsException("SaveBatch IOException", e);
            }
        }
        
        return failBulkItemResponses;
    }
    
    /**
     * 保存
     */
    @Override
    public boolean save(String type, Object esData, String... indexs) {
        
        List<BulkItemResponse> bulkItemResponses = saveBatch(type, Collections.singletonList(esData), indexs);
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
    public boolean update(String type, Object esData,String... indexs) {
        boolean childIndex = isChildIndex(esData);
        for (String index : indexs) {
            try {
                UpdateRequest updateRequest = new UpdateRequest(index, type, GlobalParamHolder.getDocId(index, esData)).doc(
                        JsonUtils.toJsonStr(esData), XContentType.JSON);
                //乐观锁重试次数
                updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
                updateRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
                if (childIndex) {
                    updateRequest.routing(FieldUtils.getStrFieldValue(esData, "joinField", "parent"));
                }
                UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                    printErrorLog(" {} update data={}  error reason: doc  deleted", index, JsonUtils.toJsonStr(esData));
                    return false;
                } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                    //noop标识没有数据改变。前后的值相同
                    return false;
                } else {
                    printInfoLog(" {} update success data={}", index, JsonUtils.toJsonStr(esData));
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
                    printErrorLog(" {} update data={}  error reason:  not found doc", index, JsonUtils.toJsonStr(esData));
                    //                throw new ElasticsearchException(e);
                }
            } catch (Exception e) {
                throw new EsException("update error", e);
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
    public List<BulkItemResponse> updateBatch(String type, Collection<?> esDataList,String... indexs) {
        List<BulkItemResponse> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            try {
                BulkRequest bulkRequest = new BulkRequest();
                for (Object esData : esDataList) {
                    UpdateRequest updateRequest = new UpdateRequest(index, type,
                            GlobalParamHolder.getDocId(index, esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
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
                        printErrorLog("updateBatch {} error" + bulkItemResponse.getId() + " message:"
                                + bulkItemResponse.getFailureMessage(), index);
                    }
                }
            } catch (IOException e) {
                throw new EsException("updateBatch IOException", e);
            }
        }
        
        
        return responses;
    }
    
    
    /**
     * 更新包装
     */
    @Override
    public <T> BulkByScrollResponse updateByWrapper(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        EsUpdateField esUpdateField = esParamWrapper.getEsUpdateField();
        List<EsUpdateField.Field> fields = esUpdateField.getFields();
        String scipt = esUpdateField.getScipt();
        Map<String, Object> params = esUpdateField.getSciptParams();
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
                } else if (!ResolveUtils.isCommonDataType(value.getClass()) && !ResolveUtils.isWrapClass(
                        value.getClass())) {
                    value = BeanUtils.beanToMap(value);
                }
                //list直接覆盖 丢进去 无需再特殊处理
                params.put(name, value);
                sb.append("ctx._source.");
                sb.append(name).append(" = params.").append(name).append(";");
            }
            scipt = sb.toString();
        }
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //版本号不匹配更新失败不停止
            request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
            request.setQuery(esQueryParamWrapper.getQueryBuilder());
            request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            //请求完成后立即刷新索引，保证读一致性
            request.setRefresh(true);
            //分片多线程执行任务
            //            request.setSlices(2)
            String[] routings = esQueryParamWrapper.getRoutings();
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
            printInfoLog("updateByWrapper index:{} requst: script:{},params={}  query:{}", index, scipt, params,
                    esQueryParamWrapper.getQueryBuilder().toString());
            BulkByScrollResponse bulkResponse = restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog("updateByWrapper index:{} response:{} update count={}", index, bulkResponse,
                    bulkResponse.getUpdated());
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper IOException", e);
        }
    }
    
    @Override
    public <T> BulkByScrollResponse increment(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
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
        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(index);
            //版本号不匹配更新失败不停止
            request.setConflicts(EsConstant.DEFAULT_CONFLICTS);
            request.setQuery(esQueryParamWrapper.getQueryBuilder());
            // 一次批处理的大小.因为是滚动处理的 这里才是这是的批处理查询数据量
            request.setBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            String[] routings = esQueryParamWrapper.getRoutings();
            if (routings != null) {
                request.setRouting(routings[0]);
            }
            request.setMaxRetries(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
            //一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
            request.setRequestsPerSecond(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            
            Script painless = new Script(ScriptType.INLINE, PAINLESS, script.toString(), params);
            request.setScript(painless);
            
            printInfoLog(" {} updateByWrapper increment requst: script:{},params={}", index, script, params);
            BulkByScrollResponse bulkResponse = restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            printInfoLog(" {} updateByWrapper increment response:{} update count=", index, bulkResponse);
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper increment IOException", e);
        }
    }
    
    @Override
    public boolean delete( String type, String id,String... indexs) {
        for (String index : indexs) {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            try {
                deleteRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());
                restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                printInfoLog("delete index={}", index);
            } catch (IOException e) {
                throw new EsException("delete error", e);
            }
        }
        
        return true;
    }
    
    @Override
    public <T> BulkByScrollResponse deleteByQuery(String type, EsParamWrapper<T> esParamWrapper, String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(esQueryParamWrapper.getQueryBuilder());
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
            printInfoLog(" {} delete body:" + source.toString(), index);
            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(request,
                    RequestOptions.DEFAULT);
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
    public boolean deleteBatch( String type, Collection<String> esDataList,String... indexs) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return false;
        }
        for (String index : indexs) {
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
                        printErrorLog("deleteBatch index={} id={} FailureMessage=:{}", index, item.getId(),
                                item.getFailureMessage());
                    }
                }
            } catch (IOException e) {
                throw new EsException("es delete error", e);
            }
        }
        return true;
    }
    
    //统计
    @Override
    public <T> long count(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        CountRequest countRequest = new CountRequest();
        SearchSourceBuilder query = SearchSourceBuilder.searchSource().query(esQueryParamWrapper.getQueryBuilder());
        countRequest.source(query);
        countRequest.indices(index);
        CountResponse count = null;
        try {
            printSearchInfoLog("count index=:{} body:{}", index,
                    JsonUtils.toJsonStr(esQueryParamWrapper.getQueryBuilder()));
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
    public <T> EsResponse<T> search(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        SearchRequest searchRequest = new SearchRequest();
        
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        
        //获取查询语句源数据
        SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(esParamWrapper);
        
        populateSearchRequest(type, searchRequest, esQueryParamWrapper, sourceBuilder,index);
        
        if (esQueryParamWrapper.getSearchType() != null) {
            searchRequest.searchType();
        }
        
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            long millis = end - start;
            printSearchInfoLog("search index={} body:{} tookMills={}", index, sourceBuilder, millis);
        } catch (Exception e) {
            throw new EsException("es-plus search body=" + sourceBuilder, e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("es-plus search error:" + searchResponse.status().getStatus());
        }
        EsResponse<T> esResponse = getEsResponse(esParamWrapper.getTClass(), searchResponse);
        return esResponse;
    }
    
    /**
     * 滚动包装器
     *
     * @param esParamWrapper es参数包装器
     * @param index          索引
     * @param keepTime       保持时间
     * @param scrollId       滚动id
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public <T> EsResponse<T> scroll(String type, EsParamWrapper<T> esParamWrapper, Duration keepTime,
            String scrollId,String... index) {
        SearchResponse searchResponse;
        SearchHit[] searchHits = null;
        List<T> result = new ArrayList<>();
        final Scroll scroll = new Scroll(TimeValue.timeValueMillis(keepTime.toMillis()));
        try {
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            if (StringUtils.isNotBlank(scrollId)) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            } else {
                SearchRequest searchRequest = new SearchRequest(index);
                searchRequest.scroll(scroll);
                SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(esParamWrapper);
                populateSearchRequest( type, searchRequest, esQueryParamWrapper, searchSourceBuilder,index);
                //调用scroll处理
                searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }
            EsResponse<T> esResponse = getEsResponse(esParamWrapper.getTClass(), searchResponse);
            if (searchHits == null || searchHits.length <= 0) {
                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                ClearScrollResponse clearScrollResponse = restHighLevelClient
                        .clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
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
    public <T> EsAggResponse<T> aggregations( String type, EsParamWrapper<T> esParamWrapper,String... index) {
        
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        SearchRequest searchRequest = new SearchRequest();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esQueryParamWrapper.getQueryBuilder();
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
            printSearchInfoLog("aggregations index={} body:{}", index, sourceBuilder);
            long start = System.currentTimeMillis();
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            long millis = end - start;
            printSearchInfoLog("{} aggregations tookMills={}", index, millis);
        } catch (Exception e) {
            throw new EsException("aggregations error", e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch aggregations error");
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlus6Aggregations<T> esAggregationReponse = new EsPlus6Aggregations<>();
        esAggregationReponse.setAggregations(aggregations);
        esAggregationReponse.settClass(esParamWrapper.getTClass());
        return esAggregationReponse;
    }
    
    
    /**
     * 填充搜索请求
     *
     * @param index               索引
     * @param searchRequest       搜索请求
     * @param esQueryParamWrapper es查询参数包装器
     * @param sourceBuilder       源构建器
     */
    private void populateSearchRequest(String type, SearchRequest searchRequest,
            EsQueryParamWrapper esQueryParamWrapper, SearchSourceBuilder sourceBuilder,String ... index) {
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
    
    @Override
    public String executeDSL(String dsl, String... index) {
        String indexs = String.join(",", index);
        Request request = new Request("get", indexs + "_search");
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
    
    @Override
    public String translateSql(String sql) {
        Map<String, Object> jsonRequest = new HashMap<>();
        jsonRequest.put("query",sql);
        Request request = new Request("post", "/_xpack/sql/translate");
        request.setJsonEntity(JsonUtils.toJsonStr(jsonRequest));
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("executeSql", e);
        }
        return null;
    }
    @Override
    public <T> EsResponse<T> executeSQL(String sql,Class<T> tClass) {
        String dsl = translateSql(sql);
        // 匹配 SQL 语句中的表名
        Pattern pattern = Pattern.compile("FROM\\s+([a-zA-Z_]+)");
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        // 提取表名
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        if (StringUtils.isBlank(tableName)){
            throw new EsException("sql语句中未找到表名");
        }
        String rs = executeDSL(dsl, tableName);
        XContent xContent = XContentFactory.xContent(XContentType.JSON);
        XContentParser parser = null;
        try {
            parser = xContent.createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, rs);
            SearchResponse searchResponse = SearchResponse.fromXContent(parser);
            return getEsResponse(tClass,searchResponse);
        } catch (IOException e) {
            throw new EsException("result parse error",e);
        }
    }
    
    
    private <T> EsResponse<T> getEsResponse(Class<T> tClass,
            SearchResponse searchResponse) {
        //获取结果集
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = SearchHitsUtil.parseList(tClass, hitArray);
        EsHits esHits = setInnerHits(hits, false);
        //设置聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        EsPlus6Aggregations<T> esAggsResponse = new EsPlus6Aggregations<>();
        esAggsResponse.setAggregations(aggregations);
        esAggsResponse.settClass(tClass);
        
        //设置返回结果
        EsResponse<T> esResponse = new EsResponse<>(result, hits.getTotalHits(), esAggsResponse);
        esResponse.setShardFailures(searchResponse.getShardFailures());
        esResponse.setSkippedShards(searchResponse.getSkippedShards());
//        esResponse.setTookInMillis(searchResponse.getTook().getMillis());
        esResponse.setSuccessfulShards(searchResponse.getSuccessfulShards());
        esResponse.setTotalShards(searchResponse.getTotalShards());
        esResponse.setScrollId(searchResponse.getScrollId());
        esResponse.setInnerHits(esHits);
        esResponse.setSourceResponse(searchResponse);
        // 设置最小和最大的排序字段值
        if (ArrayUtils.isNotEmpty(hitArray)) {
            esResponse.setFirstSortValues(hitArray[0].getSortValues());
            esResponse.setTailSortValues(hitArray[hitArray.length - 1].getSortValues());
        }
        
        //profile是性能分析类似mysql的explain
        if (searchResponse.getProfileResults()!=null) {
            Map<String, ProfileShardResult> profileResults = searchResponse.getProfileResults();
            esResponse.setProfileResults(profileResults);
        }
        return esResponse;
    }
    
    
    private EsHits setInnerHits(SearchHits hits, boolean populate) {
        if (hits == null || ArrayUtils.isEmpty(hits.getHits())) {
            return null;
        }
        //如果没有嵌套类则不填充
        boolean anyMatch = Arrays.stream(hits.getHits()).anyMatch(a -> CollectionUtils.isEmpty(a.getInnerHits()));
        if (anyMatch) {
            return null;
        }
        
        long totalHits = hits.getTotalHits();
        EsHits esHits = new EsHits();
        esHits.setTotal(totalHits);
        
        List<EsHit> esHitList = new ArrayList<>();
        esHits.setEsHitList(esHitList);
        for (SearchHit searchHit : hits.getHits()) {
            EsHit esHit = new EsHit();
            //一级数据不填充
            if (populate) {
                String sourceAsString = searchHit.getSourceAsString();
                esHit.setData(sourceAsString);
            }
            esHitList.add(esHit);
            Map<String, SearchHits> innerHits = searchHit.getInnerHits();
            
            // 填充innerHits
            if (!CollectionUtils.isEmpty(innerHits)) {
                Map<String, EsHits> esHitsMap = new HashMap<>();
                innerHits.forEach((k, v) -> {
                    EsHits eshits = setInnerHits(v, true);
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
        BoolQueryBuilder queryBuilder = esQueryParamWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        
        //超过1万条加了才能返回
        if (GlobalConfigCache.GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(true);
        }
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
            if (esSelect.getTrackTotalHits() != null) {
                sourceBuilder.trackTotalHits(esSelect.getTrackTotalHits());
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
                highlightBuilder.preTags(highLight.getPreTag()).postTags(highLight.getPostTag())
                        .fragmentSize(highLight.getFragmentSize());
                sourceBuilder.highlighter(highlightBuilder);
            }
        }
        
        //排序
        if (!CollectionUtils.isEmpty(esQueryParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esQueryParamWrapper.getEsOrderList();
            orderFields.forEach(order -> {
                sourceBuilder.sort(new FieldSortBuilder(order.getName())
                        .order(SortOrder.valueOf(order.getSort().toUpperCase(Locale.ROOT))));
            });
        }
        populateGroupField(esParamWrapper, sourceBuilder);
        
        Integer searchTimeout = GlobalConfigCache.GLOBAL_CONFIG.getSearchTimeout();
        sourceBuilder.timeout(TimeValue.timeValueSeconds(searchTimeout));
        
        return sourceBuilder;
    }
    
    //填充分组字段
    private <T> void populateGroupField(EsParamWrapper<T> esParamWrapper, SearchSourceBuilder sourceBuilder) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        List<BaseAggregationBuilder> aggregationBuilders = esQueryParamWrapper.getAggregationBuilder();
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
    /**
     * 打印信息日志
     *
     * @param format 格式
     * @param params 参数个数
     */
    private void printInfoLog(String format, Object... params) {
        log.info("es-plus " + format, params);
    }
    
    
    private void printSearchInfoLog(String format, Object... params) {
        boolean enableSearchLog = GlobalConfigCache.GLOBAL_CONFIG.isEnableSearchLog();
        if (enableSearchLog) {
            log.info("es-plus " + format, params);
        }
    }
    
    
    /**
     * 打印错误日志
     *
     * @param format 格式
     * @param params 参数个数
     */
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
        return new EsUpdateField.Field(EsConstant.REINDEX_TIME_FILED, System.currentTimeMillis());
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
