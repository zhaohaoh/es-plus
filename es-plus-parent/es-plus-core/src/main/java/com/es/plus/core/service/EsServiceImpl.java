package com.es.plus.core.service;


import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaUpdateWrapper;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
@SuppressWarnings({"unchecked"})
public class EsServiceImpl<T> extends AbstractEsService<T> implements EsService<T> {
    
    @Override
    public EsQueryWrapper<T> esQueryWrapper() {
        return new EsQueryWrapper<>(this.clazz);
    }
    
    @Override
    public EsChainLambdaQueryWrapper<T> esChainQueryWrapper() {
        return new EsChainLambdaQueryWrapper<>(clazz, getEsPlusClientFacade());
    }
    
    @Override
    public EsChainUpdateWrapper<T> esUpdateWrapper() {
        return new EsChainUpdateWrapper<>(this.clazz,getEsPlusClientFacade());
    }
    
    @Override
    public EsChainLambdaUpdateWrapper<T> esChainUpdateWrapper() {
        return new EsChainLambdaUpdateWrapper<>(clazz, getEsPlusClientFacade());
    }
    
    
    /**
     * 创建索引
     */
    @Override
    public void createIndex() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(getIndex());
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().createIndex(getIndex(), clazz);
    }
    
    /**
     * 创建索引及映射
     */
    @Override
    public void createIndexMapping() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(getIndex());
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().createIndexMapping(getIndex(), clazz);
    }
    
    /**
     * 创建映射
     */
    @Override
    public void createMapping() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(getIndex());
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().putMapping(getIndex(), clazz);
    }
    
    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(EsSettings esSettings,String... indexs) {
        return getEsPlusClientFacade().updateSettings(esSettings, indexs);
    }
    
    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(Map<String, Object> esSettings,String... indexs) {
        return getEsPlusClientFacade().updateSettings(esSettings, indexs);
    }
    
    /**
     * 保存
     */
    @Override
    public boolean save(T entity,String... indexs) {
        return getEsPlusClientFacade().save(getType(), entity,indexs);
    }
    
    /**
     * 保存或更新
     */
    @Override
    public boolean saveOrUpdate(T entity,String... indexs) {
        return getEsPlusClientFacade().saveOrUpdate(getType(), entity, indexs);
    }
    
    /**
     * 批量保存或更新
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList,String... indexs) {
        return getEsPlusClientFacade().saveOrUpdateBatch(getType(), entityList, indexs);
    }
    
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList,String... indexs) {
        return getEsPlusClientFacade().saveBatch(getType(), entityList, indexs);
    }
    
    /**
     * 批量保存或更新
     */
    @Override
    public void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList,String... indexs) {
        getEsPlusClientFacade().saveOrUpdateBatchAsyncProcessor(getType(), entityList, indexs);
    }
    
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void saveBatchAsyncProcessor(Collection<T> entityList,String... indexs) {
        getEsPlusClientFacade().saveBatchAsyncProcessor(getType(), entityList, indexs);
    }
    
    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void updateBatchAsyncProcessor(Collection<T> entityList,String... indexs) {
        getEsPlusClientFacade().updateBatchAsyncProcessor(getType(), entityList, indexs);
    }
    
    
    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    @Override
    public boolean removeById(Serializable id,String... indexs) {
        return getEsPlusClientFacade().delete(getType(), id.toString(), indexs);
    }
    
    /**
     * 删除由ids
     *
     * @param idList id列表
     * @return boolean
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList,String... indexs) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        List<String> ids = idList.stream().map(Object::toString).collect(Collectors.toList());
        return getEsPlusClientFacade().deleteBatchByIds(getType(), ids, indexs);
    }
    

    
    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    @Override
    public boolean updateById(T entity,String... indexs) {
        return getEsPlusClientFacade().update(getType(), entity,indexs);
    }
    
    /**
     * 批处理更新
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList,String... indexs) {
        return getEsPlusClientFacade().updateBatch(getType(), entityList, getIndex());
    }
    
    
    /**
     * 更新包装
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByQuery(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().updateByWrapper(getType(), esUpdateWrapper.esParamWrapper(), ArrayUtils.isNotEmpty(esUpdateWrapper.getIndexs()) ? esUpdateWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 增量
     *
     * @param esUpdateWrapper es更新包装器
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse increment(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().increment(getType(), esUpdateWrapper.esParamWrapper(), ArrayUtils.isNotEmpty(esUpdateWrapper.getIndexs()) ? esUpdateWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 删除索引
     */
    @Override
    public void deleteIndex(String... indexs) {
        for (String index : indexs) {
             getEsPlusClientFacade().deleteIndex(index);
        }
    }
    
    
    @Override
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush,String... indexs) {
        return getEsPlusClientFacade().forceMerge(maxSegments, onlyExpungeDeletes, flush, indexs);
    }
    
    @Override
    public boolean refresh(String... indexs) {
        return getEsPlusClientFacade().refresh(indexs);
    }
    
    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().deleteByQuery(getType(), esUpdateWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esUpdateWrapper.getIndexs()) ? esUpdateWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    @Override
    public T searchById(Serializable id,String... indexs) {
        List<String> ids = Collections.singletonList(id.toString());
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(ids);
        //查询
        EsResponse<T> esResponse = getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),indexs);
        List<T> list = esResponse.getList();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表
     */
    @Override
    public List<T> searchByIds(Collection<? extends Serializable> idList,String... indexs) {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(idList.stream().map(Objects::toString).collect(Collectors.toList()));
        //查询
        return getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),indexs).getList();
    }
    
    /**
     * 检索 默认有限的size
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search(EsWrapper<T> esQueryWrapper) {
        //默认查询所有
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        return getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 检索
     *
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search(EsWrapper<T> esQueryWrapper, int size) {
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        EsQueryParamWrapper esQueryParamWrapper = esQueryWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setSize(size);
        return getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 分页查询
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> searchPage(int page, int size, EsWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        EsQueryParamWrapper esQueryParamWrapper = esQueryWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setPage(page);
        esQueryParamWrapper.setSize(size);
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        return getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 统计
     *
     * @param esQueryWrapper es查询包装器
     * @return long
     */
    @Override
    public long count(EsWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return getEsPlusClientFacade().count(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 聚合
     *
     * @param esQueryWrapper es查询包装器
     */
    @Override
    public EsAggResponse<T> aggregations(EsWrapper<T> esQueryWrapper) {
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        return getEsPlusClientFacade().aggregations(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 性能分析
     *
     * @param esQueryWrapper es查询包装
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> profile(EsWrapper<T> esQueryWrapper) {
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        esQueryWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return getEsPlusClientFacade().search(getType(), esQueryWrapper.esParamWrapper(),
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    /**
     * 滚动查询
     *
     * @param esQueryWrapper es查询包装器
     * @param size           大小
     * @param keepTime       保持时间
     * @param scollId        滚动处理Id
     */
    @Override
    public EsResponse<T> scroll(EsWrapper<T> esQueryWrapper, int size, Duration keepTime, String scollId) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        esQueryWrapper.esParamWrapper().getEsQueryParamWrapper().setSize(size);
        return getEsPlusClientFacade().scroll(getType(), esQueryWrapper.esParamWrapper(), keepTime, scollId,
                ArrayUtils.isNotEmpty(esQueryWrapper.getIndexs()) ? esQueryWrapper.getIndexs() : getAlias());
    }
    
    
    /**
     * 匹配所有
     *
     * @return {@link EsWrapper}<{@link T}>
     */
    private EsQueryWrapper<T> matchAll() {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.must().query(QueryBuilders.matchAllQuery());
        return esQueryWrapper;
    }
}
