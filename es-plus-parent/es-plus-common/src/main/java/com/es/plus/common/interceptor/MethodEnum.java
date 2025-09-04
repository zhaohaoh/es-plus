package com.es.plus.common.interceptor;

import com.es.plus.common.constants.EsPlusMethodConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MethodEnum {
    /**
     * 所有新增
     */
    SAVE(new String[] {EsPlusMethodConstant.SAVE, EsPlusMethodConstant.SAVE_BATCH,
            EsPlusMethodConstant.SAVE_OR_UPDATE_BATCH}),
    /**
     * 所有更新
     */
    UPDATE(new String[] {EsPlusMethodConstant.UPDATE, EsPlusMethodConstant.UPDATE_BATCH,
            EsPlusMethodConstant.SAVE_OR_UPDATE_BATCH, EsPlusMethodConstant.UPDATE_BY_WRAPPER,
            EsPlusMethodConstant.INCREMENT}),
    /**
     * 所有删除
     */
    DELETE(new String[] {EsPlusMethodConstant.DELETE, EsPlusMethodConstant.DELETE_BATCH,
            EsPlusMethodConstant.DELETE_BY_QUERY}),
    /**
     * 所有搜索方法包括聚合
     */
    SEARCH(new String[] {EsPlusMethodConstant.SEARCH, EsPlusMethodConstant.COUNT, EsPlusMethodConstant.SCROLL,
            EsPlusMethodConstant.AGGREGATIONS}),
    /**
     * 所有聚合方法
     */
    AGGREGATIONS(new String[] {EsPlusMethodConstant.AGGREGATIONS});
    
    private String[] methods;
    
    public String[] getMethods() {
        return methods;
    }
}
