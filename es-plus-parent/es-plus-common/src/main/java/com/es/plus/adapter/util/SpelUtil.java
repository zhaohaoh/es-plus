package com.es.plus.adapter.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * spel工具类
 *
 * @author hzh
 * @date 2023/06/29
 */
@Component
public class SpelUtil implements BeanFactoryAware {
    private static final Pattern COMPILED = Pattern.compile("#\\{([^}]+)}");
    private static final Logger logger = LoggerFactory.getLogger(SpelUtil.class);
    
    private static final StandardReflectionParameterNameDiscoverer u = new StandardReflectionParameterNameDiscoverer();
    
    //使用SPEL进行key的解析
    private static final ExpressionParser parser = new SpelExpressionParser();
    
    //SPEL上下文
    private static final StandardEvaluationContext context = new StandardEvaluationContext();
 
    
 
    
    /**
     * 获取的key key 定义在注解上，支持SPEL表达式
     *
     * @return
     */
    public static String parseSpelValue(String key, Method method, Object[] args) {
        if (StringUtils.isEmpty(key)) {
            return key;
        }
        //获取被拦截方法参数名列表(使用Spring支持类库)
        String[] paramNameArr = u.getParameterNames(method);
        if (paramNameArr == null) {
            return key;
        }
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paramNameArr.length; i++) {
            context.setVariable(paramNameArr[i], args[i]);
        }
        Expression expression = parser.parseExpression(key);
        String expressionString = expression.getExpressionString();
        for (String param : paramNameArr) {
            if (expressionString.contains(param)) {
                try {
                    return expression.getValue(context, String.class);
                } catch (Exception e) {
                    logger.error("spel error", e);
                }
            }
        }
        return key;
    }
    
    /**
     *
     * @param key
     * @return
     */
    public static String parseSpelValue(String key) {
        try {
            if (StringUtils.isEmpty(key)) {
                return key;
            }
            String spel = parseSpecialSpel(key);
            if (spel == null ||spel.isEmpty()) {
                return key;
            }
            
            Expression expression = parser.parseExpression(spel);
            String value = (String) expression.getValue(context);
            return value;
        }catch (Exception e)  {
         logger.error("spel parseSpelValue error", e);
          return null;
        }
    }
    
    public static <T> T parseSpelValue(String key, Class<T> tClass) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String spel = parseSpecialSpel(key);
        if (spel == null) {
            return null;
        }
        
        Expression expression = parser.parseExpression(spel);
        T value = expression.getValue(context, tClass);
        return value;
    }
    
    public static Object parseSpelObjectValue(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String spel = parseSpecialSpel(key);
        if (spel == null) {
            return null;
        }
        
        Expression expression = parser.parseExpression(spel);
        Object value = expression.getValue(context);
        return value;
    }
    
    /**
     * 解析SPEL表达式。spel的内容在#{}中
     */
    public static String parseSpecialSpel(String key) {
        Matcher matcher = COMPILED.matcher(key);

        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(beanFactory);
        context.setBeanResolver(beanFactoryResolver);
    }
}
