package com.es.plus.es6.client;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsAggClient;
import com.es.plus.adapter.params.EsParamWrapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geocentroid.GeoCentroidAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.mad.MedianAbsoluteDeviationAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetricAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.weighted_avg.WeightedAvgAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.avg.AvgBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.max.MaxBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.min.MinBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.percentile.PercentilesBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.StatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.extended.ExtendedStatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.sum.SumBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.cumulativesum.CumulativeSumPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.derivative.DerivativePipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.serialdiff.SerialDiffPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


public class EsPlus6AggsClient implements EsAggClient {
    @Override
    public BaseAggregationBuilder count(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName,ValueType.STRING);
        valueCountAggregationBuilder.field(field);
        return valueCountAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder avg(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
        AvgAggregationBuilder avgAggregationBuilder = new AvgAggregationBuilder(aggName);
        avgAggregationBuilder.field(field);
        return avgAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder weightedAvg(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        WeightedAvgAggregationBuilder weightedAvgAggregationBuilder = new WeightedAvgAggregationBuilder(aggName);
        return weightedAvgAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder max(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder(aggName);
        maxAggregationBuilder.field(field);
        return maxAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder min(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MinAggregationBuilder.NAME;
        MinAggregationBuilder minAggregationBuilder = new MinAggregationBuilder(aggName);
        minAggregationBuilder.field(field);
        return  minAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder sum(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SumAggregationBuilder.NAME;
        SumAggregationBuilder sumAggregationBuilder = new SumAggregationBuilder(aggName);
        sumAggregationBuilder.field(field);
        return sumAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder stats(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
        StatsAggregationBuilder statsAggregationBuilder = new StatsAggregationBuilder(aggName);
        statsAggregationBuilder.field(field);
        return statsAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder extendedStats(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ExtendedStatsAggregationBuilder.NAME;
        ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder = new ExtendedStatsAggregationBuilder(aggName);
        extendedStatsAggregationBuilder.field(field);
        return extendedStatsAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder filter(String name,String field, EsParamWrapper<?> esParamWrapper) {
        String aggName = name!=null?name : field + AGG_DELIMITER + FilterAggregationBuilder.NAME;
        FilterAggregationBuilder filterAggregationBuilder = new FilterAggregationBuilder(aggName,esParamWrapper.getEsQueryParamWrapper()
                .getQueryBuilder());
        return filterAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder filters(String name,String field, EsParamWrapper<?>... esParamWrapper) {
        QueryBuilder[] boolQueryBuilders = Arrays.stream(esParamWrapper).map(e->e.getEsQueryParamWrapper().getQueryBuilder()).toArray(QueryBuilder[]::new);
        String aggName = name!=null?name : field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        FiltersAggregationBuilder filterAggregationBuilder = new FiltersAggregationBuilder(aggName,boolQueryBuilders);
        return filterAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder adjacencyMatrix(String name,String field, Map<String, EsParamWrapper<?>> esParamWrapper) {
        Map<String,QueryBuilder> queryBuilderMap=new HashMap<>();
        esParamWrapper.forEach((k,v)->queryBuilderMap.put(k,v.getEsQueryParamWrapper().getQueryBuilder()));
        String aggName = name!=null?name : field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        AdjacencyMatrixAggregationBuilder adjacencyMatrixAggregationBuilder = new AdjacencyMatrixAggregationBuilder(aggName,queryBuilderMap);
        return adjacencyMatrixAggregationBuilder;
    }
  
    
    @Override
    public BaseAggregationBuilder adjacencyMatrix(String name,String field, String separator, Map<String, EsParamWrapper<?>> esParamWrapper) {
        Map<String,QueryBuilder> queryBuilderMap=new HashMap<>();
        esParamWrapper.forEach((k,v)->queryBuilderMap.put(k,v.getEsQueryParamWrapper().getQueryBuilder()));
        String aggName = name + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        AdjacencyMatrixAggregationBuilder adjacencyMatrixAggregationBuilder = new AdjacencyMatrixAggregationBuilder(aggName,separator,queryBuilderMap);
        return adjacencyMatrixAggregationBuilder;
    }


    /**
     */
    @Override
    public BaseAggregationBuilder sampler(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
        SamplerAggregationBuilder samplerAggregationBuilder = new SamplerAggregationBuilder(aggName);
        return  samplerAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder diversifiedSampler(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + DiversifiedAggregationBuilder.NAME;
        DiversifiedAggregationBuilder diversifiedAggregationBuilder = new DiversifiedAggregationBuilder(aggName);
        diversifiedAggregationBuilder.field(field);
        return diversifiedAggregationBuilder;
    }

    /**
     * Create a new {@link Global} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder global(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + GlobalAggregationBuilder.NAME;
        GlobalAggregationBuilder globalAggregationBuilder = new GlobalAggregationBuilder(aggName);
        return globalAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder missing(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName,ValueType.STRING);
        missingAggregationBuilder.field(field);
        return missingAggregationBuilder;
    }

    /**
     * Create a new {@link Nested} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder nested(String name,String field, String path) {
        String aggName = name!=null?name : field + AGG_DELIMITER + NestedAggregationBuilder.NAME;
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder(aggName, path);
        return nestedAggregationBuilder;
    }

    /**

     */
    @Override
    public BaseAggregationBuilder reverseNested(String name,String field) throws OperationNotSupportedException {
        String aggName = name!=null?name : field + AGG_DELIMITER + ReverseNestedAggregationBuilder.NAME;
        ReverseNestedAggregationBuilder reverseNestedAggregationBuilder = new ReverseNestedAggregationBuilder(aggName);
        return reverseNestedAggregationBuilder;
    }


    /**
     * Create a new {@link Histogram} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder histogram(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + HistogramAggregationBuilder.NAME;
        HistogramAggregationBuilder histogramAggregationBuilder = new HistogramAggregationBuilder(aggName);
        histogramAggregationBuilder.field(field);
        return  histogramAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder geohashGrid(String name,String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     */
    @Override
    public BaseAggregationBuilder geotileGrid(String name,String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     */
    @Override
    public BaseAggregationBuilder significantTerms(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName,ValueType.STRING);
        significantTermsAggregationBuilder.field(field);
        return significantTermsAggregationBuilder;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given aggName and text field aggName
     */
    @Override
    public BaseAggregationBuilder significantText(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SignificantTextAggregationBuilder.NAME;
        SignificantTextAggregationBuilder significantTextAggregationBuilder = new SignificantTextAggregationBuilder(aggName, field);
        significantTextAggregationBuilder.fieldName(aggName);
        return significantTextAggregationBuilder;
    }


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * aggName.
     */
    @Override
    public BaseAggregationBuilder dateHistogram(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = new DateHistogramAggregationBuilder(aggName);
        dateHistogramAggregationBuilder.field(field);
        return  dateHistogramAggregationBuilder;
    }

    /**
     *
     */
    @Override
    public BaseAggregationBuilder range(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + RangeAggregationBuilder.NAME;
        RangeAggregationBuilder rangeAggregationBuilder = new RangeAggregationBuilder(aggName);
        return rangeAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder dateRange(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + DateRangeAggregationBuilder.NAME;
        DateRangeAggregationBuilder dateRangeAggregationBuilder = new DateRangeAggregationBuilder(aggName);
        return dateRangeAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder ipRange(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
        IpRangeAggregationBuilder dateRangeAggregationBuilder = new IpRangeAggregationBuilder(aggName);
        return dateRangeAggregationBuilder;
    }

    /**
     * Create a new {@link Terms} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder terms(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName,ValueType.STRING);
        termsAggregationBuilder.field(field);
        termsAggregationBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getAggSize());
        return termsAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder percentiles(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
        PercentilesAggregationBuilder percentilesAggregationBuilder = new PercentilesAggregationBuilder(aggName);
        percentilesAggregationBuilder.field(field);
        return percentilesAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder percentileStringanks(String name,String field, double[] values) {
        String aggName = name!=null?name : field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
        PercentileRanksAggregationBuilder percentileRanksAggregationBuilder = new PercentileRanksAggregationBuilder(aggName, values);
        percentileRanksAggregationBuilder.field(field);
        return percentileRanksAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder medianAbsoluteDeviation(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
        MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAggregationBuilder = new MedianAbsoluteDeviationAggregationBuilder(aggName);
        medianAbsoluteDeviationAggregationBuilder.field(field);
        return medianAbsoluteDeviationAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder cardinality(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName, ValueType.STRING);
        cardinalityAggregationBuilder.field(field);
        return cardinalityAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder topHits(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
        TopHitsAggregationBuilder topHitsAggregationBuilder = new TopHitsAggregationBuilder(aggName);
        return topHitsAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder geoBounds(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
        GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = new GeoBoundsAggregationBuilder(aggName);
        geoBoundsAggregationBuilder.field(field);
        return geoBoundsAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder geoCentroid(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
        GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = new GeoCentroidAggregationBuilder(aggName);
        geoCentroidAggregationBuilder.field(field);
        return geoCentroidAggregationBuilder;
    }

    /**
     */
    @Override
    public BaseAggregationBuilder scriptedMetric(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
        ScriptedMetricAggregationBuilder scriptedMetricAggregationBuilder = new ScriptedMetricAggregationBuilder(aggName);
        return scriptedMetricAggregationBuilder;
    }


    /**
     * piple
     */
    @Override
    public BaseAggregationBuilder derivative(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + DerivativePipelineAggregationBuilder.NAME;
        DerivativePipelineAggregationBuilder derivativePipelineAggregationBuilder = new DerivativePipelineAggregationBuilder(aggName, bucketsPath);
        return derivativePipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder maxBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MaxBucketPipelineAggregationBuilder.NAME;
        MaxBucketPipelineAggregationBuilder maxBucketPipelineAggregationBuilder = new MaxBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return maxBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder minBucket(String name,String field , String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MinBucketPipelineAggregationBuilder.NAME;
        MinBucketPipelineAggregationBuilder minBucketPipelineAggregationBuilder = new MinBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return minBucketPipelineAggregationBuilder;
    }

    @Override
    public final BaseAggregationBuilder avgBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AvgBucketPipelineAggregationBuilder.NAME;
        AvgBucketPipelineAggregationBuilder avgBucketPipelineAggregationBuilder = new AvgBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return avgBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder sumBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SumBucketPipelineAggregationBuilder.NAME;
        SumBucketPipelineAggregationBuilder sumBucketPipelineAggregationBuilder = new SumBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return sumBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder statsBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + StatsBucketPipelineAggregationBuilder.NAME;
        StatsBucketPipelineAggregationBuilder statsBucketPipelineAggregationBuilder = new StatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return statsBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder extendedStatsBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ExtendedStatsBucketPipelineAggregationBuilder.NAME;
        ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucketPipelineAggregationBuilder = new ExtendedStatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return extendedStatsBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder percentilesBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + PercentilesBucketPipelineAggregationBuilder.NAME;
        PercentilesBucketPipelineAggregationBuilder percentilesBucketPipelineAggregationBuilder = new PercentilesBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return percentilesBucketPipelineAggregationBuilder;
    }



    @Override
    public BaseAggregationBuilder cumulativeSum(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + CumulativeSumPipelineAggregationBuilder.NAME;
        CumulativeSumPipelineAggregationBuilder cumulativeSumPipelineAggregationBuilder = new CumulativeSumPipelineAggregationBuilder(aggName, bucketsPath);
        return cumulativeSumPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder diff(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SerialDiffPipelineAggregationBuilder.NAME;
        SerialDiffPipelineAggregationBuilder serialDiffPipelineAggregationBuilder = new SerialDiffPipelineAggregationBuilder(aggName, bucketsPath);
        return serialDiffPipelineAggregationBuilder;
    }

}
