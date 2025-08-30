package com.es.plus.adapter.pojo.es;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义查询构建器，实现类似QueryBuilder的功能，完全解耦于Elasticsearch
 */
public class EpQueryBuilder {
    
    private String name;
    private String type;
    private float boost = 1.0f;
    private String queryName;
    private final Map<String, Object> parameters = new HashMap<>();
    
    public EpQueryBuilder() {
    }
    
    public EpQueryBuilder(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * 设置查询名称
     * @param name 查询名称
     * @return this
     */
    public EpQueryBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * 设置查询类型
     * @param type 查询类型
     * @return this
     */
    public EpQueryBuilder type(String type) {
        this.type = type;
        return this;
    }
    
    /**
     * 设置boost值
     * @param boost boost值
     * @return this
     */
    public EpQueryBuilder boost(float boost) {
        this.boost = boost;
        return this;
    }
    
    /**
     * 设置查询名称
     * @param queryName 查询名称
     * @return this
     */
    public EpQueryBuilder queryName(String queryName) {
        this.queryName = queryName;
        return this;
    }
    
    /**
     * 添加参数
     * @param key 参数名
     * @param value 参数值
     * @return this
     */
    public EpQueryBuilder param(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
    
    /**
     * 获取查询名称
     * @return 查询名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取查询类型
     * @return 查询类型
     */
    public String getType() {
        return type;
    }
    
    /**
     * 获取boost值
     * @return boost值
     */
    public float getBoost() {
        return boost;
    }
    
    /**
     * 获取查询名称
     * @return 查询名称
     */
    public String getQueryName() {
        return queryName;
    }
    
    /**
     * 获取参数
     * @return 参数映射
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
  
}