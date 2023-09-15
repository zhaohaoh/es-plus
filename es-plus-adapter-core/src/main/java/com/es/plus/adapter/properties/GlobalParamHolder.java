package com.es.plus.adapter.properties;

import com.es.plus.adapter.EsAnnotationParamProcess;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.constants.EsIdType;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.util.ClassUtils;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: hzh
 * @Date: 2022/1/24 15:27
 */
@SuppressWarnings("all")
public class GlobalParamHolder {
    private static final Logger logger = LoggerFactory.getLogger(GlobalParamHolder.class);
    // 属性解析器
    private static final EsAnnotationParamProcess ES_ANNOTATION_PARAM_RESOLVE = new EsAnnotationParamProcess();
    // id的线程本地变量
    private static final ThreadLocal<String> _ID = new ThreadLocal<>();

    // 分词处理器
    private static final Map<String, Map> ANALYSIS_MAP = new ConcurrentHashMap<>();

    // 实体类对象存储
    private static final Map<String, EsEntityInfo> ES_ENTITY_INFO_MAP = new ConcurrentHashMap<>();


    public static EsEntityInfo getEsEntityInfo(Class<?> clazzName) {
        return ES_ENTITY_INFO_MAP.computeIfAbsent(clazzName.getName(),a->new EsEntityInfo());
    }

    public static <T> String getDocId(T obj) {
        Class<T> clazz = (Class<T>) ClassUtils.getClass(obj.getClass());
        try {
            EsEntityInfo esEntityInfo = ES_ENTITY_INFO_MAP.get(clazz.getName());
            //先获取线程本地id名
            String idFeildName = _id();
            if (idFeildName == null) {
                //如果对象字段获取不到id走自动生成
                idFeildName = esEntityInfo.getIdName();
                if (idFeildName == null) {
                    idFeildName = GlobalConfigCache.GLOBAL_CONFIG.getGlobalEsId();
                }
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


    public static String _id() {
        String _id = _ID.get();
        _ID.remove();
        return _id;
    }

    public static void set_id(String _id) {
        _ID.set(_id);
    }


    /**
     * 得到es索引参数  初始化EsEntityInfo的入口
     *
     * @param clazz clazz
     * @return {@link EsIndexParam}
     */
    public static EsIndexParam getAndInitEsIndexParam(Class<?> clazz) {
        // 如果是子文档获取其父文档的属性
        EsIndex annotation = clazz.getAnnotation(EsIndex.class);
        if (annotation != null && annotation.parentClass() != DefaultClass.class) {
            clazz = annotation.parentClass();
        }
        EsEntityInfo esEntityInfo = getEsEntityInfo(clazz);
        EsIndexParam indexParam = esEntityInfo.getEsIndexParam();
        if (indexParam == null) {
            indexParam = ES_ANNOTATION_PARAM_RESOLVE.buildEsIndexParam(clazz, GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix());
            if (indexParam == null) {
                return null;
            }
            Map<String, Object> mapping = new LinkedHashMap<>(1);
            Map<String, Object> mappingProperties = ES_ANNOTATION_PARAM_RESOLVE.buildMappingProperties(clazz, indexParam);
            // 子文档属性
            if (indexParam.getChildClass() != null) {
                Map<String, Object> childProperties = ES_ANNOTATION_PARAM_RESOLVE.buildMappingProperties(indexParam.getChildClass(), indexParam);
                childProperties.forEach(mappingProperties::putIfAbsent);
            }
            mapping.put(EsConstant.PROPERTIES, mappingProperties);
            indexParam.setMappings(mapping);
        }
        return indexParam;
    }


    public static String getStringKeyword(Class<?> clazz, String name) {
        EsEntityInfo esEntityInfo = ES_ENTITY_INFO_MAP.computeIfAbsent(clazz.getName(), e -> new EsEntityInfo());
        return esEntityInfo.getConvertKeywordMap().get(name);
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

    /**
     * 取索引字段信息
     *
     * @param clazz clazz
     * @param name  名字
     */
    public static EsFieldInfo getIndexField(Class<?> clazz, String name) {
        EsEntityInfo esEntityInfo = ES_ENTITY_INFO_MAP.computeIfAbsent(clazz.getName(), e -> new EsEntityInfo());
        return esEntityInfo.getFieldsInfoMap().get(name);
    }



}
