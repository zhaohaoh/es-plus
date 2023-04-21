package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsUpdateField;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;

public interface EsWrapper< T> {


    EsParamWrapper<T> esParamWrapper();

    EsLambdaAggWrapper<T> esLambdaAggWrapper();

    EsAggWrapper<T> esAggWrapper();

    BoolQueryBuilder getQueryBuilder();



    default EsUpdateField getEsUpdateField() {
        return null;
    }
}
