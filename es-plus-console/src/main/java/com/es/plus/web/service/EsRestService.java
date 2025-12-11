package com.es.plus.web.service;

import com.es.plus.web.http.EsHttpClient;
import com.es.plus.web.http.EsUrlBuilder;
import com.es.plus.web.http.HttpException;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import com.es.plus.web.util.JsonUtils;
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
    private SqlToDslConverter sqlToDslConverter;
    
    @Autowired
    private EsHttpClient httpClient;
    
    @Autowired
    private EsClientMapper esClientMapper;
    
    @Autowired
    private EsResponseParser responseParser;
    
    /**
     * 打印响应日志（限制长度）
     */
    private void logResponse(String operation, String response) {
        if (response == null) {
            log.info("ES响应 [{}]: null", operation);
            return;
        }
        
        // 限制日志长度，避免日志过大
        int maxLength = 1000;
        if (response.length() > maxLength) {
            log.info("ES响应 [{}]: {}... (总长度: {})", operation, response.substring(0, maxLength), response.length());
        } else {
            log.info("ES响应 [{}]: {}", operation, response);
        }
    }
    
    /**
     * 搜索查询
     */
    public String search(String clientKey, String index, String dsl) {
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildSearchUrl(baseUrl, index);
            Map<String, String> headers = getAuthHeaders(clientKey);
            
            String response = httpClient.post(url, dsl, headers);
            logResponse("search", response);
            return response;
        } catch (HttpException e) {
            log.error("搜索失败: clientKey={}, index={}", clientKey, index, e);
            throw e;
        }
    }
    
    /**
     * SQL查询（先转DSL再执行）
     */
    public String searchBySql(String clientKey, String sql) {
        try {
            // 1. 从SQL中提取索引名
            String indexName = extractIndexFromSql(sql);
            if (indexName == null || indexName.isEmpty()) {
                throw new RuntimeException("无法从SQL中提取索引名称");
            }
            
            // 2. 将SQL转换为DSL
            String dslResponse = sql2Dsl(clientKey, sql);
            
            // 3. 解析DSL响应，提取查询部分
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> dslMap = mapper.readValue(dslResponse, Map.class);
            
            // 4. 如果没有 size 字段，设置默认值为 100
            if (!dslMap.containsKey("size")) {
                dslMap.put("size", 100);
                log.debug("SQL查询未指定LIMIT，设置默认size=100");
            }
            
            String dsl = mapper.writeValueAsString(dslMap);
            
            // 5. 使用DSL执行查询
            return search(clientKey, indexName, dsl);
        } catch (HttpException e) {
            log.error("SQL查询失败: clientKey={}, sql={}", clientKey, sql, e);
            throw e;
        } catch (Exception e) {
            log.error("SQL查询处理失败: clientKey={}, sql={}", clientKey, sql, e);
            throw new RuntimeException("SQL查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取查询执行计划（使用 ES 的 profile API）
     * 支持 SQL 和 DSL 两种输入
     */
    public String explainQuery(String clientKey, String query, String index) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String indexName;
            Map<String, Object> dslMap;
            
            // 判断是 SQL 还是 DSL
            if (isSqlQuery(query)) {
                // SQL 查询：先转换为 DSL
                indexName = extractIndexFromSql(query);
                if (indexName == null || indexName.isEmpty()) {
                    // 如果 SQL 中没有索引名，使用传入的参数
                    if (index != null && !index.isEmpty()) {
                        indexName = index;
                    } else {
                        throw new RuntimeException("无法从SQL中提取索引名称，且未提供 index 参数");
                    }
                }
                
                String dslResponse = sql2Dsl(clientKey, query);
                @SuppressWarnings("unchecked")
                Map<String, Object> tempMap = mapper.readValue(dslResponse, Map.class);
                dslMap = tempMap;
            } else {
                // DSL 查询：使用传入的索引名
                if (index == null || index.isEmpty()) {
                    throw new RuntimeException("DSL 执行计划需要指定 index 参数");
                }
                indexName = index;
                
                // 解析 DSL
                @SuppressWarnings("unchecked")
                Map<String, Object> tempMap = mapper.readValue(query, Map.class);
                dslMap = tempMap;
            }
            
            // 添加 profile 字段
            dslMap.put("profile", true);
            
            // 如果没有 size，设置为 0（只关注执行计划，不需要返回数据）
            if (!dslMap.containsKey("size")) {
                dslMap.put("size", 0);
            }
            
            String dsl = mapper.writeValueAsString(dslMap);
            log.info("执行计划 DSL: {}", dsl);
            
            // 执行查询
            return search(clientKey, indexName, dsl);
        } catch (HttpException e) {
            log.error("获取执行计划失败: clientKey={}, query={}, index={}", clientKey, query, index, e);
            throw e;
        } catch (Exception e) {
            log.error("解析执行计划查询失败: clientKey={}, query={}, index={}", clientKey, query, index, e);
            throw new RuntimeException("获取执行计划失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 判断是否为 SQL 查询
     */
    private boolean isSqlQuery(String query) {
        String trimmed = query.trim().toUpperCase();
        return trimmed.startsWith("SELECT") ||
                trimmed.startsWith("SHOW") ||
                trimmed.startsWith("DESCRIBE") ||
                trimmed.startsWith("DESC");
    }
    
    
    /**
     * SQL转DSL（使用自定义转换器）
     */
    public String sql2Dsl(String clientKey, String sql) {
        try {
            // 使用自定义的 SQL 转 DSL 转换器
            String dsl = sqlToDslConverter.convertSqlToDsl(sql);
            log.debug("SQL 转 DSL 成功: sql={}, dsl={}", sql, dsl);
            return dsl;
        } catch (Exception e) {
            log.error("SQL转DSL失败: clientKey={}, sql={}", clientKey, sql, e);
            throw new RuntimeException("SQL转DSL失败: " + e.getMessage(), e);
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
            logResponse("aggregations", response);
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
            
            String response = httpClient.put(url, document, headers);
            logResponse("save", response);
            return response;
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
            String response = httpClient.post(url, bulkBody, headers);
            logResponse("saveBatch", response);
            return response;
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
            
            String response = httpClient.delete(url, headers);
            logResponse("deleteById", response);
            return response;
        } catch (HttpException e) {
            log.error("删除文档失败: clientKey={}, index={}, id={}", clientKey, index, id, e);
            throw e;
        }
    }
    
    
    /**
     * 批量删除文档（带routing）
     */
    public String deleteByIds(String clientKey, String index, List<Map<String, Object>> datas) {
        if (CollectionUtils.isEmpty(datas)) {
            throw new IllegalArgumentException("数据列表不能为空");
        }
        
        try {
            String baseUrl = getBaseUrl(clientKey);
            String url = EsUrlBuilder.buildBulkUrl(baseUrl);
            Map<String, String> headers = getAuthHeaders(clientKey);
            headers.put("Content-Type", "application/x-ndjson");
            
            String bulkBody = buildBulkDeleteRequestBody(index, datas);
            String response = httpClient.post(url, bulkBody, headers);
            logResponse("deleteByIds", response);
            return response;
        } catch (HttpException e) {
            log.error("批量删除文档失败: clientKey={}, index={}, count={}", clientKey, index, datas.size(), e);
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
            String response = httpClient.post(url, updateBody, headers);
            logResponse("update", response);
            return response;
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
            
            String response = httpClient.put(url, mapping, headers);
            logResponse("createIndex", response);
            return response;
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
            
            String response = httpClient.post(url, "", headers);
            logResponse("refresh", response);
            return response;
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
            logResponse("ping", response);
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
            logResponse("pingWithConfig", response);
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
            
            String response = httpClient.get(url, headers);
            logResponse("getClusterHealth", response);
            return response;
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
            
            String response = httpClient.get(url, headers);
            logResponse("getNodes", response);
            return response;
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
            
            String response = httpClient.get(url, headers);
            logResponse("getCmd", response);
            return response;
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
            
            String response = httpClient.get(url, headers);
            logResponse("getAliases", response);
            return response;
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
            
            String response = httpClient.post(url, dsl, headers);
            logResponse("count", response);
            return response;
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
                Map<String, Object> settingsMap = JsonUtils.toMap(settings);
                // 过滤掉系统生成的不可设置字段
                settingsMap = filterSystemSettings(settingsMap);
                requestBody.put("settings", settingsMap);
            }
            if (mappings != null) {
                Map<String, Object> mappingsMap = JsonUtils.toMap(mappings);
                
                // 检查是否已经有外层的 "mappings" 包装，避免双层嵌套
                if (mappingsMap.containsKey("mappings")) {
                    // 如果有外层 mappings 包装，直接使用内层的 mappings
                    requestBody.put("mappings", mappingsMap.get("mappings"));
                    log.debug("检测到 mappings 已有外层包装，使用内层数据");
                } else {
                    // 如果没有，说明传入的就是 properties 等内容，需要包装
                    requestBody.put("mappings", mappingsMap);
                }
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
     * 过滤掉系统生成的不可设置字段
     */
    private Map<String, Object> filterSystemSettings(Map<String, Object> settings) {
        if (settings == null) {
            return settings;
        }
        
        // 需要过滤的系统生成字段（不带 index. 前缀）
        java.util.Set<String> excludedSettings = new java.util.HashSet<>(java.util.Arrays.asList(
                "creation_date",
                "uuid",
                "provided_name",
                "version",
                "version.created",
                "version.upgraded"
        ));
        
        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            // 跳过系统生成的字段
            if (!excludedSettings.contains(key)) {
                filtered.put(key, entry.getValue());
            } else {
                log.debug("过滤掉系统设置字段: {}", key);
            }
        }
        
        return filtered;
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
            
            // 处理mappings格式：如果包含外层的"mappings"包装，需要提取内部内容
            String processedMappings = mappings;
            try {
                Map<String, Object> mappingMap = JsonUtils.toMap(mappings);
                if (mappingMap.containsKey("mappings")) {
                    // 如果有外层mappings包装，提取内部内容
                    Object innerMappings = mappingMap.get("mappings");
                    processedMappings = JsonUtils.toJsonStr(innerMappings);
                }
            } catch (Exception e) {
                log.warn("解析mappings JSON失败，使用原始内容: {}", e.getMessage());
            }
            
            return httpClient.put(url, processedMappings, headers);
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
            // 使用 PUT /{index}/_alias/{alias} 方式，更简单直接
            String url = baseUrl + "/" + indexName + "/_alias/" + aliasName;
            Map<String, String> headers = getAuthHeaders(clientKey);
            
            // PUT 方式创建别名，可以传空body或者包含filter等配置
            return httpClient.put(url, "{}", headers);
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
            // 使用 /_cat/aliases?format=json 获取所有别名，然后过滤
            // 这样即使某些索引没有别名也不会报错
            String url = baseUrl + "/_cat/aliases?format=json";
            Map<String, String> headers = getAuthHeaders(clientKey);
            
            String response = httpClient.get(url, headers);
            
            // 如果响应为空或者是空数组，直接返回空的aliases
            if (response == null || response.trim().equals("[]") || response.trim().isEmpty()) {
                log.debug("没有找到任何别名信息");
                return aliases;
            }
            
            List<Map> aliasList = JsonUtils.toList(response, Map.class);
            
            // 创建索引名称集合用于快速查找
            java.util.Set<String> indexNameSet = new java.util.HashSet<>(java.util.Arrays.asList(indexNames));
            
            for (Map alias : aliasList) {
                String index = (String) alias.get("index");
                String aliasName = (String) alias.get("alias");
                
                // 只添加我们关心的索引的别名
                if (indexNameSet.contains(index)) {
                    aliases.computeIfAbsent(index, k -> new ArrayList<>()).add(aliasName);
                }
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
                log.debug("索引 {} 的 mapping 响应: {}", indexName, mappingResponse);
                
                Map<String, Object> mapping = JsonUtils.toMap(mappingResponse);
                
                // 尝试获取索引的 mapping，支持多种格式
                Map<String, Object> indexMapping = (Map<String, Object>) mapping.get(indexName);
                if (indexMapping == null) {
                    log.warn("索引 {} 的 mapping 中找不到索引名对应的数据，尝试直接解析", indexName);
                    // 如果直接用索引名获取不到，可能响应格式就是 mappings 本身
                    indexMapping = mapping;
                }
                
                // 尝试获取 mappings 字段（某些版本的 ES 可能有这一层）
                Object mappingsObj = indexMapping.get("mappings");
                if (mappingsObj instanceof Map) {
                    indexMapping = (Map<String, Object>) mappingsObj;
                    log.debug("从 mappings 字段中提取数据");
                }
                
                // 获取 properties
                Map<String, Object> properties = (Map<String, Object>) indexMapping.get("properties");
                if (properties == null) {
                    log.warn("索引 {} 的 mapping 中找不到 properties 字段，跳过", indexName);
                    continue;
                }
                
                Map<String, String> fieldTypes = new HashMap<>();
                properties.forEach((field, fieldInfo) -> {
                    if (fieldInfo instanceof Map) {
                        Map<String, Object> fieldMap = (Map<String, Object>) fieldInfo;
                        String type = (String) fieldMap.get("type");
                        if (type != null) {
                            fieldTypes.put(field, type);
                        }
                        
                        // 处理fields字段（如text字段的keyword子字段）
                        Object fieldsObj = fieldMap.get("fields");
                        if (fieldsObj instanceof Map) {
                            Map<String, Object> fields = (Map<String, Object>) fieldsObj;
                            fields.forEach((subField, subInfo) -> {
                                if (subInfo instanceof Map) {
                                    Map<String, Object> subMap = (Map<String, Object>) subInfo;
                                    String subType = (String) subMap.get("type");
                                    if (subType != null) {
                                        fieldTypes.put(field + "." + subField, subType);
                                    }
                                }
                            });
                        }
                    }
                });
                
                if (!fieldTypes.isEmpty()) {
                    flatMappings.put(indexName, fieldTypes);
                    log.debug("索引 {} 成功构建 flatMappings，包含 {} 个字段", indexName, fieldTypes.size());
                } else {
                    log.warn("索引 {} 的 flatMappings 为空", indexName);
                }
            } catch (Exception e) {
                log.error("构建索引{}的flatMappings失败", indexName, e);
            }
        }
        
        log.debug("最终构建的 flatMappings 包含 {} 个索引", flatMappings.size());
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
     * 从SQL语句中提取索引名称
     */
    private String extractIndexFromSql(String sql) {
        try {
            // 匹配 FROM 子句后的索引名
            // 支持格式：SELECT ... FROM index_name ...
            // 也支持：FROM index_name, index_name2 （逗号分隔多个索引）
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "(?i)FROM\\s+([\\w\\-\\*,\\.]+)",
                    java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = pattern.matcher(sql);
            
            if (matcher.find()) {
                String indexPart = matcher.group(1).trim();
                // 如果有多个索引（逗号分隔），取第一个或返回完整的索引列表
                // ES支持多索引查询，例如：index1,index2
                return indexPart;
            }
            
            log.warn("无法从SQL中提取索引名称: {}", sql);
            return null;
        } catch (Exception e) {
            log.error("从SQL中提取索引名失败: {}", sql, e);
            return null;
        }
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
            
            // 从文档中获取 routing
            Object routing = doc.get("_routing");
            
            // 索引操作头
            bulkBody.append("{\"index\":{\"_index\":\"").append(index).append("\",\"_id\":\"").append(docId).append("\"");
            
            // 如果有routing且不为空，添加到操作头中
            if (routing != null && !routing.toString().isEmpty()) {
                bulkBody.append(",\"routing\":\"").append(routing).append("\"");
            }
            
            bulkBody.append("}}\n");
            
            // 文档内容（移除 _id 和 _routing 元数据字段）
            try {
                Map<String, Object> docCopy = new HashMap<>(doc);
                docCopy.remove("_id");
                docCopy.remove("_routing");
                String docJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(docCopy);
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
    private String buildBulkDeleteRequestBody(String index, List<Map<String, Object>> datas) {
        StringBuilder bulkBody = new StringBuilder();
        
        for (Map<String, Object> data : datas) {
            Object id = data.get("_id");
            if (id == null) {
                log.warn("删除操作缺少 _id，跳过该文档");
                continue;
            }
            
            // 从数据中获取 routing
            Object routing = data.get("_routing");
            
            // 删除操作头
            bulkBody.append("{\"delete\":{\"_index\":\"").append(index).append("\",\"_id\":\"").append(id).append("\"");
            
            // 如果有routing且不为空，添加到操作头中
            if (routing != null && !routing.toString().isEmpty()) {
                bulkBody.append(",\"routing\":\"").append(routing).append("\"");
            }
            
            bulkBody.append("}}\n");
        }
        
        return bulkBody.toString();
    }
    
    /**
     * 构建 delete_by_query 请求体（根据ID列表删除，无需routing）
     */
    private String buildDeleteByQueryRequestBody(List<String> ids) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> terms = new HashMap<>();
        
        terms.put("_id", ids);
        query.put("terms", terms);
        requestBody.put("query", query);
        
        return cn.hutool.json.JSONUtil.toJsonStr(requestBody);
    }
    
    /**
     * 构建 update_by_query 请求体（根据ID更新单个文档，无需routing）
     */
    private String buildUpdateByQueryRequestBody(String id, Map<String, Object> doc) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 构建查询条件：匹配指定的 _id
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> term = new HashMap<>();
        term.put("_id", id);
        query.put("term", term);
        requestBody.put("query", query);
        
        // 构建脚本：更新文档的所有字段
        Map<String, Object> script = new HashMap<>();
        StringBuilder scriptSource = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        
        // 移除 _id 字段，不需要更新
        Map<String, Object> docCopy = new HashMap<>(doc);
        docCopy.remove("_id");
        
        // 为每个字段生成更新语句
        int fieldIndex = 0;
        for (Map.Entry<String, Object> entry : docCopy.entrySet()) {
            String fieldName = entry.getKey();
            String paramName = "param" + fieldIndex;
            
            if (scriptSource.length() > 0) {
                scriptSource.append("; ");
            }
            scriptSource.append("ctx._source['").append(fieldName).append("'] = params.").append(paramName);
            
            params.put(paramName, entry.getValue());
            fieldIndex++;
        }
        
        script.put("source", scriptSource.toString());
        script.put("lang", "painless");
        script.put("params", params);
        requestBody.put("script", script);
        
        return cn.hutool.json.JSONUtil.toJsonStr(requestBody);
    }
}