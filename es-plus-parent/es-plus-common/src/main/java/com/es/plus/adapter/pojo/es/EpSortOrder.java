package com.es.plus.adapter.pojo.es;

/**
 * 自定义排序顺序枚举
 */
public enum EpSortOrder {
    ASC {
        @Override
        public String toString() {
            return "asc";
        }
    },
    DESC {
        @Override
        public String toString() {
            return "desc";
        }
    };

    public static EpSortOrder fromString(String order) {
        if (order == null) {
            return ASC;
        }
        switch (order.toLowerCase()) {
            case "asc":
                return ASC;
            case "desc":
                return DESC;
            default:
                return ASC;
        }
    }
}