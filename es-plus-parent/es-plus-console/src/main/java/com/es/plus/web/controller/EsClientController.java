package com.es.plus.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
         esClientMapper.insert(esClientProperties);
    }
    
    @PostMapping("update")
    public void update(@RequestBody EsClientProperties esClientProperties) {
          esClientMapper.insert(esClientProperties);
    }
    
    @PostMapping("delete")
    public void delete(Long id) {
         esClientMapper.deleteById(id);
    }
}
