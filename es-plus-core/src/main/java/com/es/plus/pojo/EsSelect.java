package com.es.plus.pojo;

import java.util.Arrays;

public class EsSelect {
    //查询结果包含字段
    private String[] includes;
    //查询结果不包含字段
    private String[] excludes;

    public String[] getIncludes() {
        return includes;
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
