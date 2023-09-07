package com.es.plus.core.wrapper.chain;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsSelect;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.core.wrapper.core.*;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/7/4 17:23
 * T是实体类 R的方法 Children是自己
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsChainWrapper<T, R, Children extends AbstractEsChainWrapper<T, R, Children, QUERY>, QUERY extends AbstractEsWrapper<T, R, QUERY>>
        implements IEsQueryWrapper<Children, QUERY, R>, EsWrapper<T>, EsExtendsWrapper<Children, R>, EsStaitcsWrapper<Children> {
    protected QUERY esWrapper;
    protected Children children = (Children) this;
    protected Class<T> tClass;
    //链式静态编程用来指定index
    protected String index;
    protected String type = GlobalConfigCache.GLOBAL_CONFIG.getType();

    public QUERY getWrapper() {
        return esWrapper;
    }

    @Override
    public EsParamWrapper<T> esParamWrapper() {
        return getWrapper().esParamWrapper();
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
    public Children index(String index) {
        //手动传入的索引名需要加上后缀
        this.index = index + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
        return this.children;
    }

    @Override
    public Children type(String type) {
        this.type = type;
        return this.children;
    }

    @Override
    public Children _id(String _id) {
        if (index == null) {
            throw new EsException("index is null");
        }
        GlobalParamHolder.set_id(_id);
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
    public Children orderBy(String order, R column) {
        getWrapper().orderBy(order, column);
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
    public <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer) {
        getWrapper().nestedQuery(path, sClass, consumer);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer) {
        getWrapper().nestedQuery(path, consumer);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer) {
        getWrapper().nestedQuery(condition, path, sClass, consumer);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer) {
        getWrapper().nestedQuery(condition, path, consumer);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(path, sClass, consumer, mode, innerHitBuilder);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(path, consumer, mode, innerHitBuilder);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(condition, path, sClass, consumer, mode, innerHitBuilder);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(condition, path, consumer, mode, innerHitBuilder);
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
    public Children fuzzy(boolean condition, R name, String value, Fuzziness fuzziness) {
        getWrapper().fuzzy(condition, name, value, fuzziness);
        return children;
    }
    @Override
    public Children fuzzy(boolean condition, R name, String value,Fuzziness fuzziness,int prefixLength) {
        getWrapper().fuzzy(condition, name, value, fuzziness,prefixLength);
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
    public Children range(boolean condition, R name, Object from, Object to) {
        getWrapper().range(condition, name, from, to);
        return children;
    }

    @Override
    public Children range(boolean condition, R name, Object from, Object to, String timeZone) {
        getWrapper().range(condition, name, from, to, timeZone);
        return children;
    }

    @Override
    public Children range(boolean condition, R name, Object from, Object to, boolean fromInclude, boolean toInclude) {
        getWrapper().range(condition, name, from, to, fromInclude, toInclude);
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

    public BoolQueryBuilder getQueryBuilder() {
        return getWrapper().getQueryBuilder();
    }

    @Override
    public Children fetch(boolean fetch) {
        getWrapper().fetch(fetch);
        return children;
    }

    @Override
    public Children includes(R... func) {
        getWrapper().includes(func);
        return children;
    }

    @Override
    public Children excludes(R... func) {
        getWrapper().excludes(func);
        return children;
    }

    @Override
    public Children minScope(float minScope) {
        getWrapper().minScope(minScope);
        return this.children;
    }

    @Override
    public Children trackScores(boolean trackScores) {
        getWrapper().trackScores(trackScores);
        return this.children;
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

    @Override
    public Children preference(String preference) {
        getWrapper().preference(preference);
        return this.children;
    }

    @Override
    public Children searchAfterValues(Object[] searchAfterValues) {
        getWrapper().searchAfterValues(searchAfterValues);
        return this.children;
    }
}
