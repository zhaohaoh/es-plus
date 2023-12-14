package com.es.plus.es7.client;

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
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.*;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


public class EsPlusAggsClient implements EsAggClient {
    @Override
    public BaseAggregationBuilder count(String field) {
        String aggName = field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName);
        valueCountAggregationBuilder.field(field);
        return valueCountAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder avg(String field) {
        String aggName = field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
        AvgAggregationBuilder avgAggregationBuilder = new AvgAggregationBuilder(aggName);
        avgAggregationBuilder.field(field);
        return avgAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder weightedAvg(String field) {
        String aggName = field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        WeightedAvgAggregationBuilder weightedAvgAggregationBuilder = new WeightedAvgAggregationBuilder(aggName);
        return weightedAvgAggregationBuilder;
    }

    /**
     * Create a new {@link Max} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder max(String field) {
        String aggName = field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder(aggName);
        maxAggregationBuilder.field(field);
        return maxAggregationBuilder;
    }

    /**
     * Create a new {@link Min} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder min(String field) {
        String aggName = field + AGG_DELIMITER + MinAggregationBuilder.NAME;
        MinAggregationBuilder minAggregationBuilder = new MinAggregationBuilder(aggName);
        minAggregationBuilder.field(field);
        return  minAggregationBuilder;
    }

    /**
     * Create a new {@link Sum} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder sum(String field) {
        String aggName = field + AGG_DELIMITER + SumAggregationBuilder.NAME;
        SumAggregationBuilder sumAggregationBuilder = new SumAggregationBuilder(aggName);
        sumAggregationBuilder.field(field);
        return sumAggregationBuilder;
    }

    /**
     * Create a new {@link Stats} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder stats(String field) {
        String aggName = field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
        StatsAggregationBuilder statsAggregationBuilder = new StatsAggregationBuilder(aggName);
        statsAggregationBuilder.field(field);
        return statsAggregationBuilder;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder extendedStats(String field) {
        String aggName = field + AGG_DELIMITER + ExtendedStatsAggregationBuilder.NAME;
        ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder = new ExtendedStatsAggregationBuilder(aggName);
        extendedStatsAggregationBuilder.field(field);
        return extendedStatsAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder filter(String field, EsParamWrapper<?> esParamWrapper) {
        String aggName = field + AGG_DELIMITER + FilterAggregationBuilder.NAME;
        FilterAggregationBuilder filterAggregationBuilder = new FilterAggregationBuilder(aggName,esParamWrapper.getEsQueryParamWrapper().getQueryBuilder());
        return filterAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder filters(String field, EsParamWrapper<?>... esParamWrapper) {
        QueryBuilder[] boolQueryBuilders = Arrays.stream(esParamWrapper).map(e->e.getEsQueryParamWrapper().getQueryBuilder()).toArray(QueryBuilder[]::new);
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        FiltersAggregationBuilder filterAggregationBuilder = new FiltersAggregationBuilder(aggName,boolQueryBuilders);
        return filterAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder adjacencyMatrix(String field, Map<String, EsParamWrapper<?>> esParamWrapper) {
        Map<String,QueryBuilder> queryBuilderMap=new HashMap<>();
        esParamWrapper.forEach((k,v)->queryBuilderMap.put(k,v.getEsQueryParamWrapper().getQueryBuilder()));
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        AdjacencyMatrixAggregationBuilder adjacencyMatrixAggregationBuilder = new AdjacencyMatrixAggregationBuilder(aggName,queryBuilderMap);
        return adjacencyMatrixAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder adjacencyMatrix(String name, String separator, Map<String, EsParamWrapper<?>> esParamWrapper) {
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
    public BaseAggregationBuilder sampler(String field) {
        String aggName = field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
        SamplerAggregationBuilder samplerAggregationBuilder = new SamplerAggregationBuilder(aggName);
        return  samplerAggregationBuilder;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder diversifiedSampler(String field) {
        String aggName = field + AGG_DELIMITER + DiversifiedAggregationBuilder.NAME;
        DiversifiedAggregationBuilder diversifiedAggregationBuilder = new DiversifiedAggregationBuilder(aggName);
        diversifiedAggregationBuilder.field(field);
        return diversifiedAggregationBuilder;
    }

    /**
     * Create a new {@link Global} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder global(String field) {
        String aggName = field + AGG_DELIMITER + GlobalAggregationBuilder.NAME;
        GlobalAggregationBuilder globalAggregationBuilder = new GlobalAggregationBuilder(aggName);
        return globalAggregationBuilder;
    }

    /**
     * Create a new {@link Missing} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder missing(String field) {
        String aggName = field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName);
        missingAggregationBuilder.field(field);
        return missingAggregationBuilder;
    }

    /**
     * Create a new {@link Nested} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder nested(String field, String path) {
        String aggName = field + AGG_DELIMITER + NestedAggregationBuilder.NAME;
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder(aggName, path);
        return nestedAggregationBuilder;
    }

    /**
     * Create a new {@link StringeverseNested} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder reverseNested(String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }


    /**
     * Create a new {@link Histogram} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder histogram(String field) {
        String aggName = field + AGG_DELIMITER + HistogramAggregationBuilder.NAME;
        HistogramAggregationBuilder histogramAggregationBuilder = new HistogramAggregationBuilder(aggName);
        histogramAggregationBuilder.field(field);
        return  histogramAggregationBuilder;
    }

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geohashGrid(String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geotileGrid(String field) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder significantTerms(String field) {
        String aggName = field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName);
        significantTermsAggregationBuilder.field(field);
        return significantTermsAggregationBuilder;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given aggName and text field aggName
     */
    @Override
    public BaseAggregationBuilder significantText(String field, String fieldName) {
        String aggName = field + AGG_DELIMITER + SignificantTextAggregationBuilder.NAME;
        SignificantTextAggregationBuilder significantTextAggregationBuilder = new SignificantTextAggregationBuilder(aggName, fieldName);
        significantTextAggregationBuilder.fieldName(aggName);
        return significantTextAggregationBuilder;
    }


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * aggName.
     */
    @Override
    public BaseAggregationBuilder dateHistogram(String field) {
        String aggName = field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = new DateHistogramAggregationBuilder(aggName);
        dateHistogramAggregationBuilder.field(field);
        return  dateHistogramAggregationBuilder;
    }

    /**
     *
     */
    @Override
    public BaseAggregationBuilder range(String field) {
        String aggName = field + AGG_DELIMITER + RangeAggregationBuilder.NAME;
        RangeAggregationBuilder rangeAggregationBuilder = new RangeAggregationBuilder(aggName);
        return rangeAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder dateRange(String field) {
        String aggName = field + AGG_DELIMITER + DateRangeAggregationBuilder.NAME;
        DateRangeAggregationBuilder dateRangeAggregationBuilder = new DateRangeAggregationBuilder(aggName);
        return dateRangeAggregationBuilder;
    }


    @Override
    public BaseAggregationBuilder ipRange(String field) {
        String aggName = field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
        IpRangeAggregationBuilder dateRangeAggregationBuilder = new IpRangeAggregationBuilder(aggName);
        return dateRangeAggregationBuilder;
    }

    /**
     * Create a new {@link Terms} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder terms(String field) {
        String aggName = field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName);
        termsAggregationBuilder.field(field);
        termsAggregationBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getSearchSize());
        return termsAggregationBuilder;
    }

    /**
     * Create a new {@link Percentiles} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder percentiles(String field) {
        String aggName = field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
        PercentilesAggregationBuilder percentilesAggregationBuilder = new PercentilesAggregationBuilder(aggName);
        percentilesAggregationBuilder.field(field);
        return percentilesAggregationBuilder;
    }

    /**
     * Create a new {@link PercentileStringanks} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder percentileStringanks(String field, double[] values) {
        String aggName = field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
        PercentileRanksAggregationBuilder percentileRanksAggregationBuilder = new PercentileRanksAggregationBuilder(aggName, values);
        percentileRanksAggregationBuilder.field(field);
        return percentileRanksAggregationBuilder;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given aggName
     */
    @Override
    public BaseAggregationBuilder medianAbsoluteDeviation(String field) {
        String aggName = field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
        MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAggregationBuilder = new MedianAbsoluteDeviationAggregationBuilder(aggName);
        medianAbsoluteDeviationAggregationBuilder.field(field);
        return medianAbsoluteDeviationAggregationBuilder;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder cardinality(String field) {
        String aggName = field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName);
        cardinalityAggregationBuilder.field(field);
        return cardinalityAggregationBuilder;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder topHits(String field) {
        String aggName = field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
        TopHitsAggregationBuilder topHitsAggregationBuilder = new TopHitsAggregationBuilder(aggName);
        return topHitsAggregationBuilder;
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geoBounds(String field) {

        String aggName = field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
        GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = new GeoBoundsAggregationBuilder(aggName);
        geoBoundsAggregationBuilder.field(field);
        return geoBoundsAggregationBuilder;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder geoCentroid(String field) {
        String aggName = field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
        GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = new GeoCentroidAggregationBuilder(aggName);
        geoCentroidAggregationBuilder.field(field);
        return geoCentroidAggregationBuilder;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder scriptedMetric(String field) {
        String aggName = field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
        ScriptedMetricAggregationBuilder scriptedMetricAggregationBuilder = new ScriptedMetricAggregationBuilder(aggName);
        return scriptedMetricAggregationBuilder;
    }


    /**
     * piple 管道聚合，就是对桶进行聚合， bucketsPath=A>B
     */
    @Override
    public BaseAggregationBuilder derivative(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + DerivativePipelineAggregationBuilder.NAME;
        DerivativePipelineAggregationBuilder derivativePipelineAggregationBuilder = new DerivativePipelineAggregationBuilder(aggName, bucketsPath);
        return derivativePipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder maxBucket(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + MaxBucketPipelineAggregationBuilder.NAME;
        MaxBucketPipelineAggregationBuilder maxBucketPipelineAggregationBuilder = new MaxBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return maxBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder minBucket(String field , String bucketsPath) {
        String aggName = field + AGG_DELIMITER + MinBucketPipelineAggregationBuilder.NAME;
        MinBucketPipelineAggregationBuilder minBucketPipelineAggregationBuilder = new MinBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return minBucketPipelineAggregationBuilder;
    }

    @Override
    public final BaseAggregationBuilder avgBucket(String field, String bucketsPath) {
        String aggName = field + AvgBucketPipelineAggregationBuilder.NAME;
        AvgBucketPipelineAggregationBuilder avgBucketPipelineAggregationBuilder = new AvgBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return avgBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder sumBucket(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + SumBucketPipelineAggregationBuilder.NAME;
        SumBucketPipelineAggregationBuilder sumBucketPipelineAggregationBuilder = new SumBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return sumBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder statsBucket(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + StatsBucketPipelineAggregationBuilder.NAME;
        StatsBucketPipelineAggregationBuilder statsBucketPipelineAggregationBuilder = new StatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return statsBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder extendedStatsBucket(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + ExtendedStatsBucketPipelineAggregationBuilder.NAME;
        ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucketPipelineAggregationBuilder = new ExtendedStatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return extendedStatsBucketPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder percentilesBucket(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + PercentilesBucketPipelineAggregationBuilder.NAME;
        PercentilesBucketPipelineAggregationBuilder percentilesBucketPipelineAggregationBuilder = new PercentilesBucketPipelineAggregationBuilder(aggName, bucketsPath);
        return percentilesBucketPipelineAggregationBuilder;
    }



    @Override
    public BaseAggregationBuilder cumulativeSum(String field, String bucketsPath) {

        String aggName = field + AGG_DELIMITER + CumulativeSumPipelineAggregationBuilder.NAME;
        CumulativeSumPipelineAggregationBuilder cumulativeSumPipelineAggregationBuilder = new CumulativeSumPipelineAggregationBuilder(aggName, bucketsPath);
        return cumulativeSumPipelineAggregationBuilder;
    }

    @Override
    public BaseAggregationBuilder diff(String field, String bucketsPath) {
        String aggName = field + AGG_DELIMITER + SerialDiffPipelineAggregationBuilder.NAME;
        SerialDiffPipelineAggregationBuilder serialDiffPipelineAggregationBuilder = new SerialDiffPipelineAggregationBuilder(aggName, bucketsPath);

        return serialDiffPipelineAggregationBuilder;
    }

}
