package com.es.plus.adapter.pojo.es;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义日期直方图间隔类，类似于DateHistogramInterval
 */
@Data
public class EpDateHistogramInterval {
    
    
    public static final EpDateHistogramInterval SECOND = new EpDateHistogramInterval("1s");
    public static final EpDateHistogramInterval MINUTE = new EpDateHistogramInterval("1m");
    public static final EpDateHistogramInterval HOUR = new EpDateHistogramInterval("1h");
    public static final EpDateHistogramInterval DAY = new EpDateHistogramInterval("1d");
    public static final EpDateHistogramInterval WEEK = new EpDateHistogramInterval("1w");
    public static final EpDateHistogramInterval MONTH = new EpDateHistogramInterval("1M");
    public static final EpDateHistogramInterval QUARTER = new EpDateHistogramInterval("1q");
    public static final EpDateHistogramInterval YEAR = new EpDateHistogramInterval("1y");
    
    private final String expression;

    /**
     * 创建指定秒数的间隔
     * @param sec 秒数
     * @return CustomDateHistogramInterval实例
     */
    public static EpDateHistogramInterval seconds(int sec) {
        return new EpDateHistogramInterval(sec + "s");
    }

    /**
     * 创建指定分钟数的间隔
     * @param min 分钟数
     * @return CustomDateHistogramInterval实例
     */
    public static EpDateHistogramInterval minutes(int min) {
        return new EpDateHistogramInterval(min + "m");
    }

    /**
     * 创建指定小时数的间隔
     * @param hours 小时数
     * @return CustomDateHistogramInterval实例
     */
    public static EpDateHistogramInterval hours(int hours) {
        return new EpDateHistogramInterval(hours + "h");
    }

    /**
     * 创建指定天数的间隔
     * @param days 天数
     * @return CustomDateHistogramInterval实例
     */
    public static EpDateHistogramInterval days(int days) {
        return new EpDateHistogramInterval(days + "d");
    }

    /**
     * 创建指定周数的间隔
     * @param weeks 周数
     * @return CustomDateHistogramInterval实例
     */
    public static EpDateHistogramInterval weeks(int weeks) {
        return new EpDateHistogramInterval(weeks + "w");
    }

    /**
     * 构造函数
     * @param expression 间隔表达式
     */
    public EpDateHistogramInterval(String expression) {
        this.expression = expression;
    }
 
}