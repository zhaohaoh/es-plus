package com.es.plus.web.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EsRequstInfo {

    /**
     * ids
     */
    private List<String> ids;

    /**
     * 索引
     */
    private String index;
    /**
     *
     */
    private List<Map<String, Object>> datas;
}