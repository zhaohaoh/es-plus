package com.es.plus.adapter.proxy;

import java.lang.reflect.Method;

public interface EsInterceptor {

    default void before(String index, Method method,Object[] args) {

    }

    default void after(String index, Method method,Object[] args,Object result) {

    }

}
