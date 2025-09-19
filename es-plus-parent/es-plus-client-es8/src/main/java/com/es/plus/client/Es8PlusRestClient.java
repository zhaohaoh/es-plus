package com.es.plus.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SearchType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
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
import co.elastic.clients.elasticsearch.core.search.InnerHitsResult;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.elasticsearch.indices.GetMappingRequest;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.JsonData;
import com.es.plus.common.config.EsObjectHandler;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsPlusClient;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.interceptor.EsUpdateField;
import com.es.plus.common.params.*;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;
import com.es.plus.common.pojo.es.EpBulkResponse;
import com.es.plus.common.properties.EsIndexParam;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.BeanUtils;
import com.es.plus.common.util.FieldUtils;
import com.es.plus.common.util.JsonUtils;
import com.es.plus.common.util.ResolveUtils;
import com.es.plus.util.BulkProcessorConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;
 
 public class Es8PlusRestClient implements EsPlusClient {
    
    private static final Logger log = LoggerFactory.getLogger(Es8PlusRestClient.class);
    
    private final ElasticsearchClient elasticsearchClient;
    
    public Es8PlusRestClient(Object elasticsearchClient) {
        this.elasticsearchClient = (ElasticsearchClient) elasticsearchClient;
    }
    
    @Override
    public ElasticsearchClient getEsClient() {
        return elasticsearchClient;
    }
     
     @Override
     public void addBulkProcessor(BulkProcessorParam bulkProcessorParam, String index) {
         BulkProcessorConfig.getBulkProcessor(elasticsearchClient, bulkProcessorParam, index);
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
        return CollectionUtils.isEmpty(bulkItemResponses);
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
        Query queryBuilder = Ep8QueryConverter.toEsQuery(boolQueryBuilder);
        
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
        Query queryBuilder = Ep8QueryConverter.toEsQuery(boolQueryBuilder);
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
        
        //获取查询语句源数据
        SearchRequest searchRequest = getSearchSourceBuilder(esParamWrapper, index).build();
       
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            searchResponse = elasticsearchClient.search(searchRequest, esParamWrapper.getTClass());
            long end = System.currentTimeMillis();
            long mills = end - start;
            printSearchInfoLog("search index={} timeCost={} \nDSL:{} ", index,  mills,searchRequest);
        } catch (Exception e) {
            throw new EsException("es-plus search body=" + searchRequest, e);
        }
//        if (searchResponse. != 200) {
//            throw new EsException("es-plus search error:" + searchResponse.status().getStatus());
//        }
        EsResponse<T> esResponse = getEsResponse(esParamWrapper.getTClass(), searchResponse);
        return esResponse;
    }
     
     @Override
     public <T> EsResponse<T> scroll(String type, EsParamWrapper<T> esParamWrapper, Duration keepTime, String scrollId,
             String... index) {
         try {
             SearchRequest searchRequest;
             ResponseBody responseBody ;
             if (scrollId == null) {
                 // 首次滚动查询
                 SearchRequest.Builder searchSourceBuilder = getSearchSourceBuilder(esParamWrapper, index);
                 searchSourceBuilder.scroll(Time.of(t -> t.time(keepTime.toString())));
                 searchRequest = searchSourceBuilder.build();
                 SearchResponse<T> searchResponse = elasticsearchClient.search(searchRequest, esParamWrapper.getTClass());
                 responseBody = searchResponse;
             } else {
                 // 继续滚动查询
                 ScrollRequest scrollRequest = ScrollRequest.of(s -> s.scrollId(scrollId)
                         .scroll(Time.of(t -> t.time(keepTime.toString()))));
                 ScrollResponse<T> scrollResponse = elasticsearchClient.scroll(scrollRequest, esParamWrapper.getTClass());
                 
                 responseBody = scrollResponse;
             }
             HitsMetadata hits = responseBody.hits();
             if (hits == null || hits.total().value() <= 0) {
                 ClearScrollRequest clearScrollRequest = new ClearScrollRequest.Builder().scrollId(scrollId).build();
                 ClearScrollResponse clearScrollResponse = elasticsearchClient.clearScroll(clearScrollRequest);
                 boolean succeeded = clearScrollResponse.succeeded();
             }
             return getEsResponse(esParamWrapper.getTClass(), responseBody);
         } catch (Exception e) {
             throw new EsException("es-plus scroll search error", e);
         }
     }
     
     private <T> EsResponse<T> getEsResponse(Class<T> tClass, ResponseBody searchResponse) {
        //获取结果集
        HitsMetadata<T> hits = searchResponse.hits();
        List<Hit<T>> hitList = hits.hits();
        List<T> result = hitList.stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> {
                    T bean = hit.source();
                    if (tClass.equals(Map.class)) {
                        Map map = (Map) bean;
                        map.put("_id", hit.id());
                    }
                    // 设置高亮
                    setHighLishtField(hit, bean);
                    // 设置分数
                    setScore(hit, bean);
                    return bean;
                })
                .collect(Collectors.toList());
        
        EsHits esHits = setInnerHits(hits, false);
        //设置聚合结果
        Map<String, Aggregate> aggregations = searchResponse.aggregations();
        Es8PlusAggregations<T> esAggsResponse = new Es8PlusAggregations<>();
        esAggsResponse.setAggregations(aggregations);
        esAggsResponse.settClass(tClass);
        
        //设置返回结果
        EsResponse<T> esResponse = new EsResponse<>(result, hits.total().value(), esAggsResponse);
        if (searchResponse.shards().skipped() != null) {
            esResponse.setSkippedShards(searchResponse.shards().skipped().intValue());
        }
        esResponse.setInnerHits(esHits);
        esResponse.setSuccessfulShards(searchResponse.shards().successful().intValue());
        esResponse.setTotalShards(searchResponse.shards().total().intValue());
        esResponse.setScrollId(searchResponse.scrollId());
        esResponse.setSourceResponse(searchResponse.toString());
        // 设置最小和最大的排序字段值
        if (!hitList.isEmpty()) {
            List<FieldValue> first = hitList.get(0).sort();
            esResponse.setFirstSortValues(first.stream().map(FieldValue::_get).toArray());
            List<FieldValue> tail = hitList.get(hitList.size() - 1).sort();
            esResponse.setTailSortValues(tail.stream().map(FieldValue::_get).toArray());
        }
        
        return esResponse;
    }
    // ... existing code ...
    
    private <T> EsHits setInnerHits(HitsMetadata<T> hits, boolean populate) {
        List<? extends Hit<T>> hitList = hits.hits();
        if (hitList == null || hitList.isEmpty()) {
            return null;
        }
        
        // 检查是否有inner hits
        boolean hasInnerHits = false;
        for (Hit<T> hit : hitList) {
            if (hit.innerHits() != null && !hit.innerHits().isEmpty()) {
                hasInnerHits = true;
                break;
            }
        }
        
        if (!hasInnerHits) {
            return null;
        }
        
        EsHits esHits = new EsHits();
        esHits.setTotal(hits.total().value());
        
        List<EsHit> esHitList = new ArrayList<>();
        esHits.setEsHitList(esHitList);
        
        for (Hit<T> hit : hitList) {
            EsHit esHit = new EsHit();
            // 一级数据不填充
            if (populate && hit.source() != null) {
                esHit.setData(hit.source().toString());
            }
            esHitList.add(esHit);
            
            Map<String, InnerHitsResult> innerHits = hit.innerHits();
            
            // 填充innerHits
            if (innerHits != null && !innerHits.isEmpty()) {
                Map<String, EsHits> esHitsMap = new HashMap<>();
                for (Map.Entry<String, InnerHitsResult> entry : innerHits.entrySet()) {
                    EsHits innerEsHits = setInnerHits(entry.getValue().hits(), true);
                    esHitsMap.put(entry.getKey(), innerEsHits);
                }
                esHit.setInnerHitsMap(esHitsMap);
            }
        }
        return esHits;
    }
    
    // ... existing code ...
    
    /**
     * 设置高亮
     *
     * @param hit  打击
     * @param bean 豆
     */
    public   <T> void setHighLishtField(Hit<T> hit, T bean) {
        Map<String, List<String>> highlightFields = hit.highlight();
        if (highlightFields != null && !highlightFields.isEmpty()) {
            highlightFields.forEach((k, v) -> {
                StringBuilder highlightStr = new StringBuilder();
                for (String text : v) {
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
            });
        }
    }
    
    /**
     * 设置分数
     */
    public   <T> void setScore(Hit<T> hit, T bean) {
        Double score = hit.score();
        if (score != null && !score.isNaN()) {
            EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(bean.getClass());
            try {
                if (esIndexParam == null) {
                    return;
                }
                if (StringUtils.isNotBlank(esIndexParam.getScoreField())) {
                    Field field = bean.getClass().getDeclaredField(esIndexParam.getScoreField());
                    field.setAccessible(true);
                    field.set(bean, score.floatValue());
                }
            } catch (Exception e) {
                log.error("setScore ", e);
            }
        }
    }
    private <T> SearchRequest.Builder getSearchSourceBuilder(EsParamWrapper<T> esParamWrapper,String... index) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        Integer page = esQueryParamWrapper.getPage();
        Integer size = esQueryParamWrapper.getSize();
        //查询条件组合
        EpBoolQueryBuilder boolQueryBuilder = esQueryParamWrapper.getBoolQueryBuilder();
        Query queryBuilder = Ep8QueryConverter.toEsBoolQuery(boolQueryBuilder);
        
        SearchRequest.Builder sourceBuilder = new SearchRequest.Builder().query(queryBuilder);
        
        //设置索引偏好
        sourceBuilder.index(Arrays.asList(index));
        sourceBuilder.preference(esQueryParamWrapper.getPreference());
        if (esQueryParamWrapper.getSearchType() != null) {
            SearchType searchType = SearchType.valueOf(esQueryParamWrapper.getSearchType().name());
            sourceBuilder.searchType(searchType);
        }
        
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
            Highlight esHighlight = Ep8QueryConverter.toEsHighlight(esHighLight);
            sourceBuilder.highlight(esHighlight);
        }
        
        //排序
        if (!CollectionUtils.isEmpty(esQueryParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esQueryParamWrapper.getEsOrderList();
            List<SortOptions> esSorts = Ep8QueryConverter.toEsSorts(orderFields);
            sourceBuilder.sort(esSorts);
        }
        // 设置聚合
        List<EpAggBuilder> aggregationBuilders = esQueryParamWrapper.getAggregationBuilder();
        if (aggregationBuilders != null && !aggregationBuilders.isEmpty()) {
            Map<String, Aggregation> aggregations = new HashMap<>();
            for (EpAggBuilder epAggBuilder : aggregationBuilders) {
                Aggregation aggregation = Ep8AggregationConvert.toEsAggregation(epAggBuilder);
                aggregations.put(epAggBuilder.getName(), aggregation);
            }
            sourceBuilder.aggregations(aggregations);
        }

        return sourceBuilder;
    }
     
     @Override
     public <T> EsAggResponse<T> aggregations(String type, EsParamWrapper<T> esParamWrapper, String... indexs) {
         try {
             EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
             
             // 创建搜索请求构建器
             SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
             
             // 设置索引
             searchBuilder.index(Arrays.asList(indexs));
             
             // 设置查询条件
             if (esQueryParamWrapper.getBoolQueryBuilder() != null) {
                 Query query = Ep8QueryConverter.toEsQuery(esQueryParamWrapper.getBoolQueryBuilder());
                 searchBuilder.query(query);
             }
             
             // 设置聚合
             List<EpAggBuilder> aggregationBuilders = esQueryParamWrapper.getAggregationBuilder();
             if (aggregationBuilders != null && !aggregationBuilders.isEmpty()) {
                 Map<String, Aggregation> aggregations = new HashMap<>();
                 for (EpAggBuilder epAggBuilder : aggregationBuilders) {
                     Aggregation aggregation = Ep8AggregationConvert.toEsAggregation(epAggBuilder);
                     aggregations.put(epAggBuilder.getName(), aggregation);
                 }
                 searchBuilder.aggregations(aggregations);
             }
             
             // 设置size为0，因为我们只关心聚合结果，不关心具体文档
             searchBuilder.size(0);
             
             // 设置路由等其他参数
             String[] routings = esQueryParamWrapper.getRoutings();
             if (routings != null && routings.length > 0) {
                 searchBuilder.routing(routings[0]);
             }
             
             if (esQueryParamWrapper.getPreference() != null) {
                 searchBuilder.preference(esQueryParamWrapper.getPreference());
             }
             
             // 执行搜索
             long start = System.currentTimeMillis();
             SearchResponse<T> searchResponse = elasticsearchClient.search(searchBuilder.build(), esParamWrapper.getTClass());
             long end = System.currentTimeMillis();
             long timeCost = end - start;
             
             printSearchInfoLog("aggregations index={} timeCost={} \nDSL:{} ", indexs, timeCost, searchBuilder);
             
             // 处理聚合结果
             Map<String, Aggregate> responseAggregations = searchResponse.aggregations();
             Es8PlusAggregations<T> esAggregations = new Es8PlusAggregations<>();
             esAggregations.setAggregations(responseAggregations);
             esAggregations.settClass(esParamWrapper.getTClass());
             
             return esAggregations;
         } catch (Exception e) {
             throw new EsException("es-plus aggregations error", e);
         }
     }
     
     @Override
     public String executeDSL(String dsl, String... index) {
         String indexs = String.join(",", index);
         try {
             // ES8使用低级客户端来执行DSL查询
             co.elastic.clients.transport.TransportOptions transportOptions =
                 elasticsearchClient._transport().options();
             co.elastic.clients.elasticsearch.core.SearchRequest.Builder searchBuilder =
                 new co.elastic.clients.elasticsearch.core.SearchRequest.Builder();

             // 设置索引
             searchBuilder.index(Arrays.asList(index));

             // 使用withJson方法解析DSL字符串
             java.io.StringReader dslReader = new java.io.StringReader(dsl);
             searchBuilder.withJson(dslReader);

             // 执行搜索
             SearchResponse<?> response = elasticsearchClient.search(searchBuilder.build(), Object.class);
 
             return response.toString();
         } catch (Exception e) {
             log.error("executeDSL error", e);
             throw new EsException("executeDSL failed", e);
         }
     }
     
     @Override
     public String translateSql(String sql) {
         Map<String, Object> jsonRequest = new HashMap<>();
         jsonRequest.put("query", sql);
         try {
             // ES8使用SQL API进行翻译
             co.elastic.clients.elasticsearch.sql.TranslateRequest.Builder translateBuilder =
                 new co.elastic.clients.elasticsearch.sql.TranslateRequest.Builder();
             translateBuilder.query(sql);

             co.elastic.clients.elasticsearch.sql.TranslateResponse response =
                 elasticsearchClient.sql().translate(translateBuilder.build());

             // 将响应转换为JSON字符串
             return response.toString();
         } catch (IOException e) {
             log.error("translateSql error", e);
         }
         return null;
     }
     
     @Override
     public <T> EsResponse<T> executeSQL(String sql, Class<T> tClass) {
         String rs = executeSQL(sql);
         if (rs == null) {
             return null;
         }
         try {
             SearchResponse.Builder<T> builder = new SearchResponse.Builder<>();
             java.io.StringReader jsonReader = new java.io.StringReader(rs);
             builder.withJson(jsonReader);
             SearchResponse<T> searchResponse = builder.build();

             // 使用现有的getEsResponse方法来处理响应
             return getEsResponse(tClass, searchResponse);
         } catch (Exception e) {
             throw new EsException("executeSQL result parse error", e);
         }
     }
     
     @Override
     public String executeSQL(String sql) {
         String dsl = sql2Dsl(sql, false);
         if (dsl == null) {
             return null;
         }
         // 匹配 SQL 语句中的表名
         String tableName = getTableName(sql);
         return executeDSL(dsl, tableName);
     }
     
     @Override
     public String sql2Dsl(String sql, boolean explain) {
         String limit = StringUtils.substringAfterLast(sql, "limit");
         Integer from = null;
         Integer size = null;
         if (limit != null && limit.contains(",")) {
             String[] split = limit.split(",");
             from = Integer.parseInt(split[0].trim());
             size = Integer.parseInt(split[1].trim());
             sql = sql.replace(limit, "");
         }
         if (sql.contains("group")) {
             sql = StringUtils.substringBeforeLast(sql, "limit");
         }
         String dsl = translateSql(sql);
         if (dsl == null) {
             throw new EsException("sql无法转换成dsl");
         }

         Map<String, Object> map = JsonUtils.toMap(dsl);
         if (from != null) {
             map.put("from", from);
         }
         if (size != null) {
             map.put("size", size);
         }

         List docvalueList = (List) map.get("docvalue_fields");
         if (docvalueList != null && !docvalueList.isEmpty() && !map.get("_source").equals(Boolean.FALSE)) {
             Map source = (Map) map.get("_source");
             List includes = source.get("includes") != null ? (List) source.get("includes") : new ArrayList();
             List fields = (List) docvalueList.stream().map(a -> ((Map) a).get("field")).collect(Collectors.toList());
             includes.addAll(fields);
         }
         if (explain) {
             map.put("profile", true);
         }
         dsl = JsonUtils.toJsonStr(map);
         return dsl;
     }
     
     @Override
     public EsIndexResponse getMappings(String indexName) {
         try {
             GetMappingRequest request = GetMappingRequest.of(r -> r.index(indexName));
             GetMappingResponse response = elasticsearchClient.indices().getMapping(request);

             // 构建EsIndexResponse对象
             EsIndexResponse esIndexResponse = new EsIndexResponse();

             // ES8中GetMappingResponse.result()返回的是Map<String, IndexMappingRecord>
             // IndexMappingRecord包含mappings信息
             Map<String, IndexMappingRecord> mappings = response.result();

             // 设置索引名称
             esIndexResponse.setIndices(new String[]{indexName});

             // 转换mappings信息
             Map<String, Object> mappingsMap = new HashMap<>();
             for (Map.Entry<String, IndexMappingRecord> entry : mappings.entrySet()) {
                 String index = entry.getKey();
                 IndexMappingRecord record = entry.getValue();

                 // 获取mappings内容并转换为Map
                 if (record.mappings() != null) {
                     String mappingJson = record.mappings().toString();
                     Map<String, Object> mappingMap = JsonUtils.toMap(mappingJson);
                     mappingsMap.put(index, mappingMap);
                 }
             }
             esIndexResponse.setMappings(mappingsMap);

             // 初始化其他字段为空
             esIndexResponse.setAliases(new HashMap<>());
             esIndexResponse.setSettings(new HashMap<>());
             esIndexResponse.setSettingsObj(new HashMap<>());

             return esIndexResponse;
         } catch (Exception e) {
             throw new EsException("getMappings error", e);
         }
     }
     
     @Override
     public String explain(String sql) {
         String dsl = sql2Dsl(sql, true);
         if (dsl == null) {
             return null;
         }
         // 匹配 SQL 语句中的表名
         String tableName = getTableName(sql);
         return executeDSL(dsl, tableName);
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
                Query query = Ep8QueryConverter.toEsQuery(esQueryParamWrapper.getBoolQueryBuilder());
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
    public long count(String type, EsParamWrapper esParamWrapper, String... indexs) {
        try {
            CountRequest.Builder countBuilder = new CountRequest.Builder();
            countBuilder.index(Arrays.asList(indexs));
            
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            if (esQueryParamWrapper.getBoolQueryBuilder() != null) {
                Query query = Ep8QueryConverter.toEsQuery(esQueryParamWrapper.getBoolQueryBuilder());
                countBuilder.query(query);
            }
            
            CountResponse response = elasticsearchClient.count(countBuilder.build());
            return response.count();
        } catch (IOException e) {
            throw new EsException("count IOException", e);
        }
    }
    
    
    protected Object handlerSaveParamter(String index,Object esData) {
        Map<String, EsObjectHandler> esObjectHandler = GlobalConfigCache.ES_OBJECT_HANDLER;
        if (esObjectHandler !=null){
            EsObjectHandler objectHandler = esObjectHandler.get(index);
            if (objectHandler == null){
                objectHandler = esObjectHandler.get("global");
            }
            if (objectHandler != null && objectHandler.insertFill()!=null){
                objectHandler.setInsertFeild(esData);
                
            }
        }
        return esData;
    }
    
    protected Object handlerUpdateParamter(String index,Object esData) {
        Map<String, EsObjectHandler> esObjectHandler = GlobalConfigCache.ES_OBJECT_HANDLER;
        if (esObjectHandler !=null){
            EsObjectHandler objectHandler = esObjectHandler.get(index);
            if (objectHandler == null){
                objectHandler = esObjectHandler.get("global");
            }
            if (objectHandler != null && objectHandler.updateFill()!=null){
                objectHandler.setUpdateFeild(esData);
            }
        }
        return esData;
    }
    
    
    /**
     * 打印信息日志
     *
     * @param format 格式
     * @param params 参数个数
     */
    private void printSearchInfoLog(String format, Object... params) {
        boolean enableSearchLog = GlobalConfigCache.GLOBAL_CONFIG.isEnableSearchLog();
        if (enableSearchLog) {
            log.info("es-plus " + format, params);
        }
    }
    
    
    private void printInfoLog(String format, Object... params) {
        log.info("es-plus " + format, params);
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

    private String getTableName(String sql) {
        // 匹配 SQL 语句中的表名
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)FROM\\s+([\\w.]+)");
        java.util.regex.Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        // 提取表名
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        if (StringUtils.isBlank(tableName)) {
            throw new EsException("sql语句中未找到表名");
        }
        return tableName;
    }
}