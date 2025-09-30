package com.es.plus.samples.test;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.es.plus.common.EsPlusClientFacade;
import com.es.plus.common.params.EsAggResponse;
import com.es.plus.common.params.EsAggResult;
import com.es.plus.common.params.EsIndexResponse;
import com.es.plus.common.params.EsResponse;
import com.es.plus.common.pojo.es.EpAggBuilder;
import com.es.plus.common.pojo.es.EpBucketOrder;
import com.es.plus.common.pojo.es.EpQueryBuilder;
import com.es.plus.core.ClientContext;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.aggregation.EsLambdaAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.service.FastTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest(classes = SamplesApplication.class)
public class FastTest {
    
    @Autowired
    private FastTestService fastTestService;
    
    @org.junit.jupiter.api.Test
    public void fast() {
        EsResponse<Map> list = Es.chainQuery(Map.class).index("fast_test_new_v11").term("username","酷酷的").search();
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
        for (int i = 1000000; i < 8000000; i++) {
            FastTestDTO fastTestDTO = new FastTestDTO();
            fastTestDTO.setId((long) i);
            fastTestDTO.setText("1刚发的古古怪怪滚滚滚古古怪怪滚滚滚古古怪怪滚滚滚古古怪怪滚滚滚的反反复复方法反反复复方法反反复复方法的发发发发发发反反复复方法反反复复去呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜呜我问问发生的发生的发");
            fastTestDTO.setAge(1L);
            fastTestDTO.setUsername("新来的朋友们" );
            fastTestDTO.setUsernameTest("干啥的复古风古大爱上大叔大叔三大四典风格电风扇广东分公司法刀发生的发生的发水电费都是发生的发生的刮大风反反复复方法反反复复发个电饭锅梵蒂冈地方古典风格电饭锅电饭锅电饭锅");
            fastTestDTO.setCreateTime(new Date());
            fastTestDTOs.add(fastTestDTO);
            if (fastTestDTOs.size()>=3000){
                Es.chainUpdate(FastTestDTO.class).saveBatch(fastTestDTOs);
                fastTestDTOs.clear();
            }
        }
    }
    
    @org.junit.jupiter.api.Test
    public void forceMerge() {
        Es.chainIndex().index("fast_test_new").forceMerge(1000,true,true,"fast_test_new");
    }
    
    @org.junit.jupiter.api.Test
    public void aaa() {
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = Es.chainLambdaQuery(FastTestDTO.class);
        EsAggWrapper<FastTestDTO> aggWrapper = fastTestDTOEsChainLambdaQueryWrapper.esAggWrapper();
        aggWrapper.terms("username",a->
                        a.size(10000).order(EpBucketOrder.aggregation("id_max",true))
                
                )
                .subAgg(es->es.max("age"));
  
        EsAggResult<FastTestDTO> esAggResult = fastTestDTOEsChainLambdaQueryWrapper.aggregations().getEsAggResult();
        Map<String, EsAggResult<FastTestDTO>> usernameTerms = esAggResult.getMultiBucketNestedMap("username_terms");
        usernameTerms.forEach((k, v) -> {
            System.out.println(v.getMax("age_max"));
        });
        System.out.println(usernameTerms);
    }
    
    @org.junit.jupiter.api.Test
    public void bbb() {
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class).sortByAsc("id").search();
        System.out.println(test);
    }
    
    @org.junit.jupiter.api.Test
    public void searchQuery() {
        TermsQuery.Builder builder = new TermsQuery.Builder();
        List<FieldValue> a = List.of("a", "新来的朋友们").stream().map(FieldValue::of).collect(Collectors.toList());
        builder.field("username").terms(b->b.value(a));
        
        EpQueryBuilder queryBuilder = new EpQueryBuilder()
                .orginalQuery(builder);
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class)
                .sortByAsc("id")
                .query(queryBuilder)
                .term(FastTestDTO::getAge,111L)
                .must(b->b.match(FastTestDTO::getText,"新来的朋友们啊"))
                .search();
        System.out.println(test);
    }
    
    @org.junit.jupiter.api.Test
    public void searchAggQuery() {
        TermsQuery.Builder builder = new TermsQuery.Builder();
        List<FieldValue> a = List.of("a", "新来的朋友们").stream().map(FieldValue::of).collect(Collectors.toList());
        builder.field("username").terms(b->b.value(a));
        
        
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = Es.chainLambdaQuery(
                FastTestDTO.class);
        EsLambdaAggWrapper<FastTestDTO> fastTestDTOEsLambdaAggWrapper = fastTestDTOEsChainLambdaQueryWrapper.esLambdaAggWrapper();
        fastTestDTOEsLambdaAggWrapper.terms(FastTestDTO::getUsername).terms(FastTestDTO::getId)
                .subAgg(b-> {
                            TermsAggregation.Builder username1 = new TermsAggregation.Builder().field("username");
                            TermsAggregation username = username1.build();
                            b.avg(FastTestDTO::getAge)
                                    .add(new EpAggBuilder() .esOrginalAgg(username));
                        }
                );
        EsResponse<FastTestDTO> test = fastTestDTOEsChainLambdaQueryWrapper.sortBy("ASC",FastTestDTO::getCreateTime)
                .search();
        System.out.println( );
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
        
        EsChainLambdaQueryWrapper<FastTestDTO> wrapper = Es.chainLambdaQuery(
                FastTestDTO.class);
        EsResponse<FastTestDTO> test3 = wrapper.sortByAsc("id").sortByAsc("username")  .includes(FastTestDTO::getId)
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
    
    @org.junit.jupiter.api.Test
    public void sql() {
        EsResponse<Map> esResponse = Es.chainQuery(Map.class).executeSQLep("select * from  fast_test_alias");
        System.out.println(esResponse);
    }
    
    
    @org.junit.jupiter.api.Test
    public void indexs() {
        EsResponse<Map> esResponse = Es.chainQuery(Map.class).index("fast_test_new_v116", "fast_test_new_v115")
                .sortByDesc("id").search(10);
        System.out.println(esResponse);
    }
    
    @org.junit.jupiter.api.Test
    public void sss() {
        //        EsChainQueryWrapper<Map> index = Es.chainQuery(Map.class).index("fast_test_new_v116", "fast_test_new_v115");
        //
        //        EsAggWrapper<Map> mapEsAggWrapper = index.esAggWrapper();
        //        EsAggWrapper<Map> username = mapEsAggWrapper.percentiles("username_test1");
        //        EsResponse<Map> search = index.search();
        //        System.out.println();
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(1L);
        fastTestDTO.setText("asdasdasdsa");
        fastTestService.saveBatchAsyncProcessor(Collections.singletonList(fastTestDTO));
    }
    
    
    // 实测保存的时候不允许使用*  只有查询能用
    @org.junit.jupiter.api.Test
    public void esTongpeifu() {
        FastTestDTO fastTest = new FastTestDTO();
        fastTest.setId(6543210L);
        fastTest.setUsername("哈哈哈哈哈123");
        Es.chainUpdate(FastTestDTO.class).index("fast_test_new_v116", "fast_test_new_v115").save(fastTest);
    }
    
    
    @org.junit.jupiter.api.Test
    public void aaaaaaaa() {
        
        EsResponse<FastTestDTO> search = Es.chainQuery(FastTestDTO.class)
                .search();
        System.out.println(search);
    }
}