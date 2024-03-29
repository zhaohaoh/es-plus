package com.es.plus.core.wrapper.aggregation;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.core.EsAggClient;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import com.es.plus.es6.client.EsPlus6AggsClient;
import com.es.plus.es7.client.EsPlusAggsClient;
import lombok.SneakyThrows;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.*;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;

/**
 * @Author: hzh
 * @Date: 2022/9/14 18:06
 * 抽象聚合封装
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsAggWrapper<T, R, Children extends AbstractEsAggWrapper<T, R, Children>> extends AbstractLambdaAggWrapper<T, R>
        implements IEsAggWrapper<Children, R, T>, IEsAggFuncWrapper<Children, R, T> {
    protected AbstractEsAggWrapper() {
        if (GlobalConfigCache.GLOBAL_CONFIG.getVersion().equals(6)) {
            esAggClient = new EsPlus6AggsClient();
        } else {
            esAggClient = new EsPlusAggsClient();
        }
    }

    protected abstract Children instance();

    protected Children children = (Children) this;
    protected List<BaseAggregationBuilder> aggregationBuilder = new ArrayList<>();
    protected BaseAggregationBuilder currentBuilder;
    protected EsAggClient esAggClient;
    protected AggregatorFactories.Builder subAggregations;
    
    public List<BaseAggregationBuilder> getAggregationBuilder() {
        return aggregationBuilder;
    }

    @Override
    public Children subAggregation(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        List<BaseAggregationBuilder> aggregationBuilder = children.getAggregationBuilder();
        if (!CollectionUtils.isEmpty(aggregationBuilder)) {
            if (subAggregations == null){
                subAggregations = new AggregatorFactories.Builder();
                //当前的聚合数据加入
                if (currentBuilder instanceof AggregationBuilder){
                    AggregationBuilder aggregations =   (AggregationBuilder) currentBuilder;
                    Collection<AggregationBuilder> subAggregations = aggregations.getSubAggregations();
                    for (AggregationBuilder subAggregation : subAggregations) {
                        this.subAggregations.addAggregator(subAggregation);
                    }
                }
            }
            
            for (BaseAggregationBuilder baseAggregationBuilder : aggregationBuilder) {
                if (baseAggregationBuilder instanceof AggregationBuilder) {
                    subAggregations.addAggregator((AggregationBuilder) baseAggregationBuilder);
                } else {
                    subAggregations.addPipelineAggregator((PipelineAggregationBuilder) baseAggregationBuilder);
                }
            }
            
            currentBuilder.subAggregations(subAggregations);
        }
        return this.children;
    }

    @Override
    public Children add(BaseAggregationBuilder baseAggregationBuilder) {
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(baseAggregationBuilder);
        return this.children;
    }

    @Override
    public Children count(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.count(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children avg(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.avg(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children weightedAvg(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.weightedAvg(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    @Override
    public Children max(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.max(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    @Override
    public Children min(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.min(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    @Override
    public Children sum(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sum(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    @Override
    public Children stats(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.stats(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    @Override
    public Children extendedStats(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.extendedStats(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    @Override
    public Children filter(R name, Supplier<EsWrapper<T>> supplier) {
        String field = getAggregationField(name);
        BaseAggregationBuilder filter = esAggClient.filter(field, supplier.get().esParamWrapper());
        currentBuilder = filter;
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
    public Children filters(R name, Supplier<EsWrapper<T>>... supplier) {
        String field = getAggregationField(name);
        EsParamWrapper<T>[] esParamWrappers = Arrays.stream(supplier).map(a -> a.get().esParamWrapper()).toArray(EsParamWrapper[]::new);
        currentBuilder = esAggClient.filters(field, esParamWrappers);
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name.
     */
    @Override
    public Children adjacencyMatrix(R name, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap) {
        String field = getAggregationField(name);
        Map<String, EsParamWrapper<?>> esParamWrapperMap = new HashMap<>();
        adjacencyMatrixMap.forEach((k, v) -> esParamWrapperMap.put(k, v.get().esParamWrapper()));
        BaseAggregationBuilder adjacencyMatrix = esAggClient.adjacencyMatrix(field, esParamWrapperMap);
        currentBuilder = adjacencyMatrix;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    @Override
    public Children adjacencyMatrix(R name, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap) {
        String field = getAggregationField(name);
        Map<String, EsParamWrapper<?>> esParamWrapperMap = new HashMap<>();
        adjacencyMatrixMap.forEach((k, v) -> esParamWrapperMap.put(k, v.get().esParamWrapper()));
        BaseAggregationBuilder adjacencyMatrix = esAggClient.adjacencyMatrix(field, separator, esParamWrapperMap);
        currentBuilder = adjacencyMatrix;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children sampler(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sampler(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children diversifiedSampler(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.diversifiedSampler(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    @Override
    public Children global(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.global(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    @Override
    public Children missing(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.missing(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    @Override
    public Children nested(R name, String path) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.nested(field, path);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     */
    @SneakyThrows
    @Override
    public Children reverseNested(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.reverseNested(field);
        currentBuilder = baseAggregationBuilder;
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
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.histogram(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    @SneakyThrows
    @Override
    public Children geohashGrid(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geohashGrid(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    @Override
    @SneakyThrows
    public Children geotileGrid(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geohashGrid(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    @Override
    public Children significantTerms(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.significantTerms(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    @Override
    public Children significantText(R name, String fieldName) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.significantText(field, fieldName);
        currentBuilder = baseAggregationBuilder;
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
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.dateHistogram(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    @Override
    public Children range(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.range(field);
        currentBuilder = baseAggregationBuilder;
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
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.range(field);
        currentBuilder = baseAggregationBuilder;
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
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.terms(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    @Override
    public Children percentiles(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentiles(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    @Override
    public Children percentileRanks(R name, double[] values) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentiles(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    @Override
    public Children medianAbsoluteDeviation(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.medianAbsoluteDeviation(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    @Override
    public Children cardinality(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.cardinality(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    @Override
    public Children topHits(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.topHits(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    @Override
    public Children geoBounds(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geoBounds(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    @Override
    public Children geoCentroid(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geoCentroid(field);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    @Override
    public Children scriptedMetric(R name) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.scriptedMetric(field);
        currentBuilder = baseAggregationBuilder;
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
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.derivative(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(baseAggregationBuilder);
        return this.children;
    }

    @Override
    public Children maxBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.maxBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children minBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.minBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public final Children avgBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.avgBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children sumBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sumBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children statsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.statsBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children extendedStatsBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.extendedStatsBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children percentilesBucket(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentilesBucket(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
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
    public Children bucketSort(R name, int from, int size, boolean asc, String... orderColumns) {
        String field = getAggregationField(name);
        String aggName = field + AGG_DELIMITER + BucketSortPipelineAggregationBuilder.NAME;
        List<FieldSortBuilder> sorts = Arrays.stream(orderColumns).map(o -> {
            FieldSortBuilder sortBuilder = new FieldSortBuilder(o);
            sortBuilder.order(asc ? SortOrder.ASC : SortOrder.DESC);
            return sortBuilder;
        }).collect(Collectors.toList());

        BucketSortPipelineAggregationBuilder bucketSortPipelineAggregationBuilder = new BucketSortPipelineAggregationBuilder(aggName, sorts);
        bucketSortPipelineAggregationBuilder.from(from);
        bucketSortPipelineAggregationBuilder.size(size);
        currentBuilder = bucketSortPipelineAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children cumulativeSum(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.cumulativeSum(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children diff(R name, String bucketsPath) {
        String field = getAggregationField(name);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.diff(field, bucketsPath);
        currentBuilder = baseAggregationBuilder;
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

//    ------------------------------------------------------Function  es版本6不支持


    @Override
    public Children count(R name, Function<ValueCountAggregationBuilder, ValueCountAggregationBuilder> fn) {
        count(name);
        fn.apply((ValueCountAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children avg(R name, Function<AvgAggregationBuilder, AvgAggregationBuilder> fn) {
        avg(name);
        fn.apply((AvgAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children weightedAvg(R name, Function<WeightedAvgAggregationBuilder, WeightedAvgAggregationBuilder> fn) {
        weightedAvg(name);
        fn.apply((WeightedAvgAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children max(R name, Function<MaxAggregationBuilder, MaxAggregationBuilder> fn) {
        max(name);
        fn.apply((MaxAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children min(R name, Function<MinAggregationBuilder, MinAggregationBuilder> fn) {
        min(name);
        fn.apply((MinAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children sum(R name, Function<SumAggregationBuilder, SumAggregationBuilder> fn) {
        sum(name);
        fn.apply((SumAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children stats(R name, Function<StatsAggregationBuilder, StatsAggregationBuilder> fn) {
        stats(name);
        fn.apply((StatsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children extendedStats(R name, Function<ExtendedStatsAggregationBuilder, ExtendedStatsAggregationBuilder> fn) {
        extendedStats(name);
        fn.apply((ExtendedStatsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children filter(R name, Supplier<EsWrapper<T>> filter, Function<FilterAggregationBuilder, FilterAggregationBuilder> fn) {
        filter(name, filter);
        fn.apply((FilterAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children adjacencyMatrix(R name, Map<String, Supplier<EsWrapper<T>>> filters, Function<AdjacencyMatrixAggregationBuilder, AdjacencyMatrixAggregationBuilder> fn) {
        adjacencyMatrix(name, filters);
        fn.apply((AdjacencyMatrixAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children adjacencyMatrix(R name, String separator, Map<String, Supplier<EsWrapper<T>>> filters, Function<AdjacencyMatrixAggregationBuilder, AdjacencyMatrixAggregationBuilder> fn) {
        adjacencyMatrix(name, separator, filters);
        fn.apply((AdjacencyMatrixAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children sampler(R name, Function<SamplerAggregationBuilder, SamplerAggregationBuilder> fn) {
        sampler(name);
        fn.apply((SamplerAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children diversifiedSampler(R name, Function<DiversifiedAggregationBuilder, DiversifiedAggregationBuilder> fn) {
        diversifiedSampler(name);
        fn.apply((DiversifiedAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children global(R name, Function<GlobalAggregationBuilder, GlobalAggregationBuilder> fn) {
        global(name);
        fn.apply((GlobalAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children missing(R name, Function<MissingAggregationBuilder, MissingAggregationBuilder> fn) {
        missing(name);
        fn.apply((MissingAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children nested(R name, String path, Function<NestedAggregationBuilder, NestedAggregationBuilder> fn) {
        nested(name, path);
        fn.apply((NestedAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children reverseNested(R name, Function<ReverseNestedAggregationBuilder, ReverseNestedAggregationBuilder> fn) {
        reverseNested(name);
        fn.apply((ReverseNestedAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children geoDistance(R name, GeoPoint origin, Function<GeoDistanceAggregationBuilder, GeoDistanceAggregationBuilder> fn) {
        geoDistance(name, origin);
        fn.apply((GeoDistanceAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children histogram(R name, Function<HistogramAggregationBuilder, HistogramAggregationBuilder> fn) {
        histogram(name);
        fn.apply((HistogramAggregationBuilder) currentBuilder);
        return children;
    }


    @Override
    public Children significantTerms(R name, Function<SignificantTermsAggregationBuilder, SignificantTermsAggregationBuilder> fn) {
        significantTerms(name);
        fn.apply((SignificantTermsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children significantText(R name, String fieldName, Function<SignificantTextAggregationBuilder, SignificantTextAggregationBuilder> fn) {
        significantText(name, fieldName);
        fn.apply((SignificantTextAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children dateHistogram(R name, Function<DateHistogramAggregationBuilder, DateHistogramAggregationBuilder> fn) {
        dateHistogram(name);
        fn.apply((DateHistogramAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children range(R name, Function<RangeAggregationBuilder, RangeAggregationBuilder> fn) {
        range(name);
        fn.apply((RangeAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children dateRange(R name, Function<DateRangeAggregationBuilder, DateRangeAggregationBuilder> fn) {
        dateRange(name);
        fn.apply((DateRangeAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children ipRange(R name, Function<IpRangeAggregationBuilder, IpRangeAggregationBuilder> fn) {
        ipRange(name);
        fn.apply((IpRangeAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children terms(R name, Function<TermsAggregationBuilder, TermsAggregationBuilder> fn) {
        terms(name);
        fn.apply((TermsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children percentiles(R name, Function<PercentilesAggregationBuilder, PercentilesAggregationBuilder> fn) {
        percentiles(name);
        fn.apply((PercentilesAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children percentileRanks(R name, double[] values, Function<PercentileRanksAggregationBuilder, PercentileRanksAggregationBuilder> fn) {
        percentileRanks(name, values);
        fn.apply((PercentileRanksAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children medianAbsoluteDeviation(R name, Function<MedianAbsoluteDeviationAggregationBuilder, MedianAbsoluteDeviationAggregationBuilder> fn) {
        medianAbsoluteDeviation(name);
        fn.apply((MedianAbsoluteDeviationAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children cardinality(R name, Function<CardinalityAggregationBuilder, CardinalityAggregationBuilder> fn) {
        cardinality(name);
        fn.apply((CardinalityAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children topHits(R name, Function<TopHitsAggregationBuilder, TopHitsAggregationBuilder> fn) {
        topHits(name);
        fn.apply((TopHitsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children geoBounds(R name, Function<GeoBoundsAggregationBuilder, GeoBoundsAggregationBuilder> fn) {
        geoBounds(name);
        fn.apply((GeoBoundsAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children geoCentroid(R name, Function<GeoCentroidAggregationBuilder, GeoCentroidAggregationBuilder> fn) {
        geoCentroid(name);
        fn.apply((GeoCentroidAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children scriptedMetric(R name, Function<ScriptedMetricAggregationBuilder, ScriptedMetricAggregationBuilder> fn) {
        scriptedMetric(name);
        fn.apply((ScriptedMetricAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children composite(R name, List<CompositeValuesSourceBuilder<?>> sources, Function<CompositeAggregationBuilder, CompositeAggregationBuilder> fn) {
        composite(name, sources);
        fn.apply((CompositeAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children derivative(R name, Function<DerivativePipelineAggregationBuilder, DerivativePipelineAggregationBuilder> fn, String bucketsPath) {
        derivative(name, bucketsPath);
        fn.apply((DerivativePipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children maxBucket(R name, Function<MaxBucketPipelineAggregationBuilder, MaxBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        maxBucket(name, bucketsPath);
        fn.apply((MaxBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children minBucket(R name, Function<MinBucketPipelineAggregationBuilder, MinBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        minBucket(name, bucketsPath);
        fn.apply((MinBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children avgBucket(R name, Function<AvgBucketPipelineAggregationBuilder, AvgBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        avgBucket(name, bucketsPath);
        fn.apply((AvgBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children sumBucket(R name, Function<SumBucketPipelineAggregationBuilder, SumBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        sumBucket(name, bucketsPath);
        fn.apply((SumBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children statsBucket(R name, Function<StatsBucketPipelineAggregationBuilder, StatsBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        statsBucket(name, bucketsPath);
        fn.apply((StatsBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children extendedStatsBucket(R name, Function<ExtendedStatsBucketPipelineAggregationBuilder, ExtendedStatsBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        extendedStatsBucket(name, bucketsPath);
        fn.apply((ExtendedStatsBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children percentilesBucket(R name, Function<PercentilesBucketPipelineAggregationBuilder, PercentilesBucketPipelineAggregationBuilder> fn, String bucketsPath) {
        percentilesBucket(name, bucketsPath);
        fn.apply((PercentilesBucketPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children bucketScript(R name, Function<BucketScriptPipelineAggregationBuilder, BucketScriptPipelineAggregationBuilder> fn, Map<String, String> bucketsPathsMap, Script script) {
        bucketScript(name, bucketsPathsMap, script);
        fn.apply((BucketScriptPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children bucketScript(R name, Function<BucketScriptPipelineAggregationBuilder, BucketScriptPipelineAggregationBuilder> fn, Script script, String... bucketsPaths) {
        bucketScript(name, script, bucketsPaths);
        fn.apply((BucketScriptPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children bucketSelector(R name, Function<BucketSelectorPipelineAggregationBuilder, BucketSelectorPipelineAggregationBuilder> fn, Map<String, String> bucketsPathsMap, Script script) {
        bucketSelector(name, bucketsPathsMap, script);
        fn.apply((BucketSelectorPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children bucketSelector(R name, Function<BucketSelectorPipelineAggregationBuilder, BucketSelectorPipelineAggregationBuilder> fn, Script script, String... bucketsPaths) {
        bucketSelector(name, script, bucketsPaths);
        fn.apply((BucketSelectorPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children bucketSort(R name, Function<BucketSortPipelineAggregationBuilder, BucketSortPipelineAggregationBuilder> fn, List<FieldSortBuilder> sorts) {
        bucketSort(name, sorts);
        fn.apply((BucketSortPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children cumulativeSum(R name, Function<CumulativeSumPipelineAggregationBuilder, CumulativeSumPipelineAggregationBuilder> fn, String bucketsPath) {
        cumulativeSum(name, bucketsPath);
        fn.apply((CumulativeSumPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children diff(R name, Function<SerialDiffPipelineAggregationBuilder, SerialDiffPipelineAggregationBuilder> fn, String bucketsPath) {
        diff(name, bucketsPath);
        fn.apply((SerialDiffPipelineAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children movingFunction(R name, Function<MovFnPipelineAggregationBuilder, MovFnPipelineAggregationBuilder> fn, Script script, String bucketsPaths, int window) {
        movingFunction(name, script, bucketsPaths, window);
        fn.apply((MovFnPipelineAggregationBuilder) currentBuilder);
        return children;
    }
}
