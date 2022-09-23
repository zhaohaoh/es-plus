package com.es.plus.util;

import java.lang.reflect.Field;

public class FieldUtils {

    public static String getStrFieldValue(Object bean, String field) {
        Class<?> aClass = bean.getClass();
        Field declaredField = null;
        try {
            declaredField = aClass.getDeclaredField(field);
            declaredField.setAccessible(true);
            return (String) declaredField.get(bean);
        } catch (Exception e) {
            return null;
        }
    }

}
