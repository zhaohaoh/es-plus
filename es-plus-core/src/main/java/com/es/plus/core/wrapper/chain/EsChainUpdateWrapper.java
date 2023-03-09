package com.es.plus.core.wrapper.chain;


import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
import com.es.plus.core.wrapper.core.Update;
import com.es.plus.pojo.ClientContext;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: hzh
 * @Date: 2022/9/22 20:24
 * 链式方法
 */
public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, String, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> implements Update<EsChainUpdateWrapper<T>, String> {

    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient("master");

    public EsChainUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
    }

    public EsChainUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    public boolean save(T t) {
        return esPlusClientFacade.save(index, t);
    }

    public boolean update(T t) {
        return esPlusClientFacade.update(index, t);
    }

    public BulkByScrollResponse incrementByWapper() {
        return esPlusClientFacade.increment(index, esWrapper.getEsParamWrapper());
    }

    public List<BulkItemResponse> updateBatch(Collection<T> t) {
        return esPlusClientFacade.updateBatch(index, t);
    }

    public List<BulkItemResponse> saveBatch(Collection<T> t) {
        return esPlusClientFacade.saveBatch(index, t);
    }

    public BulkByScrollResponse update() {
        return esPlusClientFacade.updateByWrapper(index, esWrapper.getEsParamWrapper());
    }

    public BulkByScrollResponse remove() {
        return esPlusClientFacade.deleteByQuery(index, esWrapper.getEsParamWrapper());
    }

    @Override
    public EsChainUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt, sciptParams);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> setScipt(boolean condition, String script, Map<String, Object> sciptParams) {
        esWrapper.setScipt(condition,script, sciptParams);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> set(String name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> set(boolean condition, String column, Object val) {
        esWrapper.set(condition, column, val);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> increment(String name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    @Override
    public EsChainUpdateWrapper<T> increment(boolean condition, String column, Long val) {
        esWrapper.increment(condition, column, val);
        return this;
    }


}
