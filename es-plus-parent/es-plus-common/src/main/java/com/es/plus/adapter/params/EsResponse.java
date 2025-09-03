package com.es.plus.adapter.params;


import lombok.Data;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


@Data
public class EsResponse<T> {
    
    /**
     * 总数
     */
    private long total;
    
    /**
     * 耗时  注意，不是es返回的，而是系统统计的
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
     * 内部嵌套对象
     */
    private EsHits innerHits;
    
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
     *  原始的返回信息
     */
    private String sourceResponse;
    
    public EsResponse(List<T> list, long count, EsAggResponse<T> esAggregationReponse) {
        this.list = list;
        this.total = count;
        this.esAggsResponse = esAggregationReponse;
    }
    
    public T getOne() {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }
}
