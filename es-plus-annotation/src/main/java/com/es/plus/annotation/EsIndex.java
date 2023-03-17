package com.es.plus.annotation;

import com.es.plus.constant.DefaultClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsIndex {

    /**
     * 索引名
     */
    String index() default "";

    /**
     * 类型 高版本已默认_doc。旧版本需要手动设置
     */
    String type() default "_doc";

    /**
     * 分片
     */
    int shard() default 5;

    /**
     * 副本
     */
    int replices() default 1;

    /**
     * 初始窗口值 可更改
     */
    int initMaxResultWindow() default 100000;

    /**
     * 初始刷新值 可更改
     */
    String initRefreshInterval() default "1s";

    /**
     * 选择设置分词器
     */
    String[] analyzer() default {};

    /**
     * 选择设置默认分词器
     */
    String defaultAnalyzer() default "";

    /**
     * 别名
     */
    String alias() default "";

    /**
     * 是否试着重建索引
     */
    boolean tryReindex() default false;

    Class<?> childClass() default DefaultClass.class;

    Class<?> parentClass() default DefaultClass.class;
}
