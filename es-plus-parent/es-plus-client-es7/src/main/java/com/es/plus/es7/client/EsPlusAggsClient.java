package com.es.plus.es7.client;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsAggClient;
import com.es.plus.adapter.params.EsParamWrapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
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
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.ExtendedStatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.GeoCentroid;
import org.elasticsearch.search.aggregations.metrics.GeoCentroidAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MedianAbsoluteDeviation;
import org.elasticsearch.search.aggregations.metrics.MedianAbsoluteDeviationAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Percentiles;
import org.elasticsearch.search.aggregations.metrics.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.ScriptedMetricAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.WeightedAvgAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.AvgBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.CumulativeSumPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.DerivativePipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ExtendedStatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MaxBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.MinBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PercentilesBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.SerialDiffPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.StatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


public class EsPlusAggsClient implements EsAggClient {
    @Override
    public BaseAggregationBuilder count(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName);
        valueCountAggregationBuilder.field(field);
        return valueCountAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder avg(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
        AvgAggregationBuilder avgAggregationBuilder = new AvgAggregationBuilder(aggName);
        avgAggregationBuilder.field(field);
        return avgAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder weightedAvg(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        WeightedAvgAggregationBuilder weightedAvgAggregationBuilder = new WeightedAvgAggregationBuilder(aggName);
        
        return weightedAvgAggregationBuilder;
    }

    /**
     * Create a new {@link Max} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder max(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder(aggName);
        maxAggregationBuilder.field(field);
        return maxAggregationBuilder;
    }

    /**
     * Create a new {@link Min} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder min(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MinAggregationBuilder.NAME;
        MinAggregationBuilder minAggregationBuilder = new MinAggregationBuilder(aggName);
        minAggregationBuilder.field(field);
        return  minAggregationBuilder;
    }

    /**
     * Create a new {@link Sum} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder sum(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SumAggregationBuilder.NAME;
        SumAggregationBuilder sumAggregationBuilder = new SumAggregationBuilder(aggName);
        sumAggregationBuilder.field(field);
        return sumAggregationBuilder;
    }

    /**
     * Create a new {@link Stats} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder stats(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
        StatsAggregationBuilder statsAggregationBuilder = new StatsAggregationBuilder(aggName);
        statsAggregationBuilder.field(field);
        return statsAggregationBuilder;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given aggName.
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
        FilterAggregationBuilder filterAggregationBuilder = new FilterAggregationBuilder(aggName,esParamWrapper.getEsQueryParamWrapper().getQueryBuilder());
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
     * Create a new {@link Sampler} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder sampler(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
        SamplerAggregationBuilder samplerAggregationBuilder = new SamplerAggregationBuilder(aggName);
        return  samplerAggregationBuilder;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given aggName.
     */
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

    /**
     * Create a new {@link Missing} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder missing(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName);
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
     * Create a new {@link StringeverseNested} aggregation with the given aggName.
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
     * Create a new {@link InternalGeoHashGrid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geohashGrid(String name,String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geotileGrid(String name,String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder significantTerms(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName);
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
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName);
        termsAggregationBuilder.field(field);
        termsAggregationBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getAggSize());
        return termsAggregationBuilder;
    }
    
   
    
    /**
     * Create a new {@link Percentiles} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder percentiles(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
        PercentilesAggregationBuilder percentilesAggregationBuilder = new PercentilesAggregationBuilder(aggName);
        percentilesAggregationBuilder.field(field);
        return percentilesAggregationBuilder;
    }

    /**
     * Create a new {@link PercentileStringanks} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder percentileStringanks(String name,String field, double[] values) {
        String aggName = name!=null?name : field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
        PercentileRanksAggregationBuilder percentileRanksAggregationBuilder = new PercentileRanksAggregationBuilder(aggName, values);
        percentileRanksAggregationBuilder.field(field);
        return percentileRanksAggregationBuilder;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given aggName
     */
    @Override
    public BaseAggregationBuilder medianAbsoluteDeviation(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
        MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAggregationBuilder = new MedianAbsoluteDeviationAggregationBuilder(aggName);
        medianAbsoluteDeviationAggregationBuilder.field(field);
        return medianAbsoluteDeviationAggregationBuilder;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder cardinality(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName);
        cardinalityAggregationBuilder.field(field);
        return cardinalityAggregationBuilder;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder topHits(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
        TopHitsAggregationBuilder topHitsAggregationBuilder = new TopHitsAggregationBuilder(aggName);
        return topHitsAggregationBuilder;
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geoBounds(String name,String field) {

        String aggName = name!=null?name : field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
        GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = new GeoBoundsAggregationBuilder(aggName);
        geoBoundsAggregationBuilder.field(field);
        return geoBoundsAggregationBuilder;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geoCentroid(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
        GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = new GeoCentroidAggregationBuilder(aggName);
        geoCentroidAggregationBuilder.field(field);
        return geoCentroidAggregationBuilder;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder scriptedMetric(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
        ScriptedMetricAggregationBuilder scriptedMetricAggregationBuilder = new ScriptedMetricAggregationBuilder(aggName);
        return scriptedMetricAggregationBuilder;
    }


    /**
     * piple 管道聚合，就是对桶进行聚合， bucketsPath=A>B
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
