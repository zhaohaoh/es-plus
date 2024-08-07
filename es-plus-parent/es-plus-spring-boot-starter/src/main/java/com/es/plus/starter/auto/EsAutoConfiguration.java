package com.es.plus.starter.auto;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.interceptor.EsInterceptor;
import com.es.plus.adapter.lock.ELockClient;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.autoconfigure.config.FileConfigMonitor;
import com.es.plus.autoconfigure.config.NacosConfigMointor;
import com.es.plus.autoconfigure.properties.EsProperties;
import com.es.plus.core.ClientContext;
import com.es.plus.lock.EsLockClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;

import java.util.List;

import static com.es.plus.constant.EsConstant.MASTER;


/**
 * @Author: hzh
 * @Date: 2022/6/13 16:04
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@DependsOn("esClientConfiguration")
@ComponentScan(basePackages = "com.es.plus")
public class EsAutoConfiguration {
    
    
    @Autowired
    private EsProperties esProperties;
    
    /**
     * es 外观
     *
     * @param restHighLevelClient 其他高水平客户
     * @param esLockFactory       es锁工厂
     * @return {@link EsPlusClientFacade}
     */
    @Bean(value = "esPlusClientFacade")
    public EsPlusClientFacade esPlusClientFacade(RestHighLevelClient restHighLevelClient, EsLockFactory esLockFactory,
            List<EsInterceptor> esInterceptors) {
        EsPlusClientFacade esPlusClientFacade = ClientContext
                .buildEsPlusClientFacade(esProperties.getAddress(),restHighLevelClient, esLockFactory, esInterceptors);
        ClientContext.addClient(MASTER, esPlusClientFacade);
        return esPlusClientFacade;
    }
    
    /**
     * es锁
     *
     * @param esLockClient es锁定客户
     * @return {@link EsLockFactory}
     */
    @Bean
    public EsLockFactory esLock(ELockClient esLockClient) {
        return new EsLockFactory(esLockClient);
    }
    
    /**
     * es 锁客户端
     */
    @Bean
    public ELockClient esPlusLockClient(RestHighLevelClient restHighLevelClient) {
        return new EsLockClient(restHighLevelClient);
    }
    
    @Bean
    @ConditionalOnProperty(value = "es-plus.global-config.config-type",havingValue = "file",matchIfMissing = false)
    public FileConfigMonitor configMonitor() {
        return new FileConfigMonitor();
    }
    
    @Bean
    @ConditionalOnProperty(value = "es-plus.global-config.config-type",havingValue = "nacos",matchIfMissing = false)
    public NacosConfigMointor nacosConfig() {
        return new NacosConfigMointor();
    }
}
