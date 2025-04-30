package com.es.plus.core.wrapper.chain;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.core.ClientContext;
import com.es.plus.core.IndexContext;
import com.es.plus.core.wrapper.core.EsLambdaUpdateWrapper;
import com.es.plus.core.wrapper.core.Update;
import com.es.plus.core.wrapper.core.UpdateOperation;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.es.plus.constant.EsConstant.MASTER;

/**
 * @Author: hzh
 * @Date: 2022/9/22 20:24 链式方法
 */
public class EsChainLambdaUpdateWrapper<T>
        extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainLambdaUpdateWrapper<T>, EsLambdaUpdateWrapper<T>>
        implements Update<EsChainLambdaUpdateWrapper<T>, SFunction<T, ?>, T>, UpdateOperation<T> {
    
    
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);
    
    public EsChainLambdaUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            index(esIndexParam.getIndex());
            type = esIndexParam.getType();
            EsPlusClientFacade client = ClientContext.getClient(esIndexParam.getClientInstance());
            if (client != null) {
                esPlusClientFacade = client;
            }
        }
    }
    
    public EsChainLambdaUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            index(esIndexParam.getIndex());
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
        return esPlusClientFacade.save(type, t, indexs);
    }
    
    @Override
    public boolean saveOrUpdate(T t) {
        return esPlusClientFacade.save(type, t, indexs);
    }
    
    
    /**
     * 增量根据
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse incrementByWapper() {
        return esPlusClientFacade.increment(type, esWrapper.esParamWrapper(), indexs);
    }
    
    
    /**
     * 保存批处理
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveBatch(type, entityList, indexs);
    }
    
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveBatch(type, entityList, indexs);
    }
    
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void saveBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.saveBatchAsyncProcessor(type, entityList, indexs);
    }
    
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.saveOrUpdateBatchAsyncProcessor(type, entityList, indexs);
    }
    
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void updateBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.updateBatchAsyncProcessor(type, entityList, indexs);
    }
    
    
    @Override
    public boolean update(T t) {
        return esPlusClientFacade.update(type, t, indexs);
    }
    
    
    /**
     * 批处理更新
     *
     * @param t t
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> t) {
        return esPlusClientFacade.updateBatch(type, t, indexs);
    }
    
    /**
     * 更新
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByQuery() {
        return esPlusClientFacade.updateByWrapper(type, esWrapper.esParamWrapper(), indexs);
    }
    
    /**
     * 删除
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove() {
        return esPlusClientFacade.deleteByQuery(type, esWrapper.esParamWrapper(), indexs);
    }
    
    @Override
    public boolean removeByIds(Collection<String> ids) {
        return esPlusClientFacade.deleteBatchByIds(type, ids, indexs);
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
    
    @Override
    public EsChainLambdaUpdateWrapper<T> setEntity(boolean condition, T entity) {
        esWrapper.setEntity(condition, entity);
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
