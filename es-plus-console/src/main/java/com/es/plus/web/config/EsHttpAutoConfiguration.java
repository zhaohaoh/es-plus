package com.es.plus.web.config;

import com.es.plus.web.http.EsHttpClient;
import com.es.plus.web.http.HttpClientConfig;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Elasticsearch HTTP客户端自动配置类
 */
@Configuration
public class EsHttpAutoConfiguration {

    private final EsHttpProperties properties;

    public EsHttpAutoConfiguration(EsHttpProperties properties) {
        this.properties = properties;
    }

    /**
     * HTTP客户端配置Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpClientConfig httpClientConfig() {
        HttpClientConfig config = new HttpClientConfig();
        config.setConnectTimeout(properties.getConnectTimeout());
        config.setReadTimeout(properties.getReadTimeout());
        config.setWriteTimeout(properties.getWriteTimeout());
        config.setMaxIdleConnections(properties.getMaxIdleConnections());
        config.setKeepAliveDuration(properties.getKeepAliveDuration());
        config.setLogLevel(properties.getLogLevel());
        return config;
    }

    /**
     * OkHttp客户端Bean（可选，供高级用户使用）
     */
    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(HttpClientConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .connectionPool(config.createConnectionPool())
                .retryOnConnectionFailure(true)
                .addInterceptor(config.createLoggingInterceptor());

       
        return builder.build();
    }

    /**
     * ES HTTP客户端Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public EsHttpClient esHttpClient(HttpClientConfig config) {
        return new EsHttpClient(config);
    }

    /**
     * Gzip请求拦截器
     */
    private static class GzipRequestInterceptor implements okhttp3.Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            okhttp3.Request originalRequest = chain.request();
            okhttp3.Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .header("Accept-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            return chain.proceed(compressedRequest);
        }

        private okhttp3.RequestBody gzip(final okhttp3.RequestBody body) {
            return new okhttp3.RequestBody() {
                @Override
                public okhttp3.MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() throws java.io.IOException {
                    return -1; // 无法知道压缩后的长度
                }

                @Override
                public void writeTo(okio.BufferedSink sink) throws java.io.IOException {
                    okio.BufferedSink gzipSink = okio.Okio.buffer(new okio.GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }
}