package com.es.plus.es7.client;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusIndexClient;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.constant.EsConstant;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * es索引管理者
 *
 * @author hzh
 * @date 2022/09/03
 */
public class EsPlusIndexRestClient implements EsPlusIndexClient {
    private static final Logger log = LoggerFactory.getLogger(EsPlusIndexRestClient.class);
    private final RestHighLevelClient restHighLevelClient;


    public EsPlusIndexRestClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 创建索引
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public void createIndex(String index, Class<?> tClass) {
        EsIndexParam esDocParam = EsParamHolder.getEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            index = esDocParam.getIndex();
        }

        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        indexRequest(esDocParam, indexRequest);
    }

    /**
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public void putMapping(String index, Class<?> tClass) {
        EsIndexParam esDocParam = EsParamHolder.getEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            index = esDocParam.getIndex();
        }
        Map<String, Object> mappingProperties = esDocParam.getMappings();
        try {
            //将settings和mappings封装到一个IndexClient对象中
            PutMappingRequest putMappingRequest = new PutMappingRequest(index);
            putMappingRequest.source(mappingProperties);
            printInfoLog("putMapping index={} info={}", index, JsonUtils.toJsonStr(mappingProperties));
            restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("mappingRequest error", e);
        }
    }

    @Override
    public void putMapping(String index, Map<String, Object> mappingProperties) {
        try {
            //将settings和mappings封装到一个IndexClient对象中
            PutMappingRequest putMappingRequest = new PutMappingRequest(index);
            putMappingRequest.source(mappingProperties);
            printInfoLog("putMapping index={} info={}", index, JsonUtils.toJsonStr(mappingProperties));
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
    @Override
    public void createIndexMapping(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            index = esIndexParam.getIndex();
        }
        doCreateIndexMapping(index, esIndexParam);
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

        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(tClass);
        //创建索引的settings
        Settings.Builder settings = Settings.builder();

        EsSettings esSettings = esIndexParam.getEsSettings();
        if (esSettings != null) {
            String json = JsonUtils.toJsonStr(esSettings);
            settings.loadFromSource(json, XContentType.JSON);
        }
        try {
            if (!exists) {
                indexRequest
                        .settings(settings)
                        .mapping(esIndexParam.getMappings());
                printInfoLog("createMapping index={} settings={},mappings:{}", index, settings.build().toString(), JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
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

            Map<String, String> settingsMap = new HashMap<>();
            Collection<Settings> values = getIndexResponse.getSettings().values();
            Optional<Settings> first = values.stream().findFirst();
            first.ifPresent(s -> {
                Set<String> names = s.keySet();
                names.forEach(name -> settingsMap.put(name, s.get(name)));
            });


            String[] indices = getIndexResponse.getIndices();
            Map<String, MappingMetadata> mappings = getIndexResponse.getMappings();
            Map<String, Object> sourceAsMap = mappings.values().stream().findFirst().get().getSourceAsMap();
            esIndexResponse.setIndices(indices);
            esIndexResponse.setMappings(sourceAsMap);
            esIndexResponse.setSettings(settingsMap);
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
            GetAliasesResponse aliasesResponse = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
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
    public boolean replaceAlias(String oldIndexName, String reindexName, String alias) {
        IndicesAliasesRequest.AliasActions addIndexAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.ADD).index(reindexName).alias(alias);
        IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(
                IndicesAliasesRequest.AliasActions.Type.REMOVE).index(oldIndexName).alias(alias);

        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(addIndexAction);
        indicesAliasesRequest.addAliasAction(removeAction);
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest,
                    RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName, e);
        }
    }

    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    @Override
    public boolean reindex(String oldIndexName, String reindexName, Long currentTime) {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(reindexName);
        reindexRequest.setDestOpType(EsConstant.DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(EsConstant.DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(true);
        reindexRequest.getSearchRequest().source().fetchSource(null, EsConstant.REINDEX_TIME_FILED);
        if (currentTime != null) {
            reindexRequest.setSourceQuery(QueryBuilders.rangeQuery(EsConstant.REINDEX_TIME_FILED).gte(currentTime));
        }
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
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName:  " + reindexName, e);
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
    public boolean updateSettings(String index, EsSettings esSettings) {
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
    public boolean updateSettings(String index, Map<String, Object> esSettings) {
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
        } catch (IOException e) {
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
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest,
                    RequestOptions.DEFAULT);
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
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest,
                    RequestOptions.DEFAULT);
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
            ForceMergeResponse forceMergeResponse = restHighLevelClient.indices().forcemerge(request, RequestOptions.DEFAULT);
            int successfulShards = forceMergeResponse.getSuccessfulShards();
            return successfulShards > 0;
        } catch (IOException e) {
            throw new EsException("forceMerge exception", e);
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
            if (StringUtils.isNotBlank(esIndexParam.getAlias())) {
                indexRequest.alias(new Alias(esIndexParam.getAlias()));
            }
        }
        try {
            //将settings封装到一个IndexClient对象中
            indexRequest
                    .settings(settings);
            CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
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
        if (StringUtils.isNotBlank(esIndexParam.getAlias())) {
            indexRequest.alias(new Alias(esIndexParam.getAlias()));
        }

        try {
            boolean exists = this.indexExists(indexRequest.index());
            if (!exists) {
                indexRequest
                        .settings(settings)
                        .mapping(esIndexParam.getMappings());
                printInfoLog("doCreateIndexMapping index={} settings={},mappings:{}", indexRequest.index(), settings.build().toString(), JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch mappingRequest error", e);
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
    private void printErrorLog(String format, Object... params) {
        log.error("es-plus " + format, params);
    }

}
