package com.es.plus.core.wrapper.core;


import com.es.plus.pojo.EsSelect;
import org.elasticsearch.action.search.SearchType;

public interface EsExtendsWrapper<Children, R> {

    /**
     * 选择字段
     *
     */
    EsSelect getSelect();

    /**
     * 路由
     *
     * @param routings 路由
     * @return {@link Children 本身}
     */
    Children routings(String... routings);
    /**
     * 包含字段
     */
    Children includes(R... func);
    /**
     * 包含字段
     */
//    Children includes(String... names);
    /**
     * 忽略字段
     */
    Children excludes(R... func);
    /**
     * 忽略字段
     */
//    Children excludes(String... names);
    /**
     * 排序
     */
    Children orderBy(String order, R... columns);
    /**
     * 排序
     */
//    Children orderBy(String order, String... columns);
    /**
     * 排序
     */
    Children orderByAsc(String... columns);
    /**
     * 排序
     */
    Children orderByDesc(String... columns);
    /**
     * es查询类型
     */
    Children searchType(SearchType searchType);
    /**
     * 高亮字段
     */
    Children highLight(String field);
    /**
     *高亮字段
     */
    Children highLight(String field, String preTag, String postTag);
}
