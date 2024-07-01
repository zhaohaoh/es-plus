package com.es.plus.adapter;

import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.properties.EsEntityInfo;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.AnnotationResolveUtil;
import com.es.plus.adapter.util.ClassUtils;
import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.annotation.Score;
import com.es.plus.constant.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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
   
        esIndexParam.setType(esIndex.type());
        esIndexParam.setIndex(esIndex.index() + esSuffix);
        if (StringUtils.isNotBlank(esIndex.alias())) {
            esIndexParam.setAlias(esIndex.alias() + esSuffix);
        }
        // 索引配置
        EsSettings esSettings = new EsSettings();
        if (ArrayUtils.isNotEmpty(esIndex.sortField())){
            esSettings.setSortField(esIndex.sortField());
        }
        if (ArrayUtils.isNotEmpty(esIndex.sortOrder())){
            esSettings.setSortOrder(esIndex.sortOrder());
        }
        esSettings.setNumberOfShards(esIndex.shard());
        esSettings.setNumberOfReplicas(esIndex.replices());
        esSettings.setRefreshInterval(esIndex.initRefreshInterval());
        esSettings.setMaxResultWindow(esIndex.initMaxResultWindow() <= 0 ? GlobalConfigCache.GLOBAL_CONFIG.getSearchSize() : esIndex.initMaxResultWindow());
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
        esIndexParam.setClientInstance(esIndex.clientInstance());

        return esIndexParam;
    }

    private void putAnalyzer(String[] analyzers, Map<String, Object> analysis) {
        Map<String, Object> child = new HashMap<>();
        analysis.put(Analyzer.ANALYZER, child);
        if (ArrayUtils.isNotEmpty(analyzers)) {
            for (String analyzerName : analyzers) {
                Map map = GlobalParamHolder.getAnalysis(analyzerName);
                if (map != null) {
                    child.put(analyzerName, map);
                }
            }
        } else {
            String defaultAnalyzer = GlobalConfigCache.GLOBAL_CONFIG.getDefaultAnalyzer();
            if (StringUtils.isNotBlank(defaultAnalyzer)) {
                Map map = GlobalParamHolder.getAnalysis(defaultAnalyzer);
                if (map != null) {
                    child.put(defaultAnalyzer, map);
                }
            }
        }
    }

    private void putNormalizer(Map<String, Object> analysis) {
        String defaultNormalizer = GlobalConfigCache.GLOBAL_CONFIG.getDefaultNormalizer();
        Map epNormalizer = GlobalParamHolder.getAnalysis(defaultNormalizer);
        if (!CollectionUtils.isEmpty(epNormalizer)) {
            Map<String, Object> child = new HashMap<>();
            child.put(defaultNormalizer, epNormalizer);
            analysis.put(EsConstant.NORMALIZER, child);
        }
    }

    /**
     * 建立映射属性
     *
     * @param tClass t类
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> buildMappingProperties(Class<?> tClass, EsIndexParam indexParam) {
        List<Field> fieldList = ClassUtils.getFieldList(tClass);
        Map<String, Object> mappings = new LinkedHashMap<>();
        //实体信息
        EsEntityInfo entityInfo = GlobalParamHolder.getEsEntityInfo(tClass);
        //字段解析
        for (Field field : fieldList) {
            field.setAccessible(true);
            //id缓存.用来自动获取实体类id
            EsId esId = field.getAnnotation(EsId.class);
            EsFieldInfo esFieldInfo = null;

            if (field.getAnnotation(Score.class) != null) {
                indexParam.setScoreField(field.getName());
            }

            //创建属性对象
            Map<String, Object> properties = new LinkedHashMap<>();
            // 解析自定义注解
            EsField esField = field.getAnnotation(EsField.class);

            // 获取es字段类型
            String fieldType = processNestedObjects(field, esField, properties, indexParam);

            // 处理字段注解和es的映射
            processAnnotationEsField(properties, esField);

            if (esId != null) {
                esFieldInfo = AnnotationResolveUtil.resolveEsId(esId);
                entityInfo.setIdName(StringUtils.isNotBlank(esFieldInfo.getName()) ? esFieldInfo.getName() : field.getName());
            }
            // 解析注解字段
            if (esFieldInfo == null) {
                esFieldInfo = AnnotationResolveUtil.resolveEsField(esField);
            }
            // 解析实体字段
            if (esFieldInfo == null) {
                esFieldInfo = AnnotationResolveUtil.resolveField(field);
            }

            // 设置到实体字段
            Map<String, EsFieldInfo> fieldsInfoMap = entityInfo.getFieldsInfoMap();
            fieldsInfoMap.put(field.getName(), esFieldInfo);

            // es映射到实体字段关系
            Map<String, String> mappingFieldMap = entityInfo.getMappingFieldMap();
            String esMappingName = esField != null && StringUtils.isNotBlank(esField.name()) ? esField.name() : field.getName();
            mappingFieldMap.put(esMappingName, field.getName());

            // keyword转换关系
            Map<String, String> convertKeywordMap = entityInfo.getConvertKeywordMap();

            if (esFieldInfo.isExist()) {
                if (StringUtils.isNotBlank(fieldType)) {
                    // 字符串类型映射
                    if ((EsFieldType.STRING.name().toLowerCase().equals(fieldType))) {
                        properties.put(EsConstant.TYPE, EsConstant.TEXT);
                        properties.put(EsConstant.FIELDS, EsConstant.KEYWORDS_MAP);
                        //双类型字符串的映射转换
                        convertKeywordMap.put(esMappingName, esMappingName + ".keyword");
                    } else {
                        properties.put(EsConstant.TYPE, fieldType);
                    }
                }
                if (!CollectionUtils.isEmpty(properties)) {
                    mappings.put(esMappingName, properties);
                }
            }
        }
        return mappings;
    }


    /**
     * 处理嵌套对象
     */
    private String processNestedObjects(Field field, EsField esField, Map<String, Object> properties, EsIndexParam indexParam) {
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
            properties.put(EsConstant.PROPERTIES, buildMappingProperties(fieldClass, indexParam));
        }

        if (fieldType.equalsIgnoreCase(EsFieldType.JOIN.name())) {
            Map<String, Object> relation = new LinkedHashMap<>(1);
            relation.put(esField.parent(), esField.child());
            properties.put(EsConstant.EAGER_GLOBAL_ORDINALS, true);
            properties.put(EsConstant.RELATIONS, relation);
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
            properties.put(Analyzer.ANALYZER, esField.analyzer());
        }

        //获取搜索分词器
        if (StringUtils.isNotBlank(esField.searchAnalyzer())) {
            properties.put(EsConstant.SEARCH_ANALYZER, esField.searchAnalyzer());
        }

        //获取是否存储
        if (esField.type() != EsFieldType.NESTED && esField.store()) {
            properties.put(EsConstant.STORE, true);
        }

        //获取是否被索引
        if (esField.type() != EsFieldType.NESTED && !esField.index()) {
            properties.put(EsConstant.INDEX, false);
        }

        if (esField.type() == EsFieldType.TEXT && esField.fieldData()) {
            properties.put(EsConstant.INDEX, true);
        }
        if (esField.type() == EsFieldType.KEYWORD && StringUtils.isNotBlank(esField.normalizer())) {
            properties.put(EsConstant.NORMALIZER, esField.normalizer());
        }

        if (ArrayUtils.isNotEmpty(esField.copyTo())) {
            List<String> copyTo = Arrays.stream(esField.copyTo()).collect(Collectors.toList());
            properties.put(EsConstant.COPY_TO, copyTo);
        }

        if (esField.eagerGlobalOrdinals()) {
            properties.put(EsConstant.EAGER_GLOBAL_ORDINALS, true);
        }

        //获取格式化
        if (esField.type().equals(EsFieldType.DATE) && StringUtils.isNotBlank(esField.esFormat())) {
            properties.put(EsConstant.FORMAT, esField.esFormat());
        }
    }

    private String getEsFieldType(Class<?> clazz) {
        String type = "";
        // 否则根据类型推断,String以及找不到的类型一律被当做keyword处理
        JavaTypeEnum jdkDataType = JavaTypeEnum.getByType(clazz.getSimpleName().toLowerCase());

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
