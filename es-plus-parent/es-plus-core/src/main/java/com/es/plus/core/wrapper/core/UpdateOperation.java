package com.es.plus.core.wrapper.core;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collection;
import java.util.List;

public interface UpdateOperation<T> {

    /**
     * 保存
     *
     * @param t t
     * @return boolean
     */
    boolean save(T t);
    
    /**
     *  更新
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    boolean saveOrUpdate(T t);

    /**
     * 保存批处理
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveBatch(Collection<T> t);

    /**
     * 保存或更新批处理
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList);

    /**
     *  更新
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
     boolean update(T t);

    /**
     * 批处理更新
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> updateBatch(Collection<T> t);

    /**
     * 更新
     *
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse updateByQuery();


    /**
     * 增量
     *
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse incrementByWapper();


    /**
     * 删除
     *
     * @return {@link BulkByScrollResponse}
     */
    boolean removeByIds(Collection<String> ids);

    /**
     * 删除
     *
     * @return {@link BulkByScrollResponse}
     */
    BulkByScrollResponse remove();


}
