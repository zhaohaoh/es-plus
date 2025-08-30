package com.es.plus.core.wrapper.core;

import com.es.plus.adapter.pojo.es.*;

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
    
    default Children hasChild(String childType, EpScoreMode scoreMode, Consumer<QUERY> consumer) {
        return hasChild(true, childType, scoreMode, consumer);
    }
    
    // 修改1: ScoreMode替换为EpScoreMode
    Children hasChild(boolean condition, String childType, EpScoreMode scoreMode, Consumer<QUERY> consumer);
    
    default Children hasParent(String parentType, Boolean scoreMod, Consumer<QUERY> consumer) {
        return hasParent(true, parentType, scoreMod, consumer);
    }
    
    // 修改2: ScoreMode替换为Boolean
    Children hasParent(boolean condition, String parentType, Boolean scoreMode, Consumer<QUERY> consumer);
    
    default Children parentIdQuery(String childType, String id) {
        return parentIdQuery(true, childType, id);
    }
    
    Children parentIdQuery(boolean condition, String childType, String id);
    
    default Children query(EpQueryBuilder queryBuilder) {
        return query(true, queryBuilder);
    }
    
    // 修改3: QueryBuilder替换为EpQueryBuilder
    Children query(boolean condition, EpQueryBuilder queryBuilder);
    
    default Children exists(R name) {
        return exists(true, name);
    }
    
    Children exists(boolean condition, R name);
    
    default Children term(R name, Object value) {
        return term(true, name, value);
    }
    
    Children term(boolean condition, R name, Object value);
    
    // 修改4: Script替换为EpScript
    default Children script(EpScript script) {
        return script(true, script);
    }
    
    // 修改5: Script替换为EpScript
    Children script(boolean condition, EpScript script);
    
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
        return termKeyword(true, name, value);
    }
    
    Children termKeyword(boolean condition, R name, Object value);
    
    default Children termsKeyword(R name, Object... value) {
        return termsKeyword(true, name, value);
    }
    
    Children termsKeyword(boolean condition, R name, Object... value);
    
    default Children termsKeyword(R name, Collection<?> values) {
        return termsKeyword(true, name, values);
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
    
    default Children wildcardKeyword(R name, String value) {
        return wildcardKeyword(true, name, value);
    }
    
    Children wildcardKeyword(boolean condition, R name, String value);
    
    default Children fuzzy(R name, String value, EpFuzziness fuzziness) {
        return fuzzy(true, name, value, fuzziness);
    }
    
    // 修改6: Fuzziness替换为EpFuzziness
    Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness);
    
    default Children fuzzy(R name, String value, EpFuzziness fuzziness, int prefixLength) {
        return fuzzy(true, name, value, fuzziness, prefixLength);
    }
    
    // 修改7: Fuzziness替换为EpFuzziness
    Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness, int prefixLength);
    
    default Children ids(Collection<String> ids) {
        return ids(true, ids);
    }
    
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
    default <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        return nestedQuery(true, path, sClass, consumer, mode, innerHitBuilder);
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
    <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder);
    
    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    default <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        return nestedQuery(true, path, consumer, mode, innerHitBuilder);
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
    <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder);
    
    
    /**
     * 嵌套查询1
     *
     * @param path     路径
     * @param sClass   s类
     * @param consumer 消费者
     * @param mode     模式
     * @return {@link Children}
     */
    default <S> Children nested(String path,  Consumer<EsQueryWrapper<S>> consumer) {
        return nested(true, path, consumer);
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
    <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer);
    
    
    /**
     * 嵌套查询
     *
     * @param condition 条件
     * @param path      路径
     * @param consumer  消费者
     * @param mode      模式
     * @return {@link Children}
     */
    default <S> Children nested(String path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        return nested(true, path, consumer, mode, innerHitBuilder);
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
    <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder);
    
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
    
    default Children range(R name, Object from, Object to) {
        return range(true, name, from, to);
    }
    
    Children range(boolean condition, R name, Object from, Object to);
    
    default Children range(R name, Object from, Object to, String timeZone) {
        return range(true, name, from, to, timeZone);
    }
    
    Children range(boolean condition, R name, Object from, Object to, String timeZone);
    
    
    default Children range(R name, Object from, Object to, boolean fromInclude, boolean toInclude) {
        return range(true, name, from, to, fromInclude, toInclude);
    }
    
    Children range(boolean condition, R name, Object from, Object to, boolean fromInclude, boolean toInclude);
    
    default Children geoBoundingBox(R name, EpGeoPoint topLeft, EpGeoPoint bottomRight) {
        return geoBoundingBox(true, name, topLeft, bottomRight);
    }
    
    // 修改8: GeoPoint替换为EpGeoPoint
    Children geoBoundingBox(boolean condition, R name, EpGeoPoint topLeft, EpGeoPoint bottomRight);
    
    default Children geoDistance(R name, String distance, EpDistanceUnit distanceUnit, EpGeoPoint centralGeoPoint) {
        return geoDistance(true, name, distance, distanceUnit, centralGeoPoint);
    }
    
    // 修改9: DistanceUnit和GeoPoint替换为EpDistanceUnit和EpGeoPoint
    Children geoDistance(boolean condition, R name, String distance, EpDistanceUnit distanceUnit, EpGeoPoint centralGeoPoint);
    
    default Children geoPolygon(R name, List<EpGeoPoint> geoPoints) {
        return geoPolygon(true, name, geoPoints);
    }
    
    // 修改10: GeoPoint替换为EpGeoPoint
    Children geoPolygon(boolean condition, R name, List<EpGeoPoint> geoPoints);
}
