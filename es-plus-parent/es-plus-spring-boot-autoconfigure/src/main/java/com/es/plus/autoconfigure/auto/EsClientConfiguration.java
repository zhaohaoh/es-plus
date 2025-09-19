package com.es.plus.autoconfigure.auto;

import com.es.plus.autoconfigure.util.ClientUtil;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.config.EsObjectHandler;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.constants.GlobalConfig;
import com.es.plus.common.interceptor.EsInterceptor;
import com.es.plus.common.lock.ELockClient;
import com.es.plus.common.pojo.es.client.EpClient;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.autoconfigure.properties.EsProperties;
import com.es.plus.core.ClientContext;
import com.es.plus.es6.client.Es6LockClient;
import com.es.plus.es7.client.Es7LockClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2022/9/6 14:34
 * 默认注册高级客户端.需要适配其他客户端也在这个类中添加
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EsProperties.class)
@ConditionalOnClass(RestHighLevelClient.class)
public class EsClientConfiguration implements InitializingBean {
    
    @Autowired
    private EsProperties esProperties;
    
    @Autowired(required = false)
    private List<EsInterceptor> esInterceptors;
    
    @Autowired(required = false)
    private List<EsObjectHandler> esObjectHandlers;
    
    /**
     * 不存在才加载，存在的话以默认的为主
     */
    @Bean
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    @ConditionalOnProperty(value = "es-plus.address")
    public RestHighLevelClient restHighLevelClient() {
        ClientProperties clientProperties = new ClientProperties();
        BeanUtils.copyProperties(esProperties,clientProperties);
        RestHighLevelClient restHighLevelClient = ClientUtil.getRestHighLevelClient(clientProperties);
        return restHighLevelClient;
    }
    @Bean
    public EpClient<RestHighLevelClient> epClient(RestHighLevelClient restHighLevelClient) {
        EpClient<RestHighLevelClient> epClient = new EpClient<>(restHighLevelClient);
        return epClient;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (esObjectHandlers !=null) {
            Map<String, EsObjectHandler> handlerMap = esObjectHandlers.stream()
                    .collect(Collectors.toMap(EsObjectHandler::index, Function.identity()));
            GlobalConfigCache.ES_OBJECT_HANDLER = handlerMap;
        }
        
        //设置主客户端属性 以spring的客户端为主
        Map<String, ClientProperties> clientProperties = esProperties.getClientProperties();
        if (CollectionUtils.isEmpty(clientProperties)) {
            return;
        }
        clientProperties.forEach((k, v) -> {
            RestHighLevelClient restHighLevelClient = ClientUtil.getRestHighLevelClient(v);
            EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(v.getAddress(),restHighLevelClient,esInterceptors);
            ClientContext.addClient(k, esPlusClientFacade);
        });
    }
    
    /**
     * es 锁客户端
     */
    @Bean
    public ELockClient esPlusLockClient(RestHighLevelClient restHighLevelClient) {
        GlobalConfig globalConfig = esProperties.getGlobalConfig();
        Integer version = globalConfig.getVersion();
        if (version.equals(7)){
            return new Es7LockClient(restHighLevelClient);
        }
        return new Es6LockClient(restHighLevelClient);
    }
    
}