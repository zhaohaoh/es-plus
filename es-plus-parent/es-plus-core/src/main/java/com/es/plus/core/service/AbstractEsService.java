package com.es.plus.core.service;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.core.ClientContext;
import com.es.plus.core.IndexContext;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
public abstract class AbstractEsService<T> implements SmartInitializingSingleton , EsService<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractEsService.class);
  
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
            Type tClazz = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            clazz = (Class<T>) tClazz;
            Class<?> indexClass = clazz;
            //添加索引信息
            EsIndexParam esIndexParam = IndexContext.getIndex(indexClass);
            if (esIndexParam == null){
                logger.error("找不到索引:{}",indexClass);
                return;
            }
            this.esPlusClientFacade = ClientContext.getClient(esIndexParam.getClientInstance());
    }
    
    
    @Override
    public String[] getIndex() {
        return IndexContext.getIndex(clazz).getIndex();
    }
    @Override
    public String[] getAlias() {
        EsIndexParam index = IndexContext.getIndex(clazz);
        return ArrayUtils.isEmpty(index.getAlias()) ? getIndex() : index.getAlias();
    }
  
    
    protected String getType() {
        EsIndexParam index = IndexContext.getIndex(clazz);
        return index.getType();
    }
}
