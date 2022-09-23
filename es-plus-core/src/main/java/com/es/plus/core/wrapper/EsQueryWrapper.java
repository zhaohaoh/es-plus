package com.es.plus.core.wrapper;


import com.es.plus.core.tools.SFunction;

public class EsQueryWrapper<T> extends AbstractEsWrapper<T, SFunction<T, ?>, EsQueryWrapper<T>> {


    /**
     * 可自动映射keyword  建议使用
     *
     * @param tClass
     */
    public EsQueryWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsQueryWrapper() {
    }

    @Override
    protected EsQueryWrapper<T> instance() {
        if (super.tClass != null) {
            return new EsQueryWrapper<>(super.tClass);
        }
        return new EsQueryWrapper<>(super.tClass);
    }


    public Class<T> gettClass() {
        return super.tClass;
    }


}
