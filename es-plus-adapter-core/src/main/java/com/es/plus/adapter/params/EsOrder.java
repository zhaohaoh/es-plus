package com.es.plus.adapter.params;

public class EsOrder {

    private String name;

    private String sort = "DESC";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSort() {
        return sort.toUpperCase();
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "EsOrder{" +
                "name='" + name + '\'' +
                ", sort='" + sort + '\'' +
                '}';
    }
}
