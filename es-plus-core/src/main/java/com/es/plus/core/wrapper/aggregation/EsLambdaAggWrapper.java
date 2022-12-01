package com.es.plus.core.wrapper.aggregation;


import com.es.plus.core.tools.SFunction;

public class EsLambdaAggWrapper<T> extends AbstractEsAggWrapper<T, SFunction<T, ?>, EsLambdaAggWrapper<T>> {

    public void setClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsLambdaAggWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    @Override
    protected EsLambdaAggWrapper<T> instance() {
        return new EsLambdaAggWrapper<>(super.tClass);
    }
}
