package com.es.plus.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsId {

    /**
     * 名字
     *
     * @return {@link String}
     */
    String name() default "";
}
