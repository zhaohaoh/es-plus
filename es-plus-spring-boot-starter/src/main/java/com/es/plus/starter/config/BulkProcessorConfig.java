package com.es.plus.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.function.BiConsumer;
/**
 * @Author: hzh
 * @Date: 2021/9/17 14:48
 * 暂不使用
 * Bulk有它的弊端， 插入数据没有限制  1000万数据也会直接写入
 * BulkProcessor自定义处理分批次写入。
 * 累计到指定的数量或者达到超时时间就会进行一次写入
 * 异步写入
 */
@Slf4j
@Configuration
public class BulkProcessorConfig {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Bean(name = "bulkProcessor") // 可以封装为一个bean，非常方便其余地方来进行 写入 操作
    public BulkProcessor bulkProcessor() {

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

        return BulkProcessor.builder(bulkConsumer, new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        // 写入之前
                        int i = request.numberOfActions();
                        log.info("ES 同步数量{}", i);
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        // 写入之后执行
                        Iterator<BulkItemResponse> iterator = response.iterator();
                        while (iterator.hasNext()) {
                            log.info("写入es成功:{}",iterator.next());
                        }
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                         //写入失败后
                        log.error("写入ES失败",failure);
                    }
                }).setBulkActions(1000) //  达到刷新的条数
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)) // 达到 刷新的大小
                .setFlushInterval(TimeValue.timeValueSeconds(3)) // 固定刷新的时间频率
                .setConcurrentRequests(1) //并发线程数
                //写入失败100毫秒后重试。最多3次
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) // 重试补偿策略
                .build();
    }
}
