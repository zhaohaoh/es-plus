package com.es.plus.adapter.params;

import lombok.Data;

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
    private Long count;
    /**
     * topHits聚合后数据
     */
    private List<T> topHits;
    /**
     * max聚合后统计
     */
    private Double max;
    /**
     * min聚合后统计
     */
    private Double min;
    /**
     * avg聚合后统计
     */
    private Double avg;
    /**
     * sum聚合后统计
     */
    private Double sum;
    
    /**
     * term聚合后文档统计
     */
    private Long termDocCount;
    
    /**
     * 聚合term文档
     */
    private Map<String, EsAggResult<T>> esAggTermsMap;
}
