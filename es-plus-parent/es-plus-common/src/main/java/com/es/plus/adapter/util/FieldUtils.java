package com.es.plus.adapter.util;

import java.lang.reflect.Field;

public class FieldUtils {

    public static String getStrFieldValue(Object bean, String... field) {
        for (int i = 0; i < field.length; i++) {
            if (bean != null) {
                bean = getFieldValue(bean, field[i]);
            }
        }
        return (String) bean;
    }

    public static Object getFieldValue(Object bean, String field) {
        Class<?> aClass = bean.getClass();
        Field declaredField = null;
        try {
            declaredField = aClass.getDeclaredField(field);
            declaredField.setAccessible(true);
            return declaredField.get(bean);
        } catch (Exception e) {
            return null;
        }
    }


}
