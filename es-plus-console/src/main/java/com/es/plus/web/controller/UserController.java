package com.es.plus.web.controller;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.es.plus.web.config.EsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/")
public class UserController {
    
    @Value("${admin.password:es123456}")
    private String adminpassword;
    
    @RequestMapping("doLogin")
    public SaTokenInfo doLogin(String username, String password) {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if ("admin".equals(username) && adminpassword.equals(password)) {
            StpUtil.login("1");
            // 第2步，获取 Token  相关参数
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return tokenInfo;
        }
        
        if ("dev".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return tokenInfo;
        }
        throw new EsException("登录失败");
    }
    
    @RequestMapping("doLogout")
    public void doLogout() {
        StpUtil.logout();
    }
    
}