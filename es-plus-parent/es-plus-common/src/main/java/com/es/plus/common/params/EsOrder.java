package com.es.plus.common.params;

import com.es.plus.common.pojo.es.EpNestedSortBuilder;
import lombok.Data;
@Data
public class EsOrder {

    private String name;

    private String sort = "DESC";

    private EpNestedSortBuilder nestedSortBuilder;


}
