package com.es.plus.client;

import com.es.plus.core.ScrollHandler;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.pojo.EsAggregationsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.*;

public interface EsPlusClient {

    /**
     * 保存或更新批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveOrUpdateBatch(String index, Collection<?> esDataList);
    /**
     * 保存批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveBatch(String index, Collection<?> esDataList);

    /**
     * 保存
     */
    boolean save(String index, Object esData);

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    boolean update(String index, Object esData);

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> updateBatch(String index, Collection<?> esDataList);

    /**
     * 更新包装
     */
    <T> BulkByScrollResponse updateByWrapper(String index, EsUpdateWrapper<T> esUpdateWrapper);

    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    <T> BulkByScrollResponse increment(String index, EsUpdateWrapper<T> esUpdateWrapper);

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    boolean delete(String index, String id);

    <T> BulkByScrollResponse deleteByQuery(String index, EsUpdateWrapper<T> esUpdateWrapper);

    boolean deleteBatch(String index, Collection<String> esDataList);

    <T> long count(EsQueryWrapper<T> esQueryWrapper, String index);

    <T> EsResponse<T> searchByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index);

    <T> EsResponse<T> searchPageByWrapper(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index);

    <T> void scrollByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index, int size, int keepTime, ScrollHandler<T> scrollHandler);

    <T> EsAggregationsResponse<T> aggregations(String index, EsQueryWrapper<T> esQueryWrapper);

}
