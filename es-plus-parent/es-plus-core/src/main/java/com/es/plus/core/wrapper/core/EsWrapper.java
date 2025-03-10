package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.interceptor.EsUpdateField;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;

public interface EsWrapper< T> {


    EsParamWrapper<T> esParamWrapper();

    EsLambdaAggWrapper<T> esLambdaAggWrapper();

    EsAggWrapper<T> esAggWrapper();
    
    String[] getIndexs();
    
    default EsUpdateField getEsUpdateField() {
        return null;
    }
}
