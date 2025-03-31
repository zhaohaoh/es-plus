package com.es.plus.web.controller;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<String> list(String esClientName,String keyword) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse index = Es.chainIndex(client).getIndex("*" + keyword + "*");
        return Arrays.asList(index.getIndices());
    }
    
    
    /**
     *  获取指定索引映射和配置信息
     */
    @GetMapping("getIndex")
    public EsIndexResponse getIndex(String esClientName,String index) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        EsIndexResponse indexResponse = Es.chainIndex(client).getIndex(index);
        return indexResponse;
    }
    
    
    
    /**
     *  获取指定索引统计信息
     */
    @GetMapping("getIndexStat")
    public String getIndexStat(String esClientName,String index) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexStat(index);
        return indexResponse;
    }
    
    /**
     *  获取指定索引统计信息
     */
    @GetMapping("getIndexHealth")
    public String getIndexHealth(String esClientName,String index) {
        EsPlusClientFacade client = ClientContext.getClient(esClientName);
        String indexResponse = Es.chainIndex(client).getIndexHealth(index);
        return indexResponse;
    }
    
}
