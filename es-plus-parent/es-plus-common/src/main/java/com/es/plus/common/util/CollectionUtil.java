package com.es.plus.common.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合工具
 *
 * @author hzh
 * @date 2022/12/28
 */
public class CollectionUtil {
    public static <T> List<Collection<T>> splitList(Collection<T> collection, int splitSize) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }
        //分割成几个集合
        int splitCount = (collection.size() + splitSize - 1)  / splitSize;
        //从0开始每次循环n+1   只截取指定长度的流集合。
        return Stream.iterate(0, n -> n + 1)
                .limit(splitCount)
                //并行
                .parallel()
                .map(index -> collection.stream().skip((long)index * splitSize).limit(splitSize).collect(Collectors.toList()))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        ArrayList<String> objects = new ArrayList<>();
        objects.add("11");
        objects.add("11222");
        List<Collection<String>> collections = splitList(objects, 1);
        List<String> collect = collections.stream().map(a -> (ArrayList<String>) a)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }
}
