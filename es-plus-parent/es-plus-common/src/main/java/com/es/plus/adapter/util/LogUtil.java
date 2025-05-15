package com.es.plus.adapter.util;

import org.apache.commons.lang3.StringUtils;

public class LogUtil {
    
    public static String logSubstring(String jsonStr) {
        String res = StringUtils.substring(jsonStr, 0, 10000) + ".....省略日志";
        return res;
    }
    
}
