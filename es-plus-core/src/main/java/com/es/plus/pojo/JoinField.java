package com.es.plus.pojo;

import lombok.Data;

@Data
public class JoinField {
    /**
     * 字段名
     */
    private String name;
    /**
     * 父文档id
     */
    private String parent;
}