package com.es.plus.adapter.params;

import com.es.plus.adapter.util.JsonUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EsIndexResponse {
    
    private Map<String, Object> mappings;
    
    private String[] indices;
    
    private List<String> aliases;
    
    private Map<String, String> settings;
    
    public Map<String, Object> getMappings(String indexName) {
        Map map = (Map) mappings.get(indexName);
        return map;
    }
    
    public Map<String,String> getSetting(String indexName) {
        String setting = settings.get(indexName);
        if (setting!=null) {
            Map<String, String> settings = JsonUtils.toBean(setting, Map.class);
            return settings;
        }
        return new HashMap<>();
    }
}
