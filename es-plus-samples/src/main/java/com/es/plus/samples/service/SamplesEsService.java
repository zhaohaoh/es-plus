package com.es.plus.samples.service;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.params.PageInfo;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.core.wrapper.chain.EsChainQueryWrapper;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.dto.SamplesNestedDTO;
import com.es.plus.samples.dto.SpuEsDTO;
import org.apache.lucene.search.join.ScoreMode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SamplesEsService extends EsServiceImpl<SamplesEsDTO> {


    public void nested() {
        EsChainLambdaQueryWrapper<SamplesNestedDTO> asChainQueryWrap = new EsChainLambdaQueryWrapper<>(SamplesNestedDTO.class);
        asChainQueryWrap.should().term(SamplesNestedDTO::getUsername, "hzh");
        asChainQueryWrap.terms(SamplesNestedDTO::getUsername, "term");
        // 声明语句嵌套关系是must
        EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper = esChainQueryWrapper().must()
                .terms(SamplesEsDTO::getUsername, "admin", "hzh", "shi").nestedQuery(SamplesEsDTO::getSamplesNesteds, () -> {
                    EsQueryWrapper<SamplesNestedDTO> esQueryWrap = new EsQueryWrapper<>(SamplesNestedDTO.class);
                    esQueryWrap.must().term("samplesNesteds.email", "abc");
                    return esQueryWrap;
                }, ScoreMode.None);
        EsResponse<SamplesEsDTO> esResponse = queryWrapper.list();

        // 查询
        List<SamplesEsDTO> list = esResponse.getList();

    }

    public void search() {
        // 声明语句嵌套关系是must
        EsResponse<SamplesEsDTO> esResponse = esChainQueryWrapper().must()
                .terms(SamplesEsDTO::getUsername, "admin", "hzh", "shi")
                // 多个must嵌套
//                .must(a ->
//                        // 声明内部语句关系的should
//                        a.must()
//                                .term(SamplesEsDTO::getNickName, "dasdsad")
//                                .term(SamplesEsDTO::getPhone, "1386859111"))
                .list();
        EsResponse<SamplesEsDTO> list2 = esChainQueryWrapper().list();
        System.out.println(list2);
        List<SamplesEsDTO> list = esResponse.getList();
        System.out.println(list);

        EsChainQueryWrapper<Map> term = Es.chainQuery(Map.class).index("sys_user2ttt_alias").must().match("username", "HZH").term("email", "abc");
        term.esAggWrapper().terms("keyword");
        EsResponse<Map> list1 = term.list();
//        Map<String, Long> username1 = list1.getEsAggsResponse().getTermsAsMap("keyword");
//        System.out.println(username1);
    }


    public void agg() {
        // 声明语句嵌套关系是must
        EsChainLambdaQueryWrapper<SamplesEsDTO> esChainQueryWrapper = esChainQueryWrapper().must()
                .ge(SamplesEsDTO::getId, 1);
        esChainQueryWrapper.esLambdaAggWrapper()
                // terms聚合并且指定数量10000
                .terms(SamplesEsDTO::getUsername, a -> a.size(1000))
                // 在terms聚合的基础上统计lock数量
                .subAggregation(t -> t.sum(SamplesEsDTO::getId));
        EsResponse<SamplesEsDTO> esResponse = esChainQueryWrapper
                // 查询
                .list();
        List<SamplesEsDTO> list = esResponse.getList();

//        EsAggregations<SamplesEsDTO> esAggregationsReponse = esResponse.getEsAggsResponse();
//        Terms terms = esAggregationsReponse.getTerms(SamplesEsDTO::getUsername);
//        Map<String, Long> termsAsMap = esAggregationsReponse.getTermsAsMap(SamplesEsDTO::getUsername);
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
                                .term(SamplesEsDTO::getPhone, "1386859111")).profile();
        System.out.println(esResponse);
    }

    public void test() {
        EsResponse<SamplesEsDTO> hzh = esChainQueryWrapper().must().match(SamplesEsDTO::getKeyword, "hzh").list();

        System.out.println(hzh);
    }

    public void scroll() {
        String scrollId = null;
        int page =2;
        int size=2;

        for (int i = 0; i < page; i++) {
            EsResponse<SamplesEsDTO> hzh = esChainQueryWrapper().must().match(SamplesEsDTO::getUsername, "HZH")
                    .orderByAsc("id").scroll(size, scrollId);
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
        EsResponse<SamplesEsDTO> hzh = Es.chainLambdaQuery(SamplesEsDTO.class).term(SamplesEsDTO::getUsername, "hzh").list();
        System.out.println(hzh);
    }

    public void searhAfter() {
        PageInfo<SamplesEsDTO> pageInfo = new PageInfo<>();
        pageInfo.setSize(3);

        EsResponse<SamplesEsDTO> samplesEsDTOEsResponse = Es.chainLambdaQuery(SamplesEsDTO.class)
                .orderBy("asc", SamplesEsDTO::getId).searchAfter(pageInfo);


        pageInfo.setSearchAfterValues(samplesEsDTOEsResponse.getTailSortValues());
        EsResponse<SamplesEsDTO> samplesEsDTOEsResponse1 = Es.chainLambdaQuery(SamplesEsDTO.class)
                .orderBy("DESC", SamplesEsDTO::getId).searchAfter(pageInfo);

        System.out.println(samplesEsDTOEsResponse);

        System.out.println(samplesEsDTOEsResponse1);

    }

    public void listLandList() {
        EsResponse<SpuEsDTO> list = Es.chainLambdaQuery(SpuEsDTO.class).list();
        System.out.println(list);
    }
}
