package com.es.plus.samples.test;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.constant.EsSettingsConstants;
import com.es.plus.core.statics.Es;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.dto.SamplesNestedDTO;
import com.es.plus.samples.dto.SamplesNestedInnerDTO;
import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest(classes = SamplesApplication.class)
public class Test {

    @Autowired
    private SamplesEsService samplesEsService;

    @org.junit.jupiter.api.Test
    public void testSave() {
        SamplesEsDTO samplesEsDTO = new SamplesEsDTO();
        samplesEsDTO.setEmail("bbbberretertrebbbb");
        samplesEsDTO.setUsername("ggghhh");
        samplesEsDTO.setDate(new Date());
        List<SamplesNestedDTO> samplesNestedDTOS=new ArrayList<>();
        SamplesNestedDTO samplesNestedDTO = new SamplesNestedDTO();
        samplesNestedDTO.setEmail("3");
        samplesNestedDTO.setUsername("3");
        samplesNestedDTO.setId(1L);
        samplesNestedDTO.setState(false);
        samplesNestedDTOS.add(samplesNestedDTO);
        SamplesNestedDTO samplesNestedDTO1 = new SamplesNestedDTO();
        samplesNestedDTO1.setEmail("4");
        samplesNestedDTO1.setUsername("4");
        samplesNestedDTO1.setId(2L);
        samplesNestedDTO1.setState(true);
        samplesNestedDTOS.add(samplesNestedDTO1);
        samplesEsDTO.setSamplesNesteds(samplesNestedDTOS);
        samplesEsDTO.setId(1L);

        List<SamplesNestedInnerDTO> samplesNestedss=new ArrayList<>();
        for (int i = 0; i <500; i++) {
            SamplesNestedInnerDTO samplesNestedInnerDTO = new SamplesNestedInnerDTO();
            samplesNestedInnerDTO.setEmail("3");
            samplesNestedInnerDTO.setUsername("3");
            samplesNestedInnerDTO.setId((long)i);
            samplesNestedInnerDTO.setState(true);
            samplesNestedss.add(samplesNestedInnerDTO);
        }
        samplesNestedDTO.setSamplesNestedInner(samplesNestedss);
        samplesEsService.save(samplesEsDTO);
//        Map<String,Object> map=new HashMap<>();
//        map.put("usrname","adddbbbb");
//        map.put("sex","2");
//        map.put("id","111");
//    Es.chainUpdate(Map.class).index("sys_user2ttt_test_s0").save(map);
    }

    @org.junit.jupiter.api.Test
    public void fast() {
        EsResponse<Map> list = Es.chainQuery(Map.class).index("sys_user2ttt_s0").list();
        System.out.println(list);
    }
    @org.junit.jupiter.api.Test
    public void fastCreateIndex() {
        //没有指定索引的话会取Class中的索引
        Es.chainIndex().createIndex(FastTestDTO.class).putMapping(FastTestDTO.class);
    }

    @org.junit.jupiter.api.Test
    public void fastSave() {
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(1L);
        fastTestDTO.setText("我的个人介绍 我是一篇文章，用于搜索。我的关键词有很多。苹果 梨子 苹果X2 苹果哥哥");
        fastTestDTO.setAge(25);
        fastTestDTO.setUsername("酷酷的");
        fastTestDTO.setCreateTime(new Date());
        Es.chainUpdate(FastTestDTO.class).save(fastTestDTO);
    }

    @org.junit.jupiter.api.Test
    public void fastSearch() {
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class).match(FastTestDTO::getText, "苹果").list();
        System.out.println(test);
    }




    @org.junit.jupiter.api.Test
    public void nested() {

        boolean ss = Es.chainIndex().index("ss").indexExists();
        System.out.println(ss);
//        samplesEsService.nested();
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
    public void updateByQuery() {
        samplesEsService.updateByQuery();
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
