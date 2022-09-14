package com.es.plus.constant;

public enum EsFieldType {
    /**
     * 文本
     *///es字段枚举类型
    TEXT,
    /**
     * 字节
     */
    BYTE,
    /**
     * 短
     */
    SHORT,
    /**
     * 整数
     */
    INTEGER,
    /**
     * 长
     */
    LONG,
    /**
     * 日期
     */
    DATE,
    /**
     * 半浮动
     */
    HALF_FLOAT,
    /**
     * 浮动
     */
    FLOAT,
    /**
     * 双精度
     */
    DOUBLE,
    /**
     * 布尔
     */
    BOOLEAN,
    /**
     * 对象 默认是普通内部对象
     */
    OBJECT,
    /**
     * 自动映射关系
     */
    AUTO,
    /**
     * 嵌套对象
     */
    NESTED,
    IP,
    ATTACHMENT,
    /**
     * 关键字
     */
    KEYWORD,
    //同时创建text和keyword
    STRING;

    public String convert(String fieldType){
        if ("int".equalsIgnoreCase(fieldType)){
            return "integer";
        }
        return fieldType;
    }


}