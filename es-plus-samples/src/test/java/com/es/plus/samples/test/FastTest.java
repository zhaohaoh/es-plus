package com.es.plus.samples.test;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.es6.client.EsPlus6Aggregations;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.service.FastTestService;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.pipeline.bucketsort.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        List<FastTestDTO> fastTestDTOs=new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            FastTestDTO fastTestDTO = new FastTestDTO();
            fastTestDTO.setId((long)i);
            fastTestDTO.setText("我是第二篇文章苹果 梨子 苹果X2 苹果哥哥");
            fastTestDTO.setAge(25);
            fastTestDTO.setUsername("酷酷的"+i);
            fastTestDTO.setCreateTime(new Date());
            fastTestDTOs.add(fastTestDTO);
        }

        Es.chainUpdate(FastTestDTO.class).saveBatch(fastTestDTOs);
    }

    @org.junit.jupiter.api.Test
    public void aaa() {
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = Es.chainLambdaQuery(FastTestDTO.class);
        EsAggWrapper<FastTestDTO> aggWrapper = fastTestDTOEsChainLambdaQueryWrapper.esAggWrapper();
        aggWrapper.terms("username",a->a.size(10000).order(BucketOrder.aggregation("id_max",true)))
       .subAggregation(es->es.max("id"));

            List<FieldSortBuilder> fieldSortBuilders=new ArrayList<>();
        FieldSortBuilder id_max = SortBuilders.fieldSort("id_max").order(SortOrder.ASC);
        fieldSortBuilders.add(id_max);
        BucketSortPipelineAggregationBuilder bucketSortPipelineAggregationBuilder = new BucketSortPipelineAggregationBuilder("username_terms",fieldSortBuilders);
        bucketSortPipelineAggregationBuilder.from(0);
        bucketSortPipelineAggregationBuilder.size(10);

        EsPlus6Aggregations<FastTestDTO> aggregations = (EsPlus6Aggregations<FastTestDTO>) fastTestDTOEsChainLambdaQueryWrapper.aggregations();
        Map<String, Long> username_terms = aggregations.getTermsAsMap("username");
        System.out.println(aggregations);
    }

    @org.junit.jupiter.api.Test
    public void bbb() {
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class).orderByAsc("id").list();
        System.out.println(test);
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


    @org.junit.jupiter.api.Test
    public void dynamic() {

        EsPlusClientFacade master = ClientContext.getClient("master");
        EsPlusClientFacade local = ClientContext.getClient("local");


        EsResponse<Map> list = Es.chainQuery(master, Map.class).index("distribution_chain_info").list();
        List<Map> list1 = list.getList();
        System.out.println(list1);
        Es.chainLambdaUpdate(local,Map.class).index("distribution_chain_info").saveBatch(list1);

        System.out.println(list1);
    }


    @org.junit.jupiter.api.Test
    public void mapping() {

        EsPlusClientFacade master = ClientContext.getClient("master");
        EsPlusClientFacade local = ClientContext.getClient("local");


        EsIndexResponse index = Es.chainIndex(master).index("distribution_chain_info").getIndex();
        Map<String, Object> mappings = index.getMappings();

        Es.chainIndex(local).index("distribution_chain_info").createIndex().putMapping(mappings);
    }
}
