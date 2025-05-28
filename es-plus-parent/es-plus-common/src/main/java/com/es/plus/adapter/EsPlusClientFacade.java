package com.es.plus.adapter;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.core.EsPlusIndexClient;
import com.es.plus.adapter.interceptor.EsInterceptor;
import com.es.plus.adapter.interceptor.EsPlusClientProxy;
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
    private  EsPlusClientProxy esPlusClientProxy;
    private String host;

    public EsPlusClientFacade() {
    }
    
    public String getHost() {
        return host;
    }
    
    public EsPlusClientFacade(EsPlusClientProxy esPlusClientProxy, EsPlusIndexClient esPlusIndexClient, EsLockFactory esLockFactory,String host) {
        this.esPlusClientProxy=esPlusClientProxy;
        this.esPlusClient = (EsPlusClient)esPlusClientProxy.getProxy();
        this.esPlusIndexClient = esPlusIndexClient;
        this.esLockFactory = esLockFactory;
        this.host = host;
    }
    
    public EsLockFactory getEsLockFactory() {
        return esLockFactory;
    }
    
    public void addInterceptor(EsInterceptor esInterceptor){
        esPlusClientProxy.addInterceptor(esInterceptor);
    }
    public EsInterceptor getEsInterceptor(Class<?> clazz){
        List<EsInterceptor> esInterceptors = esPlusClientProxy.getEsInterceptors();
        for (EsInterceptor esInterceptor : esInterceptors) {
            if (clazz.equals(esInterceptor.getClass())){
                return esInterceptor;
            }
        }
        return null;
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
     * @param index 索引
     */
    public boolean createIndex(String index,String alias,EsSettings esSettings,Map<String,Object> mappings) {
        return esPlusIndexClient.createIndex(index,alias,esSettings,mappings);
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
     POST /_aliases
     * {
     *     "actions": [
     *         {
     *             "add": {
     *                 "index": "my_index3",
     *                 "alias": "my_alias"
     *             }
     *         },
     *         {
     *             "remove": {
     *                 "index": "my_index1",
     *                 "alias": "my_alias"
     *             }
     *         }
     *     ]
     * }
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
    public boolean updateSettings( EsSettings esSettings,String... index) {
        return esPlusIndexClient.updateSettings( esSettings,index);
    }

    /**
     * 更新设置
     *
     * @param index      索引
     * @param esSettings es设置
     * @return boolean
     */
    public boolean updateSettings( Map<String, Object> esSettings,String... index) {
        return esPlusIndexClient.updateSettings( esSettings,index);
    }

    public boolean ping() {
        return esPlusIndexClient.ping();
    }

    /**
     * ----------------------------------------------------------------------------------------------------------
     * 数据操作
     */
    
    
    /**
     * 异步定时批量保存接口
     */
    public void saveOrUpdateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs){
       esPlusClient.saveOrUpdateBatchAsyncProcessor(type, esDataList,indexs);
    };
    
    /**
     * 异步定时批量保存接口
     */
    public void saveBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs){
        esPlusClient.saveBatchAsyncProcessor(type, esDataList,indexs);
    };
    
    /**
     * 异步定时批量保存接口
     */
    public void updateBatchAsyncProcessor(String type, Collection<?> esDataList, String... indexs){
        esPlusClient.updateBatchAsyncProcessor(type, esDataList,indexs);
    };
    
    public List<BulkItemResponse> saveOrUpdateBatch(String type, Collection<?> esDataList,String... index) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (esDataList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(esDataList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.saveOrUpdateBatch( type, list,index);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.saveOrUpdateBatch( type, esDataList,index);
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
    public List<BulkItemResponse> saveBatch(String type, Collection<?> esDataList,String... index) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(esDataList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (esDataList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(esDataList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.saveBatch( type, list,index);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.saveBatch( type, esDataList,index);
            failBulkItemResponses.addAll(bulkItemResponses);
        }
        return failBulkItemResponses;
    }

    /**
     * 保存
     */
    public boolean save(String type, Object esData,String... index) {
        return esPlusClient.save(type, esData, index);
    }

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    public boolean update( String type, Object esData,String... index) {
        return esPlusClient.update(type, esData,index);
    }

    /**
     * 批处理更新 返回失败数据
     *
     * @param index 索引
     * @return {@link List}<{@link BulkItemResponse}>
     */
    public List<BulkItemResponse> updateBatch(String type, Collection<?> entityList,String... index) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityList)) {
            return failBulkItemResponses;
        }
        int batchSize = GlobalConfigCache.GLOBAL_CONFIG.getBatchSize();
        if (entityList.size() > batchSize) {
            List<? extends Collection<?>> collections = CollectionUtil.splitList(entityList, batchSize);
            collections.forEach(list -> {
                        List<BulkItemResponse> bulkItemResponses = esPlusClient.updateBatch( type, list,index);
                        failBulkItemResponses.addAll(bulkItemResponses);
                    }
            );
        } else {
            List<BulkItemResponse> bulkItemResponses = esPlusClient.updateBatch( type, entityList,index);
            failBulkItemResponses.addAll(bulkItemResponses);
        }

        return failBulkItemResponses;
    }

    /**
     * 更新包装
     */
    public <T> BulkByScrollResponse updateByWrapper(String type, EsParamWrapper<T> esUpdateWrapper,String... index) {
        return esPlusClient.updateByWrapper( type, esUpdateWrapper,index);
    }


    /**
     * 增量
     *
     * @param index           索引
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse increment(String type, EsParamWrapper<T> esUpdateWrapper,String... index) {
        return esPlusClient.increment( type, esUpdateWrapper,index);
    }

    /**
     * 删除
     *
     * @param index 索引
     * @param id    id
     * @return boolean
     */
    public boolean delete( String type, String id,String... index) {
        return esPlusClient.delete( type, id,index);
    }


    /**
     * 删除根据查询
     *
     * @param index           指数
     * @param esUpdateWrapper es更新包装
     * @return {@link BulkByScrollResponse}
     */
    public <T> BulkByScrollResponse deleteByQuery( String type, EsParamWrapper<T> esUpdateWrapper,String... index) {
        return esPlusClient.deleteByQuery(type, esUpdateWrapper,index);
    }


    /**
     * 删除批处理
     *
     * @param index      指数
     * @param esDataList 数据列表
     * @return boolean
     */
    public boolean deleteBatchByIds(String type, Collection<String> esDataList,String... index) {
        return esPlusClient.deleteBatch(type, esDataList,index);
    }


    /**
     * 统计
     *
     * @param esParamWrapper es param包装
     * @param index          索引
     * @return long
     */
    public <T> long count(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        return esPlusClient.count(type, esParamWrapper,index);
    }


    /**
     * 搜索根据包装器
     *
     * @param esParamWrapper es param包装
     * @param index          索引
     * @return {@link EsResponse}<{@link T}>
     */
    public <T> EsResponse<T> search(String type, EsParamWrapper<T> esParamWrapper,String... index) {
        return esPlusClient.search(type, esParamWrapper,index);
    }


    /**
     * 滚动根据包装器
     *
     * @param esParamWrapper es param包装
     * @param index          指数
     * @param keepTime       保持时间
     * @param scollId        滚动处理Id
     */
    public <T> EsResponse<T> scroll( String type, EsParamWrapper<T> esParamWrapper, Duration keepTime, String scollId,String... index) {
        return esPlusClient.scroll(type, esParamWrapper, keepTime, scollId,index);
    }


    /**
     * 聚合
     *
     * @param index          指数
     * @param esParamWrapper es param包装
     */
    public <T> EsAggResponse<T> aggregations( String type, EsParamWrapper<T> esParamWrapper,String... index) {
        return esPlusClient.aggregations( type, esParamWrapper,index);
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

    public String executeDSL(String dsl, String... index) {
        return esPlusClient.executeDSL(dsl,index);
    }
    public String translateSQL(String sql) {
        return esPlusClient.translateSql(sql);
    }
    public <T> EsResponse<T> executeSQL(String sql,Class<T> tClass) {
        return esPlusClient.executeSQL(sql,tClass);
    }
    
    public String getAliasByIndex(String index) {
     return    esPlusIndexClient.getAliasByIndex(index);
    }
    
    public void removeInterceptor(Class<?> clazz) {
        esPlusClientProxy.removeInterceptor(clazz);
    }
    
    /**
     *  saveOrUpdate
     */
    public <T> boolean saveOrUpdate(String type, T entity, String... index) {
        return esPlusClient.saveOrUpdate(type,entity,index);
    }
    
    public EsIndexResponse getMappings(String indexName) {
        return esPlusClient.getMappings(indexName);
    }
    
    public String getIndexStat() {
        return esPlusIndexClient.getIndexStat();
    }
    
    public String getIndexHealth() {
        return esPlusIndexClient.getIndexHealth();
    }
    public String getNodes() {
        return esPlusIndexClient.getNodes();
    }
    
    public String getCmd(String cmd) {
        return esPlusIndexClient.cmdGet(cmd);
    }
}
