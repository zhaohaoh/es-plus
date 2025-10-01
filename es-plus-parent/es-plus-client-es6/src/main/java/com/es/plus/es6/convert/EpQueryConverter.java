package com.es.plus.es6.convert;

import com.es.plus.common.pojo.es.*;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * EpQueryBuilder到Elasticsearch QueryBuilder的转换工厂类
 */
public class EpQueryConverter {
    
    /**
     * 将自定义EpScoreMode转换为Elasticsearch ScoreMode
     * @param epScoreMode 自定义评分模式
     * @return Elasticsearch评分模式
     */
    private static org.apache.lucene.search.join.ScoreMode toEsScoreMode(EpScoreMode epScoreMode) {
        if (epScoreMode == null) {
            return org.apache.lucene.search.join.ScoreMode.None;
        }
        
        switch (epScoreMode) {
            case None:
                return org.apache.lucene.search.join.ScoreMode.None;
            case Avg:
                return org.apache.lucene.search.join.ScoreMode.Avg;
            case Max:
                return org.apache.lucene.search.join.ScoreMode.Max;
            case Total:
                return org.apache.lucene.search.join.ScoreMode.Total;
            case Min:
                return org.apache.lucene.search.join.ScoreMode.Min;
            default:
                return org.apache.lucene.search.join.ScoreMode.None;
        }
    }
    
    /**
     * 将EpFetchSourceContext转换为FetchSourceContext
     * @param epFetchSourceContext 自定义FetchSourceContext
     * @return Elasticsearch FetchSourceContext
     */
    private static org.elasticsearch.search.fetch.subphase.FetchSourceContext toEsFetchSourceContext(EpFetchSourceContext epFetchSourceContext) {
        if (epFetchSourceContext == null) {
            return null;
        }
        
        return new org.elasticsearch.search.fetch.subphase.FetchSourceContext(
                epFetchSourceContext.fetchSource(),
                epFetchSourceContext.includes(),
                epFetchSourceContext.excludes()
        );
    }
    
    /**
     * 将EpHighlightBuilder转换为HighlightBuilder
     * @param epHighlightBuilder 自定义HighlightBuilder
     * @return Elasticsearch HighlightBuilder
     */
    private static HighlightBuilder toEsHighlightBuilder(EpHighlightBuilder epHighlightBuilder) {
        if (epHighlightBuilder == null) {
            return null;
        }
        
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        
        // 设置字段
        if (epHighlightBuilder.getFields() != null) {
            for (String field : epHighlightBuilder.getFields()) {
                highlightBuilder.field(field);
            }
        }
        
        // 设置标签
        if (epHighlightBuilder.getPreTags() != null) {
            highlightBuilder.preTags(epHighlightBuilder.getPreTags());
        }
        
        if (epHighlightBuilder.getPostTags() != null) {
            highlightBuilder.postTags(epHighlightBuilder.getPostTags());
        }
        
        // 设置其他属性
        if (epHighlightBuilder.getRequireFieldMatch() != null) {
            highlightBuilder.requireFieldMatch(epHighlightBuilder.getRequireFieldMatch());
        }
        
        if (epHighlightBuilder.getFragmentSize() != null) {
            highlightBuilder.fragmentSize(Integer.parseInt(epHighlightBuilder.getFragmentSize()));
        }
        
        if (epHighlightBuilder.getNumberOfFragments() != null) {
            highlightBuilder.numOfFragments(epHighlightBuilder.getNumberOfFragments());
        }
        
        return highlightBuilder;
    }
    
    /**
     * 将EpSortBuilder转换为SortBuilder
     * @param epSortBuilder 自定义SortBuilder
     * @return Elasticsearch SortBuilder
     */
    private static SortBuilder<?> toEsSortBuilder(EpSortBuilder epSortBuilder) {
        if (epSortBuilder == null || epSortBuilder.getField() == null) {
            return null;
        }
        
        FieldSortBuilder  sortBuilder =  SortBuilders.fieldSort(epSortBuilder.getField());
        
        // 设置排序顺序
        if (epSortBuilder.getOrder() != null) {
            sortBuilder.order( SortOrder.valueOf(epSortBuilder.getOrder().name()));
        }
        
        
        // 设置嵌套排序
        if (epSortBuilder.getNestedSort() != null) {
            sortBuilder.setNestedSort(toEsNestedSortBuilder(epSortBuilder.getNestedSort()));
        }
        
        return sortBuilder;
    }
    
    /**
     * 将EpNestedSortBuilder转换为NestedSortBuilder
     * @param epNestedSortBuilder 自定义NestedSortBuilder
     * @return Elasticsearch NestedSortBuilder
     */
    private static org.elasticsearch.search.sort.NestedSortBuilder toEsNestedSortBuilder(EpNestedSortBuilder epNestedSortBuilder) {
        if (epNestedSortBuilder == null) {
            return null;
        }
        
        // 创建NestedSortBuilder并设置path
        org.elasticsearch.search.sort.NestedSortBuilder nestedSortBuilder =
                new org.elasticsearch.search.sort.NestedSortBuilder(epNestedSortBuilder.getPath());
        
        // 设置filter（如果EpNestedSortBuilder支持）
        if (epNestedSortBuilder.getFilter() != null) {
            // 注意：这里需要EpQueryBuilder到QueryBuilder的转换
            nestedSortBuilder.setFilter(toEsQueryBuilder(epNestedSortBuilder.getFilter()));
        }
        
        // 设置maxChildren（如果EpNestedSortBuilder支持）
        if (epNestedSortBuilder.getMaxChildren() != null) {
            nestedSortBuilder.setMaxChildren(epNestedSortBuilder.getMaxChildren());
        }
        
        // 处理嵌套的NestedSortBuilder（递归调用）
        if (epNestedSortBuilder.getNestedSort() != null) {
            org.elasticsearch.search.sort.NestedSortBuilder nestedNestedSort =
                    toEsNestedSortBuilder(epNestedSortBuilder.getNestedSort());
            nestedSortBuilder.setNestedSort(nestedNestedSort);
        }
        
        return nestedSortBuilder;
    }
    
    /**
     * 将EpInnerHitBuilder转换为InnerHitBuilder
     * @param epInnerHitBuilder 自定义InnerHitBuilder
     * @return Elasticsearch InnerHitBuilder
     */
    private static InnerHitBuilder toEsInnerHitBuilder(EpInnerHitBuilder epInnerHitBuilder) {
        if (epInnerHitBuilder == null) {
            return null;
        }
        
        InnerHitBuilder innerHitBuilder =
                new  InnerHitBuilder();
        
        // 设置名称
        if (epInnerHitBuilder.getName() != null) {
            innerHitBuilder.setName(epInnerHitBuilder.getName());
        }
        
        
        
        // 设置文档值字段
        if (epInnerHitBuilder.getDocValueFields() != null) {
            for (String docValueField : epInnerHitBuilder.getDocValueFields()) {
                innerHitBuilder.addDocValueField(docValueField);
            }
        }
        
        // 设置分页
        if (epInnerHitBuilder.getFrom() != null) {
            innerHitBuilder.setFrom(epInnerHitBuilder.getFrom());
        }
        
        if (epInnerHitBuilder.getSize() != null) {
            innerHitBuilder.setSize(epInnerHitBuilder.getSize());
        }
        
        // 设置排序
        if (epInnerHitBuilder.getSorts() != null) {
            for (EpSortBuilder epSort : epInnerHitBuilder.getSorts()) {
                SortBuilder<?> sortBuilder = toEsSortBuilder(epSort);
                if (sortBuilder != null) {
                    innerHitBuilder.addSort(sortBuilder);
                }
            }
        }
        
        // 设置高亮
        if (epInnerHitBuilder.getHighlightBuilder() != null) {
            innerHitBuilder.setHighlightBuilder(toEsHighlightBuilder(epInnerHitBuilder.getHighlightBuilder()));
        }
        
        // 设置其他属性
        if (epInnerHitBuilder.getExplain() != null) {
            innerHitBuilder.setExplain(epInnerHitBuilder.getExplain());
        }
        
        if (epInnerHitBuilder.getVersion() != null) {
            innerHitBuilder.setVersion(epInnerHitBuilder.getVersion());
        }
        
        if (epInnerHitBuilder.getSeqNoAndPrimaryTerm() != null) {
            innerHitBuilder.setSeqNoAndPrimaryTerm(epInnerHitBuilder.getSeqNoAndPrimaryTerm());
        }
        if (epInnerHitBuilder.getTrackScores() != null) {
            innerHitBuilder.setTrackScores(epInnerHitBuilder.getTrackScores());
        }
        
        // 设置源字段上下文
        if (epInnerHitBuilder.getFetchSourceContext() != null) {
            innerHitBuilder.setFetchSourceContext(toEsFetchSourceContext(epInnerHitBuilder.getFetchSourceContext()));
        }
        
        return innerHitBuilder;
    }
    /**
     * 将EpScript转换为Elasticsearch Script
     * @param epScript 自定义EpScript
     * @return Elasticsearch Script
     */
    public static org.elasticsearch.script.Script toEsScript(EpScript epScript) {
        if (epScript == null) {
            return null;
        }
        
        // 根据EpScript的类型创建相应的Script
        org.elasticsearch.script.ScriptType scriptType = org.elasticsearch.script.ScriptType.INLINE;
        if (epScript.getScriptType() == EpScript.ScriptType.STORED) {
            scriptType = org.elasticsearch.script.ScriptType.STORED;
        }
        
        // 创建Script
        return new org.elasticsearch.script.Script(
                scriptType,
                epScript.getLang(),
                epScript.getScript(),
                epScript.getParams()
        );
    }
    
    /**
     * 将EpQueryBuilder转换为Elasticsearch原生QueryBuilder
     * @param epQuery 自定义查询构建器
     * @return Elasticsearch原生QueryBuilder
     */
    public static QueryBuilder toEsQueryBuilder(EpQueryBuilder epQuery) {
        if (epQuery == null) {
            return null;
        }
        // 如果是
        if (epQuery.getEsOrginalQuery() instanceof QueryBuilder) {
            return (QueryBuilder) epQuery;
        }
        
        // 如果是BoolQueryBuilder特殊处理
        if (epQuery instanceof EpBoolQueryBuilder) {
            return toEsBoolQueryBuilder((EpBoolQueryBuilder) epQuery);
        }
        
        String type = epQuery.getType();
        Map<String, Object> params = epQuery.getParameters();
        
        QueryBuilder esQuery = null;
        
        switch (type) {
            case "term":
                String termField = (String) params.get("field");
                Object termValue = params.get("value");
                esQuery = QueryBuilders.termQuery(termField, termValue);
                break;
            
            case "terms":
                String termsField = (String) params.get("field");
                Object[] termsValues = (Object[]) params.get("values");
                esQuery = QueryBuilders.termsQuery(termsField, termsValues);
                break;
            
            case "match":
                String matchField = (String) params.get("field");
                Object matchValue = params.get("value");
                esQuery = QueryBuilders.matchQuery(matchField, matchValue);
                break;
            
            case "match_all":
                esQuery = QueryBuilders.matchAllQuery();
                break;
            
            case "range":
                String rangeField = (String) params.get("field");
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(rangeField);
                
                if (params.containsKey("from")) {
                    Object from = params.get("from");
                    if (from != null) {
                        rangeQuery.from(from);
                    }
                }
                
                if (params.containsKey("to")) {
                    Object to = params.get("to");
                    if (to != null) {
                        rangeQuery.to(to);
                    }
                }
                
                if (params.containsKey("includeLower")) {
                    rangeQuery.includeLower((Boolean) params.get("includeLower"));
                }
                
                if (params.containsKey("includeUpper")) {
                    rangeQuery.includeUpper((Boolean) params.get("includeUpper"));
                }
                
                if (params.containsKey("time_zone")) {
                    rangeQuery.timeZone((String) params.get("time_zone"));
                }
                
                esQuery = rangeQuery;
                break;
            
            case "exists":
                String existsField = (String) params.get("field");
                esQuery = QueryBuilders.existsQuery(existsField);
                break;
            
            case "wildcard":
                String wildcardField = (String) params.get("field");
                String wildcardValue = (String) params.get("value");
                esQuery = QueryBuilders.wildcardQuery(wildcardField, wildcardValue);
                break;
            
            case "prefix":
                String prefixField = (String) params.get("field");
                String prefixValue = (String) params.get("value");
                esQuery = QueryBuilders.prefixQuery(prefixField, prefixValue);
                break;
            
            case "fuzzy":
                String fuzzyField = (String) params.get("field");
                Object fuzzyValue = params.get("value");
                Object fuzziness = params.get("fuzziness");
                FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(fuzzyField, fuzzyValue);
                if (fuzziness != null) {
                    if (fuzziness instanceof EpFuzziness) {
                        fuzzyQueryBuilder.fuzziness(org.elasticsearch.common.unit.Fuzziness.build(((EpFuzziness) fuzziness).getFuzziness()));
                    } else {
                        fuzzyQueryBuilder.fuzziness(org.elasticsearch.common.unit.Fuzziness.build(fuzziness));
                    }
                }
                if (params.containsKey("prefix_length")) {
                    fuzzyQueryBuilder.prefixLength((Integer) params.get("prefix_length"));
                }
                esQuery = fuzzyQueryBuilder;
                break;
            
            case "regexp":
                String regexpField = (String) params.get("field");
                String regexpValue = (String) params.get("value");
                esQuery = QueryBuilders.regexpQuery(regexpField, regexpValue);
                break;
            
            case "nested":
                String nestedPath = (String) params.get("path");
                EpQueryBuilder nestedQuery = (EpQueryBuilder) params.get("query");
                Object scoreModeObj = params.get("score_mode");
                org.apache.lucene.search.join.ScoreMode scoreMode = org.apache.lucene.search.join.ScoreMode.None;
                if (scoreModeObj instanceof EpScoreMode) {
                    scoreMode = toEsScoreMode((EpScoreMode) scoreModeObj);
                } else if (scoreModeObj instanceof org.apache.lucene.search.join.ScoreMode) {
                    scoreMode = (org.apache.lucene.search.join.ScoreMode) scoreModeObj;
                }
                
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(nestedPath, toEsQueryBuilder(nestedQuery), scoreMode);
                
                // 处理inner_hit参数
                if (params.containsKey("inner_hit")) {
                    Object innerHitObj = params.get("inner_hit");
                    if (innerHitObj instanceof EpInnerHitBuilder) {
                        nestedQueryBuilder.innerHit(toEsInnerHitBuilder((EpInnerHitBuilder) innerHitObj));
                    }
                }
                
                esQuery = nestedQueryBuilder;
                break;
            
            case "ids":
                String[] ids = (String[]) params.get("values");
                esQuery = QueryBuilders.idsQuery().addIds(ids);
                break;
            
            case "match_phrase":
                String matchPhraseField = (String) params.get("field");
                Object matchPhraseValue = params.get("value");
                esQuery = QueryBuilders.matchPhraseQuery(matchPhraseField, matchPhraseValue);
                break;
            
            case "match_phrase_prefix":
                String matchPhrasePrefixField = (String) params.get("field");
                Object matchPhrasePrefixValue = params.get("value");
                Integer maxExpansions = (Integer) params.get("max_expansions");
                MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(matchPhrasePrefixField, matchPhrasePrefixValue);
                if (maxExpansions != null) {
                    matchPhrasePrefixQueryBuilder.maxExpansions(maxExpansions);
                }
                esQuery = matchPhrasePrefixQueryBuilder;
                break;
            
            case "multi_match":
                Object multiMatchValue = params.get("value");
                String[] multiMatchFields = (String[]) params.get("fields");
                esQuery = QueryBuilders.multiMatchQuery(multiMatchValue, multiMatchFields);
                break;
            
            case "query_string":
                String queryString = (String) params.get("query");
                esQuery = QueryBuilders.queryStringQuery(queryString);
                break;
            
            case "simple_query_string":
                String simpleQueryString = (String) params.get("query");
                esQuery = QueryBuilders.simpleQueryStringQuery(simpleQueryString);
                break;
            
            case "geo_distance":
                String geoDistanceField = (String) params.get("field");
                Double lat = (Double) params.get("lat");
                Double lon = (Double) params.get("lon");
                String distance = (String) params.get("distance");
                String unit = (String) params.get("unit");
                GeoDistanceQueryBuilder geoDistanceQueryBuilder = QueryBuilders.geoDistanceQuery(geoDistanceField)
                        .point(lat, lon)
                        .distance(distance, org.elasticsearch.common.unit.DistanceUnit.fromString(unit));
                esQuery = geoDistanceQueryBuilder;
                break;
            
            case "geo_bounding_box":
                String geoBoundingBoxField = (String) params.get("field");
                Double topLeftLat = (Double) params.get("top_left_lat");
                Double topLeftLon = (Double) params.get("top_left_lon");
                Double bottomRightLat = (Double) params.get("bottom_right_lat");
                Double bottomRightLon = (Double) params.get("bottom_right_lon");
                esQuery = QueryBuilders.geoBoundingBoxQuery(geoBoundingBoxField)
                        .setCorners(topLeftLat, topLeftLon, bottomRightLat, bottomRightLon);
                break;
            
            case "geo_polygon":
                String geoPolygonField = (String) params.get("field");
                List<GeoPoint> points = (List<GeoPoint>)params.get("points");
                GeoPolygonQueryBuilder geoPolygonQueryBuilder = QueryBuilders.geoPolygonQuery(geoPolygonField,points);
                esQuery = geoPolygonQueryBuilder;
                break;
            
            case "script":
                Object script = params.get("script");
                if (script instanceof org.elasticsearch.script.Script) {
                    esQuery = QueryBuilders.scriptQuery((org.elasticsearch.script.Script) script);
                } else if (script instanceof EpScript) {
                    esQuery = QueryBuilders.scriptQuery(toEsScript((EpScript) script));
                }
                break;
            case "has_child":
                String childType = (String) params.get("type");
                EpQueryBuilder childQuery = (EpQueryBuilder) params.get("query");
                Object childScoreModeObj = params.get("score_mode");
                org.apache.lucene.search.join.ScoreMode childScoreMode = org.apache.lucene.search.join.ScoreMode.None;
                if (childScoreModeObj instanceof EpScoreMode) {
                    childScoreMode = toEsScoreMode((EpScoreMode) childScoreModeObj);
                } else if (childScoreModeObj instanceof org.apache.lucene.search.join.ScoreMode) {
                    childScoreMode = (org.apache.lucene.search.join.ScoreMode) childScoreModeObj;
                }
                esQuery = new HasChildQueryBuilder(childType, toEsQueryBuilder(childQuery), childScoreMode);
                break;
            
            case "has_parent":
                String parentType = (String) params.get("type");
                EpQueryBuilder parentQuery = (EpQueryBuilder) params.get("query");
                Object parentScoreModeObj = params.get("score_mode");
                Boolean parentScoreModeBool = (Boolean) params.get("score_mode");
                org.apache.lucene.search.join.ScoreMode parentScoreMode = org.apache.lucene.search.join.ScoreMode.None;
                if (parentScoreModeObj instanceof EpScoreMode) {
                    parentScoreMode = toEsScoreMode((EpScoreMode) parentScoreModeObj);
                } else if (parentScoreModeObj instanceof org.apache.lucene.search.join.ScoreMode) {
                    parentScoreMode = (org.apache.lucene.search.join.ScoreMode) parentScoreModeObj;
                }
                esQuery = new HasParentQueryBuilder(parentType, toEsQueryBuilder(parentQuery), parentScoreModeBool);
                break;
            
            case "parent_id":
                String parentIdType = (String) params.get("type");
                String id = (String) params.get("id");
                esQuery = new ParentIdQueryBuilder(parentIdType, id);
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported query type: " + type);
        }
        
        // 设置通用属性
        if (esQuery != null) {
            if (epQuery.getQueryName() != null) {
                esQuery.queryName(epQuery.getQueryName());
            }
            
            if (epQuery.getBoost() != 1.0f) {
                esQuery.boost(epQuery.getBoost());
            }
        }
        
        return esQuery;
    }
    
    /**
     * 将EpBoolQueryBuilder转换为Elasticsearch BoolQueryBuilder
     * @param epBoolQuery 自定义Bool查询构建器
     * @return Elasticsearch BoolQueryBuilder
     */
    public static BoolQueryBuilder toEsBoolQueryBuilder(EpBoolQueryBuilder epBoolQuery) {
        if (epBoolQuery == null) {
            return null;
        }
        
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 转换must子句
        List<EpQueryBuilder> mustClauses = epBoolQuery.getMustClauses();
        for (EpQueryBuilder mustClause : mustClauses) {
            boolQuery.must(toEsQueryBuilder(mustClause));
        }
        
        // 转换mustNot子句
        List<EpQueryBuilder> mustNotClauses = epBoolQuery.getMustNotClauses();
        for (EpQueryBuilder mustNotClause : mustNotClauses) {
            boolQuery.mustNot(toEsQueryBuilder(mustNotClause));
        }
        
        // 转换filter子句
        List<EpQueryBuilder> filterClauses = epBoolQuery.getFilterClauses();
        for (EpQueryBuilder filterClause : filterClauses) {
            boolQuery.filter(toEsQueryBuilder(filterClause));
        }
        
        // 转换should子句
        List<EpQueryBuilder> shouldClauses = epBoolQuery.getShouldClauses();
        for (EpQueryBuilder shouldClause : shouldClauses) {
            boolQuery.should(toEsQueryBuilder(shouldClause));
        }
        
        // 设置minimumShouldMatch
        Map<String, Object> params = epBoolQuery.getParameters();
        if (params.containsKey("minimum_should_match")) {
            boolQuery.minimumShouldMatch((Integer) params.get("minimum_should_match"));
        }
        
        // 设置通用属性
        if (epBoolQuery.getQueryName() != null) {
            boolQuery.queryName(epBoolQuery.getQueryName());
        }
        
        if (epBoolQuery.getBoost() != 1.0f) {
            boolQuery.boost(epBoolQuery.getBoost());
        }
        
        return boolQuery;
    }
}