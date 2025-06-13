package com.es.plus.core;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.core.EsPlusIndexClient;
import com.es.plus.adapter.interceptor.EsInterceptor;
import com.es.plus.adapter.interceptor.EsPlusClientProxy;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.es6.client.EsPlus6IndexRestClient;
import com.es.plus.es6.client.EsPlus6RestClient;
import com.es.plus.es7.client.EsPlusIndexRestClient;
import com.es.plus.es7.client.EsPlusRestClient;
import com.es.plus.lock.EsLockClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: hzh
 * @Date: 2023/2/1 16:57
 */
public class ClientContext {
    private static final Map<String, EsPlusClientFacade> CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 保存客户端
     *
     * @param name               名字
     * @param esPlusClientFacade es +客户外观
     */
    public static void addClient(String name, EsPlusClientFacade esPlusClientFacade) {
        CLIENT_MAP.put(name, esPlusClientFacade);
    }
    
    /**
     * 删除客户端
     *
     * @param name               名字
     */
    public static void removeClient(String name) {
        CLIENT_MAP.remove(name);
    }

    /**
     * 得到客户
     *
     * @param name 名字
     * @return {@link EsPlusClientFacade}
     */
    public static EsPlusClientFacade getClient(String name) {
        return CLIENT_MAP.get(name);
    }
    public static Collection<EsPlusClientFacade> getClients() {
        return CLIENT_MAP.values();
    }

    /**
     * 构建es端外观
     *
     * @return {@link EsPlusClientFacade}
     */
    public static EsPlusClientFacade buildEsPlusClientFacade(String host,RestHighLevelClient restHighLevelClient, EsLockFactory esLockFactory, List<EsInterceptor> esInterceptors) {
        EsPlusClient esPlusClient;
        EsPlusIndexClient esPlusIndexRestClient;
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            esPlusClient = new EsPlus6RestClient(restHighLevelClient);
            esPlusIndexRestClient = new EsPlus6IndexRestClient(restHighLevelClient);
        }else{
            esPlusClient = new EsPlusRestClient(restHighLevelClient);
            esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        }

        EsPlusClientProxy esPlusClientProxy = new EsPlusClientProxy(esPlusClient,esInterceptors);

       
        EsPlusClientFacade esPlusClientFacade = new EsPlusClientFacade(esPlusClientProxy, esPlusIndexRestClient, esLockFactory,host);

        return esPlusClientFacade;
    }
    
    public static EsPlusClientFacade buildEsPlusClientFacade(String host,RestHighLevelClient restHighLevelClient,List<EsInterceptor> esInterceptors) {
        EsPlusClient esPlusClient;
        EsPlusIndexClient esPlusIndexRestClient;
        EsLockClient esLockClient = new EsLockClient(restHighLevelClient);
        EsLockFactory esLockFactory = new EsLockFactory(esLockClient);
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            esPlusClient = new EsPlus6RestClient(restHighLevelClient);
            esPlusIndexRestClient = new EsPlus6IndexRestClient(restHighLevelClient);
        }else{
            esPlusClient = new EsPlusRestClient(restHighLevelClient);
            esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        }
        
        EsPlusClientProxy esPlusClientProxy = new EsPlusClientProxy(esPlusClient,esInterceptors);
        
        EsPlusClient proxy = (EsPlusClient) esPlusClientProxy.getProxy();
        
        EsPlusClientFacade esPlusClientFacade = new EsPlusClientFacade(esPlusClientProxy, esPlusIndexRestClient, esLockFactory,host);
        
        return esPlusClientFacade;
    }


}
