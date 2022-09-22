package com.es.plus.pojo;

import com.es.plus.constant.EsConstant;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.aggregation.AbstractLambdaAggregationWrapper;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.RareTerms;
import org.elasticsearch.search.aggregations.bucket.terms.RareTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.BucketMetricValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: hzh
 * @Date: 2022/6/21 12:31
 */
public class EsAggregationsResponse<T> extends AbstractLambdaAggregationWrapper<T, SFunction<T, ?>> {
    private Aggregations aggregations;

    public void settClass(Class<T> tClass) {
        super.tClass = tClass;
    }


    public Aggregations getAggregations() {
        return aggregations;
    }

    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }


    /**
     * 根据名称获取聚合
     */
    public Aggregation get(String name) {
        return aggregations.get(name);
    }

    /**
     * 得到聚合的map
     *
     */
    public Map<String, Long> getTermsAsMap(SFunction<T, ?> name) {
        Terms terms = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
        Map<String, Long> data = new HashMap<>();
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            data.put(keyAsString, bucket.getDocCount());
        }
        return data;
    }

    /**
     * 得到esplus封装的map
     *
     * @param name 名字
     * @return {@link Map}<{@link String}, {@link EsPLusTerms}<{@link T}>>
     */
    public Map<String, EsPLusTerms<T>> getEsPLusTermsAsMap(SFunction<T, ?> name) {
        Terms terms = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
        Map<String, EsPLusTerms<T>> data = new HashMap<>();
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            EsPLusTerms<T> pLusTerms = new EsPLusTerms<>();
            pLusTerms.setDocCount(bucket.getDocCount());
            pLusTerms.setDocCountError(bucket.getDocCountError());
            EsAggregationsResponse<T> tEsAggregationsResponse = new EsAggregationsResponse<>();
            tEsAggregationsResponse.settClass(super.tClass);
            tEsAggregationsResponse.setAggregations(bucket.getAggregations());
            pLusTerms.setEsAggregationsReponse(tEsAggregationsResponse);
            data.put(keyAsString, pLusTerms);
        }
        return data;
    }

    public Terms getTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }

    public RareTerms getRareTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    }

    public Filters getFilters(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }

    public AdjacencyMatrix getAdjacencyMatrix(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }

    public SignificantTerms getSignificantTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }

    public Histogram getHistogram(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }

    public GeoGrid getGeoGrid(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    }

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

    public BucketMetricValue getBucketMetricValue(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SumAggregationBuilder.NAME);
    }

}
