package com.es.plus.web.pojo;

import lombok.Data;

@Data
public class EsReindexRequst {
    
    /**
     * 源索引
     */
    private String sourceIndex;
    /**
     * 目标索引
     */
    private String targetIndex;
    /**
     * 是否异步 重建索引
     */
    private String async;
}
