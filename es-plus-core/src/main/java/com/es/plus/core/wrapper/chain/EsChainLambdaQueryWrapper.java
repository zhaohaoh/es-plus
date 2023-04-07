package com.es.plus.core.wrapper.chain;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import com.es.plus.pojo.ClientContext;
import com.es.plus.pojo.EsAggsResponse;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;

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

    public EsResponse<T> list() {
        return esPlusClientFacade.searchByWrapper(esWrapper.getEsParamWrapper(), tClass, index);
    }

    public EsResponse<T> page(int page, int size) {
        return esPlusClientFacade.searchPageByWrapper(new PageInfo<>(page, size), esWrapper.getEsParamWrapper(), tClass, index);
    }

    public EsResponse<T> searchAfter(PageInfo<T> pageInfo) {
        return esPlusClientFacade.searchAfter(pageInfo, esWrapper.getEsParamWrapper(), tClass, index);
    }

    public EsAggsResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index, esWrapper.getEsParamWrapper(), tClass);
    }

    public long count() {
        return esPlusClientFacade.count(esWrapper.getEsParamWrapper(), index);
    }

    /**
     * 滚动查询
     *
     * @param size    大小
     * @param scollId scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> scroll(int size, String scollId ) {
     return    esPlusClientFacade.scrollByWrapper(esWrapper.getEsParamWrapper(), tClass, index, size, 5, scollId);
    }

    /**
     * 滚动查询
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> scroll(int size, int keepTime,String scollId) {
      return   esPlusClientFacade.scrollByWrapper(esWrapper.getEsParamWrapper(), tClass, index, size, keepTime, scollId);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        esWrapper.getEsParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.searchByWrapper(esWrapper.getEsParamWrapper(), tClass, index);
    }

}
