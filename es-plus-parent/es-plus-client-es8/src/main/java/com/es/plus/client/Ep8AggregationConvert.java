package com.es.plus.client;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义聚合构建器工厂类，用于将EpAggBuilder转换为ES8原生的聚合构建器
 */
public class Ep8AggregationConvert {
    
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
        if (name == null){
            throw new EsException("agg need name");
        }
        Map<String, Object> params = customAgg.getParameters();
        Aggregation esAgg = null;
        
        Object esOrginalAgg = customAgg.getEsOrginalAgg();
        if (esOrginalAgg!=null) {
            if (esOrginalAgg instanceof Aggregation) {
                return (Aggregation) esOrginalAgg;
            } else if (esOrginalAgg instanceof co.elastic.clients.util.ObjectBuilder) {
                co.elastic.clients.util.ObjectBuilder<? extends AggregationVariant> orginalAggObjectBuilder = (co.elastic.clients.util.ObjectBuilder<? extends AggregationVariant>) esOrginalAgg;
                return orginalAggObjectBuilder.build()._toAggregation();
            } else if (esOrginalAgg instanceof AggregationVariant) {
                // 如果是 AggregationBase 实例，直接转换为 Aggregation
                return ((AggregationVariant) esOrginalAgg)._toAggregation();
            }
        }
        
        // 预处理子聚合
        Map<String, Aggregation> subAggregations = new HashMap<>();
        List<EpAggBuilder> subAggs = customAgg.getSubAggregation();
        if (subAggs != null && !subAggs.isEmpty()) {
            for (EpAggBuilder subAgg : subAggs) {
                Aggregation subAggregation = toEsAggregation(subAgg);
                if (subAggregation != null) {
                    subAggregations.put(subAgg.getName(), subAggregation);
                }
            }
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
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.terms(termsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.terms(termsAggBuilder.build()));
                }
                break;
            
            case "sum":
                SumAggregation.Builder sumAggBuilder = new SumAggregation.Builder();
                if (params.containsKey("field")) {
                    sumAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.sum(sumAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.sum(sumAggBuilder.build()));
                }
                break;
            
            case "avg":
                AverageAggregation.Builder avgAggBuilder = new AverageAggregation.Builder();
                if (params.containsKey("field")) {
                    avgAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.avg(avgAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.avg(avgAggBuilder.build()));
                }
                break;
            
            case "count":
                ValueCountAggregation.Builder countAggBuilder = new ValueCountAggregation.Builder();
                if (params.containsKey("field")) {
                    countAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.valueCount(countAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.valueCount(countAggBuilder.build()));
                }
                break;
            
            case "max":
                MaxAggregation.Builder maxAggBuilder = new MaxAggregation.Builder();
                if (params.containsKey("field")) {
                    maxAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.max(maxAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.max(maxAggBuilder.build()));
                }
                break;
            
            case "min":
                MinAggregation.Builder minAggBuilder = new MinAggregation.Builder();
                if (params.containsKey("field")) {
                    minAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.min(minAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.min(minAggBuilder.build()));
                }
                break;
            
            case "nested":
                NestedAggregation.Builder nestedAggBuilder = new NestedAggregation.Builder();
                if (params.containsKey("path")) {
                    nestedAggBuilder.path((String) params.get("path"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.nested(nestedAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.nested(nestedAggBuilder.build()));
                }
                break;
            
            case "filter":
                EsParamWrapper<?> o = (EsParamWrapper<?>) params.get("query");
                EpBoolQueryBuilder boolQueryBuilder = o.getEsQueryParamWrapper().getBoolQueryBuilder();
                Query esQuery = Ep8QueryConverter.toEsQuery(boolQueryBuilder);
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.filter(esQuery)
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.filter(esQuery));
                }
                break;
            
            case "stats":
                StatsAggregation.Builder statsAggBuilder = new StatsAggregation.Builder();
                if (params.containsKey("field")) {
                    statsAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.stats(statsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.stats(statsAggBuilder.build()));
                }
                break;
            
            case "extended_stats":
                ExtendedStatsAggregation.Builder extendedStatsAggBuilder = new ExtendedStatsAggregation.Builder();
                if (params.containsKey("field")) {
                    extendedStatsAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.extendedStats(extendedStatsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.extendedStats(extendedStatsAggBuilder.build()));
                }
                break;
            
            case "cardinality":
                CardinalityAggregation.Builder cardinalityAggBuilder = new CardinalityAggregation.Builder();
                if (params.containsKey("field")) {
                    cardinalityAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.cardinality(cardinalityAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.cardinality(cardinalityAggBuilder.build()));
                }
                break;
            
            case "missing":
                MissingAggregation.Builder missingAggBuilder = new MissingAggregation.Builder();
                if (params.containsKey("field")) {
                    missingAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.missing(missingAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.missing(missingAggBuilder.build()));
                }
                break;
            
            case "reverse_nested":
                ReverseNestedAggregation.Builder reverseNestedAggBuilder = new ReverseNestedAggregation.Builder();
                if (params.containsKey("path")) {
                    reverseNestedAggBuilder.path((String) params.get("path"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.reverseNested(reverseNestedAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.reverseNested(reverseNestedAggBuilder.build()));
                }
                break;
            
            case "histogram":
                HistogramAggregation.Builder histogramAggBuilder = new HistogramAggregation.Builder();
                if (params.containsKey("field")) {
                    histogramAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    histogramAggBuilder.interval(((Number) params.get("interval")).doubleValue());
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.histogram(histogramAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.histogram(histogramAggBuilder.build()));
                }
                break;
            
            case "date_histogram":
                DateHistogramAggregation.Builder dateHistogramAggBuilder = new DateHistogramAggregation.Builder();
                if (params.containsKey("field")) {
                    dateHistogramAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    dateHistogramAggBuilder.fixedInterval(co.elastic.clients.elasticsearch._types.Time.of(t ->
                            t.time((String) params.get("interval"))));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.dateHistogram(dateHistogramAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.dateHistogram(dateHistogramAggBuilder.build()));
                }
                break;
            
            case "range":
                RangeAggregation.Builder rangeAggBuilder = new RangeAggregation.Builder();
                if (params.containsKey("field")) {
                    rangeAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.range(rangeAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.range(rangeAggBuilder.build()));
                }
                break;
            
            case "date_range":
                DateRangeAggregation.Builder dateRangeAggBuilder = new DateRangeAggregation.Builder();
                if (params.containsKey("field")) {
                    dateRangeAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.dateRange(dateRangeAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.dateRange(dateRangeAggBuilder.build()));
                }
                break;
            
            case "ip_range":
                IpRangeAggregation.Builder ipRangeAggBuilder = new IpRangeAggregation.Builder();
                if (params.containsKey("field")) {
                    ipRangeAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.ipRange(ipRangeAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.ipRange(ipRangeAggBuilder.build()));
                }
                break;
            
            case "significant_terms":
                SignificantTermsAggregation.Builder significantTermsAggBuilder = new SignificantTermsAggregation.Builder();
                if (params.containsKey("field")) {
                    significantTermsAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.significantTerms(significantTermsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.significantTerms(significantTermsAggBuilder.build()));
                }
                break;
            
            case "significant_text":
                SignificantTextAggregation.Builder significantTextAggBuilder = new SignificantTextAggregation.Builder();
                if (params.containsKey("field")) {
                    significantTextAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.significantText(significantTextAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.significantText(significantTextAggBuilder.build()));
                }
                break;
            
            case "percentiles":
                PercentilesAggregation.Builder percentilesAggBuilder = new PercentilesAggregation.Builder();
                if (params.containsKey("field")) {
                    percentilesAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.percentiles(percentilesAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.percentiles(percentilesAggBuilder.build()));
                }
                break;
            
            case "percentile_ranks":
                PercentileRanksAggregation.Builder percentileRanksAggBuilder = new PercentileRanksAggregation.Builder();
                if (params.containsKey("field")) {
                    percentileRanksAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("values")) {
                    double[] values = (double[]) params.get("values");
                    percentileRanksAggBuilder.values(java.util.Arrays.stream(values).boxed().collect(java.util.stream.Collectors.toList()));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.percentileRanks(percentileRanksAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.percentileRanks(percentileRanksAggBuilder.build()));
                }
                break;
            
            case "median_absolute_deviation":
                MedianAbsoluteDeviationAggregation.Builder medianAbsoluteDeviationAggBuilder = new MedianAbsoluteDeviationAggregation.Builder();
                if (params.containsKey("field")) {
                    medianAbsoluteDeviationAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.medianAbsoluteDeviation(medianAbsoluteDeviationAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.medianAbsoluteDeviation(medianAbsoluteDeviationAggBuilder.build()));
                }
                break;
            
            case "top_hits":
                TopHitsAggregation.Builder topHitsAggBuilder = new TopHitsAggregation.Builder();
                if (customAgg.getSize() != null) {
                    topHitsAggBuilder.size(customAgg.getSize());
                }
                if (customAgg.getFrom() != null) {
                    topHitsAggBuilder.from(customAgg.getFrom());
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.topHits(topHitsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.topHits(topHitsAggBuilder.build()));
                }
                break;
            
            case "global":
                GlobalAggregation.Builder globalAggBuilder = new GlobalAggregation.Builder();
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.global(globalAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.global(globalAggBuilder.build()));
                }
                break;
            
            case "filters":
                FiltersAggregation.Builder filtersAggBuilder = new FiltersAggregation.Builder();
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.filters(filtersAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.filters(filtersAggBuilder.build()));
                }
                break;
            
            case "adjacency_matrix":
                AdjacencyMatrixAggregation.Builder adjacencyMatrixAggBuilder = new AdjacencyMatrixAggregation.Builder();
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.adjacencyMatrix(adjacencyMatrixAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.adjacencyMatrix(adjacencyMatrixAggBuilder.build()));
                }
                break;
            
            case "sampler":
                SamplerAggregation.Builder samplerAggBuilder = new SamplerAggregation.Builder();
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.sampler(samplerAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.sampler(samplerAggBuilder.build()));
                }
                break;
            
            case "weighted_avg":
                WeightedAverageAggregation.Builder weightedAvgAggBuilder = new WeightedAverageAggregation.Builder();
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.weightedAvg(weightedAvgAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.weightedAvg(weightedAvgAggBuilder.build()));
                }
                break;
            
            case "geo_bounds":
                GeoBoundsAggregation.Builder geoBoundsAggBuilder = new GeoBoundsAggregation.Builder();
                if (params.containsKey("field")) {
                    geoBoundsAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.geoBounds(geoBoundsAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.geoBounds(geoBoundsAggBuilder.build()));
                }
                break;
            
            case "geo_centroid":
                GeoCentroidAggregation.Builder geoCentroidAggBuilder = new GeoCentroidAggregation.Builder();
                if (params.containsKey("field")) {
                    geoCentroidAggBuilder.field((String) params.get("field"));
                }
                if (!subAggregations.isEmpty()) {
                    esAgg = Aggregation.of(a -> a.geoCentroid(geoCentroidAggBuilder.build())
                            .aggregations(subAggregations));
                } else {
                    esAgg = Aggregation.of(a -> a.geoCentroid(geoCentroidAggBuilder.build()));
                }
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported aggregation type: " + type);
        }
        
        return esAgg;
    }
}