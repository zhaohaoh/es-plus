package com.es.plus.adapter.params;

import com.es.plus.adapter.pojo.es.EpNestedSortBuilder;
import lombok.Data;
import org.elasticsearch.search.sort.NestedSortBuilder;

@Data
public class EsOrder {

    private String name;

    private String sort = "DESC";

    private EpNestedSortBuilder nestedSortBuilder;


}
