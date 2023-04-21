package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsUpdateField;

import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsUpdateWrapper<T> extends AbstractEsWrapper<T, String, EsUpdateWrapper<T>> implements Update<EsUpdateWrapper<T>,String> {


    public EsUpdateWrapper<T> doSet(String name, Object value) {
        List<EsUpdateField.Field> fields = getEsUpdateField().getFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    public EsUpdateWrapper<T> doIncrement(String name, Long value) {
        List<EsUpdateField.Field> fields = getEsUpdateField().getIncrementFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }

    @Override
    public EsUpdateWrapper<T> setScipt(boolean condition, String scipt, Map<String, Object> sciptParams) {
        if (condition) {
            getEsUpdateField().setScipt(scipt,sciptParams);
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
        return super.getEsUpdateField();
    }

    @Override
    public EsUpdateWrapper<T> set(String column, Object val) {
        return doSet(column, val);
    }

    @Override
    public EsUpdateWrapper<T> set(boolean condition, String column, Object val) {
        if (condition) {
            doSet(column, val);
        }
        return this;
    }

    @Override
    public EsUpdateWrapper<T> increment(boolean condition, String column, Long val) {
        if (condition) {
            doIncrement(column, val);
        }
        return this;
    }

}
