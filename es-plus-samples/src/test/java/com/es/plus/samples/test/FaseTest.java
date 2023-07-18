package com.es.plus.samples.test;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.statics.Es;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.service.FastTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;

@SpringBootTest(classes = SamplesApplication.class)
public class FaseTest {

    @Autowired
    private FastTestService fastTestService;



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
    public void fastSearch2() {
       fastTestService.test();
    }



    
}
