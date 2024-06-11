package com.es.plus.adapter.params;

import lombok.Data;

import java.util.Map;

@Data
public class EsIndexResponse {
    private Map<String, Object> mappings;
    private String[] indices;
    private Map<String, String> settings;
}
