package com.es.plus.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
@SuppressWarnings({"unchecked"})
public interface ScrollHandler<T> {
    void handler(List<T> result);

    default boolean support(Class<T> tClass) {
        Type[] genericSuperclass1 = this.getClass().getGenericInterfaces();
        ParameterizedType genericSuperclass = (ParameterizedType) genericSuperclass1[0];
        Class<T> clazz = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        return tClass.equals(clazz);
    }
}
