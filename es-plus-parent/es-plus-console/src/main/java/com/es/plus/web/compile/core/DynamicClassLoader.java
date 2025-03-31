package com.es.plus.web.compile.core;


import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态类加载器
 * 用于动态编译和加载Java源代码
 *
 * @author mahuibo
 * @email mhb0409@qq.com
 * @time 2025-01-16
 */
public class DynamicClassLoader extends ClassLoader {
    private static final Map<String, byte[]> classBytesMap = new ConcurrentHashMap<>();
    private final JavaCompiler compiler;
    private final StandardJavaFileManager standardFileManager;

    public DynamicClassLoader(ClassLoader parent) throws IOException {
        super(parent);
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.standardFileManager = compiler.getStandardFileManager(null, null, null);
//        standardFileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(new File("cc.aicats.nblowcode.generator")));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classBytesMap.get(name);
        if (classBytes != null) {
            // 确保包名存在
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                String packageName = name.substring(0, lastDot);
                if (getPackage(packageName) == null) {
                    definePackage(packageName, null, null, null, null, null, null, null);
                }
            }
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }

    public Class<?> compileAndLoad(String sourceCode, CompilerConfig config) throws Exception {
        String className = extractClassName(sourceCode);
        JavaFileObject sourceFile = createSourceFileObject(className, sourceCode);

        Map<String, Class<?>> result = compileAndLoadClasses(
            Collections.singletonList(sourceFile),
            Collections.singletonMap(className, sourceCode),
            config
        );

        return result.get(className);
    }

    /**
     * 批量编译和加载多个类
     * @return Map<类名, 编译后的类>
     */
    public Map<String, Class<?>> compileAndLoadMultiple(List<String> sourceCodes, CompilerConfig config) throws Exception {
        List<JavaFileObject> compilationUnits = new ArrayList<>();
        Map<String, String> classNameMap = new HashMap<>();

        // 准备所有源文件
        for (String sourceCode : sourceCodes) {
            String className = extractClassName(sourceCode);
            classNameMap.put(className, sourceCode);
            compilationUnits.add(createSourceFileObject(className, sourceCode));
        }

        return compileAndLoadClasses(compilationUnits, classNameMap, config);
    }

    /**
     * 核心编译和加载逻辑
     * @param compilationUnits 编译单元列表
     * @param classNameMap 类名到源码的映射
     * @param config 编译配置
     * @return 编译和加载后的类映射
     */
    private Map<String, Class<?>> compileAndLoadClasses(
            List<JavaFileObject> compilationUnits,
            Map<String, String> classNameMap,
            CompilerConfig config) throws Exception {

        try (MemoryJavaFileManager fileManager = new MemoryJavaFileManager(standardFileManager)) {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            
            List<String> options = config.toOptions();
            JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics, options,
                null,
                compilationUnits
            );
         
            if (!task.call()) {
               
                System.out.println("编译失败 :"+diagnostics.getDiagnostics());
            }
    
            // 保存编译后的字节码
            Map<String, byte[]> compiledClasses = fileManager.getClassBytes();
            classBytesMap.putAll(compiledClasses);

            // 加载所有编译后的类
            Map<String, Class<?>> loadedClasses = new HashMap<>();
            for (String className : classNameMap.keySet()) {
              
                Class<?> value =  this.loadClass(className);
                loadedClasses.put(className, value);
            }

            return loadedClasses;
        }
    }

    private JavaFileObject createSourceFileObject(String className, String sourceCode) {
        return new SimpleJavaFileObject(
            URI.create("string:///" + className.replace('.', '/') + ".java"),
            JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };
    }

    private String extractClassName(String sourceCode) {
        // 从源代码中提取包名和类名
        String packagePattern = "package\\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\s*;";
        String classPattern = "(?:public\\s+)?(?:class|interface)\\s+([a-zA-Z_][a-zA-Z0-9_]*)";

        Pattern pkgPattern = Pattern.compile(packagePattern);
        Pattern clsPattern = Pattern.compile(classPattern);

        Matcher pkgMatcher = pkgPattern.matcher(sourceCode);
        Matcher clsMatcher = clsPattern.matcher(sourceCode);

        String packageName = pkgMatcher.find() ? pkgMatcher.group(1) : "";
        if (!clsMatcher.find()) {
            throw new IllegalArgumentException("Could not find class name in source code");
        }
        String className = clsMatcher.group(1);

        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    public static void addClassBytes(String name, byte[] bytes) {
        classBytesMap.put(name, bytes);
    }

    public static void removeClassBytes(String className) {
        classBytesMap.remove(className);
    }

    public static void clearClassBytes() {
        classBytesMap.clear();
    }
    public static Map<String, byte[]> getClassBytesMap() {
       return classBytesMap ;
    }
    
}