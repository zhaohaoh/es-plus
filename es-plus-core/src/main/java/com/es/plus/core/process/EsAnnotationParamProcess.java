package com.es.plus.core.process;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.config.GlobalConfigCache;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsFieldType;
import com.es.plus.constant.JdkDataTypeEnum;
import com.es.plus.exception.EsException;
import com.es.plus.pojo.EsSettings;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.util.ClassUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.es.plus.constant.Analyzer.ANALYZER;
import static com.es.plus.constant.EsConstant.*;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsAnnotationParamProcess {

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
        EsIndexParam esIndexParam = new EsIndexParam();


        if (StringUtils.isBlank(esIndex.index())) {
            throw new EsException("es entity annotation @EsIndex no has index");
        }

        esIndexParam.setIndex(esIndex.index() + esSuffix);
        if (StringUtils.isNotBlank(esIndex.alias())) {
            esIndexParam.setAlias(esIndex.alias() + esSuffix);
        }
        // 索引配置
        EsSettings esSettings = new EsSettings();
        esSettings.setNumberOfShards(esIndex.shard());
        esSettings.setNumberOfReplicas(esIndex.replices());
        esSettings.setRefreshInterval(esIndex.initRefreshInterval());
        esSettings.setMaxResultWindow(Math.max(esIndex.initMaxResultWindow(), GlobalConfigCache.GLOBAL_CONFIG.getSearchSize()));
        if (StringUtils.isNotBlank(esIndex.defaultAnalyzer())) {
            esSettings.setDefaultAnalyzer(esIndex.defaultAnalyzer());
        }

        //添加设置的多个分词器
        String[] analyzers = esIndex.analyzer();

        Map<String, Object> analysis = new HashMap<>();

        //添加自定义分词器
        putAnalyzer(analyzers, analysis);

        //添加自定义keyword处理器
        putNormalizer(analysis);

        esSettings.setAnalysis(analysis);

        esIndexParam.setEsSettings(esSettings);
        //父子文档
        Class<?> childClass = esIndex.childClass();
        if (childClass != DefaultClass.class) {
            esIndexParam.setChildClass(childClass);
        }

        return esIndexParam;
    }

    private void putAnalyzer(String[] analyzers, Map<String, Object> analysis) {
        Map<String, Object> child = new HashMap<>();
        analysis.put(ANALYZER, child);
        if (ArrayUtils.isNotEmpty(analyzers)) {
            for (String analyzerName : analyzers) {
                Map map = EsParamHolder.getAnalysis(analyzerName);
                if (map != null) {
                    child.put(analyzerName, map);
                }
            }
        } else {
            String defaultAnalyzer = GlobalConfigCache.GLOBAL_CONFIG.getDefaultAnalyzer();
            if (StringUtils.isNotBlank(defaultAnalyzer)) {
                Map map = EsParamHolder.getAnalysis(defaultAnalyzer);
                if (map != null) {
                    child.put(defaultAnalyzer, map);
                }
            }
        }
    }

    private void putNormalizer(Map<String, Object> analysis) {
        String defaultNormalizer = GlobalConfigCache.GLOBAL_CONFIG.getDefaultNormalizer();
        Map epNormalizer = EsParamHolder.getAnalysis(defaultNormalizer);
        if (!CollectionUtils.isEmpty(epNormalizer)) {
            Map<String, Object> child = new HashMap<>();
            child.put(defaultNormalizer, epNormalizer);
            analysis.put(NORMALIZER, child);
        }
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

            // 获取es字段类型
            String fieldType = processNestedObjects(field, esField, properties);

            // 处理字段注解
            processAnnotationEsField(properties, esField);

            String fieldName = esField != null && StringUtils.isNotBlank(esField.name()) ? esField.name() : field.getName();

            if (StringUtils.isNotBlank(fieldType)) {
                // 字符串类型映射
                if ((EsFieldType.STRING.name().toLowerCase().equals(fieldType))) {
                    properties.put(TYPE, TEXT);
                    properties.put(FIELDS, EsParamHolder.getFieldsMap());
                    //双类型字符串的映射转换
                    EsParamHolder.putTextKeyword(tClass, fieldName);
                } else {
                    properties.put(TYPE, fieldType);
                }
            }
            if (!CollectionUtils.isEmpty(properties)) {
                mappings.put(fieldName, properties);
            }
        }
        return mappings;
    }

    // 待完成.这里需要处理数据是否需要递归.但是又要处理fileType还没想清楚
    private String processNestedObjects(Field field, EsField esField, Map<String, Object> properties) {
        String fieldType;
        Class<?> fieldClass = field.getType();
        if (esField == null || esField.type().equals(EsFieldType.AUTO)) {
            fieldType = getEsFieldType(fieldClass);
        } else {
            fieldType = esField.type().name().toLowerCase();
        }

        //处理数组集合
        if (fieldClass.isArray()) {
            fieldClass = fieldClass.getComponentType();
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            Type genericType = field.getGenericType();
            ParameterizedType pt = (ParameterizedType) genericType;
            Type typeArgument = pt.getActualTypeArguments()[0];
            while (typeArgument instanceof ParameterizedType) {
                typeArgument = ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
            }
            fieldClass = (Class<?>) typeArgument;
        }

        // list的自动映射
        if (esField == null) {
            fieldType = getEsFieldType(fieldClass);
        }

        if (EsFieldType.OBJECT.name().equalsIgnoreCase(fieldType) || EsFieldType.NESTED.name().equalsIgnoreCase(fieldType)) {
            properties.put(PROPERTIES, buildMappingProperties(fieldClass));
        }

        if (fieldType.equalsIgnoreCase(EsFieldType.JOIN.name())) {
            Map<String, Object> relation = new HashMap<>(1);
            relation.put(esField.parent(), esField.child());
            properties.put(EAGER_GLOBAL_ORDINALS, true);
            properties.put(RELATIONS, relation);
        }

        if (fieldType.equalsIgnoreCase(EsFieldType.OBJECT.name())) {
            fieldType = "";
        }
        return fieldType;
    }


    /**
     * 解析es注解
     */
    private void processAnnotationEsField(Map<String, Object> properties, EsField esField) {
        if (esField == null) {
            return;
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

        if (esField.type() == EsFieldType.TEXT && esField.fieldData()) {
            properties.put(INDEX, true);
        }
        if (esField.type() == EsFieldType.KEYWORD && StringUtils.isNotBlank(esField.normalizer())) {
            properties.put(NORMALIZER, esField.normalizer());
        }

        if (ArrayUtils.isNotEmpty(esField.copyTo())) {
            List<String> copyTo = Arrays.stream(esField.copyTo()).collect(Collectors.toList());
            properties.put(COPY_TO, copyTo);
        }

        if (esField.eagerGlobalOrdinals()) {
            properties.put(EAGER_GLOBAL_ORDINALS, true);
        }

        //获取格式化
        if (StringUtils.isNotBlank(esField.format())) {
            properties.put(FORMAT, esField.format());
        }
    }

    private String getEsFieldType(Class<?> clazz) {
        String type = "";
        // 否则根据类型推断,String以及找不到的类型一律被当做keyword处理
        JdkDataTypeEnum jdkDataType = JdkDataTypeEnum.getByType(clazz.getSimpleName().toLowerCase());

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
                type = EsFieldType.KEYWORD.name();
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
                break;
            default:
                break;
        }
        return type.toLowerCase();
    }
}
