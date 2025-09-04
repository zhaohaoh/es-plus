package com.es.plus.core.service;


import com.es.plus.common.params.EsAggResponse;
import com.es.plus.common.params.EsResponse;
import com.es.plus.common.params.EsSettings;
import com.es.plus.common.pojo.es.EpBulkResponse;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaUpdateWrapper;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

public interface EsService<T> {
    
    String[] getIndex();
    
    String[] getAlias();

    /**
     * es查询包装器
     *
     * @return {@link EsQueryWrapper}<{@link T}>
     */
    EsQueryWrapper<T> esQueryWrapper();

    /**
     * es更新包装器
     *
     * @return {@link EsUpdateWrapper}<{@link T}>
     */
    EsChainUpdateWrapper<T> esUpdateWrapper();

    /**
     * es链查询包装器
     *
     * @return {@link EsChainLambdaQueryWrapper}<{@link T}>
     */
    EsChainLambdaQueryWrapper<T> esChainQueryWrapper();

    /**
     * es链更新包装器
     *
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    EsChainLambdaUpdateWrapper<T> esChainUpdateWrapper();

    /**
     * 创建索引
     */
    void createIndex();

    /**
     * 创建索引映射
     */
    void createIndexMapping();

    /**
     * 创建映射
     */
    void createMapping();

    /**
     * 更新设置
     *
     * @param esSettings es设置
     * @return boolean
     */
    default boolean updateSettings(EsSettings esSettings){
     return  updateSettings(esSettings,getIndex());
    }
    
    /**
     * 更新设置
     *
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(EsSettings esSettings,String... indexs);
    
    default boolean updateSettings(Map<String, Object> esSettings){
        return  updateSettings(esSettings,getIndex());
    }
    /**
     * 更新设置
     *
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(Map<String, Object> esSettings,String... indexs);
    
    default boolean save(T entity){
        return  save(entity,getIndex());
    }
    /**
     * 保存
     *
     * @param entity 实体
     * @return boolean
     */
    boolean save(T entity,String... indexs);
    
    
    default boolean saveOrUpdate(T entity){
        return  saveOrUpdate(entity,getIndex());
    }
    /**
     * 保存或更新
     *
     * @param entity 实体
     * @return boolean
     */
    boolean saveOrUpdate(T entity,String... indexs);
    
    default List<String> saveOrUpdateBatch(Collection<T> entityList){
        return  saveOrUpdateBatch(entityList,getIndex());
    }
    /**
     * 保存或更新批处理
     *
     * @param entityList 实体列表
     */
    List<String> saveOrUpdateBatch(Collection<T> entityList,String... indexs);
    
    default List<String> saveBatch(Collection<T> entityList){
        return  saveBatch(entityList,getIndex());
    }
    /**
     * 保存批处理
     *
     * @param entityList 实体列表
     */
    List<String> saveBatch(Collection<T> entityList,String... indexs);
    
    /**
     * 批量保存或更新
     */
    default void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList) {
        saveOrUpdateBatchAsyncProcessor(entityList, getIndex());
    }
    
    void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList, String... indexs);
    
    
    default void saveBatchAsyncProcessor(Collection<T> entityList) {
        saveBatchAsyncProcessor(entityList, getIndex());
    }
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     */

     void saveBatchAsyncProcessor(Collection<T> entityList,String... indexs);
    
    default void updateBatchAsyncProcessor(Collection<T> entityList) {
        updateBatchAsyncProcessor(entityList, getIndex());
    }
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     */

     void updateBatchAsyncProcessor(Collection<T> entityList,String... indexs) ;
    
    default boolean removeById(Serializable id) {
       return removeById(id, getIndex());
    }
    /**
     * 删除根据id
     *
     * @param id id
     * @return boolean
     */
    boolean removeById(Serializable id,String... indexs);
    
    default boolean removeByIds(Collection<? extends Serializable> idList) {
       return removeByIds(idList, getIndex());
    }
    /**
     * 删除根据id
     *
     * @param idList id列表
     * @return boolean
     */
    boolean removeByIds(Collection<? extends Serializable> idList,String... indexs);
    
    
    default boolean updateById(T entity) {
       return updateById(entity, getIndex());
    }
    /**
     * 更新根据id
     *
     * @param entity 实体
     * @return boolean
     */
    boolean updateById(T entity,String... indexs);
    
    
    default List<String> updateBatch(Collection<T> entityList) {
       return updateBatch(entityList, getIndex());
    }
    /**
     * 批处理更新
     *
     * @param entityList 实体列表
     */
    List<String> updateBatch(Collection<T> entityList,String... indexs);
    
    default void deleteIndex() {
        deleteIndex(getIndex());
    }

    /**
     * 删除索引
     */
    void deleteIndex(String... indexs);
    
    default boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush) {
        return forceMerge(maxSegments, onlyExpungeDeletes, flush, getIndex());
    }
    /**
     * 合并
     *
     */
    boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush,String... indexs);
    
    /**
     * 强制刷
     * @return boolean
     */
    default boolean refresh() {
        return refresh(getIndex());
    }
    /**
     * 强制刷
     * @return boolean
     */
    boolean refresh(String... indexs);
    
    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装器
     */
    EpBulkResponse remove(EsWrapper<T> esUpdateWrapper);
    /**
     * 更新根据包装器
     *
     * @param esUpdateWrapper es更新包装器
     */
    EpBulkResponse updateByQuery(EsWrapper<T> esUpdateWrapper);
    
    /**
     * 获取根据id
     *
     * @param id id
     * @return {@link T}
     */
    default T searchById(Serializable id) {
        return searchById(id,getAlias());
    }

    /**
     * 获取根据id
     *
     * @param id id
     * @return {@link T}
     */
    T searchById(Serializable id,String... indexs);
    
    /**
     * 获取根据ids
     */
    default List<T> searchById(Collection<? extends Serializable> idList) {
        return searchByIds(idList,getAlias());
    }
    /**
     * 列表根据id
     *
     * @param idList id列表
     * @return {@link List}<{@link T}>
     */
    List<T> searchByIds(Collection<? extends Serializable> idList,String... indexs);

    /**
     * 列表
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> search(EsWrapper<T> esQueryWrapper);
    /**
     * 列表
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> search(EsWrapper<T> esQueryWrapper,int size);

    /**
     * 页面
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> searchPage(int page,int size, EsWrapper<T> esQueryWrapper);

    /**
     * 统计
     *
     * @param esQueryWrapper es查询包装器
     * @return long
     */
    long count(EsWrapper<T> esQueryWrapper);

    /**
     * 聚合
     *
     * @param esQueryWrapper es查询包装器
     */
    EsAggResponse<T> aggregations(EsWrapper<T> esQueryWrapper);

    /**
     * 性能分析
     *
     * @param esQueryWrapper es查询包装
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> profile(EsWrapper<T> esQueryWrapper);

    /**
     * 滚动
     *
     * @param esQueryWrapper es查询包装器
     * @param size           大小
     * @param scollId        scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    default EsResponse<T> scroll(EsWrapper<T> esQueryWrapper, int size, String scollId) {
        return scroll(esQueryWrapper, size, SCROLL_KEEP_TIME, scollId);
    }

    /**
     * 滚动
     *
     * @param esQueryWrapper es查询包装器
     * @param size           大小
     * @param keepTime       保持时间
     * @param scollId        scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> scroll(EsWrapper<T> esQueryWrapper, int size, Duration keepTime, String scollId);

    /**
     * 自增
     *
     * @param esUpdateWrapper es更新包装器
     */
    EpBulkResponse increment(EsWrapper<T> esUpdateWrapper);
}
