package com.es.plus.adapter.core;


import com.es.plus.adapter.params.EsParamWrapper;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoTileGrid;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.GeoCentroid;
import org.elasticsearch.search.aggregations.metrics.MedianAbsoluteDeviation;
import org.elasticsearch.search.aggregations.metrics.ScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.TopHits;

import javax.naming.OperationNotSupportedException;
import java.util.Map;


public interface EsAggClient {

    BaseAggregationBuilder count(String name,String field);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */

    BaseAggregationBuilder avg(String name,String field);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */

    BaseAggregationBuilder weightedAvg(String name,String field);

    /**
     * Create a new {@link Max} aggregation with the given name.
     */

    BaseAggregationBuilder max(String name,String field);

    /**
     * Create a new {@link Min} aggregation with the given name.
     */

    BaseAggregationBuilder min(String name,String field);

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */

    BaseAggregationBuilder sum(String name,String field);

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */

    BaseAggregationBuilder stats(String name,String field);

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */

    BaseAggregationBuilder extendedStats(String name,String field);

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */

    BaseAggregationBuilder filter(String name,String field, EsParamWrapper<?> esParamWrapper);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */

//    BaseAggregationBuilder filters(String name,String field, FiltersAggregator.KeyedFilter... filters);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */

    BaseAggregationBuilder filters(String name,String field, EsParamWrapper<?>... esParamWrapper);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */

    BaseAggregationBuilder adjacencyMatrix(String name,String field, Map<String, EsParamWrapper<?>> esParamWrapper);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */

    BaseAggregationBuilder adjacencyMatrix(String name,String field, String separator, Map<String, EsParamWrapper<?>> esParamWrapper);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */

    BaseAggregationBuilder sampler(String name,String field);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */

    BaseAggregationBuilder diversifiedSampler(String name,String field);

    /**
     * Create a new {@link Global} aggregation with the given name.
     */

    BaseAggregationBuilder global(String name,String field);

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */

    BaseAggregationBuilder missing(String name,String field);

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */

    BaseAggregationBuilder nested(String name,String field, String path);

    /**
     * Create a new {@link StringeverseNested} aggregation with the given name.
     */

    BaseAggregationBuilder reverseNested(String name,String field) throws OperationNotSupportedException;

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */

//    BaseAggregationBuilder geoDistance(String name,String field, GeoPoint origin);

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */

    BaseAggregationBuilder histogram(String name,String field);

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */

    BaseAggregationBuilder geohashGrid(String name,String field) throws OperationNotSupportedException;

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */

    BaseAggregationBuilder geotileGrid(String name,String field) throws OperationNotSupportedException;

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */

    BaseAggregationBuilder significantTerms(String name,String field);


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */

    BaseAggregationBuilder significantText(String name,String field);


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */

    BaseAggregationBuilder dateHistogram(String name,String field);

    /**
     * Create a new {@link Stringange} aggregation with the given name.
     */

    BaseAggregationBuilder range(String name,String field);

    /**
     * Create a new {@link DateStringangeAggregationBuilder} aggregation with the
     * given name.
     */

    BaseAggregationBuilder dateRange(String name,String field);

    /**
     * Create a new {@link IpStringangeAggregationBuilder} aggregation with the
     * given name.
     */

    BaseAggregationBuilder ipRange(String name,String field);

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */

    BaseAggregationBuilder terms(String name,String field);

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */

    BaseAggregationBuilder percentiles(String name,String field);

    /**
     * Create a new {@link PercentileStringanks} aggregation with the given name.
     */

    BaseAggregationBuilder percentileStringanks(String name,String field, double[] values);

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */

    BaseAggregationBuilder medianAbsoluteDeviation(String name,String field);

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */

    BaseAggregationBuilder cardinality(String name,String field);

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */

    BaseAggregationBuilder topHits(String name,String field);

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */

    BaseAggregationBuilder geoBounds(String name,String field);

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */

    BaseAggregationBuilder geoCentroid(String name,String field);

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */

    BaseAggregationBuilder scriptedMetric(String name,String field);

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */

//    BaseAggregationBuilder composite(String name,String field, List<CompositeValuesSourceBuilder<?>> sources);

    /**
     * piple
     */

    BaseAggregationBuilder derivative(String name,String field, String bucketsPath);


    BaseAggregationBuilder maxBucket(String name,String field, String bucketsPath);


    BaseAggregationBuilder minBucket(String name,String field, String bucketsPath);


    BaseAggregationBuilder avgBucket(String name,String field, String bucketsPath);


    BaseAggregationBuilder sumBucket(String name,String field, String bucketsPath);

    BaseAggregationBuilder statsBucket(String name,String field, String bucketsPath);


    BaseAggregationBuilder extendedStatsBucket(String name,String field, String bucketsPath);


    BaseAggregationBuilder percentilesBucket(String name,String field, String bucketsPath);


//    BaseAggregationBuilder bucketScript(String name,String field, Map<String, String> bucketsPathsMap, Script script);


//    BaseAggregationBuilder bucketScript(String name,String field, Script script, String... bucketsPaths);


//    BaseAggregationBuilder bucketSelector(String name,String field, Map<String, String> bucketsPathsMap, Script script);


//    BaseAggregationBuilder bucketSelector(String name,String field, Script script, String... bucketsPaths);


//    BaseAggregationBuilder bucketSort(String name,String field, List<FieldSortBuilder> sorts);


    BaseAggregationBuilder cumulativeSum(String name,String field, String bucketsPath);


    BaseAggregationBuilder diff(String name,String field, String bucketsPath);


//    BaseAggregationBuilder movingFunction(String name,String field, Script script, String bucketsPaths, int window);
}
