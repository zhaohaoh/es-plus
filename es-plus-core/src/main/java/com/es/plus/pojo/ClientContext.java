package com.es.plus.pojo;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.client.EsPlusIndexRestClient;
import com.es.plus.client.EsPlusRestClient;
import com.es.plus.lock.EsLockClient;
import com.es.plus.lock.EsLockFactory;
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
    public static EsPlusClientFacade buildEsPlusClientFacade(RestHighLevelClient restHighLevelClient) {
        EsLockClient esLockClient = new EsLockClient(restHighLevelClient);
        EsLockFactory esLockFactory = new EsLockFactory(esLockClient);
        EsPlusRestClient esPlusRestClient = new EsPlusRestClient(restHighLevelClient, esLockFactory);
        EsPlusIndexRestClient esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        EsPlusClientFacade esPlusClientFacade = new EsPlusClientFacade(esPlusRestClient, esPlusIndexRestClient, esLockFactory);
        return esPlusClientFacade;
    }
}
