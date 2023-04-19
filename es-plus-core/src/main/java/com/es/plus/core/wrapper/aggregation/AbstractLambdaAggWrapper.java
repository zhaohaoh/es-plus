package com.es.plus.core.wrapper.aggregation;


import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.adapter.tools.SerializedLambda;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public abstract class AbstractLambdaAggWrapper<T, R> {

    protected Class<T> tClass;

    public String getAggregationField(R sFunction) {
        if (sFunction == null || sFunction instanceof String) {
            return (String) sFunction;
        }
        String name = nameToString(sFunction);

        String keyword = EsParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }

    protected String nameToString(R function) {
        SerializedLambda lambda = LambdaUtils.resolve((SFunction<T, ?>) function);
        return getColumn(lambda);
    }

    private String getColumn(SerializedLambda lambda) {
        return methodToProperty(lambda.getImplMethodName());
    }

    private String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new EsException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }
}
