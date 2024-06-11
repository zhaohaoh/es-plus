package com.es.plus.adapter.params;

import lombok.Getter;

/**
 * es聚合查询获取结果封装
 *
 * @author hzh
 * @date 2024/03/21
 */
@Getter
public class EsAggResultQuery {
    
    private String term;
    
    private String count;
    
    private String topHits;
    
    private String max;
    
    private String min;
    
    private String avg;
    
    private String sum;
    
    private EsAggResultQuery subQuery;
    
    
    public EsAggResultQuery term(String term) {
        this.term = term;
        return this;
    }
    
    public EsAggResultQuery count(String count) {
        this.count = count;
        return this;
    }
    
    public EsAggResultQuery topHits(String topHits) {
        this.topHits = topHits;
        return this;
    }
    public EsAggResultQuery max(String max) {
        this.max = max;
        return this;
    }
    
    public EsAggResultQuery min(String min) {
        this.min = min;
        return this;
    }
    
    public EsAggResultQuery avg(String avg) {
        this.avg = avg;
        return this;
    }
    
    public EsAggResultQuery sum(String sum) {
        this.sum = sum;
        return this;
    }
    
    public EsAggResultQuery subQuery() {
        this.subQuery = new EsAggResultQuery();
        return this.subQuery;
    }
    
    public static EsAggResultQuery  build() {
        return new EsAggResultQuery();
    }
    
}
