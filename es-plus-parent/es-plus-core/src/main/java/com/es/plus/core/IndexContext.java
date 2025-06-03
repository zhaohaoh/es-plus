package com.es.plus.core;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IndexContext {
    
    public static String dynamicIndexLock = "dynamic_index_lock_";
    
    public static EsIndexParam getIndex(Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        String index;
        if (esIndexParam != null && esIndexParam.getDynamicIndex()) {
            String clientInstance = esIndexParam.getClientInstance();
            String preIndex = esIndexParam.getPreIndex();
            index = esIndexParam.getIndex()[0];
            if (!Objects.equals(preIndex, index)) {
                log.info("dynamicIndex preIndex:{} newIndex:{} begin", preIndex, index);
                EsPlusClientFacade client = ClientContext.getClient(clientInstance);
                ELock lock = client.getLock(dynamicIndexLock + esIndexParam.getOriIndex());
                boolean tryLock = lock.tryLock(1, TimeUnit.SECONDS);
                try {
                    if (tryLock) {
                        boolean exists = client.indexExists(index);
                        if (!exists) {
                            client.createIndexMapping(index, tClass);
                        }
                    }else{
                        int count=0;
                        while (true) {
                            boolean exists = client.indexExists(index);
                            if (!exists && count>=10){
                                log.error("dynamicIndex indexNotExists tryMax OriIndex:{}"
                                        ,esIndexParam.getOriIndex());
                                break;
                            }
                            if (exists){
                                break;
                            }
                            count++;
                            Thread.sleep(1000);
                        }
                    }
                    log.info("dynamicIndex \npreIndex:{} newIndex:{} end exists:{}", preIndex, index, tryLock);
                } catch (Exception e) {
                    log.error("dynamicIndex Exception",e);
                } finally {
                    if (tryLock) {
                        lock.unlock();
                    }
                }
            }
        }
        return esIndexParam;
    }
}
