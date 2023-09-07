package com.es.plus.adapter.params;

import lombok.Data;

@Data
public class EsSelect {
    //查询结果包含字段
    private String[] includes;
    //查询结果不包含字段
    private String[] excludes;

    private Boolean fetch ;

    private Float minScope;

    private Boolean trackScores ;

}
