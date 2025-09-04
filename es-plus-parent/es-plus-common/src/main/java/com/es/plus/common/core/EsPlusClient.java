package com.es.plus.common.core;

import com.es.plus.common.params.EsAggResponse;
import com.es.plus.common.params.EsIndexResponse;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.params.EsResponse;
import com.es.plus.common.pojo.es.EpBulkResponse;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public interface EsPlusClient {

    Object getEsClient();
    
    
    /**
     * 异步定时批量保存接口
     */
    void saveOrUpdateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs);
    
    /**
     * 异步定时批量保存接口
     */
    void saveBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs);
    
    /**
     * 异步定时批量保存接口
     */
    void updateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs);
   
    /**
     * 保存或更新批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     */
    List<String> saveOrUpdateBatch(String type, Collection<?> esDataList,String... index);

    /**
     * 保存批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     */
    List<String> saveBatch(String type, Collection<?> esDataList,String... index);

    /**
     * 保存
     */
    boolean save(String type, Object esData,String... index);
    
    /**
     * 保存或者更新
     */
    <T> boolean saveOrUpdate(String type, T entity, String... index);

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    boolean update(String type, Object esData,String... index);

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     */
    List<String> updateBatch(String type, Collection<?> esDataList,String... index);

    /**
     * 更新包装
     */
    <T> EpBulkResponse updateByWrapper( String type, EsParamWrapper<T> esUpdateWrapper,String... index);

    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link EpBulkResponse}
     */
    <T> EpBulkResponse increment(String type, EsParamWrapper<T> esUpdateWrapper,String... index);

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    boolean delete(String type, String id,String... index);

    /**
     * 删除,查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link EpBulkResponse}
     */
    <T> EpBulkResponse deleteByQuery(String type, EsParamWrapper<T> esUpdateWrapper,String... index);

    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 西文数据列表
     * @return boolean
     */
    boolean deleteBatch(String type, Collection<String> esDataList,String... index);

    /**
     * 统计数
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @return long
     */
    <T> long count(String type, EsParamWrapper<T> esParamWrapper,String... index);

    /**
     * 搜索包装
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @return {@link EsResponse}<{@link T}>
     */
    <T> EsResponse<T> search(String type, EsParamWrapper<T> esParamWrapper,String... index);

    /**
     * 滚动查询包装
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @param keepTime       保持时间
     * @return
     */
    <T> EsResponse<T> scroll(String type, EsParamWrapper<T> esParamWrapper, Duration keepTime, String scrollId,String... index);

    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es查询包装
     */
    <T> EsAggResponse<T> aggregations(String type, EsParamWrapper<T> esParamWrapper,String... index);


    /**
     * 执行dsl
     *
     * @param dsl       dsl
     * @param index 索引名字
     * @return {@link String}
     */
    String executeDSL(String dsl, String... index);
    
    /**
     * 翻译sql
     *
     * @return {@link String}
     */
    String translateSql(String sql);
    /**
     * 执行sql
     *
     * @return {@link String}
     */
    <T> EsResponse<T> executeSQL(String sql,Class<T> tClass);
    
    
    String executeSQL(String sql);
    
    String sql2Dsl(String sql,boolean explain);
    /**
     * 获取映射
     * @param indexName
     * @return
     */
    EsIndexResponse getMappings(String indexName);
    
    String explain(String sql);
}
