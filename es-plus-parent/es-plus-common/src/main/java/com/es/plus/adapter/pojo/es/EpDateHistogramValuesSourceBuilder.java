package com.es.plus.adapter.pojo.es;

/**
 * Date histogram类型的复合值源构建器
 */
public class EpDateHistogramValuesSourceBuilder extends EpCompositeValuesSourceBuilder<EpDateHistogramValuesSourceBuilder> {
    private String interval;
    private String timeZone;

    public EpDateHistogramValuesSourceBuilder(String name) {
        super(name);
    }
    
    public EpDateHistogramValuesSourceBuilder interval(String interval) {
        this.interval = interval;
        return this;
    }
    
    public String interval() {
        return this.interval;
    }
    
    public EpDateHistogramValuesSourceBuilder timeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }
    
    public String timeZone() {
        return this.timeZone;
    }
    
    @Override
    String type() {
        return "date_histogram";
    }
}
