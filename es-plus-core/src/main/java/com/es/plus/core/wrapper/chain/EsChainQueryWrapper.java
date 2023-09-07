package com.es.plus.core.wrapper.chain;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsQueryParamWrapper;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.core.ClientContext;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Request;

import java.time.Duration;

import static com.es.plus.constant.EsConstant.MASTER;
import static com.es.plus.constant.EsConstant.SCROLL_KEEP_TIME;

/**
 * es链查询包装器
 *
 * @author hzh
 * @date 2023/04/07
 */
public class EsChainQueryWrapper<T> extends AbstractEsChainWrapper<T, String, EsChainQueryWrapper<T>, EsQueryWrapper<T>> implements ChainQueryWrapper {
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);

    public EsChainQueryWrapper(Class<T> clazz) {
        super.tClass = clazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
        }
    }

    public EsChainQueryWrapper(Class<T> clazz, EsPlusClientFacade esPlusClientFacade) {
        super.tClass = clazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(super.tClass);
        if (esIndexParam != null) {
            index = StringUtils.isBlank(esIndexParam.getAlias()) ? esIndexParam.getIndex() : esIndexParam.getAlias();
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
    @Override
    public EsResponse<T> search() {
        return esPlusClientFacade.search(index, type, esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 列表
     *
     * @return {@link EsResponse}<{@link T}>
     */
    @Override
    public EsResponse<T> search(int size) {
        EsQueryParamWrapper esQueryParamWrapper = esWrapper.esParamWrapper().getEsQueryParamWrapper();
        esQueryParamWrapper.setSize(size);
        return esPlusClientFacade.search(index, type, esWrapper.esParamWrapper(), tClass);
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
        return esPlusClientFacade.search(index, type, super.esWrapper.esParamWrapper(), tClass);
    }


    /**
     * 聚合
     */
    @Override
    public EsAggResponse<T> aggregations() {
        return esPlusClientFacade.aggregations(index, type, super.esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 统计
     *
     * @return long
     */
    @Override
    public long count() {
        return esPlusClientFacade.count(index, type, super.esWrapper.esParamWrapper());
    }

    /**
     * 滚动
     *
     * @param size    大小
     * @param scollId scoll id
     */
    @Override
    public EsResponse<T> scroll(int size, String scollId) {
        return esPlusClientFacade.scroll(index, type, super.esWrapper.esParamWrapper(), tClass, size, SCROLL_KEEP_TIME, scollId);
    }

    /**
     * 滚动
     *
     * @param size     大小
     * @param keepTime 保持时间
     * @param scollId  scoll id
     */
    @Override
    public EsResponse<T> scroll(int size, Duration keepTime, String scollId) {
        return esPlusClientFacade.scroll(index, type, esWrapper.esParamWrapper(), tClass, size, keepTime, scollId);
    }

    /**
     * 性能分析
     */
    @Override
    public EsResponse<T> profile() {
        esWrapper.esParamWrapper().getEsQueryParamWrapper().setProfile(true);
        return esPlusClientFacade.search(index, type, esWrapper.esParamWrapper(), tClass);
    }

    /**
     * 执行dsl
     *
     * @param dsl dsl
     * @return {@link String}
     */
    @Override
    public String executeDSL(String dsl) {
        Request request = new Request("get", index + "_search");
        request.setJsonEntity(dsl);
        return esPlusClientFacade.executeDSL(dsl, index);
    }
}
