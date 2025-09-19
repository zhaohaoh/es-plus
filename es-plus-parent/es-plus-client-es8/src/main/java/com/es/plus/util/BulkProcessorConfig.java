package com.es.plus.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.es.plus.annotation.BulkProcessor;
import com.es.plus.common.params.BulkProcessorParam;
import com.es.plus.common.util.JsonUtils;
import com.es.plus.common.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ES8批量处理器配置
 *
 * @Author: hzh
 * @Date: 2024/09/19
 * ES8使用BulkIngester替代BulkProcessor进行批量操作
 * 支持异步批量写入，自动分批次处理
 */
@Slf4j
public class BulkProcessorConfig {

    private static final Map<String, BulkIngester<Void>> bulkIngesterMap = new ConcurrentHashMap<>();

    public static BulkIngester<Void> getBulkProcessor(Object elasticsearchClient, String index) {
        BulkIngester<Void> bulkIngester = bulkIngesterMap.computeIfAbsent(index,
                a -> {
                    BulkProcessorParam bulkProcessorParam = new BulkProcessorParam();
                    return doGetBulkIngester((ElasticsearchClient) elasticsearchClient, bulkProcessorParam);
                });
        return bulkIngester;
    }
    public static BulkIngester getBulkProcessor(Object elasticsearchClient,BulkProcessorParam param,String index) {
        BulkIngester bulkProcessor = bulkIngesterMap.computeIfAbsent(index,
                a -> {
                    BulkIngester<Void> processor = doGetBulkIngester((ElasticsearchClient) elasticsearchClient,param);
                    return processor;
                });
        return bulkProcessor;
    }

    /**
     * 创建ES8的BulkIngester
     *
     * @param elasticsearchClient ES8客户端
     * @param param 批量处理参数
     * @return BulkIngester实例
     */
    public static BulkIngester<Void> doGetBulkIngester(ElasticsearchClient elasticsearchClient, BulkProcessorParam param) {

        int bulkActions = param.getBulkActions();
        long bulkSize = param.getBulkSize() * 1024 * 1024; // 转换为字节
        Duration flushInterval = Duration.ofSeconds(param.getFlushInterval());
        int concurrent = param.getConcurrent();
        int backoffPolicyRetryMax = param.getBackoffPolicyRetryMax();
        long backoffPolicyTime = param.getBackoffPolicyTime();

        return BulkIngester.of(builder -> builder
                .client(elasticsearchClient)
                .maxOperations(bulkActions)
                .maxSize(bulkSize)
                .flushInterval(flushInterval.toMinutes(), TimeUnit.MINUTES)
                .maxConcurrentRequests(concurrent)
                .listener(new BulkListener<Void>() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request, List<Void> contexts) {
                        // 写入之前
                        List<BulkOperation> operations = request.operations();
                        List<String> saves = new ArrayList<>();
                        for (BulkOperation operation : operations) {
                            String info = operation.toString();
                            saves.add(info);
                        }

                        int num = operations.size();
                        String data = LogUtil.logSubstring(saves.toString());
                        log.info("ES BulkIngester Begin  executionId:{} batchNum:{} "
                                        + "\n datas:{}"
                                        + "\nbulkActions:{} bulkSize:{} flushInterval:{} concurrent:{}",
                                executionId, num, data, bulkActions, bulkSize, flushInterval, concurrent);
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, List<Void> contexts, BulkResponse response) {
                        // 写入之后执行
                        List<BulkResponseItem> items = response.items();

                        List<BulkResponseItem> failureList = items.stream()
                                .filter(item -> item.error() != null)
                                .collect(Collectors.toList());

                        List<BulkResponseItem> successList = items.stream()
                                .filter(item -> item.error() == null)
                                .collect(Collectors.toList());

                        long ingestTookInMillis = response.took();
                        String jsonStr = JsonUtils.toJsonStr(successList);
                        String res = LogUtil.logSubstring(jsonStr);

                        if (CollectionUtils.isEmpty(failureList)) {
                            log.info("ES BulkIngester Success executionId:{} timeCost:{} response:{}",
                                    executionId, ingestTookInMillis, res);
                        } else {
                            log.info("ES BulkIngester Success executionId:{} timeCost:{} response:{}",
                                    executionId, ingestTookInMillis, res);

                            String failureMessages = failureList.stream()
                                    .map(item -> item.error() != null ? item.error().toString() : "")
                                    .filter(msg -> !msg.isEmpty())
                                    .collect(Collectors.joining(", "));

                            log.error("ES BulkIngester Fail executionId:{} timeCost:{} failures:{}",
                                    executionId, ingestTookInMillis, failureMessages);
                        }
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, List<Void> contexts, Throwable failure) {
                        List<BulkOperation> operations = request.operations();
                        List<String> saves = new ArrayList<>();
                        for (BulkOperation operation : operations) {
                            String info = operation.toString();
                            saves.add(info);
                        }
                        String data = LogUtil.logSubstring(saves.toString());
                        //写入失败后
                        log.error("ES BulkIngester executionId:{} "
                                + "\n datas:{} \n ex:", executionId, data, failure);
                    }
                }));
    }

    /**
     * 关闭指定索引的BulkIngester
     *
     * @param index 索引名称
     */
    public static void closeBulkIngester(String index) {
        BulkIngester<Void> bulkIngester = bulkIngesterMap.remove(index);
        if (bulkIngester != null) {
            try {
                bulkIngester.close();
                log.info("BulkIngester for index {} closed successfully", index);
            } catch (Exception e) {
                log.error("Error closing BulkIngester for index {}", index, e);
            }
        }
    }

    /**
     * 关闭所有BulkIngester
     */
    public static void closeAllBulkIngesters() {
        for (Map.Entry<String, BulkIngester<Void>> entry : bulkIngesterMap.entrySet()) {
            try {
                entry.getValue().close();
                log.info("BulkIngester for index {} closed successfully", entry.getKey());
            } catch (Exception e) {
                log.error("Error closing BulkIngester for index {}", entry.getKey(), e);
            }
        }
        bulkIngesterMap.clear();
    }

    /**
     * 获取当前活跃的BulkIngester数量
     *
     * @return 活跃的BulkIngester数量
     */
    public static int getActiveBulkIngesterCount() {
        return bulkIngesterMap.size();
    }

    /**
     * 检查指定索引的BulkIngester是否存在
     *
     * @param index 索引名称
     * @return 是否存在
     */
    public static boolean hasBulkIngester(String index) {
        return bulkIngesterMap.containsKey(index);
    }
}