package com.es.plus.pojo;

import lombok.Data;

import java.util.List;


@Data
public class PageInfo<T> {
    private long page = 1;
    private long size = 10;
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

    public PageInfo() {
    }
    public PageInfo(long page, long size) {
        this.page = page;
        this.size = size;
    }
    public int start() {
        return (int) ((int) (this.page - 1) * this.size);
    }

    public int end() {
        return (int) ((int) this.page * this.size);
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
