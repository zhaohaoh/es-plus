package com.es.plus.common.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

@Data
public class EsSettings {
    /**
     * 静态 无法修改
     */
    //分片数
    @JsonProperty("number_of_shards")
    private Integer numberOfShards;
    //自定义路由值可以转发的目的分片数。默认为 1，只能在索引创建时设置。此值必须小于index.number_of_shards
    @JsonProperty("routing_partition_size")
    private Integer routingPartitionSize;
    @JsonProperty("index.sort.field")
    private String[] sortField;
    @JsonProperty("index.sort.order")
    private String[] sortOrder;
    /**
     * 动态
     */
    //每个主分片的副本数。默认为 1。
    @JsonProperty("number_of_replicas")
    private Integer numberOfReplicas;
    //用于索引搜索的 from+size 的最大值。默认为 10000
    @JsonProperty("max_result_window")
    private Integer maxResultWindow;
    //基于可用节点的数量自动分配副本数量,默认为 false（即禁用此功能）
    @JsonProperty("auto_expand_replicas")
    private String autoExpandReplicas;
    //es内存数据刷新到主机内存的周期默认1s  支持ms  m
    @JsonProperty("refresh_interval")
    private String refreshInterval;
    @JsonProperty("analysis.analyzer.default.type")
    private String defaultAnalyzer;

    @JsonProperty("analysis")
    private Map<String, Object> analysis;



    /**
     * 查询日志 收集文档id慢查询日志
     */
    @JsonProperty("search.slowlog.threshold.query.debug")
    private Duration queryDebug;
    @JsonProperty("search.slowlog.threshold.query.info")
    private Duration queryWarn;
    @JsonProperty("search.slowlog.threshold.query.warn")
    private Duration queryInfo;
    @JsonProperty("search.slowlog.threshold.query.trace")
    private Duration queryTrace;
    /**
     * 根据文档id拉取数据慢查询日志
     */
    @JsonProperty("search.slowlog.threshold.fetch.warn")
    private Duration fetchWarn;
    @JsonProperty("search.slowlog.threshold.fetch.info")
    private Duration fetchInfo;
    @JsonProperty("search.slowlog.threshold.fetch.debug")
    private Duration fetchDebug;
    @JsonProperty("search.slowlog.threshold.fetch.trace")
    private Duration fetchTrace;
    //日志级别
    @JsonProperty("index.search.slowlog.level") //info warn  debug trace
    private String searchlevel;

    //索引日志
    @JsonProperty("indexing.slowlog.threshold.index.warn")
    private Duration indexWarn;
    @JsonProperty("indexing.slowlog.threshold.index.info")
    private Duration indexInfo;
    @JsonProperty("indexing.slowlog.threshold.index.debug")
    private Duration indexDebug;
    @JsonProperty("indexing.slowlog.threshold.index.trace")
    private Duration indexTrace;
    @JsonProperty("indexing.slowlog.level") //info warn  debug trace
    private String indexlevel;
    @JsonProperty("indexing.slowlog.source")
    private Integer indexSource;

}


