package com.es.plus.common.interceptor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EsInterceptors {
    InterceptorElement[] value();
}
