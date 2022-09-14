package com.es.plus.core.wrapper;


import com.es.plus.core.tools.SFunction;
import com.es.plus.pojo.EsHighLight;
import com.es.plus.pojo.EsOrder;
import com.es.plus.pojo.EsSelect;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.core.wrapper.aggregation.EsAggregationWrapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsWrapper<T, R, Children>> extends AbstractLambdaEsWrapper<T, R>
        implements IEsQueryWrapper<Children, Children, R>, EsWrapper<T> {
    protected AbstractEsWrapper() {
    }

    protected List<EsOrder> esOrderList;

    @Override
    public List<EsOrder> getEsOrderList() {
        return esOrderList;
    }

    protected Class<T> tClass;

    protected Children children = (Children) this;

    protected BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

    protected QueryBuilder currentBuilder;

    /*
     *实例
     */
    protected abstract Children instance();

    /*
     *高亮
     */
    protected List<EsHighLight> esHighLights;
    /*
     *聚合封装
     */
    protected EsAggregationWrapper<T> esAggregationWrapper;

    private List<QueryBuilder> queryBuilders = queryBuilder.must();
    //查询结果包含字段
    private EsSelect esSelect;
    //查询结果包含字段
    private SearchType searchType;

    public SearchType getSearchType() {
        return searchType;
    }

    /**
     * 设置es聚合包装 不能删
     *
     * @param esAggregationWrapper es聚合包装
     */
    public void setEsAggregationWrapper(EsAggregationWrapper<T> esAggregationWrapper) {
        this.esAggregationWrapper = esAggregationWrapper;
        esAggregationWrapper.setClass(tClass);
    }

    public EsAggregationWrapper<T> getEsAggregationWrapper() {
        if (esAggregationWrapper == null) {
            esAggregationWrapper = new EsAggregationWrapper<>(tClass);
        }
        return esAggregationWrapper;
    }

    @Override
    protected String nameToString(R function) {
        return super.nameToString(function);
    }


    @Override
    public EsSelect getEsSelect() {
        return esSelect;
    }

    public void setEsSelect(EsSelect esSelect) {
        this.esSelect = esSelect;
    }

    @Override
    public List<EsHighLight> getEsHighLight() {
        return esHighLights;
    }


    @Override
    public BoolQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    //获取select的字段
    @Override
    public EsSelect getSelect() {
        EsSelect esSelect = this.getEsSelect();
        if (esSelect == null) {
            this.setEsSelect(new EsSelect());
        }
        return this.getEsSelect();
    }

    public Children matchAll() {
        queryBuilder.must(QueryBuilders.matchAllQuery());
        return this.children;
    }

    public Children boost(float boost) {
        currentBuilder.boost(boost);
        return this.children;
    }

    @Override
    public Children must(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.must(children.queryBuilder);
        return this.children;
    }

    @Override
    public Children should(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.should(children.queryBuilder);
        return this.children;
    }

    @Override
    public Children mustNot(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.mustNot(children.queryBuilder);
        return this.children;
    }

    @Override
    public Children filters(boolean condition, Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.filter(children.queryBuilder);
        return this.children;
    }

    public Children must() {
        if (queryBuilders != queryBuilder.must()) {
            queryBuilders = queryBuilder.must();
        }
        return children;
    }

    public Children should() {
        if (queryBuilders != queryBuilder.should()) {
            queryBuilders = queryBuilder.should();
        }
        return children;
    }

    public Children filter() {
        if (queryBuilders != queryBuilder.filter()) {
            queryBuilders = queryBuilder.filter();
        }
        return children;
    }

    public Children mustNot() {
        if (queryBuilders != queryBuilder.mustNot()) {
            queryBuilders = queryBuilder.mustNot();
        }
        return children;
    }

    //match方法中配合or使用，百分比匹配
    public void minimumShouldMatch(String minimumShouldMatch) {
        if (currentBuilder instanceof MatchQueryBuilder) {
            ((MatchQueryBuilder) currentBuilder).minimumShouldMatch(minimumShouldMatch);
        }
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
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
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
    public Children between(boolean condition, R name, Object from, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).from(from, true).to(to, true));
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


    /**
     * -----------下面的根据name查询，这里违反了设计原则但是方便了
     */

    @Override
    public Children exists(boolean condition, String name) {
        if (condition) {
            ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(name);
            currentBuilder = existsQueryBuilder;
            queryBuilders.add(existsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children term(boolean condition, String name, Object value) {
        if (condition) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(name, value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return this.children;
    }

    @Override
    public Children terms(boolean condition, String name, Object... value) {
        if (condition) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(name, value);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, String name, Collection<Object> values) {
        if (condition) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(name, values);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children match(boolean condition, String name, Object value) {
        if (condition) {
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(name, value);
            currentBuilder = matchQueryBuilder;
            queryBuilders.add(matchQueryBuilder);
        }
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, String name, Object value) {
        if (condition) {
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(name, value);
            currentBuilder = matchPhraseQueryBuilder;
            queryBuilders.add(matchPhraseQueryBuilder);
        }
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, String... name) {
        if (condition) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(value, name);
            currentBuilder = multiMatchQueryBuilder;
            queryBuilders.add(multiMatchQueryBuilder);
        }
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, String name, Object value) {
        if (condition) {
            MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(name, value);
            currentBuilder = matchPhrasePrefixQueryBuilder;
            queryBuilders.add(matchPhrasePrefixQueryBuilder);
        }
        return children;
    }

    @Override
    public Children wildcard(boolean condition, String name, String value) {
        if (condition) {
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(name, value);
            currentBuilder = wildcardQueryBuilder;
            queryBuilders.add(wildcardQueryBuilder);
        }
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, String name, String value) {
        if (condition) {
            FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(name, value);
            currentBuilder = fuzzyQueryBuilder;
            queryBuilders.add(fuzzyQueryBuilder);
        }
        return children;
    }


    @Override
    public Children gt(boolean condition, String name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).gt(from));
        }
        return children;
    }

    @Override
    public Children ge(boolean condition, String name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).gte(from));
        }
        return children;
    }

    @Override
    public Children lt(boolean condition, String name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).lt(to));
        }
        return children;
    }

    @Override
    public Children le(boolean condition, String name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).lte(to));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).from(from, true).to(to, true));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to, boolean include) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).from(from, include).to(to, include));
        }
        return children;
    }

    public Children includes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setIncludes(includes);
        return (Children) this;
    }

    public Children includes(String... names) {
        EsSelect esSelect = getSelect();
        esSelect.setIncludes(names);
        return (Children) this;
    }

    public Children excludes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setExcludes(includes);
        return (Children) this;
    }

    public Children excludes(String... names) {
        EsSelect esSelect = getSelect();
        esSelect.setExcludes(names);
        return (Children) this;
    }


    public Children orderBy(String order, R... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        String[] arr = nameToString(columns);
        for (String name : arr) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(order);
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderBy(String order, String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(order);
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderByAsc(String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.ASC.name());
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderByDesc(String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.DESC.name());
            esOrderList.add(esOrder);
        }
        return children;
    }

    public void searchType(SearchType searchType) {
        this.searchType = searchType;
    }


    public Children highLight(String field) {
        if (esHighLights == null) {
            esHighLights = new ArrayList<>();
        }
        EsHighLight esHighLight = new EsHighLight(field);
        esHighLights.add(esHighLight);
        return children;
    }

    public Children highLight(String field, String preTag, String postTag) {
        if (esHighLights == null) {
            esHighLights = new ArrayList<>();
        }
        EsHighLight esHighLight = new EsHighLight(preTag, postTag, field);
        esHighLights.add(esHighLight);
        return children;
    }

}
