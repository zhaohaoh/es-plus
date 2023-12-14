package com.es.plus.adapter.params;

import com.es.plus.adapter.proxy.EsUpdateField;
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
}
