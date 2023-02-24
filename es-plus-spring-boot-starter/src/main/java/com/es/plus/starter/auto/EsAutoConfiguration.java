package com.es.plus.starter.auto;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.client.EsPlusIndexRestClient;
import com.es.plus.client.EsPlusRestClient;
import com.es.plus.lock.ELockClient;
import com.es.plus.lock.EsLockClient;
import com.es.plus.lock.EsLockFactory;
import com.es.plus.pojo.ClientContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;


/**
 * @Author: hzh
 * @Date: 2022/6/13 16:04
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ComponentScan(basePackages = "com.es.plus")
@DependsOn("esClientConfiguration")
public class EsAutoConfiguration {


    /**
     * es 外观
     *
     * @param restHighLevelClient 其他高水平客户
     * @param esLock              es锁
     * @return {@link EsPlusClientFacade}
     */
    @Bean
    public EsPlusClientFacade esPlusClientFacade(RestHighLevelClient restHighLevelClient, EsLockFactory esLock) {
        EsPlusRestClient esPlusRestClient = new EsPlusRestClient(restHighLevelClient, esLock);
        EsPlusIndexRestClient esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        EsPlusClientFacade esPlusClientFacade = new EsPlusClientFacade(esPlusRestClient, esPlusIndexRestClient, esLock);
        ClientContext.addClient("master", esPlusClientFacade);
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

}
