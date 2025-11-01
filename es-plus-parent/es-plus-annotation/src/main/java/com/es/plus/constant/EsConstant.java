package com.es.plus.constant;


import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public interface EsConstant {
    /* 框架缩写
     */
    String EP = "ep";
    
    /* 主库
     */
    String MASTER = "master";
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
     * 针对keyword使用的查询前字段处理 和analyzer类似
     */
    String NORMALIZER = "normalizer";
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
     * 复制字段到指定字段进行冗余
     */
    String COPY_TO = "copy_to";
    /**
     * 时间格式
     */
    String FORMAT = "format";
    /**
     * 属性
     */
    String PROPERTIES = "properties";
    /**
     * routing
     */
    String ROUTING = "_routing";
    /**
     * 引用
     */
    String RELATIONS = "relations";
    
    /**
     * 全球序数
     */
    String EAGER_GLOBAL_ORDINALS = "eager_global_ordinals";
    /**
     * 字段
     */
    String FIELDS = "fields";
    
    /**
     * 代表keyword字符串有效搜索长度
     */
    String IGNORE_ABOVE = "ignore_above";
    
    
    /* 字段属性分割府
     */
    String DELIMITER = "_";
    
    /*
     点
     */
    String DOT = ".";
    
    // 聚合字段分隔符
    String AGG_DELIMITER = "_";
    /**
     * 片数量
     */
    String NUMBER_OF_SHARDS = "number_of_shards";
    
    /**
     * 最大结果窗口数量
     */
    String MAX_RESULT_WINDOW = "max_result_window";
    /**
     * 数量副本
     */
    String NUMBER_OF_REPLICAS = "number_of_replicas";
    /**
     * 默认迁移操作规则 覆盖index   create只创建      index和EXTERNAL搭配使用可以达到版本号大于目标版本号才会更新  create和EXTERNAL一起使用，也会版本号大于目标版本号才会更新
     * 返回体的"version_conflicts": 0,代表冲突数量。如果只配置了index那么冲突数量为0。说明有配置version_type以version_type为主
     * {
     * "conflicts": "proceed",
     * "source": {
     * "index": "xx"
     * },
     * "dest": {
     * "index": "xxxxxx",
     * "op_type": "index",
     * "version_type": "external"
     * }
     * }
     */
    String DEFAULT_DEST_OP_TYPE = "index";
    /**
     * 默认冲突处理 忽略proceed冲突继续执行下一个
     */
    String DEFAULT_CONFLICTS = "proceed";
    /**
     * 默认版本号冲突规则  INTERNAL 和EXTERNAL   EXTERNAL会判断版本号大于目标版本号才会更新  INTERNAL是都会更新
     */
    String DEFAULT_REINDEX_VERSION_TYPE = "EXTERNAL";
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
     * LOCK 名
     */
    Duration SCROLL_KEEP_TIME = Duration.ofMinutes(5);
    
    /**
     * LOCK过期时间的key
     */
    String GLOBAL_LOCK_EXPIRETIME = "lockExpireTime";
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
    String REINDEX_TIME_FILED = "epReindexTime";
    
    /**
     * 重建索引时的锁 决定索引是否在reindex
     */
    String REINDEX_LOCK_SUFFIX = "_reindex_lock";
    
    /**
     * 重建索引时的执行锁  重建索引时。插入更新数据需要获取的读写锁
     */
    String REINDEX_UPDATE_LOCK = "_reindex_update_lock";
    
    /**
     * 内部嵌套返回体的记录父类id
     */
    String INNER_HITS_PARENT_ID = "parentId";
    /**
     * 内部嵌套对象
     */
    String INNERHITS = "innerHits";
    /**
     * 脚本语言 painless的常量
     */
    String PAINLESS = "painless";
    
    // 字段映射
    Map<String, Object> KEYWORDS_MAP = new HashMap<String, Object>() {{
        Map<String, Object> keywordsMap = new HashMap<>();
        keywordsMap.put(EsConstant.TYPE, "keyword");
        keywordsMap.put(IGNORE_ABOVE, 256);
        put("keyword", keywordsMap);
    }};
    
}