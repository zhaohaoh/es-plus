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
//        String version = getVersion(restHighLevelClient);
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
        
//        log.info("Es-plus client Facade build success version:{}",version);
        return esPlusClientFacade;
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
