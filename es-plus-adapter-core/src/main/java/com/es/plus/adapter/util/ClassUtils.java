package com.es.plus.adapter.util;


import com.es.plus.adapter.exception.EsException;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;


public class ClassUtils {

    private static final Map<Class<?>, List<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();
    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList("net.sf.cglib.proxy.Factory"
            // cglib
            , "org.springframework.cglib.proxy.Factory"
            , "javassist.util.proxy.ProxyObject"
            // javassist
            , "org.apache.ibatis.javassist.util.proxy.ProxyObject");


    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * 获取当前对象,如果是代理的获取本身
     */
    public static Class<?> getClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

    /**
     * 获取该类的所有属性列表
     *
     * @param clazz 类
     * @return 所有属性列表
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        clazz = getClass(clazz);
        List<Field> fields = CLASS_FIELD_CACHE.get(clazz);
        if (CollectionUtils.isEmpty(fields)) {
            synchronized (CLASS_FIELD_CACHE) {
                fields = doGetFieldList(clazz);
                CLASS_FIELD_CACHE.put(clazz, fields);
            }
        }
        return fields;
    }

    private static List<Field> doGetFieldList(Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            List<Field> fieldList = Stream.of(clazz.getDeclaredFields())
                    /* 过滤静态属性 */
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    /* 过滤 transient关键字修饰的属性 */
                    .filter(field -> !Modifier.isTransient(field.getModifiers()))
                    .collect(toCollection(LinkedList::new));
            /* 处理父类字段 */
            Class<?> superClass = clazz.getSuperclass();
            /* 排除重载属性 */
            return excludeOverrideSuperField(fieldList, getFieldList(superClass));
        } else {
            return Collections.emptyList();
        }
    }

    public static List<Field> excludeOverrideSuperField(List<Field> fieldList, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = fieldList.stream().collect(toMap(Field::getName, identity()));
        superFieldList.stream().filter(field -> !fieldMap.containsKey(field.getName())).forEach(fieldList::add);
        return fieldList;
    }

    public static Class<?> toClassConfident(String name) {
        try {
            return Class.forName(name, false, getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new EsException("es ClassUtils class not found", e);
            }
        }
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
}
