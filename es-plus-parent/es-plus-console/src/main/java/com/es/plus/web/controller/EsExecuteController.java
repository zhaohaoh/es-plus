package com.es.plus.web.controller;

import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.web.compile.core.CompilationResult;
import com.es.plus.web.compile.core.DynamicCodeCompiler;
import com.es.plus.web.pojo.EsDslInfo;
import com.es.plus.web.pojo.EsPageInfo;
import com.es.plus.web.pojo.EsRequstInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/es")
public class EsExecuteController {
    
    private String function = "package com.es.plus.web.controller;\n" + "\n" + "import com.es.plus.core.statics.Es;\n"
            + "import java.util.Map;\n" + "import com.es.plus.core.wrapper.aggregation.EsAggWrapper;\n"
            + "import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;\n" + " \n" + "public class EsQuery {\n"
            + "    \n" + "    public  Object query(){\n" + "         %s;\n" + "    }\n" + "}";
    
    @GetMapping("esQuery/epl")
    public Object esQueryEpl(String epl, @RequestHeader("currentEsClient") String currentEsClient) throws Exception {
        if (epl.endsWith(";")) {
            epl = epl.substring(0, epl.length() - 1);
        }
        epl= abc(epl);
        
        String s = String.format(function, epl);
        s = StringUtils.replace(s, "chainQuery()", "chainQuery(\"" + currentEsClient + "\")");
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        DynamicCodeCompiler.clearCache("com.es.plus.web.controller.EsQuery");
        //        Class<?> compiled = dynamicClassLoader.compileAndLoad(s, CompilerConfig.createDefault());
        CompilationResult compilationResult = DynamicCodeCompiler.compile("com.es.plus.web.controller.EsQuery", s);
        
        CustomClassLoader customClassLoader = new CustomClassLoader(getClass().getClassLoader());
        
        Class<?> aClass = customClassLoader.loadClass("com.es.plus.web.controller.EsQuery");
        
        Object object = aClass.newInstance();
        Method sleep = aClass.getDeclaredMethod("query");
        sleep.setAccessible(true);
        Object invoke =  sleep.invoke(object);
        Object result = "";
        if (invoke instanceof EsResponse) {
            EsResponse esResponse = (EsResponse) invoke;
            SearchResponse sourceResponse = esResponse.getSourceResponse();
            result = sourceResponse.toString();
            ;
        }
        if (invoke instanceof EsAggResponse) {
            EsAggResponse esAggResponse = (EsAggResponse) invoke;
            esAggResponse.getAggregations();
            result =  Strings.toString(esAggResponse.getAggregations());
        }
        
        //        String jsonStr = JsonUtils.toJsonStr(result);
        //        System.out.println(jsonStr);
        //        EsResponse<Map> search = Es.chainQuery(Map.class).index("sys_user2ttt_alias").search();
        return result;
    }
    
    
    @PostMapping("esQuery/dsl")
    public String esQueryDsl(@RequestBody EsDslInfo  esDslInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(currentEsClient);
        String executed = Es.chainQuery(esPlusClientFacade, Map.class).index(esDslInfo.getIndex()).executeDSL(esDslInfo.getDsl());
        return executed;
    }
    
    
    /**
     * sql语句
     *
     * @param sql
     * @return
     */
    @GetMapping("esQuery/sql")
    public String esQuerySql(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        sql=sql.replace("SELECT","select");
        sql=sql.replace("BY","by");
        sql=sql.replace("FROM","from");
        sql=sql.replace("GROUP","group");
        sql=sql.replace("LIMIT","limit");
        // 匹配 SQL 语句中的表名
        Pattern pattern = Pattern.compile("(?i)FROM\\s+([\\w.]+)");
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        // 提取表名
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        if (StringUtils.isBlank(tableName)) {
            throw new EsException("sql语句中未找到表名");
        }
        
        if (sql.contains("group")&&containsAgg(sql)){
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> termsed = queryWrapper.esAggWrapper().terms(terms,a->{
                aggStr(a, trim);
                return;
            });
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
           return Strings.toString(aggregations.getAggregations());
        } else if (containsAgg(sql)) {
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> mapEsAggWrapper = queryWrapper.esAggWrapper();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            aggStr(mapEsAggWrapper, trim);
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        } else if (sql.contains("group")) {
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
        
      
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            
            setWhereSql(sql, queryWrapper);
            
            queryWrapper.esAggWrapper().terms(terms);
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        }
        
        String limit = StringUtils.substringAfterLast(sql, "limit");
        if (StringUtils.isBlank(limit)){
            sql = sql+" limit 100";
            limit = StringUtils.substringAfterLast(sql, "limit");
        }
        String[] split = limit.split(",");
        String pageSize = split[split.length - 1].trim();
        if (Integer.parseInt(pageSize.trim()) > 50000) {
            throw new RuntimeException("分页数量不能超过50000");
        }
        String result = Es.chainQuery(currentEsClient).executeSQL(sql);
        return result;
    }
    
    private  void setWhereSql(String sql, EsChainQueryWrapper<Map> queryWrapper) {
        List<Triple<String, String, String>> triples = extractWhereParams(sql);
        if (!triples.isEmpty()) {
            triples.forEach(
                    t->{
                        String k = t.getLeft();
                        String v = t.getRight();
                        String where = StringUtils.substringAfter(sql, "where");
                        if (where.contains("or")){
                            queryWrapper.should();
                        }
                        queryWrapper.terms(  t.getMiddle().equals("="),k,v.replace("=",""));
                        queryWrapper.ge(t.getMiddle().equals(">="),k,v.replace(">=",""));
                        queryWrapper.gt(t.getMiddle().equals(">"),k,v.replace(">",""));
                        queryWrapper.le(t.getMiddle().equals("<="),k,v.replace("<=",""));
                        queryWrapper.lt(t.getMiddle().equals("<"),k,v.replace("<",""));
                        if (t.getMiddle().equals("!=")){
                            queryWrapper.mustNot(a->{
                                a.term(k,v);
                            });
                        }
                        String[] split = t.getRight().split(",");
                        queryWrapper.terms(t.getMiddle().equalsIgnoreCase("in"),k, Arrays.stream(split).collect(Collectors.toSet()));
                    }
            );
           
        }
    }
    
    /**
     * sql语句
     *
     * @param sql
     * @return
     */
    @GetMapping("esQuery/explain")
    public String explain(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        sql=sql.replace("SELECT","select");
        sql=sql.replace("BY","by");
        sql=sql.replace("FROM","from");
        sql=sql.replace("GROUP","group");
        sql=sql.replace("LIMIT","limit");
        // 匹配 SQL 语句中的表名
        Pattern pattern = Pattern.compile("(?i)FROM\\s+([\\w.]+)");
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        // 提取表名
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        if (StringUtils.isBlank(tableName)) {
            throw new EsException("sql语句中未找到表名");
        }
        if (sql.contains("group")&&containsAgg(sql)){
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName).profile();
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> termsed = queryWrapper.esAggWrapper().terms(terms,a->{
                aggStr(a, trim);
                return;
            });
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        } else if (containsAgg(sql)) {
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName).profile();
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> mapEsAggWrapper = queryWrapper.esAggWrapper();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            aggStr(mapEsAggWrapper, trim);
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        } else if (sql.contains("group")) {
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName).profile();
            setWhereSql(sql, queryWrapper);
            queryWrapper.esAggWrapper().terms(terms);
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        }
        
        String limit = StringUtils.substringAfterLast(sql, "limit");
        if (StringUtils.isBlank(limit)){
            sql = sql+" limit 100";
            limit = StringUtils.substringAfterLast(sql, "limit");
        }
        String[] split = limit.split(",");
        String pageSize = split[split.length - 1].trim();
        if (Integer.parseInt(pageSize.trim()) > 50000) {
            throw new RuntimeException("分页数量不能超过50000");
        }
        String result = Es.chainQuery(currentEsClient).explainSQL(sql);
        return result;
    }
    
    /**
     * sql语句
     *
     * @param sql
     * @return
     */
    @GetMapping("esQuery/sql2Dsl")
    public String sql2Dsl(String sql, @RequestHeader("currentEsClient") String currentEsClient) {
        sql=sql.replace("SELECT","select");
        sql=sql.replace("BY","by");
        sql=sql.replace("FROM","from");
        sql=sql.replace("GROUP","group");
        sql=sql.replace("LIMIT","limit");
        // 匹配 SQL 语句中的表名
        Pattern pattern = Pattern.compile("(?i)FROM\\s+([\\w.]+)");
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        // 提取表名
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        if (StringUtils.isBlank(tableName)) {
            throw new EsException("sql语句中未找到表名");
        }
        if (sql.contains("group")&&containsAgg(sql)){
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> termsed = queryWrapper.esAggWrapper().terms(terms,a->{
                aggStr(a, trim);
                return;
            });
            SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(queryWrapper, termsed);
            return sourceBuilder.toString();
        } else if (containsAgg(sql)) {
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> mapEsAggWrapper = queryWrapper.esAggWrapper();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            aggStr(mapEsAggWrapper, trim);
            SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(queryWrapper, mapEsAggWrapper);
            return sourceBuilder.toString();
        } else if (sql.contains("group")) {
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            setWhereSql(sql, queryWrapper);
            EsAggWrapper<Map> mapEsAggWrapper = queryWrapper.esAggWrapper();
            mapEsAggWrapper.terms(terms);
            SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(queryWrapper, mapEsAggWrapper);
            return sourceBuilder.toString();
        }
        
        String limit = StringUtils.substringAfterLast(sql, "limit");
        if (StringUtils.isBlank(limit)){
            sql = sql+" limit 100";
            limit = StringUtils.substringAfterLast(sql, "limit");
        }
        String[] split = limit.split(",");
        String pageSize = split[split.length - 1].trim();
        if (Integer.parseInt(pageSize.trim()) > 50000) {
            throw new RuntimeException("分页数量不能超过50000");
        }
        String result = Es.chainQuery(currentEsClient).sql2Dsl(sql);
        return result;
    }
    
    private static SearchSourceBuilder getSearchSourceBuilder(EsChainQueryWrapper<Map> queryWrapper,
            EsAggWrapper<Map> mapEsAggWrapper) {
        BoolQueryBuilder queryBuilder = queryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(0);
        List<BaseAggregationBuilder> aggregationBuilders = mapEsAggWrapper.getAggregationBuilder();
        if (aggregationBuilders != null) {
            for (BaseAggregationBuilder aggregation : aggregationBuilders) {
                if (aggregation instanceof AggregationBuilder) {
                    sourceBuilder.aggregation((AggregationBuilder) aggregation);
                } else {
                    sourceBuilder.aggregation((PipelineAggregationBuilder) aggregation);
                }
            }
        }
        return sourceBuilder;
    }
    
    private static void aggStr(EsAggWrapper<Map> a, String trim) {
        if (trim.contains("sum")){
            String field = StringUtils.substringBetween(trim, "(", ")");
            a.sum(field);
        }
        else if (trim.contains("avg")){
            String field = StringUtils.substringBetween(trim, "(", ")");
            a.avg(field);
        }
        else if (trim.contains("count")){
            String field = StringUtils.substringBetween(trim, "(", ")");
            if (!field.contains("*")){
                a.count(field);
            }else{
                a.count("_id");
            }
        }
        else if (trim.contains("max")){
            String field = StringUtils.substringBetween(trim, "(", ")");
            a.max(field);
        }
        else if (trim.contains("min")){
            String field = StringUtils.substringBetween(trim, "(", ")");
            a.min(field);
        }
    }
    
    public boolean containsAgg(String str){
       if (str.contains("count")){
            return true;
        }else if (str.contains("sum")){
            return true;
        }else if (str.contains("min")){
            return true;
        }else if (str.contains("avg")){
            return true;
        }else if (str.contains("max")){
            return true;
        }
        return false;
    }
    
    
    /**
     * sql语句
     *
     */
    @PostMapping("esQuery/sqlPage")
    public String esQuerySqlPage(@RequestBody EsPageInfo esPageInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        if (esPageInfo.getSize() > 1000) {
            throw new RuntimeException("分页数量不能超过1000");
        }
        EsResponse<Map> esResponse = Es.chainQuery(currentEsClient).executeSQLep(esPageInfo.getSql());
        SearchResponse sourceResponse = esResponse.getSourceResponse();
        String result = sourceResponse.toString();
        return result;
    }
    
    
    /**
     * 根据id删除es数据
     */
    @DeleteMapping("/deleteByIds")
    public void deleteByIds(@RequestBody EsRequstInfo esRequstInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        List<String> ids = esRequstInfo.getIds();
        String index = esRequstInfo.getIndex();
        if (CollectionUtils.isEmpty(ids)) {
            throw new RuntimeException("需要删除的id不能为空");
        }
        EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(currentEsClient);
        Es.chainUpdate(esPlusClientFacade, Map.class).index(index).removeByIds(ids);
    }
    
    /**
     * 批量更新数据
     */
    @PutMapping("/updateBatch")
    public void updateBatch(@RequestBody EsRequstInfo esRequstInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(currentEsClient);
        
        EsIndexResponse mappings = Es.chainIndex(esPlusClientFacade).getIndex(esRequstInfo.getIndex());
        Map<String, Object> mapping = mappings.getMappings(esRequstInfo.getIndex());
        Map object = (Map) mapping.get("properties");
        Set keySet = object.keySet();
        
        List<Map> datas = esRequstInfo.getDatas();
        for (Map map : datas) {
            map.forEach((k,v)->{
                if (k!="_id"){
                    boolean contains = keySet.contains(k);
                    if (!contains){
                        throw new EsException("添加的字段再索引映射中不存在，请确认");
                    }
                }
            });
        }
        
        Es.chainUpdate(esPlusClientFacade, Map.class).index(esRequstInfo.getIndex()).saveOrUpdateBatch(esRequstInfo.getDatas());
    }
    
    
    public String abc(String input) {
        int lastIndex = input.lastIndexOf(';');
        
        if (lastIndex == -1) {
          return   "return "+input;
        }
        
        int secondLastIndex = input.lastIndexOf(';', lastIndex );
        
        if (secondLastIndex == -1) {
            System.out.println(input);
            return input;
        }
        
        String result = input.substring(0, secondLastIndex + 1)
                + "\nreturn "
                + input.substring(secondLastIndex + 1);
        
        return result;
    }
    /**
     * 提取 SQL WHERE 子句中的参数，返回 List<Triple<参数名, 条件运算符, 参数值>>
     * @param sql SQL 语句
     * @return List<Triple<String, String, String>>，例如：
     *         [("status", "IN", "1,2"), ("createTime", "<=", "2025-02-05 00:00:00")]
     */
    public static List<Triple<String, String, String>> extractWhereParams(String sql) {
        List<Triple<String, String, String>> params = new ArrayList<>();
        String whereClause = extractWhereClause(sql);
        
        if (whereClause == null || whereClause.trim().isEmpty()) {
            return params;
        }
        
        // 1. 匹配普通比较条件（=, >, <, >=, <=, !=）
        Pattern comparisonPattern = Pattern.compile(
                "(\\w+)\\s*(>=|<=|!=|=|>|<)\\s*(?:'([^']*)'|\"([^\"]*)\"|([^\\s,)]+))",
                Pattern.CASE_INSENSITIVE
        );
        
        // 匹配 IN 子句（支持数字、字符串、混合类型，包括换行符）
        Pattern inPattern = Pattern.compile(
                "(?i)(\\w+)\\s+IN\\s*\\(\\s*([\\s\\S]*?)\\s*\\)",
                Pattern.CASE_INSENSITIVE
        );
        
        // 处理普通比较条件
        Matcher comparisonMatcher = comparisonPattern.matcher(whereClause);
        while (comparisonMatcher.find()) {
            String paramName = comparisonMatcher.group(1);
            String operator = comparisonMatcher.group(2);
            String paramValue = comparisonMatcher.group(3) != null ? comparisonMatcher.group(3) :
                    comparisonMatcher.group(4) != null ? comparisonMatcher.group(4) :
                            comparisonMatcher.group(5);
            params.add(Triple.of(paramName, operator, paramValue));
        }
        
        // 处理 IN 子句
        Matcher inMatcher = inPattern.matcher(whereClause);
        while (inMatcher.find()) {
            String paramName = inMatcher.group(1);
            String operator = "IN";
            String inValues = inMatcher.group(2).trim();
            
            // 提取 IN 里面的各个值（支持数字、字符串、混合类型）
            String[] values = extractInValues(inValues);
            params.add(Triple.of(paramName, operator, String.join(",", values)));
        }
        
        return params;
    }
    
    private static String extractWhereClause(String sql) {
        String cleanSql = removeSqlComments(sql);
        
        // 更宽松的 WHERE 子句匹配
        Pattern wherePattern = Pattern.compile(
                "(?i)\\bWHERE\\b\\s+(.+)", // 匹配 WHERE 后的所有内容
                Pattern.DOTALL // 让 `.` 匹配换行符
        );
        
        Matcher whereMatcher = wherePattern.matcher(cleanSql);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1).trim();
            return whereClause;
//            // 检查是否包含 IN 子句（可选）
//            if (whereClause.toLowerCase().contains(" in ")) {
//                return whereClause;
//            }
        }
        
        return null;
    }
    private static String removeSqlComments(String sql) {
        // 移除单行注释 (-- ...)
        String noSingleLineComments = sql.replaceAll("--.*", "");
        // 移除多行注释 (/* ... */)
        String noMultiLineComments = noSingleLineComments.replaceAll("(?s)/\\*.*?\\*/", "");
        return noMultiLineComments.trim();
    }
    /**
     * 提取 IN 子句中的值列表（支持数字、字符串、混合类型）
     */
    private static String[] extractInValues(String inValues) {
        // 匹配数字或带引号的字符串
        Pattern valuePattern = Pattern.compile(
                "('[^']*'|\"[^\"]*\"|\\d+|[^\\s,]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher valueMatcher = valuePattern.matcher(inValues);
        
        List<String> values = new ArrayList<>();
        while (valueMatcher.find()) {
            String value = valueMatcher.group(1).trim();
            // 去掉引号（如果存在）
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            } else if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            values.add(value);
        }
        
        return values.toArray(new String[0]);
    }
}
