package com.es.plus.common.pojo.es;

/**
 * 自定义字段排序构建器，用于替代Elasticsearch的FieldSortBuilder
 */
public class EpFieldSortBuilder {
    private String field;
    private EpSortOrder order;
    private String mode;
    private String missing;
    private EpNestedSortBuilder nestedSort;

    public EpFieldSortBuilder() {
    }

    public EpFieldSortBuilder(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public EpFieldSortBuilder setField(String field) {
        this.field = field;
        return this;
    }

    public EpSortOrder getOrder() {
        return order;
    }

    public EpFieldSortBuilder setOrder(EpSortOrder order) {
        this.order = order;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public EpFieldSortBuilder setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String getMissing() {
        return missing;
    }

    public EpFieldSortBuilder setMissing(String missing) {
        this.missing = missing;
        return this;
    }

    public EpNestedSortBuilder getNestedSort() {
        return nestedSort;
    }

    public EpFieldSortBuilder setNestedSort(EpNestedSortBuilder nestedSort) {
        this.nestedSort = nestedSort;
        return this;
    }
}
