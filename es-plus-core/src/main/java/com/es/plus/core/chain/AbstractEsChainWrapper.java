package com.es.plus.core.chain;


import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.AbstractEsWrapper;
import com.es.plus.core.wrapper.IEsQueryWrapper;
import com.es.plus.core.wrapper.aggregation.EsAggregationWrapper;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/7/4 17:23
 * T是实体类 R的方法 Children是自己
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsChainWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsChainWrapper<T, R, Children, QUERY>, QUERY extends AbstractEsWrapper<T, R, QUERY>>
        implements IEsQueryWrapper<Children, QUERY, R> {
    protected QUERY esWrapper;
    protected Children children = (Children) this;
    protected Class<T> tClass;

    public AbstractEsWrapper<T, R, QUERY> getWrapper() {
        return esWrapper;
    }

    public EsAggregationWrapper<T> esAggregationWrapper() {
        return getWrapper().esAggregationWrapper();
    }

    @Override
    public Children must(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().must(condition, consumer);
        return this.children;
    }

    @Override
    public Children should(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().should(consumer);
        return this.children;
    }

    @Override
    public Children mustNot(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().mustNot(consumer);
        return this.children;
    }

    @Override
    public Children filters(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().filters(condition, consumer);
        return this.children;
    }

    public Children orderBy(String order, R... columns) {
        getWrapper().orderBy(order, columns);
        return children;
    }

    public Children orderBy(String order, String... columns) {
        getWrapper().orderBy(order, columns);
        return children;
    }

    public Children orderByAsc(String... columns) {
        getWrapper().orderByAsc(columns);
        return children;
    }

    public Children orderByDesc(String... columns) {
        getWrapper().orderByDesc(columns);
        return children;
    }

    public Children matchAll() {
        getWrapper().matchAll();
        return children;
    }


    public Children must() {
        getWrapper().must();
        return children;
    }

    public Children should() {
        getWrapper().should();
        return children;
    }

    public Children filter() {
        getWrapper().filter();
        return children;
    }

    public Children mustNot() {
        getWrapper().mustNot();
        return children;
    }

    //match方法中配合or使用，百分比匹配
    public void minimumShouldMatch(String minimumShouldMatch) {
        getWrapper().minimumShouldMatch(minimumShouldMatch);
    }

    @Override
    public Children query(boolean condition, QueryBuilder queryBuilder) {
        getWrapper().query(condition, queryBuilder);
        return children;
    }

    @Override
    public Children exists(boolean condition, R name) {
        getWrapper().exists(condition, name);
        return children;
    }

    @Override
    public Children term(boolean condition, R name, Object value) {
        getWrapper().term(condition, name, value);
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Object... value) {
        getWrapper().terms(condition, name, value);
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Collection<?> values) {
        getWrapper().terms(condition, name, values);
        return children;
    }


    @Override
    public Children termKeyword(boolean condition, R name, Object value) {
        getWrapper().termKeyword(condition, name, value);
        return children;
    }


    @Override
    public Children termsKeyword(boolean condition, R name, Object... values) {
        getWrapper().termsKeyword(condition, name, values);
        return children;
    }

    @Override
    public Children termsKeyword(boolean condition, R name, Collection<?> values) {
        getWrapper().termsKeyword(condition, name, values);
        return children;
    }

    @Override
    public Children match(boolean condition, R name, Object value) {
        getWrapper().match(condition, name, value);
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, R name, Object value) {
        getWrapper().matchPhrase(condition, name, value);
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, R... name) {
        getWrapper().multiMatch(condition, value, name);
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, R name, Object value) {
        getWrapper().matchPhrasePrefix(condition, name, value);
        return children;
    }

    @Override
    public Children wildcard(boolean condition, R name, String value) {
        getWrapper().wildcard(condition, name, value);
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value) {
        getWrapper().fuzzy(condition, name, value);
        return children;
    }


    @Override
    public Children ids(boolean condition, Collection<String> ids) {
        getWrapper().ids(condition, ids);
        return children;
    }

    @Override
    public Children gt(boolean condition, R name, Object from) {
        getWrapper().gt(condition, name, from);
        return children;
    }

    @Override
    public Children ge(boolean condition, R name, Object from) {
        getWrapper().ge(condition, name, from);
        return children;
    }

    @Override
    public Children lt(boolean condition, R name, Object to) {
        getWrapper().lt(condition, name, to);
        return children;
    }

    @Override
    public Children le(boolean condition, R name, Object to) {
        getWrapper().le(condition, name, to);
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to) {
        getWrapper().between(condition, name, from, to);
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to, boolean include) {
        getWrapper().between(condition, name, from, to, include);
        return children;
    }


    /**
     * -----------下面的根据name查询，这里违反了设计原则但是方便了
     */
    @Override
    public Children exists(String name) {
        getWrapper().exists(name);
        return children;
    }

    @Override
    public Children exists(boolean condition, String name) {
        getWrapper().exists(condition, name);
        return children;
    }

    @Override
    public Children term(boolean condition, String name, Object value) {
        getWrapper().terms(condition, name, value);
        return this.children;
    }

    @Override
    public Children terms(boolean condition, String name, Object... value) {
        getWrapper().terms(condition, name, value);
        return children;
    }

    @Override
    public Children terms(boolean condition, String name, Collection<Object> values) {
        getWrapper().terms(condition, name, values);
        return children;
    }

    @Override
    public Children match(boolean condition, String name, Object value) {
        getWrapper().match(condition, name, value);
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, String name, Object value) {
        getWrapper().matchPhrase(condition, name, value);
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, String... name) {
        getWrapper().multiMatch(condition, value, name);
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, String name, Object value) {
        getWrapper().matchPhrasePrefix(condition, name, value);
        return children;
    }

    @Override
    public Children wildcard(boolean condition, String name, String value) {
        getWrapper().wildcard(condition, name, value);
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, String name, String value) {
        getWrapper().fuzzy(condition, name, value);
        return children;
    }


    @Override
    public Children gt(boolean condition, String name, Object from) {
        getWrapper().gt(condition, name, from);
        return children;
    }

    @Override
    public Children ge(boolean condition, String name, Object from) {
        getWrapper().ge(condition, name, from);
        return children;
    }

    @Override
    public Children lt(boolean condition, String name, Object to) {
        getWrapper().lt(condition, name, to);
        return children;
    }

    @Override
    public Children le(boolean condition, String name, Object to) {
        getWrapper().le(condition, name, to);
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to) {
        getWrapper().between(condition, name, from, to);
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to, boolean include) {
        getWrapper().between(condition, name, from, to, include);
        return children;
    }

    public Children includes(R... func) {
        getWrapper().excludes(func);
        return children;
    }

    public Children includes(String... names) {
        getWrapper().excludes(names);
        return children;
    }

    public Children excludes(R... func) {
        getWrapper().excludes(func);
        return children;
    }

    public Children excludes(String... names) {
        getWrapper().excludes(names);
        return children;
    }

    public Children boost(Float boost) {
        getWrapper().boost(boost);
        return children;
    }

    public Children searchType(SearchType searchType) {
        getWrapper().searchType(searchType);
        return children;
    }

    public Children highLight(String field) {
        getWrapper().highLight(field);
        return children;
    }

    public Children highLight(String field, String preTag, String postTag) {
        getWrapper().highLight(field, preTag, postTag);
        return children;
    }

}
