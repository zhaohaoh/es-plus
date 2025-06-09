package com.es.plus.web.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.web.mapper.EsReindexMapper;
import com.es.plus.web.pojo.EsReindexTask;
import com.es.plus.web.pojo.EsindexDataMove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class EsReIndexService {
    
    @Autowired
    private EsReindexMapper esReindexMapper;
    
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
        EsPlusClientFacade sourceClient = ClientContext.getClient(esindexDataMove.getSourceClient());
        EsPlusClientFacade targetClient = ClientContext.getClient(esindexDataMove.getTargetClient());
        if (sourceClient == null || targetClient == null) {
            throw new EsException("来源或目标客户端为空");
        }
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
        EsResponse<Map> res = Es.chainQuery(sourceClient, Map.class).index(esindexDataMove.getSourceIndex())
                .sortByDesc("_id").search(esindexDataMove.getBatchSize());
        long total = res.getTotal();
        List<Map> list = res.getList();
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
        
        int count =0;
        if (list != null) {
            while (true) {
                if (CollectionUtils.isEmpty(list)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("taskProcess", currentTotalSize + "/" + total);
                    esReindexTask.setTaskJson(JsonUtils.toJsonStr(map));
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
                
                //每10次更新一次进度
                if (count % 5==0){
                    Map<String, Object> map = new HashMap<>();
                    map.put("taskProcess", currentTotalSize + "/" + total);
                    esReindexTask.setTaskJson(JsonUtils.toJsonStr(map));
                    reindexTaskUpdate(esReindexTask);
                }
                
                // 执行保存
                Es.chainUpdate(targetClient, Map.class).index(esindexDataMove.getTargetIndex()).saveOrUpdateBatch(list);
             
                
                Object[] tailSortValues = res.getTailSortValues();
                log.info("Es-plus 跨集群迁移 batchId:{} 本次同步数据 :{} 本次尾部数据:{}", batchId, list.size(),
                        tailSortValues);
                
                list.clear();
                res = Es.chainQuery(sourceClient, Map.class).index(esindexDataMove.getSourceIndex()).sortByDesc("_id")
                        .searchAfterValues(tailSortValues).search(esindexDataMove.getBatchSize());
                list = res.getList();
                try {
                    Thread.sleep(esindexDataMove.getSleepTime());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        log.info("Es-plus 跨集群迁移  同步所有数据完成 batchId:{} 总数:{}", batchId, currentTotalSize);
    }
}
