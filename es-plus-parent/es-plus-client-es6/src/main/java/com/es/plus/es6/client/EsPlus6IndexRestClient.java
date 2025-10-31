package com.es.plus.client;

import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsPlusIndexClient;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.params.EsAliasResponse;
import com.es.plus.common.params.EsIndexResponse;
import com.es.plus.common.params.EsSettings;
import com.es.plus.common.pojo.EsPlusGetTaskResponse;
import com.es.plus.common.pojo.es.EpQueryBuilder;
import com.es.plus.common.properties.EsIndexParam;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.JsonUtils;
import com.es.plus.constant.EsConstant;
import com.es.plus.es6.convert.EpQueryConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksResponse;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.TaskId;
import org.elasticsearch.tasks.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.es.plus.constant.EsConstant.DEFAULT_REINDEX_VERSION_TYPE;
import static com.es.plus.constant.EsConstant.PROPERTIES;

/**
 * es索引管理者
 *
 * @author hzh
 * @date 2022/09/03
 */
public class EsPlus6IndexRestClient implements EsPlusIndexClient {
    
    private static final Logger log = LoggerFactory.getLogger(EsPlus6IndexRestClient.class);
    
    private final RestHighLevelClient restHighLevelClient;
    
    
    public EsPlus6IndexRestClient(Object restHighLevelClient) {
        this.restHighLevelClient = (RestHighLevelClient) restHighLevelClient;
    }
    
    /**
     * 创建索引
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public void createIndex(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            for (String esIndexParamIndex : esIndexParam.getIndex()) {
                CreateIndexRequest indexRequest = new CreateIndexRequest(esIndexParamIndex);
                indexRequest(esIndexParam, indexRequest);
            }
        }
        
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        indexRequest(esIndexParam, indexRequest);
    }
    
    @Override
    public boolean createIndex(String index) {
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        
        CreateIndexResponse indexResponse = null;
        try {
            indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return indexResponse.isAcknowledged();
    }
    
    @Override
    public boolean createIndex(String index,String alias,EsSettings esSettings,Map<String, Object> mappings) {
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        Settings.Builder settings = Settings.builder();
        
        if (esSettings != null) {
            String json = JsonUtils.toJsonStr(esSettings);
            settings.loadFromSource(json, XContentType.JSON);
            indexRequest.settings(settings);
        }
        if (mappings != null) {
            indexRequest.mapping(mappings);
        }
        if (StringUtils.isNotBlank(alias)) {
            indexRequest.alias(new Alias(alias));
        }
        
        CreateIndexResponse indexResponse = null;
        try {
            try {
                BytesReference reference = XContentHelper.toXContent(indexRequest, XContentType.JSON, true);
                String string = reference.utf8ToString();
                log.info("createIndex index:{} :{}",index, string);
            } catch (IOException e) {
                throw new ElasticsearchException(e);
            }
            indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            printErrorLog("createIndex:{}", e);
            return false;
        }
        return indexResponse.isAcknowledged();
    }
    
    @Override
    public boolean createIndex(String index,String[] alias,Map<String, Object> esSettings,Map<String, Object> mappings) {
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        Settings.Builder settings = Settings.builder();
        
        if (esSettings != null) {
            String json = JsonUtils.toJsonStr(esSettings);
            settings.loadFromSource(json, XContentType.JSON);
            indexRequest.settings(settings);
        }
        if (mappings != null) {
            indexRequest.mapping(mappings);
        }
        if (ArrayUtils.isNotEmpty(alias)) {
            List<Alias> newAliases = new ArrayList<>();
            for (String a : alias) {
                Alias one = new Alias(a);
                newAliases.add(one);
            }
            indexRequest.aliases(newAliases);
        }
        
        CreateIndexResponse indexResponse = null;
        try {
            try {
                BytesReference reference = XContentHelper.toXContent(indexRequest, XContentType.JSON, true);
                String string = reference.utf8ToString();
                log.info("createIndex index:{} :{}",index, string);
            } catch (IOException e) {
                throw new ElasticsearchException(e);
            }
            indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            printErrorLog("createIndex:{}", e);
            return false;
        }
        return indexResponse.isAcknowledged();
    }
    
    /**
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public boolean putMapping(String index, Class<?> tClass) {
        EsIndexParam esDocParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            for (String esDocParamIndex : esDocParam.getIndex()) {
                Map<String, Object> mappingProperties = esDocParam.getMappings();
                try {
                    //将settings和mappings封装到一个IndexClient对象中
                    PutMappingRequest putMappingRequest = new PutMappingRequest(esDocParamIndex);
                    putMappingRequest.source(mappingProperties);
                    printInfoLog("putMapping index={} info={}", esDocParamIndex, JsonUtils.toJsonStr(mappingProperties));
                    restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
                    return true;
                } catch (IOException e) {
                    printErrorLog("putMapping:{}", e);
                    return false;
                }
            }
        }else{
            Map<String, Object> mappingProperties = esDocParam.getMappings();
            try {
                //将settings和mappings封装到一个IndexClient对象中
                PutMappingRequest putMappingRequest = new PutMappingRequest(index);
                putMappingRequest.source(mappingProperties);
                printInfoLog("putMapping index={} info={}", index, JsonUtils.toJsonStr(mappingProperties));
                restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
                return true;
            } catch (IOException e) {
                printErrorLog("putMapping:{}", e);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void putMapping(String index, Map<String, Object> mappingProperties) {
        try {
            //将settings和mappings封装到一个IndexClient对象中
            PutMappingRequest putMappingRequest = new PutMappingRequest(index);
            putMappingRequest.source(mappingProperties);
            printInfoLog("putMapping info={}", JsonUtils.toJsonStr(mappingProperties));
            restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("mappingRequest error", e);
        }
    }
    
    /**
     * 创建索引映射
     *
     * @param index  指数
     * @param tClass t类
     */
    /**
     * 创建索引映射
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public void createIndexMapping(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            String[] indexs = esIndexParam.getIndex();
            for (String idx : indexs) {
                doCreateIndexMapping(idx, esIndexParam);
            }
        }else{
            doCreateIndexMapping(index, esIndexParam);
        }
    }
    
    /**
     * 创建索引没有别名
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public boolean createIndexWithoutAlias(String index, Class<?> tClass) {
        // 如果已经存在
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        boolean exists = this.indexExists(indexRequest.index());
        
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        //创建索引的settings
        Settings.Builder settings = Settings.builder();
        
        EsSettings esSettings = esIndexParam.getEsSettings();
        if (esSettings != null) {
            String json = JsonUtils.toJsonStr(esSettings);
            settings.loadFromSource(json, XContentType.JSON);
        }
        try {
            if (!exists) {
                indexRequest.settings(settings).mapping(esIndexParam.getMappings());
                printInfoLog("createMapping settings={},mappings:{}", settings.build().toString(),
                        JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse indexResponse = restHighLevelClient.indices()
                        .create(indexRequest, RequestOptions.DEFAULT);
                return indexResponse.isAcknowledged();
            }
        } catch (Exception e) {
            throw new EsException("mappingRequest error", e);
        }
        return false;
    }
    
    /**
     * 删除索引
     */
    @Override
    public boolean deleteIndex(String index) {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            boolean acknowledged = delete.isAcknowledged();
            printInfoLog("deleteIndex index={} ack:{}", index, acknowledged);
            return acknowledged;
        } catch (IOException e) {
            throw new RuntimeException("delete index error ", e);
        }
    }
    
    
    /**
     * 得到索引
     *
     * @param indexName 索引名称
     * @return {@link GetIndexResponse}
     */
    @Override
    public EsIndexResponse getIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            EsIndexResponse esIndexResponse = new EsIndexResponse();
            GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
            
            Map<String, String> settingsMap = new LinkedHashMap<>();
            Map<String, Settings> settings = getIndexResponse.getSettings();
            
            settings.forEach((k, v) -> {
                String string = v.toString();
                string = string.replace("index.","");
                settingsMap.put(k, string);
            });
            
            
            String[] indices = getIndexResponse.getIndices();
            Map<String, Object> mappingMap = new HashMap<>();
            
            Map<String, MappingMetaData> mappings = getIndexResponse.getMappings();
            mappings.forEach((k,v)->{
                Map<String, Object> sourceMap = v.getSourceAsMap();
                mappingMap.put(k,sourceMap);
            });
            esIndexResponse.setIndices(indices);
            esIndexResponse.setMappings(mappingMap);
            esIndexResponse.setSettings(settingsMap);
            if (!CollectionUtils.isEmpty( getIndexResponse.getAliases())) {
                Map<String, List<String>> aliasesMap = new LinkedHashMap<>();
                getIndexResponse.getAliases().forEach((k,v)->{
                    List<String> alias = v.stream().map(AliasMetaData::getAlias)
                            .collect(Collectors.toList());
                    aliasesMap.put(k,alias);
                });
                
                esIndexResponse.setAliases(aliasesMap);
            }
            
            return esIndexResponse;
        } catch (IOException e) {
            throw new EsException("getIndex IOException", e);
        } catch (ElasticsearchStatusException e) {
            if (e.status().equals(RestStatus.NOT_FOUND)) {
                return null;
            }
            throw e;
        }
    }
    
    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    @Override
    public EsAliasResponse getAliasIndex(String alias) {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        try {
            EsAliasResponse esAliasResponse = new EsAliasResponse();
            GetAliasesResponse aliasesResponse = restHighLevelClient.indices()
                    .getAlias(request, RequestOptions.DEFAULT);
            Set<String> indexs = aliasesResponse.getAliases().keySet();
            esAliasResponse.setIndexs(indexs);
            return esAliasResponse;
        } catch (IOException e) {
            throw new EsException("getIndex IOException", e);
        }
    }
    
    
    /**
     * 查询index是否存在
     */
    @Override
    public boolean indexExists(String index) {
        try {
            return restHighLevelClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("index exists", e);
        }
    }
    
    /**
     * 更新别名
     *
     * @param oldIndexName 旧索引名称
     * @param reindexName  重建索引名称
     * @param alias        别名
     * @return boolean
     */
    @Override
    public boolean swapAlias(String oldIndexName, String reindexName, String alias) {
        IndicesAliasesRequest.AliasActions addIndexAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.ADD).index(reindexName).alias(alias);
        IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(oldIndexName).alias(alias);
        
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        indicesAliasesRequest.addAliasAction(removeAction);
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices()
                    .updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw new EsException("swapAlias exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName,
                    e);
        }
    }
    
    
    @Override
    public boolean replaceAlias(String index, String oldAlias, String alias) {
        IndicesAliasesRequest.AliasActions addIndexAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.ADD).index(index).alias(alias);
        IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(index).alias(oldAlias);
        
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        indicesAliasesRequest.addAliasAction(removeAction);
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices()
                    .updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw new EsException("replaceAlias exception index:" + index + ", oldAlias:  " + oldAlias, e);
        }
    }
    
    @Override
    public String getAliasByIndex(String index) {
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest();
        getAliasesRequest.indices(index);
        
        GetAliasesResponse getAliasesResponse = null;
        try {
            getAliasesResponse = restHighLevelClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (getAliasesResponse!=null && getAliasesResponse.getAliases() != null && !CollectionUtils
                .isEmpty(getAliasesResponse.getAliases().values())) {
            Collection<Set<AliasMetaData>> values = getAliasesResponse.getAliases().values();
            AliasMetaData aliasMetaData = values.stream().findFirst().get().stream().findFirst().get();
            return aliasMetaData.getAlias();
        }
        return null;
    }
    
    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    @Override
    public boolean reindex(String oldIndexName, String reindexName) {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(reindexName);
        reindexRequest.setDestOpType(EsConstant.DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(true);
        reindexRequest.setDestVersionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE));
        reindexRequest.getSearchRequest().source().fetchSource(null, EsConstant.REINDEX_TIME_FILED);
        reindexRequest.setSourceBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        reindexRequest.setTimeout(TimeValue.timeValueNanos(Long.MAX_VALUE));
        try {
            BulkByScrollResponse response = restHighLevelClient.reindex(reindexRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
            if (CollectionUtils.isEmpty(bulkFailures)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName,
                    e);
        }
    }
    
    /**
     * 重新索引
     *
     * @param oldIndexName  老索引名称
     * @param reindexName   重新索引名称
     * @param changeMapping 更改属性映射
     * @return boolean
     */
    @Override
    public boolean reindex(String oldIndexName, String reindexName, Map<String, Object> changeMapping) {
        boolean exists = indexExists(reindexName);
        if (!exists) {
            EsIndexResponse indexResponse = getIndex(oldIndexName);
            Map<String, Object> mappings = indexResponse.getMappings(oldIndexName);
            //更换新的配置属性
            Map<String, Object> properties = (Map<String, Object>) mappings.get(PROPERTIES);
            properties.putAll(changeMapping);
            boolean index = createIndex(reindexName);
            putMapping(reindexName, mappings);
        }
        return reindex(oldIndexName, reindexName);
    }
    
    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    @Override
    public boolean reindex(String oldIndexName, String reindexName, EpQueryBuilder esQueryBuilder) {
        QueryBuilder queryBuilder = EpQueryConverter.toEsQueryBuilder(esQueryBuilder);
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(reindexName);
        reindexRequest.setDestOpType(EsConstant.DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(true);
        reindexRequest.setDestVersionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE));
        reindexRequest.getSearchRequest().source().fetchSource(null, EsConstant.REINDEX_TIME_FILED);
        //        reindexRequest.setSourceQuery(QueryBuilders.rangeQuery(EsConstant.REINDEX_TIME_FILED).gte(currentTime));
        reindexRequest.setSourceQuery(queryBuilder);
        reindexRequest.setSourceBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        reindexRequest.setTimeout(TimeValue.timeValueNanos(Long.MAX_VALUE));
        try {
            if (queryBuilder != null) {
                log.info("reindex oldIndexName:{},targetName:{} queryBuilder:{}", oldIndexName, reindexName,
                        queryBuilder);
            }
            BulkByScrollResponse response = restHighLevelClient.reindex(reindexRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
            if (CollectionUtils.isEmpty(bulkFailures)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName,
                    e);
        }
    }
    
    /**
     * 更新设置
     *
     * @param index      指数
     * @param esSettings es设置
     * @return boolean
     */
    @Override
    public boolean updateSettings(EsSettings esSettings,String... index) {
        String json = JsonUtils.toJsonStr(esSettings);
        Settings settings = Settings.builder().loadFromSource(json, XContentType.JSON).build();
        //创建索引的settings
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(settings, index);
        
        //执行put
        AcknowledgedResponse settingsResult = null;
        try {
            settingsResult = restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
        
        //成功的话，返回结果是true
        return settingsResult.isAcknowledged();
    }
    
    @Override
    public boolean updateSettings(Map<String, Object> esSettings,String... index) {
        String json = JsonUtils.toJsonStr(esSettings);
        Settings settings = Settings.builder().loadFromSource(json, XContentType.JSON).build();
        //创建索引的settings
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(settings, index);
        
        //执行put
        AcknowledgedResponse settingsResult = null;
        try {
            settingsResult = restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
        
        //成功的话，返回结果是true
        return settingsResult.isAcknowledged();
    }
    
    /**
     * 连接
     */
    @Override
    public boolean ping() {
        try {
            return restHighLevelClient.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es initPing error", e);
            return false;
        }
    }
    
    @Override
    public void createAlias(String currentIndex, String alias) {
        IndicesAliasesRequest.AliasActions addIndexAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.ADD).index(currentIndex).alias(alias);
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices()
                    .updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("createAlias exception", e);
        }
    }
    
    @Override
    public void removeAlias(String index, String alias) {
        IndicesAliasesRequest.AliasActions remove = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(index).alias(alias);
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(remove);
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices()
                    .updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("createAlias exception", e);
        }
    }
    
    @Override
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... index) {
        ForceMergeRequest request = new ForceMergeRequest(index);
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        request.maxNumSegments(maxSegments);
        request.onlyExpungeDeletes(onlyExpungeDeletes);
        request.flush(flush);
        try {
            ForceMergeResponse forceMergeResponse = restHighLevelClient.indices()
                    .forcemerge(request, RequestOptions.DEFAULT);
            int successfulShards = forceMergeResponse.getSuccessfulShards();
            return successfulShards > 0;
        } catch (IOException e) {
            throw new EsException("forceMerge exception", e);
        }
    }
    
    @Override
    public boolean refresh(String... index) {
        RefreshRequest request = new RefreshRequest(index);
        try {
            RefreshResponse refresh = restHighLevelClient.indices().refresh(request, RequestOptions.DEFAULT);
            return refresh.getSuccessfulShards() == Arrays.stream(index).count();
        } catch (IOException e) {
            throw new EsException("refresh exception", e);
        }
    }
    
    
    /**
     * 索引请求
     *
     * @param esIndexParam es指数参数
     * @param indexRequest 指标要求
     */
    private void indexRequest(EsIndexParam esIndexParam, CreateIndexRequest indexRequest) {
        //创建索引的settings
        Settings.Builder settings = Settings.builder();
        if (esIndexParam != null) {
            EsSettings esSettings = esIndexParam.getEsSettings();
            if (esSettings != null) {
                String json = JsonUtils.toJsonStr(esSettings);
                settings.loadFromSource(json, XContentType.JSON);
            }
            if (ArrayUtils.isNotEmpty(esIndexParam.getAlias())) {
                List<Alias> aliases = new ArrayList<>();
                for (String alias : esIndexParam.getAlias()) {
                    aliases.add(new Alias(alias));
                }
                indexRequest.aliases(aliases);
            }
        }
        try {
            //将settings封装到一个IndexClient对象中
            indexRequest.settings(settings);
            CreateIndexResponse indexResponse = restHighLevelClient.indices()
                    .create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
    }
    
    /**
     * 创建索引映射
     *
     * @param index        指数
     * @param esIndexParam es指数参数
     */
    private void doCreateIndexMapping(String index, EsIndexParam esIndexParam) {
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        //创建索引的settings
        Settings.Builder settings = Settings.builder();
        
        EsSettings esSettings = esIndexParam.getEsSettings();
        if (esSettings != null) {
            String json = JsonUtils.toJsonStr(esSettings);
            settings.loadFromSource(json, XContentType.JSON);
        }
        if (ArrayUtils.isNotEmpty(esIndexParam.getAlias())) {
            List<Alias> aliases = new ArrayList<>();
            for (String alias : esIndexParam.getAlias()) {
                aliases.add(new Alias(alias));
            }
            indexRequest.aliases(aliases);
        }
        
        try {
            boolean exists = this.indexExists(indexRequest.index());
            if (!exists) {
                indexRequest.settings(settings).mapping(esIndexParam.getMappings());
                printInfoLog("doCreateIndexMapping index={} settings={},mappings:{}", indexRequest.index(),
                        settings.build().toString(), JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse indexResponse = restHighLevelClient.indices()
                        .create(indexRequest, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch mappingRequest error", e);
        }
    }
    
    @Override
    public String getIndexStat() {
        
        Map<String, Object> jsonRequest = new HashMap<>();
        
        // "_xpack/sql/translate"
        Request request = new Request("get", "/_cluster/stats?pretty");
        request.setJsonEntity(JsonUtils.toJsonStr(jsonRequest));
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            String res = EntityUtils.toString(response.getEntity());
            return res;
        } catch (IOException e) {
            log.error("executeSql", e);
        }
        return null;
    }
    
    @Override
    public String getIndexHealth() {
        
        Map<String, Object> jsonRequest = new HashMap<>();
        
        // "_xpack/sql/translate"
        Request request = new Request("get", "/_cluster/health?pretty");
        request.setJsonEntity(JsonUtils.toJsonStr(jsonRequest));
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            String res = EntityUtils.toString(response.getEntity());
            return res;
        } catch (IOException e) {
            log.error("executeSql", e);
        }
        return null;
    }
    
    @Override
    public String getNodes() {
        Map<String, Object> jsonRequest = new HashMap<>();
        
        // "_xpack/sql/translate"
        Request request = new Request("get", "/_nodes");
        request.setJsonEntity(JsonUtils.toJsonStr(jsonRequest));
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            String res = EntityUtils.toString(response.getEntity());
            return res;
        } catch (IOException e) {
            log.error("getNodes(", e);
        }
        return null;
    }
    
    @Override
    public String cmdGet(String cmd) {
        Map<String, Object> jsonRequest = new HashMap<>();
        
        // "_xpack/sql/translate"
        Request request = new Request("get", cmd);
        request.setJsonEntity(JsonUtils.toJsonStr(jsonRequest));
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
            String res = EntityUtils.toString(response.getEntity());
            return res;
        } catch (IOException e) {
            log.error("cmdGet", e);
        }
        return null;
    }
    
    @Override
    public String reindexTaskAsync(String oldIndexName, String reindexName) {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(reindexName);
        reindexRequest.setDestOpType(EsConstant.DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(true);
        reindexRequest.setDestVersionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE));
        reindexRequest.setSourceBatchSize(GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
        reindexRequest.setTimeout(TimeValue.timeValueNanos(Long.MAX_VALUE));
        try {
            
            TaskSubmissionResponse response = restHighLevelClient.submitReindexTask(reindexRequest, RequestOptions.DEFAULT );
            return response.getTask();
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName,
                    e);
        }
    }
    
    @Override
    public String reindexTaskList() {
        ListTasksRequest listTasksRequest = new ListTasksRequest();
        listTasksRequest.setActions("reindex"); // 筛选Reindex任务
        ListTasksResponse listTasksResponse = null;
        try {
            listTasksResponse = restHighLevelClient.tasks().list(listTasksRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("reindexTaskList exception oldIndexName:", e);
        }
        for (TaskInfo taskInfo : listTasksResponse.getTasks()) {
            if ("reindex".equals(taskInfo.getType())) {
                String fullTaskId = taskInfo.getTaskId().toString();
                
            }
        }
        return listTasksResponse.toString();
    }
    
    @Override
    public EsPlusGetTaskResponse reindexTaskGet(String taskId) {
        String[] split = taskId.split(":");
        GetTaskRequest listTasksRequest = new GetTaskRequest(split[0],Long.parseLong(split[1]));
        
        try {
            Optional<GetTaskResponse> getTaskResponse = restHighLevelClient.tasks()
                    .get(listTasksRequest, RequestOptions.DEFAULT);
            
            GetTaskResponse response = getTaskResponse.orElse(null);
            if (response != null) {
                EsPlusGetTaskResponse res = new EsPlusGetTaskResponse();
                TaskInfo taskInfo = response.getTaskInfo();
                if (taskInfo!=null) {
                    String taskInfoString = taskInfo.toString();
                    res.setTaskInfo(taskInfoString);
                }
                res.setCompleted(response.isCompleted());
                return res;
            }
        } catch (IOException e) {
            throw new EsException("reindexTaskList exception oldIndexName:", e);
        }
        return null;
    }
    
    /**
     * 取消任务
     * @return
     */
    @Override
    public Boolean cancelTask(String taskId) {
        try {
            CancelTasksRequest cancelTasksRequest =   new CancelTasksRequest();
            cancelTasksRequest.setTaskId(new TaskId(taskId));
            // 执行取消操作
            CancelTasksResponse cancel = restHighLevelClient.tasks().cancel(cancelTasksRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            throw new EsException("reindexTaskList exception oldIndexName:", e);
        }
    }
    
    /**
     * 打印信息日志
     *
     * @param message 信息
     * @param params  参数个数
     */
    private void printInfoLog(String message, Object... params) {
        log.info("es-plus " + message, params);
    }
    
    /**
     * 打印错误日志
     *
     * @param message 错误信息
     * @param params  参数个数
     */
    private void printErrorLog(String message, Object... params) {
        log.error("es-plus " + message, params);
    }
    
}