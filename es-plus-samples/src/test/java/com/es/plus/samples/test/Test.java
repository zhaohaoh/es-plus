package com.es.plus.samples.test;

import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.dto.SamplesNestedDTO;
import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SamplesApplication.class)
public class Test {
    @Autowired
    private SamplesEsService samplesEsService;

    @org.junit.jupiter.api.Test
    public void testSave() {
        SamplesEsDTO samplesEsDTO = new SamplesEsDTO();
        samplesEsDTO.setEmail("abc");
        samplesEsDTO.setUsername("hzh");
        SamplesNestedDTO samplesNestedDTO = new SamplesNestedDTO();
        samplesNestedDTO.setEmail("abc");
        samplesEsDTO.setSamplesNesteds(samplesNestedDTO);
        samplesEsDTO.setSamplesNesteds(samplesNestedDTO);
        samplesEsDTO.setId(1L);
        samplesEsService.save(samplesEsDTO);
    }


    @org.junit.jupiter.api.Test
    public void testSearch() {
        samplesEsService.nested();
    }


    @org.junit.jupiter.api.Test
    public void search() {
        samplesEsService.search();
    }

    @org.junit.jupiter.api.Test
    public void profile() {
        samplesEsService.profile1();
    }
}
