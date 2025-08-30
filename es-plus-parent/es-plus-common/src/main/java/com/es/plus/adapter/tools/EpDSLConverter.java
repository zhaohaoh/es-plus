package com.es.plus.adapter.tools;

import com.es.plus.adapter.pojo.es.EpAggBuilder;
import com.es.plus.adapter.pojo.es.EpBoolQueryBuilder;
import com.es.plus.adapter.pojo.es.EpFieldSortBuilder;
import com.es.plus.adapter.pojo.es.EpNestedSortBuilder;
import com.es.plus.adapter.pojo.es.EpQueryBuilder;
import com.es.plus.adapter.pojo.es.EpSortOrder;
import com.es.plus.adapter.util.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DSL转换工具类，用于将自定义查询构建器转换为Elasticsearch DSL语句
 */
public class EpDSLConverter {
    
    /**
     * 将EpBoolQueryBuilder和多个EpAggBuilder转换为Elasticsearch DSL语句
     *
     * @param queryBuilder 查询构建器
     * @param aggBuilders 聚合构建器列表
     * @return Elasticsearch DSL字符串
     */
    public static String convertToDSL(EpBoolQueryBuilder queryBuilder, List<EpAggBuilder> aggBuilders) {
        Map<String, Object> dsl = new HashMap<>();
        
        // 构建查询部分
        if (queryBuilder != null) {
            Map<String, Object> query = buildQuery(queryBuilder);
            if (!query.isEmpty()) {
                dsl.put("query", query);
            }
        }
        
        // 构建聚合部分
        if (aggBuilders != null && !aggBuilders.isEmpty()) {
            Map<String, Object> aggs = new HashMap<>();
            for (EpAggBuilder aggBuilder : aggBuilders) {
                aggs.putAll(buildAggregations(aggBuilder));
            }
            if (!aggs.isEmpty()) {
                dsl.put("aggs", aggs);
            }
        }
        
        return JsonUtils.toJsonStr(dsl);
    }
    
    /**
     * 将EpBoolQueryBuilder和多个EpAggBuilder转换为Elasticsearch DSL语句，包含排序、分页参数
     *
     * @param queryBuilder 查询构建器
     * @param aggBuilders 聚合构建器列表
     * @param sorts 排序构建器列表
     * @param size 返回结果数量
     * @param from 起始位置
     * @return Elasticsearch DSL字符串
     */
    public static String convertToDSL(EpBoolQueryBuilder queryBuilder, List<EpAggBuilder> aggBuilders,
            List<EpFieldSortBuilder> sorts, Integer size, Integer from) {
        Map<String, Object> dsl = new HashMap<>();
        
        // 构建查询部分
        if (queryBuilder != null) {
            Map<String, Object> query = buildQuery(queryBuilder);
            if (!query.isEmpty()) {
                dsl.put("query", query);
            }
        }
        
        // 构建聚合部分
        if (aggBuilders != null && !aggBuilders.isEmpty()) {
            Map<String, Object> aggs = new HashMap<>();
            for (EpAggBuilder aggBuilder : aggBuilders) {
                aggs.putAll(buildAggregations(aggBuilder));
            }
            if (!aggs.isEmpty()) {
                dsl.put("aggs", aggs);
            }
        }
        
        // 构建排序部分
        if (sorts != null && !sorts.isEmpty()) {
            List<Map<String, Object>> sortList = new ArrayList<>();
            for (EpFieldSortBuilder sort : sorts) {
                Map<String, Object> sortItem = new HashMap<>();
                Map<String, Object> sortParams = new HashMap<>();
                
                // 添加排序顺序
                if (sort.getOrder() != null) {
                    sortParams.put("order", sort.getOrder().name().toLowerCase());
                }
                
                // 添加其他排序参数
                if (sort.getMode() != null) {
                    sortParams.put("mode", sort.getMode());
                }
                
                if (sort.getMissing() != null) {
                    sortParams.put("missing", sort.getMissing());
                }
                
                if (sort.getNestedSort() != null) {
                    sortParams.put("nested", buildNestedSort(sort.getNestedSort()));
                }
                
                sortItem.put(sort.getField(), sortParams);
                sortList.add(sortItem);
            }
            dsl.put("sort", sortList);
        }
        
        // 添加分页参数
        if (size != null) {
            dsl.put("size", size);
        }
        
        if (from != null) {
            dsl.put("from", from);
        }
        
        return JsonUtils.toJsonStr(dsl);
    }
    
    /**
     * 构建嵌套排序DSL
     *
     * @param nestedSortBuilder 嵌套排序构建器
     * @return 嵌套排序DSL映射
     */
    private static Map<String, Object> buildNestedSort(EpNestedSortBuilder nestedSortBuilder) {
        Map<String, Object> nestedSort = new HashMap<>();
        
        if (nestedSortBuilder.getPath() != null) {
            nestedSort.put("path", nestedSortBuilder.getPath());
        }
        
        if (nestedSortBuilder.getNestedSort() != null) {
            nestedSort.put("nested", buildNestedSort(nestedSortBuilder.getNestedSort()));
        }
        
        return nestedSort;
    }
    
    /**
     * 构建查询DSL
     *
     * @param queryBuilder 查询构建器
     * @return 查询DSL映射
     */
    private static Map<String, Object> buildQuery(EpBoolQueryBuilder queryBuilder) {
        Map<String, Object> boolQuery = new HashMap<>();
        Map<String, Object> boolClause = new HashMap<>();
        
        // 处理must子句
        List<EpQueryBuilder> mustClauses = queryBuilder.getMustClauses();
        if (!mustClauses.isEmpty()) {
            List<Map<String, Object>> mustList = new ArrayList<>();
            for (EpQueryBuilder must : mustClauses) {
                mustList.add(buildSingleQuery(must));
            }
            boolClause.put("must", mustList);
        }
        
        // 处理must_not子句
        List<EpQueryBuilder> mustNotClauses = queryBuilder.getMustNotClauses();
        if (!mustNotClauses.isEmpty()) {
            List<Map<String, Object>> mustNotList = new ArrayList<>();
            for (EpQueryBuilder mustNot : mustNotClauses) {
                mustNotList.add(buildSingleQuery(mustNot));
            }
            boolClause.put("must_not", mustNotList);
        }
        
        // 处理filter子句
        List<EpQueryBuilder> filterClauses = queryBuilder.getFilterClauses();
        if (!filterClauses.isEmpty()) {
            List<Map<String, Object>> filterList = new ArrayList<>();
            for (EpQueryBuilder filter : filterClauses) {
                filterList.add(buildSingleQuery(filter));
            }
            boolClause.put("filter", filterList);
        }
        
        // 处理should子句
        List<EpQueryBuilder> shouldClauses = queryBuilder.getShouldClauses();
        if (!shouldClauses.isEmpty()) {
            List<Map<String, Object>> shouldList = new ArrayList<>();
            for (EpQueryBuilder should : shouldClauses) {
                shouldList.add(buildSingleQuery(should));
            }
            boolClause.put("should", shouldList);
        }
        
        // 添加其他参数
        Map<String, Object> params = queryBuilder.getParameters();
        boolClause.putAll(params);
        
        boolQuery.put("bool", boolClause);
        return boolQuery;
    }
    
    /**
     * 构建单个查询DSL
     *
     * @param queryBuilder 查询构建器
     * @return 查询DSL映射
     */
    private static Map<String, Object> buildSingleQuery(EpQueryBuilder queryBuilder) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> queryType = new HashMap<>();
        
        String type = queryBuilder.getType();
        if (type != null) {
            // 添加查询参数
            Map<String, Object> params = queryBuilder.getParameters();
            queryType.putAll(params);
            
            // 处理boost
            float boost = queryBuilder.getBoost();
            if (boost != 1.0f) {
                queryType.put("boost", boost);
            }
            
            query.put(type, queryType);
        }
        
        return query;
    }
    
    /**
     * 构建聚合DSL
     *
     * @param aggBuilder 聚合构建器
     * @return 聚合DSL映射
     */
    private static Map<String, Object> buildAggregations(EpAggBuilder aggBuilder) {
        Map<String, Object> aggs = new HashMap<>();
        
        String name = aggBuilder.getName();
        if (name != null) {
            Map<String, Object> agg = new HashMap<>();
            
            // 添加聚合类型和参数
            String type = aggBuilder.getType();
            if (type != null) {
                Map<String, Object> aggType = new HashMap<>();
                
                // 添加聚合参数
                Map<String, Object> params = aggBuilder.getParameters();
                aggType.putAll(params);
                
                // 处理子聚合
                List<EpAggBuilder> subAggregations = aggBuilder.getSubAggregation();
                if (!subAggregations.isEmpty()) {
                    Map<String, Object> subAggs = new HashMap<>();
                    for (EpAggBuilder subAgg : subAggregations) {
                        subAggs.putAll(buildAggregations(subAgg));
                    }
                    aggType.put("aggs", subAggs);
                }
                
                agg.put(type, aggType);
            }
            
            aggs.put(name, agg);
        }
        
        return aggs;
    }
    
    /**
     * 将EpBoolQueryBuilder转换为Elasticsearch DSL语句
     *
     * @param queryBuilder 查询构建器
     * @return Elasticsearch DSL字符串
     */
    public static String convertQueryToDSL(EpBoolQueryBuilder queryBuilder) {
        return convertToDSL(queryBuilder, (List<EpAggBuilder>) null);
    }
    
    /**
     * 将多个EpAggBuilder转换为Elasticsearch DSL语句
     *
     * @param aggBuilders 聚合构建器列表
     * @return Elasticsearch DSL字符串
     */
    public static String convertAggToDSL(List<EpAggBuilder> aggBuilders) {
        return convertToDSL(null, aggBuilders);
    }
    
    /**
     * 将单个EpAggBuilder转换为Elasticsearch DSL语句
     *
     * @param aggBuilder 聚合构建器
     * @return Elasticsearch DSL字符串
     */
    public static String convertAggToDSL(EpAggBuilder aggBuilder) {
        List<EpAggBuilder> aggBuilders = new ArrayList<>();
        if (aggBuilder != null) {
            aggBuilders.add(aggBuilder);
        }
        return convertToDSL(null, aggBuilders);
    }
}
