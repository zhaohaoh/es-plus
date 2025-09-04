package com.es.plus.core.wrapper.core;


import com.es.plus.common.interceptor.EsUpdateField;
import com.es.plus.common.properties.EsFieldInfo;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.tools.SFunction;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsLambdaUpdateWrapper<T> extends AbstractEsWrapper<T, SFunction<T, ?>, EsLambdaUpdateWrapper<T>>
        implements Update<EsLambdaUpdateWrapper<T>, SFunction<T, ?>, T> {
    
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
        super(tClass);
    }
    
    public EsLambdaUpdateWrapper() {
    }
    
    @Override
    protected EsLambdaUpdateWrapper<T> instance() {
        return new EsLambdaUpdateWrapper<T>(super.tClass);
    }
    
    
    @Override
    public EsUpdateField getEsUpdateField() {
        return super.getEsUpdateField();
    }
    
    @Override
    public EsLambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            return doSet(nameToString(column), val);
        }
        return this;
    }
    
    @Override
    public EsLambdaUpdateWrapper<T> setEntity(boolean condition, T entity) {
        if (condition) {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, field.getName());
                String name = esFieldInfo.getName();
                Object object = null;
                try {
                    field.setAccessible(true);
                    object = field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (object != null) {
                    doSet(name, object);
                }
            }
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
