package com.es.plus.es6.client;

import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.core.EsAggClient;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpDateHistogramInterval;

import javax.naming.OperationNotSupportedException;
import java.util.Map;

import static com.es.plus.constant.EsConstant.AGG_DELIMITER;


public class EsPlus6AggsClient implements EsAggClient {
    @Override
    public EpAggBuilder count(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "count", "count")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder avg(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "avg", "avg")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder weightedAvg(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "weighted_avg", "weighted_avg")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder max(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "max", "max")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder min(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "min", "min")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder sum(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "sum", "sum")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder stats(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "stats", "stats")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder extendedStats(String name,String field) {
        return new EpAggBuilder(name!=null?name : field + AGG_DELIMITER + "extended_stats", "extended_stats")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder filter(String name,String field, EsParamWrapper<?> esParamWrapper) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "filter";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "filter");
        // 注意：这里需要特殊处理，因为filter聚合需要查询条件
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder filters(String name,String field, EsParamWrapper<?>... esParamWrapper) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "filters";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "filters");
        // 注意：这里需要特殊处理，因为filters聚合需要多个查询条件
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder adjacencyMatrix(String name,String field, Map<String, EsParamWrapper<?>> esParamWrapper) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "adjacency_matrix";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "adjacency_matrix");
        // 注意：这里需要特殊处理，因为adjacency_matrix聚合需要查询条件映射
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder adjacencyMatrix(String name,String field, String separator, Map<String, EsParamWrapper<?>> esParamWrapper) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "adjacency_matrix";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "adjacency_matrix")
                .param("separator", separator);
        // 注意：这里需要特殊处理，因为adjacency_matrix聚合需要查询条件映射
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder sampler(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "sampler";
        return new EpAggBuilder(aggName, "sampler");
    }
    
    @Override
    public EpAggBuilder diversifiedSampler(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "diversified_sampler";
        return new EpAggBuilder(aggName, "diversified_sampler")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder global(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "global";
        return new EpAggBuilder(aggName, "global");
    }
    
    @Override
    public EpAggBuilder missing(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "missing";
        return new EpAggBuilder(aggName, "missing")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder nested(String name,String field, String path) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "nested";
        return new EpAggBuilder(aggName, "nested")
                .param("path", path);
    }
    
    @Override
    public EpAggBuilder reverseNested(String name,String field) throws OperationNotSupportedException {
        String aggName = name!=null?name : field + AGG_DELIMITER + "reverse_nested";
        return new EpAggBuilder(aggName, "reverse_nested");
    }
    
    @Override
    public EpAggBuilder histogram(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "histogram";
        return new EpAggBuilder(aggName, "histogram")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder geohashGrid(String name,String field) throws OperationNotSupportedException {
        String aggName = name!=null?name : field + AGG_DELIMITER + "geohash_grid";
        return new EpAggBuilder(aggName, "geohash_grid")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder geotileGrid(String name,String field) throws OperationNotSupportedException {
        String aggName = name!=null?name : field + AGG_DELIMITER + "geotile_grid";
        return new EpAggBuilder(aggName, "geotile_grid")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder significantTerms(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "significant_terms";
        return new EpAggBuilder(aggName, "significant_terms")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder significantText(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "significant_text";
        return new EpAggBuilder(aggName, "significant_text")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder dateHistogram(String name,String field, EpDateHistogramInterval dateHistogramInterval) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "date_histogram";
        return new EpAggBuilder(aggName, "date_histogram")
                .param("field", field)
                .param("interval", dateHistogramInterval.toString());
    }
    
    @Override
    public EpAggBuilder range(String name,String field,String key ,Double from,Double to) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "range";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "range")
                .param("field", field);
        // 注意：range聚合的范围需要特殊处理
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder dateRange(String name,String field,String key ,String from,String to) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "date_range";
        EpAggBuilder epAggBuilder = new EpAggBuilder(aggName, "date_range")
                .param("field", field);
        // 注意：date_range聚合的范围需要特殊处理
        return epAggBuilder;
    }
    
    @Override
    public EpAggBuilder ipRange(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "ip_range";
        return new EpAggBuilder(aggName, "ip_range")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder terms(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "terms";
        return new EpAggBuilder(aggName, "terms")
                .param("field", field)
                .param("size", GlobalConfigCache.GLOBAL_CONFIG.getAggSize());
    }
    
    @Override
    public EpAggBuilder percentiles(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "percentiles";
        return new EpAggBuilder(aggName, "percentiles")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder percentileStringanks(String name,String field, double[] values) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "percentile_ranks";
        return new EpAggBuilder(aggName, "percentile_ranks")
                .param("field", field)
                .param("values", values);
    }
    
    @Override
    public EpAggBuilder medianAbsoluteDeviation(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "median_absolute_deviation";
        return new EpAggBuilder(aggName, "median_absolute_deviation")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder cardinality(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "cardinality";
        return new EpAggBuilder(aggName, "cardinality")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder topHits(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "top_hits";
        return new EpAggBuilder(aggName, "top_hits");
    }
    
    @Override
    public EpAggBuilder geoBounds(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "geo_bounds";
        return new EpAggBuilder(aggName, "geo_bounds")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder geoCentroid(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "geo_centroid";
        return new EpAggBuilder(aggName, "geo_centroid")
                .param("field", field);
    }
    
    @Override
    public EpAggBuilder scriptedMetric(String name,String field) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "scripted_metric";
        return new EpAggBuilder(aggName, "scripted_metric");
    }
    
    @Override
    public EpAggBuilder derivative(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "derivative";
        return new EpAggBuilder(aggName, "derivative")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder maxBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "max_bucket";
        return new EpAggBuilder(aggName, "max_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder minBucket(String name,String field , String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "min_bucket";
        return new EpAggBuilder(aggName, "min_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public final EpAggBuilder avgBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + "avg_bucket";
        return new EpAggBuilder(aggName, "avg_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder sumBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "sum_bucket";
        return new EpAggBuilder(aggName, "sum_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder statsBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "stats_bucket";
        return new EpAggBuilder(aggName, "stats_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder extendedStatsBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "extended_stats_bucket";
        return new EpAggBuilder(aggName, "extended_stats_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder percentilesBucket(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "percentiles_bucket";
        return new EpAggBuilder(aggName, "percentiles_bucket")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder cumulativeSum(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "cumulative_sum";
        return new EpAggBuilder(aggName, "cumulative_sum")
                .param("bucketsPath", bucketsPath);
    }
    
    @Override
    public EpAggBuilder diff(String name,String field, String bucketsPath) {
        String aggName = name!=null?name : field + AGG_DELIMITER + "serial_diff";
        return new EpAggBuilder(aggName, "serial_diff")
                .param("bucketsPath", bucketsPath);
    }

}
