package io.github.zhaohaoh.esplus.client;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;

import java.util.List;
import java.util.Map;

/**
 * 自定义聚合构建器工厂类，用于将EpAggBuilder转换为ES8原生的聚合构建器
 */
public class EpAggregationConvert {
    
    /**
     * 创建terms聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createTermsAggregation(String name, String field) {
        return new EpAggBuilder(name, "terms")
                .param("field", field);
    }
    
    /**
     * 创建sum聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSumAggregation(String name, String field) {
        return new EpAggBuilder(name, "sum")
                .param("field", field);
    }
    
    /**
     * 创建avg聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createAvgAggregation(String name, String field) {
        return new EpAggBuilder(name, "avg")
                .param("field", field);
    }
    
    /**
     * 创建count聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createCountAggregation(String name, String field) {
        return new EpAggBuilder(name, "count")
                .param("field", field);
    }
    
    /**
     * 将EpAggBuilder转换为ES8原生的Aggregation
     *
     * @param customAgg 自定义聚合构建器
     * @return Aggregation
     */
    public static Aggregation toEsAggregation(EpAggBuilder customAgg) {
        if (customAgg == null) {
            return null;
        }
        
        String type = customAgg.getType();
        String name = customAgg.getName();
        Map<String, Object> params = customAgg.getParameters();
        Aggregation esAgg = null;
        
        Object esOrginalAgg = customAgg.getEsOrginalAgg();
        if (esOrginalAgg instanceof Aggregation) {
            return (Aggregation) esOrginalAgg;
        }
        
        switch (type) {
            case "terms":
                TermsAggregation.Builder termsAggBuilder = new TermsAggregation.Builder();
                if (params.containsKey("field")) {
                    termsAggBuilder.field((String) params.get("field"));
                }
                if (customAgg.getSize() != null) {
                    termsAggBuilder.size(customAgg.getSize());
                }
                esAgg = Aggregation.of(a -> a.terms(termsAggBuilder.build()));
                break;
                
            case "sum":
                SumAggregation.Builder sumAggBuilder = new SumAggregation.Builder();
                if (params.containsKey("field")) {
                    sumAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.sum(sumAggBuilder.build()));
                break;
                
            case "avg":
                AverageAggregation.Builder avgAggBuilder = new AverageAggregation.Builder();
                if (params.containsKey("field")) {
                    avgAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.avg(avgAggBuilder.build()));
                break;
                
            case "count":
                ValueCountAggregation.Builder countAggBuilder = new ValueCountAggregation.Builder();
                if (params.containsKey("field")) {
                    countAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.valueCount(countAggBuilder.build()));
                break;
                
            case "max":
                MaxAggregation.Builder maxAggBuilder = new MaxAggregation.Builder();
                if (params.containsKey("field")) {
                    maxAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.max(maxAggBuilder.build()));
                break;
                
            case "min":
                MinAggregation.Builder minAggBuilder = new MinAggregation.Builder();
                if (params.containsKey("field")) {
                    minAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.min(minAggBuilder.build()));
                break;
                
            case "nested":
                NestedAggregation.Builder nestedAggBuilder = new NestedAggregation.Builder();
                if (params.containsKey("path")) {
                    nestedAggBuilder.path((String) params.get("path"));
                }
                esAgg = Aggregation.of(a -> a.nested(nestedAggBuilder.build()));
                break;
                
            case "filter":
                EsParamWrapper<?> o = (EsParamWrapper<?>) params.get("query");
                EpBoolQueryBuilder boolQueryBuilder = o.getEsQueryParamWrapper().getBoolQueryBuilder();
                Query esQuery = EpQueryConverter.toEsQuery(boolQueryBuilder);
                esAgg = Aggregation.of(a -> a.filter(esQuery));
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported aggregation type: " + type);
        }
        
        // 处理子聚合
        List<EpAggBuilder> subAggs = customAgg.getSubAggregation();
        if (subAggs != null && !subAggs.isEmpty()) {
            // 注意：在ES8中，子聚合需要在父聚合上设置
            // 这里只是简单示例，实际实现可能需要根据聚合类型进行特殊处理
        }
        
        return esAgg;
    }
}
