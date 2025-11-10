package com.es.plus.web.http;

import okhttp3.ConnectionPool;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端配置类
 * 封装OkHttp的各种配置参数
 */
@Component
public class HttpClientConfig {

    private int connectTimeout = 30;
    private int readTimeout = 60;
    private int writeTimeout = 60;
    private int maxIdleConnections = 20;
    private long keepAliveDuration = 5;
    private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BASIC;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public HttpLoggingInterceptor.Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(HttpLoggingInterceptor.Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 创建连接池
     */
    public ConnectionPool createConnectionPool() {
        return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES);
    }

    /**
     * 创建日志拦截器
     */
    public HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(logLevel);
        return interceptor;
    }
}