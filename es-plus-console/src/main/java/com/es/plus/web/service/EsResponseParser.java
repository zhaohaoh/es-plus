package com.es.plus.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch响应解析工具类
 * 用于解析ES REST API的响应，并转换为原有格式
 */
@Slf4j
@Component
public class EsResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析搜索响应，提取hits部分
     */
    public String parseSearchResponse(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            JsonNode hitsNode = rootNode.get("hits");

            if (hitsNode != null) {
                return objectMapper.writeValueAsString(hitsNode);
            }

            return esResponse;
        } catch (JsonProcessingException e) {
            log.error("解析搜索响应失败", e);
            return esResponse;
        }
    }

    /**
     * 解析聚合响应
     */
    public String parseAggregationsResponse(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            JsonNode aggregationsNode = rootNode.get("aggregations");

            if (aggregationsNode != null) {
                return objectMapper.writeValueAsString(aggregationsNode);
            }

            return esResponse;
        } catch (JsonProcessingException e) {
            log.error("解析聚合响应失败", e);
            return esResponse;
        }
    }

    /**
     * 解析SQL响应
     */
    public String parseSqlResponse(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);

            // SQL响应可能有两种格式：
            // 1. 原生SQL API响应：包含columns和rows
            // 2. 转换后的DSL搜索响应：包含hits

            JsonNode columnsNode = rootNode.get("columns");
            JsonNode rowsNode = rootNode.get("rows");

            if (columnsNode != null && rowsNode != null) {
                // 原生SQL API格式，需要转换为类似搜索结果的格式
                return convertSqlToSearchFormat(rootNode);
            }

            // 如果是搜索结果格式，直接返回hits部分
            JsonNode hitsNode = rootNode.get("hits");
            if (hitsNode != null) {
                return objectMapper.writeValueAsString(hitsNode);
            }

            return esResponse;
        } catch (JsonProcessingException e) {
            log.error("解析SQL响应失败", e);
            return esResponse;
        }
    }

    /**
     * 将SQL API响应转换为搜索响应格式
     */
    private String convertSqlToSearchFormat(JsonNode sqlResponse) throws JsonProcessingException {
        JsonNode columnsNode = sqlResponse.get("columns");
        JsonNode rowsNode = sqlResponse.get("rows");

        List<ObjectNode> hits = new ArrayList<>();

        if (rowsNode != null && rowsNode.isArray()) {
            for (JsonNode rowNode : rowsNode) {
                if (rowNode.isArray()) {
                    ObjectNode hit = objectMapper.createObjectNode();
                    ObjectNode source = objectMapper.createObjectNode();

                    // 将列名和值对应起来
                    for (int i = 0; i < columnsNode.size() && i < rowNode.size(); i++) {
                        String columnName = columnsNode.get(i).get("name").asText();
                        JsonNode value = rowNode.get(i);
                        source.set(columnName, value);
                    }

                    hit.set("_source", source);
                    hits.add(hit);
                }
            }
        }

        ObjectNode searchResponse = objectMapper.createObjectNode();
        ObjectNode hitsWrapper = objectMapper.createObjectNode();
        hitsWrapper.put("total", objectMapper.createObjectNode().put("value", hits.size()));
        hitsWrapper.set("hits", objectMapper.valueToTree(hits));
        searchResponse.set("hits", hitsWrapper);

        return objectMapper.writeValueAsString(searchResponse);
    }

    /**
     * 解析索引信息响应
     */
    public String parseIndexResponse(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            log.error("解析索引响应失败", e);
            return esResponse;
        }
    }

    /**
     * 解析错误响应
     */
    public String parseErrorResponse(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            JsonNode errorNode = rootNode.get("error");

            if (errorNode != null) {
                return objectMapper.writeValueAsString(errorNode);
            }

            return esResponse;
        } catch (JsonProcessingException e) {
            log.error("解析错误响应失败", e);
            return esResponse;
        }
    }

    /**
     * 检查响应是否为错误
     */
    public boolean isError(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            JsonNode errorNode = rootNode.get("error");
            return errorNode != null;
        } catch (JsonProcessingException e) {
            return true; // 解析失败也认为是错误
        }
    }

    /**
     * 提取错误信息
     */
    public String extractErrorMessage(String esResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(esResponse);
            JsonNode errorNode = rootNode.get("error");

            if (errorNode != null) {
                if (errorNode.isObject()) {
                    JsonNode reasonNode = errorNode.get("root_cause");
                    if (reasonNode != null && reasonNode.isArray() && reasonNode.size() > 0) {
                        return reasonNode.get(0).get("reason").asText();
                    }

                    JsonNode reasonNode2 = errorNode.get("reason");
                    if (reasonNode2 != null) {
                        return reasonNode2.asText();
                    }

                    return errorNode.toString();
                } else {
                    return errorNode.asText();
                }
            }

            return "未知错误";
        } catch (JsonProcessingException e) {
            return "响应解析失败: " + esResponse;
        }
    }

    /**
     * 构建成功的响应格式
     */
    public String buildSuccessResponse(Object data) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.set("data", objectMapper.valueToTree(data));
            response.put("success", true);
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("构建成功响应失败", e);
            return "{\"success\":false,\"error\":\"响应构建失败\"}";
        }
    }

    /**
     * 构建失败的响应格式
     */
    public String buildErrorResponse(String error) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", false);
            response.put("error", error);
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("构建错误响应失败", e);
            return "{\"success\":false,\"error\":\"响应构建失败\"}";
        }
    }
}