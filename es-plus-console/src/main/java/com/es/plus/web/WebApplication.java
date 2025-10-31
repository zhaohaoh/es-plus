package com.es.plus.web;


import com.es.plus.web.compile.core.DynamicCodeCompiler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApplication {
    
    public static void main(String[] args) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        DynamicCodeCompiler.init(contextClassLoader);
        SpringApplication.run(WebApplication.class, args);
    }
    
    
}