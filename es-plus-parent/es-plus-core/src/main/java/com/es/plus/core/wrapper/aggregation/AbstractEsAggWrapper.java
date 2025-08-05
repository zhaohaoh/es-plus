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
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoTileGrid;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
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
import org.elasticsearch.search.aggregations.metrics.PercentileRanks;
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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public List<BaseAggregationBuilder> getAggregationBuilder() {
        return aggregationBuilder;
    }

    @Override
    public Children subAgg(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        List<BaseAggregationBuilder> aggregationBuilder = children.getAggregationBuilder();
        if (!CollectionUtils.isEmpty(aggregationBuilder)) {
            AggregatorFactories.Builder subAggregations = new AggregatorFactories.Builder();
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
    public Children count(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.count(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children avg(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.avg(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Avg} aggregation with the given name.
     */
    @Override
    public Children weightedAvg(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.weightedAvg(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Max} aggregation with the given name.
     */
    @Override
    public Children max(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.max(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Min} aggregation with the given name.
     */
    @Override
    public Children min(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.min(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sum} aggregation with the given name.
     */
    @Override
    public Children sum(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sum(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Stats} aggregation with the given name.
     */
    @Override
    public Children stats(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.stats(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ExtendedStats} aggregation with the given name.
     */
    @Override
    public Children extendedStats(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.extendedStats(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filter} aggregation with the given name.
     */
    @Override
    public Children filter(String name,R field, Supplier<EsWrapper<T>> supplier) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder filter = esAggClient.filter(name,fieldName, supplier.get().esParamWrapper());
        currentBuilder = filter;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public Children filters(String name,R field, FiltersAggregator.KeyedFilter... filters) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:fieldName + AGG_DELIMITER + FiltersAggregationBuilder.NAME;
        FiltersAggregationBuilder filtersAggregationBuilder = new FiltersAggregationBuilder(aggName, filters);
        currentBuilder = filtersAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Filters} aggregation with the given name.
     */
    @Override
    public Children filters(String name,R field, Supplier<EsWrapper<T>>... supplier) {
        String fieldName = getAggregationField(field);
        EsParamWrapper<T>[] esParamWrappers = Arrays.stream(supplier).map(a -> a.get().esParamWrapper()).toArray(EsParamWrapper[]::new);
        currentBuilder = esAggClient.filters(name,fieldName, esParamWrappers);
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given name and separator
     */
    @Override
    public Children adjacencyMatrix(String name,R field, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap) {
        String fieldName = getAggregationField(field);
        Map<String, EsParamWrapper<?>> esParamWrapperMap = new HashMap<>();
        adjacencyMatrixMap.forEach((k, v) -> esParamWrapperMap.put(k, v.get().esParamWrapper()));
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.adjacencyMatrix(name,fieldName, separator, esParamWrapperMap);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children sampler(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sampler(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Sampler} aggregation with the given name.
     */
    @Override
    public Children diversifiedSampler(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.diversifiedSampler(name,fieldName);
        currentBuilder = baseAggregationBuilder;
          
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Global} aggregation with the given name.
     */
    @Override
    public Children global(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.global(name,fieldName);
        currentBuilder = baseAggregationBuilder;
  
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Missing} aggregation with the given name.
     */
    @Override
    public Children missing(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.missing(name,fieldName);
        currentBuilder = baseAggregationBuilder;
  
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Nested} aggregation with the given name.
     */
    @Override
    public Children nested(String name,R field, String path,Consumer<Children> subAgg) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.nested(name,fieldName, path);
        currentBuilder = baseAggregationBuilder;
        if (subAgg!=null){
            subAgg(subAgg);
        }
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ReverseNested} aggregation with the given name.
     * 这个方法只能在嵌套聚合的的子聚合中使用。能根据子类聚合分组后，获取分组内外层数据聚合的信息
     * A嵌套B    A有一条数据B有两条数据，B的字段1=a
     * 查询B字段1的a分组后会统计出B的数量是2。 使用reverseNested聚合可以到外层A的count。也就是1
     */
    @SneakyThrows
    @Override
    public Children reverseNested(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.reverseNested(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoDistance} aggregation with the given name.
     */
    @Override
    public Children geoDistance(String name,R field, GeoPoint origin) {
        String fieldName = getAggregationField(field);
        String aggName =name!=null?name: field + AGG_DELIMITER + GeoDistanceAggregationBuilder.NAME;
        GeoDistanceAggregationBuilder geoDistanceAggregationBuilder = new GeoDistanceAggregationBuilder(aggName, origin);
        geoDistanceAggregationBuilder.field(fieldName);
        currentBuilder = geoDistanceAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Histogram} aggregation with the given name.
     */
    @Override
    public Children histogram(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.histogram(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoHashGrid} aggregation with the given name.
     */
    @SneakyThrows
    @Override
    public Children geohashGrid(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geohashGrid(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link InternalGeoTileGrid} aggregation with the given name.
     */
    @Override
    @SneakyThrows
    public Children geotileGrid(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geohashGrid(name,fieldName);
        currentBuilder = baseAggregationBuilder;
   
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link SignificantTerms} aggregation with the given name.
     */
    @Override
    public Children significantTerms(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.significantTerms(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }


    /**
     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given name and text field name
     */
    @Override
    public Children significantText(String name,R field ) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.significantText(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }


    /**
     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
     * name.
     */
    @Override
    public Children dateHistogram(String name,R field, DateHistogramInterval dateHistogramInterval) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.dateHistogram(name,fieldName,dateHistogramInterval);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Range} aggregation with the given name.
     */
    @Override
    public Children range(String name,R field,String key ,double from,double to) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.range(name,fieldName,key,from,to);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public Children dateRange(String name,R field,String key ,String from,String to) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.dateRange(name,fieldName,key,from,to);
        currentBuilder = baseAggregationBuilder;
  
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
     * given name.
     */
    @Override
    public Children ipRange(String name,R field) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:field + AGG_DELIMITER + IpRangeAggregationBuilder.NAME;
        IpRangeAggregationBuilder ipRangeAggregationBuilder = new IpRangeAggregationBuilder(aggName);
        ipRangeAggregationBuilder.field(fieldName);
        currentBuilder = ipRangeAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Terms} aggregation with the given name.
     */
    @Override
    public Children terms(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.terms(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Percentiles} aggregation with the given name.
     */
    @Override
    public Children percentiles(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentiles(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link PercentileRanks} aggregation with the given name.
     */
    @Override
    public Children percentileRanks(String name,R field, double[] values) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentiles(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given name
     */
    @Override
    public Children medianAbsoluteDeviation(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.medianAbsoluteDeviation(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link Cardinality} aggregation with the given name.
     */
    @Override
    public Children cardinality(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.cardinality(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link TopHits} aggregation with the given name.
     */
    @Override
    public Children topHits(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.topHits(name,fieldName);
        currentBuilder = baseAggregationBuilder;
 
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoBounds} aggregation with the given name.
     */
    @Override
    public Children geoBounds(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geoBounds(name,fieldName);
        currentBuilder = baseAggregationBuilder;
       
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link GeoCentroid} aggregation with the given name.
     */
    @Override
    public Children geoCentroid(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.geoCentroid(name,fieldName);
        currentBuilder = baseAggregationBuilder;
       
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link ScriptedMetric} aggregation with the given name.
     */
    @Override
    public Children scriptedMetric(String name,R field) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.scriptedMetric(name,fieldName);
        currentBuilder = baseAggregationBuilder;
       
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * Create a new {@link CompositeAggregationBuilder} aggregation with the given name.
     */
    @Override
    public Children composite(String name,R field, List<CompositeValuesSourceBuilder<?>> sources) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:fieldName + AGG_DELIMITER + CompositeAggregationBuilder.NAME;
        CompositeAggregationBuilder compositeAggregationBuilder = new CompositeAggregationBuilder(aggName, sources);
        
        currentBuilder = compositeAggregationBuilder;
       
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    /**
     * piple
     */
    @Override
    public Children derivative(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.derivative(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
     
        aggregationBuilder.add(baseAggregationBuilder);
        return this.children;
    }

    @Override
    public Children maxBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.maxBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
     
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children minBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.minBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
   
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public final Children avgBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.avgBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
     
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children sumBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.sumBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
     
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children statsBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.statsBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
     
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children extendedStatsBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.extendedStatsBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
    
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children percentilesBucket(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.percentilesBucket(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
      
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketScript(String name,R field, Map<String, String> bucketsPathsMap, Script script) {
        String fieldName = getAggregationField(field);
        String aggName =  name!=null?name:fieldName + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        BucketScriptPipelineAggregationBuilder bucketScriptPipelineAggregationBuilder = new BucketScriptPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
        currentBuilder = bucketScriptPipelineAggregationBuilder;
   
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketScript(String name,R field, Script script, String... bucketsPaths) {
        String fieldName = getAggregationField(field);
        String aggName =  name!=null?name:fieldName + AGG_DELIMITER + BucketScriptPipelineAggregationBuilder.NAME;
        BucketScriptPipelineAggregationBuilder bucketScriptPipelineAggregationBuilder = new BucketScriptPipelineAggregationBuilder(aggName, script, bucketsPaths);
        currentBuilder = bucketScriptPipelineAggregationBuilder;
   
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSelector(String name,R field, Map<String, String> bucketsPathsMap, Script script) {
        String fieldName = getAggregationField(field);
        String aggName =  name!=null?name:field + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        BucketSelectorPipelineAggregationBuilder bucketSelectorPipelineAggregationBuilder = new BucketSelectorPipelineAggregationBuilder(aggName, bucketsPathsMap, script);
        currentBuilder = bucketSelectorPipelineAggregationBuilder;
    
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSelector(String name,R field, Script script, String... bucketsPaths) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name: fieldName + AGG_DELIMITER + BucketSelectorPipelineAggregationBuilder.NAME;
        BucketSelectorPipelineAggregationBuilder bucketSelectorPipelineAggregationBuilder = new BucketSelectorPipelineAggregationBuilder(aggName, script, bucketsPaths);
        currentBuilder = bucketSelectorPipelineAggregationBuilder;
   
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSort(String name,R field, List<FieldSortBuilder> sorts) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:fieldName + AGG_DELIMITER + BucketSortPipelineAggregationBuilder.NAME;
        BucketSortPipelineAggregationBuilder bucketSortPipelineAggregationBuilder = new BucketSortPipelineAggregationBuilder(aggName, sorts);
        currentBuilder = bucketSortPipelineAggregationBuilder;
    
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children bucketSort(String name,R field, int from, int size, boolean asc, String... orderColumns) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:fieldName + AGG_DELIMITER + BucketSortPipelineAggregationBuilder.NAME;
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
    public Children cumulativeSum(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.cumulativeSum(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
  
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children diff(String name,R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        BaseAggregationBuilder baseAggregationBuilder = esAggClient.diff(name,fieldName, bucketsPath);
        currentBuilder = baseAggregationBuilder;
      
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }

    @Override
    public Children movingFunction(String name,R field, Script script, String bucketsPaths, int window) {
        String fieldName = getAggregationField(field);
        String aggName = name!=null?name:fieldName + AGG_DELIMITER + MovFnPipelineAggregationBuilder.NAME;
        MovFnPipelineAggregationBuilder movFnPipelineAggregationBuilder = new MovFnPipelineAggregationBuilder(aggName, bucketsPaths, script, window);
        currentBuilder = movFnPipelineAggregationBuilder;
      
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    //    ------------------------------------------------------ Function参数的方法  es版本6不支持。
    //    因为需要类名class。如果版本不匹配可能会出现找不到类的情况
    
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
        adjacencyMatrix(null,name, null, filters);
        fn.apply((AdjacencyMatrixAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children adjacencyMatrix(R name, String separator, Map<String, Supplier<EsWrapper<T>>> filters, Function<AdjacencyMatrixAggregationBuilder, AdjacencyMatrixAggregationBuilder> fn) {
        adjacencyMatrix(null,name, separator, filters);
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
    public Children significantText(R field, String name, Function<SignificantTextAggregationBuilder, SignificantTextAggregationBuilder> fn) {
        significantText(name ,field);
        fn.apply((SignificantTextAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children dateHistogram(R name,DateHistogramInterval dateHistogramInterval, Function<DateHistogramAggregationBuilder, DateHistogramAggregationBuilder> fn) {
        dateHistogram(name,dateHistogramInterval);
        fn.apply((DateHistogramAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children range(R name,String key,double from, double to, Function<RangeAggregationBuilder, RangeAggregationBuilder> fn) {
        range(name,key,from,to);
        fn.apply((RangeAggregationBuilder) currentBuilder);
        return children;
    }

    @Override
    public Children dateRange(R name,String key,String from, String to, Function<DateRangeAggregationBuilder, DateRangeAggregationBuilder> fn) {
        dateRange(name,key,from,to);
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
