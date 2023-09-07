package com.es.plus.adapter.proxy;

import com.es.plus.adapter.core.EsPlusClient;
import com.es.plus.adapter.params.EsResponse;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@EsInterceptors(value = {
        @InterceptorElement(type = EsPlusClient.class,methodName = "searchAfter")
})
public class EsLogInterceptor implements EsInterceptor{
    @Override
    public void before(String index, Method method, Object[] args) {

        System.out.println(index+method.toString());
    }

    @Override
    public void after(String index, Method method, Object[] args, Object result) {
        EsResponse esResponse = (EsResponse) result;
        Object[] tailSortValues = esResponse.getTailSortValues();
        System.out.println();
    }
}
