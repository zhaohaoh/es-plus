package com.es.plus.adapter.properties;


import com.es.plus.adapter.params.EsSettings;
import lombok.Data;

import java.util.Map;

@Data
public class EsIndexParam {
    /**
     * 索引名 创建的索引后缀+S0或者S1 但这里存的默认和别名一样
     */
    private String index;
    /**
     * 索引别名  默认index就是索引别名
     */
    private String alias;
    /**
     * 类型
     */
    private String type;
    /**
     * 索引配置
     */
    private EsSettings esSettings;
    /**
     * 索引映射
     */
    private Map<String, Object> mappings;
    /**
     * 子索引的类
     */
    private Class<?> childClass;


}