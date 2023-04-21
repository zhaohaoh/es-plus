package com.es.plus.pojo;

import com.es.plus.es6.client.EsPlusAggregations;
import lombok.Data;

@Data
public class EsPLusTerms<T> {
    private Long docCount;
    private Long docCountError;
    private EsPlusAggregations<T> esAggregationsReponse;
}
