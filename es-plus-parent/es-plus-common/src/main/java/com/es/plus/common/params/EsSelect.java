package com.es.plus.common.params;

import lombok.Data;

@Data
public class EsSelect {
    //查询结果包含字段
    private String[] includes;
    //查询结果不包含字段
    private String[] excludes;
    
    /**
     * 是否拉取数据
     */
    private Boolean fetch ;

    private Float minScope;
    /**
     * 跟踪查询分数
     */
    private Boolean trackScores ;
    
    /**
     * 精确跟踪总命中数
     */
    private Boolean trackTotalHits ;
    /**
     * 精确跟踪总命中数
     */
    private Integer trackTotalHitsCount;
}
