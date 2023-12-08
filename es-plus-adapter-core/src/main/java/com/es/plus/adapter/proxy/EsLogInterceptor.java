package com.es.plus.adapter.proxy;

import com.es.plus.adapter.constants.EsPlusMethodConstant;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Component
@EsInterceptors(value = {
        @InterceptorElement(type = EsPlusClient.class,methodName = EsPlusMethodConstant.SEARCH)
})
public class EsLogInterceptor implements EsInterceptor{
    // EsParamWrapper 参数
    @Override
    public void before(String index, Method method, Object[] args) {
        EsParamWrapper<Object> objectEsParamWrapper = new EsParamWrapper<>();
        List<QueryBuilder> must = objectEsParamWrapper.getQueryBuilder().must();
        for (QueryBuilder queryBuilder : must) {
            queryBuilder.queryName("f");
        }
        System.out.println(index+method.toString());
    }

    @Override
    public void after(String index, Method method, Object[] args, Object result) {
        EsResponse esResponse = (EsResponse) result;
        Object[] tailSortValues = esResponse.getTailSortValues();
        System.out.println();
    }
}
