package com.es.plus.autoconfigure.config;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.autoconfigure.interceptor.EsReindexInterceptor;
import com.es.plus.core.ClientContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

@Slf4j
public abstract class AbstractConfigManager {
    @Value("${es-plus.global-config.refreshDataId:}")
    public String dataId;
    @Value("${es-plus.global-config.refreshGroup:}")
    public String group;
    
    
    /**
     * 监听配置文件改动
     */
    @SneakyThrows
    protected void listenerChange(String name, String input) {
        Properties properties = getProperties(name, input);
        Object value = properties.get("es-plus.global-config.reindexIntercptor");
        log.info("listenerChange es-plus.global-config.reindexIntercptor:{}",value);
        if (value != null) {
            boolean reindexIntercptor = Boolean.parseBoolean(value.toString());
            Collection<EsPlusClientFacade> clients = ClientContext.getClients();
            for (EsPlusClientFacade client : clients) {
                EsReindexInterceptor esInterceptor = (EsReindexInterceptor) client
                        .getEsInterceptor(EsReindexInterceptor.class);
                if (esInterceptor != null) {
                    esInterceptor.setReindexIntercptor(reindexIntercptor);
                }
            }
        }
    }
    
    /**
     * 根据配置文件的输入获取配置属性
     *
     * @param name  名称
     * @param input 输入
     * @return {@link Properties}
     * @throws IOException IOException
     */
    public Properties getProperties(String name, String input) throws IOException {
        Properties properties = null;
        if (name.endsWith(".yml")) {
            ByteArrayResource byteArrayResource = new ByteArrayResource(input.getBytes());
            YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
            yamlPropertiesFactoryBean.setResources(byteArrayResource);
            properties = yamlPropertiesFactoryBean.getObject();
        } else {
            PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
            ByteArrayResource byteArrayResource = new ByteArrayResource(input.getBytes());
            propertiesFactoryBean.setLocation(byteArrayResource);
            properties = propertiesFactoryBean.getObject();
        }
        return properties;
    }
}
