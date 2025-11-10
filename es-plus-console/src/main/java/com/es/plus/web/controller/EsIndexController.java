package com.es.plus.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.web.cache.EsClientCache;
import com.es.plus.web.config.EsException;
import com.es.plus.web.ext.EsPlusGetTaskResponse;
import com.es.plus.web.util.JsonUtils;
import com.es.plus.web.mapper.EsReindexMapper;
import com.es.plus.web.pojo.*;
import com.es.plus.web.service.EsReIndexService;
import com.es.plus.web.service.EsRestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/es/index")
public class EsIndexController {
    
    @Autowired
    private EsReindexMapper esReindexMapper;
    
    @Autowired
    private EsReIndexService esIndexService;
    
    @Autowired
    private EsRestService esRestService;
    
    /**
     * 根据当前es链接地址  模糊查询所有索引名
     *
     * @param keyword
     * @return
     */
    @GetMapping("list")
    public EsIndexResponseVO list(@RequestHeader("currentEsClient") String esClientName, String keyword) {
        log.debug("获取索引列表: clientKey={}, keyword={}", esClientName, keyword);
        
        String pattern = "*" + keyword + "*";
        String response = esRestService.getIndices(esClientName, pattern);
        
        if (response.contains("\"error\"")) {
            throw new EsException("无匹配的索引");
        }
        
        // 解析响应为EsIndexResponseVO
        return JsonUtils.toBean(response, EsIndexResponseVO.class);
    }
    
    
    /**
     * 获取指定索引映射和配置信息
     * 注意：原方法返回EsIndexResponse，改为HTTP方式后返回String格式的JSON响应
     * 响应格式保持与原来兼容，包含索引的映射和设置信息
     */
    @GetMapping("getIndex")
    public String getIndex(@RequestHeader("currentEsClient") String esClientName, String index) {
        log.debug("获取索引信息: clientKey={}, index={}", esClientName, index);
        
        String response = esRestService.getIndex(esClientName, index);
        
        if (response.contains("\"error\"")) {
            throw new EsException("获取索引信息失败");
        }
        
        // 构建与原来EsIndexResponse格式一致的响应
        return buildIndexResponseWithSettings(response, index);
    }
    
    
    /**
     * 获取指定索引统计信息
     */
    @GetMapping("getIndexStat")
    public String getIndexStat(@RequestHeader("currentEsClient") String esClientName) {
        log.debug("获取索引统计: clientKey={}", esClientName);
        return esRestService.getIndexStats(esClientName, "*");
    }
    
    /**
     * 获取指定索引健康状态
     */
    @GetMapping("getIndexHealth")
    public String getIndexHealth(@RequestHeader("currentEsClient") String esClientName) {
        log.debug("获取索引健康状态: clientKey={}", esClientName);
        return esRestService.getIndexHealth(esClientName);
    }
    
    
    /**
     * 获取索引详细信息列表（带状态、文档数量等）
     */
    @GetMapping("getIndices")
    public String getIndices(@RequestHeader("currentEsClient") String esClientName, String keyword) {
        log.debug("获取索引详情: clientKey={}, keyword={}", esClientName, keyword);
        
        String pattern = "*" + keyword + "*";
        String cmd = "/_cat/indices/" + pattern + "?format=json&v";
        
        // 直接调用ES API获取索引详细信息
        String res = esRestService.getCmd(esClientName, cmd);
        
        if (res == null) {
            throw new EsException("无匹配索引");
        }
        
        // 获取别名信息并合并到响应中
        try {
            List<Map> list = JsonUtils.toList(res, Map.class);
            
            // 获取索引列表对应的别名信息（通过调用list方法获取EsIndexResponseVO）
            EsIndexResponseVO responseVO = list(esClientName, keyword);
            Map<String, List<String>> aliases = responseVO.getAliases();
            
            for (Map map : list) {
                String index = String.valueOf(map.get("index"));
                List<String> alias = aliases.get(index);
                if (!CollectionUtils.isEmpty(alias)) {
                    StringJoiner value = new StringJoiner(",");
                    for (String s : alias) {
                        value.add(s);
                    }
                    map.put("alias", value.toString());
                }
            }
            
            res = JsonUtils.toJsonStr(list);
        } catch (Exception e) {
            log.error("处理别名信息失败", e);
        }
        
        return res;
    }
    
    
    /**
     * updateSettings
     */
    @PostMapping("updateSettings")
    public String updateSettings(@RequestHeader("currentEsClient") String esClientName, String indexName, String settings) {
        log.debug("更新索引设置: clientKey={}, index={}", esClientName, indexName);
        
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("索引不能为空");
        }
        if (StringUtils.isBlank(settings)) {
            throw new RuntimeException("settings不能为空");
        }
        
        return esRestService.updateSettings(esClientName, indexName, settings);
    }
    
    /**
     * putMapping
     */
    @PostMapping("putMapping")
    public String putMapping(@RequestHeader("currentEsClient") String esClientName, String indexName, String mappings) {
        log.debug("更新索引映射: clientKey={}, index={}", esClientName, indexName);
        
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("索引不能为空");
        }
        if (StringUtils.isBlank(mappings)) {
            throw new RuntimeException("mappings不能为空");
        }
        
        return esRestService.putMapping(esClientName, indexName, mappings);
    }
    
    /**
     * getMapping
     */
    @GetMapping("getMapping")
    public String getMapping(@RequestHeader("currentEsClient") String esClientName, String indexName) {
        log.debug("获取索引映射: clientKey={}, index={}", esClientName, indexName);
        
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("索引不能为空");
        }
        
        String response = esRestService.getMapping(esClientName, indexName);
        
        // 解析响应，提取特定索引的映射
        try {
            Map<String, Object> mapping = JsonUtils.toMap(response);
            Object indexMapping = mapping.get(indexName);
            if (indexMapping == null) {
                return "";
            }
            return JsonUtils.toJsonStr(indexMapping);
        } catch (Exception e) {
            log.error("解析映射响应失败", e);
            return response;
        }
    }
    
    /**
     * 创建索引
     */
    @PostMapping("createIndex")
    public String createIndex(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsIndexCreateDTO esIndexCreateDTO) {
        log.debug("创建索引: clientKey={}, index={}", esClientName, esIndexCreateDTO.getIndexName());
        
        if (StringUtils.isBlank(esIndexCreateDTO.getIndexName())) {
            throw new EsException("索引名不能为空");
        }
        
        return esRestService.createIndex(
                esClientName,
                esIndexCreateDTO.getIndexName(),
                esIndexCreateDTO.getAlias(),
                JsonUtils.toJsonStr(esIndexCreateDTO.getEsSettings()),
                JsonUtils.toJsonStr(esIndexCreateDTO.getMapping())
        );
    }
    
    /**
     * 设置别名
     */
    @PostMapping("createAlias")
    public String createAlias(@RequestHeader("currentEsClient") String esClientName, String index, String alias) {
        log.debug("创建别名: clientKey={}, index={}, alias={}", esClientName, index, alias);
        
        if (StringUtils.isBlank(index) || StringUtils.isBlank(alias)) {
            throw new EsException("索引或别名不能为空");
        }
        
        return esRestService.createAlias(esClientName, index, alias);
    }
    
    /**
     * 删除别名
     */
    @PostMapping("removeAlias")
    public String removeAlias(@RequestHeader("currentEsClient") String esClientName, String index, String alias) {
        log.debug("删除别名: clientKey={}, index={}, alias={}", esClientName, index, alias);
        
        if (StringUtils.isBlank(index) || StringUtils.isBlank(alias)) {
            throw new EsException("索引或别名不能为空");
        }
        
        Object loginId = StpUtil.getLoginId();
        if (!loginId.equals("1")) {
            throw new RuntimeException("无权操作");
        }
        
        return esRestService.removeAlias(esClientName, index, alias);
    }
    
    
    /**
     * 复制索引 复制索引配置和映射 不复制别名
     */
    @PostMapping("copyIndex")
    public void copyIndex(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsCopyRequest esCopyRequest) {
        esCopyRequest.setSourceClient(esClientName);
        String sourceIndex = esCopyRequest.getSourceIndex();
        String targetIndex = esCopyRequest.getTargetIndex();
        String targetClient = esCopyRequest.getTargetClient();
        
        if (StringUtils.isBlank(sourceIndex) || StringUtils.isBlank(targetIndex)) {
            throw new EsException("索引不能为空");
        }
        
        try {
            // 使用HTTP方式获取源索引信息
            String indexResponse = esRestService.getIndex(esCopyRequest.getSourceClient(), sourceIndex);
            
            // 解析索引响应获取映射和设置
            Map<String, Object> indexInfo = JsonUtils.toMap(indexResponse);
            Map<String, Object> sourceIndexInfo = (Map<String, Object>) indexInfo.get(sourceIndex);
            if (sourceIndexInfo == null) {
                throw new EsException("获取源索引信息失败");
            }
            
            Map<String, Object> mappings = (Map<String, Object>) sourceIndexInfo.get("mappings");
            Map<String, Object> settings = (Map<String, Object>) sourceIndexInfo.get("settings");
            if (settings != null && settings.containsKey("index")) {
                Map<String, Object> indexSettings = (Map<String, Object>) settings.get("index");
                // 移除index前缀以匹配原始逻辑
                Map<String, Object> cleanSettings = new HashMap<>();
                for (Map.Entry<String, Object> entry : indexSettings.entrySet()) {
                    String key = entry.getKey().replaceFirst("^index\\.", "");
                    cleanSettings.put(key, entry.getValue());
                }
                settings = cleanSettings;
            }
            
            // 使用HTTP方式在目标客户端创建索引（不复制别名）
            String settingsJson = JsonUtils.toJsonStr(settings);
            esRestService.createIndex(targetClient, targetIndex, null, settingsJson, JsonUtils.toJsonStr(mappings));
            
        } catch (Exception e) {
            log.error("复制索引失败: source={}, target={}", sourceIndex, targetIndex, e);
            throw new EsException("复制索引失败: " + e.getMessage());
        }
    }
    
    /**
     * reindex
     */
    @PostMapping("reindex")
    public String reindex(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsReindexRequst esReindexRequst) {
        String sourceIndex = esReindexRequst.getSourceIndex();
        String targetIndex = esReindexRequst.getTargetIndex();
        if (StringUtils.isBlank(sourceIndex) || StringUtils.isBlank(targetIndex)) {
            throw new EsException("索引不能为空");
        }
        
        Object loginId = StpUtil.getLoginId();
        if (!loginId.equals("1")) {
            throw new RuntimeException("无权操作");
        }
        
        // 使用HTTP方式创建reindex任务
        String taskId = esRestService.reindex(esClientName, sourceIndex, targetIndex);
        
        refreshIndexCache(esClientName);
        EsReindexTask esReindexTask = new EsReindexTask();
        esReindexTask.setTaskId(taskId);
        esReindexTask.setSourceIndex(sourceIndex);
        esReindexTask.setTargetIndex(targetIndex);
        esReindexTask.setCreateTime(System.currentTimeMillis());
        esReindexTask.setCreateUid(StpUtil.getLoginId() != null ? Long.parseLong(StpUtil.getLoginId().toString()) : 0);
        esReindexTask.setSourceClient(esClientName);
        esReindexTask.setTargetClient(esClientName);
        esReindexTask.setType(1);
        esIndexService.reindexTaskInsert(esReindexTask);
        
        return taskId;
    }
    
    /**
     * reindex任务明细列表
     */
    @GetMapping("reindexTaskList")
    public List<EsReindexTaskVO> reindexTaskList(@RequestHeader("currentEsClient") String esClientName,
            String sourceIndex) {
        LambdaQueryWrapper<EsReindexTask> eq = Wrappers.<EsReindexTask>lambdaQuery()
                .eq(EsReindexTask::getSourceClient, esClientName).eq(EsReindexTask::getSourceIndex, sourceIndex)
                //只查看前10个
                .last("limit 10").orderByDesc(EsReindexTask::getCreateTime);
        List<EsReindexTask> esReindexTasks = esReindexMapper.selectList(eq);
        List<EsReindexTaskVO> reindexTaskVOS = esReindexTasks.stream().map(a -> {
            EsReindexTaskVO esReindexTaskVO = new EsReindexTaskVO();
            BeanUtils.copyProperties(a, esReindexTaskVO);
            if (a.getType() == null || a.getType() == 1) {
                EsPlusGetTaskResponse string = reindexTaskGet(esClientName, esReindexTaskVO.getTaskId());
                if (string != null) {
                    boolean completed = string.isCompleted();
                    esReindexTaskVO.setTaskJson(string.getTaskInfo());
                    esReindexTaskVO.setCompleted(completed);
                }
            }
            return esReindexTaskVO;
        }).collect(Collectors.toList());
        return reindexTaskVOS;
    }
    
    /**
     * reindex任务明细获取
     */
    @GetMapping("reindexTaskGet")
    public EsPlusGetTaskResponse reindexTaskGet(@RequestHeader("currentEsClient") String esClientName, String taskId) {
        
        // 使用HTTP方式获取任务状态
        String response = esRestService.getTaskStatus(esClientName, taskId);
        
        // 解析响应并构建EsPlusGetTaskResponse对象
        try {
            Map<String, Object> taskInfo = JsonUtils.toMap(response);
            EsPlusGetTaskResponse esPlusGetTaskResponse = new EsPlusGetTaskResponse();
            
            Map<String, Object> task = (Map<String, Object>) taskInfo.get("task");
            if (task != null) {
                esPlusGetTaskResponse.setTaskInfo(JsonUtils.toJsonStr(task));
                String action = (String) task.get("action");
                String status = (String) task.get("status");
                // 判断任务是否完成：reindex且status为completed
                boolean completed = "reindex".equals(action) && "completed".equals(status);
                esPlusGetTaskResponse.setCompleted(completed);
            }
            
            return esPlusGetTaskResponse;
        } catch (Exception e) {
            log.error("解析任务状态失败: taskId={}", taskId, e);
            throw new EsException("获取任务状态失败");
        }
    }
    
    /**
     * 跨集群数据迁移
     */
    @PostMapping("indexDataMove")
    public String indexDataMove(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsindexDataMove esindexDataMove) {
        esindexDataMove.setSourceClient(esClientName);
        esIndexService.indexDataMove(esindexDataMove);
        return "";
    }
    
    
    /**
     * reindex任务明细获取
     */
    @PostMapping("indexDataMoveStop")
    public String indexDataMoveStop(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsindexDataMove esindexDataMove) {
        
        return "";
    }
    
    /**
     * 刷新索引缓存
     */
    @PostMapping("refreshIndexCache")
    public void refreshIndexCache(@RequestHeader("currentEsClient") String esClientName) {
        EsClientCache.CACHE_MAP.invalidateAll();
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 构建包含settings的索引响应
     */
    private String buildIndexResponseWithSettings(String esResponse, String indexName) {
        try {
            // 解析ES API响应
            Map<String, Object> responseMap = JsonUtils.toMap(esResponse);
            Map<String, Object> indexInfo = (Map<String, Object>) responseMap.get(indexName);
            if (indexInfo == null) {
                return esResponse;
            }
            
            // 构建类似EsIndexResponse的格式
            Map<String, Object> result = new HashMap<>();
            result.put("mappings", indexInfo.get("mappings"));
            result.put("settings", indexInfo.get("settings"));
            result.put("aliases", indexInfo.get("aliases"));
            
            return JsonUtils.toJsonStr(result);
        } catch (Exception e) {
            log.error("构建索引响应失败", e);
            return esResponse;
        }
    }
    
    /**
     * 获取索引别名信息
     */
    private Map<String, List<String>> getAliasesForIndices(String esClientName, String[] indexNames) {
        Map<String, List<String>> aliasMap = new HashMap<>();
        try {
            if (indexNames == null || indexNames.length == 0) {
                return aliasMap;
            }
            
            // 构建逗号分隔的索引名
            String indices = String.join(",", indexNames);
            String aliasesResponse = esRestService.getAliases(esClientName, indices);
            
            // 解析别名响应 - _cat/aliases API 返回的是数组格式
            List<Map> aliasList = JsonUtils.toList(aliasesResponse, Map.class);
            for (Map alias : aliasList) {
                String index = (String) alias.get("index");
                String aliasName = (String) alias.get("alias");
                
                if (index != null && aliasName != null) {
                    aliasMap.computeIfAbsent(index, k -> new ArrayList<>()).add(aliasName);
                }
            }
        } catch (Exception e) {
            log.error("获取别名信息失败", e);
        }
        return aliasMap;
    }
    
    /**
     * 删除索引
     */
    @DeleteMapping("deleteIndex")
    public void deleteIndex(@RequestHeader("currentEsClient") String esClientName, String indexName) {
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("需要删除的索引不能为空");
        }
        Object loginId = StpUtil.getLoginId();
        if (!loginId.equals("1")) {
            throw new RuntimeException("无权操作");
        }
        
        // 使用HTTP方式删除索引
        esRestService.deleteIndex(esClientName, indexName);
        refreshIndexCache(esClientName);
    }
    
    
}