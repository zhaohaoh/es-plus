package com.es.plus.core.wrapper.chain;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.core.ScrollHandler;
import com.es.plus.core.tools.SFunction;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import com.es.plus.pojo.ClientContext;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.pojo.EsAggsResponse;
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

    public EsResponse<T> page(long page, long size) {
        return esPlusClientFacade.searchPageByWrapper(new PageInfo<>(page, size), esWrapper.getEsParamWrapper(), tClass, index);
    }

    public EsAggsResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index, esWrapper.getEsParamWrapper(), tClass);
    }

    public long count() {
        return esPlusClientFacade.count(esWrapper.getEsParamWrapper(), index);
    }

    public void scroll(int size, ScrollHandler<T> scrollHandler) {
        esPlusClientFacade.scrollByWrapper(esWrapper.getEsParamWrapper(), tClass, index, size, 1, scrollHandler);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        esWrapper.getEsParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.searchByWrapper(esWrapper.getEsParamWrapper(), tClass, index);
    }

}
