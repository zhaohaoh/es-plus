package com.es.plus.web;


import com.es.plus.web.compile.core.DynamicCodeCompiler;
import com.es.plus.web.manager.SystemTrayManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WebApplication {
    
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        ensureDataDirectoryExists();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        DynamicCodeCompiler.init(contextClassLoader);
        ConfigurableApplicationContext context = SpringApplication.run(WebApplication.class, args);
        SystemTrayManager.initSystemTray(context);
    }
    
    /**
     * 确保数据目录存在
     */
    private static void ensureDataDirectoryExists() {
        String userHome = System.getProperty("user.home");
        java.io.File dataDir = new java.io.File(userHome, ".es-plus-console");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("创建数据目录: " + dataDir.getAbsolutePath());
            } else {
                System.err.println("警告: 无法创建数据目录: " + dataDir.getAbsolutePath());
            }
        }
    }
    
    
}