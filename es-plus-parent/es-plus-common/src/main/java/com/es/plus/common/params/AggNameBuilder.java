//package com.es.plus.common.params;
//
//import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.global.Global;
//import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
//import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.nested.Nested;
//import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
//import org.elasticsearch.search.aggregations.metrics.*;
//
//import java.util.Map;
//
//import static com.es.plus.constant.EsConstant.AGG_DELIMITER;
//
///**
// * 默认聚合名称生成器
// *
// * @author hzh
// * @date 2023/07/19
// */
//public class AggNameBuilder {
//
//    public static String _count() {
//        return "_count";
//    }
//
//    public static String key() {
//        return "key";
//    }
//
//    public static String count(String field) {
//        return field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Avg} aggregation with the given aggName.
//     */
//
//    public static String avg(String field) {
//        return field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Avg} aggregation with the given aggName.
//     */
//
//    public static String weightedAvg(String field) {
//        return field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Max} aggregation with the given aggName.
//     */
//
//    public static String max(String field) {
//        return field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Min} aggregation with the given aggName.
//     */
//
//    public static String min(String field) {
//        return field + AGG_DELIMITER + MinAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Sum} aggregation with the given aggName.
//     */
//
//    public static String sum(String field) {
//        return field + AGG_DELIMITER + SumAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Stats} aggregation with the given aggName.
//     */
//
//    public static String stats(String field) {
//        return field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link ExtendedStats} aggregation with the given aggName.
//     */
//
//    public static String extendedStats(String field) {
//        return field + AGG_DELIMITER + ExtendedStatsAggregationBuilder.NAME;
//    }
//
//
//    public static String filter(String field, EsParamWrapper<?> esParamWrapper) {
//        return field + AGG_DELIMITER + FilterAggregationBuilder.NAME;
//    }
//
//
//    public static String filters(String field, EsParamWrapper<?>... esParamWrapper) {
//
//        return field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
//
//    }
//
//
//    public static String adjacencyMatrix(String field, Map<String, EsParamWrapper<?>> esParamWrapper) {
//        return field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
//    }
//
//
//    public static String adjacencyMatrix(String name, String separator, Map<String, EsParamWrapper<?>> esParamWrapper) {
//        return name + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Sampler} aggregation with the given aggName.
//     */
//
//    public static String sampler(String field) {
//        return field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Sampler} aggregation with the given aggName.
//     */
//
//    public static String diversifiedSampler(String field) {
//        return field + AGG_DELIMITER + DiversifiedAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Global} aggregation with the given aggName.
//     */
//
//    public static String global(String field) {
//        return field + AGG_DELIMITER + GlobalAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Missing} aggregation with the given aggName.
//     */
//
//    public static String missing(String field) {
//        return field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Nested} aggregation with the given aggName.
//     */
//
//    public static String nested(String field, String path) {
//        return field + AGG_DELIMITER + NestedAggregationBuilder.NAME;
//    }
//
//
//    /**
//     * Create a new {@link Histogram} aggregation with the given aggName.
//     */
//
//    public static String histogram(String field) {
//        return field + AGG_DELIMITER + HistogramAggregationBuilder.NAME;
//    }
//
//
//    /**
//     * Create a new {@link SignificantTerms} aggregation with the given aggName.
//     */
//
//    public static String significantTerms(String field) {
//        return field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
//    }
//
//
//    /**
//     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given aggName and text field aggName
//     */
//
//    public static String significantText(String field, String fieldName) {
//        return field + AGG_DELIMITER + SignificantTextAggregationBuilder.NAME;
//    }
//
//
//    /**
//     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
//     * aggName.
//     */
//
//    public static String dateHistogram(String field) {
//        return field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
//    }
//
//
//    public static String range(String field) {
//        return field + AGG_DELIMITER + RangeAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link DateStringangeAggregationBuilder} aggregation with the
//     * given aggName.
//     */
//
//    public static String dateRange(String field) {
//        return field + AGG_DELIMITER + DateRangeAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link IpStringangeAggregationBuilder} aggregation with the
//     * given aggName.
//     */
//
//    public static String ipRange(String field) {
//        return field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Terms} aggregation with the given aggName.
//     */
//
//    public static String terms(String field) {
//        return field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Percentiles} aggregation with the given aggName.
//     */
//
//    public static String percentiles(String field) {
//        return field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link PercentileStringanks} aggregation with the given aggName.
//     */
//
//    public static String percentileStringanks(String field, double[] values) {
//        return field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given aggName
//     */
//
//    public static String medianAbsoluteDeviation(String field) {
//        return field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link Cardinality} aggregation with the given aggName.
//     */
//
//    public static String cardinality(String field) {
//        return field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link TopHits} aggregation with the given aggName.
//     */
//
//    public static String topHits(String field) {
//        return field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link GeoBounds} aggregation with the given aggName.
//     */
//
//    public static String geoBounds(String field) {
//        return field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link GeoCentroid} aggregation with the given aggName.
//     */
//
//    public static String geoCentroid(String field) {
//        return field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
//    }
//
//    /**
//     * Create a new {@link ScriptedMetric} aggregation with the given aggName.
//     */
//
//    public static String scriptedMetric(String field) {
//        return field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
//    }
//
//
//}
