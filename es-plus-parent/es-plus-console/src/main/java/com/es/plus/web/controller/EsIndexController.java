package com.es.plus.web.controller;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/es/index")
public class EsIndexController {
    
    /**
     * 根据当前es链接地址  模糊查询所有索引名
     * @param keyword
     * @return
     */
    @GetMapping("list")
    public List<String> list(@RequestHeader("currentEsClient") String esClientName,String keyword) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse index = Es.chainIndex(client).getIndex("*" + keyword + "*");
        return Arrays.asList(index.getIndices());
    }
    
    
    /**
     *  获取指定索引映射和配置信息
     */
    @GetMapping("getIndex")
    public EsIndexResponse getIndex(@RequestHeader("currentEsClient") String esClientName,String index) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse indexResponse = Es.chainIndex(client).getIndex(index);
        return indexResponse;
    }
    
    
    
    /**
     *  获取指定索引统计信息
     */
    @GetMapping("getIndexStat")
    public String getIndexStat(@RequestHeader("currentEsClient") String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexStat();
        return indexResponse;
    }
    
    /**
     *  获取指定索引统计信息
     */
    @GetMapping("getIndexHealth")
    public String getIndexHealth(@RequestHeader("currentEsClient")  String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexHealth();
        return indexResponse;
    }
    /**
     *  getNodes
     */
    @GetMapping("getNodes")
    public String getNodes(@RequestHeader("currentEsClient") String esClientName) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String res = Es.chainIndex(client).getNodes();
        return res;
    }
    
    /**
     *  getNodes
     */
    @GetMapping("getIndices")
    public String getIndices(@RequestHeader("currentEsClient") String esClientName,String keyword) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String string = "*" + keyword + "*";
        String cmd = "/_cat/indices/" + string + "?format=json&v";
        return Es.chainIndex(client).getCmd(cmd);
    }
    
    /**
     *  删除索引
     */
    @DeleteMapping("deleteIndex")
    public void deleteIndex(@RequestHeader("currentEsClient") String esClientName,String indexName) {
        if (StringUtils.isBlank(indexName)){
            throw new RuntimeException("需要删除的索引不能为空");
        }
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        Es.chainIndex(client).deleteIndex(indexName);
    }
    
}
