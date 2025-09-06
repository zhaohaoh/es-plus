package io.github.zhaohaoh.esplus.client;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.params.EsOrder;
import com.es.plus.common.params.EsParamWrapper;
import com.es.plus.common.params.EsQueryParamWrapper;
import com.es.plus.common.params.EsResponse;
import com.es.plus.common.params.EsSelect;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBoolQueryBuilder;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class A {
    @Override
    public <T> EsResponse<T> search(String type, EsParamWrapper<T> esParamWrapper, String... indexs) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder().build();
            
            EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
            
            // 获取查询语句源数据
            SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(esParamWrapper);
            
            populateSearchRequest(type, searchRequest, esQueryParamWrapper, sourceBuilder, indexs);
            
            long start = System.currentTimeMillis();
            SearchResponse<T> searchResponse = elasticsearchClient.search(searchRequest, esParamWrapper.getTClass());
            long end = System.currentTimeMillis();
            long timeCost = end - start;
            
            printInfoLog("search index={} timeCost:{} total:{}", indexs, timeCost, searchResponse.hits().total());
            
            EsResponse<T> esResponse = getEsResponse(esParamWrapper.getTClass(), searchResponse);
            return esResponse;
        } catch (IOException e) {
            throw new EsException("search IOException", e);
        }
    }
    
    /**
     * 填充搜索请求
     *
     * @param type                类型
     * @param searchRequest       搜索请求
     * @param esQueryParamWrapper es查询参数包装器
     * @param sourceBuilder       源构建器
     * @param indexs              索引
     */
    private void populateSearchRequest(String type, SearchRequest searchRequest,
            EsQueryParamWrapper esQueryParamWrapper, SearchSourceBuilder sourceBuilder, String... indexs) {
        // 设置查询语句源数据
        searchRequest.source(sourceBuilder);
        
        // 设置索引
        searchRequest.index(Arrays.asList(indexs));
        
        // 设置偏好
        if (esQueryParamWrapper.getPreference() != null) {
            searchRequest.preference(esQueryParamWrapper.getPreference());
        }
        
        if (esQueryParamWrapper.getSearchType() != null) {
            // ES8中搜索类型的设置方式可能不同，需要根据实际情况调整
            // searchRequest.searchType(SearchType.fromString(esQueryParamWrapper.getSearchType().name()));
        }
    }
    
    private <T> SearchSourceBuilder getSearchSourceBuilder(EsParamWrapper<T> esParamWrapper) {
        EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
        Integer page = esQueryParamWrapper.getPage();
        Integer size = esQueryParamWrapper.getSize();
        // 查询条件组合
        EpBoolQueryBuilder boolQueryBuilder = esQueryParamWrapper.getBoolQueryBuilder();
        Query queryBuilder = EpQueryConverter.toEsQuery(boolQueryBuilder);
        
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        // 超过1万条加了才能返回
        if (GlobalConfigCache.GLOBAL_CONFIG.isTrackTotalHits()) {
            sourceBuilder.trackTotalHits(true);
        }
        
        EsSelect esSelect = esQueryParamWrapper.getEsSelect();
        if (esSelect != null) {
            // ES8中可能需要不同的处理方式
            // if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
            //     sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            // }
            // if (esSelect.getFetch() != null) {
            //     sourceBuilder.fetchSource(esSelect.getFetch());
            // }
            if (esSelect.getMinScope() != null) {
                sourceBuilder.minScore(esSelect.getMinScope());
            }
            if (esSelect.getTrackScores() != null) {
                sourceBuilder.trackScores(esSelect.getTrackScores());
            }
            // if (esSelect.getTrackTotalHits() != null) {
            //     sourceBuilder.trackTotalHits(esSelect.getTrackTotalHits());
            // }
        }
        
        boolean profile = esQueryParamWrapper.isProfile();
        if (profile) {
            sourceBuilder.profile(profile);
        }
        
        // searchAfter
        if (esQueryParamWrapper.getSearchAfterValues() != null) {
            // sourceBuilder.searchAfter(esQueryParamWrapper.getSearchAfterValues());
            sourceBuilder.size(size);
        }
        // 是否需要分页查询
        else if (page != null || size != null) {
            // 设置分页属性
            if (size != null) {
                sourceBuilder.size(size);
                if (page != null) {
                    sourceBuilder.from(((page - 1) * size));
                }
            }
        } else {
            sourceBuilder.size(GlobalConfigCache.GLOBAL_CONFIG.getSearchSize());
        }
        
        // 设置高亮
        if (esQueryParamWrapper.getEsHighLights() != null) {
            // List<EsHighLight> esHighLight = esQueryParamWrapper.getEsHighLights();
            // HighlightBuilder highlightBuilder = new HighlightBuilder();
            // // 设置为0获取全部内容
            // highlightBuilder.numOfFragments(0);
            // for (EsHighLight highLight : esHighLight) {
            //     // 高亮字段
            //     highlightBuilder.field(highLight.getField());
            //     // 高亮前后缀
            //     highlightBuilder.preTags(highLight.getPreTag()).postTags(highLight.getPostTag())
            //             .fragmentSize(highLight.getFragmentSize());
            //     sourceBuilder.highlighter(highlightBuilder);
            // }
        }
        
        // 排序
        if (!CollectionUtils.isEmpty(esQueryParamWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esQueryParamWrapper.getEsOrderList();
            List<SortOptions> sortOptions = new ArrayList<>();
            for (EsOrder order : orderFields) {
                SortOptions sortOption = SortOptions.of(so -> so.field(f -> f.field(order.getName())
                        .order(SortOrder.valueOf(order.getSort().toUpperCase(Locale.ROOT)))));
                // ES8中嵌套排序的处理方式可能不同
                // if (order.getNestedSortBuilder() != null) {
                //     // NestedSortBuilder nestedSortBuilder = EpNestedSortConverter.convertToNestedSort(
                //     //         order.getNestedSortBuilder());
                //     // fieldSortBuilder.setNestedSort(nestedSortBuilder);
                // }
                sortOptions.add(sortOption);
            }
            sourceBuilder.sort(sortOptions);
        }
        
        // 填充分组字段
        if (esQueryParamWrapper.getEpAggBuilders() != null && !esQueryParamWrapper.getEpAggBuilders().isEmpty()) {
            List<EpAggBuilder> epAggBuilders = esQueryParamWrapper.getEpAggBuilders();
            // TODO: 实现聚合构建器转换
            // for (EpAggBuilder epAggBuilder : epAggBuilders) {
            //     AggregationBuilder aggregation = EpAggregationConvert.toEsAggregationBuilder(epAggBuilder);
            //     sourceBuilder.aggregation(aggregation);
            // }
        }
        
        return sourceBuilder;
    }
    
    private <T> EsResponse<T> getEsResponse(Class<T> tClass, SearchResponse<T> searchResponse) {
        // 获取结果集
        HitsMetadata<T> hits = searchResponse.hits();
        List<Hit<T>> hitArray = hits.hits();
        
        EsHits<T> esHits = setInnerHits(hits, false);
        // 设置聚合结果 (需要根据ES8 API调整)
        // Aggregations aggregations = searchResponse.getAggregations();
        // EsPlusAggregations<T> esAggsResponse = new EsPlusAggregations<>();
        // esAggsResponse.setAggregations(aggregations);
        // esAggsResponse.settClass(tClass);
        
        // 设置返回结果
        EsResponse<T> esResponse = new EsResponse<>();
        esResponse.setTotal(hits.total().value());
        esResponse.setTook(searchResponse.took());
        
        List<EsHit<T>> esHitsList = new ArrayList<>();
        for (Hit<T> hit : hitArray) {
            EsHit<T> esHit = new EsHit<>();
            esHit.setId(hit.id());
            esHit.setIndex(hit.index());
            esHit.setScore(hit.score());
            esHit.setSource(hit.source());
            esHit.setHighLight(hit.highlight());
            esHitsList.add(esHit);
        }
        
        EsHits<T> esHitsWrapper = new EsHits<>();
        esHitsWrapper.setHits(esHitsList);
        esResponse.setHits(esHitsWrapper);
        
        // esResponse.setSkippedShards(searchResponse.getSkippedShards());
        // esResponse.setSuccessfulShards(searchResponse.getSuccessfulShards());
        // esResponse.setTotalShards(searchResponse.getTotalShards());
        // esResponse.setScrollId(searchResponse.getScrollId());
        esResponse.setInnerHits(esHits);
        // esResponse.setSourceResponse(searchResponse.toString());
        
        // 设置最小和最大的排序字段值
        if (!CollectionUtils.isEmpty(hitArray)) {
            // ES8中获取排序值的方法可能不同
            // esResponse.setFirstSortValues(hitArray.get(0).getSortValues());
            // esResponse.setTailSortValues(hitArray.get(hitArray.length - 1).getSortValues());
        }
        
        return esResponse;
    }
    
    private EsHits<T> setInnerHits(HitsMetadata<T> hits, boolean populate) {
        if (hits == null || CollectionUtils.isEmpty(hits.hits())) {
            return null;
        }
        
        // 如果没有嵌套类则不填充
        // boolean anyMatch = hits.hits().stream().anyMatch(a -> CollectionUtils.isEmpty(a.getInnerHits()));
        // if (anyMatch) {
        //     return null;
        // }
        
        EsHits<T> esHits = new EsHits<>();
        long totalHits = hits.total().value();
        esHits.setTotal(totalHits);
        
        List<EsHit<T>> esHitList = new ArrayList<>();
        esHits.setHits(esHitList);
        
        for (Hit<T> searchHit : hits.hits()) {
            EsHit<T> esHit = new EsHit<>();
            // 一级数据不填充
            if (populate) {
                // String sourceAsString = searchHit.source();
                // esHit.setData(sourceAsString);
            }
            esHitList.add(esHit);
            
            // Map<String, SearchHits> innerHits = searchHit.getInnerHits();
            
            // // 填充innerHits
            // if (!CollectionUtils.isEmpty(innerHits)) {
            //     Map<String, EsHits> esHitsMap = new HashMap<>();
            //     innerHits.forEach((k, v) -> {
            //         EsHits eshits = setInnerHits(v, true);
            //         esHitsMap.put(k, eshits);
            //     });
            //     esHit.setInnerHitsMap(esHitsMap);
            // }
        }
        return esHits;
    }
    
}
