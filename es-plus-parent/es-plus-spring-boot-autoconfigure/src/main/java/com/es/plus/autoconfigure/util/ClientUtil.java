package com.es.plus.autoconfigure.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.interceptor.EsInterceptor;
import com.es.plus.core.ClientContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientUtil {
    public  static EsPlusClientFacade initAndPutEsPlusClientFacade(String key, ClientProperties clientProperties){
        String address = clientProperties.getAddress();
        address = StringUtils.replace(address,"http://","");
        address = StringUtils.replace(address,"https://","");
        clientProperties.setAddress(address);
        
        
        RestHighLevelClient restHighLevelClient = ClientUtil.getRestHighLevelClient(clientProperties);
        String version = ClientContext.getVersion(restHighLevelClient);
        EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(clientProperties.getAddress(),
                restHighLevelClient,
                null,Integer.parseInt(version));
        ClientContext.addClient(key, esPlusClientFacade);
        return esPlusClientFacade;
    }
    
    public static RestHighLevelClient getRestHighLevelClient(ClientProperties clientProperties) {
        // 处理地址
        String address = clientProperties.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new EsException("please config the es address");
        }
        
        String schema = clientProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(address.split(",")).forEach(item -> hostList.add(new HttpHost(item.split(":")[0],
                Integer.parseInt(StringUtils.substringAfterLast(item,":")), schema)));
        
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