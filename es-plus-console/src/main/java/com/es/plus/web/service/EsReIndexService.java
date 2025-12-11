package com.es.plus.web.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.web.config.EsException;
import com.es.plus.web.util.JsonUtils;
import com.es.plus.web.mapper.EsReindexMapper;
import com.es.plus.web.pojo.EsReindexTask;
import com.es.plus.web.pojo.EsindexDataMove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class EsReIndexService {
    
    @Autowired
    private EsReindexMapper esReindexMapper;
    
    @Autowired
    private EsRestService esRestService;
    
    /**
     * reindex任务插入
     */
    public String reindexTaskInsert(EsReindexTask esReindexTask) {
        LambdaQueryWrapper<EsReindexTask> eq = Wrappers.<EsReindexTask>lambdaQuery()
                .eq(EsReindexTask::getTaskId, esReindexTask.getTaskId());
        EsReindexTask reindexTask = esReindexMapper.selectOne(eq);
        //数据重复
        if (reindexTask != null) {
            return reindexTask.getTaskId();
        }
        esReindexMapper.insert(esReindexTask);
        return esReindexTask.getTaskId();
    }
    
    /**
     * reindex任务更新
     */
    public void reindexTaskUpdate(EsReindexTask esReindexTask) {
        esReindexMapper.updateById(esReindexTask);
    }
    
    /**
     * 跨集群数据迁移
     */
    public void indexDataMove(EsindexDataMove esindexDataMove) {
        if (esindexDataMove.getTargetIndex() == null || esindexDataMove.getSourceIndex() == null) {
            throw new EsException("来源或目标索引为空");
        }
        
        if (esindexDataMove.getMaxSize() > 1000000) {
            throw new EsException("跨集群迁移最大限制100万数据量");
        }
        if (esindexDataMove.getBatchSize() <= 0) {
            throw new EsException("批次数量有误");
        }
        
        String batchId = UUID.randomUUID().toString();
        Integer maxSize = esindexDataMove.getMaxSize();
        int currentTotalSize = 0;
        int batchSize = esindexDataMove.getBatchSize();
        
        // 构建初始searchAfter查询
        String dsl = buildSearchAfterDsl(esindexDataMove.getBatchSize(), null);
        String searchResponse = esRestService.search(esindexDataMove.getSourceClient(), esindexDataMove.getSourceIndex(), dsl);
        
        // 解析响应获取总数和第一批数据
        Map<String, Object> responseMap = JSONUtil.toBean(searchResponse, cn.hutool.json.JSONObject.class);
        Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");
        
        // 兼容 ES 6.x 和 ES 7.x/8.x 的 total 字段格式
        long total = 0;
        if (hits != null) {
            Object totalObj = hits.get("total");
            if (totalObj instanceof Number) {
                // ES 6.x 格式：total 是数字
                total = ((Number) totalObj).longValue();
            } else if (totalObj instanceof Map) {
                // ES 7.x/8.x 格式：total 是对象 {"value": 数字, "relation": "eq"}
                Map<String, Object> totalMap = (Map<String, Object>) totalObj;
                Object valueObj = totalMap.get("value");
                if (valueObj instanceof Number) {
                    total = ((Number) valueObj).longValue();
                }
            }
        }
        
        List<Map<String, Object>> list = hits != null ? (List<Map<String, Object>>) hits.get("hits") : new ArrayList<>();
        
        // 创建任务记录
        EsReindexTask esReindexTask = new EsReindexTask();
        esReindexTask.setTaskId(batchId);
        esReindexTask.setSourceIndex(esindexDataMove.getSourceIndex());
        esReindexTask.setTargetIndex(esindexDataMove.getTargetIndex());
        esReindexTask.setCreateTime(System.currentTimeMillis());
        esReindexTask.setCreateUid(StpUtil.getLoginId() != null ? Long.parseLong(StpUtil.getLoginId().toString()) : 0);
        esReindexTask.setSourceClient(esindexDataMove.getSourceClient());
        esReindexTask.setTargetClient(esindexDataMove.getTargetClient());
        esReindexTask.setType(2);
        
        reindexTaskInsert(esReindexTask);
        
        int count = 0;
        Object[] lastSortValues = null;
        
        while (true) {
            if (CollectionUtils.isEmpty(list)) {
                Map<String, Object> map = new HashMap<>();
                map.put("taskProcess", currentTotalSize + "/" + total);
                esReindexTask.setTaskJson(cn.hutool.json.JSONUtil.toJsonStr(map));
                reindexTaskUpdate(esReindexTask);
                break;
            }
            
            currentTotalSize += list.size();
            
            if (currentTotalSize >= maxSize) {
                log.info("同步数量大于最大限制数量 停止同步 batchId:{} syncSize:{} maxSize:{}", batchId,
                        currentTotalSize, maxSize);
                break;
            }
            
            count++;
            
            // 每5次更新一次进度
            if (count % 5 == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("taskProcess", currentTotalSize + "/" + total);
                esReindexTask.setTaskJson(cn.hutool.json.JSONUtil.toJsonStr(map));
                reindexTaskUpdate(esReindexTask);
            }
            
            // 提取_source数据并执行批量保存
            List<Map<String, Object>> documents = new ArrayList<>();
            for (Map<String, Object> hit : list) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                if (source != null) {
                    // 添加_id用于保存
                    source.put("_id", hit.get("_id"));
                    
                    // 添加_routing用于保存（如果存在）
                    Object routing = hit.get("_routing");
                    if (routing != null) {
                        source.put("_routing", routing);
                    }
                    
                    documents.add(source);
                }
                
                // 获取最后一个文档的sort值用于searchAfter
                if (hit.equals(list.get(list.size() - 1))) {
                    List<Object> sortValues = (List<Object>) hit.get("sort");
                    if (sortValues != null) {
                        lastSortValues = sortValues.toArray();
                    }
                }
            }
            
            // 使用HTTP方式批量保存到目标索引
            if (!documents.isEmpty()) {
                esRestService.saveBatch(esindexDataMove.getTargetClient(), esindexDataMove.getTargetIndex(), documents);
            }
            
            log.info("Es-plus 跨集群迁移 batchId:{} 本次同步数据 :{} 尾部sort值:{}", batchId, documents.size(),
                    lastSortValues);
            
            // 构建下一次searchAfter查询
            if (lastSortValues != null) {
                dsl = buildSearchAfterDsl(batchSize, lastSortValues);
                searchResponse = esRestService.search(esindexDataMove.getSourceClient(), esindexDataMove.getSourceIndex(), dsl);
                responseMap = JsonUtils.toMap(searchResponse);
                hits = (Map<String, Object>) responseMap.get("hits");
                list = (List<Map<String, Object>>) hits.get("hits");
            } else {
                list.clear();
            }
            
            try {
                Thread.sleep(esindexDataMove.getSleepTime());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        log.info("Es-plus 跨集群迁移  同步所有数据完成 batchId:{} 总数:{}", batchId, currentTotalSize);
    }
    
    /**
     * 构建searchAfter查询DSL
     */
    private String buildSearchAfterDsl(int size, Object[] searchAfterValues) {
        Map<String, Object> dsl = new HashMap<>();
        dsl.put("size", size);
        dsl.put("sort", new Object[]{"_doc"});
        
        if (searchAfterValues != null) {
            dsl.put("search_after", searchAfterValues);
        }
        
        return cn.hutool.json.JSONUtil.toJsonStr(dsl);
    }
}