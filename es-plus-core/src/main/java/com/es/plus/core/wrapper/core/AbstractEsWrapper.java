package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.params.*;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsWrapper<T, R, Children extends AbstractEsWrapper<T, R, Children>> extends AbstractLambdaEsWrapper<T, R>
        implements IEsQueryWrapper<Children, Children, R>, EsWrapper<T>, EsExtendsWrapper<Children, R> {
    protected AbstractEsWrapper() {
    }

    protected Class<T> tClass;

    protected Children children = (Children) this;

    protected QueryBuilder currentBuilder;

    /*
     *实例
     */
    protected abstract Children instance();


    protected EsParamWrapper<T> esParamWrapper;

    private List<QueryBuilder> queryBuilders = esParamWrapper().getQueryBuilder().must();

    protected EsLambdaAggWrapper<T> esLambdaAggWrapper;
    protected EsAggWrapper<T> esAggWrapper;

    @Override
    public EsParamWrapper<T> esParamWrapper() {
        if (esParamWrapper == null) {
            esParamWrapper = new EsParamWrapper<>();
        }
        return esParamWrapper;
    }

    @Override
    public EsUpdateField getEsUpdateField() {
        return esParamWrapper().getEsUpdateField();
    }

    @Override
    public EsLambdaAggWrapper<T> esLambdaAggWrapper() {
        if (esLambdaAggWrapper == null) {
            esLambdaAggWrapper = new EsLambdaAggWrapper<>(tClass);
            esParamWrapper.setAggregationBuilder(esLambdaAggWrapper.getAggregationBuilder());
        }
        return esLambdaAggWrapper;
    }

    @Override
    public EsAggWrapper<T> esAggWrapper() {
        if (esAggWrapper == null) {
            esAggWrapper = new EsAggWrapper<>(tClass);
            esParamWrapper.setAggregationBuilder(esAggWrapper.getAggregationBuilder());
        }
        return esAggWrapper;
    }


    /**
     * 得到es param包装
     *
     * @return {@link EsQueryParamWrapper}
     */
    private EsQueryParamWrapper getEsQueryParamWrapper() {
        return esParamWrapper.getEsQueryParamWrapper();
    }

    @Override
    protected String nameToString(R function) {
        return super.nameToString(function);
    }


    public BoolQueryBuilder getQueryBuilder() {
        return esParamWrapper().getQueryBuilder();
    }

    //获取select的字段
    @Override
    public EsSelect getSelect() {
        EsSelect esSelect = getEsQueryParamWrapper().getEsSelect();
        if (esSelect == null) {
            getEsQueryParamWrapper().setEsSelect(new EsSelect());
        }
        return getEsQueryParamWrapper().getEsSelect();
    }


    @Override
    public Children matchAll() {
        getQueryBuilder().must(QueryBuilders.matchAllQuery());
        return this.children;
    }

    @Override
    public Children boost(float boost) {
        currentBuilder.boost(boost);
        return this.children;
    }

    @Override
    public Children must(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.getQueryBuilder().must(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children should(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.getQueryBuilder().should(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children mustNot(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.getQueryBuilder().mustNot(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children filter(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.getQueryBuilder().filter(children.getQueryBuilder());
        return this.children;
    }

    /**
     * 根据子文档条件查询父文档  待优化自动获取type
     */
    @Override
    public Children hasChild(boolean condition, String childType, ScoreMode scoreMode, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        HasChildQueryBuilder hasParentQueryBuilder = new HasChildQueryBuilder(childType, children.getQueryBuilder(), scoreMode);
        queryBuilders.add(hasParentQueryBuilder);
        currentBuilder = hasParentQueryBuilder;
        return this.children;
    }

    /**
     * 根据父文档条件查询子文档 待优化自动获取type
     */
    @Override
    public Children hasParent(boolean condition, String parentType, Boolean scoreMode, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        HasParentQueryBuilder hasParentQueryBuilder = new HasParentQueryBuilder(parentType, children.getQueryBuilder(), scoreMode);
        queryBuilders.add(hasParentQueryBuilder);
        currentBuilder = hasParentQueryBuilder;
        return this.children;
    }

    @Override
    public Children parentIdQuery(boolean condition, String childType, String id) {
        ParentIdQueryBuilder parentIdQueryBuilder = new ParentIdQueryBuilder(childType, id);
        queryBuilders.add(parentIdQueryBuilder);
        currentBuilder = parentIdQueryBuilder;
        return this.children;
    }

    @Override
    public Children must() {
        if (queryBuilders != getQueryBuilder().must()) {
            queryBuilders = getQueryBuilder().must();
        }
        return children;
    }

    @Override
    public Children should() {
        if (queryBuilders != getQueryBuilder().should()) {
            queryBuilders = getQueryBuilder().should();
        }
        return children;
    }

    @Override
    public Children filter() {
        if (queryBuilders != getQueryBuilder().filter()) {
            queryBuilders = getQueryBuilder().filter();
        }
        return children;
    }

    @Override
    public Children mustNot() {
        if (queryBuilders != getQueryBuilder().mustNot()) {
            queryBuilders = getQueryBuilder().mustNot();
        }
        return children;
    }

    @Override
    public Children query(boolean condition, QueryBuilder queryBuilder) {
        if (condition) {
            queryBuilders.add(queryBuilder);
        }
        return children;
    }

    @Override
    public Children exists(boolean condition, R name) {
        if (condition) {
            ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(nameToString(name));
            currentBuilder = existsQueryBuilder;
            queryBuilders.add(existsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children term(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name);
//            if (tClass != null) {
//                //获取需要加.keyword的字段
//                String key = EsParamHolder.getStringKeyword(tClass, keyword);
//                if (StringUtils.isNotBlank(key)) {
//                    keyword = key;
//                }
//            }
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(keyword, value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return children;
    }


    @Override
    public Children terms(boolean condition, R name, Object... value) {
        if (condition) {
            String keyword = nameToString(name);
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(keyword, value);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Collection<?> values) {
        if (condition) {
            String keyword = nameToString(name);
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(keyword, values);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }


    @Override
    public Children termKeyword(boolean condition, R name, Object value) {
        if (condition) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(nameToString(name) + ".keyword", value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return children;
    }


    @Override
    public Children termsKeyword(boolean condition, R name, Object... values) {
        if (condition) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(nameToString(name) + ".keyword", values);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children termsKeyword(boolean condition, R name, Collection<?> values) {
        if (condition) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(nameToString(name) + ".keyword", values);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children match(boolean condition, R name, Object value) {
        if (condition) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery(nameToString(name), value);
            currentBuilder = matchQuery;
            queryBuilders.add(matchQuery);
        }
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, R name, Object value) {
        if (condition) {
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(nameToString(name), value);
            currentBuilder = matchPhraseQueryBuilder;
            queryBuilders.add(matchPhraseQueryBuilder);
        }
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, R... name) {
        if (condition) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(value, nameToString(name));
            currentBuilder = multiMatchQueryBuilder;
            queryBuilders.add(multiMatchQueryBuilder);
        }
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, R name, Object value) {
        if (condition) {
            MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(nameToString(name), value);
            currentBuilder = matchPhrasePrefixQueryBuilder;
            queryBuilders.add(matchPhrasePrefixQueryBuilder);
        }
        return children;
    }

    @Override
    public Children wildcard(boolean condition, R name, String value) {
        if (condition) {
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(nameToString(name), value);
            currentBuilder = wildcardQueryBuilder;
            queryBuilders.add(wildcardQueryBuilder);
        }
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value) {
        if (condition) {
            FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(nameToString(name), value);
            currentBuilder = fuzzyQueryBuilder;
            queryBuilders.add(fuzzyQueryBuilder);
        }
        return children;
    }


    @Override
    public Children ids(boolean condition, Collection<String> ids) {
        if (condition) {
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds(ids.toArray(new String[ids.size()]));
            currentBuilder = idsQueryBuilder;
            queryBuilders.add(idsQueryBuilder);
        }
        return children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer) {
        if (condition) {
            String name = nameToString(path);
            Function<Class<S>, EsLambdaQueryWrapper<S>> sp = a -> new EsLambdaQueryWrapper<>(sClass);
            EsLambdaQueryWrapper<S> esQueryWrapper = sp.apply(sClass);
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), ScoreMode.None);
            currentBuilder = nestedQueryBuilder;
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer) {
        if (condition) {
            String name = nameToString(path);
            EsQueryWrapper<S> esQueryWrapper = new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(),ScoreMode.None);
            currentBuilder = nestedQueryBuilder;
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode,InnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);

            Function<Class<S>, EsLambdaQueryWrapper<S>> sp = a -> new EsLambdaQueryWrapper<>(sClass);
            EsLambdaQueryWrapper<S> esQueryWrapper = sp.apply(sClass);
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), mode);
            currentBuilder = nestedQueryBuilder;
            nestedQueryBuilder.innerHit(innerHitBuilder);
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode,InnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);
            EsQueryWrapper<S> esQueryWrapper = new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), mode);
            currentBuilder = nestedQueryBuilder;
            nestedQueryBuilder.innerHit(innerHitBuilder);
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public Children gt(boolean condition, R name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).gt(from));
        }
        return children;
    }

    @Override
    public Children ge(boolean condition, R name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).gte(from));
        }
        return children;
    }

    @Override
    public Children lt(boolean condition, R name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).lt(to));
        }
        return children;
    }

    @Override
    public Children le(boolean condition, R name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).lte(to));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to, boolean include) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).from(from, include).to(to, include));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).from(from, true).to(to, true));
        }
        return children;
    }


    @Override
    public Children geoBoundingBox(boolean condition, R name, GeoPoint topLeft, GeoPoint bottomRight) {
        if (condition) {
            GeoBoundingBoxQueryBuilder geoBoundingBox = new GeoBoundingBoxQueryBuilder(nameToString(name));
            geoBoundingBox.setCorners(topLeft, bottomRight);
            currentBuilder = geoBoundingBox;
            queryBuilders.add(geoBoundingBox);
        }
        return children;
    }

    @Override
    public Children geoDistance(boolean condition, R name, String distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        if (condition) {
            GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder(nameToString(name));
            geoDistanceQueryBuilder.distance(distance, distanceUnit);
            geoDistanceQueryBuilder.point(centralGeoPoint);
            currentBuilder = geoDistanceQueryBuilder;
            queryBuilders.add(geoDistanceQueryBuilder);
        }
        return children;
    }

    @Override
    public Children geoPolygon(boolean condition, R name, List<GeoPoint> geoPoints) {
        if (condition) {
            GeoPolygonQueryBuilder geoDistanceQueryBuilder = new GeoPolygonQueryBuilder(nameToString(name), geoPoints);
            currentBuilder = geoDistanceQueryBuilder;
            queryBuilders.add(geoDistanceQueryBuilder);
        }
        return children;
    }

    @Override
    public Children includes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setIncludes(includes);
        return (Children) this;
    }

    @Override
    public Children fetch(boolean fetch) {
        EsSelect esSelect = getSelect();
        esSelect.setFetch(fetch);
        return (Children) this;
    }


    @Override
    public Children excludes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setExcludes(includes);
        return (Children) this;
    }

    @Override
    public Children orderBy(String order, R... columns) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        String[] arr = nameToString(columns);
        for (String name : arr) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(order);
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }

    @Override
    public Children orderBy(String order, R column) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        String name = nameToString(column);
        EsOrder esOrder = new EsOrder();
        esOrder.setName(name);
        esOrder.setSort(order);
        getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        return children;
    }

    @Override
    public Children orderByAsc(String... columns) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.ASC.name());
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }

    @Override
    public Children orderByDesc(String... columns) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.DESC.name());
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }

    @Override
    public Children searchType(SearchType searchType) {
        getEsQueryParamWrapper().setSearchType(searchType);
        return children;
    }

    @Override
    public Children highLight(String field) {
        if (getEsQueryParamWrapper().getEsHighLights() == null) {
            getEsQueryParamWrapper().setEsHighLights(new ArrayList<>());
        }
        EsHighLight esHighLight = new EsHighLight(field);
        getEsQueryParamWrapper().getEsHighLights().add(esHighLight);
        return children;
    }

    @Override
    public Children highLight(String field, String preTag, String postTag) {
        if (getEsQueryParamWrapper().getEsHighLights() == null) {
            getEsQueryParamWrapper().setEsHighLights(new ArrayList<>());
        }
        EsHighLight esHighLight = new EsHighLight(preTag, postTag, field);
        getEsQueryParamWrapper().getEsHighLights().add(esHighLight);
        return children;
    }

    @Override
    public Children routings(String... routings) {
        getEsQueryParamWrapper().setRoutings(routings);
        return children;
    }


    //match方法中配合or使用，百分比匹配
    @Override
    public Children minimumShouldMatch(String minimumShouldMatch) {
        if (currentBuilder instanceof MatchQueryBuilder) {
            ((MatchQueryBuilder) currentBuilder).minimumShouldMatch(minimumShouldMatch);
        }
        return children;
    }
}
