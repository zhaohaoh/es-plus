package com.es.plus.core.process;

import com.es.plus.exception.EsException;
import com.es.plus.pojo.EsUpdateField;
import com.es.plus.util.BeanUtils;
import com.es.plus.lock.EsLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.es.plus.constant.EsConstant.*;

/**
 * @Author: hzh
 * @Date: 2022/9/9 10:04
 */
public class ReindexObjectProcess {
    private static final Logger logger = LoggerFactory.getLogger(ReindexObjectProcess.class);
    public static boolean ENABLED = false;

    private final EsLockFactory esLockClient;

    public ReindexObjectProcess(EsLockFactory esLockClient) {
        this.esLockClient = esLockClient;
    }

    public EsUpdateField.Field insertFill() {
        return null;
    }


    public EsUpdateField.Field updateFill() {
        return new EsUpdateField.Field(REINDEX_TIME_FILED, System.currentTimeMillis());
    }

    public boolean lock(String index) {
        // 是在reindex索引重建.则获取更新锁  此处加上读写锁的原因如下
        // 1如果只有下面的锁的isLocked判断.那么判断还有锁.执行后续代码.但是刚好重建索引结束那么mappins就变了.并发问题,
        //  2如果对操作全加锁则并发度低.
        Lock readLock = esLockClient.getReadWrtieLock(index + REINDEX_UPDATE_LOCK).readLock();
        boolean success = false;
        try {
            success = readLock.tryLock(3, TimeUnit.SECONDS);

            //获取reindex的锁
            boolean isLock = esLockClient.getLock(index + REINDEX_LOCK_SUFFIX).isLocked();

            //如果不是锁定的直接释放
            if (!isLock) {
                //已经执行完reindex操作 那么释放更新锁
                ENABLED = false;
                logger.info("reindex.ENABLED = false");
                if (success) {
                    readLock.unlock();
                }
                return false;
            }

            //如果获取锁失败并且是锁定的直接抛异常
            if (!success) {
                throw new EsException("index:" + index + " tryLock:" + REINDEX_UPDATE_LOCK + " fail");
            }
        } catch (InterruptedException ignored) {
        }
        return success;
    }

    public void unLock(String index) {
        Lock readLock = esLockClient.getReadWrtieLock(index + REINDEX_UPDATE_LOCK).readLock();
        readLock.unlock();
    }


    public Object setUpdateFeild(Object object) {
        EsUpdateField.Field updateFill = updateFill();
        if (updateFill == null) {
            return object;
        }
        Map<String, Object> beanToMap = BeanUtils.beanToMap(object);
        beanToMap.put(updateFill.getName(), updateFill.getValue());
        return beanToMap;
    }


    public Object setInsertFeild(Object object) {
        EsUpdateField.Field insertFill = insertFill();
        if (insertFill == null) {
            return object;
        }
        Map<String, Object> beanToMap = BeanUtils.beanToMap(object);
        beanToMap.put(insertFill.getName(), insertFill.getValue());
        return object;
    }


}
