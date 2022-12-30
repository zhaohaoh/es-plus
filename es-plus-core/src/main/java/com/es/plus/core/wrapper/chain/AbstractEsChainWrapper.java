package com.es.plus.core.wrapper.chain;


import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.core.wrapper.core.*;
import com.es.plus.pojo.EsSelect;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author: hzh
 * @Date: 2022/7/4 17:23
 * T是实体类 R的方法 Children是自己
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsChainWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsChainWrapper<T, R, Children, QUERY>, QUERY extends AbstractEsWrapper<T, R, QUERY>>
        implements IEsQueryWrapper<Children, QUERY, R>, EsWrapper<Children, T>, EsExtendsWrapper<Children, R> {
    protected QUERY esWrapper;
    protected Children children = (Children) this;
    protected Class<T> tClass;

    public QUERY getWrapper() {
        return esWrapper;
    }

    @Override
    public EsLambdaAggWrapper<T> esLambdaAggWrapper() {
        return getWrapper().esLambdaAggWrapper();
    }

    @Override
    public EsAggWrapper<T> esAggWrapper() {
        return getWrapper().esAggWrapper();
    }

    @Override
    public EsSelect getSelect() {
        return getWrapper().getSelect();
    }

    @Override
    public Children profile(boolean profile) {
        getWrapper().profile(profile);
        return this.children;
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
    public Children filter(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().filter(condition, consumer);
        return this.children;
    }

    @Override
    public Children hasChild(boolean condition, String childType, ScoreMode scoreMode, Consumer<QUERY> consumer) {
        getWrapper().hasChild(condition, childType, scoreMode, consumer);
        return this.children;
    }

    @Override
    public Children hasParent(boolean condition, String parentType, Boolean scoreMode, Consumer<QUERY> consumer) {
        getWrapper().hasParent(condition, parentType, scoreMode, consumer);
        return this.children;
    }

    @Override
    public Children parentIdQuery(boolean condition, String childType, String id) {
        getWrapper().parentIdQuery(condition, childType, id);
        return this.children;
    }

    @Override
    public Children orderBy(String order, R... columns) {
        getWrapper().orderBy(order, columns);
        return children;
    }

    @Override
    public Children orderBy(String order, String... columns) {
        getWrapper().orderBy(order, columns);
        return children;
    }

    @Override
    public Children orderByAsc(String... columns) {
        getWrapper().orderByAsc(columns);
        return children;
    }

    @Override
    public Children orderByDesc(String... columns) {
        getWrapper().orderByDesc(columns);
        return children;
    }

    @Override
    public Children matchAll() {
        getWrapper().matchAll();
        return children;
    }

    @Override
    public Children must() {
        getWrapper().must();
        return children;
    }

    @Override
    public Children should() {
        getWrapper().should();
        return children;
    }

    @Override
    public Children filter() {
        getWrapper().filter();
        return children;
    }

    @Override
    public Children mustNot() {
        getWrapper().mustNot();
        return children;
    }

    @Override
    public Children nestedQuery(boolean condition, R path, Supplier<EsQueryWrapper<?>> sp, ScoreMode mode) {
        getWrapper().nestedQuery(condition, path, sp, mode);
        return this.children;
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

    @Override
    public Children geoBoundingBox(boolean condition, R name, GeoPoint topLeft, GeoPoint bottomRight) {
        getWrapper().geoBoundingBox(condition, name, topLeft, bottomRight);
        return children;
    }

    @Override
    public Children geoDistance(boolean condition, R name, String distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        getWrapper().geoDistance(condition, name, distance, distanceUnit, centralGeoPoint);
        return children;
    }

    @Override
    public Children geoPolygon(boolean condition, R name, List<GeoPoint> geoPoints) {
        getWrapper().geoPolygon(condition, name, geoPoints);
        return children;
    }

    @Override
    public Children geoShape(boolean condition, R name, String indexedShapeId, Geometry geometry, ShapeRelation shapeRelation) {
        getWrapper().geoShape(condition, name, indexedShapeId, geometry, shapeRelation);
        return children;
    }

    @Override
    public BoolQueryBuilder getQueryBuilder() {
        return getWrapper().getQueryBuilder();
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

    @Override
    public Children nestedQuery(boolean condition, String path, Supplier<EsQueryWrapper<?>> sp, ScoreMode mode) {
        getWrapper().nestedQuery(condition, path, sp, mode);
        return children;
    }

    @Override
    public Children geoBoundingBox(boolean condition, String name, GeoPoint topLeft, GeoPoint bottomRight) {
        getWrapper().geoBoundingBox(condition, name, topLeft, bottomRight);
        return children;
    }

    @Override
    public Children geoDistance(boolean condition, String name, String distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        getWrapper().geoDistance(condition, name, distance, distanceUnit, centralGeoPoint);
        return children;
    }

    @Override
    public Children geoPolygon(boolean condition, String name, List<GeoPoint> geoPoints) {
        getWrapper().geoPolygon(condition, name, geoPoints);
        return children;
    }

    @Override
    public Children geoShape(boolean condition, String name, String indexedShapeId, Geometry geometry, ShapeRelation shapeRelation) {
        getWrapper().geoShape(condition, name, indexedShapeId, geometry, shapeRelation);
        return children;
    }

    @Override
    public Children includes(R... func) {
        getWrapper().excludes(func);
        return children;
    }

    @Override
    public Children includes(String... names) {
        getWrapper().excludes(names);
        return children;
    }

    @Override
    public Children excludes(R... func) {
        getWrapper().excludes(func);
        return children;
    }

    @Override
    public Children excludes(String... names) {
        getWrapper().excludes(names);
        return children;
    }

    @Override
    public Children boost(float boost) {
        getWrapper().boost(boost);
        return children;
    }

    @Override
    public Children searchType(SearchType searchType) {
        getWrapper().searchType(searchType);
        return children;
    }

    @Override
    public Children highLight(String field) {
        getWrapper().highLight(field);
        return children;
    }

    @Override
    public Children highLight(String field, String preTag, String postTag) {
        getWrapper().highLight(field, preTag, postTag);
        return children;
    }

    //match方法中配合or使用，百分比匹配
    @Override
    public Children minimumShouldMatch(String minimumShouldMatch) {
        getWrapper().minimumShouldMatch(minimumShouldMatch);
        return children;
    }

    @Override
    public Children routings(String... routings) {
        getWrapper().routings(routings);
        return this.children;
    }


}
