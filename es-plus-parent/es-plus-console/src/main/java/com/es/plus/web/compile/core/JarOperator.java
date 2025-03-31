package com.es.plus.web.compile.core;


import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * JAR包操作工具类 用于动态加载和操作JAR包
 */
@Slf4j
public class JarOperator {
    
    // JAR包加载器
    private static JarLoader jarLoader;
    
    private static boolean isInitialized = false;
    
    private static final Object INIT_LOCK = new Object();
    
    /**
     * 初始化JAR操作器 该方法由DynamicCodeCompiler调用，不应由其他类直接调用
     */
    static void init() {
        if (isInitialized) {
            return;
        }
        
        synchronized (INIT_LOCK) {
            if (isInitialized) {
                return;
            }
            
            try {
                // 从DynamicCodeCompiler获取共享资源
                DynamicClassLoader classLoader = (DynamicClassLoader) DynamicCodeCompiler.getCurrentClassLoader();
                CompilerConfig compilerConfig = DynamicCodeCompiler.getCompilerConfig();
                
                // 初始化JAR包加载器
                jarLoader = new JarLoader(classLoader, compilerConfig);
                
                isInitialized = true;
                log.info("JAR Operator initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize JAR Operator", e);
                throw new RuntimeException("Failed to initialize JAR Operator", e);
            }
        }
    }
    
    /**
     * 确保已初始化
     */
    private static void ensureInitialized() {
        if (!isInitialized) {
            // 先确保DynamicCodeCompiler已初始化
            DynamicCodeCompiler.ensureInitialized();
            init();
        }
    }
    
    /**
     * 加载JAR包到类路径中
     *
     * @param jarFilePath JAR文件路径
     * @return 是否加载成功
     * @throws Exception 加载过程中可能出现的异常
     */
    public static boolean loadJarFile(String jarFilePath) throws Exception {
        ensureInitialized();
        return jarLoader.loadJarFile(jarFilePath);
    }
    
    /**
     * 批量加载多个JAR包到类路径中
     *
     * @param jarFilePaths JAR文件路径列表
     * @return 成功加载的JAR包数量
     * @throws Exception 加载过程中可能出现的异常
     */
    public static int loadJarFiles(List<String> jarFilePaths) throws Exception {
        ensureInitialized();
        return jarLoader.loadJarFiles(jarFilePaths);
    }
    
    /**
     * 从目录中加载所有JAR包
     *
     * @param directoryPath 包含JAR文件的目录路径
     * @param recursive     是否递归搜索子目录
     * @return 成功加载的JAR包数量
     * @throws Exception 加载过程中可能出现的异常
     */
    public static int loadJarsFromDirectory(String directoryPath, boolean recursive) throws Exception {
        ensureInitialized();
        return jarLoader.loadJarsFromDirectory(directoryPath, recursive);
    }
    
    /**
     * 检查是否已加载特定的JAR包
     *
     * @param jarFilePath JAR文件路径
     * @return 是否已加载
     */
    public static boolean isJarLoaded(String jarFilePath) {
        ensureInitialized();
        return jarLoader.isJarLoaded(jarFilePath);
    }
    
    /**
     * 获取所有已加载的JAR包路径
     *
     * @return 已加载的JAR包路径列表
     */
    public static List<String> getLoadedJarPaths() {
        ensureInitialized();
        return jarLoader.getLoadedJarPaths();
    }
    
    /**
     * 获取当前类路径
     *
     * @return 当前类路径
     */
    public static String getCurrentClasspath() {
        ensureInitialized();
        return DynamicCodeCompiler.getCompilerConfig().getClasspath();
    }
    
    /**
     * 从类路径字符串中解析所有JAR包路径
     *
     * @param classpath 类路径字符串
     * @return JAR包路径列表
     */
    public static List<String> parseJarPathsFromClasspath(String classpath) {
        return JarLoader.parseJarPathsFromClasspath(classpath);
    }
    
    /**
     * 根据包名查找JAR包中的类
     *
     * @param packageName 包名
     * @return 包名下的所有类名
     */
    public static Set<String> findClassesByPackage(String packageName) {
        ensureInitialized();
        return jarLoader.findClassesByPackage(packageName);
    }
    
    /**
     * 获取指定JAR包中的所有类
     *
     * @param jarFilePath JAR文件路径
     * @return JAR包中的所有类名
     */
    public static Set<String> getClassesInJar(String jarFilePath) {
        ensureInitialized();
        return jarLoader.getClassesInJar(jarFilePath);
    }
    
    /**
     * 获取所有已加载JAR包中的所有类
     *
     * @return 所有已加载的类名
     */
    public static Set<String> getAllLoadedClasses() {
        ensureInitialized();
        return jarLoader.getAllLoadedClasses();
    }
    
    /**
     * 扫描指定包名下的所有Spring组件类
     *
     * @param packageName 包名
     * @return Spring组件类名列表
     */
    public static List<String> scanSpringComponents(String packageName) {
        ensureInitialized();
        return jarLoader.scanSpringComponents(packageName);
    }
    
    /**
     * 加载JAR包并扫描Spring组件
     *
     * @param jarFilePath  JAR文件路径
     * @param basePackages 要扫描的基础包名数组
     * @return 找到的Spring组件类名列表
     * @throws Exception 加载过程中可能出现的异常
     */
    public static List<String> loadJarAndScanComponents(String jarFilePath, String... basePackages) throws Exception {
        ensureInitialized();
        return jarLoader.loadJarAndScanComponents(jarFilePath, basePackages);
    }
    
    
    /**
     * 获取类的完整信息
     *
     * @param className 类名
     * @return 类的详细信息
     */
    public static Map<String, Object> getClassInfo(String className) {
        ensureInitialized();
        return jarLoader.getClassInfo(className);
    }
    
    /**
     * 检查JAR包中是否包含指定的类
     *
     * @param jarFilePath JAR文件路径
     * @param className   要检查的类名
     * @return 是否包含该类
     */
    public static boolean containsClass(String jarFilePath, String className) {
        ensureInitialized();
        return jarLoader.containsClass(jarFilePath, className);
    }
    
    /**
     * 根据类名前缀查找类
     *
     * @param prefix 类名前缀
     * @return 匹配的类名列表
     */
    public static List<String> findClassesByPrefix(String prefix) {
        ensureInitialized();
        return jarLoader.findClassesByPrefix(prefix);
    }
    
    /**
     * 根据类名后缀查找类
     *
     * @param suffix 类名后缀
     * @return 匹配的类名列表
     */
    public static List<String> findClassesBySuffix(String suffix) {
        ensureInitialized();
        return jarLoader.findClassesBySuffix(suffix);
    }
    
    /**
     * 获取指定包及其子包的所有包名
     *
     * @param basePackage 基础包名
     * @return 所有相关的包名
     */
    public static Set<String> findSubPackages(String basePackage) {
        ensureInitialized();
        return jarLoader.findSubPackages(basePackage);
    }
    
    /**
     * 获取JAR包的包结构
     *
     * @param jarFilePath JAR文件路径
     * @return 包结构树
     */
    public static Map<String, Object> getPackageStructure(String jarFilePath) {
        ensureInitialized();
        return jarLoader.getPackageStructure(jarFilePath);
    }
    
    /**
     * 获取所有已加载JAR包的包结构
     *
     * @return 所有JAR包的包结构
     */
    public static Map<String, Map<String, Object>> getAllPackageStructures() {
        ensureInitialized();
        return jarLoader.getAllPackageStructures();
    }
    
    /**
     * 获取当前的类加载器
     */
    public static ClassLoader getCurrentClassLoader() {
        ensureInitialized();
        return DynamicCodeCompiler.getCurrentClassLoader();
    }
}