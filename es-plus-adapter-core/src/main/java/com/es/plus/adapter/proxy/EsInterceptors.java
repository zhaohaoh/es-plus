package com.es.plus.adapter.proxy;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EsInterceptors {
    InterceptorElement[] value();
}
