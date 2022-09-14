package com.es.plus.core.wrapper;


import com.es.plus.pojo.EsHighLight;
import com.es.plus.pojo.EsOrder;
import com.es.plus.pojo.EsSelect;
import com.es.plus.pojo.EsUpdateField;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.List;

public interface EsWrapper<T> {
    List<EsOrder> getEsOrderList();

    EsSelect getEsSelect();

    EsSelect getSelect();

    List<EsHighLight> getEsHighLight();

    BoolQueryBuilder getQueryBuilder();

    default EsUpdateField getEsUpdateField() {
        return null;
    }
}
