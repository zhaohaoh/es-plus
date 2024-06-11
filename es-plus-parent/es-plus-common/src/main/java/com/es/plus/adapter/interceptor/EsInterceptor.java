package com.es.plus.adapter.interceptor;

import com.es.plus.adapter.params.EsParamWrapper;

import java.lang.reflect.Method;

public interface EsInterceptor {
    
    default void before(String index, String type, Method method, Object[] args) {
        EsParamWrapper<?> esParamWrapper = null;
        for (Object arg : args) {
            if (arg instanceof EsParamWrapper) {
                esParamWrapper = (EsParamWrapper<?>) arg;
                break;
            }
        }
        if (esParamWrapper != null) {
            before(index, type, method, esParamWrapper);
        }
    }
    
    default void after(String index, String type, Method method, Object[] args, Object result) {
        EsParamWrapper<?> esParamWrapper = null;
        for (Object arg : args) {
            if (arg instanceof EsParamWrapper) {
                esParamWrapper = (EsParamWrapper<?>) arg;
                break;
            }
        }
        if (esParamWrapper != null) {
            after(index, type, method, esParamWrapper, result);
        }
    }
    
    default void before(String index, String type, Method method, EsParamWrapper<?> esParamWrapper) {
    
    }
    
    default void after(String index, String type, Method method, EsParamWrapper<?> esParamWrapper, Object result) {
    
    }
    
}
