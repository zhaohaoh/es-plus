package com.es.plus.common.params;

import lombok.Data;

import java.util.Set;

@Data
public class EsAliasResponse {
    /**
     * 这个别名的全部索引
     */
    private Set<String> indexs;
}
