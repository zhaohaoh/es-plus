package com.es.plus.es6.client;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsAggClient;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
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

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


public class EsPlus6AggsClient implements EsAggClient {
    @Override
    public BaseAggregationBuilder count(String field) {
        String aggName = field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName,ValueType.STRING);
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
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName,ValueType.STRING);
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
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName,ValueType.STRING);
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
     * Create a new {@link Stringange} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder range(String field) {
        return null;
    }

    /**
     * Create a new {@link DateStringangeAggregationBuilder} aggregation with the
     * given aggName.
     */
    @Override
    public BaseAggregationBuilder dateStringange(String field) {
        return null;
    }

    /**
     * Create a new {@link IpStringangeAggregationBuilder} aggregation with the
     * given aggName.
     */
    @Override
    public BaseAggregationBuilder ipStringange(String field) {
        return null;
    }

    /**
     * Create a new {@link Terms} aggregation with the given aggName.
     */
    @Override
    public BaseAggregationBuilder terms(String field) {
        String aggName = field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName,ValueType.STRING);
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
    public BaseAggregationBuilder percentileStringanks(String aggName, double[] values) {

        return null;
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
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName, ValueType.STRING);
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
     * piple
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
