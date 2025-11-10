package com.es.plus.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EPL链式语法解析器
 * 解析类似 Es.chainQuery().index("fast_test_new_v6").term("username","酷酷的").search(10) 的链式调用语法
 */
@Slf4j
@Service
public class EplChainSyntaxParser {

    /**
     * 解析EPL链式语法
     *
     * @param eplChain EPL链式语法字符串
     * @return 解析后的方法调用链
     */
    public EplParseResult parseChain(String eplChain) {
        if (StringUtils.isBlank(eplChain)) {
            throw new IllegalArgumentException("EPL链式语法不能为空");
        }

        try {
            log.debug("开始解析EPL链式语法: {}", eplChain);

            EplParseResult result = new EplParseResult();

            // 预处理：处理多行语句
            String processedChain = preprocessChain(eplChain);

            // 解析变量赋值（如果有）
            parseVariableAssignment(processedChain, result);

            // 解析方法调用链
            parseMethodChain(processedChain, result);

            log.debug("EPL链式语法解析完成，方法数量: {}", result.getMethodCalls().size());
            return result;
        } catch (Exception e) {
            log.error("EPL链式语法解析失败: {}", eplChain, e);
            throw new RuntimeException("EPL链式语法解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预处理链式语法
     */
    private String preprocessChain(String chain) {
        // 移除多余的空白字符
        chain = chain.replaceAll("\\s+", " ").trim();

        // 移除分号（如果有）
        if (chain.endsWith(";")) {
            chain = chain.substring(0, chain.length() - 1);
        }

        return chain;
    }

    /**
     * 解析变量赋值
     * 例如：EsChainQueryWrapper<Map> es = Es.chainQuery().index("fast_test_new_v6");
     */
    private void parseVariableAssignment(String chain, EplParseResult result) {
        // 匹配变量赋值模式
        Pattern pattern = Pattern.compile("^(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\s*=\\s*(.+)$");
        Matcher matcher = pattern.matcher(chain);

        if (matcher.find()) {
            result.setVariableType(matcher.group(1).trim());
            result.setVariableName(matcher.group(2).trim());
            result.setMethodChain(matcher.group(3).trim());

            log.debug("发现变量赋值: {} {} = {}",
                    result.getVariableType(), result.getVariableName(), result.getMethodChain());
        } else {
            // 没有变量赋值，整个都是方法链
            result.setMethodChain(chain);
        }
    }

    /**
     * 解析方法调用链
     */
    private void parseMethodChain(String methodChain, EplParseResult result) {
        List<MethodCall> methodCalls = new ArrayList<>();

        // 分割方法调用
        String[] methodParts = methodChain.split("\\.\\s*");

        for (String methodPart : methodParts) {
            if (StringUtils.isBlank(methodPart)) continue;

            MethodCall methodCall = parseSingleMethod(methodPart);
            if (methodCall != null) {
                methodCalls.add(methodCall);
            }
        }

        result.setMethodCalls(methodCalls);
    }

    /**
     * 解析单个方法调用
     */
    private MethodCall parseSingleMethod(String methodStr) {
        try {
            // 匹配方法名和参数
            Pattern pattern = Pattern.compile("^(\\w+)\\s*\\((.*)\\)$");
            Matcher matcher = pattern.matcher(methodStr);

            if (!matcher.find()) {
                log.warn("无法解析方法调用: {}", methodStr);
                return null;
            }

            String methodName = matcher.group(1);
            String paramsStr = matcher.group(2);

            MethodCall methodCall = new MethodCall();
            methodCall.setMethodName(methodName);
            methodCall.setParameters(parseParameters(paramsStr));

            return methodCall;
        } catch (Exception e) {
            log.warn("解析方法调用失败: {}", methodStr, e);
            return null;
        }
    }

    /**
     * 解析方法参数
     */
    private List<Object> parseParameters(String paramsStr) {
        List<Object> parameters = new ArrayList<>();

        if (StringUtils.isBlank(paramsStr)) {
            return parameters;
        }

        // 简单的参数解析（支持字符串、数字、布尔值）
        // TODO: 支持复杂的Lambda表达式解析

        String[] paramParts = paramsStr.split("\\s*,\\s*");

        for (String param : paramParts) {
            param = param.trim();
            if (StringUtils.isBlank(param)) continue;

            // 解析字符串
            if (param.startsWith("\"") && param.endsWith("\"")) {
                parameters.add(param.substring(1, param.length() - 1));
            } else if (param.startsWith("'") && param.endsWith("'")) {
                parameters.add(param.substring(1, param.length() - 1));
            }
            // 解析数字
            else if (param.matches("\\d+")) {
                parameters.add(Integer.parseInt(param));
            } else if (param.matches("\\d+\\.\\d+")) {
                parameters.add(Double.parseDouble(param));
            }
            // 解析布尔值
            else if ("true".equalsIgnoreCase(param)) {
                parameters.add(true);
            } else if ("false".equalsIgnoreCase(param)) {
                parameters.add(false);
            }
            // 解析Lambda表达式（简单处理）
            else if (param.contains("->")) {
                parameters.add(parseLambdaExpression(param));
            }
            // 其他作为字符串处理
            else {
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * 解析Lambda表达式
     */
    private LambdaExpression parseLambdaExpression(String lambdaStr) {
        LambdaExpression lambda = new LambdaExpression();
        lambda.setOriginalExpression(lambdaStr);

        // 简单解析：提取方法调用
        // 例如：a->a.size(100) 提取 size 方法和参数 100
        Pattern pattern = Pattern.compile("^\\w+->\\w+\\.(\\w+)\\((.*)\\)$");
        Matcher matcher = pattern.matcher(lambdaStr);

        if (matcher.find()) {
            lambda.setMethodName(matcher.group(1));
            String paramsStr = matcher.group(2);
            if (StringUtils.isNotBlank(paramsStr)) {
                lambda.setParameters(parseParameters(paramsStr));
            }
        }

        return lambda;
    }

    /**
     * EPL解析结果
     */
    public static class EplParseResult {
        private String variableType;
        private String variableName;
        private String methodChain;
        private List<MethodCall> methodCalls = new ArrayList<>();

        // getters and setters
        public String getVariableType() {
            return variableType;
        }

        public void setVariableType(String variableType) {
            this.variableType = variableType;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        public String getMethodChain() {
            return methodChain;
        }

        public void setMethodChain(String methodChain) {
            this.methodChain = methodChain;
        }

        public List<MethodCall> getMethodCalls() {
            return methodCalls;
        }

        public void setMethodCalls(List<MethodCall> methodCalls) {
            this.methodCalls = methodCalls;
        }
    }

    /**
     * 方法调用
     */
    public static class MethodCall {
        private String methodName;
        private List<Object> parameters = new ArrayList<>();

        // getters and setters
        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public List<Object> getParameters() {
            return parameters;
        }

        public void setParameters(List<Object> parameters) {
            this.parameters = parameters;
        }

        public String getParameterAsString(int index) {
            if (index >= 0 && index < parameters.size()) {
                Object param = parameters.get(index);
                return param != null ? param.toString() : null;
            }
            return null;
        }

        public Integer getParameterAsInteger(int index) {
            if (index >= 0 && index < parameters.size()) {
                Object param = parameters.get(index);
                if (param instanceof Number) {
                    return ((Number) param).intValue();
                }
                if (param instanceof String) {
                    try {
                        return Integer.parseInt((String) param);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Lambda表达式
     */
    public static class LambdaExpression {
        private String originalExpression;
        private String methodName;
        private List<Object> parameters = new ArrayList<>();

        // getters and setters
        public String getOriginalExpression() {
            return originalExpression;
        }

        public void setOriginalExpression(String originalExpression) {
            this.originalExpression = originalExpression;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public List<Object> getParameters() {
            return parameters;
        }

        public void setParameters(List<Object> parameters) {
            this.parameters = parameters;
        }
    }

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
}