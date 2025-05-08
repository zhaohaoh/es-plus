package com.es.plus.web.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 全局异常处理器
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalBizExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public Map handleCommonException(Exception e) {
        log.error("系统异常", e);
        Map<String,Object> map =new HashMap<>();
        map.put("code",500);
        map.put("message",e.getMessage());
        return map;
    }
    
}
