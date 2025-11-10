package com.es.plus.web.service;

import com.es.plus.web.util.JsonUtils;
import com.es.plus.web.http.EsHttpClient;
import com.es.plus.web.http.EsUrlBuilder;
import com.es.plus.web.http.HttpException;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch REST服务类
 * 封装各种ES操作的HTTP调用
 */
@Slf4j
@Service
public class EsRestService {

    @Autowired
    private EsHttpClient httpClient;

    @Autowired
    private EsClientMapper esClientMapper;

    @Autowired
    private EsResponseParser responseParser;

    /**
     * 搜索查询
     */
    public String search(String clientKey, String index, String dsl) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSearchUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String response = httpClient.post(url, dsl, headers);
            return response;
        } catch (HttpException e) {
            log.error("搜索失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * SQL查询
     */
    public String searchBySql(String clientKey, String sql) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSqlUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String requestBody = buildSqlRequestBody(sql);
            String response = httpClient.post(url, requestBody, headers);
            return responseParser.parseSqlResponse(response);
        } catch (HttpException e) {
            log.error("SQL查询失败: clientKey={}, sql={}", clientKey, sql, e);
            throw e;
        }
    }

    /**
     * SQL转DSL
     */
    public String sql2Dsl(String clientKey, String sql) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSql2DslUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String requestBody = buildSqlRequestBody(sql);
            String response = httpClient.post(url, requestBody, headers);
            return response;
        } catch (HttpException e) {
            log.error("SQL转DSL失败: clientKey={}, sql={}", clientKey, sql, e);
            throw e;
        }
    }

    /**
     * 聚合查询
     */
    public String aggregations(String clientKey, String index, String dsl) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSearchUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String response = httpClient.post(url, dsl, headers);
            return responseParser.parseAggregationsResponse(response);
        } catch (HttpException e) {
            log.error("聚合查询失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * 保存文档
     */
    public String save(String clientKey, String index, String id, String document) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildDocumentUrl(baseUrl, index, id);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.put(url, document, headers);
        } catch (HttpException e) {
            log.error("保存文档失败: clientKey={}, index={}, id={}", clientKey, index, id, e);
            throw e;
        }
    }

    /**
     * 批量保存文档
     */
    public String saveBatch(String clientKey, String index, List<Map<String, Object>> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            throw new IllegalArgumentException("文档列表不能为空");
        }

        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildBulkUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String bulkBody = buildBulkRequestBody(index, documents);
            return httpClient.post(url, bulkBody, headers);
        } catch (HttpException e) {
            log.error("批量保存文档失败: clientKey={}, index={}, count={}", clientKey, index, documents.size(), e);
            throw e;
        }
    }

    /**
     * 根据ID删除文档
     */
    public String deleteById(String clientKey, String index, String id) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildDocumentUrl(baseUrl, index, id);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.delete(url, headers);
        } catch (HttpException e) {
            log.error("删除文档失败: clientKey={}, index={}, id={}", clientKey, index, id, e);
            throw e;
        }
    }

    /**
     * 批量删除文档
     */
    public String deleteByIds(String clientKey, String index, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("ID列表不能为空");
        }

        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildBulkUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String bulkBody = buildBulkDeleteRequestBody(index, ids);
            return httpClient.post(url, bulkBody, headers);
        } catch (HttpException e) {
            log.error("批量删除文档失败: clientKey={}, index={}, count={}", clientKey, index, ids.size(), e);
            throw e;
        }
    }

    /**
     * 更新文档
     */
    public String update(String clientKey, String index, String id, String document) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildDocumentUrl(baseUrl, index, id) + "/_update";
            Map<String, String> headers = getAuthHeaders(clientKey);

            String updateBody = "{\"doc\":" + document + "}";
            return httpClient.post(url, updateBody, headers);
        } catch (HttpException e) {
            log.error("更新文档失败: clientKey={}, index={}, id={}", clientKey, index, id, e);
            throw e;
        }
    }

    /**
     * 创建索引
     */
    public String createIndex(String clientKey, String index, String mapping) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildIndexUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.put(url, mapping, headers);
        } catch (HttpException e) {
            log.error("创建索引失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }


    /**
     * 刷新索引
     */
    public String refresh(String clientKey, String index) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildRefreshUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.post(url, "", headers);
        } catch (HttpException e) {
            log.error("刷新索引失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * Ping测试连接
     */
    public boolean ping(String clientKey) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            Map<String, String> headers = getAuthHeaders(clientKey);

            String response = httpClient.get(baseUrl, headers);
            return response.contains("\"tagline\"") && response.contains("\"You Know, for Search\"");
        } catch (Exception e) {
            log.error("Ping失败: clientKey={}", clientKey, e);
            return false;
        }
    }

    /**
     * 直接使用客户端配置测试连接
     */
    public boolean pingWithConfig(EsClientProperties clientConfig) {
        try {
            String baseUrl = clientConfig.getAddress();
            Map<String, String> headers = buildAuthHeadersFromConfig(clientConfig);

            String response = httpClient.get(baseUrl, headers);
            return response.contains("\"tagline\"") && response.contains("\"You Know, for Search\"");
        } catch (Exception e) {
            log.error("Ping失败: address={}", clientConfig.getAddress(), e);
            return false;
        }
    }

    /**
     * 从客户端配置构建认证请求头
     */
    private Map<String, String> buildAuthHeadersFromConfig(EsClientProperties clientConfig) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        if (clientConfig.getUsername() != null && clientConfig.getPassword() != null) {
            String auth = clientConfig.getUsername() + ":" + clientConfig.getPassword();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            headers.put("Authorization", "Basic " + encodedAuth);
        }

        return headers;
    }

    /**
     * 获取集群健康状态
     */
    public String getClusterHealth(String clientKey) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildClusterHealthUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取集群健康状态失败: clientKey={}", clientKey, e);
            throw e;
        }
    }

    /**
     * 获取集群节点信息
     */
    public String getNodes(String clientKey) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildClusterNodesUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取集群节点信息失败: clientKey={}", clientKey, e);
            throw e;
        }
    }

    /**
     * 执行自定义ES命令
     */
    public String getCmd(String clientKey, String cmd) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + cmd;
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("执行ES命令失败: clientKey={}, cmd={}", clientKey, cmd, e);
            throw e;
        }
    }

    /**
     * 获取索引别名信息
     */
    public String getAliases(String clientKey, String indices) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_cat/aliases/" + indices + "?format=json";
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取索引别名信息失败: clientKey={}, indices={}", clientKey, indices, e);
            throw e;
        }
    }

    /**
     * 计数查询
     */
    public String count(String clientKey, String index, String dsl) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildCountUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.post(url, dsl, headers);
        } catch (HttpException e) {
            log.error("计数查询失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    // ==================== 索引管理 API ====================

    /**
     * 获取索引列表（带格式化响应）
     */
    public String getIndices(String clientKey, String pattern) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_cat/indices/" + pattern + "?format=json";
            Map<String, String> headers = getAuthHeaders(clientKey);

            String response = httpClient.get(url, headers);
            return buildIndexListResponse(response, clientKey);
        } catch (HttpException e) {
            log.error("获取索引列表失败: clientKey={}, pattern={}", clientKey, pattern, e);
            throw e;
        }
    }

    /**
     * 获取单个索引详细信息
     */
    public String getIndex(String clientKey, String index) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildIndexUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取索引信息失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * 获取索引统计信息
     */
    public String getIndexStats(String clientKey, String index) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_cat/indices/" + index + "?format=json&v";
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取索引统计失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * 获取索引健康状态
     */
    public String getIndexHealth(String clientKey) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_cluster/health?level=indices";
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取索引健康状态失败: clientKey={}", clientKey, e);
            throw e;
        }
    }

    /**
     * 创建索引
     */
    public String createIndex(String clientKey, String indexName, String alias, String settings, String mappings) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildIndexUrl(baseUrl, indexName);
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            if (settings != null) {
                requestBody.put("settings", JsonUtils.toMap(settings));
            }
            if (mappings != null) {
                requestBody.put("mappings", JsonUtils.toMap(mappings));
            }
            if (alias != null) {
                Map<String, Object> aliases = new HashMap<>();
                Map<String, Object> aliasMap = new HashMap<>();
                aliases.put(alias, aliasMap);
                requestBody.put("aliases", aliases);
            }

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.put(url, body, headers);
        } catch (HttpException e) {
            log.error("创建索引失败: clientKey={}, index={}", clientKey, indexName, e);
            throw e;
        }
    }

    /**
     * 删除索引
     */
    public String deleteIndex(String clientKey, String indexName) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildIndexUrl(baseUrl, indexName);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.delete(url, headers);
        } catch (HttpException e) {
            log.error("删除索引失败: clientKey={}, index={}", clientKey, indexName, e);
            throw e;
        }
    }

    // ==================== 映射和别名管理 API ====================

    /**
     * 获取索引映射
     */
    public String getMapping(String clientKey, String indexName) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildMappingUrl(baseUrl, indexName);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取索引映射失败: clientKey={}, index={}", clientKey, indexName, e);
            throw e;
        }
    }

    /**
     * 更新索引映射
     */
    public String putMapping(String clientKey, String indexName, String mappings) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildMappingUrl(baseUrl, indexName);
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.put(url, mappings, headers);
        } catch (HttpException e) {
            log.error("更新索引映射失败: clientKey={}, index={}", clientKey, indexName, e);
            throw e;
        }
    }

    /**
     * 创建别名
     */
    public String createAlias(String clientKey, String indexName, String aliasName) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildAliasUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> actions = new HashMap<>();
            Map<String, Object> addAlias = new HashMap<>();
            addAlias.put("index", indexName);
            addAlias.put("alias", aliasName);
            actions.put("add", addAlias);
            requestBody.put("actions", new Object[]{actions});

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.post(url, body, headers);
        } catch (HttpException e) {
            log.error("创建别名失败: clientKey={}, index={}, alias={}", clientKey, indexName, aliasName, e);
            throw e;
        }
    }

    /**
     * 删除别名
     */
    public String removeAlias(String clientKey, String indexName, String aliasName) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildAliasUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> actions = new HashMap<>();
            Map<String, Object> removeAlias = new HashMap<>();
            removeAlias.put("index", indexName);
            removeAlias.put("alias", aliasName);
            actions.put("remove", removeAlias);
            requestBody.put("actions", new Object[]{actions});

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.post(url, body, headers);
        } catch (HttpException e) {
            log.error("删除别名失败: clientKey={}, index={}, alias={}", clientKey, indexName, aliasName, e);
            throw e;
        }
    }

    /**
     * 更新索引设置
     */
    public String updateSettings(String clientKey, String indexName, String settings) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildIndexUrl(baseUrl, indexName) + "/_settings";
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.put(url, settings, headers);
        } catch (HttpException e) {
            log.error("更新索引设置失败: clientKey={}, index={}", clientKey, indexName, e);
            throw e;
        }
    }

    // ==================== Reindex和数据迁移 API ====================

    /**
     * 执行reindex操作
     */
    public String reindex(String clientKey, String sourceIndex, String targetIndex) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_reindex";
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> source = new HashMap<>();
            source.put("index", sourceIndex);
            Map<String, Object> dest = new HashMap<>();
            dest.put("index", targetIndex);
            requestBody.put("source", source);
            requestBody.put("dest", dest);
            requestBody.put("conflicts", "proceed");

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            String response = httpClient.post(url, body, headers);

            // 从响应中提取taskId
            return extractTaskId(response);
        } catch (HttpException e) {
            log.error("reindex失败: clientKey={}, source={}, target={}", clientKey, sourceIndex, targetIndex, e);
            throw e;
        }
    }

    /**
     * 获取任务状态
     */
    public String getTaskStatus(String clientKey, String taskId) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_tasks/" + taskId;
            Map<String, String> headers = getAuthHeaders(clientKey);

            return httpClient.get(url, headers);
        } catch (HttpException e) {
            log.error("获取任务状态失败: clientKey={}, taskId={}", clientKey, taskId, e);
            throw e;
        }
    }

    /**
     * 开始scroll查询
     */
    public String startScroll(String clientKey, String index, int size) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSearchUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("size", size);
            requestBody.put("sort", new Object[]{"_doc"});
            requestBody.put("track_total_hits", true);

            Map<String, Object> scrollParams = new HashMap<>();
            scrollParams.put("scroll", "1m");
            requestBody.putAll(scrollParams);

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.post(url, body, headers);
        } catch (HttpException e) {
            log.error("开始scroll查询失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }

    /**
     * 继续scroll查询
     */
    public String continueScroll(String clientKey, String scrollId) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_search/scroll";
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("scroll", "1m");
            requestBody.put("scroll_id", scrollId);

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.post(url, body, headers);
        } catch (HttpException e) {
            log.error("继续scroll查询失败: clientKey={}, scrollId={}", clientKey, scrollId, e);
            throw e;
        }
    }

    /**
     * 清理scroll上下文
     */
    public String clearScroll(String clientKey, String scrollId) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = baseUrl + "/_search/scroll";
            Map<String, String> headers = getAuthHeaders(clientKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("scroll_id", new String[]{scrollId});

            String body = cn.hutool.json.JSONUtil.toJsonStr(requestBody);
            return httpClient.delete(url, body, headers);
        } catch (HttpException e) {
            log.error("清理scroll上下文失败: clientKey={}, scrollId={}", clientKey, scrollId, e);
            throw e;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建索引列表响应（与原来格式一致）
     */
    private String buildIndexListResponse(String esResponse, String clientKey) {
        try {
            // 解析ES API响应
            List<Map> indices = JsonUtils.toList(esResponse, Map.class);

            // 提取索引名称数组
            String[] indexNames = indices.stream()
                    .map(map -> (String) map.get("index"))
                    .toArray(String[]::new);

            // 获取别名信息
            Map<String, List<String>> aliases = getAliasesForIndices(clientKey, indexNames);

            // 构建flatMappings（字段类型映射）
            Map<String, Map<String, String>> flatMappings = buildFlatMappings(clientKey, indexNames);

            // 组装最终响应（与原来EsIndexResponseVO格式一致）
            Map<String, Object> result = new HashMap<>();
            result.put("indices", indexNames);
            result.put("flatMappings", flatMappings);
            result.put("aliases", aliases);

            return JsonUtils.toJsonStr(result);
        } catch (Exception e) {
            log.error("构建索引列表响应失败", e);
            return esResponse; // 降级返回原始响应
        }
    }

    /**
     * 获取索引别名信息
     */
    private Map<String, List<String>> getAliasesForIndices(String clientKey, String[] indexNames) {
        Map<String, List<String>> aliases = new HashMap<>();

        try {
            String baseUrl = getBaseUrl(clientKey);
            String indicesPattern = String.join(",", indexNames);
            String url = baseUrl + "/_cat/aliases/" + indicesPattern + "?format=json";
            Map<String, String> headers = getAuthHeaders(clientKey);

            String response = httpClient.get(url, headers);
            List<Map> aliasList = JsonUtils.toList(response, Map.class);

            for (Map alias : aliasList) {
                String index = (String) alias.get("index");
                String aliasName = (String) alias.get("alias");

                aliases.computeIfAbsent(index, k -> new ArrayList<>()).add(aliasName);
            }
        } catch (Exception e) {
            log.error("获取别名信息失败", e);
        }

        return aliases;
    }

    /**
     * 构建flatMappings（字段类型映射）
     */
    private Map<String, Map<String, String>> buildFlatMappings(String clientKey, String[] indexNames) {
        Map<String, Map<String, String>> flatMappings = new HashMap<>();

        for (String indexName : indexNames) {
            try {
                String mappingResponse = getMapping(clientKey, indexName);
                Map<String, Object> mapping = JsonUtils.toMap(mappingResponse);

                Map<String, Object> indexMapping = (Map<String, Object>) mapping.get(indexName);
                if (indexMapping == null) {
                    continue;
                }

                Map<String, Object> properties = (Map<String, Object>) indexMapping.get("properties");
                if (properties == null) {
                    continue;
                }

                Map<String, String> fieldTypes = new HashMap<>();
                properties.forEach((field, fieldInfo) -> {
                    Map<String, Object> fieldMap = (Map<String, Object>) fieldInfo;
                    String type = (String) fieldMap.get("type");
                    fieldTypes.put(field, type);

                    // 处理fields字段（如text字段的keyword子字段）
                    Object fieldsObj = fieldMap.get("fields");
                    if (fieldsObj instanceof Map) {
                        Map<String, Object> fields = (Map<String, Object>) fieldsObj;
                        fields.forEach((subField, subInfo) -> {
                            Map<String, Object> subMap = (Map<String, Object>) subInfo;
                            String subType = (String) subMap.get("type");
                            fieldTypes.put(field + "." + subField, subType);
                        });
                    }
                });

                flatMappings.put(indexName, fieldTypes);
            } catch (Exception e) {
                log.error("构建索引{}的flatMappings失败", indexName, e);
            }
        }

        return flatMappings;
    }

    /**
     * 从reindex响应中提取taskId
     */
    private String extractTaskId(String reindexResponse) {
        try {
            Map<String, Object> response = JsonUtils.toMap(reindexResponse);
            return (String) response.get("task");
        } catch (Exception e) {
            log.error("提取taskId失败", e);
            return null;
        }
    }

    /**
     * 获取ES客户端配置
     */
    private EsClientProperties getClientConfig(String clientKey) {
        EsClientProperties clientConfig = esClientMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EsClientProperties>()
                        .eq(EsClientProperties::getUnikey, clientKey)
        );

        if (clientConfig == null) {
            throw new RuntimeException("未找到ES客户端配置: " + clientKey);
        }

        return clientConfig;
    }

    /**
     * 获取基础URL
     */
    private String getBaseUrl(String clientKey) {
        EsClientProperties clientConfig = getClientConfig(clientKey);
        return clientConfig.getAddress();
    }

    /**
     * 获取认证请求头
     */
    private Map<String, String> getAuthHeaders(String clientKey) {
        EsClientProperties clientConfig = getClientConfig(clientKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        if (clientConfig.getUsername() != null && clientConfig.getPassword() != null) {
            String auth = clientConfig.getUsername() + ":" + clientConfig.getPassword();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            headers.put("Authorization", "Basic " + encodedAuth);
        }

        return headers;
    }

    /**
     * 构建SQL请求体
     */
    private String buildSqlRequestBody(String sql) {
        return String.format("{\"query\":\"%s\",\"fetch_size\":1000}", sql.replace("\"", "\\\""));
    }

    /**
     * 构建批量操作请求体（保存）
     */
    private String buildBulkRequestBody(String index, List<Map<String, Object>> documents) {
        StringBuilder bulkBody = new StringBuilder();

        for (Map<String, Object> doc : documents) {
            Object docId = doc.get("_id");
            if (docId == null) {
                docId = java.util.UUID.randomUUID().toString();
            }

            // 索引操作头
            bulkBody.append("{\"index\":{\"_index\":\"").append(index).append("\",\"_id\":\"").append(docId).append("\"}}\n");
            // 文档内容
            try {
                String docJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(doc);
                bulkBody.append(docJson).append("\n");
            } catch (Exception e) {
                log.error("序列化文档失败: {}", doc, e);
            }
        }

        return bulkBody.toString();
    }

    /**
     * 构建批量操作请求体（删除）
     */
    private String buildBulkDeleteRequestBody(String index, List<String> ids) {
        StringBuilder bulkBody = new StringBuilder();

        for (String id : ids) {
            // 删除操作头
            bulkBody.append("{\"delete\":{\"_index\":\"").append(index).append("\",\"_id\":\"").append(id).append("\"}}\n");
        }

        return bulkBody.toString();
    }
}