package com.es.plus.web.compile.core;


import com.es.plus.web.compile.exception.CompilationException;
import com.es.plus.web.compile.util.ClassUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 动态代码编译器
 * 用于动态编译Java源代码
 */
@Slf4j
public class DynamicCodeCompiler {

    // 存储编译后的类
    private static final Map<String, Class<?>> COMPILED_CLASS_CACHE = new ConcurrentHashMap<>();
    private static DynamicClassLoader dynamicClassLoader;
    private static final CompilerConfig COMPILER_CONFIG = CompilerConfig.createDefault();
    private static volatile boolean isInitialized = false;
    private static final Object INIT_LOCK = new Object();
    
    public static CompilerConfig getCompilerConfig() {
        return COMPILER_CONFIG;
    }
    
    /**
     * 初始化编译器
     */
    public static void init(ClassLoader springClassLoader) {
        if (isInitialized) {
            return;
        }

        synchronized (INIT_LOCK) {
            if (isInitialized) {
                return;
            }

            try {
                dynamicClassLoader = new DynamicClassLoader(springClassLoader);
                Thread.currentThread().setContextClassLoader(dynamicClassLoader);

                isInitialized = true;
                log.info("Dynamic compiler initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize dynamic compiler", e);
                throw new RuntimeException("Failed to initialize dynamic compiler", e);
            }
        }
    }


    /**
     * 编译单个类
     */
    public static CompilationResult compile(String className, String sourceCode) throws CompilationException {
        ensureInitialized();

        long startTime = System.currentTimeMillis();
        log.debug("Starting compilation for class: {}", className);

        try {
            // 检查缓存
            Class<?> cachedClass = COMPILED_CLASS_CACHE.get(className);
            if (cachedClass != null) {
//                CompilerStats.recordCacheHit();
                log.debug("Cache hit for class: {}", className);
                return new CompilationResult(cachedClass);
            }

            // 编译类
            Class<?> compiledClass = dynamicClassLoader.compileAndLoad(sourceCode, COMPILER_CONFIG);

            if (compiledClass == null) {
                throw new CompilationException("Compilation failed: null class returned");
            }

            // 缓存编译后的类
            COMPILED_CLASS_CACHE.put(className, compiledClass);

            long compilationTime = System.currentTimeMillis() - startTime;
//            CompilerStats.recordCompilation(compilationTime);

            log.debug("Successfully compiled class {} in {}ms", className, compilationTime);
            return new CompilationResult(compiledClass);

        } catch (Exception e) {
            clearCache(className);
            String errorMsg = String.format("Failed to compile class %s: %s", className, e.getMessage());
            log.error(errorMsg, e);
            throw new CompilationException(errorMsg, e);
        }
    }

    /**
     * 确保编译器已初始化
     */
    public static void ensureInitialized() {
        if (!isInitialized) {
            init(ClassUtils.getDefaultClassLoader());
        }
    }

    /**
     * 批量编译多个类
     * @return Map<类名, 编译结果>
     */
    public static Map<String, CompilationResult> compileMultiple(List<String> sourceCodes) throws CompilationException {
        return compileMultiple(sourceCodes, COMPILER_CONFIG);
    }

    /**
     * 批量编译多个类
     * @return Map<类名, 编译结果>
     */
    public static Map<String, CompilationResult> compileMultiple(List<String> sourceCodes, CompilerConfig compilerConfig) throws CompilationException {
        long startTime = System.currentTimeMillis();
        ensureInitialized();
        try {
            Map<String, Class<?>> compiledClasses = dynamicClassLoader.compileAndLoadMultiple(sourceCodes, compilerConfig);
            long compilationTime = System.currentTimeMillis() - startTime;
//            CompilerStats.recordCompilation(compilationTime);

            // 为每个编译的类创建编译结果
            Map<String, CompilationResult> results = new HashMap<>();
            compiledClasses.forEach((className, compiledClass) -> {
                results.put(className, new CompilationResult(compiledClass));
                // 缓存编译后的类
                COMPILED_CLASS_CACHE.put(className, compiledClass);
            });

            return results;

        } catch (Exception e) {
            log.error("批量编译失败",e);
            throw new CompilationException("批量编译失败: " + e.getMessage());
        }
    }

    /**
     * 清理缓存
     */
    public static void clearCache(String className) {
        COMPILED_CLASS_CACHE.remove(className);
        DynamicClassLoader.removeClassBytes(className);
    }

    /**
     * 清理所有缓存
     */
    public static void clearAllCache() {
        COMPILED_CLASS_CACHE.clear();
        DynamicClassLoader.clearClassBytes();
    }

    /**
     * 获取已编译的类
     */
    public static Class<?> getCompiledClass(String className) {
        return COMPILED_CLASS_CACHE.get(className);
    }

    /**
     * 获取当前的类加载器
     */
    public static ClassLoader getCurrentClassLoader() {
        return dynamicClassLoader;
    }
    
    public static ClassLoader setCurrentClassLoader(ClassLoader classLoader) {
        try {
            return    dynamicClassLoader = new DynamicClassLoader(classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
   
}