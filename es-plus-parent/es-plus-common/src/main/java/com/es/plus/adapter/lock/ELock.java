package com.es.plus.adapter.lock;

import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.util.LockLogUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.es.plus.constant.EsConstant.*;


/**
 * @Author: hzh
 * @Date: 2022/9/9 15:47
 * es实现分布式锁.基于睡眠重试.性能并不好哦
 * 所有脚本语言必须使用params参数形式才能使其减少编译.如果是整个字符串每次都会重新编译
 */
public abstract class ELock implements Lock {
    protected final ELockClient esPlusLockClient;
    protected final String key;
    private static final String ID_FIELD = "_id";
    private static final Map<String, ScheduledFuture<?>> WATCH_DOGS = new ConcurrentHashMap<>();

    protected ELock(ELockClient esPlusLockClient, String key) {
        this.esPlusLockClient = esPlusLockClient;
        this.key = key;
    }

    @Override
    public void lock() {
        //此方法可能长时间阻塞
        while (!tryLock()) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    
    public Object getLockValue() {
        SearchResponse response = esPlusLockClient.search(lockIndexName(), ID_FIELD,key);
        SearchHits searchHits = response.getHits();
        if (searchHits != null && searchHits.getHits() != null && searchHits.getHits().length > 0) {
            for (SearchHit hit : response.getHits().getHits()) {
               return hit.getSourceAsMap().get("value");
            }
        }
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long currentTimeMillis = System.currentTimeMillis();
        long millis = unit.toMillis(time);
        while (!tryLock()) {
            if (System.currentTimeMillis() + millis - currentTimeMillis < 0) {
                return false;
            }
            // 重试8次以下.
            Thread.sleep(millis >> 3);
        }
        return true;
    }

    @Override
    public boolean tryLock() {
        boolean success = tryLock0();
        if (success) {
            lockWatchDog(key);
        }
        LockLogUtils.info("tryLock lockIndex:{} key:{} success:{}", lockIndexName(), key, success);
        return success;
    }
    
    public boolean tryLock(String value) {
        boolean success = tryLock0(value);
        if (success) {
            lockWatchDog(key);
        }
        LockLogUtils.info("tryLock lockIndex:{} key:{} success:{}", lockIndexName(), key, success);
        return success;
    }
    
    @Override
    public void unlock() {
        removeWatchDog(key);
        try {
            unlock0();
        } catch (ElasticsearchException e) {
            //找不到
            if (e.status() == RestStatus.NOT_FOUND) {
                LockLogUtils.info("unlock not_found lockIndex:{} key:{}", lockIndexName(), key);
            } else {
                throw new EsException(e);
            }
        }
        LockLogUtils.info("unlock lockIndex:{} key:{}", lockIndexName(), key);
    }

    /**
     * 被锁定
     */
    public boolean isLocked() {
        SearchResponse response = esPlusLockClient.search(lockIndexName(), ID_FIELD,key);
        SearchHits searchHits = response.getHits();
        if (searchHits != null && searchHits.getHits() != null && searchHits.getHits().length > 0) {
            for (SearchHit hit : response.getHits().getHits()) {
                Long expireTime = (Long) hit.getSourceAsMap().get(GLOBAL_LOCK_EXPIRETIME);
                // 锁过期
                if (expireTime - System.currentTimeMillis() < 0) {
                    unlock();
                    return false;
                } else {
                    return true;
                }
            }
        }
        //小于等于0就是没有锁
        return false;
    }

    public abstract String lockIndexName();

    public abstract boolean tryLock0();
    
    public abstract boolean tryLock0(String value);
    
    public abstract void unlock0();

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }


    private void lockWatchDog(String lockValue) {
        ScheduledFuture<?> scheduledFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> extensionLock(lockValue), WATCH_DOG_RELOCK, WATCH_DOG_RELOCK, TimeUnit.SECONDS);
        WATCH_DOGS.put(lockValue, scheduledFuture);
    }

    /**
     * 续期锁
     */
    private void extensionLock(String lockValue) {
        Map<String, Object> lockMap = new HashMap<>();
        lockMap.put(GLOBAL_LOCK_EXPIRETIME, System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000);
        lockMap.put("id", lockValue);
        try {
            UpdateResponse update = esPlusLockClient.update(lockIndexName(), lockMap);
            LockLogUtils.info("extensionLock success:{}", update);
        } catch (ElasticsearchException e) {
            removeWatchDog(lockValue);
        }
    }

    /**
     * 删除看狗
     *
     * @param lockValue 锁价值
     */
    private void removeWatchDog(String lockValue) {
        ScheduledFuture<?> scheduledFuture = WATCH_DOGS.get(lockValue);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            WATCH_DOGS.remove(lockValue);
        }
    }

}
