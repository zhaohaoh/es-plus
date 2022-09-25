package com.es.plus.properties;


import com.es.plus.pojo.EsSettings;

import java.util.Map;

public class EsIndexParam {

    private String index;
    private String alias;
    private EsSettings esSettings;
    private Map<String, Object> mappings;
    private Class<?> childClass;


    public Class<?> getChildClass() {
        return childClass;
    }

    public void setChildClass(Class<?> childClass) {
        this.childClass = childClass;
    }

    public Map<String, Object> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Object> mappings) {
        this.mappings = mappings;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public EsSettings getEsSettings() {
        return esSettings;
    }

    public void setEsSettings(EsSettings esSettings) {
        this.esSettings = esSettings;
    }
}
