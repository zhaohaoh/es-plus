package com.es.plus.common.interceptor;

import com.es.plus.common.constants.EsPlusMethodConstant;
import com.es.plus.common.core.EsPlusClient;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.params.EsQueryParamWrapper;

import java.lang.reflect.Method;

//@Component
@EsInterceptors(value = {
        @InterceptorElement(type = EsPlusClient.class,methodName = EsPlusMethodConstant.SEARCH)
})
public class EsLogInterceptor implements EsInterceptor{
    
    @Override
    public void before(String index,String type, Method method, EsParamWrapper<?> esParamWrapper,EsPlusClient esPlusClient) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
       
    }
    
    @Override
    public void after(String index, String type,Method method, EsParamWrapper<?> esParamWrapper, Object result,EsPlusClient esPlusClient) {
    
    }
}
