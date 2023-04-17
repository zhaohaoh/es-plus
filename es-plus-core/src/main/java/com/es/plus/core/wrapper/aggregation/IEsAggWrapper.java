package com.es.plus.core.wrapper.aggregation;

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsAggWrapper<Children, R> {

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
    Children count(R name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    Children avg(R name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    Children weightedAvg(R name);

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    Children max(R name);

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    Children min(R name);

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    Children sum(R name);

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    Children stats(R name);

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    Children extendedStats(R name);

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    Children filter(R name, QueryBuilder filter);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    Children filters(R name, FiltersAggregator.KeyedFilter... filters);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    Children filters(R name, QueryBuilder... filters);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    Children adjacencyMatrix(R name, Map<String, QueryBuilder> filters);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    Children adjacencyMatrix(R name, String separator, Map<String, QueryBuilder> filters);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    Children sampler(R name);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    Children diversifiedSampler(R name);

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    Children global(R name);

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    Children missing(R name);

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    Children nested(R name, String path);

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    Children reverseNested(R name);

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    Children geoDistance(R name, GeoPoint origin);

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    Children histogram(R name);

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    Children geohashGrid(R name);



    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    Children significantTerms(R name);


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    Children significantText(R name, String fieldName);


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    Children dateHistogram(R name);

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    Children range(R name);

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    Children dateRange(R name);

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    Children ipRange(R name);

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    Children terms(R name);

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    Children percentiles(R name);

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    Children percentileRanks(R name, double[] values);

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    Children medianAbsoluteDeviation(R name);

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    Children cardinality(R name);

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    Children topHits(R name);

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    Children geoBounds(R name);

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    Children geoCentroid(R name);

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    Children scriptedMetric(R name);

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    Children composite(R name, List<CompositeValuesSourceBuilder<?>> sources);


    /**
     * piple的方法
     */
    Children derivative(R name,String bucketsPath);

    Children maxBucket(R name,String bucketsPath);

    Children minBucket(R name,String bucketsPath);

    Children avgBucket(R name,String bucketsPath);

    Children sumBucket(R name,String bucketsPath);

    Children statsBucket(R name,String bucketsPath);

    Children extendedStatsBucket(R name,String bucketsPath);

    Children percentilesBucket(R name,String bucketsPath);

    Children bucketScript(R name,Map<String, String> bucketsPathsMap, Script script);

    Children bucketScript(R name, Script script, String... bucketsPaths);

    Children bucketSelector(R name,Map<String, String> bucketsPathsMap, Script script);

    Children bucketSelector(R name, Script script, String... bucketsPaths);

    Children bucketSort(R name,List<FieldSortBuilder> sorts);

    Children cumulativeSum(R name,String bucketsPath);

    Children diff(R name,String bucketsPath);

    Children movingFunction(R name,Script script, String bucketsPaths, int window);

}
