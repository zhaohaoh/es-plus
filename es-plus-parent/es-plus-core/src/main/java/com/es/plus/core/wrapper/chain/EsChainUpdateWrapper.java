package com.es.plus.core.wrapper.chain;


import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.core.ClientContext;
import com.es.plus.core.IndexContext;
import com.es.plus.core.wrapper.core.EsUpdateWrapper;
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
 * @Date: 2022/9/22 20:24
 * 链式方法
 */
public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, String, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> implements Update<EsChainUpdateWrapper<T>, String,T>, UpdateOperation<T> {

    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);

    public EsChainUpdateWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            String preIndex = esIndexParam.getPreIndex();
            String[] index = esIndexParam.getIndex();
            
            index(index);
            type = esIndexParam.getType();
            EsPlusClientFacade client = ClientContext.getClient(esIndexParam.getClientInstance());
            if (client!=null){
                esPlusClientFacade = client;
            }
        }
    }

    public EsChainUpdateWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            index(esIndexParam.getIndex());
            type = esIndexParam.getType();
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    @Override
    public boolean save(T t) {
        return esPlusClientFacade.save(type, t,indexs);
    }

    /**
     * 保存批处理
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveBatch( type, entityList,indexs);
    }
    
    @Override
    public boolean saveOrUpdate(T entity) {
        return esPlusClientFacade.saveOrUpdate( type, entity,indexs);
    }
    
    /**
     * 批处理更新
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> saveOrUpdateBatch(Collection<T> entityList) {
        return esPlusClientFacade.saveOrUpdateBatch( type, entityList,indexs);
    }
    
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void saveBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.saveBatchAsyncProcessor( type, entityList,indexs);
    }
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void saveOrUpdateBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.saveOrUpdateBatchAsyncProcessor( type, entityList,indexs);
    }
    /**
     * 保存批处理异步
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public void updateBatchAsyncProcessor(Collection<T> entityList) {
        esPlusClientFacade.updateBatchAsyncProcessor( type, entityList,indexs);
    }

    @Override
    public boolean update(T t) {
        return esPlusClientFacade.update( type, t,indexs);
    }

    /**
     * 批处理更新
     *
     * @return {@link List}<{@link BulkItemResponse}>
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList) {
        return esPlusClientFacade.updateBatch(type, entityList,indexs);
    }


    /**
     * 更新
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse updateByQuery() {
        return esPlusClientFacade.updateByWrapper(type, esWrapper.esParamWrapper(),indexs);
    }


    /**
     * 增量
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse incrementByWapper() {
        return esPlusClientFacade.increment( type, esWrapper.esParamWrapper(),indexs);
    }


    @Override
    public boolean removeByIds(Collection<String> ids) {
        return esPlusClientFacade.deleteBatchByIds(type, ids,indexs);
    }


    /**
     * 删除
     *
     * @return {@link BulkByScrollResponse}
     */
    @Override
    public BulkByScrollResponse remove() {
        return esPlusClientFacade.deleteByQuery(type, esWrapper.esParamWrapper(),indexs);
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
    
    @Override
    public EsChainUpdateWrapper<T> setEntity(boolean condition, T entity) {
        if (condition) {
            esWrapper.setEntity(condition,entity);
        }
        return this;
    }
    
    @Override
    public EsChainUpdateWrapper<T> setEntity(T entity) {
        return Update.super.setEntity(entity);
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
