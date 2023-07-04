package com.es.plus.adapter.core;


import com.es.plus.adapter.params.EsParamWrapper;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;

import javax.naming.OperationNotSupportedException;
import java.util.Map;


public interface EsAggClient {

    BaseAggregationBuilder count(String name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */

    BaseAggregationBuilder avg(String name);

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */

    BaseAggregationBuilder weightedAvg(String name);

    /**
     * Create a new {@link Max} aggregation with the given name.
     */

    BaseAggregationBuilder max(String name);

    /**
     * Create a new {@link Min} aggregation with the given name.
     */

    BaseAggregationBuilder min(String name);

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */

    BaseAggregationBuilder sum(String name);

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */

    BaseAggregationBuilder stats(String name);

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */

    BaseAggregationBuilder extendedStats(String name);

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */

    BaseAggregationBuilder filter(String name, EsParamWrapper<?> esParamWrapper);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */

//    BaseAggregationBuilder filters(String name, FiltersAggregator.KeyedFilter... filters);

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */

    BaseAggregationBuilder filters(String name, EsParamWrapper<?>... esParamWrapper);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */

    BaseAggregationBuilder adjacencyMatrix(String name, Map<String, EsParamWrapper<?>> esParamWrapper);

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */

    BaseAggregationBuilder adjacencyMatrix(String name, String separator, Map<String, EsParamWrapper<?>> esParamWrapper);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */

    BaseAggregationBuilder sampler(String name);

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */

    BaseAggregationBuilder diversifiedSampler(String name);

    /**
     * Create a new {@link Global} aggregation with the given name.
     */

    BaseAggregationBuilder global(String name);

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */

    BaseAggregationBuilder missing(String name);

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */

    BaseAggregationBuilder nested(String name, String path);

    /**
     * Create a new {@link StringeverseNested} aggregation with the given name.
     */

    BaseAggregationBuilder reverseNested(String name) throws OperationNotSupportedException;

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */

//    BaseAggregationBuilder geoDistance(String name, GeoPoint origin);

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */

    BaseAggregationBuilder histogram(String name);

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */

    BaseAggregationBuilder geohashGrid(String name) throws OperationNotSupportedException;

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */

    BaseAggregationBuilder geotileGrid(String name) throws OperationNotSupportedException;

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */

    BaseAggregationBuilder significantTerms(String name);


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */

    BaseAggregationBuilder significantText(String name, String fieldName);


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */

    BaseAggregationBuilder dateHistogram(String name);

    /**
     * Create a new {@link Stringange} aggregation with the given name.
     */

    BaseAggregationBuilder range(String name);

    /**
     * Create a new {@link DateStringangeAggregationBuilder} aggregation with the
     * given name.
     */

    BaseAggregationBuilder dateStringange(String name);

    /**
     * Create a new {@link IpStringangeAggregationBuilder} aggregation with the
     * given name.
     */

    BaseAggregationBuilder ipStringange(String name);

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */

    BaseAggregationBuilder terms(String name);

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */

    BaseAggregationBuilder percentiles(String name);

    /**
     * Create a new {@link PercentileStringanks} aggregation with the given name.
     */

    BaseAggregationBuilder percentileStringanks(String name, double[] values);

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */

    BaseAggregationBuilder medianAbsoluteDeviation(String name);

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */

    BaseAggregationBuilder cardinality(String name);

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */

    BaseAggregationBuilder topHits(String name);

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */

    BaseAggregationBuilder geoBounds(String name);

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */

    BaseAggregationBuilder geoCentroid(String name);

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */

    BaseAggregationBuilder scriptedMetric(String name);

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */

//    BaseAggregationBuilder composite(String name, List<CompositeValuesSourceBuilder<?>> sources);

    /**
     * piple
     */

    BaseAggregationBuilder derivative(String name, String bucketsPath);


    BaseAggregationBuilder maxBucket(String name, String bucketsPath);


    BaseAggregationBuilder minBucket(String name, String bucketsPath);


    BaseAggregationBuilder avgBucket(String name, String bucketsPath);


    BaseAggregationBuilder sumBucket(String name, String bucketsPath);

    BaseAggregationBuilder statsBucket(String name, String bucketsPath);


    BaseAggregationBuilder extendedStatsBucket(String name, String bucketsPath);


    BaseAggregationBuilder percentilesBucket(String name, String bucketsPath);


//    BaseAggregationBuilder bucketScript(String name, Map<String, String> bucketsPathsMap, Script script);


//    BaseAggregationBuilder bucketScript(String name, Script script, String... bucketsPaths);


//    BaseAggregationBuilder bucketSelector(String name, Map<String, String> bucketsPathsMap, Script script);


//    BaseAggregationBuilder bucketSelector(String name, Script script, String... bucketsPaths);


//    BaseAggregationBuilder bucketSort(String name, List<FieldSortBuilder> sorts);


    BaseAggregationBuilder cumulativeSum(String name, String bucketsPath);


    BaseAggregationBuilder diff(String name, String bucketsPath);


//    BaseAggregationBuilder movingFunction(String name, Script script, String bucketsPaths, int window);
}
