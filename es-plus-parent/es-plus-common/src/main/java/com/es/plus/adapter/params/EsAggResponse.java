package com.es.plus.adapter.params;


public interface EsAggResponse<T> {
    
    /**
     * 获取聚合
     *
     */
    Object getAggregations();
    
    /**
     * 获取es-plus提供的聚合
     *
     */
    EsAggResult<T> getEsAggResult();

    /**
     * 设置聚合
     *
     * @param aggregations 聚合
     */
    void setAggregations(Object aggregations);

    /**
     * 洞穴类
     *
     * @param tClass t类
     */
    void settClass(Class<T> tClass);
}
