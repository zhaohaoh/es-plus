package com.es.plus.common.pojo.es;

/**
 * 自定义嵌套排序构建器，用于替代Elasticsearch的NestedSortBuilder
 */
public class EpNestedSortBuilder {
    private String path;
    private EpNestedSortBuilder nestedSort;
    private EpQueryBuilder filter;  // 新增：过滤器
    private Integer maxChildren;    // 新增：最大子项数
    
    public EpNestedSortBuilder() {
    }
    
    public EpNestedSortBuilder(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public EpNestedSortBuilder setPath(String path) {
        this.path = path;
        return this;
    }
    
    public EpNestedSortBuilder getNestedSort() {
        return nestedSort;
    }
    
    public EpNestedSortBuilder setNestedSort(EpNestedSortBuilder nestedSort) {
        this.nestedSort = nestedSort;
        return this;
    }
    
    public EpQueryBuilder getFilter() {
        return filter;
    }
    
    public EpNestedSortBuilder setFilter(EpQueryBuilder filter) {
        this.filter = filter;
        return this;
    }
    
    public Integer getMaxChildren() {
        return maxChildren;
    }
    
    public EpNestedSortBuilder setMaxChildren(Integer maxChildren) {
        this.maxChildren = maxChildren;
        return this;
    }
}
