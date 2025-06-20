package com.es.plus.adapter.params;

import lombok.Data;
import lombok.ToString;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

@Data
@ToString
public class BulkProcessorParam {
    
    private int bulkActions = 1000;
    
    private ByteSizeValue bulkSize = new ByteSizeValue(5, ByteSizeUnit.MB);
    
    private TimeValue flushInterval = TimeValue.timeValueSeconds(5);
    
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
