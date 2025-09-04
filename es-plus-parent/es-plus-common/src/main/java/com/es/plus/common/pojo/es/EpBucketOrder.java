package com.es.plus.common.pojo.es;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义BucketOrder，用于替代Elasticsearch的BucketOrder
 */
public abstract class EpBucketOrder {
    
    /**
     * 按照桶的数量排序
     * @param asc 是否升序
     * @return BucketOrder实例
     */
    public static EpBucketOrder count(boolean asc) {
        return new EpInternalOrder.CountOrder(asc);
    }

    /**
     * 按照桶的key排序
     * @param asc 是否升序
     * @return BucketOrder实例
     */
    public static EpBucketOrder key(boolean asc) {
        return new EpInternalOrder.KeyOrder(asc);
    }

    /**
     * 按照聚合结果排序
     * @param path 聚合路径
     * @param asc 是否升序
     * @return BucketOrder实例
     */
    public static EpBucketOrder aggregation(String path, boolean asc) {
        return new EpInternalOrder.Aggregation(path, asc);
    }

    /**
     * 按照聚合结果排序
     * @param path 聚合路径
     * @param metricName 指标名称
     * @param asc 是否升序
     * @return BucketOrder实例
     */
    public static EpBucketOrder aggregation(String path, String metricName, boolean asc) {
        return new EpInternalOrder.Aggregation(path + "." + metricName, asc);
    }

    /**
     * 复合排序
     * @param orders 排序列表
     * @return BucketOrder实例
     */
    public static EpBucketOrder compound(List<EpBucketOrder> orders) {
        return new EpInternalOrder.CompoundOrder(orders);
    }

    /**
     * 复合排序
     * @param orders 排序数组
     * @return BucketOrder实例
     */
    public static EpBucketOrder compound(EpBucketOrder... orders) {
        return compound(Arrays.asList(orders));
    }

    /**
     * 获取排序类型ID
     * @return 排序类型ID
     */
    abstract byte id();

    /**
     * 是否为升序
     * @return 是否为升序
     */
    public abstract boolean asc();
    
    /**
     * 获取聚合路径
     * @return 聚合路径
     */
    public String getPath() {
        return null;
    }
    
    /**
     * 获取复合排序列表
     * @return 复合排序列表
     */
    public List<EpBucketOrder> getOrderList() {
        return null;
    }
}
