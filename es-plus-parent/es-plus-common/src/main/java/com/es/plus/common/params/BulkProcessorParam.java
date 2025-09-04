package com.es.plus.common.params;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BulkProcessorParam {
    
    private int bulkActions = 1000;
    //单位MB
    private int bulkSize =5;
    //刷新时间 单位秒
    private int flushInterval = 5;
    
    private int concurrent = 1;
    
    /**
     * 失败后间隔多久重试 单位ms  默认500ms
     */
    private int BackoffPolicyTime = 500;
    
    /**
     * 失败后间隔重试最大次数
     */
    private int BackoffPolicyRetryMax = 2;
}
