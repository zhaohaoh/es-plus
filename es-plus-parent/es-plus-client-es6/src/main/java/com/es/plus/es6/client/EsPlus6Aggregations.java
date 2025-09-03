package com.es.plus.es6.client;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsAggResult;
import com.es.plus.adapter.params.EsAggStats;
import com.es.plus.adapter.params.Tuple;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.constant.EsConstant;
import com.es.plus.es6.util.SearchHitsUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.stats.ParsedStats;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.weighted_avg.WeightedAvg;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: hzh
 * @Date: 20.2.7/21 12:31
 */
public class EsPlus6Aggregations<T> implements EsAggResponse<T> {
    
    private Aggregations aggregations;
    
    private Class<T> tClass;
    
    @Override
    public void settClass(Class<T> tClass) {
        this.tClass = tClass;
    }
    
    @Override
    public Object getAggregations() {
        return aggregations;
    }
    
    @Override
    public void setAggregations(Object aggregations) {
        this.aggregations = (Aggregations) aggregations;
    }
    
    
    /**
     * 根据名称获取聚合
     */
    public Aggregation getAggregation(String name) {
        return aggregations.get(name);
    }
    
    
    /**
     * 得到esplus封装的map
     */
    @Override
    public EsAggResult<T> getEsAggResult() {
        return getResult(aggregations);
    }
    
    private EsAggResult<T> getResult(Aggregations aggregations) {
        
        
        if (aggregations == null) {
            return new EsAggResult<>();
        }
        
        EsAggResult<T> esAgg = new EsAggResult<>();
        
        //多桶多层聚合
        Map<String,Map<String, EsAggResult<T>>> multiBucketMap= null;
        //单桶多层聚合
        //单桶多层聚合
        Map<String, Tuple<Long,EsAggResult<T>>> singleBucketMap = null;
        //单文档doc的设置
        Map<String,Long> docMap = null;
        
        Map<String, Double> sumMap = null;
        
        Map<String, Double> avgMap = null;
        
        Map<String, Long> countMap = null;
        
        Map<String, Double> maxMap= null;
        
        Map<String, Double> minMap= null;
        
        Map<String, List<T>> topHitsMap= null;
        
        Map<String, Object> aggMap = null;
        
        Map<String, EsAggStats> statsMap = null;
        
        Map<String, Aggregation> aggregationsMap = aggregations.asMap();
        
        if (CollectionUtils.isEmpty(aggregationsMap)){
            return new EsAggResult<>();
        }
        
        for (Map.Entry<String, Aggregation> entry : aggregationsMap.entrySet()) {
            String aggName = entry.getKey();
            Aggregation agg = entry.getValue();
            
            //单桶聚合
            if (agg instanceof SingleBucketAggregation){
                if (singleBucketMap==null){
                    singleBucketMap=new HashMap<>();
                }
                SingleBucketAggregation aggregation = (SingleBucketAggregation) agg;
                long docCount = aggregation.getDocCount();
                EsAggResult<T> subAgg = getResult(aggregation.getAggregations());
                singleBucketMap.put(aggName,Tuple.tuple(docCount,subAgg));
            }
            
            //多个桶聚合
            else if (agg instanceof MultiBucketsAggregation){
                if (multiBucketMap==null){
                    multiBucketMap=new LinkedHashMap<>();
                }
                MultiBucketsAggregation aggregation = (MultiBucketsAggregation) agg;
                Map<String, EsAggResult<T>> data = new LinkedHashMap<>();
                multiBucketMap.put(aggName,data);
                List<? extends MultiBucketsAggregation.Bucket> buckets = aggregation.getBuckets();
                for (MultiBucketsAggregation.Bucket bucket : buckets) {
                    Aggregations bucketAggregations = bucket.getAggregations();
                    EsAggResult<T> subAgg = getResult( bucketAggregations);
                    long docCount = bucket.getDocCount();
                    subAgg.setDocCount(docCount);
                    Object key = bucket.getKey();
                    String keyAsString = bucket.getKeyAsString();
                    if (key instanceof Map){
                        keyAsString = JsonUtils.toJsonStr(key);
                    }
                    data.put(keyAsString, subAgg);
                }
            }
            else if (agg instanceof Sum){
                if (sumMap==null){
                    sumMap=new HashMap<>();
                }
                sumMap.put(aggName,((Sum) agg).value());
            }
            else if (agg instanceof Avg){
                if (avgMap==null){
                    avgMap=new HashMap<>();
                }
                avgMap.put(aggName,((Avg) agg).value());
            }
            else if (agg instanceof ValueCount){
                if (countMap==null){
                    countMap=new HashMap<>();
                }
                countMap.put(aggName,((ValueCount) agg).getValue());
            }
            else  if (agg instanceof Max){
                if (maxMap==null){
                    maxMap=new HashMap<>();
                }
                maxMap.put(aggName,((Max) agg).value());
            }
            else  if (agg instanceof Min){
                if (minMap==null){
                    minMap=new HashMap<>();
                }
                minMap.put(aggName,((Min) agg).value());
            }
            else if (agg instanceof TopHits){
                if (topHitsMap==null){
                    topHitsMap=new HashMap<>();
                }
                SearchHits hits = ((TopHits) agg).getHits();
                List<T> result = SearchHitsUtil.parseList(tClass, hits.getHits());
                topHitsMap.put(aggName, result);
            }
            else if (agg instanceof ParsedStats){
                if (statsMap==null){
                    statsMap=new HashMap<>();
                }
                double avg = ((ParsedStats) agg).getAvg();
                long count = ((ParsedStats) agg).getCount();
                double max = ((ParsedStats) agg).getMax();
                double sum = ((ParsedStats) agg).getSum();
                double min = ((ParsedStats) agg).getMin();
                EsAggStats esAggStats = new EsAggStats();
                esAggStats.setAvg(avg);
                esAggStats.setCount(count);
                esAggStats.setMax(max);
                esAggStats.setSum(sum);
                esAggStats.setMin(min);
                statsMap.put(aggName,esAggStats);
            }
            
            //框架暂未解析的agg
            else {
                if (aggMap==null){
                    aggMap=new HashMap<>();
                }
                aggMap.put(aggName,agg);
            }
        }
        
        esAgg.setSingleBucketsMap(singleBucketMap);
        esAgg.setMultiBucketsMap(multiBucketMap);
        esAgg.setDocCountMap(docMap);
        esAgg.setSumMap(sumMap);
        esAgg.setMinMap(minMap);
        esAgg.setMaxMap(maxMap);
        esAgg.setCountMap(countMap);
        esAgg.setAvgMap(avgMap);
        esAgg.setTopHitsMap(topHitsMap);
        esAgg.setAggMap(aggMap);
        esAgg.setStatsMap(statsMap);
        
        return esAgg;
    }
    
    
    public Terms getTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }
    
    //    public RareTerms getRareTerms(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    //    }
    
    public Filters getFilters(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }
    
    public AdjacencyMatrix getAdjacencyMatrix(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }
    
    public SignificantTerms getSignificantTerms(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }
    
    public Histogram getHistogram(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }
    
    //    public GeoGrid getGeoGrid(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    //    }
    
    public Max getMax(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + MaxAggregationBuilder.NAME);
    }
    
    public Avg getAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AvgAggregationBuilder.NAME);
    }
    
    public Sum getSum(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public ValueCount getCount(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public WeightedAvg getWeightedAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    //    public BucketMetricValue getBucketMetricValue(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    //    }
 
    public Terms getTerms(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }
    
    //    public RareTerms getRareTerms(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    //    }
    
    public Filters getFilters(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }
    
    public AdjacencyMatrix getAdjacencyMatrix(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }
    
    public SignificantTerms getSignificantTerms(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }
    
    public Histogram getHistogram(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }
    
    //    public GeoGrid getGeoGrid(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    //    }
    
    public Max getMax(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + MaxAggregationBuilder.NAME);
    }
    
    public Avg getAvg(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + AvgAggregationBuilder.NAME);
    }
    
    public Sum getSum(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public ValueCount getCount(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public WeightedAvg getWeightedAvg(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    //    public BucketMetricValue getBucketMetricValue(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    //    }
    
    
    private String getAggregationField(SFunction<T, ?> sFunction) {
        String name = nameToString(sFunction);
        String keyword = GlobalParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }
    
    private String nameToString(SFunction<T, ?> function) {
        String fieldName = LambdaUtils.getFieldName(function);
        EsFieldInfo indexField = GlobalParamHolder.getIndexField(tClass, fieldName);
        return indexField != null && StringUtils.isNotBlank(indexField.getName()) ? indexField.getName() : fieldName;
    }
    
}
