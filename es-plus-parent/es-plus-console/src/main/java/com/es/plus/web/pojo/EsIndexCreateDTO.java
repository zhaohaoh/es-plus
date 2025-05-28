package com.es.plus.web.pojo;

import com.es.plus.adapter.params.EsSettings;
import lombok.Data;

import java.util.Map;

@Data
public class EsIndexCreateDTO {
    
    private String indexName;
    
    private String alias;
    
    private EsSettings esSettings;
    
    private Map<String, Object> mapping;
    
}
