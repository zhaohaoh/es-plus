package com.es.plus.core.wrapper.chain;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.PageInfo;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.EsParamHolder;
import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.core.ClientContext;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

/**
 * es链查询包装器
 *
 * @author hzh
 * @date 2023/04/07
 */
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

    /**
     * 列表
     *
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> list() {
        return esPlusClientFacade.searchByWrapper(index,type,esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 分页
     *
     * @param page 页面
     * @param size 大小
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> page(int page, int size) {
        return esPlusClientFacade.searchPageByWrapper(index,type,new PageInfo<>(page, size), super.esWrapper.esParamWrapper(), tClass );
    }

    /**
     * 搜索后
     *
     * @param pageInfo 页面信息
     * @return {@link EsResponse}<{@link T}>
     */
    public EsResponse<T> searchAfter(PageInfo<T> pageInfo) {
        return esPlusClientFacade.searchAfter(index,type,pageInfo, super.esWrapper.esParamWrapper(), tClass );
    }

    /**
     * 聚合
     *
     */
    public EsAggResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index,type, super.esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 统计
     *
     * @return long
     */
    public long count() {
        return esPlusClientFacade.count(index,type,super.esWrapper.esParamWrapper());
    }

    /**
     * 滚动
     *
     * @param size    大小
     * @param scollId scoll id
     */
    public void scroll(int size, String scollId) {
        esPlusClientFacade.scrollByWrapper(index,type,super.esWrapper.esParamWrapper(), tClass, size, SCROLL_KEEP_TIME, scollId);
    }

    /**
     * 滚动
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     */
    public void scroll(int size, Duration keepTime, String scollId) {
        esPlusClientFacade.scrollByWrapper(index,type,esWrapper.esParamWrapper(), tClass, size, keepTime, scollId);
    }

    /**
     * 性能分析
     */
    public EsResponse<T> profile() {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.searchByWrapper(index,type,esWrapper.esParamWrapper(), tClass);
    }

}
