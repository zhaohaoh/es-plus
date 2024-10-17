package com.es.plus.adapter.params;

import lombok.Data;
import org.springframework.util.CollectionUtils;

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
    /**
     * count聚合后数据
     */
    private Map<String,Long> count;
    /**
     * topHits聚合后数据
     */
    private Map<String,List<T>> topHits;
    /**
     * max聚合后统计
     */
    private Map<String,Double> max;
    /**
     * min聚合后统计
     */
    private Map<String,Double> min;
    /**
     * avg聚合后统计
     */
    private Map<String,Double> avg;
    /**
     * sum聚合后统计
     */
    private Map<String,Double> sum;
    
    /**
     * term聚合后文档统计
     */
    private Long docCount;
   
    /**
     * 聚合term文档
     */
    private Map<String,Map<String, EsAggResult<T>>> esAggTermsMap;
    
    /**
     * 聚合term文档
     */
    private Map<String, EsAggResult<T>>  esAggFiltersMap;
    
    /**
     * 获取terms数据多级嵌套
     */
    public    Map<String, EsAggResult<T>> getTermsMap(String aggName) {
        return esAggTermsMap.get(aggName);
    }
    
    
    /**
     * 获取terms本级数据
     */
    public   Map<String, Long> getTermsLongMap(String aggName) {
        Map<String,EsAggResult<T>> map = esAggTermsMap.get(aggName);
        Map<String,Long> result = new HashMap<>();
       if (!CollectionUtils.isEmpty(map)){
           map.forEach((k,v)->{
               result.put(k,v.getDocCount());
           });
       }
       return result;
    }
    
    /**
     * 获取第一个count值
     */
    private   Long  getFirstCount() {
        Long result = count.values().stream().findFirst().orElse(0L);
        return result;
    }
    
    /**
     * 获取第一个sum值
     */
    private  Double getFirstSum() {
        Double result = sum.values().stream().findFirst().orElse(0D);
        return result;
    }
    
    /**
     * 获取第一个Max
     */
    private  Double getFirstMax() {
        Double result = max.values().stream().findFirst().orElse(0D);
        return result;
    }
    
    /**
     * 获取第一个Min
     */
    private  Double getFirstMin() {
        Double result = min.values().stream().findFirst().orElse(0D);
        return result;
    }
    /**
     * 获取第一个avg
     */
    private  Double getFirstAvg() {
        Double result = avg.values().stream().findFirst().orElse(0D);
        return result;
    }
    
}
