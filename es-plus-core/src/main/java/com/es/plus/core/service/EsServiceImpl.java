package com.es.plus.core.service;


import com.es.plus.config.GlobalConfigCache;
import com.es.plus.core.ScrollHandler;
import com.es.plus.core.wrapper.chain.EsChainUpdateWrapper;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.pojo.EsAggregationsResponse;
import com.es.plus.pojo.EsSettings;
import com.es.plus.util.CollectionUtil;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.es.plus.constant.EsConstant.SO_SUFFIX;

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
    public EsChainQueryWrapper<T> esChainQueryWrapper() {
        return new EsChainQueryWrapper<>(this);
    }

    @Override
    public EsUpdateWrapper<T> esUpdateWrapper() {
        return new EsUpdateWrapper<>(this.clazz);
    }

    @Override
    public EsChainUpdateWrapper<T> esChainUpdateWrapper() {
        return new EsChainUpdateWrapper<>(this);
    }

    /**
     * 创建索引
     */
    @Override
    public void createIndex() {
        GetAliasesResponse aliasIndex = esPlusClientFacade.getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getAliases())) {
            return;
        }
        esPlusClientFacade.createIndex(this.index + SO_SUFFIX, clazz);
    }

    /**
     * 创建索引及映射
     */
    @Override
    public void createIndexMapping() {
        GetAliasesResponse aliasIndex = esPlusClientFacade.getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getAliases())) {
            return;
        }
        esPlusClientFacade.createIndexMapping(this.index + SO_SUFFIX, clazz);
    }

    /**
     * 创建映射
     */
    @Override
    public void createMapping() {
        GetAliasesResponse aliasIndex = esPlusClientFacade.getAliasIndex(alias);
        if (!CollectionUtils.isEmpty(aliasIndex.getAliases())) {
            return;
        }
        esPlusClientFacade.putMapping(alias, clazz);
    }

    /**
     * 更新设置
     */
    @Override
    public boolean updateSettings(EsSettings esSettings) {
        return esPlusClientFacade.updateSettings(alias, esSettings);
    }

    /**
     * 保存
     */
    @Override
    public boolean save(T entity) {
        return esPlusClientFacade.save(alias, entity);
    }

    /**
     * 保存或更新
     */
    @Override
    public boolean saveOrUpdate(T entity) {
        if (!updateById(entity)) {
            return esPlusClientFacade.save(alias, entity);
        }
        return true;
    }

    /**
     * 批量保存或更新
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveOrUpdateBatch(index, entityList);
    }

    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
    }

    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @param batchSize  批量大小
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList, int batchSize) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityList)) {
            return failBulkItemResponses;
        }
        if (entityList.size() > batchSize) {
            List<Collection<T>> collections = CollectionUtil.splitList(entityList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClientFacade.saveBatch(alias, list);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClientFacade.saveBatch(alias, entityList);
            failBulkItemResponses.addAll(bulkItemResponses);
        }
        return failBulkItemResponses;
    }


    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    @Override
    public boolean removeById(Serializable id) {
        return esPlusClientFacade.delete(alias, id.toString());
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
        return esPlusClientFacade.deleteBatch(alias, ids);
    }

    /**
     * 删除
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove(EsUpdateWrapper<T> esUpdateWrapper) {
        return esPlusClientFacade.deleteByQuery(alias, esUpdateWrapper);
    }

    /**
     * 删除所有
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse removeAll() {
        return esPlusClientFacade.deleteByQuery(alias, esUpdateWrapper().matchAll());
    }

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    @Override
    public boolean updateById(T entity) {
        return esPlusClientFacade.update(alias, entity);
    }

    /**
     * 批处理更新
     *
     * @param entityList 实体列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList) {
        return updateBatch(entityList, GlobalConfigCache.GLOBAL_CONFIG.getBatchSize());
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     * @param batchSize  更新批次数量
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList, int batchSize) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityList)) {
            return failBulkItemResponses;
        }
        if (entityList.size() > batchSize) {
            List<Collection<T>> collections = CollectionUtil.splitList(entityList, batchSize);
            collections.forEach(list -> {
                        failBulkItemResponses.addAll(doUpdateBatch(list));
                    }
            );
        } else {
            failBulkItemResponses.addAll(doUpdateBatch(entityList));
        }
        return failBulkItemResponses;
    }

    private List<BulkItemResponse> doUpdateBatch(Collection<T> list) {
        return esPlusClientFacade.updateBatch(alias, list);
    }

    /**
     * 更新包装
     *
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByWrapper(EsUpdateWrapper<T> esUpdateWrapper) {
        return esPlusClientFacade.updateByWrapper(alias, esUpdateWrapper);
    }

    /**
     * 删除索引
     */
    @Override
    public void deleteIndex() {
        // 查出别名下所有索引删除
        GetAliasesResponse aliasIndex = esPlusClientFacade.getAliasIndex(alias);
        if (CollectionUtils.isEmpty(aliasIndex.getAliases())) {
            return;
        }
        aliasIndex.getAliases().forEach((index, aliasMetadata) -> {
            esPlusClientFacade.deleteIndex(index);
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
        EsResponse<T> esResponse = esPlusClientFacade.searchByWrapper(esQueryWrapper, clazz, alias);
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
        return esPlusClientFacade.searchByWrapper(esQueryWrapper, clazz, alias).getList();
    }

    //最多返回3万条
    @Override
    public EsResponse<T> list(EsQueryWrapper<T> esQueryWrapper) {
        //默认查询所有
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esPlusClientFacade.searchByWrapper(esQueryWrapper, clazz, alias);
    }

    @Override
    public EsResponse<T> page(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esPlusClientFacade.searchPageByWrapper(pageInfo, esQueryWrapper, clazz, alias);
    }

    @Override
    public long count(EsQueryWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esPlusClientFacade.count(esQueryWrapper, alias);
    }

    @Override
    public EsAggregationsResponse<T> aggregations(EsQueryWrapper<T> esQueryWrapper) {
        return esPlusClientFacade.aggregations(alias, esQueryWrapper);
    }


    @Override
    public void scroll(EsQueryWrapper<T> esQueryWrapper, int size, int keepTime, ScrollHandler<T> scrollHandler) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }

        esPlusClientFacade.scrollByWrapper(esQueryWrapper, clazz, alias, size, keepTime, scrollHandler);
    }

    @Override
    public BulkByScrollResponse increment(EsUpdateWrapper<T> esUpdateWrapper) {
        return esPlusClientFacade.increment(alias, esUpdateWrapper);
    }

    private EsQueryWrapper<T> matchAll() {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.must().query(QueryBuilders.matchAllQuery());
        return esQueryWrapper;
    }


}
