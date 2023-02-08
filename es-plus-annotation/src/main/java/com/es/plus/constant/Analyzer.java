package com.es.plus.constant;

public interface Analyzer {
    /**
     * 分析仪
     */
    String ANALYZER = "analyzer";

    /**
     * ep智能
     */
    String EP_IK_SMART = "ep_ik_smart";
    /**
     * ep反向马克斯词
     */
    String EP_IK_MAX_WORD = "ep_ik_max_word";
    /**
     * ep简单
     */
    String EP_SIMPLE = "ep_simple";
    /**
     * ep标准
     */
    String EP_STANDARD = "ep_standard";
    /**
     * ep关键字
     */
    String EP_KEYWORD = "ep_keyword";
    /**
     * ep停止
     */
    String EP_STOP = "ep_stop";
    /**
     * ep空格
     */
    String EP_WHITESPACE = "ep_whitespace";
    /**
     * ep模式
     */
    String EP_PATTERN = "ep_pattern";
    /**
     * ep语言
     */
    String EP_LANGUAGE = "ep_language";
    /**
     * ep雪球
     */
    String EP_SNOWBALL = "ep_snowball";



    /**
     * ep KEWORD处理器
     */
    String EP_NORMALIZER = "ep_normalizer";

    /**
     * ep智能
     */
    String IK_SMART = "ik_smart";
    /**
     * ep反向马克斯词
     */
    String IK_MAX_WORD = "ik_max_word";
    /**
     * ep简单
     */
    String SIMPLE = "simple";
    /**
     * ep标准
     */
    String STANDARD = "standard";
    /**
     * ep关键字
     */
    String KEYWORD = "keyword";
    /**
     * ep停止
     */
    String STOP = "stop";
    /**
     * ep空格
     */
    String WHITESPACE = "whitespace";
    /**
     * ep模式
     */
    String PATTERN = "pattern";
    /**
     * ep语言
     */
    String LANGUAGE = "language";
    /**
     * ep雪球
     */
    String SNOWBALL = "snowball";


    /**
     * 去除单词的复数形式，提取原单词，这样可以让单词原来的形态也能匹配
     */
    String STEMMER = "stemmer";
    /**
     * 存储转化为小写字母 默认
     */
    String LOWERCASE = "lowercase";
    /**
     * 不存在在ASCII 中的特殊字符转化存储
     */
    String ASCIIFOLDING = "asciifolding";
    /**
     * 一个字段相同的词只存储一次，会导致无法使用match_parsh
     */
    String UNIQUE = "unique";
}
