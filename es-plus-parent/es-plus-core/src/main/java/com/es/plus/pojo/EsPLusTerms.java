package com.es.plus.pojo;

import com.es.plus.es6.client.EsPlus6Aggregations;
import lombok.Data;

@Data
public class EsPLusTerms<T> {
    private Long docCount;
    private Long docCountError;
    private EsPlus6Aggregations<T> esAggregationsReponse;
}
