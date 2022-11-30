package com.es.plus.client;

import com.es.plus.exception.EsException;
import com.es.plus.pojo.EsSettings;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.lock.EsLockFactory;
import com.es.plus.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static com.es.plus.config.GlobalConfigCache.GLOBAL_CONFIG;
import static com.es.plus.constant.EsConstant.*;

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
        if (StringUtils.isBlank(index)) {
            throw new EsException("createMapping index not exists");
        }
        EsIndexParam esDocParam = EsParamHolder.getEsIndexParam(tClass);
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
        Map<String, Object> mappingProperties = EsParamHolder.getEsIndexParam(tClass).getMappings();
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
    @Override
    public void createIndexMapping(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(tClass);
        doCreateIndexMapping(index, esIndexParam);
    }

    /**
     * 创建索引没有别名
     *
     * @param index  指数
     * @param tClass t类
     */
    @Override
    public void deleteAndCreateIndexWithoutAlias(String index, Class<?> tClass) {
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
                printInfoLog("createMapping settings={},mappings:{}", settings.build().toString(), JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new EsException("mappingRequest error", e);
        }
    }

    /**
     * 删除索引
     */
    @Override
    public void deleteIndex(String index) {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            printInfoLog("deleteIndex index={}", index);
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
    public GetIndexResponse getIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("getIndex IOException", e);
        }
    }

    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    @Override
    public GetAliasesResponse getAliasIndex(String alias) {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        try {
            return restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
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
    public boolean updateAlias(String oldIndexName, String reindexName, String alias) {
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
    public boolean reindex(String oldIndexName, String reindexName, QueryBuilder queryBuilder) {
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(oldIndexName);
        reindexRequest.setDestIndex(reindexName);
        reindexRequest.setDestOpType(DEFAULT_DEST_OP_TYPE);
        reindexRequest.setConflicts(DEFAULT_CONFLICTS);
        reindexRequest.setRefresh(true);
        reindexRequest.getSearchRequest().source().fetchSource(null, REINDEX_TIME_FILED);
        if (queryBuilder != null) {
            reindexRequest.setSourceQuery(queryBuilder);
        }
        reindexRequest.setSourceBatchSize(GLOBAL_CONFIG.getBatchSize());
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
                printInfoLog("createMapping settings={},mappings:{}", settings.build().toString(), JsonUtils.toJsonStr(esIndexParam.getMappings()));
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
