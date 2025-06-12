package com.es.plus.web.controller;

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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            EsAggWrapper<Map> termsed = queryWrapper.esAggWrapper().terms(terms,a->{
                aggStr(a, trim);
                return;
            });
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
           return Strings.toString(aggregations.getAggregations());
        } else if (containsAgg(sql)) {
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
            EsAggWrapper<Map> mapEsAggWrapper = queryWrapper.esAggWrapper();
            String trim = StringUtils.substringBetween(sql, "select", "from").trim();
            aggStr(mapEsAggWrapper, trim);
            EsAggResponse<Map> aggregations = queryWrapper.aggregations();
            return Strings.toString(aggregations.getAggregations());
        } else if (sql.contains("group")) {
            String terms = StringUtils.substringAfterLast(sql, "by").trim();
            EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(currentEsClient).index(tableName);
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
       
       
//        Map<String, Object> map = JsonUtils.toMap(result);
//        Map hits = (Map) map.get("hits");
//        List list  = (List) hits.get("hits");
//        if (list!=null && list.size()>0){
//            for (Object object : list) {
//                if (object!=null){
//                    Map hit = (Map) object;
//                    Map docValueMap = (Map) hit.remove("fields");
//                    if (docValueMap!=null) {
//                        Map data = (Map) hit.get("_source");
//                        data.putAll(docValueMap);
//                    }
//                }
//            }
//        }
        
        return result;
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
   
}
