package com.es.plus.core.wrapper.core;

import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.indices.GetIndexResponse;

import java.util.Map;

public interface IEsIndexWrapper  {
    /**
     * 创建索引
     *
     * @param index  指数
     * @param tClass t类
     */
    EsIndexWrapper createIndex(Class<?> tClass);

    /**
     * 映射
     *
     * @param index  指数
     * @param tClass t类
     */
    EsIndexWrapper putMapping(Class<?> tClass);

    /**
     * 映射
     *
     * @param index             索引
     * @param mappingProperties 映射属性
     */
    EsIndexWrapper putMapping(Map<String, Object> mappingProperties);

    /**
     * 创建索引映射
     *
     * @param index  指数
     * @param tClass t类
     */
    EsIndexWrapper createIndexMapping(Class<?> tClass);

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
    EsIndexResponse getIndex();

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
    boolean indexExists();

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
    boolean reindex(String oldIndexName, String reindexName, Long currentTime);

    /**
     * 更新设置
     *
     * @param index      指数
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(EsSettings esSettings);

    /**
     * 更新设置
     *
     * @param index      索引
     * @param esSettings es设置
     * @return boolean
     */
    boolean updateSettings(Map<String, Object> esSettings);

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
    EsIndexWrapper createAlias(String alias);

    /**
     * 删除别名
     *
     * @param index 索引
     * @param alias 别名
     */
    EsIndexWrapper removeAlias(String alias);

    /**
     * 删除别名
     *
     * @param index 索引
     * @param alias 别名
     */
    boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... index);
}
