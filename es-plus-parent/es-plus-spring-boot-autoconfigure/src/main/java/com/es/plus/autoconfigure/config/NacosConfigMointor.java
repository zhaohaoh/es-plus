package com.es.plus.autoconfigure.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author laizeh
 * @date 2020/11/18
 */
public class NacosConfigMointor extends AbstractConfigManager{
    @Autowired
    private NacosConfigManager manager;
 

    /**
     * 动态监听配置变化
     *
     * @throws NacosException
     */
    @PostConstruct
    public void listen() throws NacosException {
        //监听配置改版
        //todo: 这里后面可以改成动态配置进行初始化,使得可以支持多套配置
        manager.getConfigService().addListener(dataId, group, new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                listenerChange(dataId,configInfo);
            }
        });
    }
    
    
}
