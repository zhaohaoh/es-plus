package com.es.plus.properties;

import com.es.plus.annotation.EsIndex;
import com.es.plus.config.GlobalConfigCache;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsConstant;
import com.es.plus.core.process.EsAnnotationParamProcess;
import com.es.plus.enums.EsIdType;
import com.es.plus.exception.EsException;
import com.es.plus.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    // 分词处理器
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
            //如果对象字段获取不到id走自动生成
            String idFeildName = ID_MAP.get(clazz.getName());
            if (idFeildName == null) {
                idFeildName = GlobalConfigCache.GLOBAL_CONFIG.getGlobalEsId();
            }
            //如果map字段获取不到id走自动生成
            if (obj instanceof Map) {
                Object id = ((Map<?, ?>) obj).get(idFeildName);
                if (id == null) {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    ((Map) obj).put(idFeildName, uuid);
                    return uuid;
                } else {
                    return String.valueOf(id);
                }
            }
            //有对象字段直接走对象字段 为空则抛出异常
            Field field = clazz.getDeclaredField(idFeildName);
            field.setAccessible(true);
            Object id = field.get(obj);
            //如果没有值则自动生成uuid注入
            if (id == null) {
                if (EsIdType.UUID.equals(GlobalConfigCache.GLOBAL_CONFIG.getEsIdType())) {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    field.set(obj, uuid);
                }
            }
            return String.valueOf(id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        // es自动生成id
        return UUID.randomUUID().toString().replace("-", "");
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
            if (indexParam == null) {
                return null;
            }
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
        Map value = ANALYSIS_MAP.get(name);
        if (value != null) {
            throw new EsException("analysis config is exists");
        }
        ANALYSIS_MAP.put(name, map);
    }

}
