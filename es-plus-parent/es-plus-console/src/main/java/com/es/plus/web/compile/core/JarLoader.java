package com.es.plus.web.compile.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * JAR包加载器 用于动态加载JAR包到类路径中
 *
 * @author mahuibo
 * @email mhb0409@qq.com
 * @time 2025-03-13
 */
@Slf4j
public class JarLoader {
    
    // 存储已加载的JAR包路径
    private static final Set<String> LOADED_JAR_PATHS = Collections.synchronizedSet(new HashSet<>());
    
    // 存储JAR包路径到其包含的类的映射
    private static final Map<String, Set<String>> JAR_CLASSES_MAP = new HashMap<>();
    
    // 存储包名到类名的映射
    private static final Map<String, Set<String>> PACKAGE_CLASSES_MAP = new HashMap<>();
    
    // 编译器配置
    private final CompilerConfig compilerConfig;
    
    // 类加载器
    private final ClassLoader classLoader;
    
    
    // 常见的Spring组件注解
    private static final String[] SPRING_ANNOTATIONS = {
        "org.springframework.stereotype.Component", "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository", "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController", "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.autoconfigure.SpringBootApplication"
    };
    
    /**
     * 构造函数
     *
     * @param classLoader    类加载器
     * @param compilerConfig 编译器配置
     */
    public JarLoader(ClassLoader classLoader, CompilerConfig compilerConfig) {
        this.classLoader = classLoader;
        this.compilerConfig = compilerConfig;
    }
    
    /**
     * 加载JAR包到类路径中
     *
     * @param jarFilePath JAR文件路径
     * @return 是否加载成功
     * @throws Exception 加载过程中可能出现的异常
     */
    public boolean loadJarFile(String jarFilePath) throws Exception {
       
        File jarFile =  FileUtils.getFile(jarFilePath);
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
            log.error("无效的JAR文件: {}", jarFilePath);
            return false;
        }
        
        // 检查是否已经加载过该JAR包
        String canonicalPath = jarFile.getCanonicalPath();
        if (LOADED_JAR_PATHS.contains(canonicalPath)) {
            log.info("JAR包已经加载过: {}", canonicalPath);
            return true;
        }
        
        try {
            // 将JAR文件添加到类路径
            URL jarUrl = jarFile.toURI().toURL();
            
            // 首先更新编译器配置的类路径，确保编译时能找到这些类
            String currentClasspath = compilerConfig.getClasspath();
            String newClasspath = currentClasspath + File.pathSeparator + jarFilePath;
            compilerConfig.setClasspath(newClasspath);
            
            // 记录已加载的JAR包路径
            LOADED_JAR_PATHS.add(canonicalPath);
            
            // 尝试使用不同的方法将JAR添加到类加载器
            boolean added = addUrlToClassLoader(classLoader, jarUrl);
            
            if (!added) {
                // 尝试添加到父加载器
                ClassLoader parentLoader = classLoader.getParent();
                added = addUrlToClassLoader(parentLoader, jarUrl);
            }
            
            log.info("成功加载JAR文件到编译器类路径: {}", jarFilePath);
            return true;
        } catch (Exception e) {
            log.error("加载JAR文件失败: {}", jarFilePath, e);
            throw new Exception("加载JAR文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量加载多个JAR包到类路径中
     *
     * @param jarFilePaths JAR文件路径列表
     * @return 成功加载的JAR包数量
     * @throws Exception 加载过程中可能出现的异常
     */
    public int loadJarFiles(List<String> jarFilePaths) throws Exception {
        if (jarFilePaths == null || jarFilePaths.size()<=0) {
            return 0;
        }
        
        int successCount = 0;
        StringBuilder newClasspathBuilder = new StringBuilder(compilerConfig.getClasspath());
        
        for (String jarFilePath : jarFilePaths) {
            File jarFile = FileUtils.getFile(jarFilePath);
            if (!jarFile.exists() || !jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
                log.warn("跳过无效的JAR文件: {}", jarFilePath);
                continue;
            }
            
            // 检查是否已经加载过该JAR包
            String canonicalPath = jarFile.getCanonicalPath();
            if (LOADED_JAR_PATHS.contains(canonicalPath)) {
                log.info("JAR包已经加载过: {}", canonicalPath);
                successCount++;
                continue;
            }
            
            try {
                // 将JAR文件添加到类路径
                URL jarUrl = jarFile.toURI().toURL();
                
                // 添加到类路径字符串
                newClasspathBuilder.append(File.pathSeparator).append(jarFilePath);
                // 记录已加载的JAR包路径
                LOADED_JAR_PATHS.add(canonicalPath);
                
                // 尝试使用不同的方法将JAR添加到类加载器
                boolean added = addUrlToClassLoader(classLoader, jarUrl);
                
                if (!added) {
                    // 尝试添加到父加载器
                    ClassLoader parentLoader = classLoader.getParent();
                    added = addUrlToClassLoader(parentLoader, jarUrl);
                }
                
                successCount++;
                log.info("成功加载JAR文件到编译器类路径: {}", jarFilePath);
                
            } catch (Exception e) {
                log.error("加载JAR文件失败: {}", jarFilePath, e);
            }
        }
        
        // 更新编译器配置的类路径
        compilerConfig.setClasspath(newClasspathBuilder.toString());
        
        return successCount;
    }
    
    /**
     * 从目录中加载所有JAR包
     *
     * @param directoryPath 包含JAR文件的目录路径
     * @param recursive     是否递归搜索子目录
     * @return 成功加载的JAR包数量
     * @throws Exception 加载过程中可能出现的异常
     */
    public int loadJarsFromDirectory(String directoryPath, boolean recursive) throws Exception {
        File directory = FileUtils.getFile(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("无效的目录路径: {}", directoryPath);
            return 0;
        }
        
        List<String> jarFilePaths = new ArrayList <>(); collectJarFiles(directory, jarFilePaths, recursive);
        
        return loadJarFiles(jarFilePaths);
    }
    
    /**
     * 收集目录中的所有JAR文件
     */
    private void collectJarFiles(File directory, List<String> jarFilePaths, boolean recursive) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                jarFilePaths.add(file.getAbsolutePath());
            }
            else if(recursive && file.isDirectory()) {
                collectJarFiles(file, jarFilePaths, true);
            }
        }
    }
    
    /**
     * 尝试将URL添加到类加载器 支持Java 8和Java 9+
     */
    private boolean addUrlToClassLoader(ClassLoader classLoader, URL url) {
        if (classLoader == null) {
            return false;
        }
        
        // 方法1: 如果是 URLClassLoader 实例，使用 addURL 方法
        if (classLoader instanceof URLClassLoader) {
            try {
                Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                addURLMethod.invoke(classLoader, url);
                log.info("成功使用 URLClassLoader.addURL 方法添加 JAR: {}", url);
                return true;
            } catch (Exception e) {
                log.debug("使用 URLClassLoader.addURL 方法添加 JAR 失败: {}", e.getMessage());
            }
        }
        
        // 方法2: 尝试访问类加载器的 ucp 字段 (适用于某些 Java 9+ 类加载器)
        try {
            Field ucpField = null;
            
            // 尝试在当前类或父类中查找 ucp 字段
            Class<?> clazz = classLoader.getClass();
            while (clazz != null) {
                try {
                    ucpField = clazz.getDeclaredField("ucp");
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            
            if (ucpField != null) {
                ucpField.setAccessible(true);
                Object ucp = ucpField.get(classLoader);
                Method addURLMethod = ucp.getClass().getDeclaredMethod("addURL", URL.class);
                addURLMethod.setAccessible(true);
                addURLMethod.invoke(ucp, url);
                log.info("成功使用 UCP 字段添加 JAR: {}", url);
                return true;
            }
        } catch (Exception e) {
            log.debug("使用 UCP 字段添加 JAR 失败: {}", e.getMessage());
        }
        
        // 方法3: 对于 DynamicClassLoader，直接添加类字节码
        if (classLoader instanceof DynamicClassLoader) {
            try {
                // 注意：这种方法只能添加类，不能添加资源文件
                log.info("检测到 DynamicClassLoader，将通过其他方式处理 JAR: {}", url);
                // 尝试从 JAR 中提取类并加载
                extractAndLoadClassesFromJar(url);
                // 我们不直接加载类，而是更新编译器类路径，让编译时能找到这些类
                return true;
            } catch (Exception e) {
                log.debug("处理 DynamicClassLoader 失败: {}", e.getMessage());
            }
        }
        
        // 方法4: 创建新的类加载器作为子加载器
        try {
            // 创建一个新的 URLClassLoader 作为当前类加载器的子加载器
            URLClassLoader childLoader = new URLClassLoader(new URL[]{url}, classLoader);
            
            // 将这个类加载器设置为线程上下文类加载器
            // 注意：这只会影响当前线程，不会影响其他线程
            Thread.currentThread().setContextClassLoader(childLoader);
            log.info("已创建子类加载器并设置为线程上下文类加载器: {}", url);
            return true;
        } catch (Exception e) {
            log.debug("创建子类加载器失败: {}", e.getMessage());
        }
        
        // 如果所有方法都失败，我们仍然可以通过更新编译器类路径来使编译时能找到这些类
        log.warn("无法将 JAR 添加到类加载器，但已更新编译器类路径配置: {}", url);
        return false;
    }
    
    /**
     * 从 JAR 文件中提取类并加载到 DynamicClassLoader 中
     *
     * @param jarUrl      JAR 文件的 URL
     * @param classLoader 目标类加载器
     */
    private void extractAndLoadClassesFromJar(URL jarUrl) {
        try {
            File jarFile = FileUtils.toFile(jarUrl);
            String jarPath = jarFile.getCanonicalPath();
            Set<String> classesInJar = new HashSet <>();
            
            // 打开 JAR 文件
            try (JarFile jar = new JarFile(jarFile)){
                // 遍历 JAR 文件中的所有条目
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    
                    // 只处理类文件
                    if (entryName.endsWith(".class")) {
                        // 将路径转换为类名
                        String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                        classesInJar.add(className);
                        
                        // 更新包名到类名的映射
                        int lastDotIndex = className.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            String packageName = className.substring(0, lastDotIndex);
                            PACKAGE_CLASSES_MAP.computeIfAbsent(packageName, k -> new HashSet < > ()).add(className);
                        }
                        
                        try {
                            String string = entry.toString();
                            // 读取类文件的字节码
                            try (java.io.InputStream is = jar.getInputStream(entry)){
                                byte[] classBytes = IOUtils.toByteArray(is);
//                                String read = IoUtil.read(is, StandardCharsets.UTF_8);
                                
                                // 将类字节码添加到 DynamicClassLoader
                                DynamicClassLoader.addClassBytes(className, classBytes);
                                log.debug("已将类 {} 添加到 DynamicClassLoader", className);
                            }
                        } catch (Exception e) {
                            log.debug("无法加载类 {}: {}", className, e.getMessage());
                        }
                    }
                }
                
                // 更新JAR包到类的映射
                JAR_CLASSES_MAP.put(jarPath, classesInJar);
                log.info("已从 JAR 文件中提取并加载类: {}, 共 {} 个类", jarUrl, classesInJar.size());
            }
        } catch (Exception e) {
            log.warn("从 JAR 提取类失败: {}", e.getMessage());
        }
    }
    
    /**
     * 根据包名查找JAR包中的类
     *
     * @param packageName 包名
     * @return 包名下的所有类名
     */
    public Set<String> findClassesByPackage(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return Collections.emptySet();
        }
        
        Set<String> result = new HashSet <>();
        
        // 精确匹配该包名
        Set<String> exactMatch = PACKAGE_CLASSES_MAP.getOrDefault(packageName, Collections.emptySet());
        result.addAll(exactMatch);
        
        // 查找所有子包
        String prefix = packageName + ".";
        for (Map.Entry<String, Set<String>> entry : PACKAGE_CLASSES_MAP.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * 获取指定JAR包中的所有类
     *
     * @param jarFilePath JAR文件路径
     * @return JAR包中的所有类名
     */
    public Set<String> getClassesInJar(String jarFilePath) {
        try {
            File jarFile = FileUtils.getFile(jarFilePath);
            if (!jarFile.exists() || !jarFile.isFile()) {
                return Collections.emptySet();
            }
            
            String canonicalPath = jarFile.getCanonicalPath();
            if (!LOADED_JAR_PATHS.contains(canonicalPath)) {
                log.warn("JAR包尚未加载: {}", jarFilePath);
                return Collections.emptySet();
            }
            
            return JAR_CLASSES_MAP.getOrDefault(canonicalPath, Collections.emptySet());
        } catch (Exception e) {
            log.error("获取JAR包中的类失败: {}", jarFilePath, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * 获取所有已加载JAR包中的所有类
     *
     * @return 所有已加载的类名
     */
    public Set<String> getAllLoadedClasses() {
        Set<String> allClasses = new HashSet <>(); for (Set<String> classes : JAR_CLASSES_MAP.values()) {
            allClasses.addAll(classes);
        }
        return allClasses;
    }
    
    /**
     * 扫描指定包名下的所有Spring组件类
     *
     * @param packageName 包名
     * @return Spring组件类名列表
     */
    public List<String> scanSpringComponents(String packageName) {
        Set<String> classesInPackage = findClassesByPackage(packageName);
        List<String> springComponents = new ArrayList <>();
        
        for (String className : classesInPackage) {
            try {
                // 加载类
                Class<?> clazz = classLoader.loadClass(className);
                
                // 检查是否有Spring注解
                if (isSpringComponent(clazz)) {
                    springComponents.add(className);
                    log.debug("找到Spring组件: {}", className);
                }
            } catch (Exception e) {
                log.debug("无法检查类 {}: {}", className, e.getMessage());
            }
        }
        
        return springComponents;
    }
    
    /**
     * 检查类是否是Spring组件
     *
     * @param clazz 要检查的类
     * @return 是否是Spring组件
     */
    private boolean isSpringComponent(Class<?> clazz) {
        // 检查常见的Spring注解
        for (String annotationName : SPRING_ANNOTATIONS) {
            try {
                if (hasAnnotation(clazz, annotationName)) {
                    return true;
                }
            } catch (Exception e) {
                // 忽略找不到注解类的异常
            }
        }
        return false;
    }
    
    /**
     * 检查类是否有指定的注解
     *
     * @param clazz               要检查的类
     * @param annotationClassName 注解类名
     * @return 是否有该注解
     */
    private boolean hasAnnotation(Class<?> clazz, String annotationClassName) {
        try {
            // 尝试加载注解类
            Class<?> annotationClass = classLoader.loadClass(annotationClassName);
            
            // 检查类上是否有该注解
            return clazz.isAnnotationPresent((Class) annotationClass);
        } catch (ClassNotFoundException e) {
            // 忽略找不到注解类的异常
            return false;
        }
    }
    
    
    /**
     * 加载JAR包并扫描Spring组件
     *
     * @param jarFilePath  JAR文件路径
     * @param basePackages 要扫描的基础包名数组
     * @return 找到的Spring组件类名列表
     * @throws Exception 加载过程中可能出现的异常
     */
    public List<String> loadJarAndScanComponents(String jarFilePath, String... basePackages) throws Exception {
        // 先加载JAR包
        boolean loaded = loadJarFile(jarFilePath);
        if (!loaded) {
            return Collections.emptyList();
        }
        
        List<String> components = new ArrayList <>();
        
        // 如果没有指定包名，则扫描整个JAR
        if (basePackages == null || basePackages.length == 0) {
            Set<String> allClasses = getClassesInJar(jarFilePath);
            for (String className : allClasses) {
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (isSpringComponent(clazz)) {
                        components.add(className);
                    }
                } catch (Exception e) {
                    log.debug("无法检查类 {}: {}", className, e.getMessage());
                }
            }
        }else {
            // 扫描指定的包
            for (String basePackage : basePackages) {
                components.addAll(scanSpringComponents(basePackage));
            }
        }
        
        return components;
    }
    
    /**
     * 获取类的完整信息
     *
     * @param className 类名
     * @return 类的详细信息
     */
    public Map<String, Object> getClassInfo(String className) {
        Map<String, Object> info = new HashMap<>();
        try {
            Class<?> clazz = classLoader.loadClass(className);
            info.put("name", clazz.getName());
            info.put("simpleName", clazz.getSimpleName());
            info.put("package", clazz.getPackage() != null ? clazz.getPackage().getName() : "");
            info.put("isInterface", clazz.isInterface());
            info.put("isAbstract", java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()));
            info.put("superclass", clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : null);
            
            // 获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = interfaces[i].getName();
            }
            info.put("interfaces", interfaceNames);
            
            // 获取注解
            List<String> annotations = new ArrayList <>();
            for (java.lang.annotation.Annotation annotation : clazz.getAnnotations()) {
                annotations.add(annotation.annotationType().getName());
            }
            info.put("annotations", annotations);
            
        } catch (ClassNotFoundException e) {
            info.put("error", "类未找到: " + e.getMessage());
        } catch (Exception e) {
            info.put("error", "获取类信息失败: " + e.getMessage());
        } return info;
    }
    
    /**
     * 检查是否已加载特定的JAR包
     *
     * @param jarFilePath JAR文件路径
     * @return 是否已加载
     */
    public boolean isJarLoaded(String jarFilePath) {
        try {
            File jarFile = FileUtils.getFile(jarFilePath);
            if (!jarFile.exists() || !jarFile.isFile()) {
                return false;
            }
            return LOADED_JAR_PATHS.contains(jarFile.getCanonicalPath());
        } catch (Exception e) {
            log.error("检查JAR包是否已加载时出错: {}", jarFilePath, e);
            return false;
        }
    }
    
    /**
     * 获取所有已加载的JAR包路径
     *
     * @return 已加载的JAR包路径列表
     */
    public List<String> getLoadedJarPaths() {
        return new ArrayList<> (LOADED_JAR_PATHS);
    }
    
    /**
     * 获取当前类路径
     *
     * @return 当前类路径
     */
    public String getCurrentClasspath() {
        return compilerConfig.getClasspath();
    }
    
    /**
     * 从类路径字符串中解析所有JAR包路径
     *
     * @param classpath 类路径字符串
     * @return JAR包路径列表
     */
    public static List<String> parseJarPathsFromClasspath(String classpath) {
        if (StringUtils.isBlank(classpath)) {
            return Collections.emptyList();
        }
        
        List<String> jarPaths = new ArrayList <>(); String[] paths = StringUtils.split(classpath, File.pathSeparator);
        
        for (String path : paths) {
            if (StringUtils.endsWithIgnoreCase(path, ".jar")) {
                jarPaths.add(path);
            }
        }
        
        return jarPaths;
    }
    
    
    /**
     * 检查JAR包中是否包含指定的类
     *
     * @param jarFilePath JAR文件路径
     * @param className   要检查的类名
     * @return 是否包含该类
     */
    public boolean containsClass(String jarFilePath, String className) {
        Set<String> classes = getClassesInJar(jarFilePath);
        return classes.contains(className);
    }
    
    /**
     * 根据类名前缀查找类
     *
     * @param prefix 类名前缀
     * @return 匹配的类名列表
     */
    public List<String> findClassesByPrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return Collections.emptyList();
        }
        
        List<String> matchedClasses = new ArrayList <>(); for (Set<String> classes : JAR_CLASSES_MAP.values()) {
            for (String className : classes) {
                if (className.startsWith(prefix)) {
                    matchedClasses.add(className);
                }
            }
        }
        
        return matchedClasses;
    }
    
    /**
     * 根据类名后缀查找类
     *
     * @param suffix 类名后缀
     * @return 匹配的类名列表
     */
    public List<String> findClassesBySuffix(String suffix) {
        if (StringUtils.isBlank(suffix)) {
            return Collections.emptyList();
        }
        
        List<String> matchedClasses = new ArrayList <>(); for (Set<String> classes : JAR_CLASSES_MAP.values()) {
            for (String className : classes) {
                if (className.endsWith(suffix)) {
                    matchedClasses.add(className);
                }
            }
        }
        
        return matchedClasses;
    }
    
    /**
     * 获取指定包及其子包的所有包名
     *
     * @param basePackage 基础包名
     * @return 所有相关的包名
     */
    public Set<String> findSubPackages(String basePackage) {
        if (StringUtils.isBlank(basePackage)) {
            return new HashSet<> (PACKAGE_CLASSES_MAP.keySet());
        }
        
        Set<String> result = new HashSet <>();
        
        // 添加精确匹配的包
        if (PACKAGE_CLASSES_MAP.containsKey(basePackage)) {
            result.add(basePackage);
        }
        
        // 添加所有子包
        String prefix = basePackage + ".";
        for (String packageName : PACKAGE_CLASSES_MAP.keySet()) {
            if (packageName.startsWith(prefix)) {
                result.add(packageName);
            }
        }
        
        return result;
    }
    
    /**
     * 获取JAR包的包结构
     *
     * @param jarFilePath JAR文件路径
     * @return 包结构树
     */
    public Map<String, Object> getPackageStructure(String jarFilePath) {
        try {
            File jarFile = FileUtils.getFile(jarFilePath);
            if (!jarFile.exists() || !jarFile.isFile()) {
                return Collections.emptyMap();
            }
            
            String canonicalPath = jarFile.getCanonicalPath();
            if (!LOADED_JAR_PATHS.contains(canonicalPath)) {
                log.warn("JAR包尚未加载: {}", jarFilePath);
                return Collections.emptyMap();
            }
            
            // 获取JAR包中的所有类
            Set<String> classes = JAR_CLASSES_MAP.getOrDefault(canonicalPath, Collections.emptySet());
            
            // 构建包结构树
            Map<String, Object> root = new HashMap<>();
            
            for (String className : classes) {
                // 分割类名，获取包路径
                String[] parts = StringUtils.split(className, '.');
                Map<String, Object> current = root;
                
                // 构建包路径
                for (int i = 0; i < parts.length - 1; i++) {
                    String part = parts[i];
                    if (!current.containsKey(part)) {
                        current.put(part, new HashMap<>());
                    }
                    current = (Map<String, Object>) current.get(part);
                }
                
                // 添加类名
                String simpleName = parts[parts.length - 1];
                if (!current.containsKey("classes")) {
                    current.put("classes", new ArrayList <String> ());
                } ((List<String>) current.get("classes")).add(simpleName);
            }
            
            return root;
        } catch (Exception e) {
            log.error("获取JAR包结构失败: {}", jarFilePath, e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 获取所有已加载JAR包的包结构
     *
     * @return 所有JAR包的包结构
     */
    public Map<String, Map<String, Object>> getAllPackageStructures() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        for (String jarPath : LOADED_JAR_PATHS) {
            result.put(jarPath, getPackageStructure(jarPath));
        }
        
        return result;
    }
}