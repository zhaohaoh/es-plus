package com.es.plus.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.autoconfigure.util.Client8Util;
import com.es.plus.autoconfigure.util.ClientUtil;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/es/client")
public class EsClientController {
    
    @Autowired
    private EsClientMapper esClientMapper;
    
    
    @PostMapping("list")
    public List<EsClientProperties> list() {
        List<EsClientProperties> esClientProperties = esClientMapper.selectList(Wrappers.lambdaQuery());
        return esClientProperties;
    }
    
    @GetMapping("get")
    public EsClientProperties get(Long id) {
        EsClientProperties esClientProperties = esClientMapper.selectOne(
                Wrappers.<EsClientProperties>lambdaQuery().eq(EsClientProperties::getId,id));
        return esClientProperties;
    }
    
    @PostMapping("save")
    public void save(@RequestBody EsClientProperties esClientProperties) {
        try {
            List<HttpHost> hostList = new ArrayList<>();
            String address = esClientProperties.getAddress();
            Arrays.stream(address.split(",")).forEach(item -> hostList.add(new HttpHost(item.split(":")[0],
                    Integer.parseInt(StringUtils.substringAfterLast(item,":")), esClientProperties.getSchema())));
        } catch (Exception e) {
            throw new RuntimeException("地址格式有误");
        }
        LambdaQueryWrapper<EsClientProperties> eq = Wrappers.<EsClientProperties>lambdaQuery()
                .eq(EsClientProperties::getUnikey, esClientProperties.getUnikey());
        EsClientProperties properties = esClientMapper.selectOne(eq);
        if (properties != null){
            BeanUtils.copyProperties(esClientProperties,properties);
            esClientMapper.updateById(properties);
        }else{
            esClientMapper.insert(esClientProperties);
        }
        
        ClientProperties clientProperties = new ClientProperties();
        BeanUtils.copyProperties(esClientProperties, clientProperties);
        Object client;
        if (esClientProperties.getVersion().equals("8")){
            client = Client8Util.getElasticsearchClient(clientProperties);
        }else {
            client = ClientUtil.getRestHighLevelClient(clientProperties);
        }
        String address = clientProperties.getAddress();
        address = StringUtils.replace(address,"http://","");
        address = StringUtils.replace(address,"https://","");
        clientProperties.setAddress(address);
        
        EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(clientProperties.getAddress(),
                client,
                null,Integer.parseInt(esClientProperties.getVersion()));
        ClientContext.addClient(esClientProperties.getUnikey(), esPlusClientFacade);
    }
    
    @PostMapping("testClient")
    public boolean testClient(@RequestBody EsClientProperties esClientProperties) {
        
        ClientProperties clientProperties = new ClientProperties();
        BeanUtils.copyProperties(esClientProperties, clientProperties);
        Object client;
        if (esClientProperties.getVersion().equals("8")){
            client = Client8Util.getElasticsearchClient(clientProperties);
        }else {
            client = ClientUtil.getRestHighLevelClient(clientProperties);
        }
        String address = clientProperties.getAddress();
        address = StringUtils.replace(address,"http://","");
        address = StringUtils.replace(address,"https://","");
        clientProperties.setAddress(address);
        
        EsPlusClientFacade esPlusClientFacade = ClientContext.buildEsPlusClientFacade(clientProperties.getAddress(),
                client,
                null,Integer.parseInt(esClientProperties.getVersion()));
        ClientContext.addClient(esClientProperties.getUnikey(), esPlusClientFacade);
        boolean ping = Es.chainIndex(esPlusClientFacade).ping();
        if (!ping){
            ClientContext.removeClient(esClientProperties.getUnikey());
        }
        return ping;
    }
    
    @DeleteMapping("delete")
    public void delete(Long id) {
        esClientMapper.deleteById(id);
        
    }
}