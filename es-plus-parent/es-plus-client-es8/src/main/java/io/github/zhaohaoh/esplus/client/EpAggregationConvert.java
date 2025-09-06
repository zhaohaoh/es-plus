package io.github.zhaohaoh.esplus.client;

import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return new EpAggBuilder(name, "value_count")
                .param("field", field);
    }
    
    /**
     * 创建max聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMaxAggregation(String name, String field) {
        return new EpAggBuilder(name, "max")
                .param("field", field);
    }
    
    /**
     * 创建min聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMinAggregation(String name, String field) {
        return new EpAggBuilder(name, "min")
                .param("field", field);
    }
    
    /**
     * 创建stats聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createStatsAggregation(String name, String field) {
        return new EpAggBuilder(name, "stats")
                .param("field", field);
    }
    
    /**
     * 创建extended_stats聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createExtendedStatsAggregation(String name, String field) {
        return new EpAggBuilder(name, "extended_stats")
                .param("field", field);
    }
    
    /**
     * 创建cardinality聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createCardinalityAggregation(String name, String field) {
        return new EpAggBuilder(name, "cardinality")
                .param("field", field);
    }
    
    /**
     * 创建missing聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMissingAggregation(String name, String field) {
        return new EpAggBuilder(name, "missing")
                .param("field", field);
    }
    
    /**
     * 创建reverse_nested聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createReverseNestedAggregation(String name) {
        return new EpAggBuilder(name, "reverse_nested");
    }
    
    /**
     * 创建histogram聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @param interval 间隔
     * @return EpAggBuilder
     */
    public static EpAggBuilder createHistogramAggregation(String name, String field, double interval) {
        return new EpAggBuilder(name, "histogram")
                .param("field", field)
                .param("interval", interval);
    }
    
    /**
     * 创建date_histogram聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @param interval 间隔
     * @return EpAggBuilder
     */
    public static EpAggBuilder createDateHistogramAggregation(String name, String field, String interval) {
        return new EpAggBuilder(name, "date_histogram")
                .param("field", field)
                .param("interval", interval);
    }
    
    /**
     * 创建range聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createRangeAggregation(String name, String field) {
        return new EpAggBuilder(name, "range")
                .param("field", field);
    }
    
    /**
     * 创建date_range聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createDateRangeAggregation(String name, String field) {
        return new EpAggBuilder(name, "date_range")
                .param("field", field);
    }
    
    /**
     * 创建ip_range聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createIpRangeAggregation(String name, String field) {
        return new EpAggBuilder(name, "ip_range")
                .param("field", field);
    }
    
    /**
     * 创建significant_terms聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSignificantTermsAggregation(String name, String field) {
        return new EpAggBuilder(name, "significant_terms")
                .param("field", field);
    }
    
    /**
     * 创建significant_text聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSignificantTextAggregation(String name, String field) {
        return new EpAggBuilder(name, "significant_text")
                .param("field", field);
    }
    
    /**
     * 创建percentiles聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createPercentilesAggregation(String name, String field) {
        return new EpAggBuilder(name, "percentiles")
                .param("field", field);
    }
    
    /**
     * 创建percentile_ranks聚合
     *
     * @param name   聚合名称
     * @param field  字段名
     * @param values 值数组
     * @return EpAggBuilder
     */
    public static EpAggBuilder createPercentileRanksAggregation(String name, String field, double[] values) {
        return new EpAggBuilder(name, "percentile_ranks")
                .param("field", field)
                .param("values", values);
    }
    
    /**
     * 创建median_absolute_deviation聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMedianAbsoluteDeviationAggregation(String name, String field) {
        return new EpAggBuilder(name, "median_absolute_deviation")
                .param("field", field);
    }
    
    /**
     * 创建top_hits聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createTopHitsAggregation(String name) {
        return new EpAggBuilder(name, "top_hits");
    }
    
    /**
     * 创建geo_bounds聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createGeoBoundsAggregation(String name, String field) {
        return new EpAggBuilder(name, "geo_bounds")
                .param("field", field);
    }
    
    /**
     * 创建geo_centroid聚合
     *
     * @param name  聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createGeoCentroidAggregation(String name, String field) {
        return new EpAggBuilder(name, "geo_centroid")
                .param("field", field);
    }
    
    /**
     * 创建scripted_metric聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createScriptedMetricAggregation(String name) {
        return new EpAggBuilder(name, "scripted_metric");
    }
    
    /**
     * 创建global聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createGlobalAggregation(String name) {
        return new EpAggBuilder(name, "global");
    }
    
    /**
     * 创建sampler聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSamplerAggregation(String name) {
        return new EpAggBuilder(name, "sampler");
    }
    
    /**
     * 创建weighted_avg聚合
     *
     * @param name 聚合名称
     * @return EpAggBuilder
     */
    public static EpAggBuilder createWeightedAvgAggregation(String name) {
        return new EpAggBuilder(name, "weighted_avg");
    }
    
    /**
     * 创建derivative聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createDerivativeAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "derivative")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建max_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMaxBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "max_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建min_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createMinBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "min_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建avg_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createAvgBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "avg_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建sum_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSumBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "sum_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建stats_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createStatsBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "stats_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建extended_stats_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createExtendedStatsBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "extended_stats_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建percentiles_bucket聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createPercentilesBucketAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "percentiles_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建cumulative_sum聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createCumulativeSumAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "cumulative_sum")
                .param("bucketsPath", bucketsPath);
    }
    
    /**
     * 创建serial_diff聚合
     *
     * @param name        聚合名称
     * @param bucketsPath 桶路径
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSerialDiffAggregation(String name, String bucketsPath) {
        return new EpAggBuilder(name, "serial_diff")
                .param("bucketsPath", bucketsPath);
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
            
            case "value_count":
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
            
            case "stats":
                StatsAggregation.Builder statsAggBuilder = new StatsAggregation.Builder();
                if (params.containsKey("field")) {
                    statsAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.stats(statsAggBuilder.build()));
                break;
            
            case "extended_stats":
                ExtendedStatsAggregation.Builder extendedStatsAggBuilder = new ExtendedStatsAggregation.Builder();
                if (params.containsKey("field")) {
                    extendedStatsAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.extendedStats(extendedStatsAggBuilder.build()));
                break;
            
            case "cardinality":
                CardinalityAggregation.Builder cardinalityAggBuilder = new CardinalityAggregation.Builder();
                if (params.containsKey("field")) {
                    cardinalityAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.cardinality(cardinalityAggBuilder.build()));
                break;
            
            case "missing":
                MissingAggregation.Builder missingAggBuilder = new MissingAggregation.Builder();
                if (params.containsKey("field")) {
                    missingAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.missing(missingAggBuilder.build()));
                break;
            
            case "nested":
                NestedAggregation.Builder nestedAggBuilder = new NestedAggregation.Builder();
                if (params.containsKey("path")) {
                    nestedAggBuilder.path((String) params.get("path"));
                }
                esAgg = Aggregation.of(a -> a.nested(nestedAggBuilder.build()));
                break;
            
            case "reverse_nested":
                ReverseNestedAggregation.Builder reverseNestedAggBuilder = new ReverseNestedAggregation.Builder();
                esAgg = Aggregation.of(a -> a.reverseNested(reverseNestedAggBuilder.build()));
                break;
            
            case "histogram":
                HistogramAggregation.Builder histogramAggBuilder = new HistogramAggregation.Builder();
                if (params.containsKey("field")) {
                    histogramAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    histogramAggBuilder.interval(((Number) params.get("interval")).doubleValue());
                }
                esAgg = Aggregation.of(a -> a.histogram(histogramAggBuilder.build()));
                break;
            
            case "date_histogram":
                DateHistogramAggregation.Builder dateHistogramAggBuilder = new DateHistogramAggregation.Builder();
                if (params.containsKey("field")) {
                    dateHistogramAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    dateHistogramAggBuilder.fixedInterval(Time.of(a->a.time((String) params.get("interval"))));
                }
                esAgg = Aggregation.of(a -> a.dateHistogram(dateHistogramAggBuilder.build()));
                break;
            
            case "range":
                RangeAggregation.Builder rangeAggBuilder = new RangeAggregation.Builder();
                if (params.containsKey("field")) {
                    rangeAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.range(rangeAggBuilder.build()));
                break;
            
            case "date_range":
                DateRangeAggregation.Builder dateRangeAggBuilder = new DateRangeAggregation.Builder();
                if (params.containsKey("field")) {
                    dateRangeAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.dateRange(dateRangeAggBuilder.build()));
                break;
            
            case "ip_range":
                IpRangeAggregation.Builder ipRangeAggBuilder = new IpRangeAggregation.Builder();
                if (params.containsKey("field")) {
                    ipRangeAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.ipRange(ipRangeAggBuilder.build()));
                break;
            
            case "significant_terms":
                SignificantTermsAggregation.Builder significantTermsAggBuilder = new SignificantTermsAggregation.Builder();
                if (params.containsKey("field")) {
                    significantTermsAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.significantTerms(significantTermsAggBuilder.build()));
                break;
            
            case "significant_text":
                SignificantTextAggregation.Builder significantTextAggBuilder = new SignificantTextAggregation.Builder();
                if (params.containsKey("field")) {
                    significantTextAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.significantText(significantTextAggBuilder.build()));
                break;
            
            case "percentiles":
                PercentilesAggregation.Builder percentilesAggBuilder = new PercentilesAggregation.Builder();
                if (params.containsKey("field")) {
                    percentilesAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.percentiles(percentilesAggBuilder.build()));
                break;
            
            case "percentile_ranks":
                PercentileRanksAggregation.Builder percentileRanksAggBuilder = new PercentileRanksAggregation.Builder();
                if (params.containsKey("field")) {
                    percentileRanksAggBuilder.field((String) params.get("field"));
                }
                if (params.containsKey("values")) {
                    Double[] values = (Double[]) params.get("values");
                    List<Double> collect = Arrays.stream(values).collect(Collectors.toList());
                    percentileRanksAggBuilder.values(collect);
                }
                esAgg = Aggregation.of(a -> a.percentileRanks(percentileRanksAggBuilder.build()));
                break;
            
            case "median_absolute_deviation":
                MedianAbsoluteDeviationAggregation.Builder medianAbsoluteDeviationAggBuilder = new MedianAbsoluteDeviationAggregation.Builder();
                if (params.containsKey("field")) {
                    medianAbsoluteDeviationAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.medianAbsoluteDeviation(medianAbsoluteDeviationAggBuilder.build()));
                break;
            
            case "top_hits":
                TopHitsAggregation.Builder topHitsAggBuilder = new TopHitsAggregation.Builder();
                if (customAgg.getSize() != null) {
                    topHitsAggBuilder.size(customAgg.getSize());
                }
                Integer from = customAgg.getFrom();
                if (from != null) {
                    topHitsAggBuilder.from(from);
                }
                esAgg = Aggregation.of(a -> a.topHits(topHitsAggBuilder.build()));
                break;
            
            case "geo_bounds":
                GeoBoundsAggregation.Builder geoBoundsAggBuilder = new GeoBoundsAggregation.Builder();
                if (params.containsKey("field")) {
                    geoBoundsAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.geoBounds(geoBoundsAggBuilder.build()));
                break;
            
            case "geo_centroid":
                GeoCentroidAggregation.Builder geoCentroidAggBuilder = new GeoCentroidAggregation.Builder();
                if (params.containsKey("field")) {
                    geoCentroidAggBuilder.field((String) params.get("field"));
                }
                esAgg = Aggregation.of(a -> a.geoCentroid(geoCentroidAggBuilder.build()));
                break;
            
            case "scripted_metric":
                ScriptedMetricAggregation.Builder scriptedMetricAggBuilder = new ScriptedMetricAggregation.Builder();
                esAgg = Aggregation.of(a -> a.scriptedMetric(scriptedMetricAggBuilder.build()));
                break;
            
            case "global":
                GlobalAggregation.Builder globalAggBuilder = new GlobalAggregation.Builder();
                esAgg = Aggregation.of(a -> a.global(globalAggBuilder.build()));
                break;
            
            case "filter":
                EsParamWrapper<?> o = (EsParamWrapper<?>) params.get("query");
                EpBoolQueryBuilder boolQueryBuilder = o.getEsQueryParamWrapper().getBoolQueryBuilder();
                Query esQuery = EpQueryConverter.toEsQuery(boolQueryBuilder);
                esAgg = Aggregation.of(a -> a.filter(f -> f.query(esQuery)));
                break;
            
            case "sampler":
                SamplerAggregation.Builder samplerAggBuilder = new SamplerAggregation.Builder();
                esAgg = Aggregation.of(a -> a.sampler(samplerAggBuilder.build()));
                break;
            
            case "weighted_avg":
                WeightedAverageAggregation.Builder weightedAvgAggBuilder = new WeightedAverageAggregation.Builder();
                esAgg = Aggregation.of(a -> a.weightedAvg(weightedAvgAggBuilder.build()));
                break;
            
            // Pipeline aggregations
            case "derivative":
                String bucketsPath = (String) params.get("bucketsPath");
                DerivativeAggregation.Builder derivativeAggBuilder = new DerivativeAggregation.Builder();
                derivativeAggBuilder.bucketsPath(new BucketsPath.Builder().single(bucketsPath).build());
                esAgg = Aggregation.of(a -> a.derivative(derivativeAggBuilder.build()));
                break;
            
            case "max_bucket":
                String maxBucketPath = (String) params.get("bucketsPath");
                MaxBucketAggregation.Builder maxBucketAggBuilder = new MaxBucketAggregation.Builder();
                maxBucketAggBuilder.bucketsPath(maxBucketPath);
                esAgg = Aggregation.of(a -> a.maxBucket(maxBucketAggBuilder.build()));
                break;
            
            case "min_bucket":
                String minBucketPath = (String) params.get("bucketsPath");
                MinBucketAggregation.Builder minBucketAggBuilder = new MinBucketAggregation.Builder();
                minBucketAggBuilder.bucketsPath(minBucketPath);
                esAgg = Aggregation.of(a -> a.minBucket(minBucketAggBuilder.build()));
                break;
            
            case "avg_bucket":
                String avgBucketPath = (String) params.get("bucketsPath");
                AvgBucketAggregation.Builder avgBucketAggBuilder = new AvgBucketAggregation.Builder();
                avgBucketAggBuilder.bucketsPath(avgBucketPath);
                esAgg = Aggregation.of(a -> a.avgBucket(avgBucketAggBuilder.build()));
                break;
            
            case "sum_bucket":
                String sumBucketPath = (String) params.get("bucketsPath");
                SumBucketAggregation.Builder sumBucketAggBuilder = new SumBucketAggregation.Builder();
                sumBucketAggBuilder.bucketsPath(sumBucketPath);
                esAgg = Aggregation.of(a -> a.sumBucket(sumBucketAggBuilder.build()));
                break;
            
            case "stats_bucket":
                String statsBucketPath = (String) params.get("bucketsPath");
                StatsBucketAggregation.Builder statsBucketAggBuilder = new StatsBucketAggregation.Builder();
                statsBucketAggBuilder.bucketsPath(statsBucketPath);
                esAgg = Aggregation.of(a -> a.statsBucket(statsBucketAggBuilder.build()));
                break;
            
            case "extended_stats_bucket":
                String extendedStatsBucketPath = (String) params.get("bucketsPath");
                ExtendedStatsBucketAggregation.Builder extendedStatsBucketAggBuilder = new ExtendedStatsBucketAggregation.Builder();
                extendedStatsBucketAggBuilder.bucketsPath(extendedStatsBucketPath);
                esAgg = Aggregation.of(a -> a.extendedStatsBucket(extendedStatsBucketAggBuilder.build()));
                break;
            
            case "percentiles_bucket":
                String percentilesBucketPath = (String) params.get("bucketsPath");
                PercentilesBucketAggregation.Builder percentilesBucketAggBuilder = new PercentilesBucketAggregation.Builder();
                percentilesBucketAggBuilder.bucketsPath(percentilesBucketPath);
                esAgg = Aggregation.of(a -> a.percentilesBucket(percentilesBucketAggBuilder.build()));
                break;
            
            case "cumulative_sum":
                String cumulativeSumPath = (String) params.get("bucketsPath");
                CumulativeSumAggregation.Builder cumulativeSumAggBuilder = new CumulativeSumAggregation.Builder();
                cumulativeSumAggBuilder.bucketsPath(cumulativeSumPath);
                esAgg = Aggregation.of(a -> a.cumulativeSum(cumulativeSumAggBuilder.build()));
                break;
            
            case "serial_diff":
                String serialDiffPath = (String) params.get("bucketsPath");
                SerialDifferencingAggregation.Builder serialDiffAggBuilder = new SerialDifferencingAggregation.Builder();
                serialDiffAggBuilder.bucketsPath(serialDiffPath);
                esAgg = Aggregation.of(a -> a.serialDiff(serialDiffAggBuilder.build()));
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
