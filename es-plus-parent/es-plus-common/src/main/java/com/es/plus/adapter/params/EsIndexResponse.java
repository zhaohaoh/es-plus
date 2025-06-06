package com.es.plus.adapter.params;

import com.es.plus.adapter.util.JsonUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EsIndexResponse {
    
    private String[] indices;
    
    private Map<String, List<String>> aliases;
    
    private Map<String, Object> settingsObj;
    
    private Map<String, Object> mappings;
    
    private Map<String, String> settings;
    
    
    public Map<String, Object> getMappings(String indexName) {
        Map map = (Map) mappings.get(indexName);
        return map;
    }
    
    public Map<String,Object> getSetting(String indexName) {
        String setting = settings.get(indexName);
        if (setting!=null) {
            Map<String, Object> settings = JsonUtils.toBean(setting, Map.class);
            return settings;
        }
        return new HashMap<>();
    }
    public List<String> getAlias(String indexName) {
        List<String> alias = aliases.get(indexName);
        return alias;
    }
}
