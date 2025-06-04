package com.es.plus.autoconfigure;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.BulkProcessorConfig;
import com.es.plus.adapter.config.ConnectFailHandleEnum;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.params.BulkProcessorParam;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.pojo.EsReindexResult;
import com.es.plus.adapter.properties.EsEntityInfo;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.AnnotationResolveUtil;
import com.es.plus.adapter.util.ClassUtils;
import com.es.plus.annotation.BulkProcessor;
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
import com.es.plus.core.statics.Es;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.es.plus.adapter.config.GlobalConfigCache.GLOBAL_CONFIG;
import static com.es.plus.constant.EsConstant.IGNORE_ABOVE;
import static com.es.plus.constant.EsConstant.KEYWORDS_MAP;

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
            EsIndexParam esIndexParam = buildEsIndexParam(indexClass);
            
            // 获取映射
            Map<String, Object> mapping = getMappings(indexClass, esIndexParam);
            
            EsIndex esIndex = indexClass.getAnnotation(EsIndex.class);
            
            EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(esIndexParam.getClientInstance());
            
            BulkProcessor bulkProcessor = indexClass.getAnnotation(BulkProcessor.class);
            //设置批量异常处理的参数
            if (bulkProcessor != null) {
                BulkProcessorParam bulkProcessorParam = new BulkProcessorParam();
                bulkProcessorParam.setBulkActions(bulkProcessor.bulkActions());
                bulkProcessorParam.setBulkSize(new ByteSizeValue(bulkProcessor.bulkSize(), ByteSizeUnit.MB));
                bulkProcessorParam.setConcurrent(bulkProcessor.concurrent());
                bulkProcessorParam.setFlushInterval(TimeValue.timeValueSeconds(bulkProcessor.flushInterval()));
                bulkProcessorParam.setBackoffPolicyTime(bulkProcessor.BackoffPolicyTime());
                bulkProcessorParam.setBackoffPolicyRetryMax(bulkProcessor.BackoffPolicyRetryMax());
                for (String index : esIndexParam.getIndex()) {
                    BulkProcessorConfig.getBulkProcessor(esPlusClientFacade.getEsPlusClient().getRestHighLevelClient(),
                            index );
                }
            
            }
            
            // 参数设置
            esIndexParam.setMappings(mapping);
            esEntityInfo.setEsIndexParam(esIndexParam);
            
            // 如果是子文档不执行创建索引的相关操作
            Class<?> parentClass = esIndex.parentClass();
            if (parentClass != DefaultClass.class) {
                continue;
            }
            
            //启动时不初始化
            if (!GLOBAL_CONFIG.isStartInit()) {
                continue;
            }
            
            // 启动时不初始化
            if (!esIndex.startInit()) {
                continue;
            }
            
            try {
                //尝试创建或重建索引
                tryCreateOrReindex(esPlusClientFacade, indexClass, esIndexParam);
            } catch (Exception e) {
                if (StringUtils.isNotBlank(e.getLocalizedMessage()) && e.getLocalizedMessage()
                        .contains("ConnectException")) {
                    if (GLOBAL_CONFIG.getConnectFailHandle().equals(ConnectFailHandleEnum.THROW_EXCEPTION)) {
                        throw new EsException(e);
                    } else {
                        GLOBAL_CONFIG.setStartInit(false);
                    }
                } else {
                    log.error("es-plus tryLock Or createIndex OR tryReindex Exception:", e);
                }
            }
        }
        
        //1.全部索引都reindex    2.当前索引在重建索引的名单中
        String reindexScope = GLOBAL_CONFIG.getReindexScope();
        if (StringUtils.isNotBlank(reindexScope)) {
            Collection<EsPlusClientFacade> clients = ClientContext.getClients();
            for (EsPlusClientFacade esPlusClientFacade : clients) {
                List<String> indexList = Arrays.stream(reindexScope.split(",")).collect(Collectors.toList());
                //重建索引时的拦截器
                EsReindexInterceptor esInterceptor = new EsReindexInterceptor(esPlusClientFacade.getEsLockFactory());
                esInterceptor.setReindexList(indexList);
                esPlusClientFacade.addInterceptor(esInterceptor);
                
                log.info("reindexEnable:{} reindexScope :{} esPlusClientFacade host:{} addInterceptor",
                        GLOBAL_CONFIG.isAutoReindex(), reindexScope, esPlusClientFacade.getHost());
            }
        }
    }
    
    private void tryCreateOrReindex(EsPlusClientFacade esPlusClientFacade, Class<?> indexClass,
            EsIndexParam esIndexParam) {
        String[] indexs = esIndexParam.getIndex();
        String[] aliases = esIndexParam.getAlias();
        String alias = null;
        if (aliases != null && aliases.length == 1) {
            alias = esIndexParam.getAlias()[0];
        } else if (aliases != null && aliases.length > 1) {
            log.error("{} 多别名的索引不支持自动reindex  别名:{},", Arrays.toString(esIndexParam.getIndex()),
                    Arrays.toString(esIndexParam.getAlias()));
            return;
        }
        log.info("Es-plus tryCreateIndexOrMappingOrReindex indexNames:{}", Arrays.toString(esIndexParam.getIndex()));
        for (String index : indexs) {
            //此处获取的是执行锁
            ELock eLock = esPlusClientFacade.getLock(index);
            boolean lock = eLock.tryLock();
            try {
                if (lock) {
                    //取索引名判断，会同时判断索引名和别名
                    boolean exists =
                            esPlusClientFacade.indexExists(index);
                    if (exists) {
                        boolean isReindex = EsReindexProcess.tryReindex(esPlusClientFacade, index, alias, indexClass);
                        if (isReindex) {
                            reindexTask(esPlusClientFacade, esIndexParam, index);
                        }
                    } else {
                        esPlusClientFacade.createIndexMapping(index, indexClass);
                        exists = true;
                    }
                    esIndexParam.setExists(exists);
                    log.info("init es-plus index={}", index);
                } else {
                    //异步更新reindex后的index的任务
                    reindexTask(esPlusClientFacade, esIndexParam, index);
                }
            } finally {
                if (lock) {
                    eLock.unlock();
                }
            }
        }
        
    }
    
    private String getReindexNewIndexName(String index, EsIndexParam esIndexParam) {
        String currentIndex = index;
        while (true) {
            EsReindexResult record;
            try {
                boolean exists = Es.chainIndex().index("es_plus_reindex_record").indexExists();
                if (!exists){
                    return index;
                }
                record = Es.chainQuery(EsReindexResult.class).index("es_plus_reindex_record")._id(currentIndex).search()
                        .getOne();
                if (record == null || record.getReIndexName() == null || record.getProcessType() == null
                        || record.getProcessType() == 0) {
                    return null;
                }
            } catch (Exception e) {
                log.error("getReindexNewIndexName Exception:", e);
                return index;
            }
            
            if (record.getProcessType().equals(1)) {
                log.error(
                        "Es-plus 当前注解索引已经产生reindex的变更。系统自动使用索引  当前索引:{} 已经reindex成功的索引:{}",
                        currentIndex, record.getReIndexName());
                String[] indexParamIndex = esIndexParam.getIndex();
                String[] idxs = ArrayUtils.removeElement(indexParamIndex, currentIndex);
                idxs = ArrayUtils.add(idxs, record.getReIndexName());
                esIndexParam.setIndex(idxs);
                currentIndex = record.getReIndexName();
            }
            
            return currentIndex;
        }
    }
    
    /**
     * 重建索引的任务  每10秒获取一次索引别名。检测reindex是否完成。
     */
    public void reindexTask(EsPlusClientFacade esPlusClientFacade, EsIndexParam esIndexParam, String annotationIndex) {
        String alias = esIndexParam.getAlias()[0];
        String[] indexs = esIndexParam.getIndex();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (true) {
                // 定时任务
                EsReindexResult record = Es.chainQuery(EsReindexResult.class).index("es_plus_reindex_record")
                        ._id(annotationIndex).search().getOne();
                if (record != null) {
                    String reIndexName = record.getReIndexName();
                    if (record.getProcessType().equals(1)) {
                        ArrayUtils.removeElement(indexs, annotationIndex);
                        ArrayUtils.add(indexs, reIndexName);
                        log.info("reindex maybe success changeIndex newIndex={} oldIndex:{} lastIndexParam:{}",
                                reIndexName, annotationIndex, esIndexParam);
                        
                        //解锁
                        ELock eLock = esPlusClientFacade.getLock(annotationIndex + EsConstant.REINDEX_LOCK_SUFFIX);
                        eLock.unlock();
                        break;
                    }else{
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                        }
                    }
                }else{
                    break;
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
    public EsIndexParam buildEsIndexParam(Class<?> tClass) {
        EsIndex esIndex = tClass.getAnnotation(EsIndex.class);
        if (esIndex == null) {
            return null;
        }
        
        EsIndexParam esIndexParam = new EsIndexParam();
        if (ArrayUtils.isEmpty(esIndex.index())) {
            throw new EsException("es entity annotation @EsIndex no has index");
        }
        // TODO 这里要改
        String[] indexArray = splitIndex(esIndex.index()[0]);
        if (indexArray == null) {
            esIndexParam.setDynamicIndex(false);
        } else {
            if (esIndex.index().length > 1) {
                throw new EsException("dynamicIndex 不支持多索引名");
            }
            esIndexParam.setDynamicIndex(true);
            esIndexParam.setDynamicIndexPrefix(indexArray[0]);
            esIndexParam.setDynamicIndexSpel(indexArray[1]);
            esIndexParam.setDynamicIndexSuffix(indexArray[2]);
        }
        
        esIndexParam.setType(esIndex.type());
        esIndexParam.setIndex(esIndex.index());
        
        //如果索引名和别名都只有一个   之前是为了做索引自动reindex 目前为了兼容多索引名和多alias名称就先这样兼容
        if (ArrayUtils.isNotEmpty(esIndex.alias())) {
            esIndexParam.setAlias(esIndex.alias());
        }
        String[] index = esIndexParam.getIndex();
        for (String idx : index) {
            getReindexNewIndexName(idx,esIndexParam);
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
                esIndex.initMaxResultWindow() <= 0 ? GLOBAL_CONFIG.getSearchSize() : esIndex.initMaxResultWindow());
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
            String defaultAnalyzer = GLOBAL_CONFIG.getDefaultAnalyzer();
            if (StringUtils.isNotBlank(defaultAnalyzer)) {
                Map map = GlobalParamHolder.getAnalysis(defaultAnalyzer);
                if (map != null) {
                    child.put(defaultAnalyzer, map);
                }
            }
        }
    }
    
    private void putNormalizer(Map<String, Object> analysis) {
        String defaultNormalizer = GLOBAL_CONFIG.getDefaultNormalizer();
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
                esFieldInfo = AnnotationResolveUtil.resolveEsField(esField, field);
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
                        properties.put(EsConstant.FIELDS, KEYWORDS_MAP);
                        //双类型字符串的映射转换
                        convertKeywordMap.put(esMappingName, esMappingName + ".keyword");
                    } else if (EsFieldType.KEYWORD.name().toLowerCase().equals(fieldType)) {
                        //很关键
                        properties.put(EsConstant.TYPE, "keyword");
                        properties.put(IGNORE_ABOVE,
                                esFieldInfo.getIgnoreAbove() == null || esFieldInfo.getIgnoreAbove() == 0 ? 256
                                        : esFieldInfo.getIgnoreAbove());
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
    
    public static String[] splitIndex(String input) {
        // 定义正则表达式模式，其中(.*?)表示非贪婪匹配任意字符，直到遇到后面的模式
        // #\{ 和 \} 分别匹配 #{ 和 }
        // (.*?) 在 #{} 中进行非贪婪匹配
        // (.*) 匹配前缀和后缀，这里使用贪婪匹配因为我们需要尽可能多地匹配字符直到遇到 #{} 或字符串末尾/开头
        String regex = "(.*?)#\\{([^}]+)\\}(.*)";
        
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        
        // 创建匹配器对象
        Matcher matcher = pattern.matcher(input);
        
        // 如果找到匹配项
        if (matcher.matches()) {
            // 提取前缀、中间值和后缀
            String prefix = matcher.group(1);
            String middle = matcher.group(2);
            String suffix = matcher.group(3);
            
            if (StringUtils.isNotBlank(middle)) {
                middle = "#{" + middle + "}";
            }
            // 返回结果数组
            return new String[] {prefix, middle, suffix};
        } else {
            return null;
        }
    }
    
    public static void main(String[] args) {
        // 测试示例
        String testString1 = "es_index_test_#{value}";
        String[] result1 = splitIndex(testString1);
        System.out.println("Result 1: " + Arrays.toString(result1)); // 输出: [es_index_, 123, _suffix]
        
        String testString2 = "prefix_#{value}_anotherSuffix";
        String[] result2 = splitIndex(testString2);
        System.out.println("Result 2: " + Arrays.toString(result2)); // 输出: [prefix_, value, _anotherSuffix]
        
        // 注意：如果输入字符串不符合预期模式（即没有#{}），将抛出异常
        // String testString3 = "no_match_here";
        // String[] result3 = splitString(testString3); // 这将抛出IllegalArgumentException
    }
}
