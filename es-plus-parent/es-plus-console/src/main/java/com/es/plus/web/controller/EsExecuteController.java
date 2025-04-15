package com.es.plus.web.controller;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.web.compile.core.CompilationResult;
import com.es.plus.web.compile.core.DynamicCodeCompiler;
import com.es.plus.web.pojo.EsPageInfo;
import com.es.plus.web.pojo.EsRequstInfo;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.misc.ReflectUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/es")
public class EsExecuteController {
    
    private String function = "package com.es.plus.web.controller;\n" + "\n" + "import com.es.plus.core.statics.Es;\n"
            + "import java.util.Map;\n" + "\n" + "public class EsQuery {\n" + "\n" + "    public  Object query(){\n"
            + "        return %s;\n" + "    }\n" + "}\n";
    
    @GetMapping("esQuery/epl")
    public Object esQueryEpl(String epl, @RequestHeader("currentEsClient") String currentEsClient) throws Exception {
        if (epl.endsWith(";")) {
            epl = epl.substring(0, epl.length() - 1);
        }
        String s = String.format(function, epl);
        s = StringUtils.replace(s, "chainQuery()", "chainQuery(\"" + currentEsClient + "\")");
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        DynamicCodeCompiler.clearCache("com.es.plus.web.controller.EsQuery");
        //        Class<?> compiled = dynamicClassLoader.compileAndLoad(s, CompilerConfig.createDefault());
        CompilationResult compilationResult = DynamicCodeCompiler.compile("com.es.plus.web.controller.EsQuery", s);
        
        CustomClassLoader customClassLoader = new CustomClassLoader(getClass().getClassLoader());
        
        Class<?> aClass = customClassLoader.loadClass("com.es.plus.web.controller.EsQuery");
        
        Object object = ReflectUtil.newInstance(aClass);
        Method sleep = aClass.getDeclaredMethod("query");
        sleep.setAccessible(true);
        Object invoke = (EsResponse) sleep.invoke(object);
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
            result = esAggResponse.getAggregations().toString();
        }
        
        //        String jsonStr = JsonUtils.toJsonStr(result);
        //        System.out.println(jsonStr);
        //        EsResponse<Map> search = Es.chainQuery(Map.class).index("sys_user2ttt_alias").search();
        return result;
    }
    
    
    @GetMapping("esQuery/dsl")
    public String esQueryDsl(String index, String dsl, @RequestHeader("currentEsClient") String currentEsClient) {
        EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(currentEsClient);
        String executed = Es.chainQuery(esPlusClientFacade, Map.class).index(index).executeDSL(dsl);
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
        EsResponse<Map> esResponse = Es.chainQuery(currentEsClient).executeSQL(sql);
        SearchResponse sourceResponse = esResponse.getSourceResponse();
        String result = sourceResponse.toString();
        return result;
    }
    
    
    /**
     * sql语句
     *
     */
    @PostMapping("esQuery/sqlPage")
    public String esQuerySqlPage(@RequestBody EsPageInfo esPageInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        EsResponse<Map> esResponse = Es.chainQuery(currentEsClient).executeSQL(esPageInfo.getSql());
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
     * 根据id删除es数据
     */
    @PutMapping("/updateBatch")
    public void updateBatch(@RequestBody EsRequstInfo esRequstInfo, @RequestHeader("currentEsClient") String currentEsClient) {
        EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(currentEsClient);
        
        Es.chainUpdate(esPlusClientFacade, Map.class).index(esRequstInfo.getIndex()).updateBatch(esRequstInfo.getDatas());
    }
    
}
