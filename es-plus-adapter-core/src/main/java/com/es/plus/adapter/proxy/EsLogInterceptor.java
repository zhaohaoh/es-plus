package com.es.plus.adapter.proxy;

import com.es.plus.adapter.constants.EsPlusMethodConstant;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@EsInterceptors(value = {
        @InterceptorElement(type = EsPlusClient.class,methodName = EsPlusMethodConstant.SEARCH)
})
public class EsLogInterceptor implements EsInterceptor{
    
    @Override
    public void before(String index, Method method, EsParamWrapper<?> esParamWrapper) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        BoolQueryBuilder queryBuilder = esQueryParamWrapper.getQueryBuilder();
    }
    
    @Override
    public void after(String index, Method method, EsParamWrapper<?> esParamWrapper, Object result) {
    
    }
}
