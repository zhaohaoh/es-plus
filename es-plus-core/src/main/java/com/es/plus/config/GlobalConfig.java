package com.es.plus.config;


import com.es.plus.constant.Analyzer;
import com.es.plus.enums.ConnectFailHandle;
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
     * 全局后缀
     */
    private String globalSuffix = "";
    /**
     * 自动开启索引迁移
     */
    private boolean autoReindex = false;

    /**
     * 自动开启索引迁移是否异步
     */
    private boolean reindexAsync = false;

    /**
     * 是否开启查询全部数据 默认开启
     */
    private boolean trackTotalHits = true;
    /**
     * data refresh policy 数据刷新策略,默认为WAIT_UNTIL
     */
    private WriteRequest.RefreshPolicy refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL;
    /**
     * 最大更新文档数 默认100万
     */
    private int maxDocs = 1000000;

    /**
     * 最大查询文档数量 默认10万
     */
    private int searchSize = 100000;
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
     * 启动检查
     */
    private ConnectFailHandle connectFailHandle = ConnectFailHandle.THROW_EXCEPTION;

    /**
     * 启动时初始化
     */
    private boolean startInit = true;

}
