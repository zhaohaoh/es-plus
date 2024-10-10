package com.es.plus.core.wrapper.core;


public interface EsStaitcsWrapper<Children> {

    /**
     * 索引
     *
     * @param index 索引
     * @return {@link Children}
     */
    Children index(String... index);

    /**
     * 类型
     *
     * @param type 类型
     * @return {@link Children}
     */
    Children type(String type);

    /**
     * _id
     *
     * @param _id id关键
     * @return {@link Children}
     */
    Children _id(String _id);

}
