package com.es.plus.web.pojo;

import lombok.Data;

@Data
public class EsCopyRequest {
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
}
