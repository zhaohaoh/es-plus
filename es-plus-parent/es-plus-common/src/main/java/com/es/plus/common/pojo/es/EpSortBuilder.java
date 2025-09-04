package com.es.plus.common.pojo.es;

/**
 * 自定义排序构建器，用于替代Elasticsearch的SortBuilder
 */
public class EpSortBuilder {
    private String field;
    private EpSortOrder order;
    private EpNestedSortBuilder nestedSort;
    
    public EpSortBuilder() {
    }
    
    public EpSortBuilder(String field) {
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
    
    public EpSortBuilder setField(String field) {
        this.field = field;
        return this;
    }
    
    public EpSortOrder getOrder() {
        return order;
    }
    
    public EpSortBuilder setOrder(EpSortOrder order) {
        this.order = order;
        return this;
    }
    
    
    public EpNestedSortBuilder getNestedSort() {
        return nestedSort;
    }
    
    public EpSortBuilder setNestedSort(EpNestedSortBuilder nestedSort) {
        this.nestedSort = nestedSort;
        return this;
    }
}
