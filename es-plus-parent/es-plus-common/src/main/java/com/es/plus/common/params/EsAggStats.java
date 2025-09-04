package com.es.plus.common.params;

import lombok.Data;

/**
 * es聚合结果
 *
 * @author hzh
 * @date 2024/03/21
 */
@Data
public class EsAggStats {
    
    /**
     * count聚合后数据
     */
    private Long count;
    
    
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
    
    
}
