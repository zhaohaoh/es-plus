package com.es.plus.web.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class EsClientCache {
    
    public static  final Cache<String,String> CACHE_MAP = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .initialCapacity(100)
            .maximumSize(100)
            .build();
}
