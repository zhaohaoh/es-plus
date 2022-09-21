package com.es.plus.core.wrapper.aggregation;

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoTileGrid;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.*;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.*;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsAggregationFuncWrapper<Children, R> {

    /**
     * 子聚合
     *
     * @param consumer 消费者
     * @return {@link Children}
     */
    Children subAggregation(Consumer<Children> consumer);

    /**
     * 统计
     *
     * @param name 名字
     * @return {@link Children}
     */
    Children count(R name, Function<ValueCountAggregationBuilder, ValueCountAggregationBuilder> fn);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    Children avg(R name, Function<AvgAggregationBuilder, AvgAggregationBuilder> fn);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    Children weightedAvg(R name, Function<WeightedAvgAggregationBuilder, WeightedAvgAggregationBuilder> fn);

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    Children max(R name, Function<MaxAggregationBuilder, MaxAggregationBuilder> fn);

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    Children min(R name, Function<MinAggregationBuilder, MinAggregationBuilder> fn);

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    Children sum(R name, Function<SumAggregationBuilder, SumAggregationBuilder> fn);

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    Children stats(R name, Function<StatsAggregationBuilder, StatsAggregationBuilder> fn);

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    Children extendedStats(R name, Function<ExtendedStatsAggregationBuilder, ExtendedStatsAggregationBuilder> fn);

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    Children filter(R name, QueryBuilder filter, Function<FilterAggregationBuilder, FilterAggregationBuilder> fn);


    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    Children adjacencyMatrix(R name, Map<String, QueryBuilder> filters, Function<AdjacencyMatrixAggregationBuilder, AdjacencyMatrixAggregationBuilder> fn);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    Children adjacencyMatrix(R name, String separator, Map<String, QueryBuilder> filters, Function<AdjacencyMatrixAggregationBuilder, AdjacencyMatrixAggregationBuilder> fn);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    Children sampler(R name, Function<SamplerAggregationBuilder, SamplerAggregationBuilder> fn);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    Children diversifiedSampler(R name, Function<DiversifiedAggregationBuilder, DiversifiedAggregationBuilder> fn);

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    Children global(R name, Function<GlobalAggregationBuilder, GlobalAggregationBuilder> fn);

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    Children missing(R name, Function<MissingAggregationBuilder, MissingAggregationBuilder> fn);

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    Children nested(R name, String path, Function<NestedAggregationBuilder, NestedAggregationBuilder> fn);

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    Children reverseNested(R name, Function<ReverseNestedAggregationBuilder, ReverseNestedAggregationBuilder> fn);

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    Children geoDistance(R name, GeoPoint origin, Function<GeoDistanceAggregationBuilder, GeoDistanceAggregationBuilder> fn);

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    Children histogram(R name, Function<HistogramAggregationBuilder, HistogramAggregationBuilder> fn);

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    Children geohashGrid(R name, Function<GeoHashGridAggregationBuilder, GeoHashGridAggregationBuilder> fn);

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    Children geotileGrid(R name, Function<GeoTileGridAggregationBuilder, GeoTileGridAggregationBuilder> fn);

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    Children significantTerms(R name, Function<SignificantTermsAggregationBuilder, SignificantTermsAggregationBuilder> fn);


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    Children significantText(R name, String fieldName, Function<SignificantTextAggregationBuilder, SignificantTextAggregationBuilder> fn);


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    Children dateHistogram(R name, Function<DateHistogramAggregationBuilder, DateHistogramAggregationBuilder> fn);

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    Children range(R name, Function<RangeAggregationBuilder, RangeAggregationBuilder> fn);

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    Children dateRange(R name, Function<DateRangeAggregationBuilder, DateRangeAggregationBuilder> fn);

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    Children ipRange(R name, Function<IpRangeAggregationBuilder, IpRangeAggregationBuilder> fn);

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    Children terms(R name, Function<TermsAggregationBuilder, TermsAggregationBuilder> fn);

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    Children percentiles(R name, Function<PercentilesAggregationBuilder, PercentilesAggregationBuilder> fn);

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    Children percentileRanks(R name, double[] values, Function<PercentileRanksAggregationBuilder, PercentileRanksAggregationBuilder> fn);

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    Children medianAbsoluteDeviation(R name, Function<MedianAbsoluteDeviationAggregationBuilder, MedianAbsoluteDeviationAggregationBuilder> fn);

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    Children cardinality(R name, Function<CardinalityAggregationBuilder, CardinalityAggregationBuilder> fn);

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    Children topHits(R name, Function<TopHitsAggregationBuilder, TopHitsAggregationBuilder> fn);

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    Children geoBounds(R name, Function<GeoBoundsAggregationBuilder, GeoBoundsAggregationBuilder> fn);

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    Children geoCentroid(R name, Function<GeoCentroidAggregationBuilder, GeoCentroidAggregationBuilder> fn);

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    Children scriptedMetric(R name, Function<ScriptedMetricAggregationBuilder, ScriptedMetricAggregationBuilder> fn);

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    Children composite(R name, List<CompositeValuesSourceBuilder<?>> sources, Function<CompositeAggregationBuilder, CompositeAggregationBuilder> fn);


    /**
     * piple的方法
     */
    Children derivative(R name, Function<DerivativePipelineAggregationBuilder, DerivativePipelineAggregationBuilder> fn, String bucketsPath);

    Children maxBucket(R name, Function<MaxBucketPipelineAggregationBuilder, MaxBucketPipelineAggregationBuilder> fn, String bucketsPath);

    Children minBucket(R name, Function<MinBucketPipelineAggregationBuilder, MinBucketPipelineAggregationBuilder> fn, String bucketsPath);

    Children avgBucket(R name, Function<AvgBucketPipelineAggregationBuilder, AvgBucketPipelineAggregationBuilder> fn, String bucketsPath);

    Children sumBucket(R name, Function<SumBucketPipelineAggregationBuilder, SumBucketPipelineAggregationBuilder> fn, String bucketsPath);

    Children statsBucket(R name, Function<StatsBucketPipelineAggregationBuilder, StatsBucketPipelineAggregationBuilder> fn,String bucketsPath);

    Children extendedStatsBucket(R name, Function<ExtendedStatsBucketPipelineAggregationBuilder, ExtendedStatsBucketPipelineAggregationBuilder> fn,String bucketsPath);

    Children percentilesBucket(R name, Function<PercentilesBucketPipelineAggregationBuilder, PercentilesBucketPipelineAggregationBuilder> fn,String bucketsPath);

    Children bucketScript(R name, Function<BucketScriptPipelineAggregationBuilder, BucketScriptPipelineAggregationBuilder> fn,Map<String, String>bucketsPathsMap, Script script);

    Children bucketScript(R name, Function<BucketScriptPipelineAggregationBuilder, BucketScriptPipelineAggregationBuilder> fn, Script script, String... bucketsPaths);

    Children bucketSelector(R name, Function<BucketSelectorPipelineAggregationBuilder, BucketSelectorPipelineAggregationBuilder> fn,Map<String, String>bucketsPathsMap, Script script);

    Children bucketSelector(R name, Function<BucketSelectorPipelineAggregationBuilder, BucketSelectorPipelineAggregationBuilder> fn, Script script, String... bucketsPaths);

    Children bucketSort(R name, Function<BucketSortPipelineAggregationBuilder, BucketSortPipelineAggregationBuilder> fn,List<FieldSortBuilder>sorts);

    Children cumulativeSum(R name, Function<CumulativeSumPipelineAggregationBuilder, CumulativeSumPipelineAggregationBuilder> fn,String bucketsPath);

    Children diff(R name, Function<SerialDiffPipelineAggregationBuilder, SerialDiffPipelineAggregationBuilder> fn,String bucketsPath);

    Children movingFunction(R name, Function<MovFnPipelineAggregationBuilder, MovFnPipelineAggregationBuilder> fn,Script script, String bucketsPaths, int window);

}
