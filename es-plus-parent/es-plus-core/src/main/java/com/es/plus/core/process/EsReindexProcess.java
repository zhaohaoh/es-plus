package com.es.plus.core.process;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.Commend;
import com.es.plus.constant.EsConstant;
import com.es.plus.core.statics.Es;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.set.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.es.plus.constant.EsConstant.MAX_RESULT_WINDOW;
import static com.es.plus.constant.EsConstant.NUMBER_OF_SHARDS;
import static com.es.plus.constant.EsConstant.REINDEX_TIME_FILED;

/**
 * es执行人工具封装
 *
 * @author hzh
 * @date 2022/09/03
 */
public class EsReindexProcess {
    
    private static final Logger log = LoggerFactory.getLogger(EsReindexProcess.class);
    
    // 重建索引的最多有3个就已经很多了  再多cpu飙升
    private static final ThreadPoolExecutor reindexExecutor = new ThreadPoolExecutor(1, 3, 60L, TimeUnit.SECONDS,
            // 这个范围内的视为核心线程可以处理
            new SynchronousQueue<>(), new ThreadFactory() {
        private final ThreadGroup group;
        
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        
        private final String NAME_PREFIX = "ES-PLUS-REINDEX-";
        
        {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }
        
        @Override
        public Thread newThread(Runnable r) {
            //除了固定的boss线程。临时新增的线程会删除了会递增，int递增有最大值。这里再9999的时候就从固定线程的数量上重新计算.防止线程名字过长
            int current = threadNumber.getAndUpdate(operand -> operand >= 99999 ? 1 + 1 : operand + 1);
            Thread t = new Thread(group, r, NAME_PREFIX + current);
            //此线程未执行完容器不关闭
            t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());
    
    
    /**
     * 试着重建索引
     *
     * @param esPlusClientFacade es索引执行人
     * @param clazz              clazz
     */
    public static boolean tryReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz) {
        
        //获取索引信息
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(clazz);
        
        //根据别名获取索引结果 获取不到则通过索引名获取并且修改成目前的别名
        EsIndexResponse getIndexResponse = null;
        if (StringUtils.isBlank(esIndexParam.getAlias())) {
            getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getIndex());
        } else {
            getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getAlias());
            if (getIndexResponse == null) {
                getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getIndex());
                String index = getIndexResponse.getIndices()[0];
                String oldAlias = esPlusClientFacade.getAliasByIndex(index);
                if (oldAlias == null) {
                    esPlusClientFacade.createAlias(index, esIndexParam.getAlias());
                } else {
                    esPlusClientFacade.replaceAlias(index, oldAlias, esIndexParam.getAlias());
                }
            }
        }
        
        // 获取当前索引
        String currentIndex = getIndexResponse.getIndices()[0];
        
        // 重新更新配置  返回值是代表是否重建索引
        boolean reindex = settingsUpdate(getIndexResponse, currentIndex, clazz, esPlusClientFacade);
        
        //获取旧索引映射
        Map<String, Object> currentEsMapping = getIndexResponse.getMappings();
        
        //索引是否改变
        String updateCommend = getMappingUpdateCommend(currentEsMapping, clazz);
        
        //执行对应操作
        EsIndex annotation = clazz.getAnnotation(EsIndex.class);
        
        log.info("EsExecutorUtil tryReindex Commend:{}", updateCommend);
        if (Objects.equals(updateCommend, Commend.MAPPING_UPDATE)) {
            if (!annotation.updateMapping()) {
                return false;
            }
            log.info("es-plus mapping_update index [{}]", currentIndex);
            esPlusClientFacade.putMapping(currentIndex, clazz);
        } else if (Objects.equals(updateCommend, Commend.REINDEX) || reindex) {
            // 忽略处理reindex
            if (!annotation.tryReindex()) {
                log.info("es-plus index happened change but is not reindex  indexName: [{}]", currentIndex);
                return false;
            }
            if (StringUtils.isBlank(annotation.alias())) {
                throw new EsException(annotation.index() + " tryReindex alias Cannot be null");
            }
            if (GlobalConfigCache.GLOBAL_CONFIG.isAutoReindex()) {
                if (GlobalConfigCache.GLOBAL_CONFIG.isReindexAsync()) {
                    reindexExecutor
                            .execute(() -> tryLockReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex));
                } else {
                    tryLockReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex);
                }
                return true;
            }
        }
        return false;
    }
    
    //有事临时编写的代码
    private static boolean settingsUpdate(EsIndexResponse indexResponse, String currentIndex, Class<?> clazz,
            EsPlusClientFacade esPlusClientFacade) {
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(clazz);
        EsSettings esSettings = esIndexParam.getEsSettings();
        Map<String, String> settings = indexResponse.getSettings();
        
        String json = JsonUtils.toJsonStr(esSettings);
        Map<String, Object> localSettings = JsonUtils.toMap(json);
        Integer remoteShards =
                settings.get(NUMBER_OF_SHARDS) != null ? Integer.parseInt(settings.get(NUMBER_OF_SHARDS)) : 5;
        Integer remoteMaxResultWindow =
                settings.get(MAX_RESULT_WINDOW) != null ? Integer.parseInt(settings.get(MAX_RESULT_WINDOW)) : 10000;
        String remoteRefreshInterval = settings.get("index.refresh_interval");
        if (remoteShards != localSettings.get("number_of_shards")) {
            return true;
        }
        Map<String, Object> analysis = esSettings.getAnalysis();
        if (analysisChange(settings, analysis)) {
            return true;
        }
        
        //以下几个字段的变更对esSettings进行更新。但不reindex
        EsSettings newEsSettings = null;
        if (!remoteMaxResultWindow.equals(localSettings.get("max_result_window"))) {
            newEsSettings = new EsSettings();
            newEsSettings.setMaxResultWindow((Integer) localSettings.get("max_result_window"));
        }
        if (remoteRefreshInterval != null && !remoteRefreshInterval.equals(localSettings.get("refresh_interval"))) {
            if (newEsSettings == null) {
                newEsSettings = new EsSettings();
            }
            newEsSettings.setMaxResultWindow((Integer) localSettings.get("max_result_window"));
            newEsSettings.setRefreshInterval((String) localSettings.get("refresh_interval"));
        }
        if (newEsSettings != null) {
            esPlusClientFacade.updateSettings(currentIndex, newEsSettings);
        }
        return false;
    }
    
    
    private static boolean analysisChange(Map<String, String> settings, Map<String, Object> analysis) {
        Map<StringBuilder, Object> analysisList = new LinkedHashMap<>();
        buildAnalysis(analysis, analysisList, new StringBuilder("index.analysis."));
        
        long count = settings.keySet().stream().filter(a -> a.startsWith("index.analysis.")).count();
        
        //如果es的配置比本地的多的话要reindex
        if (count > analysisList.size()) {
            return true;
        }
        
        for (Map.Entry<StringBuilder, Object> entry : analysisList.entrySet()) {
            String value = settings.get(entry.getKey().toString());
            if (value == null) {
                return true;
            }
            if (!value.equals(entry.getValue().toString())) {
                return true;
            }
        }
        return false;
    }
    
    private static void buildAnalysis(Map<String, Object> analysis, Map<StringBuilder, Object> sbs, StringBuilder sb) {
        analysis.forEach((k, v) -> {
            StringBuilder builder = new StringBuilder(sb);
            builder.append(k);
            if (v instanceof Map) {
                builder.append(".");
                buildAnalysis((Map<String, Object>) v, sbs, builder);
            } else {
                if (v != null) {
                    sbs.put(builder, v);
                }
            }
        });
    }
    
    
    /**
     * 做重建索引 注意事项。es重建索引的时候，如果旧的字段被删除了并且旧的字段有数据的话，重建索引会自动创建旧的字段
     */
    private static void tryLockReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz, EsIndexParam esIndexParam,
            String currentIndex) {
        //获取新索引
        String reindexName = getReindexName(currentIndex);
        ELock eLock = esPlusClientFacade.getLock(esIndexParam.getIndex() + EsConstant.REINDEX_LOCK_SUFFIX);
        boolean lock = eLock.tryLock(reindexName);
        if (lock) {
            //如果能找到当前索引才需要执行reindex，否则已经执行过
            if (esPlusClientFacade.getIndex(currentIndex) != null) {
                doReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex, reindexName);
            }
        }
        
    }
    
    private static void doReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz, EsIndexParam esIndexParam,
            String currentIndex, String reindexName) {
        
        //获取重建索引的字段的值
        Object reindexFieldValue = getReindexValue(clazz, esIndexParam, currentIndex);
        if (reindexFieldValue == null) {
            return;
        }
        
        //创建没有别名的新索引
        esPlusClientFacade.createIndexWithoutAlias(reindexName, clazz);
        
        log.info("es-plus doReindex Begin currentIndex:{} newIndex:{}", currentIndex, reindexName);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        //迁移数据  不能随便修改索引映射的名字。如果改了名字，旧的字段还有数据，那么mapping和数据依然会被带到新的索引去。索引只能改变结构。而不是名字。改名字要先特殊处理字段
        boolean reindex = esPlusClientFacade.reindex(currentIndex, reindexName);
        if (!reindex) {
            log.error("es-plus reindex Fail");
        }
        
        //增量数据用户自己保障
        
        stopWatch.stop();
        
        log.info("es-plus first reindex End currentIndex:{} newIndex:{} totalTimeSeconds:{}", currentIndex, reindexName,
                stopWatch.getTotalTimeSeconds());
        
        //切换索引名
        esPlusClientFacade.swapAlias(currentIndex, reindexName, esIndexParam.getAlias());
        
        //切换当前索引名
//        esIndexParam.setIndex(reindexName);
        
        // 不删除老索引 备份历史数据 用户手动删除
        //        esPlusClientFacade.deleteIndex(currentIndex);
        
        log.info("es-plus doReindex All End currentIndex:{} newIndex:{}", currentIndex, reindexName);
    }
    
    private static Object getReindexValue(Class<?> clazz, EsIndexParam esIndexParam, String currentIndex) {
        //重建索引用来过滤新增数据的值
        Object reindexFieldValue = null;
        try {
            Object one = Es.chainQuery(clazz).sortByDesc(esIndexParam.getReindexField()).search(1).getOne();
            Field declaredField = one.getClass().getDeclaredField(esIndexParam.getReindexField());
            declaredField.setAccessible(true);
            reindexFieldValue = declaredField.get(one);
        } catch (Exception e) {
            log.error("reindex getReindexField error:", e);
        }
        
        if (reindexFieldValue == null) {
            log.error("reindex getReindexField fieldValue cannot be null so not reindex currentIndex:{}", currentIndex);
            return null;
        }
        return reindexFieldValue;
    }
    
    /**
     * 获取指令
     */
    public static String getMappingUpdateCommend(Map<String, Object> esIndexMapping, Class<?> clazz) {
        // 获取索引信息
        EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(clazz);
        // 新map添加NUMBER_OF_SHARDS
        Map<String, Object> localIndexMapping = esIndexParam.getMappings();
        // 本地和远程的索引
        return getCmd(esIndexMapping, localIndexMapping);
    }
    
    private static String getCmd(Map<String, Object> esIndexMapping, Map<String, Object> localIndexMapping) {
        boolean equals = localIndexMapping.equals(esIndexMapping);
        // 如果需要更新
        if (!equals) {
            // 获取属性中相同的属性判断是否改变
            Map<String, Object> localMappings = (Map<String, Object>) localIndexMapping.get(EsConstant.PROPERTIES);
            Map<String, Object> esMappings = (Map<String, Object>) esIndexMapping.get(EsConstant.PROPERTIES);
            
            //排除reindexTime字段
            esMappings.remove(REINDEX_TIME_FILED);
            
            // 如果减少字段.那么必然要reindex
            if (localMappings.size() < esMappings.size()) {
                return Commend.REINDEX;
            }
            // 如果减少字段.那么必然要reindex  针对有type的
            if (esIndexMapping.size() != localIndexMapping.size()) {
                return Commend.REINDEX;
            }
            
            // 本地长度大于等于es长度的话. 需要查询嵌套对象判断子对象需要reindex还是update
            // 取交集 取es中的映射
            Set<String> set = Sets.intersection(localMappings.keySet(), esMappings.keySet());
            //如果取交集的数量少于es的数量。本地和es的映射字段名发生了改变
            if (set.size() < esMappings.size()) {
                return Commend.REINDEX;
            }
            // 判断相同的字段是否改变.如果改了reindex.如果没有改变说明只是本地新增字段，只需要对新的映射进行新增
            boolean isUpdate = set.stream().anyMatch(key -> {
                Map<String, Object> localMapping = (Map) localMappings.get(key);
                Map<String, Object> esMapping = (Map) esMappings.get(key);
                Map<String, Object> localProperties = (Map) localMapping.get(EsConstant.PROPERTIES);
                Map<String, Object> esProperties = (Map) esMapping.get(EsConstant.PROPERTIES);
                boolean mappingChange = !localMapping.equals(esMapping);
                if (localProperties != null && esProperties != null) {
                    String cmd = getCmd(esMapping, localMapping);
                    //如果返回reindex说明有嵌套对象的变更，否则嵌套对象也只是更新或者不处理
                    boolean reindex = cmd.equals(Commend.REINDEX);
                    return mappingChange && reindex;
                }
                return mappingChange;
            });
            // 有字段改变REINDEX
            if (isUpdate) {
                return Commend.REINDEX;
            } else {
                return Commend.MAPPING_UPDATE;
            }
        }
        return Commend.NO_EXECUTE;
    }
    
    public static Map<String, Object> getCurrentEsMapping(EsIndexResponse indexResponse, String index,
            EsPlusClientFacade esPlusClientFacade) {
        Map<String, Object> esIndexMapping = indexResponse.getMappings();
        return esIndexMapping;
    }
    
    private static String getReindexName(String currentIndex) {
        return incrementLastNumber(currentIndex);
    }
    
    
    public static String incrementLastNumber(String input) {
        if (input == null) {
            return null;
        }
        // 正则表达式匹配字符串末尾的数字
        Pattern pattern = Pattern.compile("(.*)(\\d+)$");
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.matches()) {
            // 提取前面的字符串和末尾的数字
            String prefix = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));
            
            // 数字自增并转换回字符串
            String incrementedNumberStr = Integer.toString(number + 1);
            
            // 重新拼接字符串
            return prefix + incrementedNumberStr;
        } else {
            // 如果没有找到数字，返回原字符串
            return input + "_v1";
        }
    }
    
}
