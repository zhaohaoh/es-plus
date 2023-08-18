package com.es.plus.core.wrapper.chain;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.core.ClientContext;
import com.es.plus.core.wrapper.core.EsLambdaUpdateWrapper;
import com.es.plus.core.wrapper.core.Update;
import com.es.plus.core.wrapper.core.UpdateOperation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.es.plus.constant.EsConstant.MASTER;

/**
 * @Author: hzh
 * @Date: 2022/9/22 20:24
 * 链式方法
 */
public class EsChainLambdaUpdateWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainLambdaUpdateWrapper<T>, EsLambdaUpdateWrapper<T>> implements Update<EsChainLambdaUpdateWrapper<T>, SFunction<T, ?>>, UpdateOperation<T> {


    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);

    public EsChainLambdaUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
            type = esIndexParam.getType();
        }
    }

    public EsChainLambdaUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
            type = esIndexParam.getType();
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    /**
     * 保存
     *
     * @param t t
     * @return boolean
     */
    @Override
    public boolean save(T t) {
        return esPlusClientFacade.save(index, type, t);
    }


    /**
     * 增量根据
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse incrementByWapper() {
        return esPlusClientFacade.increment(index, type, esWrapper.esParamWrapper());
    }


    /**
     * 保存批处理
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveBatch(index, type, entityList);
    }

    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveBatch(index, type, entityList);
    }

    @Override
    public boolean update(T t) {
        return esPlusClientFacade.update(index, type, t);
    }


    /**
     * 批处理更新
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> t) {
        return esPlusClientFacade.updateBatch(index, type, t);
    }

    /**
     * 更新
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByQuery() {
        return esPlusClientFacade.updateByWrapper(index, type, esWrapper.esParamWrapper());
    }

    /**
     * 删除
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove() {
        return esPlusClientFacade.deleteByQuery(index, type, esWrapper.esParamWrapper());
    }

    @Override
    public boolean removeByIds(Collection<String> ids) {
        return esPlusClientFacade.deleteBatchByIds(index, type, ids);
    }

    /**
     * 设置scipt
     *
     * @param scipt       scipt
     * @param sciptParams scipt参数
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt, sciptParams);
        return this;
    }

    /**
     * 设置scipt
     *
     * @param condition   条件
     * @param script      脚本
     * @param sciptParams scipt参数
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> setScipt(boolean condition, String script, Map<String, Object> sciptParams) {
        esWrapper.setScipt(condition, script, sciptParams);
        return this;
    }


    /**
     * 设置
     *
     * @param name  名字
     * @param value 价值
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> set(SFunction<T, ?> name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    /**
     * 设置
     *
     * @param condition 条件
     * @param column    列
     * @param val       瓦尔
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        esWrapper.set(condition, column, val);
        return this;
    }


    /**
     * 增量
     *
     * @param name  名字
     * @param value 价值
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> increment(SFunction<T, ?> name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    /**
     * 增量
     *
     * @param condition 条件
     * @param column    列
     * @param val       瓦尔
     * @return {@link EsChainLambdaUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainLambdaUpdateWrapper<T> increment(boolean condition, SFunction<T, ?> column, Long val) {
        esWrapper.increment(condition, column, val);
        return this;
    }

}
