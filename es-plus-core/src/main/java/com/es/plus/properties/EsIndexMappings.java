package com.es.plus.properties;

import lombok.Data;

import java.util.Map;

@Data
public class EsIndexMappings {
   private Map<String, Object> indexSettings;
    private Map<String, Object> mappings;
}
