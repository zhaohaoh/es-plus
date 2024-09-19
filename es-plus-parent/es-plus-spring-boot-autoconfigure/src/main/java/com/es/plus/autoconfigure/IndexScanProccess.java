package com.es.plus.autoconfigure;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.ConnectFailHandleEnum;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.params.EsAliasResponse;
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
import com.es.plus.autoconfigure.interceptor.EsReindexInterceptor;
import com.es.plus.constant.Analyzer;
import com.es.plus.constant.DefaultClass;
import com.es.plus.constant.EsConstant;
import com.es.plus.constant.EsFieldType;
import com.es.plus.constant.JavaTypeEnum;
import com.es.plus.core.ClientContext;
import com.es.plus.core.process.EsReindexProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class IndexScanProccess implements InitializingBean, ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    
    private String basePackage;
    
    private String startAppClassName;
    
    public String getBasePackage() {
        return basePackage;
    }
    
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
    
    public String getStartAppClassName() {
        return startAppClassName;
    }
    
    public void setStartAppClassName(String startAppClassName) {
        this.startAppClassName = startAppClassName;
    }
    
    /**
     * 过程
     */
    public void proccess() {
        //可以通过扫描包获取加上注解的所有方法。获取加注解的所有方法
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().addScanners(Scanners.TypesAnnotated).forPackages(basePackage));
        
        //获取所有ES实体类 注入到全局参数
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EsIndex.class);
        for (Class<?> indexClass : classes) {
            
            // es索引实体信息
            EsEntityInfo esEntityInfo = GlobalParamHolder.getEsEntityInfo(indexClass);
            
            // 获取索引信息
            EsIndexParam esIndexParam = buildEsIndexParam(indexClass,
                    GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix());
            
            // 获取映射
            Map<String, Object> mapping = getMappings(indexClass, esIndexParam);
            
            EsIndex annotation = indexClass.getAnnotation(EsIndex.class);
            
            // 参数设置
            esIndexParam.setMappings(mapping);
            esEntityInfo.setEsIndexParam(esIndexParam);
            
            // 如果是子文档不执行创建索引的相关操作
            Class<?> parentClass = annotation.parentClass();
            if (parentClass != DefaultClass.class) {
                continue;
            }
            
            //启动时不初始化
            if (!GlobalConfigCache.GLOBAL_CONFIG.isStartInit()) {
                continue;
            }
            
            // 启动时不初始化
            if (!annotation.startInit()) {
                continue;
            }
    
          
            EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(esIndexParam.getClientInstance());
            
            try {
                //尝试创建或重建索引
                tryCreateOrReindex(esPlusClientFacade,indexClass, esIndexParam);
            } catch (Exception e) {
                if (StringUtils.isNotBlank(e.getLocalizedMessage()) && e.getLocalizedMessage()
                        .contains("ConnectException")) {
                    if (GlobalConfigCache.GLOBAL_CONFIG.getConnectFailHandle()
                            .equals(ConnectFailHandleEnum.THROW_EXCEPTION)) {
                        throw new EsException(e);
                    } else {
                        GlobalConfigCache.GLOBAL_CONFIG.setStartInit(false);
                    }
                } else {
                    log.error("es-plus tryLock Or createIndex OR tryReindex Exception:", e);
                }
            }
        }
        
        
        
        //1.全部索引都reindex    2.当前索引在重建索引的名单中
        String reindexScope = GlobalConfigCache.GLOBAL_CONFIG.getReindexScope();
        if (StringUtils.isNotBlank(reindexScope)){
            Collection<EsPlusClientFacade> clients = ClientContext.getClients();
            for (EsPlusClientFacade esPlusClientFacade : clients) {
                List<String> indexList = Arrays.stream(reindexScope.split(",")).collect(Collectors.toList());
                //重建索引时的拦截器
                EsReindexInterceptor esInterceptor = new EsReindexInterceptor(esPlusClientFacade.getEsLockFactory());
                esInterceptor.setReindexList(indexList);
                esPlusClientFacade.addInterceptor(esInterceptor);
            
                log.info("reindexScope :{} esPlusClientFacade host:{} addInterceptor",reindexScope,esPlusClientFacade.getHost());
            }
        }
    }
    
    private void tryCreateOrReindex(EsPlusClientFacade esPlusClientFacade,Class<?> indexClass, EsIndexParam esIndexParam) {
        String index = esIndexParam.getIndex();
        String alias = esIndexParam.getAlias();
        //此处获取的是执行锁
        ELock eLock = esPlusClientFacade.getLock(index);
        boolean lock = eLock.tryLock();
        try {
            if (lock) {
                //取索引名判断，会同时判断索引名和别名
                boolean exists = esPlusClientFacade.indexExists(index) || esPlusClientFacade.indexExists(alias);
                if (exists) {
                    boolean isReindex = EsReindexProcess.tryReindex(esPlusClientFacade, indexClass);
                    if (isReindex) {
                        reindexTask(esPlusClientFacade,esIndexParam);
                    }
                } else {
                    esPlusClientFacade.createIndexMapping(index, indexClass);
                    exists = true;
                }
                esIndexParam.setExists(exists);
                log.info("init es-plus indexResponse={} exists={}",index, exists);
            } else {
                //异步更新reindex后的index的任务
                reindexTask(esPlusClientFacade,esIndexParam);
            }
            
        } finally {
            if (lock) {
                eLock.unlock();
            }
        }
    }
    
    /**
     *   重建索引的任务  每10秒获取一次索引别名。检测reindex是否完成。
     */
    public void reindexTask(EsPlusClientFacade esPlusClientFacade,EsIndexParam esIndexParam) {
        String annotationIndex = esIndexParam.getIndex();
        String alias = esIndexParam.getAlias();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(()->{
            while (true) {
                // 定时任务
                String index = esPlusClientFacade.getAliasIndex(alias).getIndexs().stream().findFirst().get();
                if (!index.equals(annotationIndex)) {
                    log.info("reindex maybe success changeIndex newIndex={} oldIndex:{}", index,annotationIndex);
                    esIndexParam.setIndex(index);
                    //解锁
                    ELock eLock = esPlusClientFacade.getLock(annotationIndex + EsConstant.REINDEX_LOCK_SUFFIX);
                    eLock.unlock();
                    break;
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        });
    }
    
    /**
     * 获取映射
     *
     * @param indexClass   索引班
     * @param esIndexParam es索引参数
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    private Map<String, Object> getMappings(Class<?> indexClass, EsIndexParam esIndexParam) {
        Map<String, Object> mapping = new LinkedHashMap<>(1);
        Map<String, Object> mappingProperties = buildMappingProperties(indexClass, esIndexParam);
        // 子文档属性
        if (esIndexParam.getChildClass() != null) {
            Map<String, Object> childProperties = buildMappingProperties(esIndexParam.getChildClass(), esIndexParam);
            childProperties.forEach(mappingProperties::putIfAbsent);
        }
        mapping.put(EsConstant.PROPERTIES, mappingProperties);
        return mapping;
    }
    
    
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
            
            //如果索引存在别名，则通过别名获取真实的索引名称
            EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(esIndex.clientInstance());
            EsAliasResponse aliasResponse = esPlusClientFacade.getAliasIndex(esIndexParam.getAlias());
            Set<String> indexs = aliasResponse.getIndexs();
            if (!CollectionUtils.isEmpty(indexs)) {
                if (indexs.size() > 1) {
                    throw new EsException("存在多个索引指向同一个索引别名，请检查");
                }
                String index = indexs.stream().findFirst().get();
                if (!index.equals(esIndex.index())) {
                    log.error("索引已设置别名 别名获取到的索引和注解设置的索引不一致，请检查。" + " 最终设置的索引:{} 注解上的索引:{} 别名:{}", index,
                            esIndex.index(), esIndex.alias());
                }
                esIndexParam.setIndex(index);
            }
        }
        
        // 索引配置
        EsSettings esSettings = new EsSettings();
        if (ArrayUtils.isNotEmpty(esIndex.sortField())) {
            esSettings.setSortField(esIndex.sortField());
        }
        if (ArrayUtils.isNotEmpty(esIndex.sortOrder())) {
            esSettings.setSortOrder(esIndex.sortOrder());
        }
        esSettings.setNumberOfShards(esIndex.shard());
        esSettings.setNumberOfReplicas(esIndex.replices());
        esSettings.setRefreshInterval(esIndex.initRefreshInterval());
        esSettings.setMaxResultWindow(
                esIndex.initMaxResultWindow() <= 0 ? GlobalConfigCache.GLOBAL_CONFIG.getSearchSize()
                        : esIndex.initMaxResultWindow());
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
                entityInfo.setIdName(
                        StringUtils.isNotBlank(esFieldInfo.getName()) ? esFieldInfo.getName() : field.getName());
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
            String esMappingName =
                    esField != null && StringUtils.isNotBlank(esField.name()) ? esField.name() : field.getName();
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
    private String processNestedObjects(Field field, EsField esField, Map<String, Object> properties,
            EsIndexParam indexParam) {
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
        
        if (EsFieldType.OBJECT.name().equalsIgnoreCase(fieldType) || EsFieldType.NESTED.name()
                .equalsIgnoreCase(fieldType)) {
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
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        proccess();
    }
    
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        //后续可以通过启动类名配置默认扫描路径
        startAppClassName = event.getSpringApplication().getMainApplicationClass().getName();
    }
}
