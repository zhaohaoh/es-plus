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
        EsResponse<Map> list = Es.chainQuery(Map.class).index("fast_test").term("username","酷酷的").search();
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
            fastTestDTO.setUsernameTest("ggg");
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
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class).sortByAsc("id").search();
        System.out.println(test);
    }

    @org.junit.jupiter.api.Test
    public void fastSearch() {
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class).match(FastTestDTO::getText, "苹果")
                .term(FastTestDTO::getUsernameTest,"ggg").search();
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
                .wildcard(FastTestDTO::getText, "*凄切切请求群哈哈好哈哈哈任天堂吞吞吐吐吞吞吐吐吞吞吐吐天太热富贵花广发华福广汇股份蒂冈群咕咕咕咕咕咕过过过过个若若若若若若若若若ggrr二位而gggggGG古古怪怪滚滚滚官方电饭锅短发女孩苟富贵哈哈哈哈哈哈哈哈哈富贵个干白VNBVR人v个版雇个人全文我test1凄切切请求群群群咕咕咕咕咕咕过过过过过过过过过个若若若若若若若若若若若ggrr二位而个干白VNBVR人v个版雇个人全文我test1*")

                .search();
        System.out.println(test);
    }

    @org.junit.jupiter.api.Test
    public void searchAfter() {
//        PageInfo<FastTestDTO> pageInfo = new PageInfo<>(501, 10);
//        //1000页以内不走缓存。不开启firstSortValues和tailSortValues。保证数据的时效性。 1000页以上走缓存。数据时效性在5分钟，虽然是5分钟。但是会无限续期
//        if (pageInfo.getPage() * pageInfo.getSize() <= 10000){
//            EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class)
//                    .includes(FastTestDTO::getId)
//                    .orderByAsc("id").orderByAsc("username")
//                    .search();
//        } else {
////            PageInfo<FastTestDTO> searchAfterPage = new PageInfo<>(2000, 10);
////
////            int i = page * size;
//            //从redis中获取最接近当前查询页的数据
//
//            //计算差值
//            //int a=4990
//
//            //根据差值searchAfter
//
//            //最后查找需要的10条数据
//        }


        // 查询  page=1000 size=10   计算5010到10000的数据差
        // int diff= 5010 到 10000;不计算10
        // diff = 4990;
        // 根据4990 的SortValues 查询4990的size的  得到10000的tailSortValues
        // 根据10000的tailSortValues在向后size=10得到真实需要的数据
        // 记录真实数据的size firstSortValues  tailSortValues

                    EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class)
                    .includes(FastTestDTO::getId)
                    .sortByAsc("id").sortByAsc("username")
                    .search();


        EsResponse<FastTestDTO> test1 = Es.chainLambdaQuery(FastTestDTO.class).sortByAsc("id").sortByAsc("username")  .includes(FastTestDTO::getId)
                .trackScores(true)
                .minScope(0)
                .searchAfterValues(test.getTailSortValues()).search(10000);


        EsResponse<FastTestDTO> test3 = Es.chainLambdaQuery(FastTestDTO.class).sortByAsc("id").sortByAsc("username")  .includes(FastTestDTO::getId)
                .fetch(false)
                .searchAfterValues(test.getTailSortValues()).search(11);

        System.out.println(test);
    }

    @org.junit.jupiter.api.Test
    public void dynamic() {

        EsPlusClientFacade master = ClientContext.getClient("master");
        EsPlusClientFacade local = ClientContext.getClient("local");


        EsResponse<Map> list = Es.chainQuery(master, Map.class).index("distribution_chain_info").search();
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
