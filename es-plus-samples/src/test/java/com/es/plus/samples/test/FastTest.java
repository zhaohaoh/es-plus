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
public class FastTest {

    @Autowired
    private FastTestService fastTestService;



    @org.junit.jupiter.api.Test
    public void fast() {
        EsResponse<Map> list = Es.chainQuery(Map.class).index("fast_test").term("username","酷酷的").list();
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
        fastTestDTO.setId(2L);
        fastTestDTO.setText("我是第二篇文章苹果 梨子 苹果X2 苹果哥哥");
        fastTestDTO.setAge(25);
        fastTestDTO.setUsername("酷酷的2");
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


    @org.junit.jupiter.api.Test
    public void fastAgg() {
        fastTestService.agg();
    }

    @org.junit.jupiter.api.Test
    public void wildCard() {
        // 50字符要100多毫秒 80个字符就要400毫秒了
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class)
                .wildcard(FastTestDTO::getText, "*凄切切请求群群群咕咕咕咕咕咕过过过过过过过过过个若若若若若若若若若若若ggrr二位而个干白VNBVR人v个版雇个人全文我test1凄切切请求群群群咕咕咕咕咕咕过过过过过过过过过个若若若若若若若若若若若ggrr二位而个干白VNBVR人v个版雇个人全文我test1*")

                .list();
        System.out.println(test);
    }

}
