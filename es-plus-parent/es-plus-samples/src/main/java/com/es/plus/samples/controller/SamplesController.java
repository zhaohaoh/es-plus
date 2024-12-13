package com.es.plus.samples.controller;

import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.service.FastTestService;
import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("sample")
public class SamplesController {
    @Autowired
    private SamplesEsService samplesEsService;
    @Autowired
    private FastTestService fastTestService;
    @PostMapping("agg")
    public void agg() {
        samplesEsService.agg();
    }
    @PostMapping("search")
    public void search() {
        samplesEsService.search();
    }
    @PostMapping("saveOrUpdateAsync2222")
    public void saveOrUpdateAsync222() {
        SamplesEsDTO samplesEsDTO = new SamplesEsDTO();
        samplesEsDTO.setId(1L);
        samplesEsDTO.setEmail("hhhjhj");
        samplesEsService.saveBatchAsyncProcessor(Collections.singletonList(samplesEsDTO));
    }
    
    @PostMapping("fastTestService")
    public void fastTestService() {
        fastTestService.test();
    }
    
    @PostMapping("save")
    public void save() {
        fastTestService.save();
    }
    
    @PostMapping("saveAsync")
    public void saveAsync() {
        fastTestService.saveAsync();
    }
    
    @PostMapping("updateAsync")
    public void updateAsync() {
        fastTestService.updateAsync();
    }
    
    @PostMapping("saveOrUpdateAsync")
    public void saveOrUpdateAsync() {
        fastTestService.saveOrUpdateAsync();
    }
    
    @PostMapping("delete")
    public void delete() {
        fastTestService.delete();
    }
    @PostMapping("update")
    public void update() {
        fastTestService.update();
    }
    
    @PostMapping("updateBy")
    public void updateBy() {
        fastTestService.updateBy();
    }
}
