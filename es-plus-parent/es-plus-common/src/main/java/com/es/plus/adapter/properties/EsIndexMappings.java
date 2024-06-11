package com.es.plus.adapter.properties;

import lombok.Data;

import java.util.Map;

@Data
public class EsIndexMappings {
   private Map<String, Object> indexSettings;
    private Map<String, Object> mappings;
}
