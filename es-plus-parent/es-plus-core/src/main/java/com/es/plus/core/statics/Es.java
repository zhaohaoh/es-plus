package com.es.plus.core.statics;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.core.ClientContext;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaUpdateWrapper;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.core.wrapper.core.EsIndexWrapper;

import java.util.Map;

/**
 * @author hzh
 * @date 2023/02/06
 * 静态es链式操作类
 */
public class Es {

    /**
     * 函数链查询
     *
     * @param rClass r类
     * @return {@link EsChainLambdaQueryWrapper}<{@link T}>
     */
    public static <T> EsChainLambdaQueryWrapper<T> chainLambdaQuery(Class<T> rClass) {
        return new EsChainLambdaQueryWrapper<>(rClass);
    }

    /**
     * 链查询
     *
     * @param rClass r类
     * @return {@link EsChainQueryWrapper}<{@link T}>
     */
    public static <T> EsChainQueryWrapper<T> chainQuery(Class<T> rClass) {
        return new EsChainQueryWrapper<>(rClass);
    }
    /**
     * 无参默认以Map作为链式查询链查询
     */
    public static EsChainQueryWrapper<Map> chainQuery() {
        return new EsChainQueryWrapper<>(Map.class);
    }
    
    /**
     * 链查询
     *
     * @param rClass r类
     * @return {@link EsChainQueryWrapper}<{@link T}>
     */
    public static  EsChainQueryWrapper<Map> chainQuery(String esClientName) {
        return new EsChainQueryWrapper<>(Map.class, ClientContext.getClient(esClientName));
    }

    /**
     * 函数链查询
     *
     * @param rClass r类
     * @return {@link EsChainLambdaQueryWrapper}<{@link T}>
     */
    public static <T> EsChainLambdaQueryWrapper<T> chainLambdaQuery(EsPlusClientFacade esPlusClientFacade, Class<T> rClass) {
        return new EsChainLambdaQueryWrapper<>(rClass, esPlusClientFacade);
    }

    /**
     * 链查询
     *
     * @param rClass r类
     * @return {@link EsChainQueryWrapper}<{@link T}>
     */
    public static <T> EsChainQueryWrapper<T> chainQuery(EsPlusClientFacade esPlusClientFacade, Class<T> rClass) {
        return new EsChainQueryWrapper<>(rClass, esPlusClientFacade);
    }
    
  

    /**
     * 函数链更新
     *
     * @param rClass r类
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    public static <T> EsChainLambdaUpdateWrapper<T> chainLambdaUpdate(Class<T> rClass) {
        return new EsChainLambdaUpdateWrapper<>(rClass);
    }

    /**
     * 链更新
     *
     * @param rClass r类
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    public static <T> EsChainUpdateWrapper<T> chainUpdate(Class<T> rClass) {
        return new EsChainUpdateWrapper<>(rClass);
    }


    /**
     * 函数链更新
     *
     * @param rClass r类
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    public static <T> EsChainLambdaUpdateWrapper<T> chainLambdaUpdate(EsPlusClientFacade esPlusClientFacade, Class<T> rClass) {
        return new EsChainLambdaUpdateWrapper<>(rClass, esPlusClientFacade);
    }

    /**
     * 链更新
     *
     * @param rClass r类
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    public static <T> EsChainUpdateWrapper<T> chainUpdate(EsPlusClientFacade esPlusClientFacade, Class<T> rClass) {
        return new EsChainUpdateWrapper<>(rClass, esPlusClientFacade);
    }


    /**
     * 链更新
     *
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    public static <T> EsIndexWrapper chainIndex(EsPlusClientFacade esPlusClientFacade) {
        return new EsIndexWrapper(esPlusClientFacade);
    }
    /**
     * 索引
     *
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    public static <T> EsIndexWrapper chainIndex() {
        return new EsIndexWrapper();
    }
    
}
