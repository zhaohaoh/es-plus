package com.es.plus.adapter.params;


import lombok.Data;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.profile.ProfileShardResult;

import java.util.List;
import java.util.Map;


@Data
public class EsResponse<T> {
    /**
     * 总数
     */
    private long total;
    /**
     * 耗时
     */
    private long tookInMillis;
    /**
     * 数据集合
     */
    private List<T> list;
    /**
     * 聚合结果
     */
    private EsAggResponse<T> esAggsResponse;

    /**
     * 分析结果
     */
    private Map<String, ProfileShardResult> profileResults;

    /**
     * SearchAfter搜索查询起始值
     */
    private Object[] firstSortValues;
    /**
     * SearchAfter搜索查询结束值
     */
    private Object[] tailSortValues;
    /**
     * 滚动id
     */
    private Map<String, List<Map<String, Object>>> innerHits;
    /**
     * 滚动id
     */
    private String scrollId;

    /**
     * 查询的总分片
     */
    private int totalShards;
    /**
     * 成功的分片
     */
    private int successfulShards;
    /**
     * 跳过的分片
     */
    private int skippedShards;
    /**
     * 分片失败的信息
     */
    private ShardSearchFailure[] shardFailures;


    public EsResponse(List<T> list, long count, EsAggResponse<T> esAggregationReponse) {
        this.list = list;
        this.total = count;
        this.esAggsResponse = esAggregationReponse;
    }
}
