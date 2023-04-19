package com.es.plus.adapter.lock;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author: hzh
 * @Date: 2022/9/9 15:01
 */

public  class EsReadWriteLock implements ReadWriteLock {
    private final ELockClient esPlusLockClient;
    private final String key;

    public EsReadWriteLock(ELockClient esPlusLockClient, String key) {
        this.esPlusLockClient = esPlusLockClient;
        this.key = key;
    }

    @Override
    public Lock readLock() {
        return new EsReadLock(esPlusLockClient,key);
    }

    @Override
    public Lock writeLock() {
        return new EsWriteLock(esPlusLockClient,key);
    }
}
