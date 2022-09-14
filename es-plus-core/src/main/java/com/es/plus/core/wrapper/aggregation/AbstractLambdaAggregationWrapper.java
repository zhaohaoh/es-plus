package com.es.plus.core.wrapper.aggregation;


import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.AbstractLambdaEsWrapper;
import com.es.plus.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractLambdaAggregationWrapper<T, R extends SFunction<T, ?>> extends AbstractLambdaEsWrapper<T, R> {

    protected Class<T> tClass;

    public String getAggregationField(R sFunction) {
        String name = nameToString(sFunction);
        String keyword = EsParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }

}
