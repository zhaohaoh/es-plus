
package com.es.plus.web.compile.core;
 
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 */
@Slf4j
@Accessors(chain = true)
@Setter
public class CompilerConfig {

    private static final String COMPILER_CACHE_DIR = "dynamic-compiler";
 
    private int javaVersion = 8;
    private boolean debug = true;
    private boolean parameters = true;
    private boolean suppressWarnings = false;
    private Charset encoding = StandardCharsets.UTF_8;

    // 楂樼骇閰嶇疆
    private boolean enablePreview = false;
    private boolean verbose = false;
    private List<String> additionalOptions = new ArrayList<>();

    @Getter
    private String classpath;

    public CompilerConfig() {
        classpath = getCurrentRuntimeClasspath();
    }

    /**
     * 鑾峰彇杩愯鏃剁被璺緞
     */
    public String getCurrentRuntimeClasspath() {
        try {
            Set<String> classpathEntries = new LinkedHashSet<>();
            File source = findSource(getClass());
            if (source != null && source.exists()) {
                // 浣跨敤绯荤粺缂撳瓨鐩綍
                String userHome = System.getProperty("user.home");
                File cacheBaseDir;
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    cacheBaseDir = new File(userHome, "AppData/Local");
                } else {
                    cacheBaseDir = new File(userHome, ".cache");
                }

                File compilerDir = new File(cacheBaseDir, COMPILER_CACHE_DIR);
                if (compilerDir.exists()) {
                    FileUtils.deleteDirectory(compilerDir);
                }
                compilerDir.mkdirs();

                // 鍒ゆ柇鏄惁涓� IDE 寮€鍙戠幆澧�
                boolean isIdeEnvironment = isIdeEnvironment();

                if (isIdeEnvironment) {
                    // IDE 寮€鍙戠幆澧冧笅浣跨敤绯荤粺绫昏矾寰�
                    String sysClasspath = System.getProperty("java.class.path");
                    
                    log.info("Running in IDE environment, using system classpath");
                    return sysClasspath;
                } else {
                    // 鐢熶骇鐜涓嬩粠 jar 涓彁鍙栦緷璧�
                    classpathEntries.add(source.getAbsolutePath());
                    extractDependencies(source, compilerDir, classpathEntries);
                }
            }

            if (classpathEntries.isEmpty()) {
                log.warn("No classpath entries found, using current directory");
                return ".";
            }

            String classpath = String.join(File.pathSeparator, classpathEntries);
            log.info("Final classpath entries count: {}", classpathEntries.size());
            classpathEntries.forEach(entry -> log.debug("Classpath entry: {}", entry));

            return classpath;

        } catch (Exception e) {
            log.error("Failed to get runtime classpath", e);
            return ".";
        }
    }
    
    private File findSource(Class<?> sourceClass) {
        try {
            ProtectionDomain domain = sourceClass != null ? sourceClass.getProtectionDomain() : null;
            CodeSource codeSource = domain != null ? domain.getCodeSource() : null;
            URL location = codeSource != null ? codeSource.getLocation() : null;
            File source = location != null ? this.findSource(location) : null;
            if (source != null && source.exists() && !this.isUnitTest()) {
                return source.getAbsoluteFile();
            }
        } catch (Exception var6) {
        }
        
        return null;
    }
    
    private boolean isUnitTest() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            for(int i = stackTrace.length - 1; i >= 0; --i) {
                if (stackTrace[i].getClassName().startsWith("org.junit.")) {
                    return true;
                }
            }
        } catch (Exception var3) {
        }
        
        return false;
    }
    private File findSource(URL location) throws IOException, URISyntaxException {
        URLConnection connection = location.openConnection();
        if (connection instanceof JarURLConnection) {
            JarURLConnection jarURLConnection = (JarURLConnection) connection;
            return this.getRootJarFile(jarURLConnection.getJarFile());
        } else {
            return new File(location.toURI());
        }
    }
    private File getRootJarFile(JarFile jarFile) {
        String name = jarFile.getName();
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }
        
        return new File(name);
    }

    /**
     * 鍒ゆ柇鏄惁鍦� IDE 鐜涓繍琛�
     */
    private boolean isIdeEnvironment() {
        // 妫€鏌ユ槸鍚﹀瓨鍦ㄥ父瑙佺殑 IDE 鐩稿叧绯荤粺灞炴€�
        String classPath = System.getProperty("java.class.path");
        String sunJavaCommand = System.getProperty("sun.java.command");

        // IDE 鐜閫氬父浼氭湁寰堥暱鐨� classpath锛屽寘鍚澶氬垎闅旂
        boolean hasMultipleClasspathEntries = classPath != null &&
            classPath.split(File.pathSeparator).length > 5;

        // IDE 鐜閫氬父浼氬湪 sun.java.command 涓寘鍚壒瀹氱殑鏍囪
        boolean hasIdeCommand = sunJavaCommand != null &&
            (sunJavaCommand.contains("idea") || sunJavaCommand.contains("eclipse") ||
             sunJavaCommand.contains("netbeans") || sunJavaCommand.contains("vscode"));

        // 妫€鏌ユ槸鍚﹀瓨鍦� IDE 鐗瑰畾鐨勭郴缁熷睘鎬�
        boolean hasIdeProperties = System.getProperty("idea.launcher.port") != null ||
                                 System.getProperty("eclipse.launcher") != null ||
                                 System.getProperty("netbeans.home") != null;

        return hasMultipleClasspathEntries || hasIdeCommand || hasIdeProperties;
    }


    private void extractDependencies(File jarFile, File tempDir, Set<String> classpathEntries) {
        if (!jarFile.exists() || jarFile.isDirectory()) {
            log.warn("Invalid jar file or directory: {}", jarFile.getAbsolutePath());
            return;
        }

        try (JarFile jar = new JarFile(jarFile)) {
            // 鎻愬彇 BOOT-INF/lib/ 涓嬬殑鎵€鏈� jar
            jar.stream()
                    .filter(entry -> entry.getName().startsWith("BOOT-INF/lib/") && entry.getName().endsWith(".jar"))
                    .forEach(entry -> {
                        try {
                            String jarName = entry.getName().substring("BOOT-INF/lib/".length());
                            File targetJar = new File(tempDir, jarName);

                            if (!targetJar.exists()) {
                                targetJar.getParentFile().mkdirs();
                                try (InputStream in = jar.getInputStream(entry);
                                     FileOutputStream out = new FileOutputStream(targetJar)) {
                                    IOUtils.copy(in,out);
                                }
                                targetJar.deleteOnExit();
                            }

                            classpathEntries.add(targetJar.getAbsolutePath());
                            log.debug("Added dependency: {}", targetJar.getAbsolutePath());

                        } catch (Exception e) {
                            log.warn("Failed to extract jar: {}", entry.getName(), e);
                        }
                    });

            //   BOOT-INF/classes/
            File classesDir = new File(tempDir, "classes");
            classesDir.mkdirs();

            jar.stream()
                    .filter(entry -> entry.getName().startsWith("BOOT-INF/classes/"))
                    .forEach(entry -> {
                        try {
                            String relativePath = entry.getName().substring("BOOT-INF/classes/".length());
                            File targetFile = new File(classesDir, relativePath);

                            if (entry.isDirectory()) {
                                targetFile.mkdirs();
                            } else {
                                targetFile.getParentFile().mkdirs();
                                try (InputStream in = jar.getInputStream(entry);
                                     FileOutputStream out = new FileOutputStream(targetFile)) {
                                     IOUtils.copy(in,out);
//                                    in.transferTo(out);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Failed to extract class file: {}", entry.getName(), e);
                        }
                    });

            classesDir.deleteOnExit();
            classpathEntries.add(classesDir.getAbsolutePath());
            log.debug("Added classes directory: {}", classesDir.getAbsolutePath());

        } catch (Exception e) {
            log.error("Failed to extract dependencies from jar: {}", jarFile, e);
        }
    }

    /**
     * 杞崲涓虹紪璇戝櫒閫夐」
     */
    public List<String> toOptions() {
        List<String> options = new ArrayList<>();

        // java版本
//        options.add("--release");
//        options.add(String.valueOf(javaVersion));
        
        options.add("-source");
        options.add("1.8");
        options.add("-target");
        options.add("1.8");
        if (debug) {
            options.add("-g");
        }
        if (parameters) {
            options.add("-parameters");
        }
        if (suppressWarnings) {
            options.add("-Xlint:none");
        }
        if (enablePreview) {
            options.add("--enable-preview");
            options.add("-source");
            options.add(String.valueOf(javaVersion));
        }
        if (verbose) {
            options.add("-verbose");
        }

        // 编码
        options.add("-encoding");
        options.add(encoding.name());

        // classpath
        options.add("-classpath");
        options.add(classpath);

        // 额外编译参数
        options.addAll(additionalOptions);

        return options;
    }

    /**
     *
     */
    public static CompilerConfig createDefault() {
        CompilerConfig compilerConfig = new CompilerConfig().setJavaVersion(8).setDebug(true).setParameters(true)
                .setSuppressWarnings(false).setEncoding(StandardCharsets.UTF_8);
        return compilerConfig;
    }
}