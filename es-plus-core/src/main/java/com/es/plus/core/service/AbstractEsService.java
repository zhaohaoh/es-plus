package com.es.plus.core.service;


import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.lock.ELock;
import com.es.plus.core.EsReindexHandler;
import com.es.plus.lock.EsLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.es.plus.constant.EsConstant.SO_SUFFIX;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
public abstract class AbstractEsService<T> implements InitializingBean {
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
     * 索引,这里存的是别名
     */
    protected String index;

    /**
     * clazz
     */
    protected Class<T> clazz;

    @Autowired
    protected EsPlusClientFacade esPlusClientFacade;


    /**
     * es锁客户
     */
    @Autowired
    protected EsLockFactory esLockFactory;


    @Override
    @SuppressWarnings({"unchecked"})
    public void afterPropertiesSet() throws Exception {
        try {
            Type tClazz = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            clazz = (Class<T>) tClazz;
            EsIndex annotation = clazz.getAnnotation(EsIndex.class);
            //添加id字段映射
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(EsId.class) != null) {
                    EsParamHolder.put(clazz, field.getName());
                }
            }
            //添加索引信息
            EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(clazz);

            this.alias = esIndexParam.getAlias();

            this.index = esIndexParam.getIndex();

            type = annotation.type();

            ELock eLock = esLockFactory.getLock(index);
            boolean lock = eLock.tryLock();
            try {
                if (lock) {
                    boolean exists = esPlusClientFacade.indexExists(this.alias);
                    if (exists) {
                        EsReindexHandler.tryReindex(esPlusClientFacade, clazz);
                    } else {
                        esPlusClientFacade.createIndexMapping(this.index + SO_SUFFIX, clazz);
                    }
                    logger.info("init es indexResponse={} exists={}", this.index, exists);
                }
            } finally {
                if (lock) {
                    eLock.unlock();
                }
            }
        } catch (Exception e) {
            logger.error("es-plus tryLock Or createIndex OR tryReindex Exception:", e);
        }
    }


}
