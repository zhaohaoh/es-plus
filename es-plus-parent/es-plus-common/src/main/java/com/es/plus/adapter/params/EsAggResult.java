package com.es.plus.adapter.params;

import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * es聚合结果
 *
 * @author hzh
 * @date 2024/03/21
 */
@Data
public class EsAggResult<T> {
    private Class<T> tClass;
    /**
     * count聚合后数据
     */
    private Map<String, Long> countMap;
    
    /**
     * topHits聚合后数据
     */
    private Map<String, List<T>> topHitsMap;
    
    /**
     * max聚合后统计
     */
    private Map<String, Double> maxMap;
    
    /**
     * min聚合后统计
     */
    private Map<String, Double> minMap;
    
    /**
     * avg聚合后统计
     */
    private Map<String, Double> avgMap;
    
    /**
     * sum聚合后统计
     */
    private Map<String, Double> sumMap;
    
    /**
     * 汇总统计聚合
     */
    private Map<String, EsAggStats> statsMap;
    
    /**
     * term聚合后文档统计
     */
    private Long docCount;
    
    /**
     * 聚合单桶文档 当前级别count数据
     */
    private Map<String, Long> docCountMap;
    
    /**
     * 聚合多桶文档
     */
    private Map<String, Map<String, EsAggResult<T>>> multiBucketsMap;
    
    /**
     * 聚合单桶文档
     */
    private Map<String, Tuple<Long,EsAggResult<T>>> singleBucketsMap;
    
    /**
     * 框架暂未提供解析的agg
     */
    private Map<String, Aggregation> aggMap;
    
    /**
     * 获取多桶数据多级嵌套
     */
    public Map<String, EsAggResult<T>> getMultiBucketNestedMap(String aggName) {
        if (CollectionUtils.isEmpty(multiBucketsMap)) {
            return new HashMap<>();
        }
        Map<String, EsAggResult<T>> esAggResultMap = multiBucketsMap.get(aggName);
        if (esAggResultMap==null){
            return new HashMap<>();
        }
        return esAggResultMap;
    }
    
    
    /**
     * 获取多桶本级数据
     */
    public Map<String, Long> getMultiBucketMap(String aggName) {
        if (CollectionUtils.isEmpty(multiBucketsMap)) {
            return new HashMap<>();
        }
        Map<String, EsAggResult<T>> map = multiBucketsMap.get(aggName);
        Map<String, Long> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(map)) {
            map.forEach((k, v) -> {
                result.put(k, v.getDocCount());
            });
        }
        return result;
    }
    
    
    /**
     * 获取多桶本级数据
     */
    public Map<String, Long> getMultiBucketMap() {
        if (CollectionUtils.isEmpty(multiBucketsMap)) {
            return new HashMap<>();
        }
        Map<String, Long> result = new HashMap<>();
        Collection<Map<String, EsAggResult<T>>> values = multiBucketsMap.values();
        for (Map<String, EsAggResult<T>> map : values) {
            if (!CollectionUtils.isEmpty(map)) {
                map.forEach((k, v) -> {
                    result.put(k, v.getDocCount());
                });
            }
        }
        return result;
    }
    
    
    /**
     * 获取单桶数据嵌套
     */
    public  EsAggResult<T> getSingleBucketNested(String aggName) {
        if (CollectionUtils.isEmpty(singleBucketsMap)) {
            return new EsAggResult<>();
        }
        Tuple<Long, EsAggResult<T>> aggResultTuple = singleBucketsMap.get(aggName);
        if (aggResultTuple==null){
            return new EsAggResult<>();
        }
        return aggResultTuple.v2();
    }
    
    /**
     * 获取单桶count数据
     */
    public Long getSingleBucketDocCount(String aggName) {
        if (CollectionUtils.isEmpty(singleBucketsMap)) {
            return null;
        }
        Tuple<Long, EsAggResult<T>> aggResultTuple = singleBucketsMap.get(aggName);
        if (aggResultTuple==null){
            return null;
        }
        return  aggResultTuple.v1();
    }
    
    
    /**
     * 获取第一个count值
     */
    public Long getFirstCount() {
         if (CollectionUtils.isEmpty(countMap)){
           return null;
          }
        Long result = countMap.values().stream().findFirst().orElse(null);
        return result;
    }
    
    /**
     * 获取第一个sum值
     */
    public Double getFirstSum() {
        if (CollectionUtils.isEmpty(sumMap)){
            return null;
        }
        Double result = sumMap.values().stream().findFirst().orElse(null);
        return result;
    }
    
    /**
     * 获取第一个Max
     */
    public Double getFirstMax() {
        if (CollectionUtils.isEmpty(maxMap)){
            return null;
        }
        Double result = maxMap.values().stream().findFirst().orElse(null);
        return result;
    }
    
    /**
     * 获取第一个Min
     */
    public Double getFirstMin() {
        if (CollectionUtils.isEmpty(minMap)){
            return null;
        }
        Double result = minMap.values().stream().findFirst().orElse(null);
        return result;
    }
    
    /**
     * 获取第一个avg
     */
    public Double getFirstAvg() {
        if (CollectionUtils.isEmpty(avgMap)){
            return null;
        }
        Double result = avgMap.values().stream().findFirst().orElse(null);
        return result;
    }
    
    
    /**
     * 获取count值
     */
    public Long getCount(String aggName) {
        if (CollectionUtils.isEmpty(countMap)){
            return null;
        }
        Long result = countMap.get(aggName);
        return result;
    }
    
    /**
     * 获取sum值
     */
    public Double getSum(String aggName) {
        if (CollectionUtils.isEmpty(sumMap)){
            return null;
        }
        Double result = sumMap.get(aggName);
        return result;
    }
    
    /**
     * 获取Max
     */
    public Double getMax(String aggName) {
        if (CollectionUtils.isEmpty(maxMap)){
            return null;
        }
        Double result = maxMap.get(aggName);
        return result;
    }
    
    /**
     * 获取Min
     */
    public Double getMin(String aggName) {
        if (CollectionUtils.isEmpty(minMap)){
            return null;
        }
        Double result = minMap.get(aggName);
        return result;
    }
    
    /**
     * 获取avg
     */
    public Double getAvg(String aggName) {
        if (CollectionUtils.isEmpty(avgMap)){
            return null;
        }
        Double result = avgMap.get(aggName);
        return result;
    }
    
//    ---------------------------------------------------------- function方法获取aggName
    /**
     * 获取多桶数据多级嵌套
     */
    public Map<String, EsAggResult<T>> getMultiBucketNestedMap(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(multiBucketsMap)) {
            return new HashMap<>();
        }
        Map<String, EsAggResult<T>> esAggResultMap = multiBucketsMap.get(getAggregationField(aggName));
        if (esAggResultMap==null){
            return new HashMap<>();
        }
        return esAggResultMap;
    }
    
    
    /**
     * 获取多桶本级数据
     */
    public Map<String, Long> getMultiBucketMap(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(multiBucketsMap)) {
            return new HashMap<>();
        }
        Map<String, EsAggResult<T>> map = multiBucketsMap.get(getAggregationField(aggName));
        Map<String, Long> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(map)) {
            map.forEach((k, v) -> {
                result.put(k, v.getDocCount());
            });
        }
        return result;
    }
    
    
    /**
     * 获取单桶数据嵌套
     */
    public  EsAggResult<T> getSingleBucketNested(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(singleBucketsMap)) {
            return new EsAggResult<>();
        }
        Tuple<Long, EsAggResult<T>> aggResultTuple = singleBucketsMap.get(getAggregationField(aggName));
        if (aggResultTuple==null){
            return new EsAggResult<>();
        }
        return aggResultTuple.v2();
    }
    
    /**
     * 获取单桶count数据
     */
    public Long getSingleBucketDocCount(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(singleBucketsMap)) {
            return null;
        }
        Tuple<Long, EsAggResult<T>> aggResultTuple = singleBucketsMap.get(getAggregationField(aggName));
        if (aggResultTuple==null){
            return null;
        }
        return  aggResultTuple.v1();
    }
    
    /**
     * 获取count值
     */
    public Long getCount(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(countMap)){
            return null;
        }
        Long result = countMap.get(getAggregationField(aggName));
        return result;
    }
    
    /**
     * 获取sum值
     */
    public Double getSum(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(sumMap)){
            return null;
        }
        Double result = sumMap.get(getAggregationField(aggName));
        return result;
    }
    
    /**
     * 获取Max
     */
    public Double getMax(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(maxMap)){
            return null;
        }
        Double result = maxMap.get(getAggregationField(aggName));
        return result;
    }
    
    /**
     * 获取Min
     */
    public Double getMin(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(minMap)){
            return null;
        }
        Double result = minMap.get(getAggregationField(aggName));
        return result;
    }
    
    /**
     * 获取avg
     */
    public Double getAvg(SFunction<T,?> aggName) {
        if (CollectionUtils.isEmpty(avgMap)){
            return null;
        }
        
        Double result = avgMap.get(getAggregationField(aggName));
        return result;
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
