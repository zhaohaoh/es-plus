package com.es.plus.adapter.pojo.es;

import java.util.Objects;

/**
 * 自定义复合值源构建器，用于替代Elasticsearch的CompositeValuesSourceBuilder
 */
public abstract class EpCompositeValuesSourceBuilder<AB extends EpCompositeValuesSourceBuilder<AB>> {
    protected final String name;
    private String field;
    private EpScript script;
    private String valueType;
    private boolean missingBucket;
    private String order;
    private String format;

    EpCompositeValuesSourceBuilder(String name) {
        this(name, null);
    }

    EpCompositeValuesSourceBuilder(String name, String valueType) {
        this.field = null;
        this.script = null;
        this.valueType = null;
        this.missingBucket = false;
        this.order = "asc";
        this.format = null;
        this.name = name;
        this.valueType = valueType;
    }

    public String name() {
        return this.name;
    }

    abstract String type();

    public AB field(String field) {
        if (field == null) {
            throw new IllegalArgumentException("[field] must not be null");
        } else {
            this.field = field;
            return (AB)this;
        }
    }

    public String field() {
        return this.field;
    }

    public AB script(EpScript script) {
        if (script == null) {
            throw new IllegalArgumentException("[script] must not be null");
        } else {
            this.script = script;
            return (AB)this;
        }
    }

    public EpScript script() {
        return this.script;
    }

    public AB valueType(String valueType) {
        if (valueType == null) {
            throw new IllegalArgumentException("[valueType] must not be null");
        } else {
            this.valueType = valueType;
            return (AB)this;
        }
    }

    public String valueType() {
        return this.valueType;
    }

    public AB missingBucket(boolean missingBucket) {
        this.missingBucket = missingBucket;
        return (AB)this;
    }

    public boolean missingBucket() {
        return this.missingBucket;
    }

    public AB order(String order) {
        if (order == null) {
            throw new IllegalArgumentException("[order] must not be null");
        } else {
            this.order = order;
            return (AB)this;
        }
    }

    public AB order(EpSortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("[order] must not be null");
        } else {
            this.order = order.toString();
            return (AB)this;
        }
    }

    public String order() {
        return this.order;
    }

    public AB format(String format) {
        if (format == null) {
            throw new IllegalArgumentException("[format] must not be null: [" + this.name + "]");
        } else {
            this.format = format;
            return (AB)this;
        }
    }

    public String format() {
        return this.format;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, missingBucket, script, valueType, order, format);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            AB that = (AB)(o);
            return Objects.equals(this.field, that.field()) && 
                   Objects.equals(this.script, that.script()) && 
                   Objects.equals(this.valueType, that.valueType()) && 
                   Objects.equals(this.missingBucket, that.missingBucket()) && 
                   Objects.equals(this.order, that.order()) && 
                   Objects.equals(this.format, that.format());
        } else {
            return false;
        }
    }
}
