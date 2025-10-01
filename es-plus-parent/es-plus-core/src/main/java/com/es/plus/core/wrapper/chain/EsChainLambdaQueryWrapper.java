package com.es.plus.core.wrapper.chain;

import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.params.EsAggResponse;
import com.es.plus.common.params.EsQueryParamWrapper;
import com.es.plus.common.params.EsResponse;
import com.es.plus.common.properties.EsIndexParam;
import com.es.plus.common.tools.SFunction;
import com.es.plus.core.ClientContext;
import com.es.plus.core.IndexContext;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Duration;

import static com.es.plus.constant.EsConstant.MASTER;
import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

/**
 * @Author: hzh
 * @Date: 2023/2/6 17:08
 */
public class EsChainLambdaQueryWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainLambdaQueryWrapper<T>, EsLambdaQueryWrapper<T>> implements ChainQueryWrapper {
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);

    public EsChainLambdaQueryWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            index(ArrayUtils.isEmpty(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias());
            EsPlusClientFacade client = ClientContext.getClient(esIndexParam.getClientInstance());
            if (client!=null){
                esPlusClientFacade = client;
            }
        }
    }

    public EsChainLambdaQueryWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = IndexContext.getIndex(super.tClass);
        if (esIndexParam != null) {
            index(ArrayUtils.isEmpty(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias());
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    /**
     * 检索
     *
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search() {
        return esPlusClientFacade.search( type, esWrapper.esParamWrapper(),indexs);
    }

    /**
     * 检索
     *
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search(int size) {
        EsQueryParamWrapper esQueryParamWrapper = esWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setSize(size);
        return esPlusClientFacade.search( type, esWrapper.esParamWrapper(),indexs);
    }

    /**
     * 分页
     *
     * @param page 页面
     * @param size 大小
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> searchPage(int page, int size) {
        EsQueryParamWrapper esQueryParamWrapper = esWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setPage(page);
        esQueryParamWrapper.setSize(size);
        return esPlusClientFacade.search( type, esWrapper.esParamWrapper(),indexs);
    }

    /**
     * 聚合
     */
    @Override
    public EsAggResponse<T> aggregations() {
        return esPlusClientFacade.aggregations( type, esWrapper.esParamWrapper(),indexs);
    }

    /**
     * 统计
     *
     * @return long
     */
    @Override
    public long count() {
        return esPlusClientFacade.count( type, esWrapper.esParamWrapper(),indexs);
    }

    /**
     * 滚动查询
     *
     * @param size    大小
     * @param scollId scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> scroll(int size, String scollId) {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setSize(size);
        return esPlusClientFacade.scroll(type, esWrapper.esParamWrapper(), SCROLL_KEEP_TIME, scollId,indexs);
    }

    /**
     * 滚动查询
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> scroll(int size, Duration keepTime, String scollId) {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setSize(size);
        return esPlusClientFacade.scroll(type, esWrapper.esParamWrapper(), keepTime, scollId,indexs);
    }

    

    /**
     * 执行dsl
     *
     * @param dsl dsl
     * @return {@link String}
     */
    @Override
    public String executeDSL(String dsl) {
        return esPlusClientFacade.executeDSL(dsl, indexs);
    }
    
    @Override
    public String translateSQL(String sql) {
        return esPlusClientFacade.translateSQL(sql);
    }
    
    @Override
    public EsResponse<T> executeSQLep(String sql) {
        return esPlusClientFacade.executeSQL(sql,tClass);
    }
    
    @Override
    public String executeSQL(String sql) {
        return esPlusClientFacade.executeSQL(sql);
    }
    
    @Override
    public String sql2Dsl(String sql) {
        return esPlusClientFacade.sql2Dsl(sql);
    }
    
    @Override
    public String explainSQL(String sql) {
        return esPlusClientFacade.explain(sql);
    }

    /**
     * 转换为DSL字符串（不执行查询）
     *
     * @return DSL JSON字符串
     */
    @Override
    public String toDsl() {
        return esPlusClientFacade.toDsl(esWrapper.esParamWrapper(), indexs);
    }

}
