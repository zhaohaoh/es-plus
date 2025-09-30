package com.es.plus.core.wrapper.core;

import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.interceptor.EsUpdateField;
import com.es.plus.common.params.EsHighLight;
import com.es.plus.common.params.EsOrder;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.params.EsQueryParamWrapper;
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
import com.es.plus.common.pojo.es.EpSortOrder;
import com.es.plus.common.properties.EsFieldInfo;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.DateUtil;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import org.apache.commons.lang3.StringUtils;
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
        queryBuilders = esParamWrapper().getEsQueryParamWrapper().getBoolQueryBuilder().must();
    }
    protected AbstractEsWrapper(Class<T> tClass) {
        //成员变量比构造方法更快执行 所以在这里加载
        super.tClass=tClass;
        queryBuilders = esParamWrapper().getEsQueryParamWrapper().getBoolQueryBuilder().must();
    }
    
    protected Children children = (Children) this;
    
    protected EpQueryBuilder currentBuilder;
    
    /*
     *实例
     */
    protected abstract Children instance();
    
    private EsParamWrapper<T> esParamWrapper;
    
    private List<EpQueryBuilder> queryBuilders;
    
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
    
    public EpBoolQueryBuilder getQueryBuilder() {
        return   esParamWrapper().getEsQueryParamWrapper().getBoolQueryBuilder();
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
        getQueryBuilder().must(new EpQueryBuilder("match_all", "match_all"));
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
        EpBoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.getMustClauses())  &&CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) &&
                CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) &&CollectionUtils.isEmpty(queryBuilder.getShouldClauses())){
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
        EpBoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.getMustClauses())  &&CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) &&
                CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) &&CollectionUtils.isEmpty(queryBuilder.getShouldClauses())){
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
        EpBoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.getMustClauses())  &&CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) &&
                CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) &&CollectionUtils.isEmpty(queryBuilder.getShouldClauses())){
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
        EpBoolQueryBuilder queryBuilder = children.getQueryBuilder();
        if (CollectionUtils.isEmpty(queryBuilder.getMustClauses())  &&CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) &&
                CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) &&CollectionUtils.isEmpty(queryBuilder.getShouldClauses())){
            return this.children;
        }
        this.children.getQueryBuilder().filter(children.getQueryBuilder());
        return this.children;
    }
    
    /**
     * 根据子文档条件查询父文档  待优化自动获取type
     */
    @Override
    public Children hasChild(boolean condition, String childType, EpScoreMode scoreMode, Consumer<Children> consumer) {
        if (condition) {
            final Children children = instance();
            children.parentFieldName = super.parentFieldName;
            consumer.accept(children);
            EpBoolQueryBuilder childQueryBuilder = children.getQueryBuilder();
            if (!CollectionUtils.isEmpty(childQueryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(childQueryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(childQueryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(childQueryBuilder.getShouldClauses())) {
                EpQueryBuilder hasChildQuery = new EpQueryBuilder("has_child", "has_child")
                        .param("type", childType)
                        .param("query", childQueryBuilder)
                        .param("score_mode", EpScoreMode.valueOf(scoreMode.name()));
                queryBuilders.add(hasChildQuery);
                currentBuilder = hasChildQuery;
            }
        }
        return this.children;
    }
    
    /**
     * 根据父文档条件查询子文档 待优化自动获取type
     */
    @Override
    public Children hasParent(boolean condition, String parentType, Boolean scoreMode, Consumer<Children> consumer) {
        if (condition) {
            final Children children = instance();
            children.parentFieldName = super.parentFieldName;
            consumer.accept(children);
            EpBoolQueryBuilder parentQueryBuilder = children.getQueryBuilder();
            if (!CollectionUtils.isEmpty(parentQueryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(parentQueryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(parentQueryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(parentQueryBuilder.getShouldClauses())) {
                EpQueryBuilder hasParentQuery = new EpQueryBuilder("has_parent", "has_parent")
                        .param("type", parentType)
                        .param("query", parentQueryBuilder)
                        .param("score_mode", scoreMode);
                queryBuilders.add(hasParentQuery);
                currentBuilder = hasParentQuery;
            }
        }
        return this.children;
    }
    
    @Override
    public Children parentIdQuery(boolean condition, String childType, String id) {
        if (condition) {
            EpQueryBuilder parentIdQuery = new EpQueryBuilder("parent_id", "parent_id")
                    .param("type", childType)
                    .param("id", id);
            queryBuilders.add(parentIdQuery);
            currentBuilder = parentIdQuery;
        }
        return this.children;
    }
    
    @Override
    public Children must() {
        // 切换到must子句列表
        queryBuilders = getQueryBuilder().getMustClauses();
        
        return children;
    }
    
    @Override
    public Children should() {
        // 切换到should子句列表
        queryBuilders = getQueryBuilder().getShouldClauses();
        return children;
    }
    
    @Override
    public Children filter() {
        // 切换到filter子句列表
        queryBuilders = getQueryBuilder().getFilterClauses();
        return children;
    }
    
    @Override
    public Children mustNot() {
        // 切换到mustNot子句列表
        queryBuilders = getQueryBuilder().getMustNotClauses();
        return children;
    }
    
    @Override
    public Children esQuery(boolean condition, EpQueryBuilder queryBuilder) {
        if (condition) {
            queryBuilders.add(queryBuilder);
        }
        return children;
    }
    
    @Override
    public Children exists(boolean condition, R name) {
        if (condition) {
            EpQueryBuilder existsQueryBuilder = new EpQueryBuilder(nameToString(name), "exists")
                    .param("field", nameToString(name));
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
            EpQueryBuilder termQueryBuilder = new EpQueryBuilder(keyword, "term")
                    .param("field", keyword)
                    .param("value", value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children script(boolean condition, EpScript script) {
        if (condition) {
            EpQueryBuilder scriptQueryBuilder = new EpQueryBuilder("script", "script")
                    .param("script", script);
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
            if (esFieldInfo != null) {
                values = Arrays.stream(values)
                        .map(v -> DateUtil.format(v, esFieldInfo.getDateFormat(), esFieldInfo.getTimeZone()))
                        .toArray();
            }
            EpQueryBuilder termsQueryBuilder = new EpQueryBuilder(keyword, "terms")
                    .param("field", keyword)
                    .param("values", values);
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
                values = values.stream()
                        .map(v -> DateUtil.format(v, esFieldInfo.getDateFormat(), esFieldInfo.getTimeZone()))
                        .collect(Collectors.toList());
            }
            EpQueryBuilder termsQueryBuilder = new EpQueryBuilder(column, "terms")
                    .param("field", column)
                    .param("values", values.toArray());
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }
    
    
    @Override
    public Children termKeyword(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name) + ".keyword";
            EpQueryBuilder termQueryBuilder = new EpQueryBuilder(keyword, "term")
                    .param("field", keyword)
                    .param("value", value);
            currentBuilder = termQueryBuilder;
            queryBuilders.add(termQueryBuilder);
        }
        return children;
    }
    
    
    @Override
    public Children termsKeyword(boolean condition, R name, Object... values) {
        if (condition) {
            String keyword = nameToString(name) + ".keyword";
            EpQueryBuilder termsQueryBuilder = new EpQueryBuilder(keyword, "terms")
                    .param("field", keyword)
                    .param("values", values);
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children termsKeyword(boolean condition, R name, Collection<?> values) {
        if (condition) {
            String keyword = nameToString(name) + ".keyword";
            EpQueryBuilder termsQueryBuilder = new EpQueryBuilder(keyword, "terms")
                    .param("field", keyword)
                    .param("values", values.toArray());
            currentBuilder = termsQueryBuilder;
            queryBuilders.add(termsQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children match(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name);
            EpQueryBuilder matchQuery = new EpQueryBuilder(keyword, "match")
                    .param("field", keyword)
                    .param("value", value);
            currentBuilder = matchQuery;
            queryBuilders.add(matchQuery);
        }
        return children;
    }
    
    @Override
    public Children matchPhrase(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name);
            EpQueryBuilder matchPhraseQueryBuilder = new EpQueryBuilder(keyword, "match_phrase")
                    .param("field", keyword)
                    .param("value", value);
            currentBuilder = matchPhraseQueryBuilder;
            queryBuilders.add(matchPhraseQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children multiMatch(boolean condition, Object value, R... name) {
        if (condition) {
            String[] fieldNames = nameToString(name);
            EpQueryBuilder multiMatchQueryBuilder = new EpQueryBuilder("multi_match", "multi_match")
                    .param("value", value)
                    .param("fields", fieldNames);
            currentBuilder = multiMatchQueryBuilder;
            queryBuilders.add(multiMatchQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children matchPhrasePrefix(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name);
            EpQueryBuilder matchPhrasePrefixQueryBuilder = new EpQueryBuilder(keyword, "match_phrase_prefix")
                    .param("field", keyword)
                    .param("value", value);
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
            EpQueryBuilder wildcardQueryBuilder = new EpQueryBuilder(wildcardName, "wildcard")
                    .param("field", wildcardName)
                    .param("value", queryValue);
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
            EpQueryBuilder wildcardQueryBuilder = new EpQueryBuilder(wildcardName, "wildcard")
                    .param("field", wildcardName)
                    .param("value", queryValue);
            currentBuilder = wildcardQueryBuilder;
            queryBuilders.add(wildcardQueryBuilder);
        }
        return children;
    }
    
    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness) {
        if (condition) {
            String keyword = nameToString(name);
            EpQueryBuilder fuzzyQueryBuilder = new EpQueryBuilder(keyword, "fuzzy")
                    .param("field", keyword)
                    .param("value", value)
                    .param("fuzziness", fuzziness.getFuzziness());
            currentBuilder = fuzzyQueryBuilder;
            queryBuilders.add(fuzzyQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children fuzzy(boolean condition, R name, String value, EpFuzziness fuzziness, int prefixLength) {
        if (condition) {
            String keyword = nameToString(name);
            EpQueryBuilder fuzzyQueryBuilder = new EpQueryBuilder(keyword, "fuzzy")
                    .param("field", keyword)
                    .param("value", value)
                    .param("fuzziness", fuzziness != null ? fuzziness.getFuzziness() : null)
                    .param("prefix_length", prefixLength);
            currentBuilder = fuzzyQueryBuilder;
            queryBuilders.add(fuzzyQueryBuilder);
        }
        return children;
    }
    
    
    @Override
    public Children ids(boolean condition, Collection<String> ids) {
        if (condition) {
            EpQueryBuilder idsQueryBuilder = new EpQueryBuilder("ids", "ids")
                    .param("values", ids.toArray(new String[0]));
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
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(name, "nested")
                        .param("path", name)
                        .param("query", queryBuilder)
                        .param("score_mode", EpScoreMode.None);
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
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
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(name, "nested")
                        .param("path", name)
                        .param("query", queryBuilder)
                        .param("score_mode", EpScoreMode.None);
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
        }
        return this.children;
    }
    
    @Override
    public <S> Children nestedQuery(boolean condition, R path, Class<S> sClass, Consumer<EsLambdaQueryWrapper<S>> consumer, EpScoreMode mode,  EpInnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);
            Function<Class<S>, EsLambdaQueryWrapper<S>> sp = a -> new EsLambdaQueryWrapper<>(sClass);
            EsLambdaQueryWrapper<S> esQueryWrapper = sp.apply(sClass);
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(name, "nested")
                        .param("path", name)
                        .param("query", queryBuilder)
                        .param("inner_hit", innerHitBuilder) // 添加这行来存储innerHitBuilder
                        .param("score_mode", EpScoreMode.valueOf(mode.name()));
                // innerHitBuilder无法直接处理，需要在转换时处理
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
        }
        return this.children;
    }
    
    
    @Override
    public <S> Children nestedQuery(boolean condition, R path, Consumer<EsQueryWrapper<S>> consumer,
            EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        if (condition) {
            String name = nameToString(path);
            EsQueryWrapper<S> esQueryWrapper =  new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = name;
            consumer.accept(esQueryWrapper);
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(name, "nested")
                        .param("path", name)
                        .param("query", queryBuilder)
                        .param("inner_hit", innerHitBuilder) // 添加这行来存储innerHitBuilder
                        .param("score_mode", EpScoreMode.valueOf(mode.name()));
                // innerHitBuilder无法直接处理，需要在转换时处理
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
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
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(path, "nested")
                        .param("path", path)
                        .param("query", queryBuilder)
                        .param("score_mode", EpScoreMode.None);
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
        }
        return this.children;
    }
    
    @Override
    public <S> Children nested(boolean condition, String path, Consumer<EsQueryWrapper<S>> consumer, EpScoreMode mode, EpInnerHitBuilder innerHitBuilder) {
        if (condition) {
            EsQueryWrapper<S> esQueryWrapper = new EsQueryWrapper<>();
            //嵌套对象增加父字段名
            esQueryWrapper.parentFieldName = path;
            consumer.accept(esQueryWrapper);
            EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
            if (!CollectionUtils.isEmpty(queryBuilder.getMustClauses()) || !CollectionUtils.isEmpty(queryBuilder.getMustNotClauses()) ||
                    !CollectionUtils.isEmpty(queryBuilder.getFilterClauses()) || !CollectionUtils.isEmpty(queryBuilder.getShouldClauses())) {
                EpQueryBuilder nestedQueryBuilder = new EpQueryBuilder(path, "nested")
                        .param("path", path)
                        .param("query", queryBuilder)
                        .param("inner_hit", innerHitBuilder) // 添加这行来存储innerHitBuilder
                        .param("score_mode", EpScoreMode.valueOf(mode.name()));
                // innerHitBuilder无法直接处理，需要在转换时处理
                currentBuilder = nestedQueryBuilder;
                this.queryBuilders.add(nestedQueryBuilder);
            }
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("from", from)
                    .param("includeLower", false)
                    .param("includeUpper", false);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("from", from)
                    .param("includeLower", true)
                    .param("includeUpper", false);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("to", to)
                    .param("includeLower", false)
                    .param("includeUpper", false);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("to", to)
                    .param("includeLower", false)
                    .param("includeUpper", true);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("from", from)
                    .param("to", to)
                    .param("includeLower", fromInclude)
                    .param("includeUpper", toInclude);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("from", from)
                    .param("to", to)
                    .param("includeLower", true)
                    .param("includeUpper", true);
            queryBuilders.add(rangeQuery);
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
            EpQueryBuilder rangeQuery = new EpQueryBuilder(esName, "range")
                    .param("field", esName)
                    .param("from", from)
                    .param("to", to)
                    .param("includeLower", true)
                    .param("includeUpper", true)
                    .param("time_zone", timeZone);
            queryBuilders.add(rangeQuery);
        }
        return children;
    }
    
    
    @Override
    public Children geoBoundingBox(boolean condition, R name, EpGeoPoint topLeft, EpGeoPoint bottomRight) {
        if (condition) {
            String fieldName = nameToString(name);
            EpQueryBuilder geoBoundingBoxQuery = new EpQueryBuilder(fieldName, "geo_bounding_box")
                    .param("field", fieldName)
                    .param("top_left_lat", topLeft.getLat())
                    .param("top_left_lon", topLeft.getLon())
                    .param("bottom_right_lat", bottomRight.getLat())
                    .param("bottom_right_lon", bottomRight.getLon());
            currentBuilder = geoBoundingBoxQuery;
            queryBuilders.add(geoBoundingBoxQuery);
        }
        return children;
    }
    
    @Override
    public Children geoDistance(boolean condition, R name, String distance, EpDistanceUnit distanceUnit, EpGeoPoint centralGeoPoint) {
        if (condition) {
            String fieldName = nameToString(name);
            EpQueryBuilder geoDistanceQueryBuilder = new EpQueryBuilder(fieldName, "geo_distance")
                    .param("field", fieldName)
                    .param("distance", distance)
                    .param("unit", distanceUnit.getUnit())
                    .param("lat", centralGeoPoint.getLat())
                    .param("lon", centralGeoPoint.getLon());
            currentBuilder = geoDistanceQueryBuilder;
            queryBuilders.add(geoDistanceQueryBuilder);
        }
        return children;
    }
    
    @Override
    public Children geoPolygon(boolean condition, R name, List<EpGeoPoint> geoPoints) {
        if (condition) {
            String fieldName = nameToString(name);
            EpQueryBuilder geoPolygonQueryBuilder = new EpQueryBuilder(fieldName, "geo_polygon")
                    .param("field", fieldName)
                    .param("points", geoPoints);
            currentBuilder = geoPolygonQueryBuilder;
            queryBuilders.add(geoPolygonQueryBuilder);
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
    public Children sortBy(String order, EpNestedSortBuilder nestedSortBuilder,String... name){
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
        EpNestedSortBuilder nestedSortBuilder = new EpNestedSortBuilder(path);
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(EpSortOrder.ASC.toString());
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
        EpNestedSortBuilder nestedSortBuilder = new EpNestedSortBuilder(path);
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(EpSortOrder.DESC.toString());
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
            esOrder.setSort(EpSortOrder.ASC.toString());
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
            esOrder.setSort(EpSortOrder.DESC.toString());
            getEsQueryParamWrapper().getEsOrderList().add(esOrder);
        }
        return children;
    }
    
    @Override
    public Children searchType(EpSearchType searchType) {
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
        if (currentBuilder != null) {
            // 添加minimum_should_match参数
            currentBuilder.param("minimum_should_match", minimumShouldMatch);
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
    @Override
    public Children profile() {
        getEsQueryParamWrapper().setProfile(true);
        return this.children;
    }
}