package com.es.plus.constant;


public interface EsSettingsConstants {

    String QUERY_DEBUG = "search.slowlog.threshold.query.debug";

    String QUERY_WARN = "search.slowlog.threshold.query.warn";

    String QUERY_INFO = "search.slowlog.threshold.query.info";

    String QUERY_TRACE = "search.slowlog.threshold.query.trace";
    /**
     * 根据文档id拉取数据慢查询日志
     */
    String FETCH_WARN = "search.slowlog.threshold.fetch.warn";

    String FETCH_INFO = "search.slowlog.threshold.fetch.info";

    String FETCH_DEBUG = "search.slowlog.threshold.fetch.debug";

    String FETCH_TRACE = "search.slowlog.threshold.fetch.trace";
    //日志级别
    String SEARCH_LEVEL = "index.search.slowlog.level";

    //索引日志
    String INDEX_WARN = "indexing.slowlog.threshold.index.warn";

    String INDEX_INFO = "indexing.slowlog.threshold.index.info";

    String INDEX_DEBUG = "indexing.slowlog.threshold.index.debug";

    String INDEX_TRACE = "indexing.slowlog.threshold.index.trace";

    String INDEX_LEVEL = "index.indexing.slowlog.level";

    String INDEX_SOURCE = "index.indexing.slowlog.source";

}


