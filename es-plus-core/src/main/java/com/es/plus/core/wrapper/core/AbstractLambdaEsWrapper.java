package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;

import java.util.Arrays;

import static com.es.plus.constant.EsConstant.DOT;

public abstract class AbstractLambdaEsWrapper<T, R> {
    protected String parentFieldName;
    protected Class<T> tClass;

    protected final String[] nameToString(R... functions) {
        return Arrays.stream(functions).map(this::nameToString).toArray(String[]::new);
    }


    protected String nameToString(R function) {
        if (function instanceof String) {
            return parentFieldName != null ? parentFieldName + DOT + function : (String) function;
        }

        String fieldName = LambdaUtils.getFieldName((SFunction<T, ?>) function);
        EsFieldInfo indexField = GlobalParamHolder.getIndexField(tClass, fieldName);
        String column = indexField.getName();
        return  parentFieldName != null ? parentFieldName+ DOT + column:column;
    }

}
