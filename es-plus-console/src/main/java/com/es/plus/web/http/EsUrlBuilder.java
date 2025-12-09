package com.es.plus.web.http;

import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch URL构建工具类
 * 用于构建各种ES REST API的URL
 */
@Component
public class EsUrlBuilder {
    
    @Autowired
    private EsClientMapper esClientMapper;
    
    /**
     * 构建搜索API URL
     */
    public static String buildSearchUrl(String baseUrl, String index) {
        return baseUrl + "/" + index + "/_search";
    }
    
    /**
     * 构建SQL查询API URL
     */
    public static String buildSqlUrl(String baseUrl) {
        return baseUrl + "/_sql";
    }
    
    /**
     * 构建SQL转DSL API URL
     */
    public static String buildSql2DslUrl(String baseUrl) {
        return baseUrl + "/_sql/translate";
    }
    
    /**
     * 构建索引API URL
     */
    public static String buildIndexUrl(String baseUrl, String index) {
        return baseUrl + "/" + index;
    }
    
    /**
     * 构建索引映射API URL
     */
    public static String buildMappingUrl(String baseUrl, String index) {
        return baseUrl + "/" + index + "/_mapping";
    }
    
    /**
     * 构建文档API URL
     */
    public static String buildDocumentUrl(String baseUrl, String index, String id) {
        return baseUrl + "/" + index + "/_doc/" + id;
    }
    
    /**
     * 构建批量操作API URL
     */
    public static String buildBulkUrl(String baseUrl) {
        return baseUrl + "/_bulk";
    }
    
    /**
     * 构建集群健康API URL
     */
    public static String buildClusterHealthUrl(String baseUrl) {
        return baseUrl + "/_cluster/health";
    }
    
    /**
     * 构建集群状态API URL
     */
    public static String buildClusterStateUrl(String baseUrl) {
        return baseUrl + "/_cluster/state";
    }
    
    /**
     * 构建集群节点信息API URL
     */
    public static String buildClusterNodesUrl(String baseUrl) {
        return baseUrl + "/_cat/nodes?format=json";
    }
    
    /**
     * 构建别名API URL（批量操作）
     */
    public static String buildAliasUrl(String baseUrl) {
        return baseUrl + "/_aliases";
    }
    
    /**
     * 构建索引别名API URL
     */
    public static String buildIndexAliasUrl(String baseUrl, String index) {
        return baseUrl + "/" + index + "/_alias";
    }
    
    /**
     * 构建刷新API URL
     */
    public static String buildRefreshUrl(String baseUrl, String index) {
        return baseUrl + "/" + index + "/_refresh";
    }
    
    /**
     * 构建计数API URL
     */
    public static String buildCountUrl(String baseUrl, String index) {
        return baseUrl + "/" + index + "/_count";
    }
    
    /**
     * 构建解释API URL
     */
    public static String buildExplainUrl(String baseUrl, String index, String id) {
        return baseUrl + "/" + index + "/_explain/" + id;
    }
    
    /**
     * 构建基础URL（从客户端配置中获取）
     */
    public String buildBaseUrl(String clientKey) {
        EsClientProperties clientConfig = esClientMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EsClientProperties>()
                        .eq(EsClientProperties::getUnikey, clientKey)
        );
        
        if (clientConfig == null) {
            throw new RuntimeException("未找到ES客户端配置: " + clientKey);
        }
        
        return clientConfig.getAddress();
    }
    
    /**
     * 构建带认证信息的请求头
     */
    public java.util.Map<String, String> buildAuthHeaders(EsClientProperties clientConfig) {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        
        if (clientConfig.getUsername() != null && clientConfig.getPassword() != null) {
            String auth = clientConfig.getUsername() + ":" + clientConfig.getPassword();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            headers.put("Authorization", "Basic " + encodedAuth);
        }
        
        return headers;
    }
}