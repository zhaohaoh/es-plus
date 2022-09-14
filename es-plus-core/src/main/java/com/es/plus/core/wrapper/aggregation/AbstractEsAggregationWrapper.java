package com.es.plus.core.wrapper.aggregation;

import com.es.plus.core.tools.SFunction;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


@SuppressWarnings({"unchecked"})
public abstract class AbstractEsAggregationWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsAggregationWrapper<T, R, Children>> extends AbstractLambdaAggregationWrapper<T, R>
        implements IEsAggregationWrapper<Children, R> {
    protected AbstractEsAggregationWrapper() {
    }

    protected Children children = (Children) this;
    protected List<BaseAggregationBuilder> aggregationBuilder = new ArrayList<>();
    protected Object current;

    public List<BaseAggregationBuilder> getAggregationBuilder() {
        return aggregationBuilder;
    }

    @Override
    public ValueCountAggregationBuilder count(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ValueCountAggregationBuilder.NAME;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder(aggName);
        valueCountAggregationBuilder.field(field);
        return valueCountAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public AvgAggregationBuilder avg(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AvgAggregationBuilder.NAME;
        AvgAggregationBuilder avgAggregationBuilder = new AvgAggregationBuilder(aggName);
        avgAggregationBuilder.field(field);
        return avgAggregationBuilder;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public WeightedAvgAggregationBuilder weightedAvg(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + WeightedAvgAggregationBuilder.NAME;
        return new WeightedAvgAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    @Override
    public MaxAggregationBuilder max(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MaxAggregationBuilder.NAME;
        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder(aggName);
        maxAggregationBuilder.field(field);
        return maxAggregationBuilder;
    }

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    @Override
    public MinAggregationBuilder min(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MinAggregationBuilder.NAME;
        MinAggregationBuilder minAggregationBuilder = new MinAggregationBuilder(aggName);
        minAggregationBuilder.field(field);
        return minAggregationBuilder;
    }

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    @Override
    public SumAggregationBuilder sum(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SumAggregationBuilder.NAME;
        SumAggregationBuilder sumAggregationBuilder = new SumAggregationBuilder(aggName);
        sumAggregationBuilder.field(field);
        return sumAggregationBuilder;
    }

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    @Override
    public StatsAggregationBuilder stats(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + StatsAggregationBuilder.NAME;
        StatsAggregationBuilder statsAggregationBuilder = new StatsAggregationBuilder(aggName);
        statsAggregationBuilder.field(field);
        return statsAggregationBuilder;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    @Override
    public ExtendedStatsAggregationBuilder extendedStats(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ExtendedStatsAggregationBuilder.NAME;
        ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder = new ExtendedStatsAggregationBuilder(aggName);
        extendedStatsAggregationBuilder.field(field);
        return extendedStatsAggregationBuilder;
    }

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    @Override
    public FilterAggregationBuilder filter(R name, QueryBuilder filter) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FilterAggregationBuilder.NAME;
        return new FilterAggregationBuilder(aggName, filter);
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public FiltersAggregationBuilder filters(R name, FiltersAggregator.KeyedFilter... filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        return new FiltersAggregationBuilder(aggName, filters);
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public FiltersAggregationBuilder filters(R name, QueryBuilder... filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        return new FiltersAggregationBuilder(aggName, filters);
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    @Override
    public AdjacencyMatrixAggregationBuilder adjacencyMatrix(R name, Map<String, QueryBuilder> filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME;
        return new AdjacencyMatrixAggregationBuilder(aggName, filters);
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    @Override
    public AdjacencyMatrixAggregationBuilder adjacencyMatrix(R name, String separator, Map<String, QueryBuilder> filters) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME;
        return new AdjacencyMatrixAggregationBuilder(aggName, separator, filters);
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public SamplerAggregationBuilder sampler(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SamplerAggregationBuilder.NAME;
        return new SamplerAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public DiversifiedAggregationBuilder diversifiedSampler(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DiversifiedAggregationBuilder.NAME;
        DiversifiedAggregationBuilder diversifiedAggregationBuilder = new DiversifiedAggregationBuilder(aggName);
        diversifiedAggregationBuilder.field(field);
        return diversifiedAggregationBuilder;
    }

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    @Override
    public GlobalAggregationBuilder global(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GlobalAggregationBuilder.NAME;
        return new GlobalAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    @Override
    public MissingAggregationBuilder missing(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MissingAggregationBuilder.NAME;
        MissingAggregationBuilder missingAggregationBuilder = new MissingAggregationBuilder(aggName);
        missingAggregationBuilder.field(field);
        return missingAggregationBuilder;
    }

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    @Override
    public NestedAggregationBuilder nested(R name, String path) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + NestedAggregationBuilder.NAME;
        return new NestedAggregationBuilder(aggName, path);
    }

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    @Override
    public ReverseNestedAggregationBuilder reverseNested(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ReverseNestedAggregationBuilder.NAME;
        return new ReverseNestedAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    @Override
    public GeoDistanceAggregationBuilder geoDistance(R name, GeoPoint origin) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoDistanceAggregationBuilder.NAME;
        GeoDistanceAggregationBuilder geoDistanceAggregationBuilder = new GeoDistanceAggregationBuilder(aggName, origin);
        geoDistanceAggregationBuilder.field(field);
        return geoDistanceAggregationBuilder;
    }

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    @Override
    public HistogramAggregationBuilder histogram(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + HistogramAggregationBuilder.NAME;
        HistogramAggregationBuilder histogramAggregationBuilder = new HistogramAggregationBuilder(aggName);
        histogramAggregationBuilder.field(field);
        return histogramAggregationBuilder;
    }

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    @Override
    public GeoHashGridAggregationBuilder geohashGrid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoHashGridAggregationBuilder.NAME;
        GeoHashGridAggregationBuilder geoHashGridAggregationBuilder = new GeoHashGridAggregationBuilder(aggName);
        geoHashGridAggregationBuilder.field(field);
        return geoHashGridAggregationBuilder;
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    @Override
    public GeoTileGridAggregationBuilder geotileGrid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME;
        GeoTileGridAggregationBuilder geoTileGridAggregationBuilder = new GeoTileGridAggregationBuilder(aggName);
        geoTileGridAggregationBuilder.field(field);
        return geoTileGridAggregationBuilder;
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    @Override
    public SignificantTermsAggregationBuilder significantTerms(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME;
        SignificantTermsAggregationBuilder significantTermsAggregationBuilder = new SignificantTermsAggregationBuilder(aggName);
        significantTermsAggregationBuilder.field(field);
        return significantTermsAggregationBuilder;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    @Override
    public SignificantTextAggregationBuilder significantText(R name, String fieldName) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SignificantTextAggregationBuilder.NAME;
        SignificantTextAggregationBuilder significantTextAggregationBuilder = new SignificantTextAggregationBuilder(aggName, fieldName);
        significantTextAggregationBuilder.fieldName(aggName);
        return significantTextAggregationBuilder;
    }


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    @Override
    public DateHistogramAggregationBuilder dateHistogram(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DateHistogramAggregationBuilder.NAME;
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = new DateHistogramAggregationBuilder(aggName);
        dateHistogramAggregationBuilder.field(field);
        return dateHistogramAggregationBuilder;
    }

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    @Override
    public RangeAggregationBuilder range(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + RangeAggregationBuilder.NAME;
        RangeAggregationBuilder rangeAggregationBuilder = new RangeAggregationBuilder(aggName);
        rangeAggregationBuilder.field(field);
        return rangeAggregationBuilder;
    }

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public DateRangeAggregationBuilder dateRange(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DateRangeAggregationBuilder.NAME;
        DateRangeAggregationBuilder dateRangeAggregationBuilder = new DateRangeAggregationBuilder(aggName);
        dateRangeAggregationBuilder.field(field);
        return dateRangeAggregationBuilder;
    }

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public IpRangeAggregationBuilder ipRange(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
        IpRangeAggregationBuilder ipRangeAggregationBuilder = new IpRangeAggregationBuilder(aggName);
        ipRangeAggregationBuilder.field(field);
        return ipRangeAggregationBuilder;
    }

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    @Override
    public TermsAggregationBuilder terms(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + TermsAggregationBuilder.NAME;
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName);
        termsAggregationBuilder.field(field);
        return termsAggregationBuilder;
    }

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    @Override
    public PercentilesAggregationBuilder percentiles(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentilesAggregationBuilder.NAME;
        PercentilesAggregationBuilder percentilesAggregationBuilder = new PercentilesAggregationBuilder(aggName);
        percentilesAggregationBuilder.field(field);
        return percentilesAggregationBuilder;
    }

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    @Override
    public PercentileRanksAggregationBuilder percentileRanks(R name, double[] values) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentileRanksAggregationBuilder.NAME;
        PercentileRanksAggregationBuilder percentileRanksAggregationBuilder = new PercentileRanksAggregationBuilder(aggName, values);
        percentileRanksAggregationBuilder.field(field);
        return percentileRanksAggregationBuilder;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    @Override
    public MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviation(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MedianAbsoluteDeviationAggregationBuilder.NAME;
        MedianAbsoluteDeviationAggregationBuilder medianAbsoluteDeviationAggregationBuilder = new MedianAbsoluteDeviationAggregationBuilder(aggName);
        medianAbsoluteDeviationAggregationBuilder.field(field);
        return medianAbsoluteDeviationAggregationBuilder;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    @Override
    public CardinalityAggregationBuilder cardinality(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CardinalityAggregationBuilder.NAME;
        CardinalityAggregationBuilder cardinalityAggregationBuilder = new CardinalityAggregationBuilder(aggName);
        cardinalityAggregationBuilder.field(field);
        return cardinalityAggregationBuilder;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    @Override
    public TopHitsAggregationBuilder topHits(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + TopHitsAggregationBuilder.NAME;
        return new TopHitsAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    @Override
    public GeoBoundsAggregationBuilder geoBounds(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoBoundsAggregationBuilder.NAME;
        GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = new GeoBoundsAggregationBuilder(aggName);
        geoBoundsAggregationBuilder.field(field);
        return geoBoundsAggregationBuilder;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    @Override
    public GeoCentroidAggregationBuilder geoCentroid(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + GeoCentroidAggregationBuilder.NAME;
        GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = new GeoCentroidAggregationBuilder(aggName);
        geoCentroidAggregationBuilder.field(field);
        return geoCentroidAggregationBuilder;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    @Override
    public ScriptedMetricAggregationBuilder scriptedMetric(R name) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ScriptedMetricAggregationBuilder.NAME;
        return new ScriptedMetricAggregationBuilder(aggName);
    }

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    @Override
    public CompositeAggregationBuilder composite(R name, List<CompositeValuesSourceBuilder<?>> sources) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CompositeAggregationBuilder.NAME;
        return new CompositeAggregationBuilder(aggName, sources);
    }

    /**
     * piple
     */
    @Override
    public DerivativePipelineAggregationBuilder derivative(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + DerivativePipelineAggregationBuilder.NAME;
        return new DerivativePipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public MaxBucketPipelineAggregationBuilder maxBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MaxBucketPipelineAggregationBuilder.NAME;
        return new MaxBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public MinBucketPipelineAggregationBuilder minBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MinBucketPipelineAggregationBuilder.NAME;
        return new MinBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public final AvgBucketPipelineAggregationBuilder avgBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AvgBucketPipelineAggregationBuilder.NAME;
        return new AvgBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public SumBucketPipelineAggregationBuilder sumBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SumBucketPipelineAggregationBuilder.NAME;
        return new SumBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public StatsBucketPipelineAggregationBuilder statsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + StatsBucketPipelineAggregationBuilder.NAME;
        return new StatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public ExtendedStatsBucketPipelineAggregationBuilder extendedStatsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + ExtendedStatsBucketPipelineAggregationBuilder.NAME;
        return new ExtendedStatsBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public PercentilesBucketPipelineAggregationBuilder percentilesBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + PercentilesBucketPipelineAggregationBuilder.NAME;
        return new PercentilesBucketPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public BucketScriptPipelineAggregationBuilder bucketScript(R name, Map<String, String> bucketsPathsMap, Script script) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        return new BucketScriptPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
    }

    @Override
    public BucketScriptPipelineAggregationBuilder bucketScript(R name, Script script, String... bucketsPaths) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        return new BucketScriptPipelineAggregationBuilder(aggName, script, bucketsPaths);
    }

    @Override
    public BucketSelectorPipelineAggregationBuilder bucketSelector(R name, Map<String, String> bucketsPathsMap, Script script) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        return new BucketSelectorPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
    }

    @Override
    public BucketSelectorPipelineAggregationBuilder bucketSelector(R name, Script script, String... bucketsPaths) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        return new BucketSelectorPipelineAggregationBuilder(aggName, script, bucketsPaths);
    }

    @Override
    public BucketSortPipelineAggregationBuilder bucketSort(R name, List<FieldSortBuilder> sorts) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSortPipelineAggregationBuilder.NAME;
        return new BucketSortPipelineAggregationBuilder(aggName, sorts);
    }

    @Override
    public CumulativeSumPipelineAggregationBuilder cumulativeSum(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + CumulativeSumPipelineAggregationBuilder.NAME;
        return new CumulativeSumPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public SerialDiffPipelineAggregationBuilder diff(R name, String bucketsPath) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + SerialDiffPipelineAggregationBuilder.NAME;
        return new SerialDiffPipelineAggregationBuilder(aggName, bucketsPath);
    }

    @Override
    public MovFnPipelineAggregationBuilder movingFunction(R name, Script script, String bucketsPaths, int window) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + MovFnPipelineAggregationBuilder.NAME;
        return new MovFnPipelineAggregationBuilder(aggName, bucketsPaths, script, window);
    }

    @Override
    public Children add(BaseAggregationBuilder agg) {
        aggregationBuilder.add(agg);
        return children;
    }

}
