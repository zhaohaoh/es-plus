package com.es.plus.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.lock.ELockClient;
import com.es.plus.common.pojo.es.EpScript;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ES8分布式锁客户端
 *
 * @author hzh
 * @date 2024/09/19
 */
public class Es8LockClient implements ELockClient {

    private final ElasticsearchClient elasticsearchClient;

    public Es8LockClient(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public boolean updateByScript(String index, String id, EpScript painless) {
        try {
            UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(builder ->
                builder.index(index)
                       .id(id)
                       .retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries())
                       .script(convertToEs8Script(painless))
                       .timeout(t -> t.time("10s"))
            );

            UpdateResponse<Object> response = elasticsearchClient.update(updateRequest, Object.class);
            return response.result() != Result.NoOp;
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public boolean upsertByScript(String index, String id, Map<String, Object> insertBody, EpScript painless) {
        try {
            UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(builder ->
                builder.index(index)
                       .id(id)
                       .retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries())
                       .script(convertToEs8Script(painless))
                       .upsert(JsonData.of(insertBody))
                       .timeout(t -> t.time("10s"))
            );

            UpdateResponse<Object> response = elasticsearchClient.update(updateRequest, Object.class);
            return response.result() != Result.NoOp;
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public String update(String index, Object esData) {
        try {
            String docId = GlobalParamHolder.getDocId(index, esData);
            String refreshPolicy = GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy().name().toLowerCase();

            Refresh refresh;
            switch (refreshPolicy) {
                case "immediate":
                    refresh = Refresh.True;
                    break;
                case "wait_until":
                    refresh = Refresh.WaitFor;
                    break;
                default:
                    refresh = Refresh.False;
                    break;
            }

            UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(builder ->
                builder.index(index)
                       .id(docId)
                       .doc(JsonData.of(JsonUtils.toJsonStr(esData)))
                       .retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries())
                       .refresh(refresh)
                       .timeout(t -> t.time("10s"))
            );

            UpdateResponse<Object> response = elasticsearchClient.update(updateRequest, Object.class);
            return response.toString();
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public Map<String, Object> search(String index, String key, String value) {
        try {
            SearchRequest searchRequest = SearchRequest.of(builder ->
                builder.index(index)
                       .query(q -> q.term(t -> t.field(key).value(v -> v.stringValue(value))))
            );

            SearchResponse<Object> response = elasticsearchClient.search(searchRequest, Object.class);

            if (response.hits() != null && response.hits().hits() != null && !response.hits().hits().isEmpty()) {
                Hit<Object> hit = response.hits().hits().get(0);
                if (hit.source() != null) {
                    // 将hit.source()转换为Map
                    String sourceJson = JsonUtils.toJsonStr(hit.source());
                    return JsonUtils.toMap(sourceJson );
                }
            }
        } catch (IOException e) {
            throw new EsException(e);
        }
        return new HashMap<>();
    }

    /**
     * 将EpScript转换为ES8的Script
     *
     * ES8 Script参数说明：
     * - source: 内联脚本的源代码
     * - id: 存储脚本的ID
     * - lang: 脚本语言（如painless、expression等）
     * - params: 脚本参数Map
     */
    private Script convertToEs8Script(EpScript epScript) {
        if (epScript == null) {
            return null;
        }

        if (epScript.getScriptType() == EpScript.ScriptType.STORED) {
            // 存储脚本：使用脚本ID引用预存储的脚本
            return Script.of(builder -> {
                builder.id(epScript.getScript()); // 脚本ID

                // 设置参数
                if (epScript.getParams() != null && !epScript.getParams().isEmpty()) {
                    Map<String, JsonData> params = new HashMap<>();
                    epScript.getParams().forEach((k, v) -> params.put(k, JsonData.of(v)));
                    builder.params(params);
                }

                return builder;
            });
        } else {
            // 内联脚本：直接包含脚本源代码
            return Script.of(builder -> {
                builder.source(epScript.getScript()); // 脚本源代码

                // 设置脚本语言
                if (epScript.getLang() != null) {
                    builder.lang(epScript.getLang());
                }

                // 设置参数
                if (epScript.getParams() != null && !epScript.getParams().isEmpty()) {
                    Map<String, JsonData> params = new HashMap<>();
                    epScript.getParams().forEach((k, v) -> params.put(k, JsonData.of(v)));
                    builder.params(params);
                }

                return builder;
            });
        }
    }
}