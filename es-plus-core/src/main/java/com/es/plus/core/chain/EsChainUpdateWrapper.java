package com.es.plus.core.chain;


import com.es.plus.core.service.EsService;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.EsUpdateWrapper;
import com.es.plus.core.wrapper.Update;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
/**
 * @Author: hzh
 * @Date: 2022/9/22 20:24
 * 链式方法
 */
public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> implements Update<EsChainUpdateWrapper<T>, SFunction<T, ?>> {
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

    @Override
    public EsChainUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt, sciptParams);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> setScipt(boolean condition, String script, Map<String, Object> sciptParams) {
        return null;
    }

    @Override
    public EsChainUpdateWrapper<T> set(String name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> set(SFunction<T, ?> name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        esWrapper.set(condition, column, val);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> increment(String name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> increment(SFunction<T, ?> name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> increment(boolean condition, SFunction<T, ?> column, Long val) {
        esWrapper.increment(condition, column, val);
        return this;
    }

}
