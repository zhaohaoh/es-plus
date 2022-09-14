package com.es.plus.core.chain;


import com.es.plus.core.tools.SFunction;
import com.es.plus.core.service.EsService;
import com.es.plus.core.wrapper.EsUpdateWrapper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> {
    private final EsService<T> esService;

    public EsChainUpdateWrapper(EsService<T> esService) {
        this.esService = esService;
        Type tClazz = ((ParameterizedType) esService.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        super.tClass = (Class<T>) tClazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
    }

    public boolean save(T t) {
        return esService.save(t);
    }

    public boolean update(T t) {
        return esService.updateById(t);
    }

    public BulkByScrollResponse incrementByWapper() {
        return esService.increment(esWrapper);
    }

    public List<BulkItemResponse> updateBatch(List<T> t) {
        return esService.updateBatch(t);
    }

    public List<BulkItemResponse> saveBatch(List<T> t) {
        return esService.saveBatch(t);
    }

    public BulkByScrollResponse update() {
        return esService.updateByWrapper(esWrapper);
    }

    public BulkByScrollResponse remove() {
        return esService.remove(esWrapper);
    }

    public EsChainUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt,sciptParams);
        return this;
    }

    public EsChainUpdateWrapper<T> set(String name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    public EsChainUpdateWrapper<T> set(SFunction<T, ?> name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    public EsChainUpdateWrapper<T> increment(String name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    public EsChainUpdateWrapper<T> increment(SFunction<T, ?> name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

}
