package com.es.plus.adapter.constants;



import com.es.plus.adapter.config.ConnectFailHandleEnum;
import com.es.plus.constant.Analyzer;
import lombok.Data;
import org.elasticsearch.action.support.WriteRequest;

/**
 * @Author: hzh
 * @Date: 2022/11/1 10:21
 * es全局配置策略
 */
@Data
public class GlobalConfig {
    /**
     * es版本 目前支持6和7，默认是7
     */
    private Integer version = 7;
    /**
     * 类型
     */
    private String type = "_doc";
    /**
     * 全局后缀
     */
    private String globalSuffix = "";
    /**
     * 自动开启索引迁移
     */
    private boolean autoReindex = false;
    /**
     * 自动开启索引迁移的范围   1.all=所有 适用测试环境。2输入index的名称。指定索引名。3不设置，空字符串不开启自动reindex索引。
     *
     */
    private String reindexScope = "";

    /**
     * 自动开启索引迁移是否异步
     */
    private boolean reindexAsync = false;

    /**
     * 是否开启查询全部数据 默认关闭
     */
    private boolean trackTotalHits = false;
    /**
     * data refresh policy 数据刷新策略,默认为WAIT_UNTIL
     */
    private WriteRequest.RefreshPolicy refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL;
    /**
     * 最大更新文档数 默认10万
     */
    private int maxDocs = 100000;

    /**
     * 最大查询文档数量 默认1万
     */
    private int searchSize = 10000;
    
    /**
     * 最大聚合文档数量 默认1000
     */
    private int aggSize = 1000;
    
    /*
     * 批量执行的数量和scoll的批次数量
     */
    private int batchSize = 5000;
    /**
     * 更新最大尝试次数
     */
    private int maxRetries = 3;
    /**
     * 全局默认自动获取esId的字段
     */
    private String globalEsId = "id";
    /**
     * 全局索引分词器
     */
    private String defaultAnalyzer = Analyzer.EP_STANDARD;
    /**
     * 全局keyword处理器  keyword搜索字段转小写 必须在字段上加上normalizer = Analyzer.EP_NORMALIZER才生效
     */
    private String defaultNormalizer = Analyzer.EP_NORMALIZER;
    /**
     * es id设置方案
     */
    private EsIdType esIdType = EsIdType.DEFAULT;

    /**
     * 查询超时时间 最大努力，并不是所有地方都会检查超时。模糊查询肯定不会有效
     */
    private Integer searchTimeout = 30;
    
    /**
     * 更新操作超时时间.这个操作先不加吧。有些更新动作时间比较久
     */
//    private Integer updateTimeout = 60;
    /**
     * 启动检查
     */
    private ConnectFailHandleEnum connectFailHandle = ConnectFailHandleEnum.THROW_EXCEPTION;

    /**
     * 启动时初始化 如果启动检查失败会忽略初始化
     */
    private boolean startInit = true;

    /**
     * 是否开启日志
     */
    private boolean enableSearchLog = true;
    /**
     * 配置刷新的文件名
     */
    private String refreshDataId;
    /**
     * 配置刷新的文件分组
     */
    private String refreshGroup;
    /**
     * 使用的配置类型 nacos，file
     */
    private String  configType;
    /**
     * wildcard字段查询长度限制 能够有效减少es查询的压力，减少cpu压力。因为wildcard会构建一个词树，根据词的数量笛卡尔积
     */
    private Integer wildcardQueryLimit = 50;
}
