package com.es.plus.common.pojo.es;

/**
 * 自定义ScoreMode枚举，用于替代org.apache.lucene.search.join.ScoreMode
 */
public enum EpScoreMode {
    /**
     * 不使用评分
     */
    None,
    
    /**
     * 使用评分
     */
    Avg,
    
    /**
     * 使用最高分
     */
    Max,
    
    /**
     * 使用总分
     */
    Total,
    
    /**
     * 使用最小分
     */
    Min;
    
 
}