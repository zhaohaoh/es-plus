package com.es.plus.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 转 DSL 转换器
 * 支持基本的 SELECT、WHERE、ORDER BY、LIMIT 语法
 */
@Slf4j
@Component
public class SqlToDslConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 SQL 转换为 ES DSL
     */
    public String convertSqlToDsl(String sql) {
        try {
            sql = sql.trim();

            // 解析 SQL
            SqlParseResult parseResult = parseSql(sql);

            // 构建 DSL
            Map<String, Object> dsl = buildDsl(parseResult);

            return objectMapper.writeValueAsString(dsl);
        } catch (Exception e) {
            log.error("SQL 转 DSL 失败: {}", sql, e);
            throw new RuntimeException("SQL 转 DSL 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 SQL 语句
     */
    private SqlParseResult parseSql(String sql) {
        SqlParseResult result = new SqlParseResult();

        // 提取 SELECT 字段
        result.fields = extractFields(sql);

        // 提取 FROM 表名（索引名）
        result.index = extractIndex(sql);

        // 提取 WHERE 条件
        result.whereConditions = extractWhereConditions(sql);

        // 提取 ORDER BY
        result.orderBy = extractOrderBy(sql);

        // 提取 LIMIT
        result.limit = extractLimit(sql);

        return result;
    }

    /**
     * 提取 SELECT 字段
     */
    private List<String> extractFields(String sql) {
        Pattern pattern = Pattern.compile("(?i)SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String fieldsStr = matcher.group(1).trim();
            if ("*".equals(fieldsStr)) {
                return Arrays.asList("*");
            }
            return Arrays.asList(fieldsStr.split("\\s*,\\s*"));
        }

        return Arrays.asList("*");
    }

    /**
     * 提取索引名
     */
    private String extractIndex(String sql) {
        Pattern pattern = Pattern.compile("(?i)FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        throw new RuntimeException("无法从 SQL 中提取索引名");
    }

    /**
     * 提取 WHERE 条件
     */
    private List<WhereCondition> extractWhereConditions(String sql) {
        List<WhereCondition> conditions = new ArrayList<>();

        Pattern wherePattern = Pattern.compile("(?i)WHERE\\s+(.+?)(?:\\s+ORDER\\s+BY|\\s+LIMIT|$)", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);

        if (!whereMatcher.find()) {
            return conditions;
        }

        String whereClause = whereMatcher.group(1).trim();

        // 分割 AND 条件
        String[] andConditions = whereClause.split("(?i)\\s+AND\\s+");

        for (String condition : andConditions) {
            condition = condition.trim();

            // 解析单个条件
            WhereCondition wc = parseCondition(condition);
            if (wc != null) {
                conditions.add(wc);
            }
        }

        return conditions;
    }

    /**
     * 解析单个条件
     */
    private WhereCondition parseCondition(String condition) {
        WhereCondition wc = new WhereCondition();

        // = 条件
        if (condition.contains("=")) {
            String[] parts = condition.split("=");
            wc.field = parts[0].trim();
            wc.operator = "=";
            wc.value = parseValue(parts[1].trim());
            return wc;
        }

        // LIKE 条件
        if (condition.toUpperCase().contains(" LIKE ")) {
            String[] parts = condition.split("(?i)\\s+LIKE\\s+");
            wc.field = parts[0].trim();
            wc.operator = "LIKE";
            wc.value = parseValue(parts[1].trim()).toString().replace("%", "*");
            return wc;
        }

        // IN 条件
        if (condition.toUpperCase().contains(" IN ")) {
            Pattern inPattern = Pattern.compile("(?i)(\\w+)\\s+IN\\s+\\((.+?)\\)");
            Matcher inMatcher = inPattern.matcher(condition);
            if (inMatcher.find()) {
                wc.field = inMatcher.group(1).trim();
                wc.operator = "IN";
                String valuesStr = inMatcher.group(2).trim();
                String[] values = valuesStr.split("\\s*,\\s*");
                List<Object> valueList = new ArrayList<>();
                for (String v : values) {
                    valueList.add(parseValue(v.trim()));
                }
                wc.value = valueList;
                return wc;
            }
        }

        // > 条件
        if (condition.contains(">")) {
            String[] parts = condition.split(">");
            wc.field = parts[0].trim();
            wc.operator = ">";
            wc.value = parseValue(parts[1].trim());
            return wc;
        }

        // < 条件
        if (condition.contains("<")) {
            String[] parts = condition.split("<");
            wc.field = parts[0].trim();
            wc.operator = "<";
            wc.value = parseValue(parts[1].trim());
            return wc;
        }

        return null;
    }

    /**
     * 解析值（去除引号，转换类型）
     */
    private Object parseValue(String value) {
        value = value.trim();

        // 去除单引号或双引号
        if ((value.startsWith("'") && value.endsWith("'")) ||
            (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }

        // 尝试转换为数字
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * 提取 ORDER BY
     */
    private Map<String, String> extractOrderBy(String sql) {
        Pattern pattern = Pattern.compile("(?i)ORDER\\s+BY\\s+(\\w+)\\s*(ASC|DESC)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            Map<String, String> orderBy = new HashMap<>();
            orderBy.put("field", matcher.group(1).trim());
            orderBy.put("order", matcher.group(2) != null ? matcher.group(2).toLowerCase() : "asc");
            return orderBy;
        }

        return null;
    }

    /**
     * 提取 LIMIT
     */
    private Integer extractLimit(String sql) {
        Pattern pattern = Pattern.compile("(?i)LIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 10; // 默认返回 10 条
    }

    /**
     * 构建 DSL
     */
    private Map<String, Object> buildDsl(SqlParseResult parseResult) {
        Map<String, Object> dsl = new HashMap<>();

        // 构建 query
        if (!parseResult.whereConditions.isEmpty()) {
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> bool = new HashMap<>();
            List<Map<String, Object>> must = new ArrayList<>();

            for (WhereCondition condition : parseResult.whereConditions) {
                Map<String, Object> clause = buildQueryClause(condition);
                if (clause != null) {
                    must.add(clause);
                }
            }

            bool.put("must", must);
            query.put("bool", bool);
            dsl.put("query", query);
        } else {
            // 没有 WHERE 条件，查询所有
            Map<String, Object> query = new HashMap<>();
            query.put("match_all", new HashMap<>());
            dsl.put("query", query);
        }

        // 构建 sort
        if (parseResult.orderBy != null) {
            List<Map<String, Object>> sort = new ArrayList<>();
            Map<String, Object> sortField = new HashMap<>();
            Map<String, String> sortOrder = new HashMap<>();
            sortOrder.put("order", parseResult.orderBy.get("order"));
            sortField.put(parseResult.orderBy.get("field"), sortOrder);
            sort.add(sortField);
            dsl.put("sort", sort);
        }

        // 设置 size
        dsl.put("size", parseResult.limit);

        return dsl;
    }

    /**
     * 构建查询子句
     */
    private Map<String, Object> buildQueryClause(WhereCondition condition) {
        Map<String, Object> clause = new HashMap<>();

        switch (condition.operator) {
            case "=":
                // term 查询
                Map<String, Object> term = new HashMap<>();
                term.put(condition.field, condition.value);
                clause.put("term", term);
                break;

            case "LIKE":
                // wildcard 查询
                Map<String, Object> wildcard = new HashMap<>();
                Map<String, Object> wildcardValue = new HashMap<>();
                wildcardValue.put("value", condition.value);
                wildcard.put(condition.field, wildcardValue);
                clause.put("wildcard", wildcard);
                break;

            case "IN":
                // terms 查询
                Map<String, Object> terms = new HashMap<>();
                terms.put(condition.field, condition.value);
                clause.put("terms", terms);
                break;

            case ">":
                // range 查询
                Map<String, Object> rangeGt = new HashMap<>();
                Map<String, Object> gtValue = new HashMap<>();
                gtValue.put("gt", condition.value);
                rangeGt.put(condition.field, gtValue);
                clause.put("range", rangeGt);
                break;

            case "<":
                // range 查询
                Map<String, Object> rangeLt = new HashMap<>();
                Map<String, Object> ltValue = new HashMap<>();
                ltValue.put("lt", condition.value);
                rangeLt.put(condition.field, ltValue);
                clause.put("range", rangeLt);
                break;

            default:
                return null;
        }

        return clause;
    }

    /**
     * SQL 解析结果
     */
    private static class SqlParseResult {
        List<String> fields;
        String index;
        List<WhereCondition> whereConditions;
        Map<String, String> orderBy;
        Integer limit;
    }

    /**
     * WHERE 条件
     */
    private static class WhereCondition {
        String field;
        String operator;
        Object value;
    }
}