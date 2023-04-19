package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsUpdateField;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;

public interface EsWrapper<Children, T> {

    EsLambdaAggWrapper<T> esLambdaAggWrapper();

    EsAggWrapper<T> esAggWrapper();

    BoolQueryBuilder getQueryBuilder();

    Children must();

    Children should();

    Children filter();

    Children mustNot();

    Children minimumShouldMatch(String minimumShouldMatch);

    default EsUpdateField getEsUpdateField() {
        return null;
    }
}
