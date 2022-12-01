package com.es.plus.core.wrapper.aggregation;

/**
 * @Author: hzh
 * @Date: 2022/6/14 12:31
 */
public interface EsAggQuery<Children, R> {
    void minDocCount(R name, Object... value);

    void order(R name, Object... value);

    void includeExclude(R name, Object... value);

    void executionHint(R name, Object... value);

    void shardSize(R name, Object... value);

    void size(R name, Object... value);

    void shardMinDocCount(R name, Object... value);

    Children subAggregation(R name, Object... value);

}
