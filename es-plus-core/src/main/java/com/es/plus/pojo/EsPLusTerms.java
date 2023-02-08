package com.es.plus.pojo;

import lombok.Data;

@Data
public class EsPLusTerms<T> {
    private Long docCount;
    private Long docCountError;
    private EsAggsResponse<T> esAggregationsReponse;
}
