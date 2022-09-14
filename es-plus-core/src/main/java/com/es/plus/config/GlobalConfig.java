package com.es.plus.config;


import lombok.Data;
import org.elasticsearch.action.support.WriteRequest;


@Data
public class GlobalConfig {
    /**
     * 全局后缀
     */
    private String globalSuffix = "";
    /**
     * 自动开启索引迁移
     */
    private boolean indexAutoMove = true;

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
     * 最大更新文档数
     */
    private int maxDocs = Integer.MAX_VALUE;

    /**
     * 最大查询文档数量
     */
    private int searchSize = Integer.MAX_VALUE;
    /*
     * 批量执行的数量和scoll的批次数量
     */
    private int batchSize = 5000;
    /**
     * 更新最大尝试次数
     */
    private int maxRetries = 3;

}
