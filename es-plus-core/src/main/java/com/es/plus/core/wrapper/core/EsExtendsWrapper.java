package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.EsSelect;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.NestedSortBuilder;

public interface EsExtendsWrapper<Children, R> {


    /**
     * 选择字段
     */
    EsSelect getSelect();

    /**
     * 必须
     *
     * @return {@link Children}
     */
    Children must();

    /**
     * 应该
     *
     * @return {@link Children}
     */
    Children should();

    /**
     * 过滤器
     *
     * @return {@link Children}
     */
    Children filter();

    /**
     * 不得
     *
     * @return {@link Children}
     */
    Children mustNot();

    /**
     * 最低应该匹配
     *
     * @param minimumShouldMatch 最低应该匹配
     * @return {@link Children}
     */
    Children minimumShouldMatch(String minimumShouldMatch);

    /**
     * 最低匹配分数
     *
     */
    Children minScope(float minScope);

    /**
     * 是否查询分数
     *
     */
    Children trackScores(boolean trackScores);

    /**
     * 路由
     *
     * @param routings 路由
     * @return {@link Children 本身}
     */
    Children routings(String... routings);

    Children  preference(String preference);

    Children fetch(boolean fetch);

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

    /**
     * 排序
     */
    Children sortBy(String order, R... columns);

    /**
     * 排序
     */
    Children sortBy(String order, R column);

    /**
     * 排序
     */
    Children sortByAsc(String... columns);

    /**
     * 排序
     */

    Children sortBy(String order, R column, NestedSortBuilder nestedSortBuilder);

    /**
     * 排序
     */
    Children sortByDesc(String... columns);

    /**
     * es查询类型
     */
    Children searchType(SearchType searchType);

    /**
     * 高亮字段
     */
    Children highLight(String field);

    /**
     * 高亮字段
     */
    Children highLight(String field, String preTag, String postTag);

    /**
     * 搜索值
     *
     * @param searchAfterValues 搜索后值
     * @return {@link Children}
     */
    Children searchAfterValues(Object[] searchAfterValues);

}
