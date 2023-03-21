package com.es.plus.annotation;

import com.es.plus.constant.Analyzer;
import com.es.plus.constant.EsFieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: hzh
 * @Date: 2022/9/5 15:04
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsField {
    /**
     * 名字
     *
     * @return {@link String}
     */
    String name() default "";

    /**
     * 类型
     *
     * @return {@link EsFieldType}
     */
    EsFieldType type() default EsFieldType.AUTO;

    /**
     * 是否被索引
     *
     * @return boolean
     */
    boolean index() default true;

    /**
     * 搜索分析仪
     *
     * @return {@link String}
     */
    String searchAnalyzer() default "";

    /**
     * 分析仪
     *
     * @return {@link String}
     */
    String analyzer() default "";

    /**
     * 是否存储
     *
     * @return boolean
     */
    boolean store() default false;

    /**
     * 设置text可以进行聚合操作
     *
     * @return 是否设置可聚合
     */
    boolean fieldData() default false;

    /**
     * 设置keyword字段处理器 只会设置keyword类型字段
     * 全局目前已配置自带的转小写normalizer
     */
    String normalizer() default "";

    /**
     * 把字段复制到某一个字段。搜索的时候根据这个字段搜索。无需合并倒排链
     * 类似联合索引
     * 例:一个模糊查询字段要查询 name nicknam username phone 等字段。组合为一个模糊查询字段。磁盘空间占用变大。性能也变更好。
     * 因为无需多次检索FST的树
     * 对整合后的字段聚合的话，会得到多个字段的聚合结果  目标对象text和keyword类型都能使用
     */
    String[] copyTo() default {};

    /**
     * 存在
     *
     * @return boolean
     */
    boolean exist() default true;

    /**
     * 日期格式化
     *
     * @return {@link String}
     */
    String format() default "";

    /**
     * 全局序数 提高聚合性能 适用于聚合字段和父子文档
     *
     * @return boolean
     */
    boolean eagerGlobalOrdinals() default false;

    /**
     * 父
     *
     * @return {@link String}
     */
    String parent() default "";

    /**
     * 子
     *
     * @return {@link String}
     */
    String child() default "";
}
