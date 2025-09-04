package com.es.plus.common.config;

import com.es.plus.common.constants.GlobalConfig;

import java.util.Map;

/**
 * 配置
 */
public class GlobalConfigCache {
    /**
     * 配置
     */
    public static GlobalConfig GLOBAL_CONFIG;
    /**
     * 自动设置字段配置
     */
    public static Map<String,EsObjectHandler> ES_OBJECT_HANDLER;

}
