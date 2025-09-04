package com.es.plus.common.interceptor;

/**
 * 拦截器本地上下文
 *
 * @author hzh
 * @date 2023/12/19
 */
public class InterceptorLocalContext {
    
    private static final ThreadLocal<Boolean> LOCAL_CONTEXT = new ThreadLocal<>();
    
    public static void enable() {
        LOCAL_CONTEXT.set(true);
    }
    
    public static void disable() {
        LOCAL_CONTEXT.set(false);
    }
    public static Boolean get() {
       return LOCAL_CONTEXT.get();
    }
    public static void remove() {
        LOCAL_CONTEXT.remove();
    }
    
    public static void main(String[] args) {
        Boolean aBoolean = LOCAL_CONTEXT.get();
    
    }
}
