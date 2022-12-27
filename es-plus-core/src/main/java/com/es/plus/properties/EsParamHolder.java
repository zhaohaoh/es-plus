package com.es.plus.properties;

import com.es.plus.annotation.EsIndex;
import com.es.plus.config.GlobalConfigCache;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsConstant;
import com.es.plus.core.process.EsAnnotationParamProcess;
import com.es.plus.exception.EsException;
import com.es.plus.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.es.plus.constant.EsConstant.PROPERTIES;

/**
 * @Author: hzh
 * @Date: 2022/1/24 15:27
 */
public class EsParamHolder {
    private static final Logger logger = LoggerFactory.getLogger(EsParamHolder.class);
    // 属性解析器
    private static final EsAnnotationParamProcess ES_ANNOTATION_PARAM_RESOLVE = new EsAnnotationParamProcess();
    // id的map
    private static final Map<String, String> ID_MAP = new ConcurrentHashMap<>();
    // 转换keyword
    private static final Map<String, Map<String, String>> CONVERT_KEYWORD_MAP = new ConcurrentHashMap<>();
    // 分词器
    private static final Map<String, Map> ANALYSIS_MAP = new ConcurrentHashMap<>();
    // settings的映射
    private static final Map<String, EsIndexParam> ESINDEXPARAM_MAP = new ConcurrentHashMap<>();
    // 字段映射
    private static final Map<String, Object> FIELDS_MAP = new HashMap<>();


    static {
        Map<String, Object> keywordsMap = new HashMap<>();
        keywordsMap.put(EsConstant.TYPE, "keyword");
        keywordsMap.put("ignore_above", 256);
        FIELDS_MAP.put("keyword", keywordsMap);
    }

    public static Map<String, Object> getFieldsMap() {
        return FIELDS_MAP;
    }

    public static <T> String getDocId(T obj) {
        Class<T> clazz = (Class<T>) ClassUtils.getClass(obj.getClass());
        try {
            String idFeildName = ID_MAP.get(clazz.getName());
            if (idFeildName == null) {
                idFeildName = GlobalConfigCache.GLOBAL_CONFIG.getGlobalEsId();
                if (StringUtils.isBlank(idFeildName)) {
                    return null;
                }
            }
            if (obj instanceof Map) {
                return String.valueOf(((Map<?, ?>) obj).get(idFeildName));
            }
            Field field = clazz.getDeclaredField(idFeildName);
            field.setAccessible(true);
            Object id = field.get(obj);
            if (id == null) {
                throw new EsException("elasticsearch doc id not found");
            }
            return String.valueOf(id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        // es自动生成id
        return null;
    }

    public static void put(Class<?> clazz, String id) {
        ID_MAP.put(clazz.getName(), id);
    }


    /**
     * 得到es索引参数
     *
     * @param clazz clazz
     * @return {@link EsIndexParam}
     */
    public static EsIndexParam getEsIndexParam(Class<?> clazz) {
        // 如果是子文档获取其父文档的属性
        EsIndex annotation = clazz.getAnnotation(EsIndex.class);
        if (annotation != null && annotation.parentClass() != DefaultClass.class) {
            clazz = annotation.parentClass();
        }
        Class<?> finalClazz = clazz;
        EsIndexParam esIndexParam = ESINDEXPARAM_MAP.computeIfAbsent(clazz.getName(), s -> {
            EsIndexParam indexParam = ES_ANNOTATION_PARAM_RESOLVE.buildEsIndexParam(finalClazz, GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix());
            Map<String, Object> mapping = new HashMap<>(1);
            Map<String, Object> mappingProperties = ES_ANNOTATION_PARAM_RESOLVE.buildMappingProperties(finalClazz);
            // 子文档属性
            if (indexParam.getChildClass() != null) {
                Map<String, Object> childProperties = ES_ANNOTATION_PARAM_RESOLVE.buildMappingProperties(indexParam.getChildClass());
                childProperties.forEach(mappingProperties::putIfAbsent);
            }
            mapping.put(PROPERTIES, mappingProperties);
            indexParam.setMappings(mapping);
            return indexParam;
        });
        return esIndexParam;
    }


    public static String getStringKeyword(Class<?> clazz, String name) {
        Map<String, String> map = CONVERT_KEYWORD_MAP.get(clazz.getName());
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return map.get(name);
    }

    public static void putTextKeyword(Class<?> clazz, String name) {
        Map<String, String> map = CONVERT_KEYWORD_MAP.computeIfAbsent(clazz.getName(), p -> new HashMap<>());
        map.put(name, name + ".keyword");
    }

    public static Map getAnalysis(String name) {
        return ANALYSIS_MAP.get(name);
    }

    public static void putAnalysis(String name, Map map) {
        ANALYSIS_MAP.put(name, map);
    }

}
