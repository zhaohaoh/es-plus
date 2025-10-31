package com.es.plus.autoconfigure.auto;

import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.interceptor.EsInterceptor;
import com.es.plus.common.lock.ELockClient;
import com.es.plus.common.lock.EsLockFactory;
import com.es.plus.autoconfigure.config.FileConfigMonitor;
import com.es.plus.autoconfigure.config.NacosConfigMointor;
import com.es.plus.autoconfigure.properties.EsProperties;
import com.es.plus.common.pojo.es.client.EpClient;
import com.es.plus.core.ClientContext;
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
@DependsOn({"esClientConfiguration","epGlobalConfigCacheConfigration"})
@ComponentScan(basePackages = "com.es.plus")
public class EsAutoConfiguration {
    
    
    @Autowired
    private EsProperties esProperties;
    
    /**
     * es 外观
     *
     * @param esLockFactory       es锁工厂
     * @return {@link EsPlusClientFacade}
     */
    @Bean(value = "esPlusClientFacade")
    public EsPlusClientFacade esPlusClientFacade(EpClient epClient, EsLockFactory esLockFactory,
            List<EsInterceptor> esInterceptors) {
        EsPlusClientFacade esPlusClientFacade = ClientContext
                .buildEsPlusClientFacade(esProperties.getAddress(),epClient.getOrginalClient(), esLockFactory, esInterceptors);
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