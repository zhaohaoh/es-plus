package com.es.plus.core.wrapper.chain;

import com.es.plus.es6.client.EsPlusAggregations;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.PageInfo;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.tools.SFunction;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import com.es.plus.core.ClientContext;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

/**
 * @Author: hzh
 * @Date: 2023/2/6 17:08
 */
public class EsChainLambdaQueryWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainLambdaQueryWrapper<T>, EsLambdaQueryWrapper<T>> {
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient("master");

    public EsChainLambdaQueryWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
    }

    public EsChainLambdaQueryWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsLambdaQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    /**
     * 列表
     *
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> list() {
        return esPlusClientFacade.searchByWrapper(esWrapper.esParamWrapper(), tClass, index);
    }

    /**
     * 分页
     *
     * @param page 页面
     * @param size 大小
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> page(int page, int size) {
        return esPlusClientFacade.searchPageByWrapper(new PageInfo<>(page, size), esWrapper.esParamWrapper(), tClass, index);
    }

    /**
     * 搜索后
     *
     * @param pageInfo 页面信息
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> searchAfter(PageInfo<T> pageInfo) {
        return esPlusClientFacade.searchAfter(pageInfo, esWrapper.esParamWrapper(), tClass, index);
    }

    /**
     * 聚合
     *
     * @return {@link EsPlusAggregations}<{@link T}>
     */
    public EsAggResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index, esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 统计
     *
     * @return long
     */
    public long count() {
        return esPlusClientFacade.count(esWrapper.esParamWrapper(), index);
    }

    /**
     * 滚动查询
     *
     * @param size    大小
     * @param scollId scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> scroll(int size, String scollId ) {
     return    esPlusClientFacade.scrollByWrapper(esWrapper.esParamWrapper(), tClass, index, size, SCROLL_KEEP_TIME, scollId);
    }

    /**
     * 滚动查询
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> scroll(int size, Duration keepTime, String scollId) {
      return   esPlusClientFacade.scrollByWrapper(esWrapper.esParamWrapper(), tClass, index, size, keepTime, scollId);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.searchByWrapper(esWrapper.esParamWrapper(), tClass, index);
    }

}