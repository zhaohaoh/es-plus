package com.es.plus.samples.test;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.adapter.pojo.es.EpFuzziness;
import com.es.plus.adapter.util.JsonUtils;
import com.es.plus.core.statics.Es;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.service.FastTestService;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * 演示测试
 *   FastTestDTO fastTestDTO = new FastTestDTO();
 *         fastTestDTO.setId(1L);
 *         fastTestDTO.setText("我的个人介绍 我是一篇文章，用于搜索。我的关键词有很多。苹果 梨子 苹果X2 苹果哥哥");
 *         fastTestDTO.setAge(25);
 *         fastTestDTO.setUsername("酷酷的");
 *         fastTestDTO.setCreateTime(new Date());
 */
@SpringBootTest(classes = SamplesApplication.class)
public class DemoTest {
    @Autowired
    private FastTestService fastTestService;
    
  
    
    @org.junit.jupiter.api.Test
    public void filterTerm(){
        
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder a = QueryBuilders.termQuery("a", "1");
        boolQueryBuilder.must(a);
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper().index().filter()
                .term(FastTestDTO::getUsername, "酷酷的").search();
        System.out.println(list);
    }
    @org.junit.jupiter.api.Test
    public void mustNotTerm(){
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper().mustNot()
                .terms(FastTestDTO::getUsername, "酷酷的").search();
        System.out.println(list);
    }
    @org.junit.jupiter.api.Test
    public void mustNot(){
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .filter().term(FastTestDTO::getUsername,"酷酷的2")
                .mustNot()
                .term(FastTestDTO::getUsername, "酷酷的").search();
        System.out.println(list);
    }

    @org.junit.jupiter.api.Test
    //must是默认的方式
    public void mustMatch(){
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .match(FastTestDTO::getText,"第二篇文章苹果123dff很好呀").search();
        System.out.println(list);
    }

    /**
     * 有一个match就是match到了 多个字段里面找一个词，有一个匹配就是匹配
     */
    @org.junit.jupiter.api.Test
    public void multiMatch(){
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .multiMatch("苹果",FastTestDTO::getText,FastTestDTO::getUsername).search();
        System.out.println(list);
    }

    @org.junit.jupiter.api.Test
    public void rangeQuery() throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("+8")));
        Date parse = simpleDateFormat.parse("2023-07-01 08:00:00");
        System.out.println(parse);
        Date date1 = new Date();
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .range(FastTestDTO::getCreateTime, parse.getTime(),
                        date1.getTime()).search();
        System.out.println(list);
    }


    @org.junit.jupiter.api.Test
    public void term() throws ParseException {
        //为什么能查询出来因为底层对字符串进行了转换 20230701=202307
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .term(FastTestDTO::getCreateTime,"2023-07").search();
        System.out.println(list);
    }


    @org.junit.jupiter.api.Test
    public void terms() throws ParseException {
        //为什么能查询出来因为底层对字符串进行了转换 20230701=202307
        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .terms(FastTestDTO::getCreateTime,"2023-07","2023-06").search();
        System.out.println(list);
    }

    /**
     * 匹配短语
     * 最能代替wildcard的查询方式，推荐使用
     */
    @org.junit.jupiter.api.Test
    public void matchPhrase() throws ParseException {

        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .matchPhrase(FastTestDTO::getText,"第二篇文章苹果").search();
        System.out.println(list);
    }


    /**
     * 本来是能模糊查询出来的，但是带了prefixLength=2 前两个字必须匹配，所以查询不出
     */
    @org.junit.jupiter.api.Test
    public void fuzzy() throws ParseException {

        EsResponse<FastTestDTO> list = fastTestService.esChainQueryWrapper()
                .fuzzy(FastTestDTO::getUsername,"苦苦的", EpFuzziness.ONE).search();
        System.out.println(list);
    }

    @org.junit.jupiter.api.Test
    public void wildCard() {
        // 50字符要100多毫秒 80个字符就要400毫秒了  仅仅只是建立索引词的时间，并不包含检索。
        EsResponse<FastTestDTO> test = Es.chainLambdaQuery(FastTestDTO.class)
                .wildcard(FastTestDTO::getText, "*凄切切请求群群群咕咕干白VNBVR人v个版雇个人全文我test1*")

                .search();
        System.out.println(test);
    }
    
    
    /**
     * 根据新的属性映射
     */
    @org.junit.jupiter.api.Test
    public void reindx() {
        String s = "{\n" + "    \"properties\": {\n" + "        \"text\": {\n"
                + "            \"type\": \"keyword\"\n" + "        }\n" + "    }\n" + "}";
    
        Map<String, Object> map = JsonUtils.toMap(s);
        Map changeMapping = (Map) map.get("properties");
        Es.chainIndex().reindex("reindex_test","fast_test_s1",changeMapping);
    }
    
    @org.junit.jupiter.api.Test
    public void deleteIndex() {
        Es.chainIndex().deleteIndex("fast_test_s1");
    }
    
    
    @org.junit.jupiter.api.Test
    public void update() {
//        FastTestDTO fastTestDTO = new FastTestDTO();
//        fastTestDTO.setText("12345");
//        fastTestDTO.setId(1L);
//        fastTestDTO.setTestList(Collections.singletonList("12312"));
//        Es.chainUpdate(FastTestDTO.class).save(fastTestDTO);
        
        Es.chainUpdate(FastTestDTO.class).set("testList",null).ids(Collections.singletonList("1")).updateByQuery();
    }

    public static void main(String[] args) throws ParseException {
        LocalDateTime parse1 = LocalDateTime.now();
        Instant instant = parse1.toInstant(ZoneOffset.ofHours(0));
        long l = instant.toEpochMilli();
        System.out.println(l);
    }

}
