package com.es.plus.es6.convert;

import com.es.plus.common.pojo.es.EpNestedSortBuilder;
import com.es.plus.common.pojo.es.EpQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;

/**
 * EpNestedSortBuilder转换为Elasticsearch NestedSortBuilder的工具类
 */
public class EpNestedSortConverter {
    
    /**
     * 将EpNestedSortBuilder转换为Elasticsearch的NestedSortBuilder
     *
     * @param epNestedSort EpNestedSortBuilder对象
     * @return Elasticsearch的NestedSortBuilder对象
     */
    public static NestedSortBuilder convertToNestedSort(EpNestedSortBuilder epNestedSort) {
        if (epNestedSort == null) {
            return null;
        }
        
        // 创建NestedSortBuilder并设置path
        NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(epNestedSort.getPath());
        
        // 设置filter（如果EpNestedSortBuilder支持）
        if (epNestedSort.getFilter() != null) {
            QueryBuilder filter = convertToQueryBuilder(epNestedSort.getFilter());
            nestedSortBuilder.setFilter(filter);
        }
        
        // 设置maxChildren（如果EpNestedSortBuilder支持）
        if (epNestedSort.getMaxChildren() != null) {
            nestedSortBuilder.setMaxChildren(epNestedSort.getMaxChildren());
        }
        
        // 处理嵌套的NestedSortBuilder（递归调用）
        if (epNestedSort.getNestedSort() != null) {
            NestedSortBuilder nestedNestedSort = convertToNestedSort(epNestedSort.getNestedSort());
            nestedSortBuilder.setNestedSort(nestedNestedSort);
        }
        
        return nestedSortBuilder;
    }
    
    /**
     * 将EpQueryBuilder转换为Elasticsearch的QueryBuilder
     *
     * @param epQuery EpQueryBuilder对象
     * @return Elasticsearch的QueryBuilder对象
     */
    private static QueryBuilder convertToQueryBuilder(EpQueryBuilder epQuery) {
        QueryBuilder queryBuilder = EpQueryConverter.toEsQueryBuilder(epQuery);
        return queryBuilder;
    }
}
