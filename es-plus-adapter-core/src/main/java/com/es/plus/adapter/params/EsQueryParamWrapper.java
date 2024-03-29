package com.es.plus.adapter.params;

import lombok.Data;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
public class EsQueryParamWrapper {
    /**
     * 查询构建器
     */
    protected BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
    
    /*
     *聚合封装
     */
    protected List<BaseAggregationBuilder> aggregationBuilder = new ArrayList<>();
    /**
     * es查询结果包含字段
     */
    private EsSelect esSelect;
    /**
     * 搜索类型
     */
    private SearchType searchType;

    /**
     * 路由分片
     */
    private String[] routings;
    /**
     * 偏好
     */
    private String preference;
    /*
     *高亮
     */
    protected List<EsHighLight> esHighLights;
    /**
     * es排序列表
     */
    protected List<EsOrder> esOrderList;
    /**
     * es排序列表
     */
    protected boolean profile;
    /**
     * 页
     */
    protected Integer page;
    /**
     * 数量
     */
    protected Integer size;
    /**
     * searchAfterValues
     */
    protected Object[] searchAfterValues;
}
