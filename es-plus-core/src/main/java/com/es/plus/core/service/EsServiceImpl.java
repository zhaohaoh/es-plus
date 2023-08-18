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
        getEsPlusClientFacade().putMapping(alias, clazz);
    }

    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(EsSettings esSettings) {
        return getEsPlusClientFacade().updateSettings(alias, esSettings);
    }

    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(Map<String, Object> esSettings) {
        return getEsPlusClientFacade().updateSettings(alias, esSettings);
    }

    /**
     * 保存
     */
    @Override
    public boolean save(T entity) {
        return getEsPlusClientFacade().save(alias,type, entity);
    }

    /**
     * 保存或更新
     */
    @Override
    public boolean saveOrUpdate(T entity) {
        if (!updateById(entity)) {
            return getEsPlusClientFacade().save(alias,type, entity);
        }
        return true;
    }

    /**
     * 批量保存或更新
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return getEsPlusClientFacade().saveOrUpdateBatch(alias, type, entityList);
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
        return getEsPlusClientFacade().delete(alias, type, id.toString());
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
        return getEsPlusClientFacade().deleteBatchByIds(alias, type, ids);
    }

    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().deleteByQuery(alias,type, esUpdateWrapper.esParamWrapper());
    }

    /**
     * 删除所有
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse removeAll() {
        return getEsPlusClientFacade().deleteByQuery(alias,type, esUpdateWrapper().matchAll().esParamWrapper());
    }

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    @Override
    public boolean updateById(T entity) {
        return getEsPlusClientFacade().update(alias, type, entity);
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
        return getEsPlusClientFacade().updateByWrapper(alias,type, esUpdateWrapper.esParamWrapper());
    }

    /**
     * 删除索引
     */
    @Override
    public void deleteIndex() {
        // 查出别名下所有索引删除
        EsAliasResponse aliasIndex = getEsPlusClientFacade().getAliasIndex(alias);
        if (CollectionUtils.isEmpty(aliasIndex.getIndexs())) {
            return;
        }
        aliasIndex.getIndexs().forEach(index -> {
            getEsPlusClientFacade().deleteIndex(index);
        });
    }

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    @Override
    public T getById(Serializable id) {
        List<String> ids = Collections.singletonList(id.toString());
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(ids);
        //查询
        EsResponse<T> esResponse = getEsPlusClientFacade().searchByWrapper(alias,type,esQueryWrapper.esParamWrapper(), clazz);
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
    public List<T> listByIds(Collection<Serializable> idList) {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(idList.stream().map(Objects::toString).collect(Collectors.toList()));
        //查询
        return getEsPlusClientFacade().searchByWrapper(alias,type,esQueryWrapper.esParamWrapper(), clazz).getList();
    }

    /**
     * 列表 默认有限的size
     *
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> list(EsWrapper<T> esQueryWrapper) {
        //默认查询所有
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return getEsPlusClientFacade().searchByWrapper(alias,type,esQueryWrapper.esParamWrapper(), clazz);
    }

    /**
     * 分页查询
     *
     * @param pageInfo       页面信息
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> page(PageInfo<T> pageInfo, EsWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return getEsPlusClientFacade().searchPageByWrapper(alias,type,pageInfo, esQueryWrapper.esParamWrapper(), clazz);
    }

    /**
     * searchAfter
     *
     * @param pageInfo       页面信息
     * @param esQueryWrapper es查询包装器
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> searchAfter(PageInfo<T> pageInfo, EsWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return getEsPlusClientFacade().searchAfter(alias,type,pageInfo, esQueryWrapper.esParamWrapper(), clazz);
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
        return getEsPlusClientFacade().count(alias,type,esQueryWrapper.esParamWrapper());
    }

    /**
     * 聚合
     *
     * @param esQueryWrapper es查询包装器
     */
    @Override
    public EsAggResponse<T> aggregations(EsWrapper<T> esQueryWrapper) {
        return getEsPlusClientFacade().aggregations(alias, type,esQueryWrapper.esParamWrapper(), clazz);
    }

    /**
     * 性能分析
     *
     * @param esQueryWrapper es查询包装
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> profile(EsWrapper<T> esQueryWrapper) {
        esQueryWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return getEsPlusClientFacade().searchByWrapper(alias, type,esQueryWrapper.esParamWrapper(), clazz);
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

        return getEsPlusClientFacade().scrollByWrapper(alias, type,esQueryWrapper.esParamWrapper(), clazz, size, keepTime, scollId);
    }

    /**
     * 增量
     *
     * @param esUpdateWrapper es更新包装器
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse increment(EsWrapper<T> esUpdateWrapper) {
        return getEsPlusClientFacade().increment(alias,type, esUpdateWrapper.esParamWrapper());
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
