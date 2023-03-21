package com.es.plus.core.wrapper.core;


import com.es.plus.core.tools.LambdaUtils;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.tools.SerializedLambda;
import com.es.plus.exception.EsException;

import java.util.Arrays;
import java.util.Locale;

public abstract class AbstractLambdaEsWrapper<T, R> {


    protected final String[] nameToString(R... functions) {
        return Arrays.stream(functions).map(this::nameToString).toArray(String[]::new);
    }


    protected String nameToString(R function) {
        if (function instanceof String) {
            return (String) function;
        }
        SerializedLambda lambda = LambdaUtils.resolve((SFunction<T, ?>) function);
        return getColumn(lambda);
    }

    protected Class<?> getImplClass(R function) {
        SerializedLambda lambda = LambdaUtils.resolve((SFunction<T, ?>) function);
        return lambda.getImplClass();
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
