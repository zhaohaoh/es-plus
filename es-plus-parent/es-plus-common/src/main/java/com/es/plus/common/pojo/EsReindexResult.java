package com.es.plus.common.pojo;

import lombok.Data;

@Data
public class EsReindexResult {
    
    private String _id;
    
    private String reIndexName;
    
    private Integer processType;
}
