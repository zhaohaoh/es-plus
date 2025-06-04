package com.es.plus.samples.test;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.statics.Es;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.DynamicIndexDTO;
import com.es.plus.samples.service.FastTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SamplesApplication.class)
public class DynamicTest {

    @Autowired
    private FastTestService fastTestService;

    @org.junit.jupiter.api.Test
    public void fast() {
        EsResponse<DynamicIndexDTO> list = Es.chainQuery(DynamicIndexDTO.class).term("username","酷酷的").search();
        System.out.println(list);
    }

    
}
