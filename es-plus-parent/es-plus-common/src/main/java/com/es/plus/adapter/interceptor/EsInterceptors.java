package com.es.plus.adapter.interceptor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EsInterceptors {
    InterceptorElement[] value();
}
