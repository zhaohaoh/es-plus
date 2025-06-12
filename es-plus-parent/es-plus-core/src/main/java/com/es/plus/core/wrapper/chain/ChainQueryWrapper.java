package com.es.plus.core.wrapper.chain;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;

import java.time.Duration;

interface ChainQueryWrapper<T> {
    /**
     * 列表
     *
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> search();

    /**
     * 列表
     */
    EsResponse<T> search(int size);

    /**
     * 分页
     *
     * @param page 页面
     * @param size 大小
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> searchPage(int page, int size);

    /**
     * 聚合
     */
    EsAggResponse<T> aggregations();

    /**
     * 统计
     *
     * @return long
     */
    long count();

    /**
     * 滚动
     *
     * @param size    大小
     * @param scollId scoll id
     */
    EsResponse<T> scroll(int size, String scollId);

    /**
     * 滚动
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     */
    EsResponse<T> scroll(int size, Duration keepTime, String scollId);

    /**
     * 性能分析
     */
    EsResponse<T> profile();

    /**
     * 执行dsl
     *
     * @param dsl dsl
     * @return {@link String}
     */
    String executeDSL(String dsl);
    /**
     *  翻译sql
     *
     * @return {@link String}
     */
    String translateSQL(String sql);
    
    /**
     * 执行sql
     *
     * @return {@link String}
     */
    EsResponse<T> executeSQLep(String sql);
    
    String executeSQL(String sql);
}
