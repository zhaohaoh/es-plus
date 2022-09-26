package com.es.plus.core.wrapper.aggregation;


public class EsAggregationWrapper<T> extends AbstractEsAggregationWrapper<T, String, EsAggregationWrapper<T>> {

    public void setClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsAggregationWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    @Override
    protected EsAggregationWrapper<T> instance() {
        return new EsAggregationWrapper<>(super.tClass);
    }
}
