package com.es.plus.es6.convert;

import com.es.plus.adapter.pojo.es.EpBoolQueryBuilder;
import com.es.plus.adapter.pojo.es.EpQueryBuilder;
import com.es.plus.adapter.pojo.es.EpScoreMode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;

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
     * 将EpQueryBuilder转换为Elasticsearch原生QueryBuilder
     * @param epQuery 自定义查询构建器
     * @return Elasticsearch原生QueryBuilder
     */
    public static QueryBuilder toEsQueryBuilder(EpQueryBuilder epQuery) {
        if (epQuery == null) {
            return null;
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
                esQuery = QueryBuilders.fuzzyQuery(fuzzyField, fuzzyValue);
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
                esQuery = QueryBuilders.nestedQuery(nestedPath, toEsQueryBuilder(nestedQuery), scoreMode);
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
                esQuery = QueryBuilders.geoDistanceQuery(geoDistanceField).point(lat, lon).distance(distance);
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
                org.apache.lucene.search.join.ScoreMode parentScoreMode = org.apache.lucene.search.join.ScoreMode.None;
                if (parentScoreModeObj instanceof EpScoreMode) {
                    parentScoreMode = toEsScoreMode((EpScoreMode) parentScoreModeObj);
                } else if (parentScoreModeObj instanceof org.apache.lucene.search.join.ScoreMode) {
                    parentScoreMode = (org.apache.lucene.search.join.ScoreMode) parentScoreModeObj;
                }
                esQuery =  new HasChildQueryBuilder(parentType, toEsQueryBuilder(parentQuery), parentScoreMode);
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