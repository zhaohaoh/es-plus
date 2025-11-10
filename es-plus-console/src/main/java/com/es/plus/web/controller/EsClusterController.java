package com.es.plus.web.controller;

import com.es.plus.web.service.EsRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/es/cluster")
public class EsClusterController {
    
    @Autowired
    private EsRestService esRestService;
    
    /**
     * getNodes
     */
    @GetMapping("getNodes")
    public String getNodes(@RequestHeader("currentEsClient") String esClientName) {
        log.debug("获取ES集群节点信息: {}", esClientName);
        try {
            return esRestService.getNodes(esClientName);
        } catch (Exception e) {
            log.error("获取ES集群节点信息失败: esClient={}", esClientName, e);
            throw e;
        }
    }
}