package com.es.plus.core.wrapper.core;


import com.es.plus.pojo.EsSelect;
import org.elasticsearch.action.search.SearchType;

public interface EsExtendsWrapper<Children, R> {

    EsSelect getSelect();

    Children routings(String... routings);

    Children includes(R... func);

    Children includes(String... names);

    Children excludes(R... func);

    Children excludes(String... names);

    Children orderBy(String order, R... columns);

    Children orderBy(String order, String... columns);

    Children orderByAsc(String... columns);

    Children orderByDesc(String... columns);

    Children searchType(SearchType searchType);

    Children highLight(String field);

    Children highLight(String field, String preTag, String postTag);
}
