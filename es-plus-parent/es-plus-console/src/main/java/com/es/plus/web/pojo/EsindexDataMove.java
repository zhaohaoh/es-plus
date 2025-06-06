package com.es.plus.web.pojo;

import com.es.plus.adapter.params.EsOrder;
import lombok.Data;

@Data
public class EsindexDataMove {
    
    /**
     * 来源客户端
     */
    private String sourceClient;
    
    /**
     * 目标客户端
     */
    private String targetClient;
    
    /**
     * 来源索引
     */
    private String sourceIndex;
    
    /**
     * 目标索引
     */
    private String targetIndex;
    
    
    /**
     * 同步数据量限制
     */
    private Integer maxSize;
    
    /**
     * 排序字段
     */
    private EsOrder sortFeild;
    
    
    /**
     * 睡眠时间
     */
    private Integer sleepTime = 2000;
    
}
