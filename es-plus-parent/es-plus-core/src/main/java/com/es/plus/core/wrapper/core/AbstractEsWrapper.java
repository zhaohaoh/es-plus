package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.interceptor.EsUpdateField;
import com.es.plus.adapter.params.EsHighLight;
import com.es.plus.adapter.params.EsOrder;
import com.es.plus.adapter.params.EsParamWrapper;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import com.es.plus.adapter.params.EsSelect;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.DateUtil;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsWrapper<T, R, Children extends AbstractEsWrapper<T, R, Children>> extends AbstractLambdaEsWrapper<T, R>
        implements IEsQueryWrapper<Children, Children, R>, EsWrapper<T>, EsExtendsWrapper<Children, R>, EsStaitcsWrapper<Children> {
    protected AbstractEsWrapper() {
        //成员变量比构造方法更快执行
        queryBuilders = esParamWrapper().getEsQueryParamWrapper().getQueryBuilder().must();
    }
    protected AbstractEsWrapper(Class<T> tClass) {
        //成员变量比构造方法更快执行 所以在这里加载
        super.tClass=tClass;
        queryBuilders = esParamWrapper().getEsQueryParamWrapper().getQueryBuilder().must();
    }

    protected Children children = (Children) this;

    protected QueryBuilder currentBuilder;
    
    /*
     *实例
     */
    protected abstract Children instance();

    private EsParamWrapper<T> esParamWrapper;

    private List<QueryBuilder> queryBuilders;

    protected EsLambdaAggWrapper<T> esLambdaAggWrapper;

    protected EsAggWrapper<T> esAggWrapper;
    
    protected String[] indexs;
    
    protected String type;
    
    @Override
    public String[] getIndexs() {
        return indexs;
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
    public EsParamWrapper<T> esParamWrapper() {
        if (esParamWrapper == null) {
            esParamWrapper = new EsParamWrapper<>();
            if (tClass!=null){
                esParamWrapper.setTClass(tClass);
            }
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
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            esQueryParamWrapper.setAggregationBuilder(esLambdaAggWrapper.getAggregationBuilder());
        }
        return esLambdaAggWrapper;
    }

    @Override
    public EsAggWrapper<T> esAggWrapper() {
        if (esAggWrapper == null) {
            esAggWrapper = new EsAggWrapper<>(tClass);
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            esQueryParamWrapper.setAggregationBuilder(esAggWrapper.getAggregationBuilder());
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
        return esParamWrapper().getEsQueryParamWrapper().getQueryBuilder();
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
        if (!condition){
            return this.children;
        }
        final Children children = instance();
        children.parentFieldName = super.parentFieldName;
        consumer.accept(children);
        BoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
            return this.children;
        }
        this.children.getQueryBuilder().must(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children should(boolean condition, Consumer<Children> consumer) {
        if (!condition){
            return this.children;
        }
        final Children children = instance();
        children.parentFieldName = super.parentFieldName;
        consumer.accept(children);
        BoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
            return this.children;
        }
        this.children.getQueryBuilder().should(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children mustNot(boolean condition, Consumer<Children> consumer) {
        if (!condition){
            return this.children;
        }
        final Children children = instance();
        children.parentFieldName = super.parentFieldName;
        consumer.accept(children);
        BoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
            return this.children;
        }
        this.children.getQueryBuilder().mustNot(children.getQueryBuilder());
        return this.children;
    }

    @Override
    public Children filter(boolean condition, Consumer<Children> consumer) {
        if (!condition){
            return this.children;
        }
        final Children children = instance();
        children.parentFieldName = super.parentFieldName;
        consumer.accept(children);
        BoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
            return this.children;
        }
        this.children.getQueryBuilder().filter(children.getQueryBuilder());
        return this.children;
    }

    /**
     * 根据子文档条件查询父文档  待优化自动获取type
     */
    @Override
    public Children hasChild(boolean condition, String childType, ScoreMode scoreMode, Consumer<Children> consumer) {
        final Children children = instance();
        children.parentFieldName = super.parentFieldName;
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
        children.parentFieldName = super.parentFieldName;
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
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                value = DateUtil.format(value, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(keyword, value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children script(boolean condition,Script script) {
        if (condition) {
            ScriptQueryBuilder scriptQueryBuilder = QueryBuilders.scriptQuery(script);
            currentBuilder = scriptQueryBuilder;
            queryBuilders.add(scriptQueryBuilder);
        }
        return children;
    }


    @Override
    public Children terms(boolean condition, R name, Object... values) {
        if (condition) {
            String keyword = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            TermsQueryBuilder termsQueryBuilder;
            if (esFieldInfo != null) {
                List<Object> list = Arrays.stream(values).map(v -> DateUtil.format(v, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone())).collect(Collectors.toList());
                termsQueryBuilder = QueryBuilders.termsQuery(keyword, list);
            } else {
                termsQueryBuilder = QueryBuilders.termsQuery(keyword, values);
            }
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Collection<?> values) {
        if (condition) {
            String column = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                values = values.stream().map(v -> DateUtil.format(v, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone())).collect(Collectors.toList());
            }
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(column, values);
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
            String wildcardName = nameToString(name);
            Integer queryLimit = GlobalConfigCache.GLOBAL_CONFIG.getWildcardQueryLimit();
            String queryValue = value;
            if (queryLimit!=null && queryLimit >=0){
                queryValue = StringUtils.substring(value,0,queryLimit);
            }
            queryValue = "*"+queryValue+"*";
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(wildcardName , queryValue);
            currentBuilder = wildcardQueryBuilder;
            queryBuilders.add(wildcardQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children wildcardKeyword(boolean condition, R name, String value) {
        if (condition) {
            String wildcardName = nameToString(name) +".keyword";
            Integer queryLimit = GlobalConfigCache.GLOBAL_CONFIG.getWildcardQueryLimit();
            String queryValue = value;
            if (queryLimit!=null && queryLimit >=0){
                queryValue = StringUtils.substring(value,0,queryLimit);
            }
            queryValue = "*"+queryValue+"*";
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(wildcardName , queryValue);
            currentBuilder = wildcardQueryBuilder;
            queryBuilders.add(wildcardQueryBuilder);
        }
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value,Fuzziness fuzziness) {
        if (condition) {
            FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(nameToString(name), value);
            fuzzyQueryBuilder.fuzziness(fuzziness);
            fuzzyQueryBuilder.prefixLength();
            currentBuilder = fuzzyQueryBuilder;
            queryBuilders.add(fuzzyQueryBuilder);
        }
        return children;
    }

    @Override
    public Children fuzzy(boolean condition, R name, String value,Fuzziness fuzziness,int prefixLength) {
        if (condition) {
            FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(nameToString(name), value);
            if (fuzziness!=null) {
                fuzzyQueryBuilder.fuzziness(fuzziness);
            }
            fuzzyQueryBuilder.prefixLength(prefixLength);
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
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
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
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                    CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), ScoreMode.None);
            currentBuilder = nestedQueryBuilder;
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);
            Function<Class<S>, EsLambdaQueryWrapper<S>> sp = a -> new EsLambdaQueryWrapper<>(sClass);
            EsLambdaQueryWrapper<S> esQueryWrapper = sp.apply(sClass);
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                    CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), mode);
            currentBuilder = nestedQueryBuilder;
            nestedQueryBuilder.innerHit(innerHitBuilder);
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }
    
    
    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer,
            ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);
            EsQueryWrapper<S> esQueryWrapper =  new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                    CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(name, esQueryWrapper.getQueryBuilder(), mode);
            currentBuilder = nestedQueryBuilder;
            nestedQueryBuilder.innerHit(innerHitBuilder);
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }
    
    @Override
    public <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer) {
        if (condition) {
            EsQueryWrapper<S> esQueryWrapper = new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = path;
            consumer.accept(esQueryWrapper);
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                    CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(path, esQueryWrapper.getQueryBuilder(), ScoreMode.None);
            currentBuilder = nestedQueryBuilder;
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }
    
    @Override
    public <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer, ScoreMode mode, InnerHitBuilder innerHitBuilder) {
        if (condition) {
            EsQueryWrapper<S> esQueryWrapper = new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = path;
            consumer.accept(esQueryWrapper);
            BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (CollectionUtils.isEmpty(queryBuilder.must())  &&CollectionUtils.isEmpty(queryBuilder.mustNot()) &&
                    CollectionUtils.isEmpty(queryBuilder.filter()) &&CollectionUtils.isEmpty(queryBuilder.should())){
                return this.children;
            }
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(path, esQueryWrapper.getQueryBuilder(), mode);
            currentBuilder = nestedQueryBuilder;
            nestedQueryBuilder.innerHit(innerHitBuilder);
            this.queryBuilders.add(nestedQueryBuilder);
        }
        return this.children;
    }

    @Override
    public Children gt(boolean condition, R name, Object from) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                from = DateUtil.format(from, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).gt(from));
        }
        return children;
    }

    @Override
    public Children ge(boolean condition, R name, Object from) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                from = DateUtil.format(from, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).gte(from));
        }
        return children;
    }

    @Override
    public Children lt(boolean condition, R name, Object to) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                to = DateUtil.format(to, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).lt(to));
        }
        return children;
    }

    @Override
    public Children le(boolean condition, R name, Object to) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                to = DateUtil.format(to, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).lte(to));
        }
        return children;
    }

    @Override
    public Children range(boolean condition, R name, Object from, Object to, boolean fromInclude, boolean toInclude) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                from = DateUtil.format(from, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
                to = DateUtil.format(to, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).from(from, fromInclude).to(to, toInclude));
        }
        return children;
    }

    @Override
    public Children range(boolean condition, R name, Object from, Object to) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                from = DateUtil.format(from, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
                to = DateUtil.format(to, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).from(from, true).to(to, true));
        }
        return children;
    }

    @Override
    public Children range(boolean condition, R name, Object from, Object to, String timeZone) {
        if (condition) {
            String esName = nameToString(name);
            String fieldName = nameToFieldName(name);
            EsFieldInfo esFieldInfo = GlobalParamHolder.getIndexField(tClass, fieldName);
            if (esFieldInfo != null) {
                from = DateUtil.format(from, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
                to = DateUtil.format(to, esFieldInfo.getDateFormat(),esFieldInfo.getTimeZone());
            }
            queryBuilders.add(QueryBuilders.rangeQuery(esName).from(from, true).to(to, true).timeZone(timeZone));
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
    public Children minScope(float minScope) {
        EsSelect esSelect = getSelect();
        esSelect.setMinScope(minScope);
        return (Children) this;
    }
    
    //在字段上排序时，不会计算分数。 通过将 track_scores 设置为 true，仍将计算和跟踪分数
    @Override
    public Children trackScores(boolean trackScores) {
        EsSelect esSelect = getSelect();
        esSelect.setTrackScores(trackScores);
        return (Children) this;
    }
    
    @Override
    public Children trackTotalHits(boolean trackTotalHits) {
        EsSelect esSelect = getSelect();
        esSelect.setTrackTotalHits(trackTotalHits);
        return (Children) this;
    }
    
    @Override
    public Children sortBy(String order, R... columns) {
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
    public Children sortBy(String order, R column) {
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
    public Children sortBy(String order, NestedSortBuilder nestedSortBuilder,String... name){
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        for (String s : name) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(s);
            esOrder.setSort(order);
            esOrder.setNestedSortBuilder(nestedSortBuilder);
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        
        return children;
    }
    
    @Override
    public Children sortByAsc(String path,String[] columns) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(path);
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.ASC.name());
            esOrder.setNestedSortBuilder(nestedSortBuilder);
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }
    
    @Override
    public Children sortByDesc(String path,String[] columns) {
        if (getEsQueryParamWrapper().getEsOrderList() == null) {
            getEsQueryParamWrapper().setEsOrderList(new ArrayList<>());
        }
        NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(path);
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.DESC.name());
            esOrder.setNestedSortBuilder(nestedSortBuilder);
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }

    @Override
    public Children sortByAsc(String... columns) {
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
    public Children sortByDesc(String... columns) {
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


    //match方法中配合or使用，百分比匹配
    @Override
    public Children minimumShouldMatch(String minimumShouldMatch) {
        if (currentBuilder instanceof MatchQueryBuilder) {
            ((MatchQueryBuilder) currentBuilder).minimumShouldMatch(minimumShouldMatch);
        }
        return children;
    }

    @Override
    public Children routings(String... routings) {
        getEsQueryParamWrapper().setRoutings(routings);
        return children;
    }


    @Override
    public Children preference(String preference) {
        getEsQueryParamWrapper().setPreference(preference);
        return children;
    }

    @Override
    public Children searchAfterValues(Object[] searchAfterValues) {
        getEsQueryParamWrapper().setSearchAfterValues(searchAfterValues);
        return children;
    }
}
