package com.es.plus.core.params;

import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.core.wrapper.core.EsQueryParamWrapper;
import com.es.plus.pojo.EsUpdateField;
import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Data
public class EsParamWrapper<T> {
    private EsQueryParamWrapper esQueryParamWrapper = new EsQueryParamWrapper();
    protected BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
    /*
     * 更新字段封装
     */
    protected EsUpdateField esUpdateField;

    /*
     *聚合封装
     */
    protected EsLambdaAggWrapper<T> esLambdaAggWrapper;
    /*
     *聚合封装
     */
    protected EsAggWrapper<T> esAggWrapper;
}
