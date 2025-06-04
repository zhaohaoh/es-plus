package com.es.plus.core;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 获取索引工具类。动态索引名如果索引不存在会动态创建索引
 */
@Slf4j
public class IndexContext {
    
    public static String dynamicIndexLock = "dynamic_index_lock_";
    
    public static EsIndexParam getIndex(Class<?> tClass) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(tClass);
        String[] indexs;
        if (esIndexParam != null && esIndexParam.getDynamicIndex()) {
            String clientInstance = esIndexParam.getClientInstance();
            String[] preIndex = esIndexParam.getPreIndex();
            indexs = esIndexParam.getIndex();
            
            if (!Objects.equals(Arrays.toString(preIndex), Arrays.toString(indexs))) {
                log.info("dynamicIndex preIndex:{} newIndex:{} begin", preIndex, indexs);
                String[] diffIndex = getDiffIndex(preIndex, indexs);
                for (String index : diffIndex) {
                    EsPlusClientFacade client = ClientContext.getClient(clientInstance);
                    ELock lock = client.getLock(dynamicIndexLock + index);
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
                                Thread.sleep(300);
                            }
                        }
                        log.info("dynamicIndex \npreIndex:{} newIndex:{} end exists:{}", preIndex, indexs, tryLock);
                    } catch (Exception e) {
                        log.error("dynamicIndex Exception",e);
                    } finally {
                        if (tryLock) {
                            lock.unlock();
                        }
                    }
                }
            }
        }
        return esIndexParam;
    }
    
    private static String[] getDiffIndex(String[] preIndex, String[] indexs) {
        // 将arr2转为Set提高查询效率
        Set<String> preSet = Arrays.stream(preIndex).collect(Collectors.toSet());
        
        // 过滤arr1中不在set2中的元素（保留重复值）
        String[] diff = Arrays.stream(indexs)
                .filter(s -> !preSet.contains(s))
                .toArray(String[]::new);
        return diff;
    }
}
