package com.es.plus.common.params;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * es聚合查询获取结果封装
 *
 * @author hzh
 * @date 2024/03/21
 */
@Getter
public class EsAggResultQuery {
    
    private List<String> term = new ArrayList<>();
    
    private List<String> count = new ArrayList<>();
    
    private List<String> topHits = new ArrayList<>();
    
    private List<String> max = new ArrayList<>();
    
    private List<String> min = new ArrayList<>();
    
    private List<String> avg = new ArrayList<>();
    
    private List<String> sum = new ArrayList<>();
    
    private List<String> filters = new ArrayList<>();
    
    private EsAggResultQuery subQuery;
    
    
    public EsAggResultQuery term(String term) {
        this.term.add(term);
        return this;
    }
    
    public EsAggResultQuery count(String count) {
        this.count.add(count);
        return this;
    }
    
    public EsAggResultQuery topHits(String topHits) {
        this.topHits.add(topHits);
        return this;
    }
    
    public EsAggResultQuery max(String max) {
        this.max.add(max);
        return this;
    }
    
    public EsAggResultQuery min(String min) {
        this.min.add(min);
        return this;
    }
    
    public EsAggResultQuery avg(String avg) {
        this.avg.add(avg);
        return this;
    }
    
    public EsAggResultQuery sum(String sum) {
        this.sum.add(sum);
        return this;
    }
    public EsAggResultQuery filter(String filter,Consumer<EsAggResultQuery> consumer) {
        this.subQuery = new EsAggResultQuery();
        consumer.accept(subQuery);
        this.filters.add(filter);
        return this;
    }
    
 
    
    public static EsAggResultQuery build() {
        return new EsAggResultQuery();
    }
    
}
