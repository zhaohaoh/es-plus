package com.es.plus.core.wrapper.chain;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.core.ClientContext;
import com.es.plus.core.IndexContext;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;

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
            index(StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias());
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
            index(StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias());
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
     * 性能分析
     */
    @Override
    public EsResponse<T> profile() {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.search( type, esWrapper.esParamWrapper(),indexs);
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
    public EsResponse<T> executeSQL(String sql) {
        return esPlusClientFacade.executeSQL(sql,tClass);
    }
    
}
