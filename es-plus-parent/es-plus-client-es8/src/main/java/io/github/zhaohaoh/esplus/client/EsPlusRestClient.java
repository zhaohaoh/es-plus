 package io.github.zhaohaoh.esplus.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.DeleteOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.bulk.UpdateAction;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;
import com.es.plus.common.config.EsObjectHandler;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsPlusClient;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.interceptor.EsUpdateField;
import com.es.plus.common.params.*;
import com.es.plus.common.pojo.EsPlusGetTaskResponse;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;
import com.es.plus.common.pojo.es.EpBulkResponse;
import com.es.plus.common.pojo.es.EpNestedSortBuilder;
import com.es.plus.common.pojo.es.EpQueryBuilder;
import com.es.plus.common.properties.EsIndexParam;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.BeanUtils;
import com.es.plus.common.util.FieldUtils;
import com.es.plus.common.util.JsonUtils;
import com.es.plus.common.util.ResolveUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
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

import static com.es.plus.constant.EsConstant.*;

public class EsPlusRestClient implements EsPlusClient {
    
    private static final Logger log = LoggerFactory.getLogger(EsPlusRestClient.class);
    
    private final ElasticsearchClient elasticsearchClient;
    
    public EsPlusRestClient(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }
    
    @Override
    public ElasticsearchClient getEsClient() {
        return elasticsearchClient;
    }
    
    
    /**
     * 异步定时批量保存接口
     */
    @Override
    public void saveOrUpdateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return;
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getUpsertOperation(type, index, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                elasticsearchClient.bulk(builder.build());
            } catch (IOException e) {
                throw new EsException("saveOrUpdateBatchAsyncProcessor failed", e);
            }
        }
    }
    
    /**
     * 异步定时批量保存接口
     */
    @Override
    public void saveBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return;
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getIndexOperation(index, type, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                elasticsearchClient.bulk(builder.build());
            } catch (IOException e) {
                throw new EsException("saveBatchAsyncProcessor failed", e);
            }
        }
    }
    
    /**
     * 异步定时批量保存接口
     */
    @Override
    public void updateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs) {
        if (CollectionUtils.isEmpty(esDataList)) {
            return;
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getUpdateOperation(type, index, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                elasticsearchClient.bulk(builder.build());
            } catch (IOException e) {
                throw new EsException("updateBatchAsyncProcessor failed", e);
            }
        }
    }
    
    /**
     * saveOrUpdate
     */
    private BulkOperation getUpsertOperation(String type, String index, Object esData, boolean childIndex) {
        // 这里提前序列化了
        handlerUpdateParamter(index, esData);
        String docId = GlobalParamHolder.getDocId(index, esData);
        // 获取id的动作要前置，因为里面会修改对象
        String jsonStr = JsonUtils.toJsonStr(esData);
        
        UpdateOperation.Builder<Object, Object> updateBuilder = new UpdateOperation.Builder<>();
        updateBuilder.index(index);
        updateBuilder.id(docId);
        UpdateAction.Builder<Object, Object> upsert = new UpdateAction.Builder<>().doc(esData);
        // 这里会改变对象的数据 如果文档不存在则处理新增桉树并且插入
        handlerSaveParamter(index, esData);
        upsert.upsert(esData);
        
        updateBuilder.action(
                upsert
                        .build());
        updateBuilder.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        
       
        
        if (childIndex) {
            String routing = FieldUtils.getStrFieldValue(esData, "joinField", "parent");
            updateBuilder.routing(routing);
        }
        
        handlerUpdateParamter(index, esData);
   
        return new BulkOperation.Builder().update(updateBuilder.build()).build();
    }
    
    /**
     * 批处理更新 返回失败数据
     *
     * @return {@link List}<{@link String}>
     */
    @Override
    public List<String> saveOrUpdateBatch(String type, Collection<?> esDataList, String... indexs) {
        List<String> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return new ArrayList<>();
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getUpsertOperation(type, index, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                
                long start = System.currentTimeMillis();
                BulkResponse res = elasticsearchClient.bulk(builder.build());
                long end = System.currentTimeMillis();
                long timeCost = end - start;
                
                printInfoLog(" {} saveOrUpdateBatch timeCost:{} data:{} \n hasFailures={}", index, timeCost,
                        JsonUtils.toJsonStr(esDataList), res.errors());
                
                for (BulkResponseItem bulkItemResponse : res.items()) {
                    if (bulkItemResponse.error() != null) {
                        responses.add(bulkItemResponse.id());
                        printErrorLog(" {} saveOrUpdateBatch error" + bulkItemResponse.id() + " message:"
                                + bulkItemResponse.error().reason(), index);
                    }
                }
            } catch (Exception e) {
                throw new EsException("saveOrUpdateBatch Exception", e);
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
    public List<String> saveBatch(String type, Collection<?> esDataList, String... indexs) {
        List<String> response = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return response;
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getIndexOperation(index, type, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                
                long start = System.currentTimeMillis();
                BulkResponse res = elasticsearchClient.bulk(builder.build());
                long end = System.currentTimeMillis();
                long timeCost = end - start;
                
                printInfoLog("saveBatch {} timeCost:{}", index, timeCost);
                
                for (BulkResponseItem bulkItemResponse : res.items()) {
                    if (bulkItemResponse.error() != null) {
                        printErrorLog(" {} save error " + bulkItemResponse.id() + " message:"
                                + bulkItemResponse.error().reason(), index);
                    } else {
                        response.add(bulkItemResponse.id());
                    }
                }
            } catch (IOException e) {
                throw new EsException("SaveBatch IOException", e);
            }
        }
        
        return response;
    }
    
    private BulkOperation getIndexOperation(String index, String type, Object esData, boolean childIndex) {
        IndexOperation.Builder<Object> indexBuilder = new IndexOperation.Builder<>();
        handlerSaveParamter(index, esData);
        String docId = GlobalParamHolder.getDocId(index, esData);
        // 获取id的动作要前置，因为里面会修改对象
        indexBuilder.index(index);
        indexBuilder.id(docId);
        indexBuilder.document(esData);
        
        if (childIndex) {
            String routing = FieldUtils.getStrFieldValue(esData, "joinField", "parent");
            indexBuilder.routing(routing);
        }
        
        return new BulkOperation.Builder().index(indexBuilder.build()).build();
    }
    
    /**
     * 保存
     */
    @Override
    public boolean save(String type, Object esData, String... indexs) {
        List<String> bulkItemResponses = saveBatch(type, Collections.singletonList(esData), indexs);
        return !CollectionUtils.isEmpty(bulkItemResponses);
    }
    
    @Override
    public <T> boolean saveOrUpdate(String type, T esData, String... indexs) {
        boolean childIndex = isChildIndex(esData);
        
        try {
            for (String index : indexs) {
                List<BulkOperation> operations = new ArrayList<>();
                BulkOperation operation = getUpsertOperation(type, index, esData, childIndex);
                operations.add(operation);
                
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                
                long start = System.currentTimeMillis();
                BulkResponse res = elasticsearchClient.bulk(builder.build());
                long end = System.currentTimeMillis();
                long timeCost = end - start;
                
                printInfoLog(" {} saveOrUpdate timeCost:{} data:{} \n hasFailures={}", index, timeCost,
                        JsonUtils.toJsonStr(esData), res.errors());
                
                for (BulkResponseItem bulkItemResponse : res.items()) {
                    if (bulkItemResponse.error() != null) {
                        printErrorLog(" {} saveOrUpdate error" + bulkItemResponse.id() + " message:"
                                + bulkItemResponse.error().reason(), index);
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            throw new EsException("saveOrUpdate IOException", e);
        }
        
        return true;
    }
    
    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    @Override
    public boolean update(String type, Object esData, String... indexs) {
        boolean childIndex = isChildIndex(esData);
        
        for (String index : indexs) {
            try {
                UpdateRequest.Builder<Object, Object> updateBuilder = new UpdateRequest.Builder<>();
                updateBuilder.index(index);
                updateBuilder.id(GlobalParamHolder.getDocId(index, esData));
                updateBuilder.doc(esData);
                // 乐观锁重试次数
                updateBuilder.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
                updateBuilder.refresh(getRefreshPolicy());
                
                if (childIndex) {
                    String routing = FieldUtils.getStrFieldValue(esData, "joinField", "parent");
                    updateBuilder.routing(routing);
                }
                
                long start = System.currentTimeMillis();
                UpdateResponse<Object> updateResponse = elasticsearchClient.update(updateBuilder.build(), Object.class);
                long end = System.currentTimeMillis();
                long timeCost = end - start;
                
                if (updateResponse.result() == Result.Deleted) {
                    printErrorLog(" {} update data={}  error reason: doc  deleted", index, JsonUtils.toJsonStr(esData));
                    return false;
                } else if (updateResponse.result() == Result.NoOp) {
                    // noop标识没有数据改变。前后的值相同
                    return false;
                } else {
                    printInfoLog(" {} update success timeCost:{} data={}", index, timeCost, JsonUtils.toJsonStr(esData));
                }
            } catch (IOException e) {
                throw new EsException("elasticsearch update io error", e);
            } catch (Exception e) {
                throw new EsException("update error", e);
            }
        }
        
        return true;
    }
    
    /**
     * 批处理更新 返回失败数据
     *
     * @return {@link List}<{@link String}>
     */
    @Override
    public List<String> updateBatch(String type, Collection<?> esDataList, String... indexs) {
        List<String> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return responses;
        }
        
        for (String index : indexs) {
            boolean childIndex = isChildIndex(esDataList.stream().findFirst().get());
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Object esData : esDataList) {
                BulkOperation operation = getUpdateOperation(type, index, esData, childIndex);
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder builder = new BulkRequest.Builder();
                builder.operations(operations);
                builder.refresh(getRefreshPolicy());
                
                long start = System.currentTimeMillis();
                BulkResponse res = elasticsearchClient.bulk(builder.build());
                long end = System.currentTimeMillis();
                long timeCost = end - start;
                
                printInfoLog("updateBatch index={} timeCost:{} data:{} hasFailures={}", index, timeCost,
                        JsonUtils.toJsonStr(esDataList), res.errors());
                
                for (BulkResponseItem bulkItemResponse : res.items()) {
                    if (bulkItemResponse.error() != null) {
                        responses.add(bulkItemResponse.id());
                        printErrorLog("updateBatch {} error" + bulkItemResponse.id() + " message:"
                                + bulkItemResponse.error().reason(), index);
                    }
                }
            } catch (IOException e) {
                throw new EsException("updateBatch IOException", e);
            }
        }
        
        return responses;
    }
    
    private BulkOperation getUpdateOperation(String type, String index, Object esData, boolean childIndex) {
        handlerUpdateParamter(index, esData);
        String docId = GlobalParamHolder.getDocId(index, esData);
        
        UpdateOperation.Builder<Object, Object> updateBuilder = new UpdateOperation.Builder<>();
        updateBuilder.index(index);
        updateBuilder.id(docId);
//        updateBuilder.doc(esData);
        updateBuilder.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateBuilder.action(
                new UpdateAction.Builder<>()
                .doc(esData)
//                        .docAsUpsert()
                        .build());
        if (childIndex) {
            String routing = FieldUtils.getStrFieldValue(esData, "joinField", "parent");
            updateBuilder.routing(routing);
        }
        
        return new BulkOperation.Builder().update(updateBuilder.build()).build();
    }

    
    private Refresh getRefreshPolicy() {
        switch (GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy()) {
            case IMMEDIATE:
                return Refresh.True;
            case WAIT_UNTIL:
                return Refresh.WaitFor;
            default:
                return Refresh.False;
        }
    }
    
    /**
     * 更新包装
     */
    @Override
    public <T> EpBulkResponse updateByWrapper(String type, EsParamWrapper<T> esParamWrapper, String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        // 查询条件组合
        EpBoolQueryBuilder boolQueryBuilder = esQueryParamWrapper.getBoolQueryBuilder();
        Query queryBuilder = EpQueryConverter.toEsQuery(boolQueryBuilder);
        
        EsUpdateField esUpdateField = esParamWrapper.getEsUpdateField();
        List<EsUpdateField.Field> fields = esUpdateField.getFields();
        String scriptStr = esUpdateField.getScipt();
        Map<String, Object> params = esUpdateField.getSciptParams();
        
        if (StringUtils.isBlank(scriptStr)) {
            params = new HashMap<>();
            // 构建script语句
            StringBuilder sb = new StringBuilder();
            for (EsUpdateField.Field field : fields) {
                String name = field.getName();
                // 除了基本类型和字符串。日期的对象需要进行转化
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
                // list直接覆盖 丢进去 无需再特殊处理
                params.put(name, value);
                sb.append("ctx._source.");
                sb.append(name).append(" = params.").append(name).append(";");
            }
            scriptStr = sb.toString();
        }
        
        try {
            UpdateByQueryRequest.Builder builder = new UpdateByQueryRequest.Builder();
            builder.index(Arrays.asList(index));
            // 版本号不匹配更新失败不停止
            builder.conflicts(Conflicts.Proceed);
            builder.query(queryBuilder);
            builder.scrollSize((long) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            builder.scroll(Time.of(t -> t.time("1m")));
            // 请求完成后立即刷新索引，保证读一致性
            builder.refresh(true);
            String[] routings = esQueryParamWrapper.getRoutings();
            if (routings != null) {
                builder.routing(routings[0]);
            }
            if (esQueryParamWrapper.getPreference()!=null) {
                builder.preference(esQueryParamWrapper.getPreference());
            }
            // 一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
            // 查询到数据后
            builder.requestsPerSecond((float) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            
//            builder.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            // 修复Script构建语法
            Map<String, JsonData> finalParams = new HashMap<>();
            params.forEach((k,v)->{
                  finalParams.put(k, JsonData.of(v));
            });
            String finalScriptStr = scriptStr;
            Script script = Script.of(s -> s.source(finalScriptStr).params(finalParams));
            builder.script(script);
            
            long start = System.currentTimeMillis();
            printInfoLog("updateByWrapper index:{} requst: script:{},params={}  query:{}", index, scriptStr, params,
                    esQueryParamWrapper.getBoolQueryBuilder().toString());
            
            UpdateByQueryRequest build = builder.build();
            UpdateByQueryResponse bulkResponse = elasticsearchClient.updateByQuery(build);
            long end = System.currentTimeMillis();
            long timeCost = end - start;
            
            printInfoLog("updateByWrapper index:{} timeCost:{} response:{} update count={}", index, timeCost, bulkResponse,
                    bulkResponse.updated());
            
            EpBulkResponse epBulkResponse = new EpBulkResponse();
            epBulkResponse.setBatches(Math.toIntExact(bulkResponse.batches()));
            epBulkResponse.setTotal(bulkResponse.total());
            epBulkResponse.setTook(bulkResponse.took());
            epBulkResponse.setUpdated(bulkResponse.updated());
            epBulkResponse.setNoops(bulkResponse.noops());
            epBulkResponse.setBulkRetries(bulkResponse.retries().bulk());
            epBulkResponse.setSearchRetries(bulkResponse.retries().search());
            epBulkResponse.setVersionConflicts(bulkResponse.versionConflicts());
            epBulkResponse.setUpdated(bulkResponse.updated());
            epBulkResponse.setDeleted(bulkResponse.deleted());
            
            if (bulkResponse.failures() != null && !bulkResponse.failures().isEmpty()) {
                List<String> collect = bulkResponse.failures().stream()
                        .map(failure -> failure.id() != null ? failure.id() : "")
                        .filter(id -> !id.isEmpty())
                        .collect(Collectors.toList());
                epBulkResponse.setFailIds(collect);
            }
            
            return epBulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper IOException", e);
        }
    }
    
    @Override
    public <T> EpBulkResponse increment(String type, EsParamWrapper<T> esParamWrapper, String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        
        // 查询条件组合
        EpBoolQueryBuilder boolQueryBuilder = esQueryParamWrapper.getBoolQueryBuilder();
        Query queryBuilder = EpQueryConverter.toEsQuery(boolQueryBuilder);
        List<EsUpdateField.Field> fields = esParamWrapper.getEsUpdateField().getIncrementFields();
        Map<String, Object> params = new HashMap<>();
        // 构建script语句
        StringBuilder script = new StringBuilder();
        for (EsUpdateField.Field field : fields) {
            String name = field.getName();
            Long value = (Long) field.getValue();
            params.put(name, value);
            script.append("ctx._source.");
            script.append(name).append(" += params.").append(name).append(";");
        }
        
        try {
            UpdateByQueryRequest.Builder builder = new UpdateByQueryRequest.Builder();
            builder.index(Arrays.asList(index));
            // 版本号不匹配更新失败不停止
            builder.conflicts(Conflicts.Proceed);
            builder.query(queryBuilder);
            String[] routings = esQueryParamWrapper.getRoutings();
            if (routings != null) {
                builder.routing(routings[0]);
            }
            builder.scrollSize((long) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            // 一般需要加上requests_per_second来控制.若不加可能执行时间比较长，造成es瞬间io巨大，属于危险操作.此参数用于限流。真实查询数据是batchsize控制
            builder.requestsPerSecond((float) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            builder.scroll(Time.of(t -> t.time("1m")));
            
            // 修复Script构建语法
            Map<String, JsonData> finalParams = new HashMap<>();
            params.forEach((k,v)->{
                finalParams.put(k, JsonData.of(v));
            });
            String finalScriptStr = script.toString();
            Script painless = Script.of(s -> s.source(finalScriptStr).params(finalParams));
            builder.script(painless);
            
            long start = System.currentTimeMillis();
            printInfoLog(" {} updateByWrapper increment requst: script:{},params={}", index, script, params);
            
            UpdateByQueryResponse bulkResponse = elasticsearchClient.updateByQuery(builder.build());
            long end = System.currentTimeMillis();
            long timeCost = end - start;
            
            printInfoLog(" {} updateByWrapper timeCost:{}  increment response:{} update count=", index, timeCost,
                    bulkResponse);
            
            EpBulkResponse epBulkResponse = new EpBulkResponse();
            epBulkResponse.setBatches(Math.toIntExact(bulkResponse.batches()));
            epBulkResponse.setTotal(bulkResponse.total());
            epBulkResponse.setTook(bulkResponse.took());
            epBulkResponse.setUpdated(bulkResponse.updated());
            epBulkResponse.setNoops(bulkResponse.noops());
            
            return epBulkResponse;
        } catch (IOException e) {
            throw new EsException("increment IOException", e);
        }
    }
    
    
    @Override
    public <T> EsResponse<T> search(String type, EsParamWrapper<T> esParamWrapper, String... index) {
       
        
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        
        //获取查询语句源数据
        SearchRequest sourceBuilder = getSearchSourceBuilder(esParamWrapper);
        
        populateSearchRequest(type, sourceBuilder, esQueryParamWrapper, sourceBuilder, index);
        
        if (esQueryParamWrapper.getSearchType() != null) {
            searchRequest.searchType(SearchType.fromString(esQueryParamWrapper.getSearchType().name()));
        }
        
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            searchResponse = elasticsearchClient.search(searchRequest, esParamWrapper.getTClass());
            long end = System.currentTimeMillis();
            long mills = end - start;
            printSearchInfoLog("search index={} timeCost={} \nDSL:{} ", index,  mills,sourceBuilder);
        } catch (Exception e) {
            throw new EsException("es-plus search body=" + sourceBuilder, e);
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("es-plus search error:" + searchResponse.status().getStatus());
        }
        EsResponse<T> esResponse = getEsResponse(esParamWrapper.getTClass(), searchResponse);
        return esResponse;
    }
    private <T> SearchRequest getSearchSourceBuilder(EsParamWrapper<T> esParamWrapper) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        Integer page = esQueryParamWrapper.getPage();
        Integer size = esQueryParamWrapper.getSize();
        //查询条件组合
        EpBoolQueryBuilder boolQueryBuilder = esQueryParamWrapper.getBoolQueryBuilder();
        BoolQueryBuilder queryBuilder = EpQueryConverter.toEsBoolQueryBuilder(boolQueryBuilder);
        
        SearchRequest.Builder sourceBuilder = new SearchRequest.Builder().query();
       
        //超过1万条加了才能返回
        if (GlobalConfigCache.GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(new TrackHits.Builder()
                    .count(GlobalConfigCache.GLOBAL_CONFIG.getTrackTotalCount()).build());
        }
        EsSelect esSelect = esQueryParamWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes())
                    || ArrayUtils.isNotEmpty(esSelect.getExcludes())
            ||esSelect.getFetch() != null) {
                sourceBuilder.source(
                        a-> {
                            SourceConfig.Builder fetch = new SourceConfig.Builder();
                            if (esSelect.getFetch() != null) {
                              fetch = (SourceConfig.Builder) a.fetch(true);
                            }
                            fetch.filter(b-> {
                                SourceFilter.Builder builder=b;
                                if(ArrayUtils.isNotEmpty(esSelect.getIncludes())){
                                    builder = builder.includes(Arrays.asList(esSelect.getIncludes()));
                                }
                                if(ArrayUtils.isNotEmpty(esSelect.getExcludes())){
                                    builder = builder.excludes(Arrays.asList(esSelect.getExcludes()));
                                }
                                
                                return builder;
                            });
                            return fetch;
                        }
                );
            }
            
            if (esSelect.getMinScope() != null) {
                sourceBuilder.minScore(Double.valueOf(esSelect.getMinScope()));
            }
            if (esSelect.getTrackScores() != null) {
                sourceBuilder.trackScores(esSelect.getTrackScores());
            }
            if (esSelect.getTrackTotalHits() != null) {
                sourceBuilder.trackTotalHits(
                        TrackHits.of(t -> t.count(GlobalConfigCache.GLOBAL_CONFIG.getTrackTotalCount())));
            }
        }
        boolean profile = esQueryParamWrapper.isProfile();
        if (profile) {
            sourceBuilder.profile(profile);
        }
        
        //searchAfter
        if (esQueryParamWrapper.getSearchAfterValues() != null) {
            List<FieldValue> fieldValues=new ArrayList<>();
            for (Object searchAfterValue : esQueryParamWrapper.getSearchAfterValues()) {
                FieldValue fieldValue = FieldValue.of(searchAfterValue);
                fieldValues.add(fieldValue);
            }
            sourceBuilder.searchAfter(fieldValues);
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
                EpNestedSortBuilder epNestedSortBuilder = order.getNestedSortBuilder();
                
                FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(order.getName()).order(
                        SortOrder.valueOf(order.getSort().toUpperCase(Locale.ROOT)));
                if (epNestedSortBuilder != null) {
                    NestedSortBuilder nestedSortBuilder = EpNestedSortConverter.convertToNestedSort(
                            epNestedSortBuilder);
                    fieldSortBuilder.setNestedSort(nestedSortBuilder);
                }
                sourceBuilder.sort(fieldSortBuilder);
                
            });
        }
        populateGroupField(esParamWrapper, sourceBuilder);
        return sourceBuilder;
    }
    
    @Override
    public <T> EsAggResponse<T> searchAgg(String type, EsQueryParamWrapper<T> esQueryParamWrapper, Class<T> clazz,
            String... indexs) {
        // TODO: 实现聚合搜索
        return null;
    }
    
    @Override
    public boolean delete(String type, String id, String... indexs) {
        for (String index : indexs) {
            try {
                DeleteRequest.Builder deleteBuilder = new DeleteRequest.Builder();
                deleteBuilder.index(index);
                deleteBuilder.id(id);
                deleteBuilder.refresh(getRefreshPolicy());
                
                DeleteResponse deleteResponse = elasticsearchClient.delete(deleteBuilder.build());
                
                if (deleteResponse.result() == Result.Deleted) {
                    printInfoLog("delete index={} id={} success", index, id);
                    return true;
                } else if (deleteResponse.result() == Result.NotFound) {
                    printInfoLog("delete index={} id={} not found", index, id);
                    return false;
                }
            } catch (IOException e) {
                throw new EsException("delete IOException", e);
            }
        }
        return false;
    }
    
    @Override
    public <T> EpBulkResponse deleteByQuery(String type, EsParamWrapper<T> esParamWrapper, String... indexs) {
        try {
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            DeleteByQueryRequest.Builder deleteBuilder = new DeleteByQueryRequest.Builder();
            deleteBuilder.index(Arrays.asList(indexs));
            
            if (esQueryParamWrapper.getBoolQueryBuilder() != null) {
                Query query = EpQueryConverter.toEsQuery(esQueryParamWrapper.getBoolQueryBuilder());
                deleteBuilder.query(query);
            }
            
            deleteBuilder.refresh(true);
            deleteBuilder.scroll(Time.of(t -> t.time("1m")));
            
            DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(deleteBuilder.build());
            
            EpBulkResponse epBulkResponse = new EpBulkResponse();
            epBulkResponse.setTotal(response.total());
            epBulkResponse.setDeleted(response.deleted());
            epBulkResponse.setTook(response.took());
            epBulkResponse.setBatches(Math.toIntExact(response.batches()));
            epBulkResponse.setVersionConflicts(response.versionConflicts());
            epBulkResponse.setNoops(response.noops());
            epBulkResponse.setBulkRetries(response.retries().bulk());
            epBulkResponse.setSearchRetries(response.retries().search());
            return epBulkResponse;
        } catch (IOException e) {
            throw new EsException("deleteByQuery IOException", e);
        }
    }
    
    @Override
    public List<String> deleteBatch(String type, Collection<String> ids, String... indexs) {
        List<String> failedIds = new ArrayList<>();
        
        for (String index : indexs) {
            List<BulkOperation> operations = new ArrayList<>();
            
            for (String id : ids) {
                DeleteOperation.Builder deleteOpBuilder = new DeleteOperation.Builder();
                deleteOpBuilder.index(index);
                deleteOpBuilder.id(id);
                
                BulkOperation operation = new BulkOperation.Builder()
                        .delete(deleteOpBuilder.build())
                        .build();
                operations.add(operation);
            }
            
            try {
                BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
                bulkBuilder.operations(operations);
                bulkBuilder.refresh(getRefreshPolicy());
                
                BulkResponse response = elasticsearchClient.bulk(bulkBuilder.build());
                
                for (BulkResponseItem item : response.items()) {
                    if (item.error() != null) {
                        failedIds.add(item.id());
                    }
                }
            } catch (IOException e) {
                throw new EsException("deleteBatch IOException", e);
            }
        }
        
        return failedIds;
    }
    
    @Override
    public <T> T get(String type, String id, Class<T> clazz, String... indexs) {
        for (String index : indexs) {
            try {
                GetRequest.Builder getBuilder = new GetRequest.Builder();
                getBuilder.index(index);
                getBuilder.id(id);
                
                GetResponse<T> response = elasticsearchClient.get(getBuilder.build(), clazz);
                
                if (response.found()) {
                    return response.source();
                }
            } catch (IOException e) {
                throw new EsException("get IOException", e);
            }
        }
        return null;
    }
    
    @Override
    public <T> List<T> getList(String type, Collection<String> ids, Class<T> clazz, String... indexs) {
        List<T> result = new ArrayList<>();
        
        for (String index : indexs) {
            try {
                MgetRequest.Builder mgetBuilder = new MgetRequest.Builder();
                List<MgetOperation> operations = new ArrayList<>();
                
                for (String id : ids) {
                    MgetOperation operation = MgetOperation.of(op -> op.index(index).id(id));
                    operations.add(operation);
                }
                
                mgetBuilder.docs(operations);
                
                MgetResponse<T> response = elasticsearchClient.mget(mgetBuilder.build(), clazz);
                
                for (MgetResponseItem<T> item : response.docs()) {
                    if (item.isFound() && item.result() != null) {
                        result.add(item.result().source());
                    }
                }
            } catch (IOException e) {
                throw new EsException("getList IOException", e);
            }
        }
        
        return result;
    }
    
    @Override
    public Long count(String type, EsQueryParamWrapper esQueryParamWrapper, String... indexs) {
        try {
            CountRequest.Builder countBuilder = new CountRequest.Builder();
            countBuilder.index(Arrays.asList(indexs));
            
            if (esQueryParamWrapper.getBoolQueryBuilder() != null) {
                Query query = EpQueryConverter.toEsQuery(esQueryParamWrapper.getBoolQueryBuilder());
                countBuilder.query(query);
            }
            
            CountResponse response = elasticsearchClient.count(countBuilder.build());
            return response.count();
        } catch (IOException e) {
            throw new EsException("count IOException", e);
        }
    }
    
    
    private void handlerSaveParamter(String index, Object esData) {
        EsObjectHandler esObjectHandler = GlobalConfigCache.GLOBAL_CONFIG.getEsObjectHandler();
        if (esObjectHandler != null) {
            esObjectHandler.insertFill(esData);
        }
    }
    
    private void handlerUpdateParamter(String index, Object esData) {
        EsObjectHandler esObjectHandler = GlobalConfigCache.GLOBAL_CONFIG.getEsObjectHandler();
        if (esObjectHandler != null) {
            esObjectHandler.updateFill(esData);
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
     */
    private void printErrorLog(String format, Exception e) {
        log.error("es-plus " + format, e);
    }
    
    /**
     * 打印错误日志
     *
     * @param format 格式
     * @param params 参数
     */
    private void printErrorLog(String format, Object... params) {
        log.error("es-plus " + format, params);
    }
}
