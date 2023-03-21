package com.es.plus.pojo;


import lombok.Data;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.profile.ProfileShardResult;

import java.util.List;
import java.util.Map;


@Data
public class EsResponse<T> {
    /**
     * 数据集合
     */
    private List<T> list;

    /**
     * 总数
     */
    private long total;
    /**
     * 聚合结果
     */
    private EsAggsResponse<T> esAggsResponse;

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

    private int totalShards;
    private int successfulShards;
    private int skippedShards;
    private ShardSearchFailure[] shardFailures;
    private long tookInMillis;

    public EsResponse(List<T> list, long count, EsAggsResponse<T> esAggregationReponse) {
        this.list = list;
        this.total = count;
        this.esAggsResponse = esAggregationReponse;
    }
}
