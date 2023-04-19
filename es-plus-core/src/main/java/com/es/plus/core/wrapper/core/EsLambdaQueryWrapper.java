package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.tools.SFunction;

public class EsLambdaQueryWrapper<T> extends AbstractEsWrapper<T, SFunction<T,?>, EsLambdaQueryWrapper<T>> {


    /**
     * 可自动映射keyword  建议使用
     *
     * @param tClass
     */
    public EsLambdaQueryWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsLambdaQueryWrapper() {
    }

    @Override
    protected EsLambdaQueryWrapper<T> instance() {
        if (super.tClass != null) {
            return new EsLambdaQueryWrapper<>(super.tClass);
        }
        return new EsLambdaQueryWrapper<>(super.tClass);
    }


    public Class<T> gettClass() {
        return super.tClass;
    }

}
