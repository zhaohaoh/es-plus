package com.es.plus.adapter.core;

import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.pojo.EsPlusGetTaskResponse;
import com.es.plus.adapter.pojo.es.EpQueryBuilder;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/9/13 15:55
 */
public interface EsPlusIndexClient {
    /**
     * 创建索引
     *
     * @param index  指数
     * @param tClass t类
     */
    void createIndex(String index, Class<?> tClass);

    /**
     * 创建索引
     *
     * @param index 索引
     */
    boolean createIndex(String index);
    
    
    boolean createIndex(String index,String alias,EsSettings esSettings,Map<String, Object> mappings);
    
    /**
     *  创建索引
     */
    boolean createIndex(String index,String[] alias,Map<String, Object> esSettings,Map<String, Object> mappings);
    /**
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    boolean putMapping(String index, Class<?> tClass);

    /**
     * 映射
     *
     * @param index             索引
     * @param mappingProperties 映射属性
     */
    void putMapping(String index, Map<String, Object> mappingProperties);

    /**
     * 创建索引映射
     *
     * @param index  指数
     * @param tClass t类
     */
    void createIndexMapping(String index, Class<?> tClass);

    /**
     * 创建索引没有别名
     *
     * @param index  指数
     * @param tClass t类
     */
    boolean createIndexWithoutAlias(String index, Class<?> tClass);

    /**
     * 删除索引
     */
    boolean deleteIndex(String index);


    /**
     * 得到索引
     *
     * @param indexName 索引名称
     * @return {@link GetIndexResponse}
     */
    EsIndexResponse getIndex(String indexName);

    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    EsAliasResponse getAliasIndex(String alias);

    /**
     * 查询index是否存在
     */
    boolean indexExists(String index);

    /**
     * 更新别名
     *
     * @param oldIndexName 旧索引名称
     * @param reindexName  重建索引名称
     * @param alias        别名
     * @return boolean
     */
    boolean swapAlias(String oldIndexName, String reindexName, String alias);

    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    boolean reindex(String oldIndexName, String reindexName, EpQueryBuilder currentTime);
    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    boolean reindex(String oldIndexName, String reindexName);
    
    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    boolean reindex(String oldIndexName, String reindexName,Map<String,Object> changeProperties);

    /**
     * 更新设置
     *
     * @param index      指数
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(EsSettings esSettings,String... index);

    /**
     * 更新设置
     *
     * @param index      索引
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(Map<String, Object> esSettings,String... index);

    /**
     * 连接
     */
    boolean ping();

    /**
     * 新别名
     *
     * @param currentIndex 目前指数
     * @param alias        别名
     */
    void createAlias(String currentIndex, String alias);

    /**
     * 删除别名
     *
     * @param index 索引
     * @param alias 别名
     */
    void removeAlias(String index, String alias);


    /**
     * 合并
     *
     * @param maxSegments     最大段的数量
     * @param onlyExpungeDeletes 只删除被标记删除的索引。也就是说这次合并只是为了把被垃圾的垃圾文件删除，而去合并有效的数据;
     * @param flush              是否立即刷新
     * @param index              索引名
     * @return boolean
     */
    boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... index);
    /**
     * 强制刷
     * @param index              索引名
     * @return boolean
     */
    boolean refresh(String... index);
    /**
     * 替换别名
     */
    boolean replaceAlias(String indexName, String oldAlias, String alias);
    /**
     * 获取索引别名
     */
    String getAliasByIndex(String index);
    
    /**
     * 获取索引统计信息
     */
    String getIndexStat();
    /**
     * 获取索引健康信息
     */
    String getIndexHealth();
    
    /**
     *  获取集群节点信息
     */
    String getNodes();
    /**
     *  执行指定命令
     */
    String cmdGet(String cmd);
    
    /**
     * 异步迁移索引
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    String reindexTaskAsync(String oldIndexName, String reindexName);
    
    /**
     * 异步迁移任务列表
     * @return
     */
    ListTasksResponse reindexTaskList();
    
    /**
     * 获取任务
     * @param taskId
     * @return
     */
    EsPlusGetTaskResponse reindexTaskGet(String taskId);
    /**
     * 取消任务
     * @return
     */
    
    Boolean cancelTask(String taskId);
}
