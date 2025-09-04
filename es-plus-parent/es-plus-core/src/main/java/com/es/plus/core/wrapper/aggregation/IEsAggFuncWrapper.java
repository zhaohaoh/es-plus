package com.es.plus.core.wrapper.aggregation;

import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpCompositeValuesSourceBuilder;
import com.es.plus.common.pojo.es.EpDateHistogramInterval;
import com.es.plus.common.pojo.es.EpFieldSortBuilder;
import com.es.plus.common.pojo.es.EpGeoPoint;
import com.es.plus.common.pojo.es.EpScript;
import com.es.plus.core.wrapper.core.EsWrapper;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.GeoCentroidAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MedianAbsoluteDeviationAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ScriptedMetricAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.AvgBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.CumulativeSumPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.DerivativePipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ExtendedStatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MaxBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MinBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MovFnPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PercentilesBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.SerialDiffPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.StatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 聚合包装函数式接口
 */
public interface IEsAggFuncWrapper<Children, R, T> {
    
    
    Children count(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
   
    Children avg(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children weightedAvg(R name, Function<EpAggBuilder, EpAggBuilder> fn);
   
    Children max(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children min(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
   
    Children sum(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children stats(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
     
    Children extendedStats(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children filter(R name, Supplier<EsWrapper<T>> filter, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children adjacencyMatrix(R name, Map<String, Supplier<EsWrapper<T>>> filters, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children adjacencyMatrix(R name, String separator, Map<String, Supplier<EsWrapper<T>>> filters, Function<EpAggBuilder, EpAggBuilder> fn);
    
    Children sampler(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
   
    Children diversifiedSampler(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    
    Children global(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
     
    Children missing(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     *  .
     */
    Children nested(R name, String path, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     *  
     */
    Children reverseNested(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     *  
     */
    Children geoDistance(R name, EpGeoPoint origin, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     *  
     */
    Children histogram(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     *  
     */
    Children significantTerms(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * 
     */
    Children significantText(R field, String name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link   } aggregation with the given name.
     */
    Children dateHistogram(R name, EpDateHistogramInterval dateHistogramInterval, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link      } aggregation with the given name.
     */
    Children range(R name, String key, double from, double to, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link    } aggregation with the given name.
     */
    Children dateRange(R name, String key, String from, String to, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link     } aggregation with the given name.
     */
    Children ipRange(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link       } aggregation with the given name.
     */
    Children terms(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link PercentilesAggregationBuilder} aggregation with the given name.
     */
    Children percentiles(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link PercentileRanksAggregationBuilder} aggregation with the given name.
     */
    Children percentileRanks(R name, double[] values, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link MedianAbsoluteDeviationAggregationBuilder} aggregation with the given name.
     */
    Children medianAbsoluteDeviation(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link CardinalityAggregationBuilder} aggregation with the given name.
     */
    Children cardinality(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link TopHitsAggregationBuilder} aggregation with the given name.
     */
    Children topHits(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link GeoBoundsAggregationBuilder} aggregation with the given name.
     */
    Children geoBounds(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link GeoCentroidAggregationBuilder} aggregation with the given name.
     */
    Children geoCentroid(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link ScriptedMetricAggregationBuilder} aggregation with the given name.
     */
    Children scriptedMetric(R name, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link  } aggregation with the given name.
     */
    Children composite(R name, List<EpCompositeValuesSourceBuilder<?>> sources, Function<EpAggBuilder, EpAggBuilder> fn);
    
    /**
     * Create a new {@link DerivativePipelineAggregationBuilder} aggregation with the given name.
     */
    Children derivative(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link MaxBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children maxBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link MinBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children minBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link AvgBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children avgBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link SumBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children sumBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link StatsBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children statsBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link ExtendedStatsBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children extendedStatsBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link PercentilesBucketPipelineAggregationBuilder} aggregation with the given name.
     */
    Children percentilesBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link BucketScriptPipelineAggregationBuilder} aggregation with the given name.
     */
    Children bucketScript(R name, Function<EpAggBuilder, EpAggBuilder> fn, Map<String, String> bucketsPathsMap, EpScript script);
    
    /**
     * Create a new {@link BucketScriptPipelineAggregationBuilder} aggregation with the given name.
     */
    Children bucketScript(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String... bucketsPaths);
    
    /**
     * Create a new {@link BucketSelectorPipelineAggregationBuilder} aggregation with the given name.
     */
    Children bucketSelector(R name, Function<EpAggBuilder, EpAggBuilder> fn, Map<String, String> bucketsPathsMap, EpScript script);
    
    /**
     * Create a new {@link BucketSelectorPipelineAggregationBuilder} aggregation with the given name.
     */
    Children bucketSelector(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String... bucketsPaths);
    
    /**
     * Create a new {@link BucketSortPipelineAggregationBuilder} aggregation with the given name.
     */
    Children bucketSort(R name, Function<EpAggBuilder, EpAggBuilder> fn, List<EpFieldSortBuilder> sorts);
    
    /**
     * Create a new {@link CumulativeSumPipelineAggregationBuilder} aggregation with the given name.
     */
    Children cumulativeSum(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link SerialDiffPipelineAggregationBuilder} aggregation with the given name.
     */
    Children diff(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath);
    
    /**
     * Create a new {@link MovFnPipelineAggregationBuilder} aggregation with the given name.
     */
    Children movingFunction(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String bucketsPaths, int window);
}
