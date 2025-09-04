package com.es.plus.common.params;

import com.es.plus.common.interceptor.EsUpdateField;
import lombok.Data;

@Data
public class EsParamWrapper<T> {
    
    /**
     * es查询参数包装器
     */
    private EsQueryParamWrapper esQueryParamWrapper = new EsQueryParamWrapper();
    
    /*
     * 更新字段封装
     */
    protected EsUpdateField esUpdateField = new EsUpdateField();
    /**
     * class类
     */
    private Class<T> tClass;
}
