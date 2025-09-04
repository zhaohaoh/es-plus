package com.es.plus.autoconfigure.config;

import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.autoconfigure.interceptor.EsReindexInterceptor;
import com.es.plus.core.ClientContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
        Object value = properties.get("es-plus.global-config.reindex-scope");
        log.info("listenerChange es-plus.global-config.reindex-scope:{}",value);
        if (value != null && !value.toString().equals(GlobalConfigCache.GLOBAL_CONFIG.getReindexScope())) {
            GlobalConfigCache.GLOBAL_CONFIG.setReindexScope(value.toString());
            
            String reindexScope = GlobalConfigCache.GLOBAL_CONFIG.getReindexScope();
            //重建索引时的拦截器
            Collection<EsPlusClientFacade> clients = ClientContext.getClients();
            //如果存在reindex的索引则加载拦截器。否则去掉拦截器提高性能
            if (StringUtils.isNotBlank(reindexScope)){
                List<String> indexList = Arrays.stream(reindexScope.split(",")).collect(Collectors.toList());
                for (EsPlusClientFacade esPlusClientFacade : clients) {
                    EsReindexInterceptor esInterceptor = (EsReindexInterceptor) esPlusClientFacade
                            .getEsInterceptor(EsReindexInterceptor.class);
                    if (esInterceptor != null) {
                        esInterceptor.setReindexList(indexList);
                    }else{
                        EsReindexInterceptor esReindexInterceptor = new EsReindexInterceptor(esPlusClientFacade.getEsLockFactory());
                        esPlusClientFacade.addInterceptor(esReindexInterceptor);
                    }
                    log.info("listenerChange reindexScope :{} esPlusClientFacade host:{} addInterceptor",reindexScope,esPlusClientFacade.getHost());
                }
            }else{
                for (EsPlusClientFacade esPlusClientFacade : clients) {
                    esPlusClientFacade.removeInterceptor(EsReindexInterceptor.class);
                    log.info("listenerPropertiesChange reindexEnable :{}reindexScope :{} esPlusClientFacade host:{} remoteInterceptor",
                            GlobalConfigCache.GLOBAL_CONFIG.isAutoReindex()
                            ,reindexScope,esPlusClientFacade.getHost());
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
