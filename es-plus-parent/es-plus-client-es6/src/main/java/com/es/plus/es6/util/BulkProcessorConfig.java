package com.es.plus.es6.util;

import com.es.plus.adapter.params.BulkProcessorParam;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.adapter.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2021/9/17 14:48
 * Bulk有它的弊端， 插入数据没有限制  1000万数据也会直接写入
 * BulkProcessor自定义处理分批次写入。
 * 累计到指定的数量或者达到超时时间就会进行一次写入
 * 异步写入
 */
@Slf4j
public class BulkProcessorConfig {
    
    private static final Map<String,BulkProcessor> bulkProcessorMap = new ConcurrentHashMap<>();
    
    public static BulkProcessor getBulkProcessor(RestHighLevelClient restHighLevelClient,String index) {
        BulkProcessor bulkProcessor = bulkProcessorMap.computeIfAbsent(index,
                a -> {
                    BulkProcessorParam bulkProcessorParam = new BulkProcessorParam();
                    BulkProcessor processor = doGetBulkProcessor(restHighLevelClient,bulkProcessorParam);
                    return processor;
                });
        return bulkProcessor;
    }
    
    /**
     *
     * @return
     */
    private static BulkProcessor doGetBulkProcessor(RestHighLevelClient restHighLevelClient,BulkProcessorParam param) {
        
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) ->
                        restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        
        int bulkActions = param.getBulkActions();
        ByteSizeValue bulkSize = new ByteSizeValue(param.getBulkSize(), ByteSizeUnit.MB);
        TimeValue flushInterval = new TimeValue(param.getFlushInterval());
        int concurrent = param.getConcurrent();
        int backoffPolicyRetryMax = param.getBackoffPolicyRetryMax();
        int backoffPolicyTime = param.getBackoffPolicyTime();
        BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        // 写入之前
                        List<DocWriteRequest<?>> requests = request.requests();
                        List<String> saves = new ArrayList<>();
                        for (DocWriteRequest<?> docWriteRequest : requests) {
                            String info = docWriteRequest.toString();
                            saves.add(info);
                        }
                        //下面这种方式看不出文档的执行顺序
                        //                        List<String> saves = new ArrayList<>();
                        //                        List<String> updates = new ArrayList<>();
                        //                        List<String> deletes = new ArrayList<>();
                        //                        for (DocWriteRequest<?> docWriteRequest : requests) {
                        //                            String info = docWriteRequest.toString();
                        //                            DocWriteRequest.OpType opType = docWriteRequest.opType();
                        //                            if (opType.equals(DocWriteRequest.OpType.INDEX) || opType.equals(
                        //                                    DocWriteRequest.OpType.CREATE)) {
                        //                                saves.add(info);
                        //                            }
                        //                            if (opType.equals(DocWriteRequest.OpType.UPDATE)) {
                        //                                updates.add(info);
                        //                            }
                        //                            if (opType.equals(DocWriteRequest.OpType.DELETE)) {
                        //                                deletes.add(info);
                        //                            }
                        //                        }
                        int num = request.numberOfActions();
                        String data = LogUtil.logSubstring(saves.toString());
                        log.info("ES BulkProcessor Begin  executionId:{} batchNum:{} "
                                        + "\n datas:{}"
                                        + "\nbulkActions:{} bulkSize:{} flushInterval:{} concurrent:{}",
                                executionId,num, data,bulkActions,bulkSize,flushInterval,concurrent);
                    }
                    
                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        // 写入之后执行
                        BulkItemResponse[] items = response.getItems();
                        
                        List<BulkItemResponse.Failure> failureList = Arrays.stream(items).map(BulkItemResponse::getFailure)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        
                        List<BulkItemResponse> successList = Arrays.stream(items)
                                .filter(a -> a.getFailure() == null && StringUtils.isBlank(a.getFailureMessage()))
                                .collect(Collectors.toList());
                        long ingestTookInMillis = response.getTook().getMillis();
                        String jsonStr = JsonUtils.toJsonStr(successList);
                        String res = LogUtil.logSubstring(jsonStr);
                        if (CollectionUtils.isEmpty(failureList)){
                            log.info("ES BulkProcessor Success executionId:{} timeCost:{} response:{} "
                                    ,executionId,ingestTookInMillis
                                    , res);
                        }else {
                            log.info("ES BulkProcessor Success executionId:{} timeCost:{} response:{} "
                                    ,executionId,ingestTookInMillis
                                    , res);
                            
                            log.error("ES BulkProcessor Fail executionId:{} timeCost:{} response:{}"
                                    ,executionId,ingestTookInMillis
                                    , failureList.toString());
                        }
                    }
                    
                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        List<DocWriteRequest<?>> requests = request.requests();
                        List<String> saves = new ArrayList<>();
                        for (DocWriteRequest<?> docWriteRequest : requests) {
                            String info = docWriteRequest.toString();
                            saves.add(info);
                        }
                        String data = LogUtil.logSubstring(saves.toString());
                        //写入失败后
                        log.error("ES BulkProcessor  executionId:{} "
                                        + "\n datas:{} \n ex:",executionId,
                                data,
                                failure);
                    }
                }).setBulkActions(bulkActions) //  达到刷新的条数
                .setBulkSize(bulkSize) // 达到 刷新的大小
                .setFlushInterval(flushInterval) // 固定刷新的时间频率
                .setConcurrentRequests(concurrent) //并发线程数
                //写入失败100毫秒后重试。最多3次
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(
                        TimeValue.timeValueMillis(backoffPolicyTime), backoffPolicyRetryMax)) // 重试补偿策略
                .build();
        return bulkProcessor;
    }
    
    
}
