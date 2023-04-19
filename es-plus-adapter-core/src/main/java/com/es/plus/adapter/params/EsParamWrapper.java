package com.es.plus.adapter.params;

import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
public class EsParamWrapper<T> {
    /**
     * es查询参数包装器
     */
    private EsQueryParamWrapper esQueryParamWrapper = new EsQueryParamWrapper();
    /**
     * 查询构建器
     */
    protected BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
    /*
     * 更新字段封装
     */
    protected EsUpdateField esUpdateField;

    /*
     *聚合封装
     */
    protected  List<BaseAggregationBuilder> aggregationBuilder = new ArrayList<>();
}
