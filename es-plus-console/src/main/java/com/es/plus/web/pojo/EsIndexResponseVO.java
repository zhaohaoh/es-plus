package com.es.plus.web.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EsIndexResponseVO {
    private Map<String, Object> mappings;
    private String[] indices;
    private Map<String,List<String>> aliases;
    private Map<String, String> settings;
    
    private Map<String,Map<String, String>> flatMappings;
}
