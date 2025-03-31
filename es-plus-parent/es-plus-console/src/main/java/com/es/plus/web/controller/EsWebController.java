package com.es.plus.web.controller;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.web.compile.core.CompilationResult;
import com.es.plus.web.compile.core.DynamicCodeCompiler;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.misc.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Map;


@RestController
@RequestMapping("web")
public class EsWebController {
    
    private String function = "package com.es.plus.web.controller;\n" + "\n" + "import com.es.plus.core.statics.Es;\n"
            + "import java.util.Map;\n" + "\n" + "public class EsQuery {\n" + "\n" + "    public  Object query(){\n"
            + "        return %s;\n" + "    }\n" + "}\n";
    
    @GetMapping("esQuery/epl")
    public Object esQueryEpl(String epl) throws Exception {
        if (epl.endsWith(";")) {
            epl = epl.substring(0, epl.length() - 1);
        }
        String s = String.format(function, epl);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        DynamicCodeCompiler.clearCache("com.es.plus.web.controller.EsQuery");
//        Class<?> compiled = dynamicClassLoader.compileAndLoad(s, CompilerConfig.createDefault());
        CompilationResult compilationResult = DynamicCodeCompiler.compile("com.es.plus.web.controller.EsQuery", s);
        
        EsChainQueryWrapper<Map> mapEsChainQueryWrapper = Es.chainQuery();
 
        CustomClassLoader customClassLoader = new CustomClassLoader(getClass().getClassLoader());
       

        Class<?> aClass = customClassLoader.loadClass("com.es.plus.web.controller.EsQuery");
       
        Object object = ReflectUtil.newInstance(aClass);
        Method sleep = aClass.getDeclaredMethod("query");
        sleep.setAccessible(true);
        Object invoke = (EsResponse) sleep.invoke(object);
        Object result="";
        if (invoke instanceof EsResponse){
            EsResponse esResponse = (EsResponse) invoke;
            SearchResponse sourceResponse = esResponse.getSourceResponse();
            result= sourceResponse.toString();;
        }
        if (invoke instanceof EsAggResponse){
            EsAggResponse esAggResponse = (EsAggResponse) invoke;
            esAggResponse.getAggregations();
            result=  esAggResponse.getAggregations().toString();
        }
        
//        String jsonStr = JsonUtils.toJsonStr(result);
//        System.out.println(jsonStr);
//        EsResponse<Map> search = Es.chainQuery(Map.class).index("sys_user2ttt_alias").search();
        return result;
    }
    
    
    @GetMapping("esQuery/dsl")
    public String esQueryDsl(String index,String dsl) {
        String executed = Es.chainQuery().index(index).executeDSL(dsl);
        return executed;
    }
    
  
    
}
