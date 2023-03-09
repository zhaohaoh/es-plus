package com.es.plus.core.wrapper.chain;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.core.ScrollHandler;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.pojo.ClientContext;
import com.es.plus.pojo.EsAggsResponse;
import com.es.plus.pojo.EsResponse;
import com.es.plus.pojo.PageInfo;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;

public class EsChainQueryWrapper<T> extends AbstractEsChainWrapper<T, String, EsChainQueryWrapper<T>, EsQueryWrapper<T>> {
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient("master");

    public EsChainQueryWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias())? esIndexParam.getIndex():esIndexParam.getAlias();
        }
    }

    public EsChainQueryWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
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
        return esPlusClientFacade.searchPageByWrapper(new PageInfo<>(page, size), super.esWrapper.getEsParamWrapper(), tClass, index);
    }

    public EsAggsResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index, super.esWrapper.getEsParamWrapper(), tClass);
    }

    public long count() {
        return esPlusClientFacade.count(super.esWrapper.getEsParamWrapper(), index);
    }

    public void scroll(int size, ScrollHandler<T> scrollHandler) {
        esPlusClientFacade.scrollByWrapper(super.esWrapper.getEsParamWrapper(), tClass, index, size, 1, scrollHandler);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        esWrapper.getEsParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.searchByWrapper(esWrapper.getEsParamWrapper(), tClass, index);
    }

}
