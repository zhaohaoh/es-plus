package com.es.plus.core.service;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.constants.ConnectFailHandleEnum;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsConstant;
import com.es.plus.core.process.EsReindexProcess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
     * 索引
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
    public void afterPropertiesSet() {
        try {
            Type tClazz = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            clazz = (Class<T>) tClazz;
            Class<?> indexClass = clazz;
            EsIndex annotation = clazz.getAnnotation(EsIndex.class);

            //添加id字段映射
            Field[] fields = indexClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(EsId.class) != null) {
                    EsParamHolder.put(clazz, field.getName());
                }
            }

            //添加索引信息
            EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(indexClass);

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
        ELock eLock = esLockFactory.getLock(index);
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
        // 改变索引必须重启所有服务这里才不会出现问题。正常k8s服务集群都是多台顺序重启.
        // 不管有没有获取到上面的执行锁。都要判断reindex的状态。此处是为了多es实例。如果一个实例在reindex的状态。其他实例要能够感知到并设置锁定状态
        boolean locked = esPlusClientFacade.getLock(esIndexParam.getIndex() + EsConstant.REINDEX_LOCK_SUFFIX).isLocked();
        if (locked) {
            //设置索引的状态是在reindex中
            esPlusClientFacade.getEsPlusClient().setReindexState(true);
        }
    }


}
