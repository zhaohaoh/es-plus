package com.es.plus.core.service;


import com.es.plus.core.ScrollHandler;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.pojo.EsAggsResponse;
import com.es.plus.pojo.EsSettings;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EsService<T> {

    EsQueryWrapper<T> esQueryWrapper();

    EsUpdateWrapper<T> esUpdateWrapper();

    EsChainLambdaQueryWrapper<T> esChainQueryWrapper();

    EsChainUpdateWrapper<T> esChainUpdateWrapper();

    void createIndex();

    void createIndexMapping();

    void createMapping();

    boolean updateSettings(EsSettings esSettings);

     boolean updateSettings(Map<String,Object> esSettings);

    boolean save(T entity);

    boolean saveOrUpdate(T entity);

    List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList);

    List<BulkItemResponse> saveBatch(Collection<T> entityList);

    List<BulkItemResponse> saveBatch(Collection<T> entityList, int batchSize);

    boolean removeById(Serializable id);

    boolean removeByIds(Collection<? extends Serializable> idList);

    BulkByScrollResponse remove(EsUpdateWrapper<T> esUpdateWrapper);

    BulkByScrollResponse removeAll();

    boolean updateById(T entity);

    List<BulkItemResponse> updateBatch(Collection<T> entityList);

    List<BulkItemResponse> updateBatch(Collection<T> entityList, int batchSize);

    void deleteIndex();

    BulkByScrollResponse updateByWrapper(EsUpdateWrapper<T> esUpdateWrapper);

    T getById(Serializable id);

    List<T> listByIds(Collection<Serializable> idList);

    EsResponse<T> list(EsQueryWrapper<T> esQueryWrapper);

    EsResponse<T> page(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper);


    EsResponse<T> searchAfter(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper);

    long count(EsQueryWrapper<T> esQueryWrapper);

    EsAggsResponse<T> aggregations(EsQueryWrapper<T> esQueryWrapper);

    /**
     * 性能分析
     *
     * @param esQueryWrapper es查询包装
     * @return {@link EsResponse}<{@link T}>
     */
    EsResponse<T> profile(EsQueryWrapper<T> esQueryWrapper);

    default void scroll(EsQueryWrapper<T> esQueryWrapper, int size, ScrollHandler<T> scrollHandler) {
        scroll(esQueryWrapper, size, 1, scrollHandler);
    }

    void scroll(EsQueryWrapper<T> esQueryWrapper, int size, int keepTime, ScrollHandler<T> scrollHandler);

    BulkByScrollResponse increment(EsUpdateWrapper<T> esUpdateWrapper);
}
