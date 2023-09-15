package com.es.plus.core.wrapper.aggregation;


import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractLambdaAggWrapper<T, R> {

    protected Class<T> tClass;

    public String getAggregationField(R sFunction) {
        if (sFunction == null || sFunction instanceof String) {
            return (String) sFunction;
        }
        String name = nameToString(sFunction);

        String keyword = GlobalParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }

    protected String nameToString(R function) {
        String fieldName = LambdaUtils.getFieldName((SFunction<T, ?>) function);
        EsFieldInfo indexField = GlobalParamHolder.getIndexField(tClass, fieldName);
        return indexField != null && StringUtils.isNotBlank(indexField.getName()) ? indexField.getName() : fieldName;
    }
}
