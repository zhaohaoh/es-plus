package com.es.plus.starter.auto;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.exception.EsException;
import com.es.plus.pojo.ClientContext;
import com.es.plus.starter.properties.ClientProperties;
import com.es.plus.starter.properties.EsProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author: hzh
 * @Date: 2022/9/6 14:34
 * 默认注册高级客户端.需要适配其他客户端也在这个类中添加
 */
@Configuration
@EnableConfigurationProperties(EsProperties.class)
@AutoConfigureBefore(ElasticsearchRestClientAutoConfiguration.class)
public class EsClientConfiguration implements InitializingBean {

    @Autowired
    private EsProperties esProperties;

    /**
     * 不存在才加载，存在的话以默认的为主
     */
    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {
        // 处理地址
        String address = esProperties.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new EsException("please config the es address");
        }

        String schema = esProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(address.split(",")).forEach(item -> hostList.add(new HttpHost(item.split(":")[0],
                Integer.parseInt(item.split(":")[1]), schema)));

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostList.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        // 设置账号密码最大连接数之类的
        String username = esProperties.getUsername();
        String password = esProperties.getPassword();
        Integer maxConnTotal = esProperties.getMaxConnTotal();
        Integer maxConnPerRoute = esProperties.getMaxConnPerRoute();
        Integer keepAliveMillis = esProperties.getKeepAliveMillis();
        boolean needSetHttpClient = (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                || (Objects.nonNull(maxConnTotal) || Objects.nonNull(maxConnPerRoute)) || Objects.nonNull(keepAliveMillis);
        if (needSetHttpClient) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // 设置心跳时间等
                Optional.ofNullable(keepAliveMillis).ifPresent(p -> httpClientBuilder.setKeepAliveStrategy((response, context) -> p));
                Optional.ofNullable(maxConnTotal).ifPresent(httpClientBuilder::setMaxConnTotal);
                Optional.ofNullable(maxConnPerRoute).ifPresent(httpClientBuilder::setMaxConnPerRoute);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    // 设置账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder;
            });
        }

        // 设置超时时间
        Integer connectTimeOut = esProperties.getConnectTimeOut();
        Integer socketTimeOut = esProperties.getSocketTimeOut();
        Integer connectionRequestTimeOut = esProperties.getConnectionRequestTimeOut();
        boolean needSetRequestConfig = Objects.nonNull(connectTimeOut) || Objects.nonNull(connectionRequestTimeOut);
        if (needSetRequestConfig) {
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                Optional.ofNullable(connectTimeOut).ifPresent(requestConfigBuilder::setConnectTimeout);
                Optional.ofNullable(socketTimeOut).ifPresent(requestConfigBuilder::setSocketTimeout);
                Optional.ofNullable(connectionRequestTimeOut)
                        .ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
                return requestConfigBuilder;
            });
        }

        return new RestHighLevelClient(builder);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //设置主客户端属性 以spring的客户端为主
        Map<String, ClientProperties> clientProperties = esProperties.getClientProperties();
        if (CollectionUtils.isEmpty(clientProperties)) {
            return;
        }
        clientProperties.forEach((k, v) -> {
            RestHighLevelClient restHighLevelClient = getRestHighLevelClient(v);
            EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(restHighLevelClient);
            ClientContext.addClient(k, esPlusClientFacade);
        });
    }

    private RestHighLevelClient getRestHighLevelClient(ClientProperties clientProperties) {
        // 处理地址
        String address = clientProperties.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new EsException("please config the es address");
        }

        String schema = clientProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(address.split(",")).forEach(item -> hostList.add(new HttpHost(item.split(":")[0],
                Integer.parseInt(item.split(":")[1]), schema)));

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostList.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        // 设置账号密码最大连接数之类的
        String username = clientProperties.getUsername();
        String password = clientProperties.getPassword();
        Integer maxConnTotal = clientProperties.getMaxConnTotal();
        Integer maxConnPerRoute = clientProperties.getMaxConnPerRoute();
        Integer keepAliveMillis = clientProperties.getKeepAliveMillis();
        boolean needSetHttpClient = (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                || (Objects.nonNull(maxConnTotal) || Objects.nonNull(maxConnPerRoute)) || Objects.nonNull(keepAliveMillis);
        if (needSetHttpClient) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // 设置心跳时间等
                Optional.ofNullable(keepAliveMillis).ifPresent(p -> httpClientBuilder.setKeepAliveStrategy((response, context) -> p));
                Optional.ofNullable(maxConnTotal).ifPresent(httpClientBuilder::setMaxConnTotal);
                Optional.ofNullable(maxConnPerRoute).ifPresent(httpClientBuilder::setMaxConnPerRoute);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    // 设置账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(clientProperties.getUsername(), clientProperties.getPassword()));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder;
            });
        }

        // 设置超时时间
        Integer connectTimeOut = clientProperties.getConnectTimeOut();
        Integer socketTimeOut = clientProperties.getSocketTimeOut();
        Integer connectionRequestTimeOut = clientProperties.getConnectionRequestTimeOut();
        boolean needSetRequestConfig = Objects.nonNull(connectTimeOut) || Objects.nonNull(connectionRequestTimeOut);
        if (needSetRequestConfig) {
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                Optional.ofNullable(connectTimeOut).ifPresent(requestConfigBuilder::setConnectTimeout);
                Optional.ofNullable(socketTimeOut).ifPresent(requestConfigBuilder::setSocketTimeout);
                Optional.ofNullable(connectionRequestTimeOut)
                        .ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
                return requestConfigBuilder;
            });
        }

        return new RestHighLevelClient(builder);
    }
}
