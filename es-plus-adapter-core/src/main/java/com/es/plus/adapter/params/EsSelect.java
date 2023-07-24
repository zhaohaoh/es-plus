package com.es.plus.adapter.params;

import java.util.Arrays;

public class EsSelect {
    //查询结果包含字段
    private String[] includes;
    //查询结果不包含字段
    private String[] excludes;

    private Boolean fetch ;

    private Float minScope;

    public Float getMinScope() {
        return minScope;
    }

    public void setMinScope(Float minScope) {
        this.minScope = minScope;
    }

    public String[] getIncludes() {
        return includes;
    }

    public Boolean getFetch() {
        return fetch;
    }

    public void setFetch(Boolean fetch) {
        this.fetch = fetch;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "EsSelect{" +
                "includes=" + Arrays.toString(includes) +
                ", excludes=" + Arrays.toString(excludes) +
                '}';
    }
}
