package com.es.plus.web.controller;

import com.es.plus.web.compile.core.DynamicClassLoader;

import java.util.Map;

/**
 * 自定义类加载器，用于加载动态编译的类
 */
public class CustomClassLoader extends ClassLoader {
    // 存储类名与字节码的映射
    
    /**
     * 构造函数
     * @param parent 父类加载器（通常为当前线程的上下文类加载器）
     * @param classBytes 类名与字节码的映射表
     */
    public CustomClassLoader(ClassLoader parent) {
       
        super(parent); // 设置父类加载器
        
    }
    
    /**
     * 重写findClass方法，用于查找和加载类
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 1. 先从映射表中查找字节码
        Map<String, byte[]> classBytesMap = DynamicClassLoader.getClassBytesMap();
        byte[] bytes = classBytesMap.get(name);
        // 2. 如果找到字节码，则定义类
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        
        // 3. 如果未找到，则委托给父类加载器
        return super.findClass(name);
    }
    
    
}