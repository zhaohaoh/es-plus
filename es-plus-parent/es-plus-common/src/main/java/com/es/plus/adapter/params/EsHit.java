package com.es.plus.adapter.params;

import com.es.plus.adapter.util.JsonUtils;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
public class EsHit {
    /**
     * 数据
     */
    private String data;

    /**
     * es数据
     */
    private Map<String, EsHits> innerHitsMap = new ConcurrentHashMap<>();

//
//    /**
//     * 获取当前data的嵌套
//     */
//    public <R> EsHits getEsInnerHits(SFunction<R,?>... function) {
//        StringJoiner sj = new StringJoiner(".");
//        for (SFunction<R, ?> rsFunction : function) {
//            sj.add(LambdaUtils.getFieldName(rsFunction));
//        }
//        String fieldName = sj.toString();
//        return getEsInnerHits(fieldName);
//    }
//
//    /**
//     * 获取当前data的嵌套list
//     */
//    public <T,R> List<T> getList(Class<T> tClass, SFunction<R, ?>... function) {
//        StringJoiner sj = new StringJoiner(".");
//        for (SFunction<R, ?> rsFunction : function) {
//            sj.add(LambdaUtils.getFieldName(rsFunction));
//        }
//        String fieldName = sj.toString();
//        return getList(tClass, fieldName);
//    }
//
//    /**
//     * 获取当前data的嵌套list的总数
//     */
//    public <R> long getInnerHitsTotal(SFunction<R, ?>... function) {
//        StringJoiner sj = new StringJoiner(".");
//        for (SFunction<R, ?> rsFunction : function) {
//            sj.add(LambdaUtils.getFieldName(rsFunction));
//        }
//        String fieldName = sj.toString();
//        return getInnerHitsTotal(fieldName);
//    }

    /**
     * 获取当前data的嵌套
     */
    public <T> EsHits getEsInnerHits(String fieldName) {
        return innerHitsMap.get(fieldName);
    }

    /**
     * 获取当前data的嵌套list
     */
    public <T> List<T> getInnerList(Class<T> tClass, String fieldName) {
        EsHits esHits = innerHitsMap.get(fieldName);
        if (esHits == null || esHits.getEsHitList() == null) {
            return new ArrayList<>();
        }
        List<T> list = esHits.getEsHitList().stream().map(EsHit::getData).filter(Objects::nonNull)
                .map(d -> JsonUtils.toBean(d, tClass)).collect(Collectors.toList());
        return list;
    }

    /**
     * 获取当前data的嵌套list的总数
     */
    public <T> long getInnerHitsTotal(String fieldName) {
        EsHits esHits = innerHitsMap.get(fieldName);
        if (esHits == null) {
            return 0;
        }
        return esHits.getTotal();
    }
}
