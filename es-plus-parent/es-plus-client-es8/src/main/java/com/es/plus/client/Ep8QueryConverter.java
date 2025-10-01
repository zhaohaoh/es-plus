package com.es.plus.client;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.es.plus.common.params.EsHighLight;
import com.es.plus.common.params.EsOrder;
import com.es.plus.common.pojo.es.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EpQueryBuilder到Elasticsearch Java API Query的转换工厂类
 */
public class Ep8QueryConverter {
    
    /**
     * 将自定义EpScoreMode转换为Elasticsearch ScoreMode
     *
     * @param epScoreMode 自定义评分模式
     * @return Elasticsearch评分模式
     */
    private static ChildScoreMode toEsScoreMode(EpScoreMode epScoreMode) {
        if (epScoreMode == null) {
            return ChildScoreMode.None;
        }
        
        switch (epScoreMode) {
            case None:
                return ChildScoreMode.None;
            case Avg:
                return ChildScoreMode.Avg;
            case Max:
                return ChildScoreMode.Max;
            case Min:
                return ChildScoreMode.Min;
            default:
                return ChildScoreMode.None;
        }
    }
    
    /**
     * 将EpQueryBuilder转换为Elasticsearch原生Query
     *
     * @param epQuery 自定义查询构建器
     * @return Elasticsearch原生Query
     */
    public static Query toEsQuery(EpQueryBuilder epQuery) {
        if (epQuery == null) {
            return null;
        }
        
        Object esOrginalQuery = epQuery.getEsOrginalQuery();
        if (esOrginalQuery !=null){
            if (esOrginalQuery instanceof Query){
                return (Query) esOrginalQuery;
            }
            else if (esOrginalQuery instanceof ObjectBuilder){
                ObjectBuilder<? extends QueryVariant> orginalQueryObjectBuilder = (ObjectBuilder<? extends QueryVariant>) esOrginalQuery;
                return orginalQueryObjectBuilder.build()._toQuery();
                
            }
        }
        
        // 如果是BoolQueryBuilder特殊处理
        if (epQuery instanceof EpBoolQueryBuilder) {
            return toEsBoolQuery((EpBoolQueryBuilder) epQuery);
        }
        
        String type = epQuery.getType();
        Map<String, Object> params = epQuery.getParameters();
        
        Query esQuery = null;
        
        switch (type) {
            case "term":
                String termField = (String) params.get("field");
                Object termValue = params.get("value");
                esQuery = Query.of(q -> q.term(t -> t.field(termField).value(FieldValue.of(termValue.toString()))));
                break;
            
            case "terms":
                String termsField = (String) params.get("field");
                Object[] termsValues = (Object[]) params.get("values");
                esQuery = Query.of(q -> q.terms(t -> t.field(termsField).terms(v -> {
                    List<FieldValue> fieldValues = Arrays.stream(termsValues).map(FieldValue::of)
                            .collect(Collectors.toList());
                    v.value(fieldValues);
                    return v;
                })));
                break;
            
            case "match":
                String matchField = (String) params.get("field");
                Object matchValue = params.get("value");
                esQuery = Query.of(q -> q.match(m -> m.field(matchField).query(matchValue.toString())));
                break;
            
            case "match_all":
                esQuery = Query.of(q -> q.matchAll(m -> m));
                break;
            
            case "range":
                String rangeField = (String) params.get("field");
                RangeQuery.Builder rangeQueryBuilder = new RangeQuery.Builder();
                rangeQueryBuilder.untyped(B-> {
                    UntypedRangeQuery.Builder field = B.field(rangeField);
                    if (params.containsKey("from")) {
                        Object from = params.get("from");
                        if (from != null) {
                            field.gte(JsonData.of(from));
                        }
                    }
                    if (params.containsKey("to")) {
                        Object to = params.get("to");
                        if (to != null) {
                            field.lte(JsonData.of(to));
                        }
                    }
                    return field;
                });

                esQuery = Query.of(q -> q.range(rangeQueryBuilder.build()));
                break;
            
            case "exists":
                String existsField = (String) params.get("field");
                esQuery = Query.of(q -> q.exists(e -> e.field(existsField)));
                break;
            
            case "wildcard":
                String wildcardField = (String) params.get("field");
                String wildcardValue = (String) params.get("value");
                esQuery = Query.of(q -> q.wildcard(w -> w.field(wildcardField).value(wildcardValue)));
                break;
            
            case "prefix":
                String prefixField = (String) params.get("field");
                String prefixValue = (String) params.get("value");
                esQuery = Query.of(q -> q.prefix(p -> p.field(prefixField).value(prefixValue)));
                break;
            
            case "fuzzy":
                String fuzzyField = (String) params.get("field");
                Object fuzzyValue = params.get("value");
                Object fuzziness = params.get("fuzziness");
                FuzzyQuery.Builder fuzzyQueryBuilder = new FuzzyQuery.Builder();
                fuzzyQueryBuilder.field(fuzzyField).value(fuzzyValue.toString());
                if (fuzziness != null) {
                    if (fuzziness instanceof EpFuzziness) {
                        fuzzyQueryBuilder.fuzziness(((EpFuzziness) fuzziness).getFuzziness());
                    } else {
                        fuzzyQueryBuilder.fuzziness(fuzziness.toString());
                    }
                }
                if (params.containsKey("prefix_length")) {
                    fuzzyQueryBuilder.prefixLength((Integer) params.get("prefix_length"));
                }
                esQuery = Query.of(q -> q.fuzzy(fuzzyQueryBuilder.build()));
                break;
            
            case "regexp":
                String regexpField = (String) params.get("field");
                String regexpValue = (String) params.get("value");
                esQuery = Query.of(q -> q.regexp(r -> r.field(regexpField).value(regexpValue)));
                break;
            
            case "nested":
                String nestedPath = (String) params.get("path");
                EpQueryBuilder nestedQuery = (EpQueryBuilder) params.get("query");
                Object scoreModeObj = params.get("score_mode");
                ChildScoreMode scoreMode;
                if (scoreModeObj instanceof EpScoreMode) {
                    scoreMode = toEsScoreMode((EpScoreMode) scoreModeObj);
                } else if (scoreModeObj instanceof ChildScoreMode) {
                    scoreMode = ( ChildScoreMode) scoreModeObj;
                } else {
                    scoreMode = ChildScoreMode.None;
                }
                
                esQuery = Query.of(q -> q.nested(n -> n.path(nestedPath)
                        .query(toEsQuery(nestedQuery))
                        .scoreMode(scoreMode)));
                break;
            
            case "ids":
                String[] ids = (String[]) params.get("values");
                esQuery = Query.of(q -> q.ids(i -> i.values(Arrays.asList(ids))));
                break;
            
            case "match_phrase":
                String matchPhraseField = (String) params.get("field");
                Object matchPhraseValue = params.get("value");
                esQuery = Query.of(q -> q.matchPhrase(m -> m.field(matchPhraseField).query(matchPhraseValue.toString())));
                break;
            
            case "match_phrase_prefix":
                String matchPhrasePrefixField = (String) params.get("field");
                Object matchPhrasePrefixValue = params.get("value");
                Integer maxExpansions = (Integer) params.get("max_expansions");
                MatchPhrasePrefixQuery.Builder matchPhrasePrefixQueryBuilder = new MatchPhrasePrefixQuery.Builder();
                matchPhrasePrefixQueryBuilder.field(matchPhrasePrefixField).query(matchPhrasePrefixValue.toString());
                if (maxExpansions != null) {
                    matchPhrasePrefixQueryBuilder.maxExpansions(maxExpansions);
                }
                esQuery = Query.of(q -> q.matchPhrasePrefix(matchPhrasePrefixQueryBuilder.build()));
                break;
            
            case "multi_match":
                Object multiMatchValue = params.get("value");
                String[] multiMatchFields = (String[]) params.get("fields");
                MultiMatchQuery.Builder multiMatchQueryBuilder = new MultiMatchQuery.Builder();
                multiMatchQueryBuilder.query(multiMatchValue.toString()).fields(Arrays.asList(multiMatchFields));
                esQuery = Query.of(q -> q.multiMatch(multiMatchQueryBuilder.build()));
                break;
            
            case "query_string":
                String queryString = (String) params.get("query");
                esQuery = Query.of(q -> q.queryString(qs -> qs.query(queryString)));
                break;
            
            case "simple_query_string":
                String simpleQueryString = (String) params.get("query");
                esQuery = Query.of(q -> q.simpleQueryString(sqs -> sqs.query(simpleQueryString)));
                break;
            
            case "geo_distance":
                String geoDistanceField = (String) params.get("field");
                Double lat = (Double) params.get("lat");
                Double lon = (Double) params.get("lon");
                String distance = (String) params.get("distance");
                esQuery = Query.of(q -> q.geoDistance(g -> g.field(geoDistanceField)
                        .distance(distance)
                        .location(l -> l.latlon(la -> la.lat(lat).lon(lon)))));
                break;
            
            case "geo_bounding_box":
                String geoBoundingBoxField = (String) params.get("field");
                Double topLeftLat = (Double) params.get("top_left_lat");
                Double topLeftLon = (Double) params.get("top_left_lon");
                Double bottomRightLat = (Double) params.get("bottom_right_lat");
                Double bottomRightLon = (Double) params.get("bottom_right_lon");
                esQuery = Query.of(q -> q.geoBoundingBox(g -> {
                    g.field(geoBoundingBoxField);
                    return g.boundingBox(b -> b
                            .tlbr(t -> t.topLeft(l -> l.latlon(p -> p.lat(topLeftLat).lon(topLeftLon)))
                                    .bottomRight(l -> l.latlon(p -> p.lat(bottomRightLat).lon(bottomRightLon)))));
                }));
                break;
            
            case "geo_polygon":
                String geoPolygonField = (String) params.get("field");
                List<EpGeoPoint> points = (List<EpGeoPoint>)params.get("points");
                List<GeoLocation> geoLocations = points.stream().map(point -> GeoLocation.of(
                                p -> p.latlon(new LatLonGeoLocation.Builder().lat(point.getLat()).lon(point.getLon()).build())))
                        .collect(Collectors.toList());
                GeoPolygonPoints geoPolygonPoints = GeoPolygonPoints.of(a -> a.points(geoLocations));
                esQuery = Query.of(q -> q.geoPolygon(g -> g.field(geoPolygonField).polygon(geoPolygonPoints)));
                break;
            
            case "script":
                Object script = params.get("script");
                if (script instanceof co.elastic.clients.elasticsearch._types.Script) {
                    esQuery = Query.of(q -> q.script(s -> s.script((co.elastic.clients.elasticsearch._types.Script) script)));
                }
                break;
            
            case "has_child":
                String childType = (String) params.get("type");
                EpQueryBuilder childQuery = (EpQueryBuilder) params.get("query");
                Object childScoreModeObj = params.get("score_mode");
                ChildScoreMode childScoreMode;
                if (childScoreModeObj instanceof EpScoreMode) {
                    childScoreMode = toEsScoreMode((EpScoreMode) childScoreModeObj);
                } else if (childScoreModeObj instanceof ChildScoreMode) {
                    childScoreMode = (ChildScoreMode) childScoreModeObj;
                } else {
                    childScoreMode = ChildScoreMode.None;
                }
                esQuery = Query.of(q -> q.hasChild(h -> h.type(childType)
                        .query(toEsQuery(childQuery))
                        .scoreMode(childScoreMode)));
                break;
            
            case "has_parent":
                String parentType = (String) params.get("type");
                EpQueryBuilder parentQuery = (EpQueryBuilder) params.get("query");
                Object parentScoreModeObj = params.get("score_mode");
                Boolean parentScoreModeBool = (Boolean) params.get("score_mode");
                ChildScoreMode parentScoreMode = ChildScoreMode.None;
                if (parentScoreModeObj instanceof EpScoreMode) {
                    parentScoreMode = toEsScoreMode((EpScoreMode) parentScoreModeObj);
                } else if (parentScoreModeObj instanceof ChildScoreMode) {
                    parentScoreMode = (ChildScoreMode) parentScoreModeObj;
                }
                esQuery = Query.of(q -> q.hasParent(h -> h.parentType(parentType)
                        .query(toEsQuery(parentQuery))
                        .score(parentScoreModeBool)));
                break;
            
            case "parent_id":
                String parentIdType = (String) params.get("type");
                String id = (String) params.get("id");
                esQuery = Query.of(q -> q.parentId(p -> p.type(parentIdType).id(id)));
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported query type: " + type);
        }
        
        return esQuery;
    }
    
    /**
     * 将EpBoolQueryBuilder转换为Elasticsearch BoolQuery
     *
     * @param epBoolQuery 自定义Bool查询构建器
     * @return Elasticsearch BoolQuery
     */
    public static Query toEsBoolQuery(EpBoolQueryBuilder epBoolQuery) {
        if (epBoolQuery == null) {
            return null;
        }
        
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        // 转换must子句
        List<EpQueryBuilder> mustClauses = epBoolQuery.getMustClauses();
        for (EpQueryBuilder mustClause : mustClauses) {
            boolQueryBuilder.must(toEsQuery(mustClause));
        }
        
        // 转换mustNot子句
        List<EpQueryBuilder> mustNotClauses = epBoolQuery.getMustNotClauses();
        for (EpQueryBuilder mustNotClause : mustNotClauses) {
            boolQueryBuilder.mustNot(toEsQuery(mustNotClause));
        }
        
        // 转换filter子句
        List<EpQueryBuilder> filterClauses = epBoolQuery.getFilterClauses();
        for (EpQueryBuilder filterClause : filterClauses) {
            boolQueryBuilder.filter(toEsQuery(filterClause));
        }
        
        // 转换should子句
        List<EpQueryBuilder> shouldClauses = epBoolQuery.getShouldClauses();
        for (EpQueryBuilder shouldClause : shouldClauses) {
            boolQueryBuilder.should(toEsQuery(shouldClause));
        }
        
        // 设置minimumShouldMatch
        Map<String, Object> params = epBoolQuery.getParameters();
        if (params.containsKey("minimum_should_match")) {
            boolQueryBuilder.minimumShouldMatch(String.valueOf(params.get("minimum_should_match")));
        }
        
        return Query.of(q -> q.bool(boolQueryBuilder.build()));
    }
    public static SortOptions toEsSort(EsOrder esOrder) {
        if (esOrder == null) {
            return null;
        }
        
        SortOptions.Builder sortBuilder = new SortOptions.Builder();
        
        // 设置字段和排序顺序
        SortOrder sortOrder;
        if ("desc".equalsIgnoreCase(esOrder.getSort())) {
            sortOrder = SortOrder.Desc;
        } else {
            sortOrder = SortOrder.Asc;
        }
        
        sortBuilder.field(f -> f.field(esOrder.getName()).order(sortOrder));
        
        // 如果有嵌套排序设置
        if (esOrder.getNestedSortBuilder() != null) {
            EpNestedSortBuilder nestedSortBuilder = esOrder.getNestedSortBuilder();
            // 处理嵌套排序，这里简化处理
            if (nestedSortBuilder.getPath() != null) {
                sortBuilder.field(f -> f.field(esOrder.getName())
                        .order(sortOrder)
                        .nested(n -> n.path(nestedSortBuilder.getPath())));
            }
        }
        
        return sortBuilder.build();
    }
    
    /**
     * 将EsOrder列表转换为Elasticsearch原生SortOptions列表
     *
     * @param esOrders 自定义排序设置列表
     * @return Elasticsearch原生SortOptions列表
     */
    public static List<SortOptions> toEsSorts(List<EsOrder> esOrders) {
        if (esOrders == null || esOrders.isEmpty()) {
            return null;
        }
        
        return esOrders.stream()
                .map(Ep8QueryConverter::toEsSort)
                .collect(Collectors.toList());
    }
    
    
    /**
     * 将自定义EsHighLight列表转换为Elasticsearch原生Highlight
     *
     * @param esHighLights 自定义高亮设置列表
     * @return Elasticsearch原生Highlight
     */
    public static Highlight toEsHighlight(List<EsHighLight> esHighLights) {
        if (esHighLights == null || esHighLights.isEmpty()) {
            return null;
        }
        
        Highlight.Builder highlightBuilder = new Highlight.Builder();
        
        // 创建字段高亮映射
        Map<String, HighlightField> fields = new HashMap<>();
        
        for (EsHighLight esHighLight : esHighLights) {
            HighlightField.Builder fieldBuilder = new HighlightField.Builder();
            
            // 设置高亮标签
            if (esHighLight.getPreTag() != null) {
                fieldBuilder.preTags(esHighLight.getPreTag());
            }
            if (esHighLight.getPostTag() != null) {
                fieldBuilder.postTags(esHighLight.getPostTag());
            }
            
            // 设置片段大小
            if (esHighLight.getFragmentSize() != null) {
                fieldBuilder.fragmentSize(esHighLight.getFragmentSize());
            }
            
            // 设置片段数量
            if (esHighLight.getNumberOfFragments() != null) {
                fieldBuilder.numberOfFragments(esHighLight.getNumberOfFragments());
            }
            
            fields.put(esHighLight.getField(), fieldBuilder.build());
        }
        
        highlightBuilder.fields(fields);
        
        // 设置全局高亮参数
        EsHighLight firstHighLight = esHighLights.get(0);
        if (firstHighLight.getPreTag() != null) {
            highlightBuilder.preTags(firstHighLight.getPreTag());
        }
        if (firstHighLight.getPostTag() != null) {
            highlightBuilder.postTags(firstHighLight.getPostTag());
        }
        
        return highlightBuilder.build();
    }
}