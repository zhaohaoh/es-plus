package com.es.plus.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
    private Map<String,Object> analysis;

}


