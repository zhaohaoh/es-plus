package com.es.plus.adapter.params;

import lombok.Data;
import org.elasticsearch.search.sort.NestedSortBuilder;

@Data
public class EsOrder {

    private String name;

    private String sort = "DESC";

    private NestedSortBuilder nestedSortBuilder;


}
