package com.es.plus.autoconfigure.interceptor;

import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.interceptor.EsInterceptor;
import com.es.plus.adapter.interceptor.EsInterceptors;
import com.es.plus.adapter.interceptor.InterceptorElement;
import com.es.plus.adapter.interceptor.MethodEnum;
import com.es.plus.adapter.lock.ELock;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *  期望实现reindex的时候双写索引。具体怎么做还没想好，因为拦截器只能提前注入。
 */
@Slf4j
@EsInterceptors(value = {
        @InterceptorElement(type = EsPlusClient.class,methods = {MethodEnum.SAVE,MethodEnum.UPDATE,MethodEnum.DELETE})
})
public class EsReindexInterceptor implements EsInterceptor {
    
   
    private boolean reindexIntercptor;
 
    private final  ELock eLock;
    
    public EsReindexInterceptor(ELock eLock) {
        this.eLock = eLock;
    }
    
    public void setReindexIntercptor(boolean reindexIntercptor) {
        this.reindexIntercptor = reindexIntercptor;
    }
    
    @Override
    public void after(String index, String type, Method method, Object[] args, Object result,
            EsPlusClient esPlusClient) {
        
        if (!reindexIntercptor){
            return;
        }
        Object lockValue = eLock.getLockValue();
        if (lockValue!=null){
            Object[] newArr = Arrays.copyOf(args, args.length);
            Object arg = args[0];
            if (arg.equals(index)){
                String newIndex = (String) lockValue;
                newArr[0] = newIndex;
            }
            
            if (newArr[0].equals(index)){
                log.info("执行reindex拦截器， index相同 :{}",newArr[0]);
                return;
            }
         
            try {
                log.info("执行reindex拦截器，修改index为：{}",newArr);
                method.invoke(esPlusClient,newArr);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
