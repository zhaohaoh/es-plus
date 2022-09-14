package com.es.plus.core.wrapper;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsQueryWrapper<Children, QUERY, R> {
    default Children must(Consumer<QUERY> consumer) {
        return must(true, consumer);
    }

    Children must(boolean condition, Consumer<QUERY> consumer);

    default Children should(Consumer<QUERY> consumer) {
        return should(true, consumer);
    }

    Children should(boolean condition, Consumer<QUERY> consumer);

    default Children mustNot(Consumer<QUERY> consumer) {
        return mustNot(true, consumer);
    }

    Children mustNot(boolean condition, Consumer<QUERY> consumer);

    default Children filters(Consumer<QUERY> consumer) {
        return filters(true, consumer);
    }

    Children filters(boolean condition, Consumer<QUERY> consumer);

    default Children query(QueryBuilder queryBuilder) {
        return query(true, queryBuilder);
    }

    Children query(boolean condition, QueryBuilder queryBuilder);

    default Children exists(R name) {
        return exists(true, name);
    }

    Children exists(boolean condition, R name);

    default Children term(R name, Object value) {
        return term(true, name, value);
    }

    Children term(boolean condition, R name, Object value);

    default Children terms(R name, Object... value) {
        return terms(true, name, value);
    }

    Children terms(boolean condition, R name, Object... value);

    default Children terms(R name, Collection<?> values) {
        return terms(true, name, values);
    }

    Children terms(boolean condition, R name, Collection<?> values);

    //
    default Children termKeyword(R name, Object value) {
        return term(true, name, value);
    }

    Children termKeyword(boolean condition, R name, Object value);

    default Children termsKeyword(R name, Object... value) {
        return terms(true, name, value);
    }

    Children termsKeyword(boolean condition, R name, Object... value);

    default Children termsKeyword(R name, Collection<?> values) {
        return terms(true, name, values);
    }

    Children termsKeyword(boolean condition, R name, Collection<?> values);

    default Children match(R name, Object value) {
        return match(true, name, value);
    }

    Children match(boolean condition, R name, Object value);

    default Children matchPhrase(R name, Object value) {
        return matchPhrase(true, name, value);
    }

    Children matchPhrase(boolean condition, R name, Object value);

    default Children multiMatch(Object value, R... name) {
        return multiMatch(true, value, name);
    }

    Children multiMatch(boolean condition, Object value, R... name);

    default Children matchPhrasePrefix(R name, Object value) {
        return matchPhrasePrefix(true, name, value);
    }

    Children matchPhrasePrefix(boolean condition, R name, Object value);

    default Children wildcard(R name, String value) {
        return wildcard(true, name, value);
    }

    Children wildcard(boolean condition, R name, String value);

    default Children fuzzy(R name, String value) {
        return fuzzy(true, name, value);
    }

    //有纠错能力的模糊查询。
    Children fuzzy(boolean condition, R name, String value);

    default Children ids(Collection<String> ids) {
        return ids(true, ids);
    }

    //TODO 迟点用 根据id查询
    Children ids(boolean condition, Collection<String> ids);

    default Children gt(R name, Object from) {
        return gt(true, name, from);
    }

    Children gt(boolean condition, R name, Object from);

    default Children ge(R name, Object from) {
        return ge(true, name, from);
    }

    Children ge(boolean condition, R name, Object from);

    default Children lt(R name, Object to) {
        return lt(true, name, to);
    }

    Children lt(boolean condition, R name, Object to);

    default Children le(R name, Object to) {
        return le(true, name, to);
    }

    Children le(boolean condition, R name, Object to);

    default Children between(R name, Object from, Object to) {
        return between(true, name, from, to);
    }

    Children between(boolean condition, R name, Object from, Object to);

    default Children between(R name, Object from, Object to, boolean include) {
        return between(true, name, to, include);
    }

    Children between(boolean condition, R name, Object from, Object to, boolean include);


    //根据name查询，这里违反了设计原则但是方便了
    default Children exists(String name) {
        return exists(true, name);
    }

    Children exists(boolean condition, String name);

    default Children term(String name, Object value) {
        return term(true, name, value);
    }

    Children term(boolean condition, String name, Object value);

    default Children terms(String name, Object value) {
        return terms(true, name, value);
    }

    Children terms(boolean condition, String name, Object... value);

    default Children terms(String name, Collection<Object> values) {
        return terms(true, name, values);
    }

    Children terms(boolean condition, String name, Collection<Object> values);

    default Children match(String name, Object value) {
        return match(true, name, value);
    }

    Children match(boolean condition, String name, Object value);

    default Children matchPhrase(String name, Object value) {
        return matchPhrase(true, name, value);
    }

    Children matchPhrase(boolean condition, String name, Object value);

    default Children multiMatch(Object value, String... name) {
        return multiMatch(true, value, name);
    }

    Children multiMatch(boolean condition, Object value, String... name);

    default Children matchPhrasePrefix(String name, Object value) {
        return matchPhrasePrefix(true, name, value);
    }

    Children matchPhrasePrefix(boolean condition, String name, Object value);

    default Children wildcard(String name, String value) {
        return wildcard(true, name, value);
    }

    Children wildcard(boolean condition, String name, String value);

    default Children fuzzy(String name, String value) {
        return fuzzy(true, name, value);
    }

    //有纠错能力的模糊查询。
    Children fuzzy(boolean condition, String name, String value);


    default Children gt(String name, Object from) {
        return gt(true, name, from);
    }

    Children gt(boolean condition, String name, Object from);

    default Children ge(String name, Object from) {
        return ge(true, name, from);
    }

    Children ge(boolean condition, String name, Object from);

    default Children lt(String name, Object to) {
        return lt(true, name, to);
    }

    Children lt(boolean condition, String name, Object to);

    default Children le(String name, Object to) {
        return le(true, name, to);
    }

    Children le(boolean condition, String name, Object to);

    default Children between(String name, Object from, Object to) {
        return between(true, name, from, to);
    }

    Children between(boolean condition, String name, Object from, Object to);

    default Children between(String name, Object from, Object to, boolean include) {
        return between(true, name, from, to, include);
    }

    Children between(boolean condition, String name, Object from, Object to, boolean include);
}
