package com.es.plus.adapter.params;

import org.elasticsearch.search.aggregations.Aggregations;

public interface EsAggResponse<T> {
    /**
     * 获取聚合
     *
     * @return {@link Aggregations}
     */
    Aggregations getAggregations();

    /**
     * 设置聚合
     *
     * @param aggregations 聚合
     */
    void setAggregations(Aggregations aggregations);

    /**
     * 洞穴类
     *
     * @param tClass t类
     */
    void settClass(Class<T> tClass);
}
