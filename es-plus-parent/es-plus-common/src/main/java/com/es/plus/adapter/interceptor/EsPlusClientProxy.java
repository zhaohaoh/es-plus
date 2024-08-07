package com.es.plus.adapter.interceptor;

import com.es.plus.adapter.core.EsPlusClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.es.plus.constant.EsConstant.INDEX;
import static com.es.plus.constant.EsConstant.TYPE;

@Slf4j
public class EsPlusClientProxy implements InvocationHandler {
    
    private final Object target;
    
    private final Class<?>[] proxiedInterfaces;
    
    private List<EsInterceptor> esInterceptors=new ArrayList<>();
    

    
    /**
     * Is the {@link #equals} method defined on the proxied interfaces?
     */
    private boolean equalsDefined;
    
    /**
     * Is the {@link #hashCode} method defined on the proxied interfaces?
     */
    private boolean hashCodeDefined;
    
    public EsPlusClientProxy(Object target, List<EsInterceptor> esInterceptors) {
        this.target = target;
        this.proxiedInterfaces = target.getClass().getInterfaces();
         if (!CollectionUtils.isEmpty(esInterceptors)){
             this.esInterceptors = esInterceptors;
          }
    }
    
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }
    
    
    public Object getProxy(@Nullable ClassLoader classLoader) {
        Object proxyInstance = Proxy.newProxyInstance(classLoader, this.proxiedInterfaces, this);
        findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
        return proxyInstance;
    }
    
    public List<EsInterceptor> getEsInterceptors() {
        return esInterceptors;
    }
    
    public void addInterceptor(EsInterceptor esInterceptor){
        boolean anyMatch = esInterceptors.stream().anyMatch(a -> a.getClass().equals(esInterceptor.getClass()));
        //已经存在相同类型的拦截器
        if (anyMatch){
            return;
        }
        esInterceptors.add(esInterceptor);
    }
    
    public void removeInterceptor(Class<?> clazz) {
        esInterceptors.removeIf(a->a.getClass().equals(clazz));
    }
    
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        String index = com.es.plus.adapter.util.ClassUtils.getParamValue(INDEX, method, args,target);
        String type =com.es.plus.adapter.util.ClassUtils.getParamValue(TYPE, method, args,target);
        String methodName = method.getName();
        try {
            if (isHashCodeMethod(method) && !hashCodeDefined) {
                return method.invoke(target, args);
            }
            if (isEqualsMethod(method) && !equalsDefined) {
                return method.invoke(target, args);
            }
            if (isToStringMethod(method)) {
                return method.invoke(target, args);
            }
            if (isIngoreMethod(method)) {
                return method.invoke(target, args);
            }
            
            //如果本地线程强制关闭拦截器
            Boolean isInterceptor = InterceptorLocalContext.get();
            if (isInterceptor != null && !isInterceptor) {
                return method.invoke(target, args);
            }
            
            List<EsInterceptor> esInterceptors = getEsInterceptors(methodName, index);
            if (CollectionUtils.isEmpty(esInterceptors)) {
                return method.invoke(target, args);
            }
            
            //前置处理
            for (EsInterceptor esInterceptor : esInterceptors) {
                esInterceptor.before(index, type, method, args,(EsPlusClient)target);
            }
            
            result = method.invoke(target, args);
            
            //后置过程
            for (EsInterceptor esInterceptor : esInterceptors) {
                esInterceptor.after(index, type, method, args, result,(EsPlusClient)target);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }
    
    private List<EsInterceptor> getEsInterceptors(String name, String index) {
        List<EsInterceptor> canInterceptors = new ArrayList<>();
        if (CollectionUtils.isEmpty(esInterceptors)) {
            return canInterceptors;
        }
        for (EsInterceptor esInterceptor : esInterceptors) {
            EsInterceptors interceptors = AnnotationUtils
                    .findAnnotation(esInterceptor.getClass(), EsInterceptors.class);
            if (interceptors == null) {
                log.info("EsInterceptors is null");
            }
            InterceptorElement[] elements = interceptors.value();
            boolean isInterceptor = false;
            for (InterceptorElement element : elements) {
                String[] indexs = element.index();
                String[] methodNames = element.methodName();
                //如果方法是空取方法枚举
                if (ArrayUtils.isEmpty(methodNames)){
                    if (!ArrayUtils.isEmpty(element.methods())){
                        methodNames= Arrays.stream(element.methods()).map(MethodEnum::getMethods)
                                .flatMap(Arrays::stream).toArray(String[]::new);
                    }
                }
                
                Class<?> aClass = com.es.plus.adapter.util.ClassUtils.getClass(target.getClass());
                if (!element.type().isAssignableFrom(aClass)) {
                    continue;
                }
                if (ArrayUtils.isNotEmpty(methodNames)) {
                    boolean containsMethod = ArrayUtils.contains(methodNames, name);
                    if (!containsMethod) {
                        continue;
                    }
                }
                if (ArrayUtils.isNotEmpty(indexs)) {
                    boolean containsIndex = ArrayUtils.contains(indexs, index);
                    if (!containsIndex) {
                        continue;
                    }
                }
                isInterceptor = true;
            }
            if (isInterceptor) {
                canInterceptors.add(esInterceptor);
            }
        }
        return canInterceptors;
    }
    
  
    
    
    //查询是否有自定义的方法。是否重写tostring和
    private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
        for (Class<?> proxiedInterface : proxiedInterfaces) {
            Method[] methods = proxiedInterface.getDeclaredMethods();
            for (Method method : methods) {
                if (AopUtils.isEqualsMethod(method)) {
                    this.equalsDefined = true;
                }
                if (AopUtils.isHashCodeMethod(method)) {
                    this.hashCodeDefined = true;
                }
                if (this.equalsDefined && this.hashCodeDefined) {
                    return;
                }
            }
        }
    }
    
    public static boolean isIngoreMethod(@Nullable Method method) {
        String[] strings = {"getRestHighLevelClient", "getReindexState", "setReindexState"};
        boolean match = Arrays.stream(strings)
                .anyMatch(a -> method != null && method.getParameterCount() == 0 && a.equals(method.getName()));
        return match;
    }
    
    
    public static boolean isHashCodeMethod(@Nullable Method method) {
        return method != null && method.getParameterCount() == 0 && "hashCode".equals(method.getName());
    }
    
    /**
     * Determine whether the given method is a "toString" method.
     *
     * @see java.lang.Object#toString()
     */
    public static boolean isToStringMethod(@Nullable Method method) {
        return (method != null && method.getParameterCount() == 0 && "toString".equals(method.getName()));
    }
    
    public static boolean isEqualsMethod(@Nullable Method method) {
        if (method == null) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        if (!method.getName().equals("equals")) {
            return false;
        }
        return method.getParameterTypes()[0] == Object.class;
    }
    
 
}