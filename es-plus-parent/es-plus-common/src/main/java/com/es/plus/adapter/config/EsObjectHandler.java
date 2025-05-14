package com.es.plus.adapter.config;

import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.interceptor.EsUpdateField;
import com.es.plus.adapter.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2022/9/8 11:50 本来希望做成自动注入字段 未使用
 */
public interface EsObjectHandler {
    
    default EsUpdateField.Field insertFill(){
        return null;
    };
    
    default EsUpdateField.Field updateFill(){
        return null;
    };
    
    default String index() {
        return "global";
    }
    
    
    default Object setUpdateFeild(Object object) {
        EsUpdateField.Field updateFill = updateFill();
        if (updateFill == null) {
            return object;
        }
        try {
            List<Field> fieldList = ClassUtils.getFieldList(object.getClass());
            Map<String, Field> fieldMap = fieldList.stream()
                    .collect(Collectors.toMap(Field::getName, Function.identity()));
            Field field = fieldMap.get(updateFill.getName());
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                field.set(object, updateFill.getValue());
            }
        } catch (Exception e) {
            throw new EsException(e);
        }
        return object;
    }
    
    default Object setInsertFeild(Object object) {
        EsUpdateField.Field insertFill = insertFill();
        if (insertFill == null) {
            return object;
        }
        try {
            List<Field> fieldList = ClassUtils.getFieldList(object.getClass());
            Map<String, Field> fieldMap = fieldList.stream()
                    .collect(Collectors.toMap(Field::getName, Function.identity()));
            Field field = fieldMap.get(insertFill.getName());
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                field.set(object, insertFill.getValue());
            }
        } catch (Exception e) {
            throw new EsException(e);
        }
        return object;
    }
}
