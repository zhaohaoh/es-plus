package com.es.plus.core.wrapper.aggregation;

import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsAggClient;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpCompositeValuesSourceBuilder;
import com.es.plus.common.pojo.es.EpDateHistogramInterval;
import com.es.plus.common.pojo.es.EpFieldSortBuilder;
import com.es.plus.common.pojo.es.EpGeoPoint;
import com.es.plus.common.pojo.es.EpScript;
import com.es.plus.common.pojo.es.EpSortOrder;
import com.es.plus.core.wrapper.core.EsWrapper;
import com.es.plus.es6.client.EsPlus6AggsClient;
import com.es.plus.es7.client.EsPlusAggsClient;
import lombok.SneakyThrows;
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
    protected List<EpAggBuilder> aggregationBuilder = new ArrayList<>();
    protected EpAggBuilder currentBuilder;
    protected EsAggClient esAggClient;
    
    public List<EpAggBuilder> getAggregationBuilder() {
        return aggregationBuilder;
    }
    
    @Override
    public Children subAgg(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        List<EpAggBuilder> aggregationBuilder = children.getAggregationBuilder();
        if (!CollectionUtils.isEmpty(aggregationBuilder)) {
            currentBuilder.subAggregation(aggregationBuilder);
        }
        return this.children;
    }
    
    @Override
    public Children add(EpAggBuilder aggBuilder) {
        currentBuilder = aggBuilder;
        aggregationBuilder.add(aggBuilder);
        return this.children;
    }
    
    @Override
    public Children count(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "count";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "count").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children avg(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "avg";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "avg").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children weightedAvg(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "weighted_avg";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "weighted_avg").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children max(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "max";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "max").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children min(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "min";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "min").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children sum(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "sum";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "sum").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children stats(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "stats";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "stats").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children extendedStats(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "extended_stats";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "extended_stats").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children filter(String name, R field, Supplier<EsWrapper<T>> supplier) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "filter";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "filter")
                .param("field", fieldName)
                .param("query", supplier.get().esParamWrapper());
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children filters(String name, R field, Supplier<EsWrapper<T>>... supplier) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "filters";
        EsParamWrapper<T>[] esParamWrappers = Arrays.stream(supplier).map(a -> a.get().esParamWrapper()).toArray(EsParamWrapper[]::new);
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "filters").param("filters", esParamWrappers);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name and separator
     */
    @Override
    public Children adjacencyMatrix(String name, R field, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "adjacency_matrix";
        Map<String, EsParamWrapper<?>> esParamWrapperMap = new HashMap<>();
        adjacencyMatrixMap.forEach((k, v) -> esParamWrapperMap.put(k, v.get().esParamWrapper()));
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "adjacency_matrix")
                .param("field", fieldName)
                .param("separator", separator)
                .param("filters", esParamWrapperMap);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children sampler(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "sampler";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "sampler").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
   
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children diversifiedSampler(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "diversified_sampler";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "diversified_sampler").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children global(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "global";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "global").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children missing(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "missing";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "missing").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children nested(String name, R field, String path, Consumer<Children> subAgg) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "nested";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "nested")
                .param("field", fieldName)
                .param("path", path);
        currentBuilder = aggBuilder;
        if (subAgg != null) {
            subAgg(subAgg);
        }
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     * 这个方法只能在嵌套聚合的的子聚合中使用。能根据子类聚合分组后，获取分组内外层数据聚合的信息
     * A嵌套B    A有一条数据B有两条数据，B的字段1=a
     * 查询B字段1的a分组后会统计出B的数量是2。 使用reverseNested聚合可以到外层A的count。也就是1
     */
    @SneakyThrows
    @Override
    public Children reverseNested(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "reverse_nested";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "reverse_nested").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children geoDistance(String name, R field, EpGeoPoint origin) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : field + AGG_DELIMITER + "geo_distance";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "geo_distance")
                .param("field", fieldName)
                .param("origin", origin);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children histogram(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "histogram";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "histogram").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @SneakyThrows
    @Override
    public Children geohashGrid(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "geohash_grid";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "geohash_grid").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    @SneakyThrows
    public Children geotileGrid(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "geotile_grid";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "geotile_grid").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children significantTerms(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "significant_terms";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "significant_terms").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name and text field name
     */
    @Override
    public Children significantText(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "significant_text";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "significant_text").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children dateHistogram(String name, R field, EpDateHistogramInterval dateHistogramInterval) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "date_histogram";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "date_histogram")
                .param("field", fieldName)
                .param("interval", dateHistogramInterval);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children range(String name, R field, String key, double from, double to) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "range";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "range")
                .param("field", fieldName)
                .param("key", key)
                .param("from", from)
                .param("to", to);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children dateRange(String name, R field, String key, String from, String to) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "date_range";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "date_range")
                .param("field", fieldName)
                .param("key", key)
                .param("from", from)
                .param("to", to);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children ipRange(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : field + AGG_DELIMITER + "ip_range";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "ip_range").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children terms(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "terms";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "terms").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children percentiles(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "percentiles";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "percentiles").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children percentileRanks(String name, R field, double[] values) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "percentile_ranks";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "percentile_ranks")
                .param("field", fieldName)
                .param("values", values);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name
     */
    @Override
    public Children medianAbsoluteDeviation(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "median_absolute_deviation";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "median_absolute_deviation").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children cardinality(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "cardinality";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "cardinality").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children topHits(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "top_hits";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "top_hits").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children geoBounds(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "geo_bounds";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "geo_bounds").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children geoCentroid(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "geo_centroid";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "geo_centroid").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children scriptedMetric(String name, R field) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "scripted_metric";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "scripted_metric").param("field", fieldName);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    /**
     * Create a new aggregation with the given name.
     */
    @Override
    public Children derivative(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "derivative";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "derivative")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children maxBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "max_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "max_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children minBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "min_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "min_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public final Children avgBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "avg_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "avg_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children sumBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "sum_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "sum_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children statsBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "stats_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "stats_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children extendedStatsBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "extended_stats_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "extended_stats_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children percentilesBucket(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "percentiles_bucket";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "percentiles_bucket")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketScript(String name, R field, Map<String, String> bucketsPathsMap, EpScript script) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "bucket_script";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_script")
                .param("field", fieldName)
                .param("buckets_paths_map", bucketsPathsMap)
                .param("script", script);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketScript(String name, R field, EpScript script, String... bucketsPaths) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "bucket_script";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_script")
                .param("field", fieldName)
                .param("script", script)
                .param("buckets_paths", bucketsPaths);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketSelector(String name, R field, Map<String, String> bucketsPathsMap, EpScript script) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : field + AGG_DELIMITER + "bucket_selector";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_selector")
                .param("field", fieldName)
                .param("buckets_paths_map", bucketsPathsMap)
                .param("script", script);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketSelector(String name, R field, EpScript script, String... bucketsPaths) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "bucket_selector";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_selector")
                .param("field", fieldName)
                .param("script", script)
                .param("buckets_paths", bucketsPaths);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketSort(String name, R field, List<EpFieldSortBuilder> sorts) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "bucket_sort";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_sort")
                .param("field", fieldName)
                .param("sorts", sorts);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children bucketSort(String name, R field, int from, int size, boolean asc, String... orderColumns) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "bucket_sort";
        List<EpFieldSortBuilder> sorts = Arrays.stream(orderColumns).map(o -> {
            EpFieldSortBuilder sortBuilder = new EpFieldSortBuilder(o);
            sortBuilder.setOrder(asc ? EpSortOrder.ASC : EpSortOrder.DESC);
            return sortBuilder;
        }).collect(Collectors.toList());
        
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "bucket_sort")
                .param("field", fieldName)
                .param("sorts", sorts)
                .param("from", from)
                .param("size", size);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children cumulativeSum(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "cumulative_sum";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "cumulative_sum")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children diff(String name, R field, String bucketsPath) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "diff";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "serial_diff")
                .param("field", fieldName)
                .param("buckets_path", bucketsPath);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    @Override
    public Children movingFunction(String name, R field, EpScript script, String bucketsPaths, int window) {
        String fieldName = getAggregationField(field);
        String aggName = name != null ? name : fieldName + AGG_DELIMITER + "moving_function";
        EpAggBuilder aggBuilder = new EpAggBuilder(aggName, "moving_fn")
                .param("field", fieldName)
                .param("script", script)
                .param("buckets_paths", bucketsPaths)
                .param("window", window);
        currentBuilder = aggBuilder;
        aggregationBuilder.add(currentBuilder);
        return this.children;
    }
    
    //    ------------------------------------------------------ Function参数的方法
    
    @Override
    public Children count(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        count(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children avg(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        avg(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children weightedAvg(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        weightedAvg(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children max(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        max(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children min(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        min(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children sum(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        sum(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children stats(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        stats(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children extendedStats(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        extendedStats(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children filter(R name, Supplier<EsWrapper<T>> filter, Function<EpAggBuilder, EpAggBuilder> fn) {
        filter(name, filter);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children adjacencyMatrix(R name, Map<String, Supplier<EsWrapper<T>>> filters, Function<EpAggBuilder, EpAggBuilder> fn) {
        adjacencyMatrix(null, name, null, filters);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children adjacencyMatrix(R name, String separator, Map<String, Supplier<EsWrapper<T>>> filters, Function<EpAggBuilder, EpAggBuilder> fn) {
        adjacencyMatrix(null, name, separator, filters);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children sampler(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        sampler(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children diversifiedSampler(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        diversifiedSampler(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children global(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        global(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children missing(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        missing(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children nested(R name, String path, Function<EpAggBuilder, EpAggBuilder> fn) {
        nested(name, path, fn);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children reverseNested(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        reverseNested(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children geoDistance(R name, EpGeoPoint origin, Function<EpAggBuilder, EpAggBuilder> fn) {
        geoDistance(name, origin);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children histogram(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        histogram(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children significantTerms(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        significantTerms(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children significantText(R field, String name, Function<EpAggBuilder, EpAggBuilder> fn) {
        significantText(name, field);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children dateHistogram(R name, EpDateHistogramInterval dateHistogramInterval, Function<EpAggBuilder, EpAggBuilder> fn) {
        dateHistogram(name, dateHistogramInterval);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children range(R name, String key, double from, double to, Function<EpAggBuilder, EpAggBuilder> fn) {
        range(name, key, from, to);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children dateRange(R name, String key, String from, String to, Function<EpAggBuilder, EpAggBuilder> fn) {
        dateRange(name, key, from, to);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children ipRange(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        ipRange(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children terms(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        terms(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children percentiles(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        percentiles(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children percentileRanks(R name, double[] values, Function<EpAggBuilder, EpAggBuilder> fn) {
        percentileRanks(name, values);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children medianAbsoluteDeviation(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        medianAbsoluteDeviation(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children cardinality(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        cardinality(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children topHits(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        topHits(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children geoBounds(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        geoBounds(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children geoCentroid(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        geoCentroid(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children scriptedMetric(R name, Function<EpAggBuilder, EpAggBuilder> fn) {
        scriptedMetric(name);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children derivative(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        derivative(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children maxBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        maxBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children minBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        minBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children avgBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        avgBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children sumBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        sumBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children statsBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        statsBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children extendedStatsBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        extendedStatsBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children percentilesBucket(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        percentilesBucket(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children bucketScript(R name, Function<EpAggBuilder, EpAggBuilder> fn, Map<String, String> bucketsPathsMap, EpScript script) {
        bucketScript(name, bucketsPathsMap, script);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children bucketScript(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String... bucketsPaths) {
        bucketScript(name, script, bucketsPaths);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children bucketSelector(R name, Function<EpAggBuilder, EpAggBuilder> fn, Map<String, String> bucketsPathsMap, EpScript script) {
        bucketSelector(name, bucketsPathsMap, script);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children bucketSelector(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String... bucketsPaths) {
        bucketSelector(name, script, bucketsPaths);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children bucketSort(R name, Function<EpAggBuilder, EpAggBuilder> fn, List<EpFieldSortBuilder> sorts) {
        bucketSort(name,fn ,sorts);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children cumulativeSum(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        cumulativeSum(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children diff(R name, Function<EpAggBuilder, EpAggBuilder> fn, String bucketsPath) {
        diff(name, bucketsPath);
        fn.apply(currentBuilder);
        return children;
    }
    /**
     */
    @Override
    public Children composite(R field, List<EpCompositeValuesSourceBuilder<?>> sources,
            Function<EpAggBuilder, EpAggBuilder> fn)  {
        composite(field, sources);
        fn.apply(currentBuilder);
        return children;
    }
    
    @Override
    public Children movingFunction(R name, Function<EpAggBuilder, EpAggBuilder> fn, EpScript script, String bucketsPaths, int window) {
        movingFunction(name, script, bucketsPaths, window);
        fn.apply(currentBuilder);
        return children;
    }
}
