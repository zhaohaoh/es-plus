package com.es.plus.core.service;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.core.ClientContext;
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
    
//    /**
//     * 类型
//     */
//    protected String type;
//
//    /**
//     * 索引,这里存的是别名
//     */
//    protected String alias;
//
//    /**
//     * 索引
//     */
//    protected String index;
    
    /**
     * clazz
     */
    protected Class<T> clazz;
    private  EsIndexParam esIndexParam;
    
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
            esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(indexClass);
            
            this.esPlusClientFacade = ClientContext.getClient(esIndexParam.getClientInstance());
    
    }
    
    protected String getIndex() {
        return esIndexParam.getIndex();
    }
    
    protected String getAlias() {
        return StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
    }
    
    protected String getType() {
        return esIndexParam.getType();
    }
}
