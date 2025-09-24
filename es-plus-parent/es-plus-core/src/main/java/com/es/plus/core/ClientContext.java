package com.es.plus.core;

import com.es.plus.client.Es8LockClient;
import com.es.plus.client.Es8PlusIndexRestClient;
import com.es.plus.client.Es8PlusRestClient;
import com.es.plus.client.EsPlus6IndexRestClient;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsPlusClient;
import com.es.plus.common.core.EsPlusIndexClient;
import com.es.plus.common.interceptor.EsInterceptor;
import com.es.plus.common.interceptor.EsPlusClientProxy;
import com.es.plus.common.lock.EsLockFactory;
import com.es.plus.es6.client.EsPlus6RestClient;
import com.es.plus.es7.client.Es7LockClient;
import com.es.plus.es7.client.EsPlusIndexRestClient;
import com.es.plus.es7.client.EsPlusRestClient;
import com.es.plus.es6.client.Es6LockClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: hzh
 * @Date: 2023/2/1 16:57
 */
public class ClientContext {
    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(ClientContext.class);
    
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
    public static EsPlusClientFacade buildEsPlusClientFacade(String host,Object esClient, EsLockFactory esLockFactory, List<EsInterceptor> esInterceptors) {
        EsPlusClient esPlusClient;
        EsPlusIndexClient esPlusIndexRestClient;
        
        //        String version = getVersion(restHighLevelClient);
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            esPlusClient = new EsPlus6RestClient(esClient);
            esPlusIndexRestClient = new EsPlus6IndexRestClient(esClient);
        }else if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(7)){
            esPlusClient = new EsPlusRestClient(esClient);
            esPlusIndexRestClient = new EsPlusIndexRestClient(esClient);
        }else if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(8)){
            esPlusClient = new Es8PlusRestClient(esClient);
            esPlusIndexRestClient = new Es8PlusIndexRestClient(esClient);
        }else {
            esPlusClient = new EsPlusRestClient(esClient);
            esPlusIndexRestClient = new EsPlusIndexRestClient(esClient);
        }
        
        EsPlusClientProxy esPlusClientProxy = new EsPlusClientProxy(esPlusClient,esInterceptors);
        EsPlusClient proxy = (EsPlusClient) esPlusClientProxy.getProxy();
        
        EsPlusClientFacade esPlusClientFacade = new EsPlusClientFacade(esPlusClientProxy, esPlusIndexRestClient, esLockFactory,host);
        
        return esPlusClientFacade;
    }
    
    public static EsPlusClientFacade buildEsPlusClientFacade(String host,Object lockClient,List<EsInterceptor> esInterceptors) {
        
        //        String version = getVersion(restHighLevelClient);
        EsLockFactory esLockFactory = null;
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            Es6LockClient esLockClient = new Es6LockClient(lockClient);
            esLockFactory = new EsLockFactory(esLockClient);
        }else if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(7)){
            Es7LockClient esLockClient = new Es7LockClient(lockClient);
            esLockFactory = new EsLockFactory(esLockClient);
        }else if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(8)){
            Es8LockClient esLockClient = new Es8LockClient(lockClient);
            esLockFactory = new EsLockFactory(esLockClient);
        }else {
            Es7LockClient esLockClient = new Es7LockClient(lockClient);
            esLockFactory = new EsLockFactory(esLockClient);
        }
        return buildEsPlusClientFacade(host,lockClient,esLockFactory,esInterceptors);
    }
    
    //    public static String getVersion(RestHighLevelClient restHighLevelClient) {
    //            final String opensearch = "opensearch";
    //            final String distribution = "distribution";
    //        try {
    //            RestClient client = restHighLevelClient.getLowLevelClient();
    //            Request request = new Request("GET", "/");
    //            Response res = client.performRequest(request);
    //            if (res.getStatusLine().getStatusCode() == 200) {
    //                String jsonStr = EntityUtils.toString(res.getEntity(), "utf-8");
    //                Map<String, Object> map = JsonUtils.toMap(jsonStr);
    //                Map<String, Object> version = (Map<String, Object>) map.get("version");
    //                if (opensearch.equals(version.get(distribution))) {
    //                    int lucene_version = Integer.parseInt(version.get("lucene_version").toString().split("\\.")[0]);
    //                    return String.valueOf(lucene_version - 1);
    //                }
    //
    //                return version.get("number").toString();
    //            } else {
    //                String responseStr = EntityUtils.toString(res.getEntity());
    //            }
    //        } catch (Exception e) {
    //            log.error("getEsVersion", e);
    //        }
    //        return null;
    //    }
}