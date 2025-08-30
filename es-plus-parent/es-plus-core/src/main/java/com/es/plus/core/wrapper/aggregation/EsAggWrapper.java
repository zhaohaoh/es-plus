package com.es.plus.core.wrapper.aggregation;


import com.es.plus.adapter.pojo.es.EpAggBuilder;
import com.es.plus.adapter.pojo.es.EpCompositeValuesSourceBuilder;

import java.util.List;
import java.util.function.Function;

public class EsAggWrapper<T> extends AbstractEsAggWrapper<T, String, EsAggWrapper<T>> {

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
