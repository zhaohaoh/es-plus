package com.es.plus.constant;


public interface EsConstant {
    /* 字段属性
     */
    String TYPE = "type";
    /**
     * 文本
     */
    String TEXT = "text";
    /**
     * 分析仪
     */
    String ANALYZER = "analyzer";
    /**
     * 搜索分析仪
     */
    String SEARCH_ANALYZER = "search_analyzer";
    /**
     * 是否额外存储
     */
    String STORE = "store";
    /**
     * 比重
     */
    String BOOST = "boost";
    /**
     * 索引
     */
    String INDEX = "index";
    /**
     * 时间格式
     */
    String FORMAT = "format";
    /**
     * 属性
     */
    String PROPERTIES = "properties";
    /**
     * 字段
     */
    String FIELDS = "fields";

    /**
     * 代表keyword字符串有效搜索长度
     */
    String IGNORE_ABOVE = "ignore_above";

    /* 字段属性
     */
    String REINDEX_LOCK_SUFFIX = "_reindex_lock";

    /* 字段属性
     */
    String DELIMITER = "_";

    // 聚合字段分隔符
    String AGG_DELIMITER = "_";
    /**
     * 片数量
     */
    String NUMBER_OF_SHARDS = "index.number_of_shards";

    /**
     * 最大结果窗口数量
     */
    String MAX_RESULT_WINDOW = "index.max_result_window";
    /**
     * 数量副本
     */
    String NUMBER_OF_REPLICAS = "index.number_of_replicas";
    /**
     * 默认迁移操作规则 覆盖
     */
    String DEFAULT_DEST_OP_TYPE = "index";
    /**
     * 默认冲突处理
     */
    String DEFAULT_CONFLICTS = "proceed";
    /**
     * 自动创建的索引后缀
     */
    String SO_SUFFIX = "_s0";
    /**
     * 自动创建的索引后缀
     */
    String S1_SUFFIX = "_s1";

    /**
     * LOCK 名
     */
    String GLOBAL_LOCK = "ep_global_lock";


    /**
     * LOCK过期时间的key
     */
    String GLOBAL_LOCK_EXPIRETIME =  "lockExpireTime";
    /**
     * LOCK过期时间 秒
     */
    int GLOBAL_LOCK_TIMEOUT = 60;
    /**
     * 看门狗重新加锁时间 秒
     */
    int WATCH_DOG_RELOCK = 25;

    /**
     * 重新索引的更新时间
     */
    String REINDEX_TIME_FILED = "reindexTime";

    /**
     * 重建索引时的执行锁
     */
    String REINDEX_UPDATE_LOCK = "_reindex_update_lock";

}
