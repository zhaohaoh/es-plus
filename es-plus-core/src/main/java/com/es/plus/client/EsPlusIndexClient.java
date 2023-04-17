package com.es.plus.client;

import com.es.plus.pojo.EsSettings;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.indices.*;
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
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    void putMapping(String index, Class<?> tClass);

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
    void createIndexWithoutAlias(String index, Class<?> tClass);

    /**
     * 删除索引
     */
    void deleteIndex(String index);


    /**
     * 得到索引
     *
     * @param indexName 索引名称
     * @return {@link GetIndexResponse}
     */
    GetIndexResponse getIndex(String indexName);

    /**
     * 得到别名索引
     *
     * @param alias 别名
     * @return {@link GetAliasesResponse}
     */
    GetAliasesResponse getAliasIndex(String alias);

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
    boolean replaceAlias(String oldIndexName, String reindexName, String alias);

    /**
     * 迁移重建索引
     *
     * @param oldIndexName
     * @param reindexName
     * @return
     */
    boolean reindex(String oldIndexName, String reindexName, QueryBuilder queryBuilder);

    /**
     * 更新设置
     *
     * @param index      指数
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(String index, EsSettings esSettings);

    /**
     * 更新设置
     *
     * @param index      索引
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(String index, Map<String,Object> esSettings);

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

    void removeAlias(String index, String alias);

    GetMappingsResponse getMappings(String index);
}
