package com.es.plus.client;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.es.plus.common.params.EsAggResponse;
import com.es.plus.common.params.EsAggResult;
import com.es.plus.common.params.EsAggStats;
import com.es.plus.common.params.Tuple;
import com.es.plus.common.properties.EsFieldInfo;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.tools.LambdaUtils;
import com.es.plus.common.tools.SFunction;
import com.es.plus.constant.EsConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author: hzh
 * @Date: 2022/6/14 12:31
 */

public class Es8PlusAggregations<T> implements EsAggResponse<T> {
    private Map<String, Aggregate> aggregations;
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
        this.aggregations = (Map<String, Aggregate>) aggregations;
    }
    
    
    /**
     * 根据名称获取聚合
     */
    public Aggregate get(String name) {
        return aggregations.get(name);
    }
    
    /**
     * 得到esplus封装的map
     */
    @Override
    public EsAggResult<T> getEsAggResult() {
        return getResult(aggregations);
    }
    
    private EsAggResult<T> getResult(Map<String, Aggregate> aggregations) {
        
        if (aggregations == null) {
            return new EsAggResult<>();
        }
        
        EsAggResult<T> esAgg = new EsAggResult<>();
        esAgg.setTClass(tClass);
        
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
        
        if (CollectionUtils.isEmpty(aggregations)){
            return new EsAggResult<>();
        }
        
        for (Map.Entry<String, Aggregate> entry : aggregations.entrySet()) {
            String aggName = entry.getKey();
            Aggregate agg = entry.getValue();
            Object o = agg._get();
            //单桶聚合
            if (o instanceof SingleBucketAggregateBase) {
                if (singleBucketMap==null){
                    singleBucketMap=new HashMap<>();
                }
                
                long docCount = ((SingleBucketAggregateBase) o).docCount();
                EsAggResult<T> subAgg = getResult(((SingleBucketAggregateBase) o).aggregations());
                singleBucketMap.put(aggName,Tuple.tuple(docCount,subAgg));
            }
            
            //多个桶聚合
            else if (o instanceof MultiBucketAggregateBase) {
                if (multiBucketMap==null){
                    multiBucketMap=new LinkedHashMap<>();
                }
                MultiBucketAggregateBase<?> aggregation = ((MultiBucketAggregateBase<?>) o);
                Map<String, EsAggResult<T>> data = new LinkedHashMap<>();
                multiBucketMap.put(aggName,data);
                Buckets<?> buckets = aggregation.buckets();
                List<?> array = buckets.array();

                // 处理不同类型的bucket
                for (Object bucketObj : array) {
                    if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = String.valueOf(bucket.key());
                        data.put(keyAsString, subAgg);
                    } else if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = String.valueOf(bucket.key());
                        data.put(keyAsString, subAgg);
                    } else if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.DoubleTermsBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.DoubleTermsBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.DoubleTermsBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = String.valueOf(bucket.key());
                        data.put(keyAsString, subAgg);
                    } else if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = bucket.keyAsString() != null ? bucket.keyAsString() : String.valueOf(bucket.key());
                        data.put(keyAsString, subAgg);
                    } else if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = bucket.keyAsString() != null ? bucket.keyAsString() : String.valueOf(bucket.key());
                        data.put(keyAsString, subAgg);
                    } else if (bucketObj instanceof co.elastic.clients.elasticsearch._types.aggregations.MultiTermsBucket) {
                        co.elastic.clients.elasticsearch._types.aggregations.MultiTermsBucket bucket =
                            (co.elastic.clients.elasticsearch._types.aggregations.MultiTermsBucket) bucketObj;
                        Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                        EsAggResult<T> subAgg = getResult(bucketAggregations);
                        long docCount = bucket.docCount();
                        subAgg.setDocCount(docCount);
                        String keyAsString = bucket.keyAsString();
                        data.put(keyAsString, subAgg);
                    }
                    // 可以继续添加其他类型的bucket处理
                }
            }
            else if (agg.isSum()) {
                if (sumMap==null){
                    sumMap=new HashMap<>();
                }
                sumMap.put(aggName, agg.sum().value());
            }
            else if (agg.isAvg()) {
                if (avgMap==null){
                    avgMap=new HashMap<>();
                }
                avgMap.put(aggName, agg.avg().value());
            }
            else if (agg.isValueCount()) {
                if (countMap==null){
                    countMap=new HashMap<>();
                }
                countMap.put(aggName, (long) agg.valueCount().value());
            }
            else if (agg.isMax()) {
                if (maxMap==null){
                    maxMap=new HashMap<>();
                }
                maxMap.put(aggName, agg.max().value());
            }
            else if (agg.isMin()) {
                if (minMap==null){
                    minMap=new HashMap<>();
                }
                minMap.put(aggName, agg.min().value());
            }
            else if (agg.isTopHits()) {
                if (topHitsMap==null){
                    topHitsMap=new HashMap<>();
                }
                TopHitsAggregate hitsAggregate = agg.topHits();
                HitsMetadata<JsonData> hits2 = hitsAggregate.hits();
                List<Hit<JsonData>> hits = hits2.hits();
                
                // 简化处理，实际可能需要更复杂的转换
                List<T> result = hits.stream()
                        .filter(hit -> hit.source() != null)
                        .map(Hit::source).map(jsonData -> jsonData.to(tClass))
                        .collect(Collectors.toList());
                topHitsMap.put(aggName, result);
            }
            else if (agg.isStats()) {
                if (statsMap==null){
                    statsMap=new HashMap<>();
                }
                StatsAggregate stats = agg.stats();
                EsAggStats esAggStats = new EsAggStats();
                esAggStats.setAvg(stats.avg());
                esAggStats.setCount(stats.count());
                esAggStats.setMax(stats.max());
                esAggStats.setSum(stats.sum());
                esAggStats.setMin(stats.min());
                statsMap.put(aggName,esAggStats);
            }
            else if (agg.isExtendedStats()) {
                if (statsMap==null){
                    statsMap=new HashMap<>();
                }
                ExtendedStatsAggregate extendedStats = agg.extendedStats();
                EsAggStats esAggStats = new EsAggStats();
                esAggStats.setAvg(extendedStats.avg());
                esAggStats.setCount(extendedStats.count());
                esAggStats.setMax(extendedStats.max());
                esAggStats.setSum(extendedStats.sum());
                esAggStats.setMin(extendedStats.min());
                // 可以扩展EsAggStats类来支持extended stats的其他字段
                statsMap.put(aggName,esAggStats);
            }
            else if (agg.isCardinality()) {
                if (countMap==null){
                    countMap=new HashMap<>();
                }
                countMap.put(aggName, agg.cardinality().value());
            }
            else if (agg.isMissing()) {
                if (docMap==null){
                    docMap=new HashMap<>();
                }
                docMap.put(aggName, agg.missing().docCount());
            }
//            else if (agg.isPercentiles()) {
//                if (aggMap==null){
//                    aggMap=new HashMap<>();
//                }
//                aggMap.put(aggName, agg.percentiles());
//            }
//            else if (agg.isPercentileRanks()) {
//                if (aggMap==null){
//                    aggMap=new HashMap<>();
//                }
//                aggMap.put(aggName, agg.percentileRanks());
//            }
            else if (agg.isHistogram()) {
                if (multiBucketMap==null){
                    multiBucketMap=new LinkedHashMap<>();
                }
                HistogramAggregate histogram = agg.histogram();
                Map<String, EsAggResult<T>> data = new LinkedHashMap<>();
                multiBucketMap.put(aggName, data);

                List<co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket> buckets = histogram.buckets().array();
                for (co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket bucket : buckets) {
                    Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                    EsAggResult<T> subAgg = getResult(bucketAggregations);
                    long docCount = bucket.docCount();
                    subAgg.setDocCount(docCount);
                    String keyAsString = bucket.keyAsString() != null ? bucket.keyAsString() : String.valueOf(bucket.key());
                    data.put(keyAsString, subAgg);
                }
            }
            else if (agg.isDateHistogram()) {
                if (multiBucketMap==null){
                    multiBucketMap=new LinkedHashMap<>();
                }
                DateHistogramAggregate dateHistogram = agg.dateHistogram();
                Map<String, EsAggResult<T>> data = new LinkedHashMap<>();
                multiBucketMap.put(aggName, data);

                List<co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket> buckets = dateHistogram.buckets().array();
                for (co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket bucket : buckets) {
                    Map<String, Aggregate> bucketAggregations = bucket.aggregations();
                    EsAggResult<T> subAgg = getResult(bucketAggregations);
                    long docCount = bucket.docCount();
                    subAgg.setDocCount(docCount);
                    String keyAsString = bucket.keyAsString() != null ? bucket.keyAsString() : String.valueOf(bucket.key());
                    data.put(keyAsString, subAgg);
                }
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
    public Aggregate getTerms(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "terms");
        return aggregate;
    }
    public FiltersAggregate getFilters(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "filters");
        return aggregate != null && aggregate.isFilters() ? aggregate.filters() : null;
    }
    
    public AdjacencyMatrixAggregate getAdjacencyMatrix(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "adjacency_matrix");
        return aggregate != null && aggregate.isAdjacencyMatrix() ? aggregate.adjacencyMatrix() : null;
    }
    
    public Aggregate getSignificantTerms(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "significant_terms");
        return aggregate;
    }
    
    public HistogramAggregate getHistogram(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "histogram");
        return aggregate != null && aggregate.isHistogram() ? aggregate.histogram() : null;
    }
    
    public MaxAggregate getMax(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "max");
        return aggregate != null && aggregate.isMax() ? aggregate.max() : null;
    }
    
    public AvgAggregate getAvg(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "avg");
        return aggregate != null && aggregate.isAvg() ? aggregate.avg() : null;
    }
    
    public SumAggregate getSum(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "sum");
        return aggregate != null && aggregate.isSum() ? aggregate.sum() : null;
    }
    
    public ValueCountAggregate getCount(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "value_count");
        return aggregate != null && aggregate.isValueCount() ? aggregate.valueCount() : null;
    }
    
    public WeightedAvgAggregate getWeightedAvg(SFunction<T, ?> name) {
        Aggregate aggregate = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + "weighted_avg");
        return aggregate != null && aggregate.isWeightedAvg() ? aggregate.weightedAvg() : null;
    }
    
    /**
     * 得到聚合的map
     */
    public Map<String, Long> getTermsAsMap(String name) {
        Map<String, Long> data = new LinkedHashMap<>();
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "terms");
        
        if (aggregate == null || !aggregate.isSterms()) {
            return data;
        }
        
        TermsAggregateBase terms = (StringTermsAggregate) aggregate._get();
        List<? extends TermsBucketBase> buckets = Collections.emptyList();
        List<? extends MultiTermsBucket> multiBuckets = Collections.emptyList();
        // 处理不同类型的 Terms 聚合结果
        if (terms instanceof StringTermsAggregate) {
            buckets = ((StringTermsAggregate) terms).buckets().array();
        } else if (terms instanceof LongTermsAggregate) {
            buckets = ((LongTermsAggregate) terms).buckets().array();
        } else if (terms instanceof DoubleTermsAggregate) {
            buckets = ((DoubleTermsAggregate) terms).buckets().array();
        } else if (terms instanceof MultiTermsAggregate) {
            List<MultiTermsBucket> array = ((MultiTermsAggregate) terms).buckets().array();
            multiBuckets = array;
        } else {
            buckets = terms.buckets().array();
        }
        
       
        for (MultiTermsBucket multiBucket : multiBuckets) {
            String keyAsString = multiBucket.keyAsString();
            data.put(keyAsString, multiBucket.docCount());
        }
        return data;
    }
    
    
    public Aggregate getTerms(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "terms");
        return aggregate;
    }
    
    public FiltersAggregate getFilters(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "filters");
        return aggregate != null && aggregate.isFilters() ? aggregate.filters() : null;
    }
    
    public AdjacencyMatrixAggregate getAdjacencyMatrix(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "adjacency_matrix");
        return aggregate != null && aggregate.isAdjacencyMatrix() ? aggregate.adjacencyMatrix() : null;
    }
    
    public HistogramAggregate getHistogram(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "histogram");
        return aggregate != null && aggregate.isHistogram() ? aggregate.histogram() : null;
    }
    
    public MaxAggregate getMax(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "max");
        return aggregate != null && aggregate.isMax() ? aggregate.max() : null;
    }
    
    public AvgAggregate getAvg(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "avg");
        return aggregate != null && aggregate.isAvg() ? aggregate.avg() : null;
    }
    
    public SumAggregate getSum(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "sum");
        return aggregate != null && aggregate.isSum() ? aggregate.sum() : null;
    }
    
    public ValueCountAggregate getCount(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "value_count");
        return aggregate != null && aggregate.isValueCount() ? aggregate.valueCount() : null;
    }
    
    public WeightedAvgAggregate getWeightedAvg(String name) {
        Aggregate aggregate = aggregations.get(name + EsConstant.AGG_DELIMITER + "weighted_avg");
        return aggregate != null && aggregate.isWeightedAvg() ? aggregate.weightedAvg() : null;
    }
    
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