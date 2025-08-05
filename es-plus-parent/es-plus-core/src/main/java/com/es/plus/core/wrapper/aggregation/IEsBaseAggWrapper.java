//package com.es.plus.core.wrapper.aggregation;
//
//import com.es.plus.core.wrapper.core.EsWrapper;
//import org.elasticsearch.common.geo.GeoDistance;
//import org.elasticsearch.common.geo.GeoPoint;
//import org.elasticsearch.script.Script;
//import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
//import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
//import org.elasticsearch.search.aggregations.bucket.filter.Filter;
//import org.elasticsearch.search.aggregations.bucket.filter.Filters;
//import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
//import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
//import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoTileGrid;
//import org.elasticsearch.search.aggregations.bucket.global.Global;
//import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
//import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
//import org.elasticsearch.search.aggregations.bucket.missing.Missing;
//import org.elasticsearch.search.aggregations.bucket.nested.Nested;
//import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
//import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.range.Range;
//import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
//import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
//import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.metrics.Avg;
//import org.elasticsearch.search.aggregations.metrics.Cardinality;
//import org.elasticsearch.search.aggregations.metrics.ExtendedStats;
//import org.elasticsearch.search.aggregations.metrics.GeoBounds;
//import org.elasticsearch.search.aggregations.metrics.GeoCentroid;
//import org.elasticsearch.search.aggregations.metrics.Max;
//import org.elasticsearch.search.aggregations.metrics.MedianAbsoluteDeviation;
//import org.elasticsearch.search.aggregations.metrics.Min;
//import org.elasticsearch.search.aggregations.metrics.PercentileRanks;
//import org.elasticsearch.search.aggregations.metrics.Percentiles;
//import org.elasticsearch.search.aggregations.metrics.ScriptedMetric;
//import org.elasticsearch.search.aggregations.metrics.Stats;
//import org.elasticsearch.search.aggregations.metrics.Sum;
//import org.elasticsearch.search.aggregations.metrics.TopHits;
//import org.elasticsearch.search.sort.FieldSortBuilder;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
///**
// * @Author: hzh
// * @Date: 2022/1/21 11:10
// */
//public interface IEsBaseAggWrapper<Children, R,T> {
//
//
//    /**
//     * 自定义添加一个基础聚合
//     *
//     * @param baseAggregationBuilder 基聚合构建器
//     * @return {@link Children}
//     */
//    Children add(BaseAggregationBuilder baseAggregationBuilder);
//    /**
//     * 统计
//     *
//     * @param field 名字
//     * @return {@link Children}
//     */
//    Children count(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Avg} aggregation with the given name.
//     */
//    Children avg(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Avg} aggregation with the given name.
//     */
//    Children weightedAvg(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Max} aggregation with the given name.
//     */
//    Children max(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Min} aggregation with the given name.
//     */
//    Children min(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Sum} aggregation with the given name.
//     */
//    Children sum(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Stats} aggregation with the given name.
//     */
//    Children stats(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link ExtendedStats} aggregation with the given name.
//     */
//    Children extendedStats(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Filter} aggregation with the given name.
//     */
//    Children filter(String name,R field, Consumer<Children> subAgg, Supplier<EsWrapper<T>> supplier);
//
//    /**
//     * Create a new {@link Filters} aggregation with the given name.
//     */
//    Children filters(String name,R field, Consumer<Children> subAgg, FiltersAggregator.KeyedFilter... filters);
//
//    /**
//     * Create a new {@link Filters} aggregation with the given name.
//     */
//    Children filters(String name,R field, Consumer<Children> subAgg, Supplier<EsWrapper<T>>... supplier);
//
//    /**
//     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
//     */
////    Children adjacencyMatrix(String name,R field, Consumer<Children> subAgg, Map<String, Supplier<EsWrapper<T>>> filters);
//
//    /**
//     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
//     */
//    Children adjacencyMatrix(String name,R field, Consumer<Children> subAgg, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap);
//
//    /**
//     * Create a new {@link Sampler} aggregation with the given name.
//     */
//    Children sampler(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Sampler} aggregation with the given name.
//     */
//    Children diversifiedSampler(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Global} aggregation with the given name.
//     */
//    Children global(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Missing} aggregation with the given name.
//     */
//    Children missing(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Nested} aggregation with the given name.
//     */
//    Children nested(String name,R field, String path,Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link ReverseNested} aggregation with the given name.
//     */
//    Children reverseNested(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link GeoDistance} aggregation with the given name.
//     */
//    Children geoDistance(String name,R field,  GeoPoint origin,Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Histogram} aggregation with the given name.
//     */
//    Children histogram(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
//     */
//    Children geohashGrid(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
//     */
//    Children geotileGrid(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link SignificantTerms} aggregation with the given name.
//     */
//    Children significantTerms(String name,R field, Consumer<Children> subAgg);
//
//
//    /**
//     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
//     */
//    Children significantText(String name,R field, Consumer<Children> subAgg);
//
//
//    /**
//     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
//     * name.
//     */
//    Children dateHistogram(String name,R field, DateHistogramInterval dateHistogramInterval, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Range} aggregation with the given name.
//     */
//    Children range(String name,R field,String key ,double from,double to, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
//     * given name.
//     */
//    Children dateRange(String name,R field,String key ,String from,String to, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
//     * given name.
//     */
//    Children ipRange(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Terms} aggregation with the given name.
//     */
//    Children terms(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Percentiles} aggregation with the given name.
//     */
//    Children percentiles(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link PercentileRanks} aggregation with the given name.
//     */
//    Children percentileRanks(String name,R field, Consumer<Children> subAgg, double[] values);
//
//    /**
//     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
//     */
//    Children medianAbsoluteDeviation(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link Cardinality} aggregation with the given name.
//     */
//    Children cardinality(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link TopHits} aggregation with the given name.
//     */
//    Children topHits(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link GeoBounds} aggregation with the given name.
//     */
//    Children geoBounds(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link GeoCentroid} aggregation with the given name.
//     */
//    Children geoCentroid(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link ScriptedMetric} aggregation with the given name.
//     */
//    Children scriptedMetric(String name,R field, Consumer<Children> subAgg);
//
//    /**
//     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
//     */
//    Children composite(String name,R field, Consumer<Children> subAgg, List<CompositeValuesSourceBuilder<?>> sources);
//
//
//    /**
//     * piple的方法
//     */
//    Children derivative(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children maxBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children minBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children avgBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children sumBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children statsBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children extendedStatsBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children percentilesBucket(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children bucketScript(String name,R field, Consumer<Children> subAgg,Map<String, String> bucketsPathsMap, Script script);
//
//    Children bucketScript(String name,R field, Consumer<Children> subAgg, Script script, String... bucketsPaths);
//
//    Children bucketSelector(String name,R field, Consumer<Children> subAgg,Map<String, String> bucketsPathsMap, Script script);
//
//    Children bucketSelector(String name,R field, Consumer<Children> subAgg, Script script, String... bucketsPaths);
//
//    Children bucketSort(String name,R field, Consumer<Children> subAgg,List<FieldSortBuilder> sorts);
//
//    Children bucketSort(String name,R field, Consumer<Children> subAgg,int from, int size, boolean asc, String... orderColumns);
//
//    Children cumulativeSum(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children diff(String name,R field, Consumer<Children> subAgg,String bucketsPath);
//
//    Children movingFunction(String name,R field, Consumer<Children> subAgg,Script script, String bucketsPaths, int window);
//
//}
