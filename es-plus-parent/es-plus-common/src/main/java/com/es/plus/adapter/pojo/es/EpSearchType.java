package com.es.plus.adapter.pojo.es;

/**
 * 自定义搜索类型，用于替代Elasticsearch的SearchType
 */
public enum EpSearchType {
    /**
     * 默认搜索类型，会同时查询所有相关的分片
     */
    QUERY_THEN_FETCH("query_then_fetch"),

    /**
     * DFS搜索类型，在执行搜索前先获取所有分片的统计信息，使评分更准确
     */
    DFS_QUERY_THEN_FETCH("dfs_query_then_fetch");

    private final String name;

    EpSearchType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据名称获取对应的搜索类型
     *
     * @param name 搜索类型名称
     * @return 对应的EpSearchType枚举值
     */
    public static EpSearchType fromString(String name) {
        for (EpSearchType searchType : EpSearchType.values()) {
            if (searchType.getName().equals(name)) {
                return searchType;
            }
        }
        throw new IllegalArgumentException("No search type found for [" + name + "]");
    }

    @Override
    public String toString() {
        return name;
    }
}
