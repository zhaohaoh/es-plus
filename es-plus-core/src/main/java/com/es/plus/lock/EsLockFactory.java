package com.es.plus.lock;


/**
 * @Author: hzh
 * @Date: 2022/9/13 16:13
 */
public class EsLockFactory {
    private final ELockClient esPlusLockClient;

    public EsLockFactory(ELockClient esPlusLockClient) {
        this.esPlusLockClient = esPlusLockClient;
    }

    public EsReadWriteLock getReadWrtieLock(String key) {
        return new EsReadWriteLock(esPlusLockClient,key);
    }

    public ELock getLock(String key) {
        return new EsLock(esPlusLockClient,key);
    }

}
