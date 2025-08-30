package com.es.plus.adapter.pojo.es;

import java.util.List;

/**
 * 内部排序实现类
 */
class EpInternalOrder {
    
    /**
     * 基础排序类
     */
    abstract static class Order extends EpBucketOrder {
        protected final boolean asc;

        Order(boolean asc) {
            this.asc = asc;
        }

        @Override
        public boolean asc() {
            return asc;
        }
    }

    /**
     * 按数量排序
     */
    static class CountOrder extends Order {
        CountOrder(boolean asc) {
            super(asc);
        }

        @Override
        byte id() {
            return 1;
        }
    }

    /**
     * 按key排序
     */
    static class KeyOrder extends Order {
        KeyOrder(boolean asc) {
            super(asc);
        }

        @Override
        byte id() {
            return 2;
        }
    }

    /**
     * 按聚合结果排序
     */
    static class Aggregation extends Order {
        private final String path;

        Aggregation(String path, boolean asc) {
            super(asc);
            this.path = path;
        }

        @Override
        byte id() {
            return 3;
        }

        @Override
        public String getPath() {
            return path;
        }
    }

    /**
     * 复合排序
     */
    static class CompoundOrder extends EpBucketOrder {
        private final List<EpBucketOrder> orderList;

        CompoundOrder(List<EpBucketOrder> orderList) {
            this.orderList = orderList;
        }

        @Override
        byte id() {
            return 4;
        }

        @Override
        public boolean asc() {
            return true;
        }

        @Override
        public List<EpBucketOrder> getOrderList() {
            return orderList;
        }
    }
}
