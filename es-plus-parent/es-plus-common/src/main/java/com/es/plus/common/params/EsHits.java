package com.es.plus.common.params;

import lombok.Data;

import java.util.List;

@Data
public class EsHits {
    /**
     * 总数
     */
    private long total;

    private List<EsHit> esHitList;
}
