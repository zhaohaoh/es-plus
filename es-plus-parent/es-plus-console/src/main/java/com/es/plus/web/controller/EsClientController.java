package com.es.plus.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.autoconfigure.util.ClientUtil;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            throw new RuntimeException("已经存在重复的数据");
        }
        
        esClientMapper.insert(esClientProperties);
        ClientProperties clientProperties = new ClientProperties();
        BeanUtils.copyProperties(esClientProperties, clientProperties);
        ClientUtil.initAndPutEsPlusClientFacade(esClientProperties.getName(), clientProperties, null);
    }
    
    //    @PostMapping("update")
    //    public void update(@RequestBody EsClientProperties esClientProperties) {
    //          esClientMapper.insert(esClientProperties);
    //    }
    
    @DeleteMapping("delete")
    public void delete(Long id) {
        esClientMapper.deleteById(id);
        //重启就没了，就不做删除链接了
    }
}
