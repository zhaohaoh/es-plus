package com.es.plus.core.wrapper.aggregation;

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.*;
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

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsAggregationWrapper<Children, R> {

    ValueCountAggregationBuilder count(R name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    AvgAggregationBuilder avg(R name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    WeightedAvgAggregationBuilder weightedAvg(R name);

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    MaxAggregationBuilder max(R name);

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    MinAggregationBuilder min(R name);

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    SumAggregationBuilder sum(R name);

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    StatsAggregationBuilder stats(R name);

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    ExtendedStatsAggregationBuilder extendedStats(R name);

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    FilterAggregationBuilder filter(R name, QueryBuilder filter);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    FiltersAggregationBuilder filters(R name, FiltersAggregator.KeyedFilter... filters);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    FiltersAggregationBuilder filters(R name, QueryBuilder... filters);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    AdjacencyMatrixAggregationBuilder adjacencyMatrix(R name, Map<String, QueryBuilder> filters);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    AdjacencyMatrixAggregationBuilder adjacencyMatrix(R name, String separator, Map<String, QueryBuilder> filters);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    SamplerAggregationBuilder sampler(R name);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    DiversifiedAggregationBuilder diversifiedSampler(R name);

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    GlobalAggregationBuilder global(R name);

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    MissingAggregationBuilder missing(R name);

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    NestedAggregationBuilder nested(R name, String path);

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    ReverseNestedAggregationBuilder reverseNested(R name);

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    GeoDistanceAggregationBuilder geoDistance(R name, GeoPoint origin);

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    HistogramAggregationBuilder histogram(R name);

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    GeoHashGridAggregationBuilder geohashGrid(R name);

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    GeoTileGridAggregationBuilder geotileGrid(R name);

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    SignificantTermsAggregationBuilder significantTerms(R name);


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    SignificantTextAggregationBuilder significantText(R name, String fieldName);


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    DateHistogramAggregationBuilder dateHistogram(R name);

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    RangeAggregationBuilder range(R name);

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    DateRangeAggregationBuilder dateRange(R name);

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    IpRangeAggregationBuilder ipRange(R name);

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    TermsAggregationBuilder terms(R name);

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    PercentilesAggregationBuilder percentiles(R name);

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    PercentileRanksAggregationBuilder percentileRanks(R name, double[] values);

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviation(R name);

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    CardinalityAggregationBuilder cardinality(R name);

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    TopHitsAggregationBuilder topHits(R name);

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    GeoBoundsAggregationBuilder geoBounds(R name);

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    GeoCentroidAggregationBuilder geoCentroid(R name);

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    ScriptedMetricAggregationBuilder scriptedMetric(R name);

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    CompositeAggregationBuilder composite(R name, List<CompositeValuesSourceBuilder<?>> sources);


    /**
     * piple的方法
     */
    DerivativePipelineAggregationBuilder derivative(R name,String bucketsPath);

    MaxBucketPipelineAggregationBuilder maxBucket(R name,String bucketsPath);

    MinBucketPipelineAggregationBuilder minBucket(R name,String bucketsPath);

    AvgBucketPipelineAggregationBuilder avgBucket(R name,String bucketsPath);

    SumBucketPipelineAggregationBuilder sumBucket(R name,String bucketsPath);

    StatsBucketPipelineAggregationBuilder statsBucket(R name,String bucketsPath);

    ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucket(R name,String bucketsPath);

    PercentilesBucketPipelineAggregationBuilder percentilesBucket(R name,String bucketsPath);

    BucketScriptPipelineAggregationBuilder bucketScript(R name,Map<String, String> bucketsPathsMap, Script script);

    BucketScriptPipelineAggregationBuilder bucketScript(R name, Script script, String... bucketsPaths);

    BucketSelectorPipelineAggregationBuilder bucketSelector(R name,Map<String, String> bucketsPathsMap, Script script);

    BucketSelectorPipelineAggregationBuilder bucketSelector(R name, Script script, String... bucketsPaths);

    BucketSortPipelineAggregationBuilder bucketSort(R name,List<FieldSortBuilder> sorts);

    CumulativeSumPipelineAggregationBuilder cumulativeSum(R name,String bucketsPath);

    SerialDiffPipelineAggregationBuilder diff(R name,String bucketsPath);

    MovFnPipelineAggregationBuilder movingFunction(R name,Script script, String bucketsPaths, int window);

    Children add(BaseAggregationBuilder agg);

}
