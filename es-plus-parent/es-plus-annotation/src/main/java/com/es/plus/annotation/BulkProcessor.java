package com.es.plus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//  1000条或者10MB或者3秒发起一次输入插入
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BulkProcessor {
    
    /**
     * 达到批量的条数阈值  1000条
     */
    int bulkActions() default 1000;
    
    /**
     * 单位MB 默认5MB
     */
    int bulkSize() default 5;
    
    /**
     * 刷新周期  单位秒 默认5秒
     */
    int flushInterval() default 5;
    
    /**
     * 并发写入数  默认1
     */
    int concurrent() default 1;
    
    /**
     * 失败后间隔多久重试 单位ms  默认500ms
     */
    int BackoffPolicyTime() default 500;
    
    /**
     * 失败后间隔重试最大次数
     */
    int BackoffPolicyRetryMax() default 2;
}
