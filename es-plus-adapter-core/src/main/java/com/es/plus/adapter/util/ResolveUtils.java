package com.es.plus.adapter.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
/**
 * @Author: hzh
 * @Date: 2022/1/21 11:12
 */
public class ResolveUtils {

    /**
     * 判断是否是基础数据类型，即 int,double,long等类似格式
     */
    public static boolean isCommonDataType(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    public static boolean isDate(Class<?> clazz) {
        return clazz.equals(LocalDateTime.class) || clazz.equals(LocalDate.class) || clazz.equals(Date.class);
    }

    /**
     * 判断是否是基础数据类型的包装类型
     */
    public static boolean isWrapClass(Class<?> clz) {
        try {
            return clz.equals(String.class) || ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
