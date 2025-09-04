package com.es.plus.common.pojo.es;

import java.util.Arrays;

/**
 * 自定义FetchSourceContext，用于替代Elasticsearch的FetchSourceContext
 * 控制在搜索结果中返回哪些字段
 */
public class EpFetchSourceContext {
    
    public static final EpFetchSourceContext FETCH_SOURCE = new EpFetchSourceContext(true);
    public static final EpFetchSourceContext DO_NOT_FETCH_SOURCE = new EpFetchSourceContext(false);

    private final boolean fetchSource;
    private final String[] includes;
    private final String[] excludes;

    /**
     * 构造函数
     * @param fetchSource 是否获取_source字段
     */
    public EpFetchSourceContext(boolean fetchSource) {
        this(fetchSource, new String[0], new String[0]);
    }

    /**
     * 构造函数
     * @param fetchSource 是否获取_source字段
     * @param includes 包含的字段数组
     * @param excludes 排除的字段数组
     */
    public EpFetchSourceContext(boolean fetchSource, String[] includes, String[] excludes) {
        this.fetchSource = fetchSource;
        this.includes = includes != null ? includes : new String[0];
        this.excludes = excludes != null ? excludes : new String[0];
    }

    /**
     * 是否获取_source字段
     * @return 是否获取_source字段
     */
    public boolean fetchSource() {
        return fetchSource;
    }

    /**
     * 获取包含的字段数组
     * @return 包含的字段数组
     */
    public String[] includes() {
        return includes;
    }

    /**
     * 获取排除的字段数组
     * @return 排除的字段数组
     */
    public String[] excludes() {
        return excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpFetchSourceContext that = (EpFetchSourceContext) o;

        if (fetchSource != that.fetchSource) return false;
        if (!Arrays.equals(includes, that.includes)) return false;
        return Arrays.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        int result = (fetchSource ? 1 : 0);
        result = 31 * result + Arrays.hashCode(includes);
        result = 31 * result + Arrays.hashCode(excludes);
        return result;
    }

    @Override
    public String toString() {
        return "EpFetchSourceContext{" +
                "fetchSource=" + fetchSource +
                ", includes=" + Arrays.toString(includes) +
                ", excludes=" + Arrays.toString(excludes) +
                '}';
    }
}
