package com.es.plus.common.properties;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * es字段参数
 *
 * @author hzh
 * @date 2023/07/25
 */
@Data
public class EsEntityInfo {
    /**
     * id名称
     */
    private  String idName;
    /**
     * es索引参数
     */
    private  EsIndexParam esIndexParam;

    /**
     * 转换关键字映射
     */
    private  Map<String, String>  convertKeywordMap = new ConcurrentHashMap<>();

    // 字段属性存储
    private  Map<String, EsFieldInfo> fieldsInfoMap = new ConcurrentHashMap<>();

    // es映射字段->实体类字段的映射关系
    private  Map<String, String> mappingFieldMap = new ConcurrentHashMap<>();

}

