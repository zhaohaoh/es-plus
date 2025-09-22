package com.es.plus.autoconfigure.auto;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.autoconfigure.properties.EsProperties;
import com.es.plus.autoconfigure.util.Client8Util;
import com.es.plus.client.Es8LockClient;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.config.EsObjectHandler;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.constants.GlobalConfig;
import com.es.plus.common.interceptor.EsInterceptor;
import com.es.plus.common.lock.ELockClient;
import com.es.plus.common.pojo.es.client.EpClient;
import com.es.plus.core.ClientContext;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration(value = "esClientConfiguration")
@EnableConfigurationProperties(EsProperties.class)
@ConditionalOnClass(ElasticsearchClient.class)
public class Es8ClientConfiguration implements InitializingBean {
    
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
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    @ConditionalOnProperty(value = "es-plus.address")
    public ElasticsearchClient elasticsearchClient() {
        ClientProperties clientProperties = new ClientProperties();
        BeanUtils.copyProperties(esProperties,clientProperties);
        ElasticsearchClient restHighLevelClient = Client8Util.getElasticsearchClient(clientProperties);
        return restHighLevelClient;
    }
    @Bean
    public EpClient<ElasticsearchClient> epClient(ElasticsearchClient elasticsearchClient) {
        EpClient<ElasticsearchClient> epClient = new EpClient<>(elasticsearchClient);
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
            ElasticsearchClient elasticsearchClient = Client8Util.getElasticsearchClient(v);
            EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(v.getAddress(),elasticsearchClient,esInterceptors);
            ClientContext.addClient(k, esPlusClientFacade);
        });
    }
    
    
    
    
    
    /**
     * es 锁客户端
     */
    @Bean
    public ELockClient esPlusLockClient(ElasticsearchClient elasticsearchClient) {
        GlobalConfig globalConfig = esProperties.getGlobalConfig();
        return new Es8LockClient(elasticsearchClient);
    }
}