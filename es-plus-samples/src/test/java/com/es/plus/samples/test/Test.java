package com.es.plus.samples.test;

import com.es.plus.constant.EsSettingsConstants;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.dto.SamplesNestedDTO;
import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = SamplesApplication.class)
public class Test {
    @Autowired
    private SamplesEsService samplesEsService;

    @org.junit.jupiter.api.Test
    public void testSave() {
        SamplesEsDTO samplesEsDTO = new SamplesEsDTO();
        samplesEsDTO.setEmail("bbbberretertrebbbb");
        samplesEsDTO.setUsername("ggghhh");
        SamplesNestedDTO samplesNestedDTO = new SamplesNestedDTO();
        samplesNestedDTO.setEmail("gdfgdf");
        samplesEsDTO.setSamplesNesteds(samplesNestedDTO);
        samplesEsDTO.setSamplesNesteds(samplesNestedDTO);
        samplesEsDTO.setId(6L);
        samplesEsService.save(samplesEsDTO);
    }


    @org.junit.jupiter.api.Test
    public void testSearch() {
        samplesEsService.nested();
    }

    @org.junit.jupiter.api.Test
    public void agg() {
        samplesEsService.agg();
    }

    @org.junit.jupiter.api.Test
    public void search() {
        samplesEsService.search();
    }

    @org.junit.jupiter.api.Test
    public void test() {
        samplesEsService.test();
    }

    @org.junit.jupiter.api.Test
    public void scroll() {
        samplesEsService.scroll();
    }

    @org.junit.jupiter.api.Test
    public void count() {
        samplesEsService.count();
    }


    @org.junit.jupiter.api.Test
    public void profile() {
        samplesEsService.profile1();
    }

    @org.junit.jupiter.api.Test
    public void newUpdate() {
        samplesEsService.update();
    }

    @org.junit.jupiter.api.Test
    public void updateSettingsLog() {
        Map<String,Object> map=new HashMap<>();
        map.put(EsSettingsConstants.QUERY_INFO,"0s");
        map.put(EsSettingsConstants.QUERY_WARN,"0s");
        map.put(EsSettingsConstants.SEARCH_LEVEL,"info");
        samplesEsService.updateSettings(map);
    }

    @org.junit.jupiter.api.Test
    public void newSelect() {
        samplesEsService.newSelect();
    }

    @org.junit.jupiter.api.Test
    public void searhAfter() {
        samplesEsService.searhAfter();
    }


    @org.junit.jupiter.api.Test
    public void listLandList() {
        samplesEsService.listLandList();
    }
}
