package com.es.plus.core.wrapper.chain;

import com.es.plus.core.ScrollHandler;
import com.es.plus.core.service.EsService;
import com.es.plus.core.tools.SFunction;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.pojo.EsAggregationsResponse;
import com.es.plus.core.wrapper.core.EsQueryWrapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EsChainQueryWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainQueryWrapper<T>, EsQueryWrapper<T>> {
    private EsService<T> esService;

    public EsChainQueryWrapper(EsService<T> esService) {
        this.esService = esService;
        Type tClazz = ((ParameterizedType) esService.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        super.tClass = (Class<T>) tClazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
    }

    public EsChainQueryWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
    }

    public EsResponse<T> list() {
        return esService.list(super.esWrapper);
    }

    public EsResponse<T> page(long page, long size) {
        return esService.page(new PageInfo<>(page, size), super.esWrapper);
    }

    public EsAggregationsResponse<T> aggregations() {
        return esService.aggregations(super.esWrapper);
    }

    public long count() {
        return esService.count(super.esWrapper);
    }

    public void scroll(int size, ScrollHandler<T> scrollHandler) {
        esService.scroll(super.esWrapper, size, 1, scrollHandler);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        return esService.profile(super.esWrapper);
    }

}
