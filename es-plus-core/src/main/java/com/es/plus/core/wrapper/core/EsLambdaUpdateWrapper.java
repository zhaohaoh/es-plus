package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsUpdateField;
import com.es.plus.adapter.tools.SFunction;

import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsLambdaUpdateWrapper<T> extends AbstractEsWrapper<T, SFunction<T, ?>, EsLambdaUpdateWrapper<T>> implements Update<EsLambdaUpdateWrapper<T>, SFunction<T, ?>> {



    public EsLambdaUpdateWrapper<T> doSet(String name, Object value) {
        List<EsUpdateField.Field> fields = getEsUpdateField().getFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    public EsLambdaUpdateWrapper<T> doIncrement(String name, Long value) {
        List<EsUpdateField.Field> fields = getEsUpdateField().getIncrementFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    @Override
    public EsLambdaUpdateWrapper<T> setScipt(boolean condition, String scipt, Map<String, Object> sciptParams) {
        if (condition) {
            getEsUpdateField().setScipt(scipt, sciptParams);
        }
        return this;
    }

    public EsLambdaUpdateWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsLambdaUpdateWrapper() {
    }

    @Override
    protected EsLambdaUpdateWrapper<T> instance() {
        return new EsLambdaUpdateWrapper<T>(super.tClass);
    }

    @Override
    public EsUpdateField getEsUpdateField() {
        return getEsUpdateField();
    }

    @Override
    public EsLambdaUpdateWrapper<T> set(SFunction<T, ?> column, Object val) {
        return doSet(nameToString(column), val);
    }

    @Override
    public EsLambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            List<EsUpdateField.Field> fields = getEsUpdateField().getFields();
            EsUpdateField.Field field = new EsUpdateField.Field(nameToString(column), val);
            fields.add(field);
        }
        return this;
    }

    @Override
    public EsLambdaUpdateWrapper<T> increment(boolean condition, SFunction<T, ?> column, Long val) {
        if (condition) {
            doIncrement(nameToString(column), val);
        }
        return this;
    }

}
