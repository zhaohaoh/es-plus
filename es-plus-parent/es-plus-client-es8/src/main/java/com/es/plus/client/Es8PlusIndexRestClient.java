package com.es.plus.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.ReindexRequest;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.get_alias.IndexAliases;
import co.elastic.clients.elasticsearch.tasks.*;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.VersionType;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.json.JsonData;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

import static com.es.plus.constant.EsConstant.DEFAULT_REINDEX_VERSION_TYPE;
import static com.es.plus.constant.EsConstant.PROPERTIES;

/**
 * ES8索引管理客户端
 *
 * @author hzh
 * @date 2024/09/19
 */
public class Es8PlusIndexRestClient implements EsPlusIndexClient {

    private static final Logger log = LoggerFactory.getLogger(Es8PlusIndexRestClient.class);

    private final ElasticsearchClient elasticsearchClient;

    public Es8PlusIndexRestClient(Object elasticsearchClient) {
        this.elasticsearchClient = (ElasticsearchClient) elasticsearchClient;
    }

    @Override
    public void createIndex(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            for (String esIndexParamIndex : esIndexParam.getIndex()) {
                createIndexRequest(esIndexParamIndex, esIndexParam);
            }
        } else {
            createIndexRequest(index, esIndexParam);
        }
    }

    @Override
    public boolean createIndex(String index) {
        try {
            CreateIndexRequest request = CreateIndexRequest.of(builder -> builder.index(index));
            CreateIndexResponse response = elasticsearchClient.indices().create(request);
            return response.acknowledged();
        } catch (IOException e) {
            printErrorLog("createIndex:{}", e);
            return false;
        }
    }

    @Override
    public boolean createIndex(String index, String alias, EsSettings esSettings, Map<String, Object> mappings) {
        try {
            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
            builder.index(index);

            if (esSettings != null) {
                Map<String, JsonData> settingsMap = convertEsSettingsToMap(esSettings);
                builder.settings(s -> s.otherSettings(settingsMap));
            }

            if (mappings != null) {
                builder.mappings(TypeMapping.of(m -> m.properties(convertMappingsToProperties(mappings))));
            }

            if (StringUtils.isNotBlank(alias)) {
                builder.aliases(alias, a -> a);
            }

            CreateIndexResponse response = elasticsearchClient.indices().create(builder.build());
            printInfoLog("createIndex index:{} acknowledged:{}", index, response.acknowledged());
            return response.acknowledged();
        } catch (IOException e) {
            printErrorLog("createIndex:{}", e);
            return false;
        }
    }

    @Override
    public boolean createIndex(String index, String[] aliases, Map<String, Object> esSettings, Map<String, Object> mappings) {
        try {
            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
            builder.index(index);

            if (esSettings != null) {
                Map<String, JsonData> settingsMap = convertSettingsToMap(esSettings);
                builder.settings(s -> s.otherSettings(settingsMap));
            }

            if (mappings != null) {
                builder.mappings(TypeMapping.of(m -> m.properties(convertMappingsToProperties(mappings))));
            }

            if (ArrayUtils.isNotEmpty(aliases)) {
                Map<String, co.elastic.clients.elasticsearch.indices.Alias> aliasMap = new HashMap<>();
                for (String alias : aliases) {
                    aliasMap.put(alias, co.elastic.clients.elasticsearch.indices.Alias.of(a -> a));
                }
                builder.aliases(aliasMap);
            }

            CreateIndexResponse response = elasticsearchClient.indices().create(builder.build());
            printInfoLog("createIndex index:{} acknowledged:{}", index, response.acknowledged());
            return response.acknowledged();
        } catch (IOException e) {
            printErrorLog("createIndex:{}", e);
            return false;
        }
    }

    @Override
    public boolean putMapping(String index, Class<?> tClass) {
        EsIndexParam esDocParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            for (String esDocParamIndex : esDocParam.getIndex()) {
                return putMappingInternal(esDocParamIndex, esDocParam.getMappings());
            }
        } else {
            return putMappingInternal(index, esDocParam.getMappings());
        }
        return true;
    }

    @Override
    public void putMapping(String index, Map<String, Object> mappingProperties) {
        try {
            PutMappingRequest request = PutMappingRequest.of(builder ->
                builder.index(index).properties(convertMappingsToProperties(mappingProperties))
            );
            printInfoLog("putMapping index={} info={}", index, JsonUtils.toJsonStr(mappingProperties));
            PutMappingResponse response = elasticsearchClient.indices().putMapping(request);
            printInfoLog("putMapping acknowledged:{}", response.acknowledged());
        } catch (IOException e) {
            throw new EsException("mappingRequest error", e);
        }
    }

    @Override
    public void createIndexMapping(String index, Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        if (StringUtils.isBlank(index)) {
            String[] indices = esIndexParam.getIndex();
            for (String idx : indices) {
                doCreateIndexMapping(idx, esIndexParam);
            }
        } else {
            doCreateIndexMapping(index, esIndexParam);
        }
    }

    @Override
    public boolean createIndexWithoutAlias(String index, Class<?> tClass) {
        boolean exists = this.indexExists(index);

        if (!exists) {
            EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);

            try {
                CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
                builder.index(index);

                EsSettings esSettings = esIndexParam.getEsSettings();
                if (esSettings != null) {
                    Map<String, JsonData> settingsMap = convertEsSettingsToMap(esSettings);
                    builder.settings(s -> s.otherSettings(settingsMap));
                }

                builder.mappings(TypeMapping.of(m -> m.properties(convertMappingsToProperties(esIndexParam.getMappings()))));

                printInfoLog("createMapping index={} mappings:{}", index, JsonUtils.toJsonStr(esIndexParam.getMappings()));
                CreateIndexResponse response = elasticsearchClient.indices().create(builder.build());
                return response.acknowledged();
            } catch (Exception e) {
                throw new EsException("mappingRequest error", e);
            }
        }
        return false;
    }

    @Override
    public boolean deleteIndex(String index) {
        try {
            DeleteIndexRequest request = DeleteIndexRequest.of(builder -> builder.index(index));
            DeleteIndexResponse response = elasticsearchClient.indices().delete(request);
            boolean acknowledged = response.acknowledged();
            printInfoLog("deleteIndex index={} ack:{}", index, acknowledged);
            return acknowledged;
        } catch (IOException e) {
            throw new RuntimeException("delete index error ", e);
        }
    }

    @Override
    public EsIndexResponse getIndex(String indexName) {
        try {
            GetIndexRequest request = GetIndexRequest.of(builder -> builder.index(indexName));
            GetIndexResponse response = elasticsearchClient.indices().get(request);

            EsIndexResponse esIndexResponse = new EsIndexResponse();

            Map<String, String> settingsMap = new LinkedHashMap<>();
            response.result().forEach((k, v) -> {
                if (v.settings() != null && v.settings().otherSettings() != null) {
                    v.settings().otherSettings().forEach((sk, sv) -> {
                        settingsMap.put(k + "." + sk, sv.toString());
                    });
                }
            });

            String[] indices = response.result().keySet().toArray(new String[0]);
            Map<String, Object> mappingMap = new LinkedHashMap<>();

            response.result().forEach((k, v) -> {
                if (v.mappings() != null) {
                    mappingMap.put(k, convertTypeMapping(v.mappings()));
                }
            });

            esIndexResponse.setIndices(indices);
            esIndexResponse.setMappings(mappingMap);
            esIndexResponse.setSettings(settingsMap);

            if (!CollectionUtils.isEmpty(response.result())) {
                Map<String, List<String>> aliasesMap = new LinkedHashMap<>();
                response.result().forEach((k, v) -> {
                    if (v.aliases() != null) {
                        List<String> aliasList = new ArrayList<>(v.aliases().keySet());
                        aliasesMap.put(k, aliasList);
                    }
                });
                esIndexResponse.setAliases(aliasesMap);
            }

            return esIndexResponse;
        } catch (IOException e) {
            throw new EsException("getIndex IOException", e);
        } catch (ElasticsearchException e) {
            if (e.status() == 404) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public EsAliasResponse getAliasIndex(String alias) {
        try {
            GetAliasRequest request = GetAliasRequest.of(builder -> builder.name(alias));
            GetAliasResponse response = elasticsearchClient.indices().getAlias(request);

            EsAliasResponse esAliasResponse = new EsAliasResponse();
            Set<String> indices = response.result().keySet();
            esAliasResponse.setIndexs(indices);
            return esAliasResponse;
        } catch (IOException e) {
            throw new EsException("getAliasIndex :", e);
        }
    }

    @Override
    public boolean indexExists(String index) {
        try {
            ExistsRequest request = ExistsRequest.of(builder -> builder.index(index));
            BooleanResponse response = elasticsearchClient.indices().exists(request);
            return response.value();
        } catch (IOException e) {
            throw new EsException("index exists", e);
        }
    }

    @Override
    public boolean swapAlias(String oldIndexName, String reindexName, String alias) {
        try {
            List<co.elastic.clients.elasticsearch.indices.update_aliases.Action> actions = Arrays.asList(
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.add(a -> a.index(reindexName).alias(alias))
                ),
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.remove(r -> r.index(oldIndexName).alias(alias))
                )
            );

            UpdateAliasesRequest request = UpdateAliasesRequest.of(builder ->
                builder.actions(actions)
            );
            UpdateAliasesResponse response = elasticsearchClient.indices().updateAliases(request);
            return response.acknowledged();
        } catch (IOException e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName: " + reindexName, e);
        }
    }

    @Override
    public boolean replaceAlias(String index, String oldAlias, String alias) {
        try {
            List<co.elastic.clients.elasticsearch.indices.update_aliases.Action> actions = Arrays.asList(
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.add(a -> a.index(index).alias(alias))
                ),
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.remove(r -> r.index(index).alias(oldAlias))
                )
            );

            UpdateAliasesRequest request = UpdateAliasesRequest.of(builder ->
                builder.actions(actions)
            );
            UpdateAliasesResponse response = elasticsearchClient.indices().updateAliases(request);
            return response.acknowledged();
        } catch (IOException e) {
            throw new EsException("replaceAlias exception index:" + index + ", oldAlias: " + oldAlias, e);
        }
    }

    @Override
    public String getAliasByIndex(String index) {
        try {
            GetAliasRequest request = GetAliasRequest.of(builder -> builder.index(index));
            GetAliasResponse response = elasticsearchClient.indices().getAlias(request);

            if (response.result() != null && !response.result().isEmpty()) {
                Map<String, IndexAliases> result = response.result();
                for (Map.Entry<String, IndexAliases> entry : result.entrySet()) {
                    if (!entry.getValue().aliases().isEmpty()) {
                        return entry.getValue().aliases().keySet().iterator().next();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean reindex(String oldIndexName, String reindexName, EpQueryBuilder queryBuilder) {
        try {
            ReindexRequest.Builder builder = new ReindexRequest.Builder();
            builder.source(s -> s.index(oldIndexName));
            builder.dest(d -> d.index(reindexName).opType(co.elastic.clients.elasticsearch._types.OpType.Index)
                    .versionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE.toLowerCase())));
            builder.refresh(true);
            builder.size((long) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            builder.timeout(Time.of(t -> t.time("-1")));

            if (queryBuilder != null) {
                builder.source(s -> s.query(Ep8QueryConverter.toEsQuery(queryBuilder)));
                log.info("reindex oldIndexName:{},targetName:{} queryBuilder:{}", oldIndexName, reindexName, queryBuilder);
            }

            ReindexResponse response = elasticsearchClient.reindex(builder.build());
            return response.failures().isEmpty();
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName: " + reindexName, e);
        }
    }

    @Override
    public boolean reindex(String oldIndexName, String reindexName) {
        try {
            ReindexRequest.Builder builder = new ReindexRequest.Builder();
            builder.source(s -> s.index(oldIndexName));
            builder.dest(d -> d.index(reindexName).opType(co.elastic.clients.elasticsearch._types.OpType.Index)
                    .versionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE.toLowerCase())));
            builder.refresh(true);
            builder.size((long) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            builder.timeout(Time.of(t -> t.time("-1")));

            ReindexResponse response = elasticsearchClient.reindex(builder.build());
            return response.failures().isEmpty();
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName: " + reindexName, e);
        }
    }

    @Override
    public String reindexTaskAsync(String oldIndexName, String reindexName) {
        try {
            ReindexRequest.Builder builder = new ReindexRequest.Builder();
            builder.source(s -> s.index(oldIndexName));
            builder.dest(d -> d.index(reindexName).opType(co.elastic.clients.elasticsearch._types.OpType.Index)
                    .versionType(VersionType.valueOf(DEFAULT_REINDEX_VERSION_TYPE.toLowerCase())));
            builder.refresh(true);
            builder.size((long) GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
            builder.timeout(Time.of(t -> t.time("-1")));
            builder.waitForCompletion(false);

            ReindexResponse response = elasticsearchClient.reindex(builder.build());
            return response.task();
        } catch (Exception e) {
            throw new EsException("reindex exception oldIndexName:" + oldIndexName + ", reindexName: " + reindexName, e);
        }
    }

    @Override
    public String reindexTaskList() {
        try {
            ListRequest request = ListRequest.of(builder ->
                builder.detailed(true).waitForCompletion(true)
            );
            ListResponse response = elasticsearchClient.tasks().list(request);
            return response.toString();
        } catch (IOException e) {
            throw new EsException("reindexTaskList exception:", e);
        }
    }

    @Override
    public EsPlusGetTaskResponse reindexTaskGet(String taskId) {
        try {
            GetTasksRequest request = GetTasksRequest.of(builder -> builder.taskId(taskId));
            GetTasksResponse response = elasticsearchClient.tasks().get(request);

            if (response.completed() ) {
                EsPlusGetTaskResponse res = new EsPlusGetTaskResponse();
                if (response.task() != null) {
                    res.setTaskInfo(response.task().toString());
                }
                res.setCompleted(response.completed());
                return res;
            }
        } catch (IOException e) {
            throw new EsException("reindexTaskGet exception:", e);
        }
        return null;
    }

    @Override
    public Boolean cancelTask(String taskId) {
        try {
            CancelRequest request = CancelRequest.of(builder -> builder.taskId(taskId));
            CancelResponse response = elasticsearchClient.tasks().cancel(request);
            return !response.nodeFailures().isEmpty() || !response.taskFailures().isEmpty();
        } catch (IOException e) {
            throw new EsException("cancelTask exception:", e);
        }
    }

    @Override
    public boolean reindex(String oldIndexName, String reindexName, Map<String, Object> changeMapping) {
        boolean exists = indexExists(reindexName);
        if (!exists) {
            EsIndexResponse indexResponse = getIndex(oldIndexName);
            Map<String, Object> mappings = indexResponse.getMappings(oldIndexName);

            Map<String, Object> properties = (Map<String, Object>) mappings.get(PROPERTIES);
            properties.putAll(changeMapping);
            boolean index = createIndex(reindexName);
            putMapping(reindexName, mappings);
        }
        return reindex(oldIndexName, reindexName);
    }

    @Override
    public boolean updateSettings(EsSettings esSettings, String... indices) {
        try {
            Map<String, JsonData> settingsMap = convertEsSettingsToMap(esSettings);

            PutIndicesSettingsRequest request = PutIndicesSettingsRequest.of(builder ->
                builder.index(Arrays.asList(indices)).settings(s -> s.otherSettings(settingsMap))
            );

            PutIndicesSettingsResponse response = elasticsearchClient.indices().putSettings(request);
            return response.acknowledged();
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public boolean updateSettings(Map<String, Object> esSettings, String... indices) {
        try {
            Map<String, JsonData> settingsMap = convertSettingsToMap(esSettings);

            PutIndicesSettingsRequest request = PutIndicesSettingsRequest.of(builder ->
                builder.index(Arrays.asList(indices)).settings(s -> s.otherSettings(settingsMap))
            );

            PutIndicesSettingsResponse response = elasticsearchClient.indices().putSettings(request);
            return response.acknowledged();
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public boolean ping() {
        try {
            BooleanResponse response = elasticsearchClient.ping();
            return response.value();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void createAlias(String currentIndex, String alias) {
        try {
            List<co.elastic.clients.elasticsearch.indices.update_aliases.Action> actions = Arrays.asList(
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.add(a -> a.index(currentIndex).alias(alias))
                )
            );

            UpdateAliasesRequest request = UpdateAliasesRequest.of(builder ->
                builder.actions(actions)
            );
            elasticsearchClient.indices().updateAliases(request);
        } catch (IOException e) {
            throw new EsException("createAlias exception", e);
        }
    }

    @Override
    public void removeAlias(String index, String alias) {
        try {
            List<co.elastic.clients.elasticsearch.indices.update_aliases.Action> actions = Arrays.asList(
                co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    action -> action.remove(r -> r.index(index).alias(alias))
                )
            );

            UpdateAliasesRequest request = UpdateAliasesRequest.of(builder ->
                builder.actions(actions)
            );
            elasticsearchClient.indices().updateAliases(request);
        } catch (IOException e) {
            throw new EsException("removeAlias exception", e);
        }
    }

    @Override
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... indices) {
        try {
            ForcemergeRequest request = ForcemergeRequest.of(builder ->
                builder.index(Arrays.asList(indices))
                       .maxNumSegments((long) maxSegments)
                       .onlyExpungeDeletes(onlyExpungeDeletes)
                       .flush(flush)
            );
            ForcemergeResponse response = elasticsearchClient.indices().forcemerge(request);
            return response.shards().successful().intValue() > 0;
        } catch (IOException e) {
            throw new EsException("forceMerge exception", e);
        }
    }

    @Override
    public boolean refresh(String... indices) {
        try {
            RefreshRequest request = RefreshRequest.of(builder -> builder.index(Arrays.asList(indices)));
            RefreshResponse response = elasticsearchClient.indices().refresh(request);
            return response.shards().successful().intValue() == indices.length;
        } catch (IOException e) {
            throw new EsException("refresh exception", e);
        }
    }

    @Override
    public String getIndexStat() {
        // ES8没有直接的REST客户端，需要通过其他方式实现
        return null;
    }

    @Override
    public String getIndexHealth() {
        // ES8没有直接的REST客户端，需要通过其他方式实现
        return null;
    }

    @Override
    public String getNodes() {
        // ES8没有直接的REST客户端，需要通过其他方式实现
        return null;
    }

    @Override
    public String cmdGet(String cmd) {
        // ES8没有直接的REST客户端，需要通过其他方式实现
        throw new EsException("cmdGet not supported in ES8 client");
    }

    // 私有辅助方法

    private void createIndexRequest(String index, EsIndexParam esIndexParam) {
        try {
            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
            builder.index(index);

            if (esIndexParam != null) {
                EsSettings esSettings = esIndexParam.getEsSettings();
                if (esSettings != null) {
                    Map<String, JsonData> settingsMap = convertEsSettingsToMap(esSettings);
                    builder.settings(s -> s.otherSettings(settingsMap));
                }

                if (ArrayUtils.isNotEmpty(esIndexParam.getAlias())) {
                    Map<String, co.elastic.clients.elasticsearch.indices.Alias> aliases = new HashMap<>();
                    for (String alias : esIndexParam.getAlias()) {
                        aliases.put(alias, co.elastic.clients.elasticsearch.indices.Alias.of(a -> a));
                    }
                    builder.aliases(aliases);
                }
            }

            elasticsearchClient.indices().create(builder.build());
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    private boolean putMappingInternal(String index, Map<String, Object> mappingProperties) {
        try {
            PutMappingRequest request = PutMappingRequest.of(builder ->
                builder.index(index).properties(convertMappingsToProperties(mappingProperties))
            );
            printInfoLog("putMapping index={} info={}", index, JsonUtils.toJsonStr(mappingProperties));
            PutMappingResponse response = elasticsearchClient.indices().putMapping(request);
            return response.acknowledged();
        } catch (IOException e) {
            printErrorLog("putMapping:{}", e);
            return false;
        }
    }

    private void doCreateIndexMapping(String index, EsIndexParam esIndexParam) {
        boolean exists = this.indexExists(index);
        if (!exists) {
            try {
                CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
                builder.index(index);

                EsSettings esSettings = esIndexParam.getEsSettings();
                if (esSettings != null) {
                    Map<String, JsonData> settingsMap = convertEsSettingsToMap(esSettings);
                    builder.settings(s -> s.otherSettings(settingsMap));
                }

                if (ArrayUtils.isNotEmpty(esIndexParam.getAlias())) {
                    Map<String, co.elastic.clients.elasticsearch.indices.Alias> aliases = new HashMap<>();
                    for (String alias : esIndexParam.getAlias()) {
                        aliases.put(alias, co.elastic.clients.elasticsearch.indices.Alias.of(a -> a));
                    }
                    builder.aliases(aliases);
                }

                builder.mappings(TypeMapping.of(m -> m.properties(convertMappingsToProperties(esIndexParam.getMappings()))));

                printInfoLog("doCreateIndexMapping index={} mappings:{}", index, JsonUtils.toJsonStr(esIndexParam.getMappings()));
                elasticsearchClient.indices().create(builder.build());
            } catch (IOException e) {
                throw new EsException("elasticsearch mappingRequest error", e);
            }
        }
    }

    private Map<String, JsonData> convertEsSettingsToMap(EsSettings esSettings) {
        String json = JsonUtils.toJsonStr(esSettings);
        Map<String, Object> settingsMap = JsonUtils.toMap(json);
        return convertToJsonDataMap(settingsMap);
    }

    private Map<String, JsonData> convertSettingsToMap(Map<String, Object> settings) {
        return convertToJsonDataMap(settings);
    }

    private Map<String, JsonData> convertToJsonDataMap(Map<String, Object> map) {
        Map<String, JsonData> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, JsonData.of(v)));
        return result;
    }

    private Map<String, co.elastic.clients.elasticsearch._types.mapping.Property> convertMappingsToProperties(Map<String, Object> mappings) {
        Map<String, co.elastic.clients.elasticsearch._types.mapping.Property> properties = new HashMap<>();

        if (mappings.containsKey(PROPERTIES)) {
            Map<String, Object> propertiesMap = (Map<String, Object>) mappings.get(PROPERTIES);
            propertiesMap.forEach((k, v) -> {
                properties.put(k, convertToProperty((Map<String, Object>) v));
            });
        } else {
            mappings.forEach((k, v) -> {
                if (v instanceof Map) {
                    properties.put(k, convertToProperty((Map<String, Object>) v));
                }
            });
        }

        return properties;
    }

    private co.elastic.clients.elasticsearch._types.mapping.Property convertToProperty(Map<String, Object> propertyMap) {
        String type = (String) propertyMap.get("type");
        if (type == null) {
            type = "text"; // 默认类型
        }

        switch (type.toLowerCase()) {
            case "text":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.text(t -> t));
            case "keyword":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.keyword(k -> k));
            case "long":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.long_(l -> l));
            case "integer":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.integer(i -> i));
            case "short":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.short_(s -> s));
            case "byte":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.byte_(b -> b));
            case "double":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.double_(d -> d));
            case "float":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.float_(f -> f));
            case "date":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.date(d -> d));
            case "boolean":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.boolean_(b -> b));
            case "object":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.object(o -> o));
            case "nested":
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.nested(n -> n));
            default:
                return co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.text(t -> t));
        }
    }

    private Map<String, Object> convertTypeMapping(TypeMapping typeMapping) {
        // 简化实现，实际需要根据TypeMapping的具体结构来转换
        Map<String, Object> result = new HashMap<>();
        if (typeMapping.properties() != null) {
            result.put(PROPERTIES, new HashMap<>());
        }
        return result;
    }

    private void printInfoLog(String format, Object... params) {
        log.info("es-plus " + format, params);
    }

    private void printErrorLog(String format, Exception e) {
        log.error("es-plus " + format, e);
    }
}