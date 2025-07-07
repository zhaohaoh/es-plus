package com.es.plus.samples.service;

import com.es.plus.adapter.params.EsHit;
import com.es.plus.adapter.params.EsHits;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.core.wrapper.core.EsLambdaQueryWrapper;
import com.es.plus.core.wrapper.core.EsLambdaUpdateWrapper;
import com.es.plus.core.wrapper.core.EsWrapper;
import com.es.plus.es7.client.EsPlusAggregations;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.dto.SamplesNestedDTO;
import com.es.plus.samples.dto.SamplesNestedInnerDTO;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class SamplesEsService extends EsServiceImpl<SamplesEsDTO> {
    
    /**
     * 三级嵌套查询，如果不使用框架用原生查询将会非常复杂
     */
    public void nested() {
        //获取二级查询条件
        Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> innerConsumer = getSamplesNestedConsumer();
        // 声明语句嵌套关系是must
        InnerHitBuilder innerHitBuilder = new InnerHitBuilder("test");
        innerHitBuilder.setSize(10);
        innerHitBuilder.setFetchSourceContext(new FetchSourceContext(true));

 
        //一级查询条件
        EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper = esChainQueryWrapper().must().fetch(true)
                //二级
                .nested("samplesNesteds",
                        (esQueryWrap) -> {
                            esQueryWrap.must().term("username", "3");
                            InnerHitBuilder innerHitBuilder1 = new InnerHitBuilder();
                            innerHitBuilder1.setSize(100);
                            //三级
                            esQueryWrap.must().nested("samplesNesteds.samplesNestedInner",
                                    (innerQuery) -> {
                                        innerQuery.must().term("username", "3");
//                                        .term(SamplesNestedInnerDTO::getState,true);
                                    }, ScoreMode.None, innerHitBuilder1);
                        }, ScoreMode.None,innerHitBuilder);


        EsResponse<SamplesEsDTO> esResponse = queryWrapper.search();
        EsHits innerHits = esResponse.getInnerHits();
        List<EsHit> esHitList = innerHits.getEsHitList();
        for (EsHit esHit : esHitList) {
            long innerHitsTotal = esHit.getInnerHitsTotal("test");
            EsHits esHitEsHits = esHit.getEsInnerHits("test");
            List<SamplesNestedDTO> test = esHit.getInnerList(SamplesNestedDTO.class, "test");
            for (EsHit hit : esHitEsHits.getEsHitList()) {
                List<SamplesNestedInnerDTO> list = hit.getInnerList(SamplesNestedInnerDTO.class,"samplesNesteds.samplesNestedInner");
                System.out.println();
            }
            System.out.println();
        }
        // 查询
        List<SamplesEsDTO> list = esResponse.getList();
        System.out.println(list);
    }

    /**
     *  获取二级嵌套查询对象
     */
    private Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> getSamplesNestedConsumer() {
        Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> innerConsumer = (esQueryWrap) -> {
            esQueryWrap.must().term(SamplesNestedDTO::getUsername, "3");
            InnerHitBuilder innerHitBuilder1 = new InnerHitBuilder();
            innerHitBuilder1.setSize(100);
            Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> innerInnerConsumer = skuQueryWrapper();
            esQueryWrap.must().nestedQuery(SamplesNestedDTO::getSamplesNestedInner, SamplesNestedInnerDTO.class,
                    innerInnerConsumer, ScoreMode.None, innerHitBuilder1);
        };
        return innerConsumer;
    }

    /**
     *  获取三级嵌套查询对象
     */
    private Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> skuQueryWrapper() {
        Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> innerInnerConsumer = (innerQuery) -> {
            innerQuery.must().term(SamplesNestedInnerDTO::getUsername, 3);
        };
        return innerInnerConsumer;
    }


    public void search() {
        // 声明语句嵌套关系是must
        EsResponse<SamplesEsDTO> esResponse = esChainQueryWrapper().mustNot()
                .wildcard(SamplesEsDTO::getUsername, "*g那好好好好好好好好好好好好哈哈哈哈哈哈哈哈人兔兔兔兔兔兔兔兔吞吞吐吐他吞吞吐吐vvvvvvvvvvvvvvvvvvv请问额额额GG古古怪怪滚滚滚刚刚扭扭捏捏那你哈哈哈哈哈好哈哈哈哈哈哈哈哈应用*")
                .term(SamplesEsDTO::getEmail, "bbbbbb")
                // 多个must嵌套
//                .must(a ->
//                        // 声明内部语句关系的should
//                        a.must()
//                                .term(SamplesEsDTO::getNickName, "dasdsad")
//                                .term(SamplesEsDTO::getPhone, "1386859111"))
                .search();
//        EsResponse<SamplesEsDTO> list2 = esChainQueryWrapper().list();
//        System.out.println(list2);
        List<SamplesEsDTO> list = esResponse.getList();
        System.out.println(list);
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EsChainQueryWrapper<Map> term = Es.chainQuery(Map.class).index("sys_user2ttt_alias").must()
                .match("username", "HZH").term("email", "abc");
        term.esAggWrapper().terms("keyword");
        EsResponse<Map> list1 = term.search();
//        Map<String, Long> username1 = list1.getEsAggsResponse().getTermsAsMap("keyword");
//        System.out.println(username1);
    }


    public void agg() {

        EsResponse<SamplesEsDTO> dd = esChainQueryWrapper().must().match(SamplesEsDTO::getUsername, "dd").search();
        EsAggWrapper<SamplesEsDTO> username = esChainQueryWrapper().esAggWrapper().terms("username");

        // 声明语句嵌套关系是must
        EsChainLambdaQueryWrapper<SamplesEsDTO> esChainQueryWrapper = esChainQueryWrapper().must()
                .ge(SamplesEsDTO::getId, 1);
        esChainQueryWrapper.esLambdaAggWrapper()
                // terms聚合并且指定数量10000
                .filter(SamplesEsDTO::getUsername, ()-> {
                      EsWrapper<SamplesEsDTO> esWrapper = esChainQueryWrapper();
                      return esWrapper;
                })
                .termsFn(SamplesEsDTO::getUsername, e ->    e.size(100))
                // 在terms聚合的基础上统计lock数量
                .subAggregation(t -> t.sum(SamplesEsDTO::getId));
        EsResponse<SamplesEsDTO> esResponse = esChainQueryWrapper
                // 查询
                .search();
        List<SamplesEsDTO> list = esResponse.getList();

        EsPlusAggregations<SamplesEsDTO> esAggsResponse = (EsPlusAggregations<SamplesEsDTO>) esResponse.getEsAggsResponse();
//        Aggregations aggregations = esAggsResponse.getAggregations();
//        Map<String, Aggregation> asMap = aggregations.getAsMap();
//        Map<String, Object> map = JsonUtils.beanToMap(asMap);
//        System.out.println(map);
//        Terms terms = esAggsResponse.getTerms(SamplesEsDTO::getUsername);
//        Map<String, Long> termsAsMap = esAggsResponse.getTermsAsMap(SamplesEsDTO::getUsername);
    }

    public void profile1() {
        // 声明语句嵌套关系是must
        EsResponse<SamplesEsDTO> esResponse = esChainQueryWrapper().must()
                .terms(SamplesEsDTO::getUsername, "admin", "hzh", "shi")
                // 多个must嵌套
                .must(a ->
                        // 声明内部语句关系的should
                        a.must()
                                .term(SamplesEsDTO::getNickName, "dasdsad")
                                .term(SamplesEsDTO::getPhone, "1386859111")).profile().search();
        System.out.println(esResponse);
    }

    public void test() {
        esChainQueryWrapper().must().match(SamplesEsDTO::getSamplesNesteds, "hzh")
                .term(true, SamplesEsDTO::getEmail, null);

        System.out.println();
    }

    public void scroll() {
        String scrollId = null;
        int page = 3;
        int size = 2;

        for (int i = 0; i < page; i++) {
            EsResponse<SamplesEsDTO> hzh = esChainQueryWrapper().must()
                    .sortByAsc("id").scroll(size, scrollId);
            scrollId = hzh.getScrollId();
            System.out.println(hzh);
        }
    }

    public void count() {
        long hzh = this.count(null);

        System.out.println(hzh);
    }

    public void update() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "fsdfsfds");
        map.put("id", "d73d1b4e46244b0db766987759d6e");
        Es.chainUpdate(Map.class).index("sys_user2ttt").save(map);
    }

    public void newSelect() {
        EsResponse<SamplesEsDTO> hzh = Es.chainLambdaQuery(SamplesEsDTO.class).term(SamplesEsDTO::getUsername, "hzh").search();
        System.out.println(hzh);
    }

    public void searhAfter() {
//        PageInfo<SamplesEsDTO> pageInfo = new PageInfo<>();
//        pageInfo.setSize(3);

//        EsResponse<SamplesEsDTO> samplesEsDTOEsResponse = Es.chainLambdaQuery(SamplesEsDTO.class)
//                .orderBy("asc", SamplesEsDTO::getId).searchAfter(null);
//
//
//        pageInfo.setSearchAfterValues(samplesEsDTOEsResponse.getTailSortValues());
//        EsResponse<SamplesEsDTO> samplesEsDTOEsResponse1 = Es.chainLambdaQuery(SamplesEsDTO.class)
//                .orderBy("DESC", SamplesEsDTO::getId).searchAfter(null);
//
//        System.out.println(samplesEsDTOEsResponse);
//
//        System.out.println(samplesEsDTOEsResponse1);

    }


    public void updateByQuery() {
        EsLambdaUpdateWrapper<SamplesEsDTO> updateWrapper = new EsLambdaUpdateWrapper<>();
        updateWrapper.match(SamplesEsDTO::getUsername, "ggghhh").set(SamplesEsDTO::getEmail, "bbbbbb");
        this.updateByQuery(updateWrapper);
        
        
        EsLambdaQueryWrapper<SamplesEsDTO> queryWrapper = new EsLambdaQueryWrapper<>();
        queryWrapper.match(SamplesEsDTO::getUsername, "ggghhh");
        EsResponse<SamplesEsDTO> list = this.search(queryWrapper);
        System.out.println(list);
    }
}
