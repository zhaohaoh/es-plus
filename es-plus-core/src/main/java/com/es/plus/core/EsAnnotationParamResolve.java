package com.es.plus.core;

import com.es.plus.constant.JdkDataTypeEnum;
import com.es.plus.exception.EsException;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.util.ClassUtils;
import com.es.plus.constant.EsFieldType;
import com.es.plus.pojo.EsSettings;
import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.es.plus.constant.EsConstant.*;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsAnnotationParamResolve {

    /**
     * 构建es参数
     *
     * @param tClass   t类
     * @param esSuffix es后缀
     * @return {@link EsIndexParam}
     */
    public EsIndexParam buildEsIndexParam(Class<?> tClass, String esSuffix) {
        EsIndex esIndex = tClass.getAnnotation(EsIndex.class);
        if (esIndex == null) {
            return null;
        }
        if (StringUtils.isBlank(esIndex.index())) {
            throw new EsException("es entity annotation @EsIndex no has index");
        }
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setIndex(esIndex.index() + esSuffix);
        if (StringUtils.isNotBlank(esIndex.alias())) {
            esIndexParam.setAlias(esIndex.alias() + esSuffix);
        } else {
            esIndexParam.setAlias(esIndex.index() + esSuffix);
        }
        // 索引配置
        EsSettings esSettings = new EsSettings();
        esSettings.setNumberOfShards(esIndex.shard());
        esSettings.setNumberOfReplicas(esIndex.replices());
        esSettings.setRefreshInterval(esIndex.initRefreshInterval());
        esSettings.setMaxResultWindow(esIndex.initMaxResultWindow());
        if (StringUtils.isNotBlank(esIndex.defaultAnalyzer())) {
            esSettings.setDefaultAnalyzer(esIndex.defaultAnalyzer());
        }

        //添加设置的多个分词器
        String[] analyzers = esIndex.analyzer();

        //添加自定义分词器
        if (ArrayUtils.isNotEmpty(analyzers)) {
            Map<String, Object> analysis = new HashMap<>();
            Map<String, Object> child = new HashMap<>();
            analysis.put("analyzer", child);
            for (String analyzerName : analyzers) {
                Map map = EsParamHolder.getAnalysis(analyzerName);
                if (map != null) {
                    child.put(analyzerName, map);
                }
            }
            esSettings.setAnalysis(analysis);
        }
        esIndexParam.setEsSettings(esSettings);
        return esIndexParam;
    }

    /**
     * 建立映射属性
     *
     * @param tClass t类
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> buildMappingProperties(Class<?> tClass) {
        List<Field> fieldList = ClassUtils.getFieldList(tClass);
        Map<String, Object> mappings = new HashMap<>();

        //字段解析
        for (Field field : fieldList) {
            field.setAccessible(true);
            //id缓存.用来自动获取实体类id
            EsId esId = field.getAnnotation(EsId.class);
            if (esId != null) {
                EsParamHolder.put(tClass, field.getName());
            }

            //创建属性对象
            Map<String, Object> properties = new HashMap<>();
            // 解析自定义注解
            EsField esField = field.getAnnotation(EsField.class);

            String fieldName = field.getName();
            String fieldType;
            if (esField == null) {
                //创建属性对象
                fieldType = getAutoEsFieldType(field.getType());
                properties.put(TYPE, fieldType);
            } else {
                //创建属性对象
                if (StringUtils.isNotBlank(esField.name())) {
                    fieldName = esField.name();
                }
                fieldType = resolveAnnotationEsField(field, properties, esField);
            }
            // 字符串类型映射
            if ((EsFieldType.STRING.name().toLowerCase().equals(fieldType))) {
                properties.put(TYPE, TEXT);
                properties.put(FIELDS, EsParamHolder.getFieldsMap());
                //双类型字符串的映射转换
                EsParamHolder.putTextKeyword(tClass, fieldName);
            } else {
                properties.put(TYPE, fieldType);
            }
            mappings.put(fieldName, properties);
        }
        return mappings;
    }


    /**
     * 解析es注解
     */
    private String resolveAnnotationEsField(Field field, Map<String, Object> properties, EsField esField) {
        Class<?> fieldClass = field.getType();

        // 如果是嵌套类型.并且是集合的获取其泛型
        if (esField.type().equals(EsFieldType.NESTED) || esField.type().equals(EsFieldType.OBJECT)) {
            if (Collection.class.isAssignableFrom(fieldClass)) {
                Type genericType = field.getGenericType();
                ParameterizedType pt = (ParameterizedType) genericType;
                Type typeArgument = pt.getActualTypeArguments()[0];
                while (typeArgument instanceof ParameterizedType) {
                    typeArgument = ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                }
                fieldClass = (Class<?>) typeArgument;
                Map<String, Object> mappings = buildMappingProperties(fieldClass);
                properties.put(PROPERTIES, mappings);
            }
        }

        // 获取字段类型
        String fieldType;
        if (esField.type().equals(EsFieldType.AUTO)) {
            fieldType = getAutoEsFieldType(fieldClass);
        } else {
            fieldType = esField.type().name().toLowerCase();
        }

        //获取分词器
        if (StringUtils.isNotBlank(esField.analyzer())) {
            properties.put(ANALYZER, esField.analyzer());
        }

        //获取搜索分词器
        if (StringUtils.isNotBlank(esField.searchAnalyzer())) {
            properties.put(SEARCH_ANALYZER, esField.searchAnalyzer());
        }

        //获取是否存储
        if (esField.type() != EsFieldType.NESTED && esField.store()) {
            properties.put(STORE, true);
        }

        //获取是否被索引
        if (esField.type() != EsFieldType.NESTED && !esField.index()) {
            properties.put(INDEX, false);
        }

        //获取格式化
        if (StringUtils.isNotBlank(esField.format())) {
            properties.put(FORMAT, esField.format());
        }
        return fieldType;
    }

    private String getAutoEsFieldType(Class<?> clazz) {
        // 否则根据类型推断,String以及找不到的类型一律被当做keyword处理
        JdkDataTypeEnum jdkDataType = JdkDataTypeEnum.getByType(clazz.getSimpleName().toLowerCase());
        String type;
        switch (jdkDataType) {
            case BYTE:
                type = EsFieldType.BYTE.name();
                break;
            case SHORT:
                type = EsFieldType.SHORT.name();
                break;
            case INT:
                type = EsFieldType.INTEGER.name();
                break;
            case INTEGER:
                type = EsFieldType.INTEGER.name();
                break;
            case LONG:
                type = EsFieldType.LONG.name();
                break;
            case FLOAT:
                type = EsFieldType.FLOAT.name();
                break;
            case DOUBLE:
                type = EsFieldType.DOUBLE.name();
                break;
            case BIG_DECIMAL:
            case STRING:
                type = EsFieldType.STRING.name();
                break;
            case CHAR:
                type = EsFieldType.KEYWORD.name();
                break;
            case BOOLEAN:
                type = EsFieldType.BOOLEAN.name();
                break;
            case DATE:
                type = EsFieldType.DATE.name();
                break;
            case LOCAL_DATE:
                type = EsFieldType.DATE.name();
                break;
            case LOCAL_DATE_TIME:
                type = EsFieldType.DATE.name();
                break;
            case LIST:
                type = EsFieldType.TEXT.name();
                break;
            default:
                //识别不了返回OBJECT
                type = EsFieldType.OBJECT.name();
                break;
        }
        return type.toLowerCase();
    }
}
