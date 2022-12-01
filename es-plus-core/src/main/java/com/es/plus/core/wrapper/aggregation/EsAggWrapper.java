package com.es.plus.core.wrapper.aggregation;


public class EsAggWrapper<T> extends AbstractEsAggregationWrapper<T, String, EsAggWrapper<T>> {

    public void setClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsAggWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    @Override
    protected EsAggWrapper<T> instance() {
        return new EsAggWrapper<>(super.tClass);
    }
}
