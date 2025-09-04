package com.es.plus.common.core;


import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpDateHistogramInterval;
import com.es.plus.common.pojo.es.EpAggBuilder;

import javax.naming.OperationNotSupportedException;
import java.util.Map;


public interface EsAggClient {
    
    EpAggBuilder count(String name,String field);
    
    
    
    EpAggBuilder avg(String name,String field);
    
    
    EpAggBuilder weightedAvg(String name,String field);
    
    
    
    EpAggBuilder max(String name,String field);
    
    
    EpAggBuilder min(String name,String field);
    
    
    
    EpAggBuilder sum(String name,String field);
    
    
    EpAggBuilder stats(String name,String field);
    
    
    
    EpAggBuilder extendedStats(String name,String field);
    
    
    EpAggBuilder filter(String name,String field, EsParamWrapper<?> esParamWrapper);
    
    
    
    
    EpAggBuilder filters(String name,String field, EsParamWrapper<?>... esParamWrapper);
    
    EpAggBuilder adjacencyMatrix(String name,String field, Map<String, EsParamWrapper<?>> esParamWrapper);
    
    
    
    EpAggBuilder adjacencyMatrix(String name,String field, String separator, Map<String, EsParamWrapper<?>> esParamWrapper);
    
    EpAggBuilder sampler(String name,String field);
    
    
    
    EpAggBuilder diversifiedSampler(String name,String field);
    
    
    
    EpAggBuilder global(String name,String field);
    
    
    EpAggBuilder missing(String name,String field);
    
    
    
    EpAggBuilder nested(String name,String field, String path);
    
    
    EpAggBuilder reverseNested(String name,String field) throws OperationNotSupportedException;
    
    
    //    EpAggBuilder geoDistance(String name,String field, GeoPoint origin);
    
    
    EpAggBuilder histogram(String name,String field);
    
    
    
    EpAggBuilder geohashGrid(String name,String field) throws OperationNotSupportedException;
    
    
    
    EpAggBuilder geotileGrid(String name,String field) throws OperationNotSupportedException;
    
    
    EpAggBuilder significantTerms(String name,String field);
    
    
    
    
    EpAggBuilder significantText(String name,String field);
    
    
    
    EpAggBuilder dateHistogram(String name,String field, EpDateHistogramInterval dateHistogramInterval);
    
    
    EpAggBuilder range(String name,String field,String key ,Double from,Double to);
    
    
    EpAggBuilder dateRange(String name,String field,String key ,String from,String to);
    
    
    
    EpAggBuilder ipRange(String name,String field);
    
    
    EpAggBuilder terms(String name,String field);
    
    
    
    
    EpAggBuilder percentiles(String name,String field);
    
    
    
    EpAggBuilder percentileStringanks(String name,String field, double[] values);
    
    
    EpAggBuilder medianAbsoluteDeviation(String name,String field);
    
    
    
    EpAggBuilder cardinality(String name,String field);
    
    
    EpAggBuilder topHits(String name,String field);
    
    
    EpAggBuilder geoBounds(String name,String field);
    
    
    
    EpAggBuilder geoCentroid(String name,String field);
    
    
    
    EpAggBuilder scriptedMetric(String name,String field);
    
    
    //    EpAggBuilder composite(String name,String field, List<CompositeValuesSourceBuilder<?>> sources);
    
    /**
     * piple
     */
    
    EpAggBuilder derivative(String name,String field, String bucketsPath);
    
    
    EpAggBuilder maxBucket(String name,String field, String bucketsPath);
    
    
    EpAggBuilder minBucket(String name,String field, String bucketsPath);
    
    
    EpAggBuilder avgBucket(String name,String field, String bucketsPath);
    
    
    EpAggBuilder sumBucket(String name,String field, String bucketsPath);
    
    EpAggBuilder statsBucket(String name,String field, String bucketsPath);
    
    
    EpAggBuilder extendedStatsBucket(String name,String field, String bucketsPath);
    
    
    EpAggBuilder percentilesBucket(String name,String field, String bucketsPath);
    
    
    //    EpAggBuilder bucketScript(String name,String field, Map<String, String> bucketsPathsMap, Script script);
    
    
    //    EpAggBuilder bucketScript(String name,String field, Script script, String... bucketsPaths);
    
    
    //    EpAggBuilder bucketSelector(String name,String field, Map<String, String> bucketsPathsMap, Script script);
    
    
    //    EpAggBuilder bucketSelector(String name,String field, Script script, String... bucketsPaths);
    
    
    //    EpAggBuilder bucketSort(String name,String field, List<FieldSortBuilder> sorts);
    
    
    EpAggBuilder cumulativeSum(String name,String field, String bucketsPath);
    
    
    EpAggBuilder diff(String name,String field, String bucketsPath);
    
    
    //    EpAggBuilder movingFunction(String name,String field, Script script, String bucketsPaths, int window);
}