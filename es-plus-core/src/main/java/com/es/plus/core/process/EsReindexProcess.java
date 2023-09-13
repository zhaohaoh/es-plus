package com.es.plus.core.process;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.lock.ELock;
import com.es.plus.adapter.lock.EsReadWriteLock;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.Commend;
import com.es.plus.constant.EsConstant;
import com.es.plus.constant.EsFieldType;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.set.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static com.es.plus.constant.EsConstant.*;

/**
 * es执行人工具封装
 *
 * @author hzh
 * @date 2022/09/03
 */
public class EsReindexProcess {
    private static final Logger log = LoggerFactory.getLogger(EsReindexProcess.class);
    // 重建索引的最多有3个就已经很多了  再多cpu飙升
    private static final ThreadPoolExecutor reindexExecutor = new ThreadPoolExecutor(1, 3,
            60L, TimeUnit.SECONDS,
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
    public static void tryReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz) {

        //获取索引信息
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(clazz);

        //根据别名获取索引结果 获取不到则通过索引名获取并且修改成目前的别名
        EsIndexResponse getIndexResponse = null;
        if (StringUtils.isBlank(esIndexParam.getAlias())) {
            getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getIndex());
        } else {
            getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getAlias());
            if (getIndexResponse == null) {
                getIndexResponse = esPlusClientFacade.getIndex(esIndexParam.getIndex());
                esPlusClientFacade.createAlias(getIndexResponse.getIndices()[0], esIndexParam.getAlias());
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
        if (Objects.equals(updateCommend, Commend.MAPPING_UPDATE)) {
            log.info("es-plus mapping_update index [{}]",currentIndex);
            esPlusClientFacade.putMapping(currentIndex, clazz);
        } else if (Objects.equals(updateCommend, Commend.REINDEX) || reindex) {
            // 忽略处理reindex
            EsIndex annotation = clazz.getAnnotation(EsIndex.class);
            if (!annotation.tryReindex()) {
                return;
            }
            if (StringUtils.isBlank(annotation.alias())) {
                throw new EsException(annotation.index() + " tryReindex alias Cannot be null");
            }
            if (GlobalConfigCache.GLOBAL_CONFIG.isAutoReindex()) {
                //执行reindex前先记录旧索引的时间映射
                Map<String, Object> mappins = getUpdateReindexTimeMappins(currentEsMapping);
                esPlusClientFacade.putMapping(currentIndex, mappins);

                if (GlobalConfigCache.GLOBAL_CONFIG.isReindexAsync()) {
                    // 异步逻辑还没写
                    reindexExecutor.execute(() -> tryLockReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex));
                } else {
                    tryLockReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex);
                }
            }
        }
        log.info("EsExecutorUtil tryReindex Commend:{}", updateCommend);
    }

    //有事临时编写的代码
    private static boolean settingsUpdate(EsIndexResponse indexResponse, String currentIndex, Class<?> clazz, EsPlusClientFacade esPlusClientFacade) {
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(clazz);
        EsSettings esSettings = esIndexParam.getEsSettings();
        Map<String, String> settings = indexResponse.getSettings();

        String json = JsonUtils.toJsonStr(esSettings);
        Map<String, Object> localSettings = JsonUtils.toMap(json);
        Integer remoteShards = settings.get(NUMBER_OF_SHARDS) != null ? Integer.parseInt(settings.get(NUMBER_OF_SHARDS)) : 5;
        Integer remoteMaxResultWindow = settings.get(MAX_RESULT_WINDOW) != null ? Integer.parseInt(settings.get(MAX_RESULT_WINDOW)) : 10000;
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

    public static void main(String[] args) {
//        EsSettings esSettings = new EsSettings();
//        esSettings.setMaxResultWindow(11);
//        String s = JsonUtils.toJsonStr(esSettings);
//        Settings.Builder builder = Settings.builder().loadFromSource(s, XContentType.JSON);
//        Settings build = builder.build();
//        System.out.println(build);
    }


    /**
     * 做重建索引 注意事项。es重建索引的时候，如果旧的字段被删除了并且旧的字段有数据的话，重建索引会自动创建旧的字段
     */
    private static void tryLockReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz, EsIndexParam esIndexParam, String currentIndex) {
        ELock eLock = esPlusClientFacade.getLock(esIndexParam.getIndex() + EsConstant.REINDEX_LOCK_SUFFIX);
        boolean lock = eLock.tryLock();
        try {
            if (lock) {
                //如果能找到当前索引才需要执行reindex，否则已经执行过
                if (esPlusClientFacade.getIndex(currentIndex) != null) {
                    doReindex(esPlusClientFacade, clazz, esIndexParam, currentIndex);
                }
            }
        } finally {
            //上面是否已经释放。如果上面的方法释放了这里不释放
            if (lock) {
                eLock.unlock();
            }
        }
    }

    private static void doReindex(EsPlusClientFacade esPlusClientFacade, Class<?> clazz, EsIndexParam esIndexParam, String currentIndex) {

        //记录重建索引前的时间戳
        long currentTimeMillis = System.currentTimeMillis();

        //获取新索引
        String reindexName = getReindexName(currentIndex);

        //创建没有别名的新索引
        esPlusClientFacade.createIndexWithoutAlias(reindexName, clazz);

        log.info("es-plus doReindex Begin currentIndex:{} newIndex:{}", currentIndex, reindexName);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //迁移数据  不能随便修改索引映射的名字。如果改了名字，旧的字段还有数据，那么mapping和数据依然会被带到新的索引去。索引只能改变结构。而不是名字。改名字要先特殊处理字段
        boolean reindex = esPlusClientFacade.reindex(currentIndex, reindexName, null);
        if (!reindex) {
            log.error("es-plus reindex Fail");
        }

        stopWatch.stop();

        log.info("es-plus first reindex End currentIndex:{} newIndex:{} totalTimeSeconds:{}", currentIndex, reindexName, stopWatch.getTotalTimeSeconds());

        // 新索引添加别名  切换了别名索引对于所有服务就已经可用.此时就需要执行释放锁
        EsReadWriteLock readWrtieLock = esPlusClientFacade.getReadWrtieLock(esIndexParam.getIndex() + EsConstant.REINDEX_UPDATE_LOCK);
        Lock lock = readWrtieLock.writeLock();
        lock.lock();
        try {
            esPlusClientFacade.replaceAlias(currentIndex, reindexName, esIndexParam.getAlias());
        } finally {
            lock.unlock();
            //解放锁的状态 其他服务在进行新增修改操作的时候修改状态
            esPlusClientFacade.getEsPlusClient().setReindexState(false);
        }

        // 第二次迁移残留数据
        reindex = esPlusClientFacade.reindex(currentIndex, reindexName, currentTimeMillis);
        if (!reindex) {
            throw new EsException("es-plus second reindex Fail");
        }

        //删除老索引
        esPlusClientFacade.deleteIndex(currentIndex);

        log.info("es-plus doReindex All End currentIndex:{} newIndex:{}", currentIndex, reindexName);
    }

    /**
     * 获取指令
     */
    public static String getMappingUpdateCommend(Map<String, Object> esIndexMapping, Class<?> clazz) {
        // 获取索引信息
        EsIndexParam esIndexParam = GlobalParamHolder.getEsIndexParam(clazz);
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
            if (esIndexMapping.size()!=localIndexMapping.size()){
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

    public static Map<String, Object> getCurrentEsMapping(EsIndexResponse indexResponse, String index, EsPlusClientFacade esPlusClientFacade) {
        Map<String, Object> esIndexMapping = indexResponse.getMappings();
        return esIndexMapping;
    }

    private static String getReindexName(String currentIndex) {
        String reindexName;
        if (currentIndex.endsWith(EsConstant.SO_SUFFIX)) {
            reindexName = currentIndex.split(EsConstant.SO_SUFFIX)[0] + EsConstant.S1_SUFFIX;
        } else {
            reindexName = currentIndex.split(EsConstant.S1_SUFFIX)[0] + EsConstant.SO_SUFFIX;
        }
        return reindexName;
    }


    /**
     * 得到更新时间mappins重建索引
     *
     * @param esIndexMapping 索引映射
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    private static Map<String, Object> getUpdateReindexTimeMappins(Map<String, Object> esIndexMapping) {
        Map<String, Object> mappings = new LinkedHashMap<>((Map<String, Object>) esIndexMapping.get(EsConstant.PROPERTIES));
        Map<String, Object> type = new LinkedHashMap<>();
        type.put(EsConstant.TYPE, EsFieldType.LONG.name().toLowerCase());
        mappings.put(EsConstant.REINDEX_TIME_FILED, type);
        Map<String, Object> mappins = new LinkedHashMap<>();
        mappins.put(EsConstant.PROPERTIES, mappings);
        return mappins;
    }
}
