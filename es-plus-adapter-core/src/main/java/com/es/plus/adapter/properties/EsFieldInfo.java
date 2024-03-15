package com.es.plus.adapter.properties;

import com.es.plus.constant.EsFieldType;
import lombok.Data;

/**
 * es字段参数
 *
 * @author hzh
 * @date 2023/07/25
 */
@Data
public class EsFieldInfo {
    /**
     * 是否id
     *
     * @return {@link String}
     */
    private Boolean esId;
    /**
     * 名字
     *
     * @return {@link String}
     */
    private String name;

    /**
     * 类型
     *
     * @return {@link EsFieldType}
     */
    private EsFieldType type;

    /**
     * 是否被索引
     *
     * @return boolean
     */
    private boolean index;

    /**
     * 搜索分析仪
     *
     * @return {@link String}
     */
    private String searchAnalyzer;

    /**
     * 分析仪
     *
     * @return {@link String}
     */
    private String analyzer;

    /**
     * 是否存储
     *
     * @return boolean
     */
    private boolean store;

    /**
     * 设置text可以进行聚合操作 会有性能问题。会把text数据加载到内存中。  默认keyword类型使用的是Doc Values
     * 默认建立doc_values,即字段类型为keyword，他不会创建分词，就会默认建立doc_value，如果我们不想该字段参与聚合排序，我们可以设置doc_values=false
     *
     * @return 是否设置可聚合
     */
    private boolean fieldData;

    /**
     * 设置keyword字段处理器 只会设置keyword类型字段
     * 全局目前已配置自带的转小写normalizer
     */
    private String normalizer;

    /**
     * 把字段复制到某一个字段。搜索的时候根据这个字段搜索。无需合并倒排链
     * 类似联合索引
     * 例:一个模糊查询字段要查询 name nicknam username phone 等字段。组合为一个模糊查询字段。磁盘空间占用变大。性能也变更好。
     * 因为无需多次检索FST的树
     * 对整合后的字段聚合的话，会得到多个字段的聚合结果  目标对象text和keyword类型都能使用
     */
    private String[] copyTo;

    /**
     * 存在
     *
     * @return boolean
     */
    private boolean exist;



    /**
     * es的日期支持的存储格式。 || 可以指定多个   但不是正反序列化使用的格式  但是date转换成string的序列化不归此参数管理
     *
     * @return {@link String}
     */
    private String esFormat;

    /**
     * 指定时间类型序列化存储的格式
     */
    private String dateFormat;
    /**
     * 转换字符串的时区
     */
    private String timeZone;
    /**
     * 全局序数  插入的时候即对聚合进行预处理 提高聚合性能 适用于聚合字段和父子文档
     * 适用场景：高基数聚合 即非重复属性多的字段
     * 表示是否提前加载全局顺序号。 Global ordinals 是一个建立在 doc values 和 fielddata 基础上的数据结构
     * | Doc   | Terms                                                   |
     * | ----- | ------------------------------------------------------- |
     * | Doc_1 | brown, dog, fox, jumped, lazy, over, quick, the         |
     * | Doc_2 | brown, dogs, foxes, in, lazy, leap, over, quick, summer |
     * <p>
     * | Terms | ordinal |
     * | ----- | ------- |
     * | brown | 1       |
     * | dog   | 2       |
     * | fox   | 3       |
     *
     * @return boolean
     */
    private boolean eagerGlobalOrdinals;


    /**
     * 父
     *
     * @return {@link String}
     */
    private String parent;

    /**
     * 子
     *
     * @return {@link String}
     */
    private String child;


}

