package com.es.plus.common.interceptor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InterceptorElement {
    /**
     * 拦截的类名 有多版本控制
     */
    Class<?> type();
    /**
     * 拦截的索引名 不填默认所有
     */
    String[] index() default {};

    /**
     * 拦截的方法名 不填默认所有
     */
    String[] methodName() default {};
    
    /**
     * 拦截的请求方式枚举
     */
    MethodEnum[] methods() default {};
}
