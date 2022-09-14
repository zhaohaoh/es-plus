package com.es.plus.core.wrapper.aggregation;


import com.es.plus.core.tools.SFunction;

public class EsAggregationWrapper<T> extends AbstractEsAggregationWrapper<T, SFunction<T, ?>, EsAggregationWrapper<T>> {

    public void setClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsAggregationWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }
}
