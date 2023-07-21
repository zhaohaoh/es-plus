package com.es.plus.core.wrapper.core;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public interface IEsQueryWrapper<Children, QUERY, R> {

    Children matchAll();

    Children boost(float boost);

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

    default Children filter(Consumer<QUERY> consumer) {
        return filter(true, consumer);
    }

    Children filter(boolean condition, Consumer<QUERY> consumer);

    default Children hasChild(String childType, ScoreMode scoreMode, Consumer<QUERY> consumer) {
        return hasChild(true, childType, scoreMode, consumer);
    }

    Children hasChild(boolean condition, String childType, ScoreMode scoreMode, Consumer<QUERY> consumer);

    default Children hasParent(String parentType, Boolean scoreMod, Consumer<QUERY> consumer) {
        return hasParent(true, parentType, scoreMod, consumer);
    }

    Children hasParent(boolean condition, String parentType, Boolean scoreMode, Consumer<QUERY> consumer);

    default Children parentIdQuery(String childType, String id) {
        return parentIdQuery(true, childType, id);
    }

    Children parentIdQuery(boolean condition, String childType, String id);

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

    /**
     * 嵌套查询
     *
     * @param path     路径
     * @param sClass   s类
     * @param consumer 消费者
     * @param mode     模式
     * @return {@link Children}
     */
    default <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer) {
        return nestedQuery(true, path, sClass, consumer);
    }

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param sClass    s类
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer);

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    default <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer) {
        return nestedQuery(true, path, consumer);
    }

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer);

    /**
     * 嵌套查询
     *
     * @param path     路径
     * @param sClass   s类
     * @param consumer 消费者
     * @param mode     模式
     * @return {@link Children}
     */
    default <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode,InnerHitBuilder innerHitBuilder) {
        return nestedQuery(true, path, sClass, consumer, mode,innerHitBuilder);
    }

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param sClass    s类
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode,InnerHitBuilder innerHitBuilder);

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    default <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        return nestedQuery(true, path, consumer, mode,innerHitBuilder);
    }

    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder);

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

    default Children between(R name, Object from, Object to, boolean fromInclude,boolean toInclude) {
        return between(true, name,from, to, fromInclude,toInclude);
    }

    Children between(boolean condition, R name, Object from, Object to, boolean fromInclude,boolean toInclude);

    default Children geoBoundingBox(R name, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(true, name, topLeft, bottomRight);
    }

    Children geoBoundingBox(boolean condition, R name, GeoPoint topLeft, GeoPoint bottomRight);

    default Children geoDistance(R name, String distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return geoDistance(true, name, distance, distanceUnit, centralGeoPoint);
    }

    Children geoDistance(boolean condition, R name, String distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint);

    default Children geoPolygon(R name, List<GeoPoint> geoPoints) {
        return geoPolygon(true, name, geoPoints);
    }

    Children geoPolygon(boolean condition, R name, List<GeoPoint> geoPoints);

}
