package com.es.plus.common.pojo.es;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义BoolQueryBuilder，实现类似Elasticsearch BoolQueryBuilder的功能
 */
public class EpBoolQueryBuilder extends EpQueryBuilder {
    
    private List<EpQueryBuilder> mustClauses = new ArrayList<>();
    private List<EpQueryBuilder> mustNotClauses = new ArrayList<>();
    private List<EpQueryBuilder> filterClauses = new ArrayList<>();
    private List<EpQueryBuilder> shouldClauses = new ArrayList<>();
    
    public EpBoolQueryBuilder() {
        super(null, "bool");
    }
    
    /**
     * 添加must子句
     * @param query 查询构建器
     * @return this
     */
    public EpBoolQueryBuilder must(EpQueryBuilder query) {
        if (query != null) {
            mustClauses.add(query);
        }
        return this;
    }
    
    /**
     * 添加mustNot子句
     * @param query 查询构建器
     * @return this
     */
    public EpBoolQueryBuilder mustNot(EpQueryBuilder query) {
        if (query != null) {
            mustNotClauses.add(query);
        }
        return this;
    }
    
    /**
     * 添加filter子句
     * @param query 查询构建器
     * @return this
     */
    public EpBoolQueryBuilder filter(EpQueryBuilder query) {
        if (query != null) {
            filterClauses.add(query);
        }
        return this;
    }
    
    /**
     * 添加should子句
     * @param query 查询构建器
     * @return this
     */
    public EpBoolQueryBuilder should(EpQueryBuilder query) {
        if (query != null) {
            shouldClauses.add(query);
        }
        return this;
    }
    
    /**
     * 添加must子句
     *
     * @param query 查询构建器
     * @return this
     */
    public List<EpQueryBuilder> must() {
        return mustClauses;
        
    }
    
    /**
     * 添加must子句
     *
     * @param query 查询构建器
     * @return this
     */
    public List<EpQueryBuilder> mustNot() {
        return mustNotClauses;
        
    }
    
    /**
     * 添加must子句
     *
     * @return this
     */
    public List<EpQueryBuilder> filter() {
        return filterClauses;
    }
    
    /**
     * 添加must子句
     *
     * @return this
     */
    public List<EpQueryBuilder> should() {
        return shouldClauses;
    }
    
    /**
     * 获取must子句列表
     * @return must子句列表
     */
    public List<EpQueryBuilder> getMustClauses() {
        return mustClauses;
    }
    
    /**
     * 获取mustNot子句列表
     * @return mustNot子句列表
     */
    public List<EpQueryBuilder> getMustNotClauses() {
        return mustNotClauses;
    }
    
    /**
     * 获取filter子句列表
     * @return filter子句列表
     */
    public List<EpQueryBuilder> getFilterClauses() {
        return filterClauses;
    }
    
    /**
     * 获取should子句列表
     * @return should子句列表
     */
    public List<EpQueryBuilder> getShouldClauses() {
        return shouldClauses;
    }
    
    /**
     * 设置minimumShouldMatch
     * @param minimumShouldMatch 最小should匹配数
     * @return this
     */
    public EpBoolQueryBuilder minimumShouldMatch(int minimumShouldMatch) {
        this.param("minimum_should_match", minimumShouldMatch);
        return this;
    }
}