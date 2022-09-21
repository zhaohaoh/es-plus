package com.es.plus.core.wrapper.aggregation;

import com.es.plus.config.GlobalConfigCache;
import com.es.plus.core.tools.SFunction;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.*;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
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
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.*;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.*;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;

/**
 * @Author: hzh
 * @Date: 2022/9/14 18:06
 * 抽象聚合封装
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsAggregationWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsAggregationWrapper<T, R, Children>> extends AbstractLambdaAggregationWrapper<T, R>
        implements IEsAggregationWrapper<Children, R> {
    protected AbstractEsAggregationWrapper() {
    }

    protected abstract Children instance();

    protected Children children = (Children) this;
    protected List<BaseAggregationBuilder> aggregationBuilder = new ArrayList<>();
    protected BaseAggregationBuilder currentBuilder;

    public List<BaseAggregationBuilder> getAggregationBuilder() {
        return aggregationBuilder;
    }

    @Override
    public Children subAggregation(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        List<BaseAggregationBuilder> aggregationBuilder = children.getAggregationBuilder();
        if (!CollectionUtils.isEmpty(aggregationBuilder)) {
            AggregatorFactories.Builder builder = new AggregatorFactories.Builder();
            for (BaseAggregationBuilder baseAggregationBuilder : aggregationBuilder) {
                if (baseAggregationBuilder instanceof AggregationBuilder) {
                    builder.addAggregator((AggregationBuilder) baseAggregationBuilder);
                } else {
                    builder.addPipelineAggregator((PipelineAggregationBuilder) baseAggregationBuilder);
                }
            }
            currentBuilder.subAggregations(builder);
        }
        return this.children;
    }

    @Override
    public Children count(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName);
        valueCountAggregationBuilder.field(field);
        currentBuilder = valueCountAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children avg(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
        AvgAggregationBuilder avgAggregationBuilder = new AvgAggregationBuilder(aggName);
        avgAggregationBuilder.field(field);
        currentBuilder = avgAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children weightedAvg(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        WeightedAvgAggregationBuilder weightedAvgAggregationBuilder = new WeightedAvgAggregationBuilder(aggName);
        currentBuilder = weightedAvgAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    @Override
    public Children max(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder(aggName);
        maxAggregationBuilder.field(field);
        currentBuilder = maxAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    @Override
    public Children min(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MinAggregationBuilder.NAME;
        MinAggregationBuilder minAggregationBuilder = new MinAggregationBuilder(aggName);
        minAggregationBuilder.field(field);
        currentBuilder = minAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    @Override
    public Children sum(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SumAggregationBuilder.NAME;
        SumAggregationBuilder sumAggregationBuilder = new SumAggregationBuilder(aggName);
        sumAggregationBuilder.field(field);
        currentBuilder = sumAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    @Override
    public Children stats(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
        StatsAggregationBuilder statsAggregationBuilder = new StatsAggregationBuilder(aggName);
        statsAggregationBuilder.field(field);
        currentBuilder = statsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    @Override
    public Children extendedStats(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ExtendedStatsAggregationBuilder.NAME;
        ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder = new ExtendedStatsAggregationBuilder(aggName);
        extendedStatsAggregationBuilder.field(field);
        currentBuilder = extendedStatsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    @Override
    public Children filter(R name, QueryBuilder filter) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FilterAggregationBuilder.NAME;
        FilterAggregationBuilder filterAggregationBuilder = new FilterAggregationBuilder(aggName, filter);
        currentBuilder = filterAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public Children filters(R name, FiltersAggregator.KeyedFilter... filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        FiltersAggregationBuilder filtersAggregationBuilder = new FiltersAggregationBuilder(aggName, filters);
        currentBuilder = filtersAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public Children filters(R name, QueryBuilder... filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        FiltersAggregationBuilder filtersAggregationBuilder = new FiltersAggregationBuilder(aggName, filters);
        currentBuilder = filtersAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    @Override
    public Children adjacencyMatrix(R name, Map<String, QueryBuilder> filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME;
        AdjacencyMatrixAggregationBuilder adjacencyMatrixAggregationBuilder = new AdjacencyMatrixAggregationBuilder(aggName, filters);
        currentBuilder = adjacencyMatrixAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    @Override
    public Children adjacencyMatrix(R name, String separator, Map<String, QueryBuilder> filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME;
        AdjacencyMatrixAggregationBuilder adjacencyMatrixAggregationBuilder = new AdjacencyMatrixAggregationBuilder(aggName, separator, filters);
        currentBuilder = adjacencyMatrixAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children sampler(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
        SamplerAggregationBuilder samplerAggregationBuilder = new SamplerAggregationBuilder(aggName);
        currentBuilder = samplerAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children diversifiedSampler(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DiversifiedAggregationBuilder.NAME;
        DiversifiedAggregationBuilder diversifiedAggregationBuilder = new DiversifiedAggregationBuilder(aggName);
        diversifiedAggregationBuilder.field(field);
        currentBuilder = diversifiedAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    @Override
    public Children global(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GlobalAggregationBuilder.NAME;
        GlobalAggregationBuilder globalAggregationBuilder = new GlobalAggregationBuilder(aggName);
        currentBuilder = globalAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    @Override
    public Children missing(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName);
        missingAggregationBuilder.field(field);
        currentBuilder = missingAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    @Override
    public Children nested(R name, String path) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + NestedAggregationBuilder.NAME;
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder(aggName, path);
        currentBuilder = nestedAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    @Override
    public Children reverseNested(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ReverseNestedAggregationBuilder.NAME;
        ReverseNestedAggregationBuilder reverseNestedAggregationBuilder = new ReverseNestedAggregationBuilder(aggName);
        currentBuilder = reverseNestedAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    @Override
    public Children geoDistance(R name, GeoPoint origin) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoDistanceAggregationBuilder.NAME;
        GeoDistanceAggregationBuilder geoDistanceAggregationBuilder = new GeoDistanceAggregationBuilder(aggName, origin);
        geoDistanceAggregationBuilder.field(field);
        currentBuilder = geoDistanceAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    @Override
    public Children histogram(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + HistogramAggregationBuilder.NAME;
        HistogramAggregationBuilder histogramAggregationBuilder = new HistogramAggregationBuilder(aggName);
        histogramAggregationBuilder.field(field);
        currentBuilder = histogramAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    @Override
    public Children geohashGrid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoHashGridAggregationBuilder.NAME;
        GeoHashGridAggregationBuilder geoHashGridAggregationBuilder = new GeoHashGridAggregationBuilder(aggName);
        geoHashGridAggregationBuilder.field(field);
        currentBuilder = geoHashGridAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    @Override
    public Children geotileGrid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME;
        GeoTileGridAggregationBuilder geoTileGridAggregationBuilder = new GeoTileGridAggregationBuilder(aggName);
        geoTileGridAggregationBuilder.field(field);
        currentBuilder = geoTileGridAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    @Override
    public Children significantTerms(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName);
        significantTermsAggregationBuilder.field(field);
        currentBuilder = significantTermsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    @Override
    public Children significantText(R name, String fieldName) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SignificantTextAggregationBuilder.NAME;
        SignificantTextAggregationBuilder significantTextAggregationBuilder = new SignificantTextAggregationBuilder(aggName, fieldName);
        significantTextAggregationBuilder.fieldName(aggName);
        currentBuilder = significantTextAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    @Override
    public Children dateHistogram(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DateHistogramAggregationBuilder.NAME;
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = new DateHistogramAggregationBuilder(aggName);
        dateHistogramAggregationBuilder.field(field);
        currentBuilder = dateHistogramAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    @Override
    public Children range(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + RangeAggregationBuilder.NAME;
        RangeAggregationBuilder rangeAggregationBuilder = new RangeAggregationBuilder(aggName);
        rangeAggregationBuilder.field(field);
        currentBuilder = rangeAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public Children dateRange(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DateRangeAggregationBuilder.NAME;
        DateRangeAggregationBuilder dateRangeAggregationBuilder = new DateRangeAggregationBuilder(aggName);
        dateRangeAggregationBuilder.field(field);
        currentBuilder = dateRangeAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public Children ipRange(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
        IpRangeAggregationBuilder ipRangeAggregationBuilder = new IpRangeAggregationBuilder(aggName);
        ipRangeAggregationBuilder.field(field);
        currentBuilder = ipRangeAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    @Override
    public Children terms(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName);
        termsAggregationBuilder.field(field);
        termsAggregationBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getSearchSize());
        currentBuilder = termsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    @Override
    public Children percentiles(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
        PercentilesAggregationBuilder percentilesAggregationBuilder = new PercentilesAggregationBuilder(aggName);
        percentilesAggregationBuilder.field(field);
        currentBuilder = percentilesAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    @Override
    public Children percentileRanks(R name, double[] values) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
        PercentileRanksAggregationBuilder percentileRanksAggregationBuilder = new PercentileRanksAggregationBuilder(aggName, values);
        percentileRanksAggregationBuilder.field(field);
        currentBuilder = percentileRanksAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    @Override
    public Children medianAbsoluteDeviation(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
        MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAggregationBuilder = new MedianAbsoluteDeviationAggregationBuilder(aggName);
        medianAbsoluteDeviationAggregationBuilder.field(field);
        currentBuilder = medianAbsoluteDeviationAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    @Override
    public Children cardinality(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName);
        cardinalityAggregationBuilder.field(field);
        currentBuilder = cardinalityAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    @Override
    public Children topHits(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
        TopHitsAggregationBuilder topHitsAggregationBuilder = new TopHitsAggregationBuilder(aggName);
        currentBuilder = topHitsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    @Override
    public Children geoBounds(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
        GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = new GeoBoundsAggregationBuilder(aggName);
        geoBoundsAggregationBuilder.field(field);
        currentBuilder = geoBoundsAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    @Override
    public Children geoCentroid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
        GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = new GeoCentroidAggregationBuilder(aggName);
        geoCentroidAggregationBuilder.field(field);
        currentBuilder = geoCentroidAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    @Override
    public Children scriptedMetric(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
        ScriptedMetricAggregationBuilder scriptedMetricAggregationBuilder = new ScriptedMetricAggregationBuilder(aggName);
        currentBuilder = scriptedMetricAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    @Override
    public Children composite(R name, List<CompositeValuesSourceBuilder<?>> sources) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CompositeAggregationBuilder.NAME;
        CompositeAggregationBuilder compositeAggregationBuilder = new CompositeAggregationBuilder(aggName, sources);
        currentBuilder = compositeAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * piple
     */
    @Override
    public Children derivative(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DerivativePipelineAggregationBuilder.NAME;
        DerivativePipelineAggregationBuilder derivativePipelineAggregationBuilder = new DerivativePipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = derivativePipelineAggregationBuilder;
        aggregationBuilder.add(derivativePipelineAggregationBuilder);
        return this.children;
    }

    @Override
    public Children maxBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MaxBucketPipelineAggregationBuilder.NAME;
        MaxBucketPipelineAggregationBuilder maxBucketPipelineAggregationBuilder = new MaxBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = maxBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children minBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MinBucketPipelineAggregationBuilder.NAME;
        MinBucketPipelineAggregationBuilder minBucketPipelineAggregationBuilder = new MinBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = minBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public final Children avgBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AvgBucketPipelineAggregationBuilder.NAME;
        AvgBucketPipelineAggregationBuilder avgBucketPipelineAggregationBuilder = new AvgBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = avgBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children sumBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SumBucketPipelineAggregationBuilder.NAME;
        SumBucketPipelineAggregationBuilder sumBucketPipelineAggregationBuilder = new SumBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = sumBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children statsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + StatsBucketPipelineAggregationBuilder.NAME;
        StatsBucketPipelineAggregationBuilder statsBucketPipelineAggregationBuilder = new StatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = statsBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children extendedStatsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ExtendedStatsBucketPipelineAggregationBuilder.NAME;
        ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucketPipelineAggregationBuilder = new ExtendedStatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = extendedStatsBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children percentilesBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentilesBucketPipelineAggregationBuilder.NAME;
        PercentilesBucketPipelineAggregationBuilder percentilesBucketPipelineAggregationBuilder = new PercentilesBucketPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = percentilesBucketPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketScript(R name, Map<String, String> bucketsPathsMap, Script script) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        BucketScriptPipelineAggregationBuilder bucketScriptPipelineAggregationBuilder = new BucketScriptPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
        currentBuilder = bucketScriptPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketScript(R name, Script script, String... bucketsPaths) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        BucketScriptPipelineAggregationBuilder bucketScriptPipelineAggregationBuilder = new BucketScriptPipelineAggregationBuilder(aggName, script, bucketsPaths);
        currentBuilder = bucketScriptPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSelector(R name, Map<String, String> bucketsPathsMap, Script script) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        BucketSelectorPipelineAggregationBuilder bucketSelectorPipelineAggregationBuilder = new BucketSelectorPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
        currentBuilder = bucketSelectorPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSelector(R name, Script script, String... bucketsPaths) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        BucketSelectorPipelineAggregationBuilder bucketSelectorPipelineAggregationBuilder = new BucketSelectorPipelineAggregationBuilder(aggName, script, bucketsPaths);
        currentBuilder = bucketSelectorPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSort(R name, List<FieldSortBuilder> sorts) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSortPipelineAggregationBuilder.NAME;
        BucketSortPipelineAggregationBuilder bucketSortPipelineAggregationBuilder = new BucketSortPipelineAggregationBuilder(aggName, sorts);
        currentBuilder = bucketSortPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children cumulativeSum(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CumulativeSumPipelineAggregationBuilder.NAME;
        CumulativeSumPipelineAggregationBuilder cumulativeSumPipelineAggregationBuilder = new CumulativeSumPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = cumulativeSumPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children diff(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SerialDiffPipelineAggregationBuilder.NAME;
        SerialDiffPipelineAggregationBuilder serialDiffPipelineAggregationBuilder = new SerialDiffPipelineAggregationBuilder(aggName, bucketsPath);
        currentBuilder = serialDiffPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children movingFunction(R name, Script script, String bucketsPaths, int window) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MovFnPipelineAggregationBuilder.NAME;
        MovFnPipelineAggregationBuilder movFnPipelineAggregationBuilder = new MovFnPipelineAggregationBuilder(aggName, bucketsPaths, script, window);
        currentBuilder = movFnPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

}
