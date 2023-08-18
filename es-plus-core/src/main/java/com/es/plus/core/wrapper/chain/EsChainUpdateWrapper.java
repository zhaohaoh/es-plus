package com.es.plus.core.wrapper.chain;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.core.ClientContext;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
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
public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, String, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> implements Update<EsChainUpdateWrapper<T>, String>, UpdateOperation<T> {

    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);

    public EsChainUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
            type = esIndexParam.getType();
        }
    }

    public EsChainUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
            type = esIndexParam.getType();
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    @Override
    public boolean save(T t) {
        return esPlusClientFacade.save(index, type, t);
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

    /**
     * 批处理更新
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveOrUpdateBatch(index, type, entityList);
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
    public List<BulkItemResponse> updateBatch(Collection<T> entityList) {
        return esPlusClientFacade.updateBatch(index, type, entityList);
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
     * 增量
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse incrementByWapper() {
        return esPlusClientFacade.increment(index, type, esWrapper.esParamWrapper());
    }


    @Override
    public boolean removeByIds(Collection<String> ids) {
        return esPlusClientFacade.deleteBatchByIds(index, type, ids);
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

    /**
     * 设置scipt
     *
     * @param scipt       scipt
     * @param sciptParams scipt参数
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> setScipt(String scipt, Map<String, Object> sciptParams) {
        esWrapper.setScipt(scipt, sciptParams);
        return this;
    }

    /**
     * 设置scipt
     *
     * @param condition   条件
     * @param script      脚本
     * @param sciptParams scipt参数
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> setScipt(boolean condition, String script, Map<String, Object> sciptParams) {
        esWrapper.setScipt(condition, script, sciptParams);
        return this;
    }

    /**
     * 设置
     *
     * @param name  名字
     * @param value 价值
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> set(String name, Object value) {
        esWrapper.set(name, value);
        return this;
    }

    /**
     * 设置
     *
     * @param condition 条件
     * @param column    列
     * @param val       瓦尔
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> set(boolean condition, String column, Object val) {
        esWrapper.set(condition, column, val);
        return this;
    }

    /**
     * 增量
     *
     * @param name  名字
     * @param value 价值
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> increment(String name, Long value) {
        esWrapper.increment(name, value);
        return this;
    }

    /**
     * 增量
     *
     * @param condition 条件
     * @param column    列
     * @param val       瓦尔
     * @return {@link EsChainUpdateWrapper}<{@link T}>
     */
    @Override
    public EsChainUpdateWrapper<T> increment(boolean condition, String column, Long val) {
        esWrapper.increment(condition, column, val);
        return this;
    }

}
