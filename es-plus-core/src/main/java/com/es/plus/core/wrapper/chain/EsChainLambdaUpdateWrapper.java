package com.es.plus.core.wrapper.chain;


import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.core.EsLambdaUpdateWrapper;
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
public class EsChainLambdaUpdateWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainLambdaUpdateWrapper<T>, EsLambdaUpdateWrapper<T>> implements Update<EsChainLambdaUpdateWrapper<T>, SFunction<T, ?>> {


    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient("master");

    public EsChainLambdaUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
    }

    public EsChainLambdaUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
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
    public EsChainLambdaUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt, sciptParams);
        return this;
    }

    @Override
    public EsChainLambdaUpdateWrapper<T> setScipt(boolean condition, String script, Map<String, Object> sciptParams) {
        return null;
    }


    @Override
    public EsChainLambdaUpdateWrapper<T> set(SFunction<T, ?> name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    @Override
    public EsChainLambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        esWrapper.set(condition, column, val);
        return this;
    }


    @Override
    public EsChainLambdaUpdateWrapper<T> increment(SFunction<T, ?> name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    @Override
    public EsChainLambdaUpdateWrapper<T> increment(boolean condition, SFunction<T, ?> column, Long val) {
        esWrapper.increment(condition, column, val);
        return this;
    }

}
