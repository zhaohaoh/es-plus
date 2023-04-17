package com.es.plus.core.process;

import com.es.plus.annotation.EsIndex;
import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.client.EsPlusIndexRestClient;
import com.es.plus.config.GlobalConfigCache;
import com.es.plus.constant.Commend;
import com.es.plus.constant.EsConstant;
import com.es.plus.constant.EsFieldType;
import com.es.plus.exception.EsException;
import com.es.plus.lock.ELock;
import com.es.plus.lock.EsReadWriteLock;
import com.es.plus.pojo.EsSettings;
import com.es.plus.properties.EsIndexParam;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static com.es.plus.constant.EsConstant.MAX_RESULT_WINDOW;
import static com.es.plus.constant.EsConstant.NUMBER_OF_SHARDS;

/**
 * es执行人工具封装
 *
 * @author hzh
 * @date 2022/09/03
 */
public class EsReindexProcess {
    private static final Logger log = LoggerFactory.getLogger(EsPlusIndexRestClient.class);
    // 重建索引的最多有10个就已经很多了
    private static final ThreadPoolExecutor reindexExecutor = new ThreadPoolExecutor(1, 10,
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
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(clazz);


        //根据别名获取索引结果 获取不到则通过索引名获取并且修改成目前的别名
        GetIndexResponse getIndexResponse = null;
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
        Map<String, Object> currentEsMapping = getCurrentEsMapping(getIndexResponse, currentIndex,esPlusClientFacade);

        //索引是否改变
        String updateCommend = getMappingUpdateCommend(currentEsMapping, clazz);

        //执行对应操作
        if (Objects.equals(updateCommend, Commend.MAPPING_UPDATE)) {
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
    private static boolean settingsUpdate(GetIndexResponse indexResponse, String currentIndex, Class<?> clazz, EsPlusClientFacade esPlusClientFacade) {
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(clazz);
        EsSettings esSettings = esIndexParam.getEsSettings();
        Settings settings = indexResponse.getSettings().get(currentIndex);
        String json = JsonUtils.toJsonStr(esSettings);
        Map<String, Object> localSettings = JsonUtils.toMap(json);
        Integer remoteShards = settings.getAsInt(NUMBER_OF_SHARDS, 5);
        Integer remoteMaxResultWindow = settings.getAsInt(MAX_RESULT_WINDOW, 10000);
        String remoteRefreshInterval = settings.get("index.refresh_interval", "1s");
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
        if (!remoteRefreshInterval.equals(localSettings.get("refresh_interval"))) {
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

    private static boolean analysisChange(Settings settings, Map<String, Object> analysis) {
        Map<StringBuilder, Object> analysisList = new HashMap<>();
        buildAnalysis(analysis, analysisList, new StringBuilder("index.analysis."));

        Settings settingsByPrefix = settings.getByPrefix("index.analysis.");
        Set<String> strings = settingsByPrefix.keySet();
        //如果es的配置比本地的多的话要reindex
        if (strings.size() > analysisList.size()) {
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
        EsSettings esSettings = new EsSettings();
        esSettings.setMaxResultWindow(11);
        String s = JsonUtils.toJsonStr(esSettings);
        Settings.Builder builder = Settings.builder().loadFromSource(s, XContentType.JSON);
        Settings build = builder.build();
        System.out.println(build);
    }


    /**
     * 做重建索引
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

        //迁移数据
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
        reindex = esPlusClientFacade.reindex(currentIndex, reindexName, QueryBuilders.rangeQuery(EsConstant.REINDEX_TIME_FILED).gte(currentTimeMillis));
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
        EsIndexParam esIndexParam = EsParamHolder.getEsIndexParam(clazz);
        // 新map添加NUMBER_OF_SHARDS
        Map<String, Object> localIndexMapping = esIndexParam.getMappings();
        // 本地和远程的索引
        boolean equals = localIndexMapping.equals(esIndexMapping);
        // 如果需要更新
        if (!equals) {
            // 获取属性中相同的属性判断是否改变
            Map<String, Object> localMappings = (Map<String, Object>) localIndexMapping.get(EsConstant.PROPERTIES);
            Map<String, Object> esMappings = (Map<String, Object>) esIndexMapping.get(EsConstant.PROPERTIES);

            // 如果长度相同.或者减少字段.那么必然要reindex
            if (localMappings.size() <= esMappings.size()) {
                return Commend.REINDEX;
            }

            // 长度不相等的情况.判断相同的字段是否改变.如果改了reindex.如果没有改变只需要对新的映射进行新增
            Set<String> set = Sets.intersection(localMappings.keySet(), esMappings.keySet());
            boolean isUpdate = set.stream().anyMatch(key -> !localMappings.get(key).equals(esMappings.get(key)));
            // 有字段改变REINDEX
            if (isUpdate) {
                return Commend.REINDEX;
            } else {
                return Commend.MAPPING_UPDATE;
            }
        }
        return Commend.NO_EXECUTE;
    }

    public static Map<String, Object> getCurrentEsMapping(GetIndexResponse indexResponse, String index,EsPlusClientFacade esPlusClientFacade) {
        MappingMetadata mappingMetadata = indexResponse.getMappings().get(index);
        // 设置mapping信息
        Map<String, Object> esIndexMapping = mappingMetadata.getSourceAsMap();

        //设置索引配置
//        Map<String, Object> indexSettings = new HashMap<>();
//        indexSettings.put(EsConstant.NUMBER_OF_SHARDS, settings.getAsInt(EsConstant.NUMBER_OF_SHARDS, 0));
//        indexSettings.put(EsConstant.MAX_RESULT_WINDOW, settings.getAsInt(EsConstant.MAX_RESULT_WINDOW, 100000));
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
        Map<String, Object> mappings = new HashMap<>((Map<String, Object>) esIndexMapping.get(EsConstant.PROPERTIES));
        Map<String, Object> type = new HashMap<>();
        type.put(EsConstant.TYPE, EsFieldType.LONG.name().toLowerCase());
        mappings.put(EsConstant.REINDEX_TIME_FILED, type);
        Map<String, Object> mappins = new HashMap<>();
        mappins.put(EsConstant.PROPERTIES, mappings);
        return mappins;
    }
}
