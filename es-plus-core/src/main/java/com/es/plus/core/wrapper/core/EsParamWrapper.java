package com.es.plus.core.wrapper.core;

import com.es.plus.pojo.EsHighLight;
import com.es.plus.pojo.EsOrder;
import com.es.plus.pojo.EsSelect;
import lombok.Data;
import org.elasticsearch.action.search.SearchType;

import java.util.List;

@Data
public class EsParamWrapper {
    /**
     * es查询结果包含字段
     */
    private EsSelect esSelect;
    /**
     * 搜索类型
     */
    private SearchType searchType;

    /**
     * 路由分片
     */
    private String[] routings;
    /*
     *高亮
     */
    protected List<EsHighLight> esHighLights;
    /**
     * es排序列表
     */
    protected List<EsOrder> esOrderList;
}
