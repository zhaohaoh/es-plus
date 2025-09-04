package com.es.plus.common.properties;

import java.util.List;

public class EsMappingParam {
    private String fieldName;
    private String type;
    private Boolean store;
    private String analyzer;
    private Boolean index;
    private String searchAnalyzer;
    private Float boost;
    private List<EsMappingParam> mappingProperties;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Float getBoost() {
        return boost;
    }

    public void setBoost(Float boost) {
        this.boost = boost;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStore() {
        return store;
    }

    public void setStore(Boolean store) {
        this.store = store;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public String getSearchAnalyzer() {
        return searchAnalyzer;
    }

    public void setSearchAnalyzer(String searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
    }

    public List<EsMappingParam> getMappingProperties() {
        return mappingProperties;
    }

    public void setMappingProperties(List<EsMappingParam> mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    @Override
    public String toString() {
        return "EsMappingParam{" +
                "fieldName='" + fieldName + '\'' +
                ", type='" + type + '\'' +
                ", store=" + store +
                ", analyzer='" + analyzer + '\'' +
                ", index=" + index +
                ", searchAnalyzer='" + searchAnalyzer + '\'' +
                ", mappingProperties=" + mappingProperties +
                '}';
    }
}
