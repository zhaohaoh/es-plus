package com.es.plus.core.wrapper.chain;


import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.params.EsSelect;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;
import com.es.plus.common.pojo.es.EpDistanceUnit;
import com.es.plus.common.pojo.es.EpFuzziness;
import com.es.plus.common.pojo.es.EpGeoPoint;
import com.es.plus.common.pojo.es.EpInnerHitBuilder;
import com.es.plus.common.pojo.es.EpNestedSortBuilder;
import com.es.plus.common.pojo.es.EpQueryBuilder;
import com.es.plus.common.pojo.es.EpScoreMode;
import com.es.plus.common.pojo.es.EpScript;
import com.es.plus.common.pojo.es.EpSearchType;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.core.wrapper.core.AbstractEsWrapper;
import com.es.plus.core.wrapper.core.EsExtendsWrapper;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.wrapper.core.EsStaitcsWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import com.es.plus.core.wrapper.core.IEsQueryWrapper;

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
    protected String[] indexs;
    protected String type = GlobalConfigCache.GLOBAL_CONFIG.getType();

    public QUERY getWrapper() {
        return esWrapper;
    }
    
    @Override
    public String[] getIndexs() {
        return indexs;
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
    public Children index(String... indexs) {
        this.indexs = indexs;
        return this.children;
    }

    @Override
    public Children type(String type) {
        this.type = type;
        return this.children;
    }

    @Override
    public Children _id(String _id) {
        if (indexs == null) {
            throw new EsException("index is null");
        }
        GlobalParamHolder.set_id(indexs,_id);
        return this.children;
    }

    @Override
    public Children must(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().must(condition, consumer);
        return this.children;
    }


    @Override
    public Children should(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().should(condition,consumer);
        return this.children;
    }

    @Override
    public Children mustNot(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().mustNot(condition,consumer);
        return this.children;
    }

    @Override
    public Children filter(boolean condition, Consumer<QUERY> consumer) {
        getWrapper().filter(condition, consumer);
        return this.children;
    }

    @Override
    public Children hasChild(boolean condition, String childType, EpScoreMode scoreMode, Consumer<QUERY> consumer) {
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
    public Children sortBy(String order, R... columns) {
        getWrapper().sortBy(order, columns);
        return children;
    }

    @Override
    public Children sortBy(String order, R column) {
        getWrapper().sortBy(order, column);
        return children;
    }

    @Override
    public Children sortByAsc(String... columns) {
        getWrapper().sortByAsc(columns);
        return children;
    }

    @Override
    public Children sortByAsc(R column) {
        getWrapper().sortByAsc(column);
        return children;
    }

    @Override
    public Children sortByDesc(String... columns) {
        getWrapper().sortByDesc(columns);
        return children;
    }

    @Override
    public Children sortByDesc(R column) {
        getWrapper().sortByDesc(column);
        return children;
    }

    @Override
    public Children sortByAsc(String path,String[] columns) {
        getWrapper().sortByAsc(path,columns);
        return children;
    }

    @Override
    public Children sortByDesc(String path,String[] columns) {
        getWrapper().sortByDesc(path,columns);
        return children;
    }

    
    @Override
    public Children sortBy(String order, EpNestedSortBuilder nestedSortBuilder,String... name){
        getWrapper().sortBy(order,nestedSortBuilder,name);
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
    public <S> Children nestedQuery(R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(path, sClass, consumer, mode, innerHitBuilder);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(R path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(path, consumer, mode, innerHitBuilder);
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(condition, path, sClass, consumer, mode, innerHitBuilder);
        return this.children;
    }
    
    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer,
            EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nestedQuery(condition, path, consumer, mode, innerHitBuilder);
        return this.children;
    }
    
   
 
    @Override
    public <S> Children nested(String path, Consumer<EsQueryWrapper<S>> consumer) {
        getWrapper().nested(path, consumer);
        return this.children;
    }
    
 
    
    @Override
    public <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer) {
        getWrapper().nested(condition, path, consumer);
        return this.children;
    }
    
    @Override
    public <S> Children nested(String path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nested(path, consumer, mode, innerHitBuilder);
        return this.children;
    }
    
  

    @Override
    public <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        getWrapper().nested(condition, path, consumer, mode, innerHitBuilder);
        return this.children;
    }
    

    @Override
    public Children esQuery(boolean condition, EpQueryBuilder queryBuilder) {
        getWrapper().esQuery(condition, queryBuilder);
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
    public Children script(boolean condition, EpScript script) {
        getWrapper().script(condition, script);
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
    public Children prefix(boolean condition, R name, String value) {
        getWrapper().prefix(condition, name, value);
        return children;
    }
    
    @Override
    public Children prefixKeyword(boolean condition, R name, String value) {
        getWrapper().prefix(condition, name, value);
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
    
    @Override
    public Children wildcardKeyword(boolean condition, R name, String value) {
        getWrapper().wildcardKeyword(condition, name, value);
        return children;
    }
    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness) {
        getWrapper().fuzzy(condition, name, value, fuzziness);
        return children;
    }
    @Override
    public Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness,int prefixLength) {
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
    public Children geoBoundingBox(boolean condition, R name, EpGeoPoint topLeft, EpGeoPoint bottomRight) {
        getWrapper().geoBoundingBox(condition, name, topLeft, bottomRight);
        return children;
    }

    @Override
    public Children geoDistance(boolean condition, R name, String distance, EpDistanceUnit distanceUnit, EpGeoPoint centralGeoPoint) {
        getWrapper().geoDistance(condition, name, distance, distanceUnit, centralGeoPoint);
        return children;
    }

    @Override
    public Children geoPolygon(boolean condition, R name, List<EpGeoPoint> geoPoints) {
        getWrapper().geoPolygon(condition, name, geoPoints);
        return children;
    }

    public EpBoolQueryBuilder getQueryBuilder() {
        return getWrapper().getQueryBuilder();
    }
    
    /**
     * 是否拉取数据list。
     */
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
    public Children trackTotalHits(boolean trackTotalHits) {
        getWrapper().trackTotalHits(trackTotalHits);
        return this.children;
    }
    
    @Override
    public Children boost(float boost) {
        getWrapper().boost(boost);
        return children;
    }

    @Override
    public Children searchType(EpSearchType searchType) {
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
    
    @Override
    public Children profile() {
        getWrapper().profile();
        return this.children;
    }
}
