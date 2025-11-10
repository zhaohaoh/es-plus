package com.es.plus.web.service;

import com.es.plus.web.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * EPL链式语法到DSL转换器
 * 专门将EPL链式语法转换为Elasticsearch DSL查询
 */
@Slf4j
@Service
public class EplToDslConverter {

    @Autowired
    private EplChainSyntaxParser chainSyntaxParser;

    /**
     * 将EPL链式语法语句转换为DSL
     *
     * @param epl EPL链式查询语句
     * @return DSL查询JSON字符串
     */
    public String convertEplToDsl(String epl) {
        if (StringUtils.isBlank(epl)) {
            throw new IllegalArgumentException("EPL语句不能为空");
        }

        try {
            log.debug("开始转换EPL链式语法到DSL: {}", epl);

            // 转换链式语法
            return convertChainSyntaxToDsl(epl);
        } catch (Exception e) {
            log.error("EPL链式语法转DSL失败: {}", epl, e);
            throw new RuntimeException("EPL链式语法解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换链式语法为DSL
     */
    private String convertChainSyntaxToDsl(String epl) {
        log.debug("解析EPL链式语法: {}", epl);

        // 解析链式语法
        EplChainSyntaxParser.EplParseResult parseResult = chainSyntaxParser.parseChain(epl);

        // 构建DSL查询
        Map<String, Object> dslQuery = buildDslFromChainSyntax(parseResult);

        String dslJson = JsonUtils.toJsonStr(dslQuery);
        log.debug("EPL链式语法转DSL成功: {} -> {}", epl, dslJson);

        return dslJson;
    }

    /**
     * 从链式语法构建DSL查询
     */
    private Map<String, Object> buildDslFromChainSyntax(EplChainSyntaxParser.EplParseResult parseResult) {
        Map<String, Object> dslQuery = new HashMap<>();

        // 解析索引名称和查询参数
        ChainQueryContext context = new ChainQueryContext();

        for (EplChainSyntaxParser.MethodCall methodCall : parseResult.getMethodCalls()) {
            processMethodCall(methodCall, context);
        }

        // 构建DSL
        if (!context.getQueryConditions().isEmpty()) {
            Map<String, Object> query = buildQueryFromConditions(context.getQueryConditions());
            dslQuery.put("query", query);
        } else {
            dslQuery.put("query", Collections.singletonMap("match_all", Collections.emptyMap()));
        }

        // 添加排序
        if (!context.getSortFields().isEmpty()) {
            dslQuery.put("sort", context.getSortFields());
        }

        // 添加分页
        if (context.getSize() != null) {
            dslQuery.put("size", context.getSize());
        }
        if (context.getFrom() != null) {
            dslQuery.put("from", context.getFrom());
        }

        // 添加聚合
        if (!context.getAggregations().isEmpty()) {
            dslQuery.put("aggs", context.getAggregations());
        }

        // 添加字段过滤
        if (!context.getSourceFields().isEmpty()) {
            dslQuery.put("_source", context.getSourceFields());
        }

        return dslQuery;
    }

    /**
     * 处理单个方法调用
     */
    private void processMethodCall(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String methodName = methodCall.getMethodName();

        switch (methodName) {
            case "index":
                processIndexMethod(methodCall, context);
                break;
            case "term":
                processTermMethod(methodCall, context);
                break;
            case "terms":
                // 根据上下文判断是查询还是聚合
                if (context.isInAggregationMode()) {
                    processTermsAggMethod(methodCall, context);
                } else {
                    processTermsMethod(methodCall, context);
                }
                break;
            case "match":
                processMatchMethod(methodCall, context);
                break;
            case "wildcard":
                processWildcardMethod(methodCall, context);
                break;
            case "range":
            case "ge":
            case "le":
            case "gt":
            case "lt":
                processRangeMethod(methodCall, context);
                break;
            case "sortBy":
            case "sortByAsc":
            case "sortByDesc":
                processSortMethod(methodCall, context);
                break;
            case "search":
                processSearchMethod(methodCall, context);
                break;
            case "esAggWrapper":
                processAggWrapperMethod(methodCall, context);
                break;
            case "sum":
            case "avg":
            case "max":
            case "min":
                processMetricAggMethod(methodCall, context);
                break;
            case "subAgg":
                processSubAggMethod(methodCall, context);
                break;
            case "aggregations":
                // 标记执行聚合，无需特殊处理
                context.setShouldExecuteAggregation(true);
                break;
            case "includes":
            case "excludes":
                processSourceFieldsMethod(methodCall, context);
                break;
            default:
                log.warn("未知的方法调用: {}", methodName);
        }
    }

    private void processIndexMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String indexName = methodCall.getParameterAsString(0);
        if (StringUtils.isNotBlank(indexName)) {
            context.setIndexName(indexName);
        }
    }

    private void processTermMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String field = methodCall.getParameterAsString(0);
        String value = methodCall.getParameterAsString(1);
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)) {
            Map<String, Object> termQuery = new HashMap<>();
            termQuery.put(field, value);
            context.getQueryConditions().add(Collections.singletonMap("term", termQuery));
        }
    }

    private void processTermsMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String field = methodCall.getParameterAsString(0);
        List<Object> values = new ArrayList<>();
        for (int i = 1; i < methodCall.getParameters().size(); i++) {
            values.add(methodCall.getParameters().get(i));
        }
        if (StringUtils.isNotBlank(field) && !values.isEmpty()) {
            Map<String, Object> termsQuery = new HashMap<>();
            termsQuery.put(field, values);
            context.getQueryConditions().add(Collections.singletonMap("terms", termsQuery));
        }
    }

    private void processMatchMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String field = methodCall.getParameterAsString(0);
        String value = methodCall.getParameterAsString(1);
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)) {
            Map<String, Object> matchQuery = new HashMap<>();
            matchQuery.put(field, value);
            context.getQueryConditions().add(Collections.singletonMap("match", matchQuery));
        }
    }

    private void processWildcardMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String field = methodCall.getParameterAsString(0);
        String value = methodCall.getParameterAsString(1);
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)) {
            Map<String, Object> wildcardQuery = new HashMap<>();
            wildcardQuery.put(field, value);
            context.getQueryConditions().add(Collections.singletonMap("wildcard", wildcardQuery));
        }
    }

    private void processRangeMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String methodName = methodCall.getMethodName();
        String field = methodCall.getParameterAsString(0);
        Object value = methodCall.getParameters().size() > 1 ? methodCall.getParameters().get(1) : null;

        if (StringUtils.isNotBlank(field) && value != null) {
            Map<String, Object> rangeQuery = new HashMap<>();
            Map<String, Object> rangeValue = new HashMap<>();

            switch (methodName) {
                case "range":
                    // range方法可能有多个参数，暂时简单处理
                    if (methodCall.getParameters().size() >= 3) {
                        rangeValue.put("gte", methodCall.getParameters().get(1));
                        rangeValue.put("lte", methodCall.getParameters().get(2));
                    }
                    break;
                case "ge":
                    rangeValue.put("gte", value);
                    break;
                case "le":
                    rangeValue.put("lte", value);
                    break;
                case "gt":
                    rangeValue.put("gt", value);
                    break;
                case "lt":
                    rangeValue.put("lt", value);
                    break;
            }

            rangeQuery.put(field, rangeValue);
            context.getQueryConditions().add(Collections.singletonMap("range", rangeQuery));
        }
    }

    private void processSortMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String methodName = methodCall.getMethodName();
        String order = "asc"; // 默认升序

        if ("sortBy".equals(methodName)) {
            order = methodCall.getParameterAsString(0);
            if (StringUtils.isBlank(order)) {
                order = "asc";
            } else {
                order = order.toLowerCase();
            }
        } else if ("sortByDesc".equals(methodName)) {
            order = "desc";
        }

        String field = "sortBy".equals(methodName)
                ? methodCall.getParameterAsString(1)
                : methodCall.getParameterAsString(0);

        if (StringUtils.isNotBlank(field)) {
            // 特殊处理：如果排序字段是_id，转换为_doc排序
            if ("_id".equals(field)) {
                Map<String, Object> sortField = new HashMap<>();
                sortField.put("_doc", Collections.singletonMap("order", order));
                context.getSortFields().add(sortField);
                log.debug("检测到_id字段排序，自动转换为_doc排序: {}", order);
            } else {
                Map<String, Object> sortField = new HashMap<>();
                sortField.put(field, Collections.singletonMap("order", order));
                context.getSortFields().add(sortField);
            }
        }
    }

    private void processSearchMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        Integer size = methodCall.getParameterAsInteger(0);
        if (size != null) {
            context.setSize(size);
        }
        // 如果有第二个参数，作为from
        if (methodCall.getParameters().size() >= 2) {
            Integer from = methodCall.getParameterAsInteger(1);
            if (from != null) {
                context.setFrom(from);
            }
        }
    }

    private void processAggWrapperMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        context.setInAggregationMode(true);
    }

    private void processTermsAggMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String field = methodCall.getParameterAsString(0);
        if (StringUtils.isNotBlank(field)) {
            Map<String, Object> termsAgg = new HashMap<>();
            termsAgg.put("field", field);
            termsAgg.put("size", 100); // 默认大小
            context.getAggregations().put(field + "_terms", Collections.singletonMap("terms", termsAgg));
        }
    }

    private void processMetricAggMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String methodName = methodCall.getMethodName();
        String aggName = methodCall.getParameterAsString(0);
        String field = methodCall.getParameterAsString(1);

        if (StringUtils.isNotBlank(aggName) && StringUtils.isNotBlank(field)) {
            Map<String, Object> metricAgg = new HashMap<>();
            metricAgg.put("field", field);
            context.getAggregations().put(aggName, Collections.singletonMap(methodName, metricAgg));
        }
    }

    private void processSubAggMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        // 简单处理：暂时忽略subAgg的复杂逻辑
        log.debug("处理subAgg方法: {}", methodCall.getMethodName());
    }

    private void processSourceFieldsMethod(EplChainSyntaxParser.MethodCall methodCall, ChainQueryContext context) {
        String methodName = methodCall.getMethodName();
        List<String> fields = new ArrayList<>();
        for (Object param : methodCall.getParameters()) {
            if (param != null) {
                fields.add(param.toString());
            }
        }

        if (!fields.isEmpty()) {
            if ("includes".equals(methodName)) {
                context.getSourceFields().addAll(fields);
            } else if ("excludes".equals(methodName)) {
                context.setExcludesFields(fields);
            }
        }
    }

    /**
     * 从查询条件构建查询
     */
    private Map<String, Object> buildQueryFromConditions(List<Map<String, Object>> conditions) {
        if (conditions.isEmpty()) {
            return Collections.singletonMap("match_all", Collections.emptyMap());
        }

        if (conditions.size() == 1) {
            return conditions.get(0);
        }

        Map<String, Object> boolQuery = new HashMap<>();
        boolQuery.put("must", conditions);
        return Collections.singletonMap("bool", boolQuery);
    }

    /**
     * 链式查询上下文
     */
    private static class ChainQueryContext {
        private String indexName;
        private List<Map<String, Object>> queryConditions = new ArrayList<>();
        private List<Map<String, Object>> sortFields = new ArrayList<>();
        private Map<String, Object> aggregations = new HashMap<>();
        private List<String> sourceFields = new ArrayList<>();
        private List<String> excludesFields = new ArrayList<>();
        private Integer size;
        private Integer from;
        private boolean inAggregationMode = false;
        private boolean shouldExecuteAggregation = false;

        // getters and setters
        public String getIndexName() { return indexName; }
        public void setIndexName(String indexName) { this.indexName = indexName; }
        public List<Map<String, Object>> getQueryConditions() { return queryConditions; }
        public List<Map<String, Object>> getSortFields() { return sortFields; }
        public Map<String, Object> getAggregations() { return aggregations; }
        public List<String> getSourceFields() { return sourceFields; }
        public List<String> getExcludesFields() { return excludesFields; }
        public void setExcludesFields(List<String> excludesFields) { this.excludesFields = excludesFields; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        public Integer getFrom() { return from; }
        public void setFrom(Integer from) { this.from = from; }
        public boolean isInAggregationMode() { return inAggregationMode; }
        public void setInAggregationMode(boolean inAggregationMode) { this.inAggregationMode = inAggregationMode; }
        public boolean isShouldExecuteAggregation() { return shouldExecuteAggregation; }
        public void setShouldExecuteAggregation(boolean shouldExecuteAggregation) { this.shouldExecuteAggregation = shouldExecuteAggregation; }
    }
}