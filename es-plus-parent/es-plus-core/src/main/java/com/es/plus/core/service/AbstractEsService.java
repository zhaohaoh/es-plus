package com.es.plus.core.service;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.ConnectFailHandleEnum;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.DefaultClass;
import com.es.plus.core.ClientContext;
import com.es.plus.core.process.EsReindexProcess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
public abstract class AbstractEsService<T> implements SmartInitializingSingleton {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEsService.class);
    /**
     * 类型
     */
    protected String type;
    /**
     * 索引,这里存的是别名
     */
    protected String alias;
    /**
     * 索引
     */
    protected String index;

    /**
     * clazz
     */
    protected Class<T> clazz;
  
    private EsPlusClientFacade esPlusClientFacade;

    public EsPlusClientFacade getEsPlusClientFacade() {
        return esPlusClientFacade;
    }
    
 
    
    @Override
    @SuppressWarnings({"unchecked"})
    public void afterSingletonsInstantiated() {
        try {
            Type tClazz = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            clazz = (Class<T>) tClazz;
            Class<?> indexClass = clazz;
            EsIndex annotation = clazz.getAnnotation(EsIndex.class);

            //添加索引信息
            EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(indexClass);
    
            this.esPlusClientFacade = ClientContext.getClient(esIndexParam.getClientInstance());

            if (esPlusClientFacade==null){
                return;
            }
            
            this.index = esIndexParam.getIndex();

            //有别名取别名，没有别名就取索引名。  本框架别名就是真实操作的索引名，优先取别名进行查询
            this.alias = StringUtils.isBlank(esIndexParam.getAlias()) ? this.alias = index : esIndexParam.getAlias();

            type = annotation.type();

            // 如果是子文档不执行创建索引的相关操作
            Class<?> parentClass = annotation.parentClass();
            if (parentClass != DefaultClass.class) {
                return;
            }

            //启动时不初始化
            if (!GlobalConfigCache.GLOBAL_CONFIG.isStartInit()) {
                return;
            }

            // 启动时不初始化
            if (!annotation.startInit()) {
                return;
            }

            //尝试创建或重建索引
            tryCreateOrReindex(indexClass, esIndexParam);
        } catch (Exception e) {
            if (StringUtils.isNotBlank(e.getLocalizedMessage()) && e.getLocalizedMessage().contains("ConnectException")) {
                if (GlobalConfigCache.GLOBAL_CONFIG.getConnectFailHandle().equals(ConnectFailHandleEnum.THROW_EXCEPTION)) {
                    throw new EsException(e);
                } else {
                    GlobalConfigCache.GLOBAL_CONFIG.setStartInit(false);
                }
            } else {
                logger.error("es-plus tryLock Or createIndex OR tryReindex Exception:", e);
            }
        }
    }

    private void tryCreateOrReindex(Class<?> indexClass, EsIndexParam esIndexParam) {
        //此处获取的是执行锁
        ELock eLock = esPlusClientFacade.getLock(index);
        boolean lock = eLock.tryLock();
        try {
            if (lock) {
                //取索引名判断，会同时判断索引名和别名
                boolean exists = esPlusClientFacade.indexExists(this.index) || esPlusClientFacade.indexExists(alias);
                if (exists) {
                    EsReindexProcess.tryReindex(esPlusClientFacade, indexClass);
                } else {
                    esPlusClientFacade.createIndexMapping(this.index, indexClass);
                    exists = true;
                }
                esIndexParam.setExists(exists);
                logger.info("init es-plus indexResponse={} exists={}", this.index, exists);
            }
        } finally {
            if (lock) {
                eLock.unlock();
            }
        }
    }


}
