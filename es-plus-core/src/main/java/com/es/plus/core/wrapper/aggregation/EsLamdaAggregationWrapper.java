package com.es.plus.core.wrapper.aggregation;


import com.es.plus.core.tools.SFunction;

public class EsLamdaAggregationWrapper<T> extends AbstractEsAggregationWrapper<T, SFunction<T, ?>, EsLamdaAggregationWrapper<T>> {

    public void setClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsLamdaAggregationWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    @Override
    protected EsLamdaAggregationWrapper<T> instance() {
        return new EsLamdaAggregationWrapper<>(super.tClass);
    }
}
