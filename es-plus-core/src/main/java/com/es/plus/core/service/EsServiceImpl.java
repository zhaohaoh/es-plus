package com.es.plus.core.service;


import com.es.plus.adapter.params.*;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
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
    public EsUpdateWrapper<T> esUpdateWrapper() {
        return new EsUpdateWrapper<>(this.clazz);
    }

    @Override
    public EsChainUpdateWrapper<T> esChainUpdateWrapper() {
        return new EsChainUpdateWrapper<>(clazz, getEsPlusClientFacade());
    }


    /**
     * 创建索引
     */
    @Override
    public void createIndex() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().createIndex(this.index, clazz);
    }

    /**
     * 创建索引及映射
     */
    @Override
    public void createIndexMapping() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().createIndexMapping(this.index, clazz);
    }

    /**
     * 创建映射
     */
    @Override
    public void createMapping() {
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        getEsPlusClientFacade().putMapping(this.index, clazz);
    }

    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(EsSettings esSettings) {
        return getEsPlusClientFacade().updateSettings(this.index, esSettings);
    }

    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(Map<String, Object> esSettings) {
        return getEsPlusClientFacade().updateSettings(this.index, esSettings);
    }

    /**
     * 保存
     */
    @Override
    public boolean save(T entity) {
        return getEsPlusClientFacade().save(this.index, type, entity);
    }

    /**
     * 保存或更新
     */
    @Override
    public boolean saveOrUpdate(T entity) {
        if (!updateById(entity)) {
            return getEsPlusClientFacade().save(this.index, type, entity);
        }
        return true;
    }

    /**
     * 批量保存或更新
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return getEsPlusClientFacade().saveOrUpdateBatch(this.index, type, entityList);
    }

    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        return saveBatch(entityList);
    }


    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    @Override
    public boolean removeById(Serializable id) {
        return getEsPlusClientFacade().delete(this.index, type, id.toString());
    }

    /**
     * 删除由ids
     *
     * @param idList id列表
     * @return boolean
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        List<String> ids = idList.stream().map(Object::toString).collect(Collectors.toList());
        return getEsPlusClientFacade().deleteBatchByIds(this.index, type, ids);
    }

    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().deleteByQuery(this.index, type, esUpdateWrapper.esParamWrapper());
    }

    /**
     * 删除所有
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse removeAll() {
        return getEsPlusClientFacade().deleteByQuery(this.index, type, esUpdateWrapper().matchAll().esParamWrapper());
    }

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    @Override
    public boolean updateById(T entity) {
        return getEsPlusClientFacade().update(this.index, type, entity);
    }

    /**
     * 批处理更新
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList) {
        return updateBatch(entityList);
    }


    /**
     * 更新包装
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByQuery(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().updateByWrapper(this.index, type, esUpdateWrapper.esParamWrapper());
    }
    
    /**
     * 增量
     *
     * @param esUpdateWrapper es更新包装器
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse increment(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().increment(this.index, type, esUpdateWrapper.esParamWrapper());
    }
    
    /**
     * 删除索引
     */
    @Override
    public void deleteIndex() {
        // 查出别名下所有索引删除
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(this.index);
        if (CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        aliasIndex.getIndexs().forEach(index -> {
            getEsPlusClientFacade().deleteIndex(index);
        });
    }
    

    @Override
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush) {
        return getEsPlusClientFacade().forceMerge(maxSegments, onlyExpungeDeletes, flush, index);
    }

    @Override
    public boolean refresh() {
        return getEsPlusClientFacade().refresh(index);
    }

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    @Override
    public T searchById(Serializable id) {
        List<String> ids = Collections.singletonList(id.toString());
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(ids);
        //查询
        EsResponse<T> esResponse = getEsPlusClientFacade().search(alias, type, esQueryWrapper.esParamWrapper());
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
    public List<T> searchByIds(Collection<Serializable> idList) {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(idList.stream().map(Objects::toString).collect(Collectors.toList()));
        //查询
        return getEsPlusClientFacade().search(alias, type, esQueryWrapper.esParamWrapper()).getList();
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
        return getEsPlusClientFacade().search(alias, type, esQueryWrapper.esParamWrapper());
    }

    /**
     * 检索
     *
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search(EsWrapper<T> esQueryWrapper,int size) {
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        EsQueryParamWrapper esQueryParamWrapper = esQueryWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setSize(size);
        return getEsPlusClientFacade().search(alias, type,esQueryWrapper.esParamWrapper());
    }

    /**
     * 分页查询
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> searchPage(int page,int size, EsWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        EsQueryParamWrapper esQueryParamWrapper = esQueryWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setPage(page);
        esQueryParamWrapper.setSize(size);
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        return getEsPlusClientFacade().search(alias, type,esQueryWrapper.esParamWrapper());
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
        return getEsPlusClientFacade().count(alias, type, esQueryWrapper.esParamWrapper());
    }

    /**
     * 聚合
     *
     * @param esQueryWrapper es查询包装器
     */
    @Override
    public EsAggResponse<T> aggregations(EsWrapper<T> esQueryWrapper) {
        esQueryWrapper.esParamWrapper().setTClass(this.clazz);
        return getEsPlusClientFacade().aggregations(alias, type, esQueryWrapper.esParamWrapper());
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
        return getEsPlusClientFacade().search(alias, type, esQueryWrapper.esParamWrapper());
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
        return getEsPlusClientFacade().scroll(alias, type, esQueryWrapper.esParamWrapper(), keepTime, scollId);
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
