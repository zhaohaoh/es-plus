package com.es.plus.client;

import com.es.plus.config.GlobalConfigCache;
import com.es.plus.core.params.EsParamWrapper;
import com.es.plus.lock.ELock;
import com.es.plus.lock.EsLockFactory;
import com.es.plus.lock.EsReadWriteLock;
import com.es.plus.pojo.EsAggsResponse;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.EsSettings;
import com.es.plus.pojo.PageInfo;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/9/13 16:07
 * es客户端的门面
 */
public class EsPlusClientFacade {
    private final EsPlusClient esPlusClient;
    private final EsPlusIndexClient esPlusIndexClient;
    private final EsLockFactory esLockFactory;

    public EsPlusClientFacade(EsPlusClient esPlusClient, EsPlusIndexClient esPlusIndexClient, EsLockFactory esLockFactory) {
        this.esPlusClient = esPlusClient;
        this.esPlusIndexClient = esPlusIndexClient;
        this.esLockFactory = esLockFactory;
    }

    /**
     * 得到内部索引es客户端
     *
     * @return {@link EsPlusClient}
     */
    public EsPlusIndexClient getEsPlusIndexClient() {
        return esPlusIndexClient;
    }

    /**
     * 得到内部es客户端
     *
     * @return {@link EsPlusClient}
     */
    public EsPlusClient getEsPlusClient() {
        return esPlusClient;
    }

    /**
     * 获取锁
     */
    public ELock getLock(String key) {
        return esLockFactory.getLock(key);
    }

    /**
     * 获取读写锁
     */
    public EsReadWriteLock getReadWrtieLock(String key) {
        return esLockFactory.getReadWrtieLock(key);
    }


    /**
     * 初始化ping 如果设置为false启动时不创建索引
     */
    @PostConstruct
    public void init() {
        boolean ping = esPlusIndexClient.ping();
        if (!ping) {
            GlobalConfigCache.GLOBAL_CONFIG.setStartInit(false);
        }
    }

    /**
     * 创建索引
     *
     * @param index  指数
     * @param tClass t类
     */
    public void createIndex(String index, Class<?> tClass) {
        esPlusIndexClient.createIndex(index, tClass);
    }


    /**
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    public void putMapping(String index, Class<?> tClass) {
        esPlusIndexClient.putMapping(index, tClass);
    }


    /**
     * 映射
     *
     * @param index             索引
     * @param mappingProperties 映射属性
     */
    public void putMapping(String index, Map<String, Object> mappingProperties) {
        esPlusIndexClient.putMapping(index, mappingProperties);
    }


    /**
     * 创建索引映射
     *
     * @param index  指数
     * @param tClass t类
     */
    public void createIndexMapping(String index, Class<?> tClass) {
        esPlusIndexClient.createIndexMapping(index, tClass);
    }


    /**
     * 创建索引没有别名
     *
     * @param index  指数
     * @param tClass t类
     */
    public void createIndexWithoutAlias(String index, Class<?> tClass) {
        esPlusIndexClient.createIndexWithoutAlias(index, tClass);
    }


    /**
     * 删除索引
     */
    public void deleteIndex(String index) {
        esPlusIndexClient.deleteIndex(index);
    }


    /**
     * 得到索引
     *
     * @param indexName 索引名称
     * @return {@link GetIndexResponse}
     */
    public GetIndexResponse getIndex(String indexName) {
        return esPlusIndexClient.getIndex(indexName);
    }


    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    public GetAliasesResponse getAliasIndex(String alias) {
        return esPlusIndexClient.getAliasIndex(alias);
    }


    /**
     * 查询index是否存在
     */
    public boolean indexExists(String index) {
        return esPlusIndexClient.indexExists(index);
    }


    /**
     * 更新别名
     *
     * @param oldIndexName 旧索引名称
     * @param reindexName  重建索引名称
     * @param alias        别名
     * @return boolean
     */
    public boolean replaceAlias(String oldIndexName, String reindexName, String alias) {
        return esPlusIndexClient.replaceAlias(oldIndexName, reindexName, alias);
    }


    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    public boolean reindex(String oldIndexName, String reindexName, QueryBuilder queryBuilder) {
        return esPlusIndexClient.reindex(oldIndexName, reindexName, queryBuilder);
    }


    /**
     * 更新设置
     *
     * @param index      指数
     * @param esSettings es设置
     * @return boolean
     */
    public boolean updateSettings(String index, EsSettings esSettings) {
        return esPlusIndexClient.updateSettings(index, esSettings);
    }

    /**
     * 更新设置
     *
     * @param index      索引
     * @param esSettings es设置
     * @return boolean
     */
    public boolean updateSettings(String index, Map<String,Object> esSettings) {
        return esPlusIndexClient.updateSettings(index, esSettings);
    }


    /**
     * ----------------------------------------------------------------------------------------------------------
     * 数据操作
     */
    public List<BulkItemResponse> saveOrUpdateBatch(String index, Collection<?> esDataList) {
        return esPlusClient.saveOrUpdateBatch(index, esDataList);
    }

    /**
     * 保存
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    public List<BulkItemResponse> saveBatch(String index, Collection<?> esDataList) {
        return esPlusClient.saveBatch(index, esDataList);
    }

    /**
     * 保存
     */
    public boolean save(String index, Object esData) {
        return esPlusClient.save(index, esData);
    }

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    public boolean update(String index, Object esData) {
        return esPlusClient.update(index, esData);
    }

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    public List<BulkItemResponse> updateBatch(String index, Collection<?> esDataList) {
        return esPlusClient.updateBatch(index, esDataList);
    }

    /**
     * 更新包装
     */
    public <T> BulkByScrollResponse updateByWrapper(String index, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.updateByWrapper(index, esUpdateWrapper);
    }


    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse increment(String index, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.increment(index, esUpdateWrapper);
    }

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    public boolean delete(String index, String id) {
        return esPlusClient.delete(index, id);
    }


    /**
     * 删除根据查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse deleteByQuery(String index, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.deleteByQuery(index, esUpdateWrapper);
    }


    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 数据列表
     * @return boolean
     */
    public boolean deleteBatch(String index, Collection<String> esDataList) {
        return esPlusClient.deleteBatch(index, esDataList);
    }


    /**
     * 统计
     *
     * @param esParamWrapper es param包装
     * @param index          索引
     * @return long
     */
    public <T> long count(EsParamWrapper<T> esParamWrapper, String index) {
        return esPlusClient.count(esParamWrapper, index);
    }


    /**
     * 搜索根据包装器
     *
     * @param esParamWrapper es param包装
     * @param tClass         t类
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    public <T> EsResponse<T> searchByWrapper(EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index) {
        return esPlusClient.searchByWrapper(esParamWrapper, tClass, index);
    }


    /**
     * 搜索分页根据包装器
     *
     * @param pageInfo       页面信息
     * @param esParamWrapper es param包装
     * @param tClass         t类
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    public <T> EsResponse<T> searchPageByWrapper(PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index) {
        return esPlusClient.searchPageByWrapper(pageInfo, esParamWrapper, tClass, index);
    }

    /**
     * 搜索翻页 也可以向前搜索，只要更改排序即可
     * 此方法可以代替实现深度分页
     *
     * @param pageInfo       页面信息
     * @param esParamWrapper es param包装
     * @param tClass         t类
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    public <T> EsResponse<T> searchAfter(PageInfo<T> pageInfo, EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index) {
        return esPlusClient.searchAfter(pageInfo, esParamWrapper, tClass, index);
    }

    /**
     * 滚动根据包装器
     *
     * @param esParamWrapper es param包装
     * @param tClass         t类
     * @param index          指数
     * @param size           大小
     * @param keepTime       保持时间
     * @param scollId  滚动处理Id
     */
    public <T>  EsResponse<T> scrollByWrapper(EsParamWrapper<T> esParamWrapper, Class<T> tClass, String index, int size, Duration keepTime, String scollId) {
      return   esPlusClient.scrollByWrapper(esParamWrapper, tClass, index, size, keepTime, scollId);
    }


    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es param包装
     * @param tClass         t类
     * @return {@link EsAggsResponse}<{@link T}>
     */
    public <T> EsAggsResponse<T> aggregations(String index, EsParamWrapper<T> esParamWrapper, Class<T> tClass) {
        return esPlusClient.aggregations(index, esParamWrapper,tClass);
    }

    /**
     * 创建别名
     *
     * @param currentIndex 目前指数
     * @param alias        别名
     */
    public void createAlias(String currentIndex, String alias) {
        esPlusIndexClient.createAlias(currentIndex,alias);
    }

    /**
     * 删除别名
     *
     * @param index 指数
     * @param alias 别名
     */
    public void removeAlias(String index, String alias) {
        esPlusIndexClient.removeAlias(index,alias);
    }
}
