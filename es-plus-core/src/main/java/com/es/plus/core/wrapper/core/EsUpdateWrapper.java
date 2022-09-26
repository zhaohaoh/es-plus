package com.es.plus.core.wrapper.core;


import com.es.plus.core.tools.SFunction;
import com.es.plus.pojo.EsUpdateField;

import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsUpdateWrapper<T> extends AbstractEsWrapper<T, SFunction<T, ?>, EsUpdateWrapper<T>> implements Update<EsUpdateWrapper<T>, SFunction<T, ?>> {
    private final EsUpdateField esUpdateField = new EsUpdateField();

    @Override
    public EsUpdateWrapper<T> set(String name, Object value) {
        List<EsUpdateField.Field> fields = esUpdateField.getFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    @Override
    public EsUpdateWrapper<T> increment(String name, Long value) {
        List<EsUpdateField.Field> fields = esUpdateField.getIncrementFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    @Override
    public EsUpdateWrapper<T> setScipt(boolean condition, String scipt, Map<String, Object> sciptParams) {
        if (condition) {
            esUpdateField.setScipt(scipt,sciptParams);
        }
        return this;
    }

    public EsUpdateWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsUpdateWrapper() {
    }

    @Override
    protected EsUpdateWrapper<T> instance() {
        return new EsUpdateWrapper<T>(super.tClass);
    }

    @Override
    public EsUpdateField getEsUpdateField() {
        return esUpdateField;
    }

    @Override
    public EsUpdateWrapper<T> set(SFunction<T, ?> column, Object val) {
        return set(nameToString(column), val);
    }

    @Override
    public EsUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            set(nameToString(column), val);
        }
        return this;
    }

    @Override
    public EsUpdateWrapper<T> increment(boolean condition, SFunction<T, ?> column, Long val) {
        if (condition) {
            increment(nameToString(column), val);
        }
        return this;
    }

}
