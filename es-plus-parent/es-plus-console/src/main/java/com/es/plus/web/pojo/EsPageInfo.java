package com.es.plus.web.pojo;

import lombok.Data;

@Data
public class EsPageInfo {
    
 
    
    /**
     * 索引
     */
    private String sql;
    
    /**
     * 页
     */
    private Integer page;
    /**
     * 数
     */
    private String size;
}
