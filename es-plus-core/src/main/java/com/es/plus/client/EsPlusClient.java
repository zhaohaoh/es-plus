package com.es.plus.client;

import com.es.plus.core.params.EsParamWrapper;
import com.es.plus.pojo.EsAggsResponse;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collection;
import java.util.List;

public interface EsPlusClient {
    /**
     * 是否在执行reindex
     */
    boolean getReindexState();

    /**
     * 设置reindex状态
     */
    void setReindexState(boolean reindexState);

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
    <T> BulkByScrollResponse updateByWrapper(String index, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    <T> BulkByScrollResponse increment(String index, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    boolean delete(String index, String id);

    /**
     * 删除,查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    <T> BulkByScrollResponse deleteByQuery(String index, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 西文数据列表
     * @return boolean
     */
    boolean deleteBatch(String index, Collection<String> esDataList);

    /**
     * 统计数
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @return long
     */
    <T> long count(EsParamWrapper<T> esParamWrapper, String index);

    /**
     * 搜索包装
     *
     * @param esParamWrapper es查询包装
     * @param tClass         t类
     * @param index          指数
     * @return {@link EsResponse}<{@link T}>
     */
    <T> EsResponse<T> searchByWrapper(EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index);

    /**
     * 搜索页面包装
     *
     * @param pageInfo       页面信息
     * @param esParamWrapper es查询包装
     * @param tClass         t类
     * @param index          指数
     * @return {@link EsResponse}<{@link T}>
     */
    <T> EsResponse<T> searchPageByWrapper(PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index);

    /**
     * 滚动查询包装
     *
     * @param scrollHandler  滚动处理程序
     * @param esParamWrapper es查询包装
     * @param tClass         t类
     * @param index          指数
     * @param size           大小
     * @param keepTime       保持时间
     * @return
     */
    <T>  EsResponse<T> scrollByWrapper(EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index, int size, int keepTime, String scrollId);

    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es查询包装
     * @return {@link EsAggsResponse}<{@link T}>
     */
    <T> EsAggsResponse<T> aggregations(String index, EsParamWrapper<T> esParamWrapper, Class<T> tClass);

    /**
     * 搜索后
     *
     * @param pageInfo       页面信息
     * @param esParamWrapper es参数包装器
     * @param tClass         t类
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    <T> EsResponse<T> searchAfter(PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index);
}
