package com.es.plus.common.pojo.es;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义聚合构建器，实现类似BaseAggregationBuilder的功能
 */
@Data
public class EpAggBuilder {
    
    private String name;
    private String type;
    private Map<String, Object> parameters = new HashMap<>();
    private List<EpAggBuilder> subAggregation =new ArrayList<>();
    private Integer size;
    private Integer from;
    private EpSortBuilder epSortBuilder;
    private EpBucketOrder bucketOrder;
    private Object esOrginalAgg;
    public EpAggBuilder() {
    }
    
    public EpAggBuilder(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * 设置聚合名称
     * @param name 聚合名称
     * @return this
     */
    public EpAggBuilder name(String name) {
        this.name = name;
        return this;
    }
    /**
     * 设置聚合
     * @return this
     */
    public EpAggBuilder esOrginalAgg(Object esOrginalAgg) {
        this.esOrginalAgg = esOrginalAgg;
        return this;
    }
    
    /**
     * 设置聚合类型
     * @param type 聚合类型
     * @return this
     */
    public EpAggBuilder type(String type) {
        this.type = type;
        return this;
    }
    
    public EpAggBuilder size(int size) {
        this.size = size;
        return this;
    }
    
    public EpAggBuilder order(EpBucketOrder order) {
        this.bucketOrder = order;
        return this;
    }
    
    /**
     * 添加参数
     * @param key 参数名
     * @param value 参数值
     * @return this
     */
    public EpAggBuilder param(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
    
    /**
     * 设置子聚合
     * @param subAggregation 子聚合
     * @return this
     */
    public EpAggBuilder subAggregation(List<EpAggBuilder> subAggregation) {
        this.subAggregation.addAll(subAggregation);
        return this;
    }
 
    
    /**
     * 获取参数
     * @return 参数映射
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * 获取子聚合
     * @return 子聚合
     */
    public List<EpAggBuilder> getSubAggregation() {
        return subAggregation;
    }
    
  
    
    @Override
    public String toString() {
        return "CustomAggregationBuilder{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", parameters=" + parameters +
                ", subAggregation=" + subAggregation +
                '}';
    }
}