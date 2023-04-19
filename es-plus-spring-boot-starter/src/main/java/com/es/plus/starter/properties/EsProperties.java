package com.es.plus.starter.properties;

import com.es.plus.adapter.config.GlobalConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2023/2/1 16:52
 */
@Data
@ConfigurationProperties("es-plus")
public class EsProperties {

    /**
     * 集群地址，多个用,隔开
     */
    private String address;

    /**
     * 模式
     */
    private String schema = "http";
    /***
     * 保持心跳时间 timeUnit:millis  单位毫秒  可以解决超时timeout问题.
     * 客户端所有的HTTP连接都为永久保持Keep-Alive。
     * 如果客户端长时间没有发送请求，服务器或者防火墙已经close了HTTP底层的TCP链接，但是此时客户端并不知道，由于Keep Alive是无限期，那么并不会重新建立连接，而是直接发送请求，此时就会得到SocketTimeout异常。
     */
    private Integer keepAliveMillis = 300000;
    /**
     * 用户名称
     */

    private String username;

    /**
     * 密码
     */
    private String password;
    /**
     * maxConnectTotal 最大连接数
     */
    private Integer maxConnTotal;
    /**
     * maxConnectPerRoute 最大连接路由数
     */
    private Integer maxConnPerRoute;
    /**
     * 连接超时时间
     */

    private Integer connectTimeOut = 60;
    /**
     * 连接超时时间
     */

    private Integer socketTimeOut = Integer.MIN_VALUE;
    /**
     * 获取连接的超时时间
     */

    private Integer connectionRequestTimeOut;

    /**
     * 最大连接数
     */
    private Integer maxConnectNum;
    /**
     * 最大路由连接数
     */
    private Integer maxConnectPerRoute;

    /**
     * 多数据源客户端配置  默认上面的是master
     */
    private Map<String, ClientProperties> clientProperties;
    /**
     * 全局配置
     */
    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();

    /**
     * 分词处理器
     */
    @NestedConfigurationProperty
    private Map<String, AnalysisProperties> analysis = new HashMap<>();
    /**
     * keyword处理器
     */
    @NestedConfigurationProperty
    private Map<String, AnalysisProperties> normalizers = new HashMap<>();
}
