package com.es.plus.core.wrapper.aggregation;

import com.es.plus.adapter.pojo.es.EpAggBuilder;
import com.es.plus.adapter.pojo.es.EpDateHistogramInterval;
import com.es.plus.adapter.pojo.es.EpFieldSortBuilder;
import com.es.plus.adapter.pojo.es.EpGeoPoint;
import com.es.plus.adapter.pojo.es.EpScript;
import com.es.plus.adapter.pojo.es.EpCompositeValuesSourceBuilder;
import com.es.plus.core.wrapper.core.EsWrapper;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsAggWrapper<Children, R,T>  {
    
    Children subAgg(Consumer<Children> consumer);
    
    /**
     * 自定义添加一个基础聚合
     *
     * @return {@link Children}
     */
    default  Children add(EpAggBuilder epAggBuilder){
        return add(epAggBuilder);
    };
    /**
     * 统计
     *
     * @param field 名字
     * @return {@link Children}
     */
    default Children count(R field){
        return   count(null,  field);
    };
    
    /**
     * Create a new 平均值 aggregation with the given field.
     */
    default Children avg(R field){
        return   avg(null,  field);
    };
    
    /**
     * Create a new 加权平均值 aggregation with the given field.
     */
    default Children weightedAvg(R field){
        return   weightedAvg(null,  field);
    };
    
    /**
     * Create a new 最大值 aggregation with the given field.
     */
    default Children max(R field){
        return   max(null,  field);
    };
    
    /**
     * Create a new 最小值 aggregation with the given field.
     */
    default Children min(R field){
        return   min(null,  field);
    };
    
    /**
     * Create a new 求和 aggregation with the given field.
     */
    default Children sum(R field){
        return   sum(null,  field);
    };
    
    /**
     * Create a new 统计信息 aggregation with the given field.
     */
    default Children stats(R field){
        return   stats(null,  field);
    };
    
    /**
     * Create a new 扩展统计信息 aggregation with the given field.
     */
    default Children extendedStats(R field){
        return   extendedStats(null,  field);
    };
    
    /**
     * Create a new 过滤 aggregation with the given field.
     */
    default Children filter(R field, Supplier<EsWrapper<T>> filterQuery){
        return   filter(null,  field,filterQuery);
    };
    
    
    
    /**
     * Create a new 多过滤器 aggregation with the given field.
     */
    default Children filters(R field, Supplier<EsWrapper<T>>... filterQuery){
        return   filters(null,  field,filterQuery);
    };
    
    
    /**
     * Create a new 采样 aggregation with the given field.
     */
    default Children sampler(R field){
        return   sampler(null,  field);
    };
    
    /**
     * Create a new 多样采样 aggregation with the given field.
     */
    default Children diversifiedSampler(R field){
        return   diversifiedSampler(null,  field);
    };
    
    /**
     * Create a new 全局 aggregation with the given field.
     */
    default Children global(R field){
        return   global(null,  field);
    };
    
    /**
     * Create a new 缺失值 aggregation with the given field.
     */
    default Children missing(R field){
        return   missing(null,  field);
    };
    
    /**
     * Create a new 嵌套 aggregation with the given field.
     */
    default Children nested(R field, String path){
        return   nested(null,field,path);
    };
    
    /**
     * Create a new 反向嵌套 aggregation with the given field.
     */
    default Children reverseNested(R field){
        return   reverseNested(null,  field);
    };
    
    /**
     * Create a new 地理距离 aggregation with the given field.
     */
    default Children geoDistance(R field, EpGeoPoint origin){
        return   geoDistance(null,field,origin);
    };
    
    /**
     * Create a new 直方图 aggregation with the given field.
     */
    default Children histogram(R field){
        return   histogram(null,  field);
    };
    
    /**
     * Create a new 地理哈希网格 aggregation with the given field.
     */
    default Children geohashGrid(R field){
        return   geohashGrid(null,  field);
    };
    
    /**
     * Create a new 地理瓦片网格 aggregation with the given field.
     */
    default Children geotileGrid(R field){
        return   geotileGrid(null,  field);
    };
    
    /**
     * Create a new 重要统计项 aggregation with the given field.
     */
    default Children significantTerms(R field){
        return   significantTerms(null,  field);
    };
    
    
    /**
     * Create a new 重要文本 aggregation with the given field and text field field
     */
    default Children significantText(R field){
        return   significantText(null,  field);
    };
    
    
    /**
     * Create a new 日期直方图 aggregation with the
     * given field.
     */
    default Children dateHistogram(R field, EpDateHistogramInterval dateHistogramInterval){
        return   dateHistogram(null,field,   dateHistogramInterval);
    };
    
    /**
     * Create a new 范围 aggregation with the given field.
     */
    default Children range(R field,String key,double from, double to){
        return   range(null,field,key,from,to);
    };
    
    /**
     * Create a new 日期范围 aggregation with the
     * given field.
     */
    default Children dateRange(R field,String key,String from, String to){
        return   dateRange(null,field,key,from,to);
    };
    
    /**
     * Create a new IP范围 aggregation with the
     * given field.
     */
    default Children ipRange(R field){
        return   ipRange(null,  field);
    };
    
    /**
     * Create a new 词条 aggregation with the given field.
     */
    default Children terms(R field){
        return   terms(null,  field);
    };
    
    /**
     * Create a new 百分比 aggregation with the given field.
     * 百分比聚合
     */
    default Children percentiles(R field){
        return   percentiles(null,  field);
    };
    
    /**
     * Create a new 百分比排名 aggregation with the given field.
     */
    default Children percentileRanks(R field, double[] values){
        return   percentileRanks(null,  field,values);
    };
    
    /**
     * Create a new 中位数绝对偏差 aggregation with the given field
     */
    default Children medianAbsoluteDeviation(R field){
        return   medianAbsoluteDeviation(null,  field);
    };
    
    /**
     * Create a new 基数 aggregation with the given field.
     * 统计去重后的数量
     */
    default Children cardinality(R field){
        return   cardinality(null,  field);
    };
    
    /**
     * Create a new 顶部匹配 aggregation with the given field.
     */
    default Children topHits(R field){
        return   topHits(null,  field);
    };
    
    /**
     * Create a new 地理边界 aggregation with the given field.
     */
    default Children geoBounds(R field){
        return   geoBounds(null,  field);
    };
    
    /**
     * Create a new 地理中心 aggregation with the given field.
     */
    default Children geoCentroid(R field){
        return   geoCentroid(null,  field);
    };
    
    /**
     * Create a new 脚本指标 aggregation with the given field.
     */
    default Children scriptedMetric(R field){
        return   scriptedMetric(null,  field);
    };
    
    /**
     * Create a new 复合 aggregation with the given field.
     */
    default Children composite(R field, List<EpCompositeValuesSourceBuilder<?>> sources){
        return   composite(null,  field,sources);
    };
    
    
    /**
     * piple的方法
     */
    default Children derivative(R field,String bucketsPath){
        return   derivative(null,  field,bucketsPath);
    };
    
    default Children maxBucket(R field,String bucketsPath){
        return   maxBucket(null,  field,bucketsPath);
    };
    
    default Children minBucket(R field,String bucketsPath){
        return   minBucket(null,  field,bucketsPath);
    };
    
    default Children avgBucket(R field,String bucketsPath){
        return   avgBucket(null,  field,bucketsPath);
    };
    
    default Children sumBucket(R field,String bucketsPath){
        return   sumBucket(null,  field,bucketsPath);
    };
    
    default Children statsBucket(R field,String bucketsPath){
        return   statsBucket(null,  field,bucketsPath);
    };
    
    default Children extendedStatsBucket(R field,String bucketsPath){
        return   extendedStatsBucket(null,  field,bucketsPath);
    };
    
    default Children percentilesBucket(R field,String bucketsPath){
        return   percentilesBucket(null,  field,bucketsPath);
    };
    
    default Children bucketScript(R field,Map<String, String> bucketsPathsMap, EpScript script){
        return   bucketScript(null,  field,bucketsPathsMap,script);
    };
    
    default Children bucketScript(R field, EpScript EpScript, String... bucketsPaths){
        return   bucketScript(null,  field,EpScript,bucketsPaths);
    };
    
    default Children bucketSelector(R field,Map<String, String> bucketsPathsMap, EpScript script){
        return   bucketSelector(null,  field,bucketsPathsMap,script);
    };
    
    default Children bucketSelector(R field, EpScript script, String... bucketsPaths){
        return   bucketSelector(null,  field,script,bucketsPaths);
    };
    
    default Children bucketSort(R field,List<EpFieldSortBuilder> sorts){
        return   bucketSort(null,  field,sorts);
    };
    
    default Children bucketSort(R field,int from, int size, boolean asc, String... orderColumns){
        return   bucketSort(null,  field,from,size,asc,orderColumns);
    };
    
    default Children cumulativeSum(R field,String bucketsPath){
        return   cumulativeSum(null,  field,bucketsPath);
    };
    
    default Children diff(R field,String bucketsPath){
        return   diff(null,  field,bucketsPath);
    };
    
    default Children movingFunction(R field,EpScript script, String bucketsPaths, int window){
        return   movingFunction(null,  field,script,bucketsPaths,window);
    };
    
    
    
    //    ==============================================================
    /**
     * 统计
     *
     * @param field 名字
     * @return {@link Children}
     */
    default Children count(String name,R field){
        return   count(name,  field);
    };
    
    /**
     * Create a new 平均值 aggregation with the given field.
     */
    default Children avg(String name,R field){
        return   avg(name,  field);
    };
    
    /**
     * Create a new 加权平均值 aggregation with the given field.
     */
    default Children weightedAvg(String name,R field){
        return   weightedAvg(name,  field);
    };
    
    /**
     * Create a new 最大值 aggregation with the given field.
     */
    default Children max(String name,R field){
        return   max(name,  field);
    };
    
    /**
     * Create a new 最小值 aggregation with the given field.
     */
    default Children min(String name,R field){
        return   min(name,  field);
    };
    
    /**
     * Create a new 求和 aggregation with the given field.
     */
    default Children sum(String name,R field){
        return   sum(name,  field);
    };
    
    /**
     * Create a new 统计信息 aggregation with the given field.
     */
    default Children stats(String name,R field){
        return   stats(name,  field);
    };
    
    /**
     * Create a new 扩展统计信息 aggregation with the given field.
     */
    default Children extendedStats(String name,R field){
        return   extendedStats(name,  field);
    };
    default Children adjacencyMatrix(String name,R field, Map<String, Supplier<EsWrapper<T>>> filters){
        return   adjacencyMatrix(name,  field,null, filters);
    };
    
    /**
     * Create a new {@link AdjacencyMatrix} aggregation with the given field and separator
     */
    default Children adjacencyMatrix(String name,R field, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap){
        return   adjacencyMatrix(name,  field,separator,adjacencyMatrixMap);
    };
    /**
     * Create a new 过滤 aggregation with the given field.
     */
    default Children filter(String name,R field, Supplier<EsWrapper<T>> filterQuery){
        return   filter(name,  field,filterQuery);
    };
    
    /**
     * Create a new 多过滤器 aggregation with the given field.
     */
    default Children filters(String name,R field, Supplier<EsWrapper<T>>... filterQuery){
        return   filters(name,  field,filterQuery);
    };
    
    /**
     * Create a new 采样 aggregation with the given field.
     */
    default Children sampler(String name,R field){
        return   sampler(name,  field);
    };
    
    /**
     * Create a new 多样采样 aggregation with the given field.
     */
    default Children diversifiedSampler(String name,R field){
        return   diversifiedSampler(name,  field);
    };
    
    /**
     * Create a new 全局 aggregation with the given field.
     */
    default Children global(String name,R field){
        return   global(name,  field);
    };
    
    /**
     * Create a new 缺失值 aggregation with the given field.
     */
    default Children missing(String name,R field){
        return   missing(name,  field);
    };
    
    /**
     * Create a new 嵌套 aggregation with the given field.
     */
    default Children nested(String name,R field, String path){
        return   nested(name,field,path);
    };
    
    Children nested(String name, R field, String path, Consumer<Children> subAgg);
    
    /**
     * Create a new 反向嵌套 aggregation with the given field.
     */
    default Children reverseNested(String name,R field){
        return   reverseNested(name,  field);
    };
    
    /**
     * Create a new 地理距离 aggregation with the given field.
     */
    default Children geoDistance(String name,R field, EpGeoPoint origin){
        return   geoDistance(name,field,origin);
    };
    
    /**
     * Create a new 直方图 aggregation with the given field.
     */
    default Children histogram(String name,R field){
        return   histogram(name,  field);
    };
    
    /**
     * Create a new 地理哈希网格 aggregation with the given field.
     */
    default Children geohashGrid(String name,R field){
        return   geohashGrid(name,  field);
    };
    
    /**
     * Create a new 地理瓦片网格 aggregation with the given field.
     */
    default Children geotileGrid(String name,R field){
        return   geotileGrid(name,  field);
    };
    
    /**
     * Create a new 重要统计项 aggregation with the given field.
     */
    default Children significantTerms(String name,R field){
        return   significantTerms(name,  field);
    };
    
    
    /**
     * Create a new 重要文本 aggregation with the given field and text field field
     */
    default Children significantText(String name,R field){
        return   significantText(name,  field);
    };
    
    
    /**
     * Create a new 日期直方图 aggregation with the
     * given field.
     */
    default Children dateHistogram(String name,R field,EpDateHistogramInterval dateHistogramInterval){
        return   dateHistogram(name,field, dateHistogramInterval);
    };
    
    /**
     * Create a new 范围 aggregation with the given field.
     */
    default Children range(String name,R field,String key ,double from,double to){
        return   range(name,field,key,from,to);
    };
    
    /**
     * Create a new 日期范围 aggregation with the
     * given field.
     */
    default Children dateRange(String name,R field,String key ,String from,String to){
        return   dateRange(name,field,key,from,to);
    };
    
    /**
     * Create a new IP范围 aggregation with the
     * given field.
     */
    default Children ipRange(String name,R field){
        return   ipRange(name,  field);
    };
    
    /**
     * Create a new 词条 aggregation with the given field.
     */
    default Children terms(String name,R field){
        return   terms(name,  field);
    };
    
    /**
     * Create a new 百分比 aggregation with the given field.
     */
    default Children percentiles(String name,R field){
        return   percentiles(name,  field);
    };
    
    /**
     * Create a new 百分比排名 aggregation with the given field.
     */
    default Children percentileRanks(String name,R field, double[] values){
        return   percentileRanks(name,  field,values);
    };
    
    /**
     * Create a new 中位数绝对偏差 aggregation with the given field
     */
    default Children medianAbsoluteDeviation(String name,R field){
        return   medianAbsoluteDeviation(name,  field);
    };
    
    /**
     * Create a new 基数 aggregation with the given field.
     */
    default Children cardinality(String name,R field){
        return   cardinality(name,  field);
    };
    
    /**
     * Create a new 顶部匹配 aggregation with the given field.
     */
    default Children topHits(String name,R field){
        return   topHits(name,  field);
    };
    
    /**
     * Create a new 地理边界 aggregation with the given field.
     */
    default Children geoBounds(String name,R field){
        return   geoBounds(name,  field);
    };
    
    /**
     * Create a new 地理中心 aggregation with the given field.
     */
    default Children geoCentroid(String name,R field){
        return   geoCentroid(name,  field);
    };
    
    /**
     * Create a new 脚本指标 aggregation with the given field.
     */
    default Children scriptedMetric(String name,R field){
        return   scriptedMetric(name,  field);
    };
    
    /**
     * Create a new 复合 aggregation with the given field.
     */
    default Children composite(String name,R field, List<EpCompositeValuesSourceBuilder<?>> sources){
        return   composite(name,  field,sources);
    };
    
    
    /**
     * piple的方法
     */
    default Children derivative(String name,R field,String bucketsPath){
        return   derivative(name,  field,bucketsPath);
    };
    
    default Children maxBucket(String name,R field,String bucketsPath){
        return   maxBucket(name,  field,bucketsPath);
    };
    
    default Children minBucket(String name,R field,String bucketsPath){
        return   minBucket(name,  field,bucketsPath);
    };
    
    default Children avgBucket(String name,R field,String bucketsPath){
        return   avgBucket(name,  field,bucketsPath);
    };
    
    default Children sumBucket(String name,R field,String bucketsPath){
        return   sumBucket(name,  field,bucketsPath);
    };
    
    default Children statsBucket(String name,R field,String bucketsPath){
        return   statsBucket(name,  field,bucketsPath);
    };
    
    default Children extendedStatsBucket(String name,R field,String bucketsPath){
        return   extendedStatsBucket(name,  field,bucketsPath);
    };
    
    default Children percentilesBucket(String name,R field,String bucketsPath){
        return   percentilesBucket(name,  field,bucketsPath);
    };
    
    default Children bucketScript(String name,R field,Map<String, String> bucketsPathsMap, EpScript script){
        return   bucketScript(name,  field,bucketsPathsMap,script);
    };
    
    default Children bucketScript(String name,R field, EpScript script, String... bucketsPaths){
        return   bucketScript(name,  field,script,bucketsPaths);
    };
    
    default Children bucketSelector(String name,R field,Map<String, String> bucketsPathsMap, EpScript script){
        return   bucketSelector(name,  field,bucketsPathsMap,script);
    };
    
    default Children bucketSelector(String name,R field, EpScript script, String... bucketsPaths){
        return   bucketSelector(name,  field,script,bucketsPaths);
    };
    
    default Children bucketSort(String name,R field,List<EpFieldSortBuilder> sorts){
        return   bucketSort(name,  field,sorts);
    };
    
    default Children bucketSort(String name,R field,int from, int size, boolean asc, String... orderColumns){
        return   bucketSort(name,  field,from,size,asc,orderColumns);
    };
    
    default Children cumulativeSum(String name,R field,String bucketsPath){
        return   cumulativeSum(name,  field,bucketsPath);
    };
    
    default Children diff(String name,R field,String bucketsPath){
        return   diff(name,  field,bucketsPath);
    };
    
    default Children movingFunction(String name,R field,EpScript script, String bucketsPaths, int window){
        return   movingFunction(name,  field,script,bucketsPaths,window);
    };
    
    
    
    
    //============================================================================= field和consumer
    //    /**
    //     * 统计
    //     *
    //     * @param field 名字
    //     * @return {@link Children}
    //     */
    //    default Children count(R field, Consumer<Children> subAgg){
    //        return   count(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Avg} aggregation with the given field.
    //     */
    //    default Children avg(R field,Consumer<Children> subAgg){
    //        return   avg(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Avg} aggregation with the given field.
    //     */
    //    default Children weightedAvg(R field,Consumer<Children> subAgg){
    //        return   weightedAvg(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Max} aggregation with the given field.
    //     */
    //    default Children max(R field,Consumer<Children> subAgg){
    //        return   max(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Min} aggregation with the given field.
    //     */
    //    default Children min(R field,Consumer<Children> subAgg){
    //        return   min(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Sum} aggregation with the given field.
    //     */
    //    default Children sum(R field,Consumer<Children> subAgg){
    //        return   sum(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Stats} aggregation with the given field.
    //     */
    //    default Children stats(R field,Consumer<Children> subAgg){
    //        return   stats(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link ExtendedStats} aggregation with the given field.
    //     */
    //    default Children extendedStats(R field,Consumer<Children> subAgg){
    //        return   extendedStats(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Filter} aggregation with the given field.
    //     */
    //    default Children filter(R field,Consumer<Children> subAgg, Supplier<EsWrapper<T>> filterQuery){
    //        return   filter(null,field,subAgg,filterQuery);
    //    };
    //
    //    /**
    //     * Create a new {@link Filters} aggregation with the given field.
    //     */
    //    default Children filters(R field,Consumer<Children> subAgg, FiltersAggregator.KeyedFilter... filters){
    //        return   filters(null,field,subAgg,filters);
    //    };
    //
    //    /**
    //     * Create a new {@link Filters} aggregation with the given field.
    //     */
    //    default Children filters(R field,Consumer<Children> subAgg, Supplier<EsWrapper<T>>... filterQuery){
    //        return   filters(null,field,subAgg,filterQuery);
    //    };
    //
    //
    //    /**
    //     * Create a new {@link AdjacencyMatrix} aggregation with the given field and separator
    //     */
        //    default Children adjacencyMatrix(R field,Consumer<Children> subAgg, String separator, Map<String, Supplier<EsWrapper<T>>> adjacencyMatrixMap){
        //       return   adjacencyMatrix(null,field,subAgg,separator,adjacencyMatrixMap);
        //     };

    //    /**
    //     * Create a new {@link Sampler} aggregation with the given field.
    //     */
    //    default Children sampler(R field,Consumer<Children> subAgg){
    //        return   sampler(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Sampler} aggregation with the given field.
    //     */
    //    default Children diversifiedSampler(R field,Consumer<Children> subAgg){
    //        return   diversifiedSampler(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Global} aggregation with the given field.
    //     */
    //    default Children global(R field,Consumer<Children> subAgg){
    //        return   global(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Missing} aggregation with the given field.
    //     */
    //    default Children missing(R field,Consumer<Children> subAgg){
    //        return   missing(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Nested} aggregation with the given field.
    //     */
    default Children nested(R field, String path,Consumer<Children> subAgg){
        return   nested(null,field,path,subAgg);
    }
    
    
    
    //
    //    /**
    //     * Create a new {@link ReverseNested} aggregation with the given field.
    //     */
    //    default Children reverseNested(R field,Consumer<Children> subAgg){
    //        return   reverseNested(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link GeoDistance} aggregation with the given field.
    //     */
    //    default Children geoDistance(R field, GeoPoint origin,Consumer<Children> subAgg){
    //        return   geoDistance(null,field,origin,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Histogram} aggregation with the given field.
    //     */
    //    default Children histogram(R field,Consumer<Children> subAgg){
    //        return   histogram(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link InternalGeoHashGrid} aggregation with the given field.
    //     */
    //    default Children geohashGrid(R field,Consumer<Children> subAgg){
    //        return   geohashGrid(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link InternalGeoTileGrid} aggregation with the given field.
    //     */
    //    default Children geotileGrid(R field,Consumer<Children> subAgg){
    //        return   geotileGrid(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link SignificantTerms} aggregation with the given field.
    //     */
    //    default Children significantTerms(R field,Consumer<Children> subAgg){
    //        return   significantTerms(null,field,subAgg);
    //    };
    //
    //
    //    /**
    //     * Create a new {@link SignificantTextAggregationBuilder} aggregation with the given field and text field field
    //     */
    //    default Children significantText(R field,Consumer<Children> subAgg){
    //        return   significantText(null,field,subAgg);
    //    };
    //
    //
    //    /**
    //     * Create a new {@link DateHistogramAggregationBuilder} aggregation with the given
    //     * field.
    //     */
    //    default Children dateHistogram(R field,DateHistogramInterval dateHistogramInterval,Consumer<Children> subAgg){
    //        return   dateHistogram(null,field,dateHistogramInterval,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Range} aggregation with the given field.
    //     */
    //    default Children range(R field,String key,double from,double to,Consumer<Children> subAgg){
    //        return   range(null,field,key,from,to,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link DateRangeAggregationBuilder} aggregation with the
    //     * given field.
    //     */
    //    default Children dateRange(R field,String key,String from,String to,Consumer<Children> subAgg){
    //        return   dateRange(null,field,key,from,to,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link IpRangeAggregationBuilder} aggregation with the
    //     * given field.
    //     */
    //    default Children ipRange(R field,Consumer<Children> subAgg){
    //        return   ipRange(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Terms} aggregation with the given field.
    //     */
    //    default Children terms(R field,Consumer<Children> subAgg){
    //        return   terms(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Percentiles} aggregation with the given field.
    //     */
    //    default Children percentiles(R field,Consumer<Children> subAgg){
    //        return   percentiles(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link PercentileRanks} aggregation with the given field.
    //     */
    //    default Children percentileRanks(R field,Consumer<Children> subAgg, double[] values){
    //        return   percentileRanks(null,field,subAgg,values);
    //    };
    //
    //    /**
    //     * Create a new {@link MedianAbsoluteDeviation} aggregation with the given field
    //     */
    //    default Children medianAbsoluteDeviation(R field,Consumer<Children> subAgg){
    //        return   medianAbsoluteDeviation(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link Cardinality} aggregation with the given field.
    //     */
    //    default Children cardinality(R field,Consumer<Children> subAgg){
    //        return   cardinality(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link TopHits} aggregation with the given field.
    //     */
    //    default Children topHits(R field,Consumer<Children> subAgg){
    //        return   topHits(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link GeoBounds} aggregation with the given field.
    //     */
    //    default Children geoBounds(R field,Consumer<Children> subAgg){
    //        return   geoBounds(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link GeoCentroid} aggregation with the given field.
    //     */
    //    default Children geoCentroid(R field,Consumer<Children> subAgg){
    //        return   geoCentroid(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link ScriptedMetric} aggregation with the given field.
    //     */
    //    default Children scriptedMetric(R field,Consumer<Children> subAgg){
    //        return   scriptedMetric(null,field,subAgg);
    //    };
    //
    //    /**
    //     * Create a new {@link CompositeAggregationBuilder} aggregation with the given field.
    //     */
    //    default Children composite(R field,Consumer<Children> subAgg, List<CompositeValuesSourceBuilder<?>> sources){
    //        return   composite(null,field,subAgg,sources);
    //    };
    //
    //
    //    /**
    //     * piple的方法
    //     */
    //    default Children derivative(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   derivative(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children maxBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   maxBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children minBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   minBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children avgBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   avgBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children sumBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   sumBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children statsBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   statsBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children extendedStatsBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   extendedStatsBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children percentilesBucket(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   percentilesBucket(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children bucketScript(R field,Consumer<Children> subAgg,Map<String, String> bucketsPathsMap, Script script){
    //        return   bucketScript(null,field,subAgg,bucketsPathsMap,script);
    //    };
    //
    //    default Children bucketScript(R field,Consumer<Children> subAgg, Script script, String... bucketsPaths){
    //        return   bucketScript(null,field,subAgg,script,bucketsPaths);
    //    };
    //
    //    default Children bucketSelector(R field,Consumer<Children> subAgg,Map<String, String> bucketsPathsMap, Script script){
    //        return   bucketSelector(null,field,subAgg,bucketsPathsMap,script);
    //    };
    //
    //    default Children bucketSelector(R field,Consumer<Children> subAgg, Script script, String... bucketsPaths){
    //        return   bucketSelector(null,field,subAgg,script,bucketsPaths);
    //    };
    //
    //    default Children bucketSort(R field,Consumer<Children> subAgg,List<FieldSortBuilder> sorts){
    //        return   bucketSort(null,field,subAgg,sorts);
    //    };
    //
    //    default Children bucketSort(R field,Consumer<Children> subAgg,int from, int size, boolean asc, String... orderColumns){
    //        return   bucketSort(null,field,subAgg,from,size,asc,orderColumns);
    //    };
    //
    //    default Children cumulativeSum(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   cumulativeSum(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children diff(R field,Consumer<Children> subAgg,String bucketsPath){
    //        return   diff(null,field,subAgg,bucketsPath);
    //    };
    //
    //    default Children movingFunction(R field,Consumer<Children> subAgg,Script script, String bucketsPaths, int window){
    //        return   movingFunction(null,field,subAgg,script,bucketsPaths,window);
    //    };
}
