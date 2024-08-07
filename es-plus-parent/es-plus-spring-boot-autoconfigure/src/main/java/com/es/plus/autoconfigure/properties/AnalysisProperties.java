package com.es.plus.autoconfigure.properties;

import lombok.Data;

@Data
public class AnalysisProperties {
    /**
     * 过滤器
     */
    private String[] filters;
    /**
     * 分词器
     */
    private String tokenizer;
}
