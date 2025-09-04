package com.es.plus.common.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReindexScopeEnum {
    // 全部索引都开启索引自动reindex迁移。
    ALL("all");
    private String value;
}
