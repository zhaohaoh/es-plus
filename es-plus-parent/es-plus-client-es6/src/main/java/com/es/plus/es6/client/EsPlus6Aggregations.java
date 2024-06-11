package com.es.plus.es6.client;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsAggResult;
import com.es.plus.adapter.params.EsAggResultQuery;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.adapter.util.SearchHitsUtil;
import com.es.plus.constant.EsConstant;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.weighted_avg.WeightedAvg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: hzh
 * @Date: 20.2.7/21 12:31
 */
public class EsPlus6Aggregations<T> implements EsAggResponse<T> {
    
    private Aggregations aggregations;
    
    private Class<T> tClass;
    
    @Override
    public void settClass(Class<T> tClass) {
        this.tClass = tClass;
    }
    
    @Override
    public Aggregations getAggregations() {
        return aggregations;
    }
    
    @Override
    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }
    
    
    /**
     * 根据名称获取聚合
     */
    public Aggregation getAggregation(String name) {
        return aggregations.get(name);
    }
    
    
    /**
     * 得到esplus封装的map
     */
    @Override
    public EsAggResult<T> getEsAggResult(EsAggResultQuery esAggResultQuery) {
        return getResult(esAggResultQuery, aggregations);
    }
    
    private EsAggResult<T> getResult(EsAggResultQuery esAggResultQuery, Aggregations aggregations) {
        if (esAggResultQuery == null) {
            return new EsAggResult<>();
        }
        if (aggregations == null) {
            return new EsAggResult<>();
        }
        EsAggResult<T> esAgg = new EsAggResult<>();
        String count = esAggResultQuery.getCount();
        if (StringUtils.isNotBlank(count)) {
            ValueCount value = aggregations.get(count);
            esAgg.setCount(value.getValue());
        }
        String avg = esAggResultQuery.getAvg();
        if (StringUtils.isNotBlank(avg)) {
            Avg value = aggregations.get(avg);
            esAgg.setAvg(value.getValue());
        }
        String max = esAggResultQuery.getMax();
        if (StringUtils.isNotBlank(max)) {
            Max value = aggregations.get(max);
            esAgg.setMax(value.getValue());
        }
        String sum = esAggResultQuery.getSum();
        if (StringUtils.isNotBlank(sum)) {
            Sum value = aggregations.get(sum);
            esAgg.setSum(value.getValue());
        }
        String min = esAggResultQuery.getMin();
        if (StringUtils.isNotBlank(min)) {
            Min value = aggregations.get(min);
            esAgg.setMin(value.getValue());
        }
        String topHits = esAggResultQuery.getTopHits();
        if (StringUtils.isNotBlank(topHits)) {
            TopHits value = aggregations.get(topHits);
            SearchHits hits = value.getHits();
            List<T> result = SearchHitsUtil.parseList(tClass, hits.getHits());
            esAgg.setTopHits(result);
        }
        String term = esAggResultQuery.getTerm();
        if (StringUtils.isNotBlank(term)) {
            Terms value = aggregations.get(term);
            Map<String, EsAggResult<T>> data = new HashMap<>();
            esAgg.setEsAggTermsMap(data);
            List<? extends Terms.Bucket> buckets = value.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                Aggregations bucketAggregations = bucket.getAggregations();
                EsAggResult<T> agg = getResult(esAggResultQuery.getSubQuery(), bucketAggregations);
                long docCount = bucket.getDocCount();
                agg.setTermDocCount(docCount);
                String keyAsString = bucket.getKeyAsString();
                data.put(keyAsString, agg);
            }
        }
        
        return esAgg;
    }
    
    
    public Terms getTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }
    
    //    public RareTerms getRareTerms(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    //    }
    
    public Filters getFilters(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }
    
    public AdjacencyMatrix getAdjacencyMatrix(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }
    
    public SignificantTerms getSignificantTerms(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }
    
    public Histogram getHistogram(SFunction<T, ?> name) {
        return aggregations
                .get(getAggregationField(name) + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }
    
    //    public GeoGrid getGeoGrid(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    //    }
    
    public Max getMax(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + MaxAggregationBuilder.NAME);
    }
    
    public Avg getAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AvgAggregationBuilder.NAME);
    }
    
    public Sum getSum(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public ValueCount getCount(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public WeightedAvg getWeightedAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    //    public BucketMetricValue getBucketMetricValue(SFunction<T, ?> name) {
    //        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    //    }
 
    public Terms getTerms(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }
    
    //    public RareTerms getRareTerms(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    //    }
    
    public Filters getFilters(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }
    
    public AdjacencyMatrix getAdjacencyMatrix(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }
    
    public SignificantTerms getSignificantTerms(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }
    
    public Histogram getHistogram(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }
    
    //    public GeoGrid getGeoGrid(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    //    }
    
    public Max getMax(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + MaxAggregationBuilder.NAME);
    }
    
    public Avg getAvg(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + AvgAggregationBuilder.NAME);
    }
    
    public Sum getSum(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public ValueCount getCount(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    public WeightedAvg getWeightedAvg(String name) {
        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
    
    //    public BucketMetricValue getBucketMetricValue(String name) {
    //        return aggregations.get(name + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    //    }
    
    
    private String getAggregationField(SFunction<T, ?> sFunction) {
        String name = nameToString(sFunction);
        String keyword = GlobalParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }
    
    private String nameToString(SFunction<T, ?> function) {
        String fieldName = LambdaUtils.getFieldName(function);
        EsFieldInfo indexField = GlobalParamHolder.getIndexField(tClass, fieldName);
        return indexField != null && StringUtils.isNotBlank(indexField.getName()) ? indexField.getName() : fieldName;
    }
    
}
