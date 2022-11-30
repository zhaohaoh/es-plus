package com.es.plus.config;

import com.es.plus.exception.EsException;
import com.es.plus.pojo.EsUpdateField;
import com.es.plus.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * @Author: hzh
 * @Date: 2022/9/8 11:50
 *  本来希望做成自动注入字段 未使用
 */
public interface EsObjectHandler {

    EsUpdateField.Field insertFill();

    EsUpdateField.Field updateFill();

    default Object setUpdateFeild(Object object) {
        EsUpdateField.Field updateFill = updateFill();
        if (updateFill == null) {
            return object;
        }
        List<Field> fieldList = ClassUtils.getFieldList(object.getClass());
        Map<String, Field> fieldMap = fieldList.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
        Field field = fieldMap.get(updateFill.getName());
        field.setAccessible(true);
        try {
            field.set(object, updateFill.getValue());
        } catch (IllegalAccessException e) {
            throw new EsException(e);
        }
        return object;
    }

    default Object setInsertFeild(Object object) {
        EsUpdateField.Field insertFill = insertFill();
        if (insertFill == null) {
            return object;
        }
        List<Field> fieldList = ClassUtils.getFieldList(object.getClass());
        Map<String, Field> fieldMap = fieldList.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
        Field field = fieldMap.get(insertFill.getName());
        field.setAccessible(true);
        try {
            field.set(object, insertFill.getValue());
        } catch (IllegalAccessException e) {
            throw new EsException(e);
        }
        return object;
    }
}
