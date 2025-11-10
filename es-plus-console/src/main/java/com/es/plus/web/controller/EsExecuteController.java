package com.es.plus.web.controller;

import com.es.plus.web.pojo.EsDslInfo;
import com.es.plus.web.pojo.EsPageInfo;
import com.es.plus.web.pojo.EsRequstInfo;
import com.es.plus.web.service.EplChainSyntaxParser;
import com.es.plus.web.service.EplToDslConverter;
import com.es.plus.web.service.EsRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Elasticsearch操作控制器
 * 提供DSL查询、SQL查询、数据操作等功能
 */
@Slf4j
@RestController
@RequestMapping("/es")
public class EsExecuteController {
    
    @Autowired
    private EsRestService esRestService;
    
    @Autowired
    private EplToDslConverter eplToDslConverter;
    
    @Autowired
    private EplChainSyntaxParser eplChainSyntaxParser;
    
    /**
     * EPL查询 - 将EPL（支持SQL语法和链式语法）转换为DSL后执行
     */
    @GetMapping("esQuery/epl")
    public Object esQueryEpl(String epl, @RequestHeader("currentEsClient") String currentEsClient) {
        log.info("EPL查询请求: clientKey={}, epl={}", currentEsClient, epl);
        
        try {
            // 1. 验证EPL语句
            if (StringUtils.isBlank(epl)) {
                throw new RuntimeException("EPL语句不能为空");
            }
            
            // 2. 将EPL转换为DSL
            String dsl = eplToDslConverter.convertEplToDsl(epl);
            log.debug("EPL转DSL结果: {}", dsl);
            
            // 3. 从EPL中提取索引名称
            String indexName = extractIndexFromEpl(epl);
            if (StringUtils.isBlank(indexName)) {
                throw new RuntimeException("无法从EPL语句中提取索引名称，请确保指定了索引");
            }
            
            // 4. 执行DSL查询
            String searchResponse = esRestService.search(currentEsClient, indexName, dsl);
            
            // 5. 检查是否为聚合查询，返回相应格式
            if (isAggregationQuery(dsl)) {
                // 聚合查询，使用EsResponseParser解析aggregations部分
                return eplChainSyntaxParser.parseAggregationsResponse(searchResponse);
            } else {
                // 普通查询，返回hits部分（这应该和原来动态编译的格式一致）
                return searchResponse;
            }
            
        } catch (Exception e) {
            log.error("EPL查询失败: clientKey={}, epl={}", currentEsClient, epl, e);
            throw new RuntimeException("EPL查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * DSL查询
     */
    @PostMapping("esQuery/dsl")
    public String esQueryDsl(@RequestBody EsDslInfo esDslInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("DSL查询请求: clientKey={}, index={}", currentEsClient, esDslInfo.getIndex());
        return esRestService.search(currentEsClient, esDslInfo.getIndex(), esDslInfo.getDsl());
    }
    
    /**
     * SQL语句查询
     * 直接使用Elasticsearch SQL API
     */
    @GetMapping("esQuery/sql")
    public String esQuerySql(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("SQL查询请求: clientKey={}, sql={}", currentEsClient, sql);
        
        // 基本SQL验证
        if (StringUtils.isBlank(sql)) {
            throw new RuntimeException("SQL语句不能为空");
        }
        
        // 添加默认limit
        if (!sql.toLowerCase().contains("limit")) {
            sql += " limit 100";
        }
        
        return esRestService.searchBySql(currentEsClient, sql);
    }
    
    /**
     * SQL查询解释
     */
    @GetMapping("esQuery/explain")
    public String explain(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("SQL解释请求: clientKey={}, sql={}", currentEsClient, sql);
        
        if (StringUtils.isBlank(sql)) {
            throw new RuntimeException("SQL语句不能为空");
        }
        
        // 使用ES的SQL API进行解释
        String explainSql = "EXPLAIN " + sql;
        return esRestService.searchBySql(currentEsClient, explainSql);
    }
    
    /**
     * SQL转DSL
     */
    @GetMapping("esQuery/sql2Dsl")
    public String sql2Dsl(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("SQL转DSL请求: clientKey={}, sql={}", currentEsClient, sql);
        
        if (StringUtils.isBlank(sql)) {
            throw new RuntimeException("SQL语句不能为空");
        }
        
        return esRestService.sql2Dsl(currentEsClient, sql);
    }
    
    /**
     * SQL分页查询
     */
    @PostMapping("esQuery/sqlPage")
    public String esQuerySqlPage(@RequestBody EsPageInfo esPageInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("SQL分页查询请求: clientKey={}, sql={}", currentEsClient, esPageInfo.getSql());
        
        if (esPageInfo.getSize() > 1000) {
            throw new RuntimeException("分页数量不能超过1000");
        }
        
        return esRestService.searchBySql(currentEsClient, esPageInfo.getSql());
    }
    
    /**
     * 根据ID删除es数据
     */
    @DeleteMapping("/deleteByIds")
    public void deleteByIds(@RequestBody EsRequstInfo esRequstInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        List<String> ids = esRequstInfo.getIds();
        String index = esRequstInfo.getIndex();
        
        log.debug("批量删除请求: clientKey={}, index={}, count={}", currentEsClient, index, ids.size());
        
        if (CollectionUtils.isEmpty(ids)) {
            throw new RuntimeException("需要删除的id不能为空");
        }
        
        esRestService.deleteByIds(currentEsClient, index, ids);
    }
    
    /**
     * 批量更新数据
     */
    @PutMapping("/updateBatch")
    public void updateBatch(@RequestBody EsRequstInfo esRequstInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        String index = esRequstInfo.getIndex();
        List<Map<String, Object>> datas = esRequstInfo.getDatas();
        
        log.debug("批量更新请求: clientKey={}, index={}, count={}", currentEsClient, index, datas.size());
        
        if (CollectionUtils.isEmpty(datas)) {
            throw new RuntimeException("更新数据不能为空");
        }
        
        // 获取索引映射，验证字段
        String mappingResponse = esRestService.getMapping(currentEsClient, index);
        if (mappingResponse.contains("error")) {
            throw new RuntimeException("获取索引映射失败: " + mappingResponse);
        }
        
        // 执行批量更新
        esRestService.saveBatch(currentEsClient, index, datas);
    }
    
    /**
     * 测试ES连接
     */
    @GetMapping("/testConnection")
    public Map<String, Object> testConnection(@RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("测试连接请求: clientKey={}", currentEsClient);
        
        boolean pingResult = esRestService.ping(currentEsClient);
        String clusterHealth = esRestService.getClusterHealth(currentEsClient);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", pingResult);
        result.put("clusterHealth", clusterHealth);
        return result;
    }
    
    /**
     * 获取索引信息
     */
    @GetMapping("/indexInfo")
    public String getIndexInfo(String index, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("获取索引信息: clientKey={}, index={}", currentEsClient, index);
        return esRestService.getMapping(currentEsClient, index);
    }
    
    /**
     * 刷新索引
     */
    @PostMapping("/refresh")
    public String refreshIndex(String index, @RequestHeader("currentEsClient") String currentEsClient) {
        log.debug("刷新索引: clientKey={}, index={}", currentEsClient, index);
        return esRestService.refresh(currentEsClient, index);
    }
    
    /**
     * 判断DSL查询是否包含聚合
     */
    private boolean isAggregationQuery(String dsl) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dslMap = mapper.readValue(dsl, Map.class);
            
            // 检查是否包含aggs字段
            return dslMap.containsKey("aggs") || dslMap.containsKey("aggregations");
        } catch (Exception e) {
            log.debug("判断聚合查询失败，默认为普通查询: {}", dsl, e);
            return false;
        }
    }
    
    /**
     * 从EPL语句中提取索引名称
     * 支持SQL语法和链式语法两种格式
     */
    private String extractIndexFromEpl(String epl) {
        try {
            // 1. 尝试从SQL语法的FROM子句中提取索引名
            Pattern fromPattern = Pattern.compile("(?i)FROM\\s+([\\w_]+)(?:\\s+|$)");
            Matcher sqlMatcher = fromPattern.matcher(epl);
            
            if (sqlMatcher.find()) {
                return sqlMatcher.group(1).trim();
            }
            
            // 2. 尝试从链式语法的index()方法中提取索引名
            Pattern indexPattern = Pattern.compile("\\.index\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
            Matcher chainMatcher = indexPattern.matcher(epl);
            
            if (chainMatcher.find()) {
                return chainMatcher.group(1).trim();
            }
            
            // 3. 尝试从链式语法的index()方法中提取索引名（单引号）
            indexPattern = Pattern.compile("\\.index\\s*\\(\\s*'([^']+)'\\s*\\)");
            chainMatcher = indexPattern.matcher(epl);
            
            if (chainMatcher.find()) {
                return chainMatcher.group(1).trim();
            }
            
            // 4. 尝试从复杂链式语法中提取（使用链式语法解析器）
            if (epl.contains("Es.chainQuery") || epl.contains(".index(")) {
                try {
                    EplChainSyntaxParser.EplParseResult parseResult = eplChainSyntaxParser.parseChain(epl);
                    for (EplChainSyntaxParser.MethodCall methodCall : parseResult.getMethodCalls()) {
                        if ("index".equals(methodCall.getMethodName())) {
                            String indexName = methodCall.getParameterAsString(0);
                            if (StringUtils.isNotBlank(indexName)) {
                                return indexName;
                            }
                        }
                    }
                } catch (Exception parseEx) {
                    log.debug("链式语法解析失败，跳过: {}", parseEx.getMessage());
                }
            }
            
            log.warn("无法从EPL语句中提取索引名称: {}", epl);
            return null;
        } catch (Exception e) {
            log.error("从EPL中提取索引名失败: {}", epl, e);
            return null;
        }
    }
}