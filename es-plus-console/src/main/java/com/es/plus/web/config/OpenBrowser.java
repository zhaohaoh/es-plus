package com.es.plus.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OpenBrowser implements CommandLineRunner {
 
    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始自动加载指定的页面");
        try {
            Runtime.getRuntime().exec("cmd   /c   start   http://localhost:8080/");//可以指定自己的路径
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}