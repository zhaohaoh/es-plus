package com.es.plus.core.service;


import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

public interface EsService<T> {

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
    EsUpdateWrapper<T> esUpdateWrapper();

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
    EsChainUpdateWrapper<T> esChainUpdateWrapper();

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
    boolean updateSettings(EsSettings esSettings);

    /**
     * 更新设置
     *
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(Map<String, Object> esSettings);

    /**
     * 保存
     *
     * @param entity 实体
     * @return boolean
     */
    boolean save(T entity);

    /**
     * 保存或更新
     *
     * @param entity 实体
     * @return boolean
     */
    boolean saveOrUpdate(T entity);

    /**
     * 保存或更新批处理
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList);

    /**
     * 保存批处理
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveBatch(Collection<T> entityList);
    
    /**
     * 批量保存或更新
     */

     void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList);
    
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */

     void saveBatchAsyncProcessor(Collection<T> entityList);
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */

     void updateBatchAsyncProcessor(Collection<T> entityList) ;
    /**
     * 删除根据id
     *
     * @param id id
     * @return boolean
     */
    boolean removeById(Serializable id);

    /**
     * 删除根据id
     *
     * @param idList id列表
     * @return boolean
     */
    boolean removeByIds(Collection<? extends Serializable> idList);

    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装器
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper);

    /**
     * 删除所有
     *
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse removeAll();

    /**
     * 更新根据id
     *
     * @param entity 实体
     * @return boolean
     */
    boolean updateById(T entity);

    /**
     * 批处理更新
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> updateBatch(Collection<T> entityList);


    /**
     * 删除索引
     */
    void deleteIndex();

    /**
     * 合并
     *
     */
    boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush);
    /**
     * 强制刷
     * @return boolean
     */
    boolean refresh();

    /**
     * 更新根据包装器
     *
     * @param esUpdateWrapper es更新包装器
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse updateByQuery(EsWrapper<T> esUpdateWrapper);

    /**
     * 获取根据id
     *
     * @param id id
     * @return {@link T}
     */
    T searchById(Serializable id);

    /**
     * 列表根据id
     *
     * @param idList id列表
     * @return {@link List}<{@link T}>
     */
    List<T> searchByIds(Collection<? extends Serializable> idList);

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
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse increment(EsWrapper<T> esUpdateWrapper);
}
