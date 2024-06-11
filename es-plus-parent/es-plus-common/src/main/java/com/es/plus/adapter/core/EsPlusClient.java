package com.es.plus.adapter.core;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public interface EsPlusClient {

    RestHighLevelClient getRestHighLevelClient();

   
    /**
     * 保存或更新批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveOrUpdateBatch(String index, String type, Collection<?> esDataList);

    /**
     * 保存批
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> saveBatch(String index, String type, Collection<?> esDataList);

    /**
     * 保存
     */
    boolean save(String index, String type, Object esData);

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    boolean update(String index, String type, Object esData);

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    List<BulkItemResponse> updateBatch(String index, String type, Collection<?> esDataList);

    /**
     * 更新包装
     */
    <T> BulkByScrollResponse updateByWrapper(String index, String type, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    <T> BulkByScrollResponse increment(String index, String type, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    boolean delete(String index, String type, String id);

    /**
     * 删除,查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    <T> BulkByScrollResponse deleteByQuery(String index, String type, EsParamWrapper<T> esUpdateWrapper);

    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 西文数据列表
     * @return boolean
     */
    boolean deleteBatch(String index, String type, Collection<String> esDataList);

    /**
     * 统计数
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @return long
     */
    <T> long count(String index, String type, EsParamWrapper<T> esParamWrapper);

    /**
     * 搜索包装
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @return {@link EsResponse}<{@link T}>
     */
    <T> EsResponse<T> search(String index, String type, EsParamWrapper<T> esParamWrapper);

    /**
     * 滚动查询包装
     *
     * @param esParamWrapper es查询包装
     * @param index          指数
     * @param keepTime       保持时间
     * @return
     */
    <T> EsResponse<T> scroll(String index, String type, EsParamWrapper<T> esParamWrapper, Duration keepTime, String scrollId);

    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es查询包装
     */
    <T> EsAggResponse<T> aggregations(String index, String type, EsParamWrapper<T> esParamWrapper);


    /**
     * 执行dsl
     *
     * @param dsl       dsl
     * @param index 索引名字
     * @return {@link String}
     */
    String executeDSL(String dsl, String index);
}
