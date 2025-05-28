package com.es.plus.web.controller;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.exception.EsException;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.web.cache.EsClientCache;
import com.es.plus.web.pojo.EsIndexCreateDTO;
import com.es.plus.web.pojo.EsIndexResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@RestController
@RequestMapping("/es/index")
public class EsIndexController {
    
    /**
     * 根据当前es链接地址  模糊查询所有索引名
     *
     * @param keyword
     * @return
     */
    @GetMapping("list")
    public EsIndexResponseVO list(@RequestHeader("currentEsClient") String esClientName, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            String present = EsClientCache.CACHE_MAP.getIfPresent(keyword);
            if (present != null) {
                EsIndexResponseVO bean = JsonUtils.toBean(present, EsIndexResponseVO.class);
                return bean;
            }
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse index = Es.chainIndex(client).getIndex("*" + keyword + "*");
        EsIndexResponseVO esIndexResponseVO = new EsIndexResponseVO();
        BeanUtils.copyProperties(index, esIndexResponseVO);
        Map<String, Object> mappings = index.getMappings();
        
        //boolean存字段类型是否是字符串
        Map<String, Map<String, String>> flatMappings = new HashMap<>();
        mappings.forEach((k, v) -> {
            Map<String, String> arrayList = new HashMap<>();
            Map map = (Map) v;
            Map properties = (Map) map.get("properties");
            if (properties == null) {
                return;
            }
            properties.forEach((key, value) -> {
                
                Map valueMap = (Map) value;
                Object object = valueMap.get("fields");
                String type = (String) valueMap.get("type");
                arrayList.put((String) key, type);
                if (object != null) {
                    arrayList.put(key + ".keyword", "keyword");
                }
            });
            flatMappings.put(k, arrayList);
        });
        esIndexResponseVO.setFlatMappings(flatMappings);
        
        EsClientCache.CACHE_MAP.put(keyword, Objects.requireNonNull(JsonUtils.toJsonStr(esIndexResponseVO)));
        
        return esIndexResponseVO;
    }
    
    
    /**
     * 获取指定索引映射和配置信息
     */
    @GetMapping("getIndex")
    public EsIndexResponse getIndex(@RequestHeader("currentEsClient") String esClientName, String index) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse indexResponse = Es.chainIndex(client).getIndex(index);
        Map<String, Object> setting = indexResponse.getSetting(indexResponse.getIndices()[0]);
        indexResponse.setSettingsObj(setting);
        indexResponse.setSettings(null);
        return indexResponse;
    }
    
    
    /**
     * 获取指定索引统计信息
     */
    @GetMapping("getIndexStat")
    public String getIndexStat(@RequestHeader("currentEsClient") String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexStat();
        return indexResponse;
    }
    
    /**
     * 获取指定索引统计信息
     */
    @GetMapping("getIndexHealth")
    public String getIndexHealth(@RequestHeader("currentEsClient") String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexHealth();
        return indexResponse;
    }
    
    /**
     * getNodes
     */
    @GetMapping("getNodes")
    public String getNodes(@RequestHeader("currentEsClient") String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String res = Es.chainIndex(client).getNodes();
        return res;
    }
    
    /**
     * getNodes
     */
    @GetMapping("getIndices")
    public String getIndices(@RequestHeader("currentEsClient") String esClientName, String keyword) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String string = "*" + keyword + "*";
        String cmd = "/_cat/indices/" + string + "?format=json&v";
        String res = Es.chainIndex(client).getCmd(cmd);
        EsIndexResponseVO responseVO = list(esClientName, keyword);
        Map<String, List<String>> aliases = responseVO.getAliases();
        List<Map> list = JsonUtils.toList(res, Map.class);
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
        return res;
    }
    
    /**
     * putMapping
     */
    @PostMapping("putMapping")
    public void putMapping(@RequestHeader("currentEsClient") String esClientName, String indexName, String mappings) {
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("索引不能为空");
        }
        if (StringUtils.isBlank(mappings)) {
            throw new RuntimeException("mappings不能为空");
        }
        Map<String, Object> map = JsonUtils.toMap(mappings);
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).index(indexName).putMapping(map);
    }
    
    /**
     * getMapping
     */
    @GetMapping("getMapping")
    public String getMapping(@RequestHeader("currentEsClient") String esClientName, String indexName) {
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("索引不能为空");
        }
        
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse mappings = Es.chainIndex(client).getIndex(indexName);
        Map<String, Object> mapping = mappings.getMappings();
        Object object = mapping.get(indexName);
        if (object == null) {
            return "";
        }
        return JsonUtils.toJsonStr(object);
    }
    
    /**
     * 创建索引
     */
    @PostMapping("createIndex")
    public void createIndex(@RequestHeader("currentEsClient") String esClientName,
            @RequestBody EsIndexCreateDTO esIndexCreateDTO) {
        if (StringUtils.isBlank(esIndexCreateDTO.getIndexName())) {
            throw new EsException("索引名不能为空");
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).createIndex(esIndexCreateDTO.getIndexName(),esIndexCreateDTO.getAlias()
                ,esIndexCreateDTO.getEsSettings(),esIndexCreateDTO.getMapping());
        refreshIndexCache(esClientName);
    }
    
    /**
     * 设置别名
     */
    @PostMapping("createAlias")
    public void createAlias(@RequestHeader("currentEsClient") String esClientName,String index,String alias) {
        if (StringUtils.isBlank(index)||StringUtils.isBlank(alias)) {
            throw new EsException("索引或别名不能为空");
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).createAlias(index,alias);
        refreshIndexCache(esClientName);
    }
    
    /**
     * 删除别名
     */
    @PostMapping("removeAlias")
    public void removeAlias(@RequestHeader("currentEsClient") String esClientName,String index,String alias) {
        if (StringUtils.isBlank(index)||StringUtils.isBlank(alias)) {
            throw new EsException("索引或别名不能为空");
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).removeAlias(index,alias);
        refreshIndexCache(esClientName);
    }
    
    /**
     * 刷新索引缓存
     */
    @PostMapping("refreshIndexCache")
    public void refreshIndexCache(@RequestHeader("currentEsClient") String esClientName) {
        EsClientCache.CACHE_MAP.invalidateAll();
    }
    
    
    /**
     * 删除索引
     */
    @DeleteMapping("deleteIndex")
    public void deleteIndex(@RequestHeader("currentEsClient") String esClientName, String indexName) {
        if (StringUtils.isBlank(indexName)) {
            throw new RuntimeException("需要删除的索引不能为空");
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).deleteIndex(indexName);
        refreshIndexCache(esClientName);
    }
    
    
}
