package com.es.plus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockLogUtils {
    /**
     * 日志打印工具名称
     */
    private final static String LOGGER_NAME = "es-lock";
    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(LOGGER_NAME);

    /**
     * 打印info级别日志
     *
     * @param params 参数
     */
    public static void info(String body, Object... params) {
        log.info(body, params);
    }
    public static void error(String body, Object... params) {
        log.error(body, params);
    }
    public static void error(String body, Throwable params) {
        log.error(body, params);
    }
}
