package com.es.plus.es7.convert;

import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;
import com.es.plus.common.pojo.es.EpBucketOrder;
import com.es.plus.common.pojo.es.EpSortBuilder;
import com.es.plus.common.pojo.es.EpSortOrder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.*;
import org.elasticsearch.search.sort.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义聚合构建器工厂类，用于将EpAggBuilder转换为ES原生的聚合构建器
 */
public class EpAggregationConvert {
    
    /**
     * 创建terms聚合
     * @param name 聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createTermsAggregation(String name, String field) {
        return new EpAggBuilder(name, "terms")
                .param("field", field);
    }
    
    /**
     * 创建sum聚合
     * @param name 聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createSumAggregation(String name, String field) {
        return new EpAggBuilder(name, "sum")
                .param("field", field);
    }
    
    /**
     * 创建avg聚合
     * @param name 聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createAvgAggregation(String name, String field) {
        return new EpAggBuilder(name, "avg")
                .param("field", field);
    }
    
    /**
     * 创建count聚合
     * @param name 聚合名称
     * @param field 字段名
     * @return EpAggBuilder
     */
    public static EpAggBuilder createCountAggregation(String name, String field) {
        return new EpAggBuilder(name, "count")
                .param("field", field);
    }
    
    /**
     * 将EpAggBuilder转换为ES原生的BaseAggregationBuilder
     * @param customAgg 自定义聚合构建器
     * @return BaseAggregationBuilder
     */
    public static BaseAggregationBuilder toEsAggregationBuilder(EpAggBuilder customAgg) {
        if (customAgg == null) {
            return null;
        }
        
        String type = customAgg.getType();
        String name = customAgg.getName();
        Map<String, Object> params = customAgg.getParameters();
        Integer size = customAgg.getSize();
        EpBucketOrder bucketOrder = customAgg.getBucketOrder();
        EpSortBuilder epSortBuilder = customAgg.getEpSortBuilder();
        Integer from = customAgg.getFrom();
        BaseAggregationBuilder esAgg = null;
        
        Object esOrginalAgg = customAgg.getEsOrginalAgg();
        if (esOrginalAgg instanceof BaseAggregationBuilder){
            return (BaseAggregationBuilder) esOrginalAgg;
        }
        switch (type) {
            case "terms":
                TermsAggregationBuilder termsAgg = new TermsAggregationBuilder(name);
                if (params.containsKey("field")) {
                    termsAgg.field((String) params.get("field"));
                }
                if (size!=null) {
                    termsAgg.size(size);
                }
                if (bucketOrder!=null) {
                    termsAgg.order(BucketOrder.aggregation(bucketOrder.getPath(),bucketOrder.asc()));
                }
                esAgg = termsAgg;
                break;
                
            case "sum":
                SumAggregationBuilder sumAgg = new SumAggregationBuilder(name);
                if (params.containsKey("field")) {
                    sumAgg.field((String) params.get("field"));
                }
                esAgg = sumAgg;
                break;
                
            case "avg":
                AvgAggregationBuilder avgAgg = new AvgAggregationBuilder(name);
                if (params.containsKey("field")) {
                    avgAgg.field((String) params.get("field"));
                }
                esAgg = avgAgg;
                break;
                
            case "count":
                ValueCountAggregationBuilder countAgg = new ValueCountAggregationBuilder(name);
                if (params.containsKey("field")) {
                    countAgg.field((String) params.get("field"));
                }
                esAgg = countAgg;
                break;
                
            case "max":
                MaxAggregationBuilder maxAgg = new MaxAggregationBuilder(name);
                if (params.containsKey("field")) {
                    maxAgg.field((String) params.get("field"));
                }
                esAgg = maxAgg;
                break;
                
            case "min":
                MinAggregationBuilder minAgg = new MinAggregationBuilder(name);
                if (params.containsKey("field")) {
                    minAgg.field((String) params.get("field"));
                }
                esAgg = minAgg;
                break;
                
            case "stats":
                StatsAggregationBuilder statsAgg = new StatsAggregationBuilder(name);
                if (params.containsKey("field")) {
                    statsAgg.field((String) params.get("field"));
                }
                esAgg = statsAgg;
                break;
                
            case "extended_stats":
                ExtendedStatsAggregationBuilder extendedStatsAgg = new ExtendedStatsAggregationBuilder(name);
                if (params.containsKey("field")) {
                    extendedStatsAgg.field((String) params.get("field"));
                }
                esAgg = extendedStatsAgg;
                break;
                
            case "cardinality":
                CardinalityAggregationBuilder cardinalityAgg = new CardinalityAggregationBuilder(name);
                if (params.containsKey("field")) {
                    cardinalityAgg.field((String) params.get("field"));
                }
                esAgg = cardinalityAgg;
                break;
                
            case "missing":
                MissingAggregationBuilder missingAgg = new MissingAggregationBuilder(name);
                if (params.containsKey("field")) {
                    missingAgg.field((String) params.get("field"));
                }
                esAgg = missingAgg;
                break;
                
            case "nested":
                NestedAggregationBuilder nestedAgg = new NestedAggregationBuilder(name, (String) params.get("path"));
                esAgg = nestedAgg;
                break;
                
            case "reverse_nested":
                ReverseNestedAggregationBuilder reverseNestedAgg = new ReverseNestedAggregationBuilder(name);
                esAgg = reverseNestedAgg;
                break;
                
            case "histogram":
                HistogramAggregationBuilder histogramAgg = new HistogramAggregationBuilder(name);
                if (params.containsKey("field")) {
                    histogramAgg.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    histogramAgg.interval(((Number) params.get("interval")).doubleValue());
                }
                esAgg = histogramAgg;
                break;
                
            case "date_histogram":
                DateHistogramAggregationBuilder dateHistogramAgg = new DateHistogramAggregationBuilder(name);
                if (params.containsKey("field")) {
                    dateHistogramAgg.field((String) params.get("field"));
                }
                if (params.containsKey("interval")) {
                    dateHistogramAgg.fixedInterval(new DateHistogramInterval((String) params.get("interval")));
                }
                esAgg = dateHistogramAgg;
                break;
                
            case "range":
                RangeAggregationBuilder rangeAgg = new RangeAggregationBuilder(name);
                if (params.containsKey("field")) {
                    rangeAgg.field((String) params.get("field"));
                }
                esAgg = rangeAgg;
                break;
                
            case "date_range":
                DateRangeAggregationBuilder dateRangeAgg = new DateRangeAggregationBuilder(name);
                if (params.containsKey("field")) {
                    dateRangeAgg.field((String) params.get("field"));
                }
                esAgg = dateRangeAgg;
                break;
                
            case "ip_range":
                IpRangeAggregationBuilder ipRangeAgg = new IpRangeAggregationBuilder(name);
                if (params.containsKey("field")) {
                    ipRangeAgg.field((String) params.get("field"));
                }
                esAgg = ipRangeAgg;
                break;
                
            case "significant_terms":
                SignificantTermsAggregationBuilder significantTermsAgg = new SignificantTermsAggregationBuilder(name);
                if (params.containsKey("field")) {
                    significantTermsAgg.field((String) params.get("field"));
                }
                esAgg = significantTermsAgg;
                break;
                
            case "significant_text":
                SignificantTextAggregationBuilder significantTextAgg = new SignificantTextAggregationBuilder(name, (String) params.get("field"));
                esAgg = significantTextAgg;
                break;
                
            case "percentiles":
                PercentilesAggregationBuilder percentilesAgg = new PercentilesAggregationBuilder(name);
                if (params.containsKey("field")) {
                    percentilesAgg.field((String) params.get("field"));
                }
                esAgg = percentilesAgg;
                break;
                
            case "percentile_ranks":
                double[] values = (double[]) params.get("values");
                PercentileRanksAggregationBuilder percentileRanksAgg = new PercentileRanksAggregationBuilder(name, values);
                if (params.containsKey("field")) {
                    percentileRanksAgg.field((String) params.get("field"));
                }
                esAgg = percentileRanksAgg;
                break;
                
            case "median_absolute_deviation":
                MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAgg = new MedianAbsoluteDeviationAggregationBuilder(name);
                if (params.containsKey("field")) {
                    medianAbsoluteDeviationAgg.field((String) params.get("field"));
                }
                esAgg = medianAbsoluteDeviationAgg;
                break;
                
            case "top_hits":
                TopHitsAggregationBuilder topHitsAgg = new TopHitsAggregationBuilder(name);
                if (size!=null) {
                    topHitsAgg.size(size);
                }
                if (from!=null) {
                    topHitsAgg.from(from);
                }
                if (epSortBuilder!=null) {
                    EpSortOrder order = epSortBuilder.getOrder();
                    topHitsAgg.sort(epSortBuilder.getField(), SortOrder.fromString(order.name()));
                }
                
                esAgg = topHitsAgg;
             
                break;
            case "geo_bounds":
                GeoBoundsAggregationBuilder geoBoundsAgg = new GeoBoundsAggregationBuilder(name);
                if (params.containsKey("field")) {
                    geoBoundsAgg.field((String) params.get("field"));
                }
                esAgg = geoBoundsAgg;
                break;
                
            case "geo_centroid":
                GeoCentroidAggregationBuilder geoCentroidAgg = new GeoCentroidAggregationBuilder(name);
                if (params.containsKey("field")) {
                    geoCentroidAgg.field((String) params.get("field"));
                }
                esAgg = geoCentroidAgg;
                break;
                
            case "scripted_metric":
                ScriptedMetricAggregationBuilder scriptedMetricAgg = new ScriptedMetricAggregationBuilder(name);
                esAgg = scriptedMetricAgg;
                break;
                
            case "global":
                GlobalAggregationBuilder globalAgg = new GlobalAggregationBuilder(name);
                esAgg = globalAgg;
                break;
                
            case "filter":
                EsParamWrapper o = (EsParamWrapper)params.get("query");
                EpBoolQueryBuilder boolQueryBuilder2 = o.getEsQueryParamWrapper().getBoolQueryBuilder();
                BoolQueryBuilder esBoolQueryBuilder = EpQueryConverter.toEsBoolQueryBuilder(boolQueryBuilder2);
                FilterAggregationBuilder filterAgg = new FilterAggregationBuilder(name, esBoolQueryBuilder);
                esAgg = filterAgg;
                break;
                
            case "filters":
                EsParamWrapper esParamWrapper = (EsParamWrapper) params.get("filters");
                EpBoolQueryBuilder boolQueryBuilder = esParamWrapper.getEsQueryParamWrapper().getBoolQueryBuilder();
                BoolQueryBuilder queryBuilder = EpQueryConverter.toEsBoolQueryBuilder(boolQueryBuilder);
                FiltersAggregationBuilder filterAggregationBuilder = new FiltersAggregationBuilder(name, queryBuilder);
                esAgg = filterAggregationBuilder;
                break;
                
            case "adjacency_matrix":
                Map<String, EsParamWrapper<?>> esParamWrapperMap = ( Map<String, EsParamWrapper<?>>) params.get("filters");
                Map<String, QueryBuilder> queryMap = new HashMap<>();
                esParamWrapperMap.forEach((k,v)->{
                    EpBoolQueryBuilder boolQueryBuilder1 = v.getEsQueryParamWrapper().getBoolQueryBuilder();
                    BoolQueryBuilder queryBuilder1 = EpQueryConverter.toEsBoolQueryBuilder(boolQueryBuilder1);
                    queryMap.put(k,queryBuilder1);
                });
              
                AdjacencyMatrixAggregationBuilder adjacencyMatrixAgg = new AdjacencyMatrixAggregationBuilder(name,queryMap);
                esAgg = adjacencyMatrixAgg;
                break;
                
            case "sampler":
                SamplerAggregationBuilder samplerAgg = new SamplerAggregationBuilder(name);
                esAgg = samplerAgg;
                break;
                
            case "weighted_avg":
                WeightedAvgAggregationBuilder weightedAvgAgg = new WeightedAvgAggregationBuilder(name);
                esAgg = weightedAvgAgg;
                break;
                
            // Pipeline aggregations
            case "derivative":
                String bucketsPath = (String) params.get("bucketsPath");
                DerivativePipelineAggregationBuilder derivativeAgg = new DerivativePipelineAggregationBuilder(name, bucketsPath);
                esAgg = derivativeAgg;
                break;
                
            case "max_bucket":
                String maxBucketPath = (String) params.get("bucketsPath");
                MaxBucketPipelineAggregationBuilder maxBucketAgg = new MaxBucketPipelineAggregationBuilder(name, maxBucketPath);
                esAgg = maxBucketAgg;
                break;
                
            case "min_bucket":
                String minBucketPath = (String) params.get("bucketsPath");
                MinBucketPipelineAggregationBuilder minBucketAgg = new MinBucketPipelineAggregationBuilder(name, minBucketPath);
                esAgg = minBucketAgg;
                break;
                
            case "avg_bucket":
                String avgBucketPath = (String) params.get("bucketsPath");
                AvgBucketPipelineAggregationBuilder avgBucketAgg = new AvgBucketPipelineAggregationBuilder(name, avgBucketPath);
                esAgg = avgBucketAgg;
                break;
                
            case "sum_bucket":
                String sumBucketPath = (String) params.get("bucketsPath");
                SumBucketPipelineAggregationBuilder sumBucketAgg = new SumBucketPipelineAggregationBuilder(name, sumBucketPath);
                esAgg = sumBucketAgg;
                break;
                
            case "stats_bucket":
                String statsBucketPath = (String) params.get("bucketsPath");
                StatsBucketPipelineAggregationBuilder statsBucketAgg = new StatsBucketPipelineAggregationBuilder(name, statsBucketPath);
                esAgg = statsBucketAgg;
                break;
                
            case "extended_stats_bucket":
                String extendedStatsBucketPath = (String) params.get("bucketsPath");
                ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucketAgg = new ExtendedStatsBucketPipelineAggregationBuilder(name, extendedStatsBucketPath);
                esAgg = extendedStatsBucketAgg;
                break;
                
            case "percentiles_bucket":
                String percentilesBucketPath = (String) params.get("bucketsPath");
                PercentilesBucketPipelineAggregationBuilder percentilesBucketAgg = new PercentilesBucketPipelineAggregationBuilder(name, percentilesBucketPath);
                esAgg = percentilesBucketAgg;
                break;
                
            case "cumulative_sum":
                String cumulativeSumPath = (String) params.get("bucketsPath");
                CumulativeSumPipelineAggregationBuilder cumulativeSumAgg = new CumulativeSumPipelineAggregationBuilder(name, cumulativeSumPath);
                esAgg = cumulativeSumAgg;
                break;
                
            case "serial_diff":
                String serialDiffPath = (String) params.get("bucketsPath");
                SerialDiffPipelineAggregationBuilder serialDiffAgg = new SerialDiffPipelineAggregationBuilder(name, serialDiffPath);
                esAgg = serialDiffAgg;
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported aggregation type: " + type);
        }
        
        // 处理子聚合
        List<EpAggBuilder> subAgg = customAgg.getSubAggregation();
        if (subAgg != null) {
            for (EpAggBuilder epAggBuilder : subAgg) {
                BaseAggregationBuilder esSubAgg = toEsAggregationBuilder(epAggBuilder);
                if (esSubAgg != null) {
                    AggregatorFactories.Builder subAggregations = new AggregatorFactories.Builder();
                    if (esSubAgg instanceof AggregationBuilder) {
                        subAggregations.addAggregator((AggregationBuilder) esSubAgg);
                    } else {
                        subAggregations.addPipelineAggregator((PipelineAggregationBuilder) esSubAgg);
                    }
                    esAgg.subAggregations(subAggregations);
                }
            }
           
        }
        
        return esAgg;
    }
}