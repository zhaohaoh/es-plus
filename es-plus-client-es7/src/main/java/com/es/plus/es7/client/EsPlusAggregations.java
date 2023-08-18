package com.es.plus.es7.client;

import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.adapter.tools.SerializedLambda;
import com.es.plus.constant.EsConstant;
import org.apache.commons.lang3.StringUtils;
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
import org.elasticsearch.search.aggregations.metrics.*;

import java.util.*;


/**
 * @Author: hzh
 * @Date: 2022/6/21 12:31
 */

public class EsPlusAggregations<T> implements EsAggResponse<T> {
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
    public Aggregation get(String name) {
        return aggregations.get(name);
    }

    /**
     * 得到聚合的map
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
//    public Map<String, EsPLusTerms<T>> getEsPLusTermsAsMap(SFunction<T, ?> name) {
//        Terms terms = aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
//        Map<String, EsPLusTerms<T>> data = new HashMap<>();
//        List<? extends Terms.Bucket> buckets = terms.getBuckets();
//        for (Terms.Bucket bucket : buckets) {
//            String keyAsString = bucket.getKeyAsString();
//            EsPLusTerms<T> pLusTerms = new EsPLusTerms<>();
//            pLusTerms.setDocCount(bucket.getDocCount());
//            pLusTerms.setDocCountError(bucket.getDocCountError());
//            EsAggregations<T> tEsAggregationsResponse = new EsAggregations<>();
//            tEsAggregationsResponse.settClass(tClass);
//            tEsAggregationsResponse.setAggregations(bucket.getAggregations());
//            pLusTerms.setEsAggregationsReponse(tEsAggregationsResponse);
//            data.put(keyAsString, pLusTerms);
//        }
//        return data;
//    }
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
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }

    public SignificantTerms getSignificantTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }

    public Histogram getHistogram(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + EsConstant.AGG_DELIMITER + HistogramAggregationBuilder.NAME);
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


    /**
     * 得到聚合的map
     */
    public Map<String, Long> getTermsAsMap(String name) {
        Terms terms = aggregations.get(name + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
        Map<String, Long> data = new LinkedHashMap<>();
        if (terms == null) {
            return data;
        }

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
//    public Map<String, EsPLusTerms<T>> getEsPLusTermsAsMap(String name) {
//        Terms terms = aggregations.get(name + EsConstant.AGG_DELIMITER + TermsAggregationBuilder.NAME);
//        Map<String, EsPLusTerms<T>> data = new HashMap<>();
//        List<? extends Terms.Bucket> buckets = terms.getBuckets();
//        for (Terms.Bucket bucket : buckets) {
//            String keyAsString = bucket.getKeyAsString();
//            EsPLusTerms<T> pLusTerms = new EsPLusTerms<>();
//            pLusTerms.setDocCount(bucket.getDocCount());
//            pLusTerms.setDocCountError(bucket.getDocCountError());
//            EsAggregations<T> tEsAggregationsResponse = new EsAggregations<>();
//            tEsAggregationsResponse.settClass(tClass);
//            tEsAggregationsResponse.setAggregations(bucket.getAggregations());
//            pLusTerms.setEsAggregationsReponse(tEsAggregationsResponse);
//            data.put(keyAsString, pLusTerms);
//        }
//        return data;
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
        SerializedLambda lambda = LambdaUtils.resolve(function);
        return getColumn(lambda);
    }

    private String getColumn(SerializedLambda lambda) {
        return methodToProperty(lambda.getImplMethodName());
    }

    private String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new EsException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }


}
