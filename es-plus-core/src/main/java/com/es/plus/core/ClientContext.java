package com.es.plus.core;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.core.EsPlusIndexClient;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.es6.client.EsPlus6IndexRestClient;
import com.es.plus.es6.client.EsPlus6RestClient;
import com.es.plus.es7.client.EsPlusIndexRestClient;
import com.es.plus.es7.client.EsPlusRestClient;
import org.elasticsearch.client.RestHighLevelClient;

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
     * 得到客户
     *
     * @param name 名字
     * @return {@link EsPlusClientFacade}
     */
    public static EsPlusClientFacade getClient(String name) {
        return CLIENT_MAP.get(name);
    }

    /**
     * 构建es端外观
     *
     * @return {@link EsPlusClientFacade}
     */
    public static EsPlusClientFacade buildEsPlusClientFacade(RestHighLevelClient restHighLevelClient, EsLockFactory esLockFactory) {
        EsPlusClient esPlusRestClient;
        EsPlusIndexClient esPlusIndexRestClient;
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            esPlusRestClient = new EsPlus6RestClient(restHighLevelClient, esLockFactory);
            esPlusIndexRestClient = new EsPlus6IndexRestClient(restHighLevelClient);
        }else{
            esPlusRestClient = new EsPlusRestClient(restHighLevelClient, esLockFactory);
            esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        }

        return new EsPlusClientFacade(esPlusRestClient, esPlusIndexRestClient, esLockFactory);
    }
}
