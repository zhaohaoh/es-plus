package com.es.plus.adapter.params;

import lombok.Data;

import java.util.List;


@Data
public class PageInfo<T> {
    private int page = 1;
    private int size = 10;
    private T query;
    //缓存索引起步是0
    private long cacheBeginIndex;
    private long cacheEndIndex;
    //排序字段
    private List<EsOrder> orders;

    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;

    private String keyword;

    private Object[] searchAfterValues;

    public PageInfo() {
    }
    public PageInfo(int page, int size) {
        this.page = page;
        this.size = size;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "page=" + page +
                ", size=" + size +
                ", query=" + query +
                ", cacheBeginIndex=" + cacheBeginIndex +
                ", cacheEndIndex=" + cacheEndIndex +
                ", orders=" + orders +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
