package com.es.plus.core.wrapper.core;


import com.es.plus.core.wrapper.aggregation.EsAggregationWrapper;
import com.es.plus.core.wrapper.aggregation.EsLamdaAggregationWrapper;
import com.es.plus.pojo.EsUpdateField;
import org.elasticsearch.index.query.BoolQueryBuilder;

public interface EsWrapper<Children, T> {

    EsLamdaAggregationWrapper<T> esLamdaAggregationWrapper();

    EsAggregationWrapper<T> esAggregationWrapper();

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
