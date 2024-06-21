package com.es.plus.adapter;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.core.EsPlusIndexClient;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.lock.EsLockFactory;
import com.es.plus.adapter.lock.EsReadWriteLock;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.util.CollectionUtil;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/9/13 16:07
 * es客户端的门面
 */
public class EsPlusClientFacade   {
    private  EsPlusClient esPlusClient;
    private  EsPlusIndexClient esPlusIndexClient;
    private  EsLockFactory esLockFactory;

    public EsPlusClientFacade() {
    }

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
     * @param index 索引
     */
    public boolean createIndex(String index) {
       return esPlusIndexClient.createIndex(index);
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
    public boolean putMapping(String index, Class<?> tClass) {
      return   esPlusIndexClient.putMapping(index, tClass);
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
    public EsIndexResponse getIndex(String indexName) {
        return esPlusIndexClient.getIndex(indexName);
    }


    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    public EsAliasResponse getAliasIndex(String alias) {
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
    public boolean swapAlias(String oldIndexName, String reindexName, String alias) {
        return esPlusIndexClient.swapAlias(oldIndexName, reindexName, alias);
    }
    
    /**
     * 更新别名
     *
     * @param alias        别名
     * @return boolean
     */
    public boolean replaceAlias(String indexName, String oldAlias, String alias) {
        return esPlusIndexClient.replaceAlias(indexName, oldAlias, alias);
    }
    
    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    public boolean reindex(String oldIndexName, String reindexName) {
        return esPlusIndexClient.reindex(oldIndexName, reindexName);
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
    
    public boolean reindex(String oldIndexName, String reindexName, Map<String,Object> changeMapping) {
        return esPlusIndexClient.reindex(oldIndexName, reindexName, changeMapping);
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
    public boolean updateSettings(String index, Map<String, Object> esSettings) {
        return esPlusIndexClient.updateSettings(index, esSettings);
    }

    public boolean ping() {
        return esPlusIndexClient.ping();
    }

    /**
     * ----------------------------------------------------------------------------------------------------------
     * 数据操作
     */
    public List<BulkItemResponse> saveOrUpdateBatch(String index, String type, Collection<?> esDataList) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (esDataList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(esDataList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.saveOrUpdateBatch(index, type, list);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.saveOrUpdateBatch(index, type, esDataList);
            failBulkItemResponses.addAll(bulkItemResponses);
        }
        return failBulkItemResponses;
    }

    /**
     * 保存
     *
     * @param index      索引
     * @param esDataList 西文数据列表
     * @return {@link List}<{@link BulkItemResponse}>
     */
    public List<BulkItemResponse> saveBatch(String index, String type, Collection<?> esDataList) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (esDataList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(esDataList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.saveBatch(index, type, list);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.saveBatch(index, type, esDataList);
            failBulkItemResponses.addAll(bulkItemResponses);
        }
        return failBulkItemResponses;
    }

    /**
     * 保存
     */
    public boolean save(String index, String type, Object esData) {
        return esPlusClient.save(index, type, esData);
    }

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    public boolean update(String index, String type, Object esData) {
        return esPlusClient.update(index, type, esData);
    }

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    public List<BulkItemResponse> updateBatch(String index, String type, Collection<?> entityList) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (entityList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(entityList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.updateBatch(index, type, list);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.updateBatch(index, type, entityList);
            failBulkItemResponses.addAll(bulkItemResponses);
        }

        return failBulkItemResponses;
    }

    /**
     * 更新包装
     */
    public <T> BulkByScrollResponse updateByWrapper(String index, String type, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.updateByWrapper(index, type, esUpdateWrapper);
    }


    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse increment(String index, String type, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.increment(index, type, esUpdateWrapper);
    }

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    public boolean delete(String index, String type, String id) {
        return esPlusClient.delete(index, type, id);
    }


    /**
     * 删除根据查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse deleteByQuery(String index, String type, EsParamWrapper<T> esUpdateWrapper) {
        return esPlusClient.deleteByQuery(index, type, esUpdateWrapper);
    }


    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 数据列表
     * @return boolean
     */
    public boolean deleteBatchByIds(String index, String type, Collection<String> esDataList) {
        return esPlusClient.deleteBatch(index, type, esDataList);
    }


    /**
     * 统计
     *
     * @param esParamWrapper es param包装
     * @param index          索引
     * @return long
     */
    public <T> long count(String index, String type, EsParamWrapper<T> esParamWrapper) {
        return esPlusClient.count(index, type, esParamWrapper);
    }


    /**
     * 搜索根据包装器
     *
     * @param esParamWrapper es param包装
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    public <T> EsResponse<T> search(String index, String type, EsParamWrapper<T> esParamWrapper) {
        return esPlusClient.search(index, type, esParamWrapper);
    }


    /**
     * 滚动根据包装器
     *
     * @param esParamWrapper es param包装
     * @param index          指数
     * @param keepTime       保持时间
     * @param scollId        滚动处理Id
     */
    public <T> EsResponse<T> scroll(String index, String type, EsParamWrapper<T> esParamWrapper, Duration keepTime, String scollId) {
        return esPlusClient.scroll(index, type, esParamWrapper, keepTime, scollId);
    }


    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es param包装
     */
    public <T> EsAggResponse<T> aggregations(String index, String type, EsParamWrapper<T> esParamWrapper) {
        return esPlusClient.aggregations(index, type, esParamWrapper);
    }

    /**
     * 创建别名
     *
     * @param currentIndex 目前指数
     * @param alias        别名
     */
    public void createAlias(String currentIndex, String alias) {
        esPlusIndexClient.createAlias(currentIndex, alias);
    }
    

    /**
     * 删除别名
     *
     * @param index 指数
     * @param alias 别名
     */
    public void removeAlias(String index, String alias) {
        esPlusIndexClient.removeAlias(index, alias);
    }

    /**
     * forceMerge
     */
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... index) {
        return esPlusIndexClient.forceMerge(maxSegments, onlyExpungeDeletes, flush, index);
    }

    public boolean refresh(String... index) {
        return esPlusIndexClient.refresh(index);
    }

    public String executeDSL(String dsl, String index) {
        return esPlusClient.executeDSL(dsl,index);
    }
    
    
    public String getAliasByIndex(String index) {
     return    esPlusIndexClient.getAliasByIndex(index);
    }
}
