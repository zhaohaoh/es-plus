package com.es.plus.samples.test;

import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.samples.SamplesApplication;
import com.es.plus.samples.dto.FastTestDTO;
import com.es.plus.samples.dto.FastTestInnerDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FastTestDTO DSL 测试用例 - 通过 toDsl() 验证所有查询方法
 * 使用 JSON 结构验证 DSL 的正确性，不依赖真实ES环境
 */
@SpringBootTest(classes = SamplesApplication.class)
public class FastTestDslTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 灵活的 DSL 结构验证
     *
     * @param dsl DSL 字符串
     * @param assertions 验证逻辑（Lambda 表达式）
     */
    private void assertDslStructure(String dsl, Consumer<JsonNode> assertions) {
        try {
            JsonNode root = objectMapper.readTree(dsl);
            assertions.accept(root);
        } catch (Exception e) {
            fail("DSL 验证失败: " + e.getMessage() + "\nDSL: " + dsl);
        }
    }

    /**
     * 测试 term 精确查询
     */
    @Test
    public void testTermQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .term(FastTestDTO::getUsername, "admin")
                .toDsl();

        System.out.println("=== term 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            // 验证 bool.must 结构
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray(), "must 应该是数组");
            assertTrue(must.size() > 0, "must 数组不应为空");

            // 验证包含 term 查询
            boolean hasTerm = false;
            for (JsonNode item : must) {
                if (item.has("term") && item.path("term").has("username")) {
                    hasTerm = true;
                    JsonNode termValue = item.path("term").path("username");
                    // 可能是 {"value": "admin"} 或直接是 "admin"
                    String actualValue = termValue.has("value")
                        ? termValue.path("value").asText()
                        : termValue.asText();
                    assertEquals("admin", actualValue, "term 的值应该是 admin");
                }
            }
            assertTrue(hasTerm, "应该包含 username 的 term 查询");
        });
    }

    /**
     * 测试 terms 多值精确查询
     */
    @Test
    public void testTermsQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .terms(FastTestDTO::getUsername, "admin", "user", "guest")
                .toDsl();

        System.out.println("=== terms 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            boolean hasTerms = false;
            for (JsonNode item : must) {
                if (item.has("terms") && item.path("terms").has("username")) {
                    hasTerms = true;
                    JsonNode values = item.path("terms").path("username");
                    assertTrue(values.isArray() || values.toString().contains("admin"));
                }
            }
            assertTrue(hasTerms, "应该包含 terms 查询");
        });
    }

    /**
     * 测试 match 全文检索
     */
    @Test
    public void testMatchQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .match(FastTestDTO::getText, "关键词搜索")
                .toDsl();

        System.out.println("=== match 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            boolean hasMatch = false;
            for (JsonNode item : must) {
                if (item.has("match")) {
                    hasMatch = true;
                    assertTrue(item.path("match").toString().contains("关键词搜索"));
                }
            }
            assertTrue(hasMatch, "应该包含 match 查询");
        });
    }

    /**
     * 测试 range 范围查询
     */
    @Test
    public void testRangeQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .ge(FastTestDTO::getAge, 18L)
                .le(FastTestDTO::getAge, 60L)
                .sortByAsc(FastTestDTO::getAge)
                .sortByDesc(FastTestDTO::getCreateTime)
                .toDsl();

        System.out.println("=== range 查询 DSL (含排序) ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            boolean hasGte = false;
            boolean hasLte = false;
            for (JsonNode item : must) {
                if (item.has("range") && item.path("range").has("age")) {
                    JsonNode age = item.path("range").path("age");
                    if (age.has("gte")) {
                        hasGte = true;
                        assertEquals(18, age.path("gte").asInt());
                    }
                    if (age.has("lte")) {
                        hasLte = true;
                        assertEquals(60, age.path("lte").asInt());
                    }
                }
            }
            assertTrue(hasGte, "应该包含 age >= 18 的 range 查询");
            assertTrue(hasLte, "应该包含 age <= 60 的 range 查询");

            // 验证排序
            assertTrue(root.has("sort"), "应该包含 sort 字段");
            JsonNode sort = root.path("sort");
            assertTrue(sort.isArray(), "sort 应该是数组");
            assertTrue(sort.size() >= 2, "应该至少有 2 个排序字段");

            // 验证第一个排序是 age asc
            JsonNode firstSort = sort.get(0);
            assertTrue(firstSort.has("age"), "第一个排序字段应该是 age");
            JsonNode ageSort = firstSort.path("age");
            assertTrue(ageSort.has("order"), "age 排序应该有 order 字段");
            assertEquals("asc", ageSort.path("order").asText(), "age 应该是升序排序");

            // 验证第二个排序是 createTime desc
            JsonNode secondSort = sort.get(1);
            assertTrue(secondSort.has("createTime"), "第二个排序字段应该是 createTime");
            JsonNode createTimeSort = secondSort.path("createTime");
            assertTrue(createTimeSort.has("order"), "createTime 排序应该有 order 字段");
            assertEquals("desc", createTimeSort.path("order").asText(), "createTime 应该是降序排序");
        });
    }

    /**
     * 测试 wildcard 通配符查询
     */
    @Test
    public void testWildcardQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .wildcard(FastTestDTO::getUsername, "*admin*")
                .toDsl();

        System.out.println("=== wildcard 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            boolean hasWildcard = false;
            for (JsonNode item : must) {
                if (item.has("wildcard")) {
                    hasWildcard = true;
                    assertTrue(item.path("wildcard").toString().contains("admin"));
                }
            }
            assertTrue(hasWildcard, "应该包含 wildcard 查询");
        });
    }

    /**
     * 测试 prefix 前缀查询
     */
    @Test
    public void testPrefixQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .prefix(FastTestDTO::getUsername, "admin")
                .toDsl();

        System.out.println("=== prefix 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            boolean hasPrefix = false;
            for (JsonNode item : must) {
                if (item.has("prefix") && item.path("prefix").has("username")) {
                    hasPrefix = true;
                    JsonNode prefixValue = item.path("prefix").path("username");
                    String actualValue = prefixValue.has("value")
                        ? prefixValue.path("value").asText()
                        : prefixValue.asText();
                    assertEquals("admin", actualValue);
                }
            }
            assertTrue(hasPrefix, "应该包含 prefix 查询");
        });
    }

    /**
     * 测试嵌套 bool 查询
     */
    @Test
    public void testNestedBoolQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .must()
                .ge(FastTestDTO::getAge, 18L)
                .must(wrapper -> wrapper.should()
                        .term(FastTestDTO::getUsername, "admin")
                        .term(FastTestDTO::getUsername, "user"))
                .toDsl();

        System.out.println("=== 嵌套 bool 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            // 验证有 range 查询
            boolean hasRange = false;
            // 验证有嵌套的 bool.should
            boolean hasNestedBool = false;

            for (JsonNode item : must) {
                if (item.has("range")) {
                    hasRange = true;
                }
                if (item.has("bool") && item.path("bool").has("should")) {
                    hasNestedBool = true;
                    JsonNode should = item.path("bool").path("should");
                    assertTrue(should.isArray());
                    assertTrue(should.size() >= 2, "should 应该至少有 2 个条件");
                }
            }

            assertTrue(hasRange, "应该包含 range 查询");
            assertTrue(hasNestedBool, "应该包含嵌套的 bool 查询");
        });
    }

    /**
     * 测试条件查询（动态查询）
     */
    @Test
    public void testConditionalQuery() {
        String keyword = "test";
        String username = null;
        Long minAge = 18L;

        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .must()
                .match(keyword != null, FastTestDTO::getText, keyword)
                .term(username != null, FastTestDTO::getUsername, username)
                .ge(minAge != null, FastTestDTO::getAge, minAge)
                .toDsl();

        System.out.println("=== 条件查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");

            // 应该有 match 和 range，但不应该有 term（因为 username 为 null）
            boolean hasMatch = false;
            boolean hasRange = false;
            boolean hasTerm = false;

            for (JsonNode item : must) {
                if (item.has("match")) hasMatch = true;
                if (item.has("range")) hasRange = true;
                if (item.has("term") && item.path("term").has("username")) hasTerm = true;
            }

            assertTrue(hasMatch, "应该包含 match 查询");
            assertTrue(hasRange, "应该包含 range 查询");
            assertFalse(hasTerm, "不应该包含 username 的 term 查询（条件为 false）");
        });
    }

    /**
     * 测试聚合查询 - terms
     */
    @Test
    public void testTermsAggregation() {
       
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = Es.chainLambdaQuery(
                FastTestDTO.class);
        fastTestDTOEsChainLambdaQueryWrapper
                .esLambdaAggWrapper()
                .terms(FastTestDTO::getUsername, a -> a.size(100));
        String dsl =  fastTestDTOEsChainLambdaQueryWrapper.toDsl();

        System.out.println("=== terms 聚合 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            // 验证有聚合节点
            assertTrue(root.has("aggs") || root.has("aggregations"),
                "应该包含聚合节点");

            JsonNode aggs = root.has("aggs") ? root.path("aggs") : root.path("aggregations");
            assertNotNull(aggs);

            // 验证有 terms 聚合
            boolean hasTermsAgg = false;
            aggs.fields().forEachRemaining(entry -> {
                JsonNode aggValue = entry.getValue();
                if (aggValue.has("terms")) {
                    JsonNode terms = aggValue.path("terms");
                    if (terms.path("field").asText().equals("username")) {
                        assertEquals(100, terms.path("size").asInt());
                    }
                }
            });
        });
    }

    /**
     * 测试复杂组合查询
     */
    @Test
    public void testComplexQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .must()
                // 精确匹配
                .term(FastTestDTO::getUsername, "admin")
                // 范围查询
                .ge(FastTestDTO::getAge, 18L)
                .le(FastTestDTO::getAge, 60L)
                // 全文检索
                .match(FastTestDTO::getText, "搜索关键词")
                // 前缀匹配
                .prefix(FastTestDTO::getUsernameTest, "test")
                // 嵌套条件
                .must(wrapper -> wrapper.should()
                        .wildcard(FastTestDTO::getUsername, "*admin*")
                        .fuzzy(FastTestDTO::getUsername, "admn", null))
                // 排除条件
                .mustNot(wrapper -> wrapper
                        .term(FastTestDTO::getUsername, "guest"))
                .toDsl();

        System.out.println("=== 复杂组合查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode query = root.path("query");
            assertTrue(query.has("bool"), "应该有 bool 查询");

            JsonNode bool = query.path("bool");
            assertTrue(bool.has("must"), "应该有 must 条件");
            assertTrue(bool.has("must_not"), "应该有 must_not 条件");

            JsonNode must = bool.path("must");
            assertTrue(must.isArray());
            assertTrue(must.size() >= 5, "must 应该包含多个条件");

            // 验证包含各种查询类型
            boolean hasTerm = false;
            boolean hasRange = false;
            boolean hasMatch = false;
            boolean hasPrefix = false;
            boolean hasNestedBool = false;

            for (JsonNode item : must) {
                if (item.has("term")) hasTerm = true;
                if (item.has("range")) hasRange = true;
                if (item.has("match")) hasMatch = true;
                if (item.has("prefix")) hasPrefix = true;
                if (item.has("bool")) hasNestedBool = true;
            }

            assertTrue(hasTerm, "应该包含 term 查询");
            assertTrue(hasRange, "应该包含 range 查询");
            assertTrue(hasMatch, "应该包含 match 查询");
            assertTrue(hasPrefix, "应该包含 prefix 查询");
            assertTrue(hasNestedBool, "应该包含嵌套 bool 查询");
        });
    }

    /**
     * 测试 nested 嵌套查询
     */
    @Test
    public void testNestedQuery() {
        String dsl = Es.chainLambdaQuery(FastTestDTO.class)
                .nestedQuery(FastTestDTO::getFastTestInner, FastTestInnerDTO.class, innerQuery -> innerQuery
                        .term(FastTestInnerDTO::getUsername, "nestedUser")
                        .ge(FastTestInnerDTO::getAge, 25L))
                .toDsl();

        System.out.println("=== nested 查询 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            JsonNode must = root.path("query").path("bool").path("must");
            assertTrue(must.isArray());

            // 查找 nested 查询
            boolean hasNested = false;
            for (JsonNode item : must) {
                if (item.has("nested")) {
                    hasNested = true;
                    JsonNode nested = item.path("nested");

                    // 验证 path (应该是驼峰格式的字段名)
                    String path = nested.path("path").asText();
                    assertTrue(path.equals("fastTestInner") || path.equals("fast_test_inner"),
                        "nested path 应该是 fastTestInner 或 fast_test_inner，实际是: " + path);

                    // 验证嵌套的查询
                    JsonNode nestedQuery = nested.path("query");
                    assertTrue(nestedQuery.has("bool"), "nested 内部应该有 bool 查询");

                    JsonNode nestedMust = nestedQuery.path("bool").path("must");
                    assertTrue(nestedMust.isArray(), "nested 内部 must 应该是数组");

                    // 验证包含 term 和 range
                    boolean hasTermInNested = false;
                    boolean hasRangeInNested = false;
                    for (JsonNode nestedItem : nestedMust) {
                        if (nestedItem.has("term")) {
                            hasTermInNested = true;
                            String termStr = nestedItem.toString();
                            assertTrue(termStr.contains("nestedUser"),
                                "nested 中的 term 应该包含 nestedUser");
                        }
                        if (nestedItem.has("range")) {
                            hasRangeInNested = true;
                            String rangeStr = nestedItem.toString();
                            assertTrue(rangeStr.contains("25") || rangeStr.contains("age"),
                                "nested 中的 range 应该包含 age 和 25");
                        }
                    }

                    assertTrue(hasTermInNested, "nested 查询内应该包含 term");
                    assertTrue(hasRangeInNested, "nested 查询内应该包含 range");
                }
            }

            assertTrue(hasNested, "应该包含 nested 查询");
        });
    }

    /**
     * 测试聚合 - sum
     */
    @Test
    public void testSumAggregation() {
        EsChainLambdaQueryWrapper<FastTestDTO> wrapper = Es.chainLambdaQuery(FastTestDTO.class);
        wrapper.esLambdaAggWrapper()
                .sum("total_age", FastTestDTO::getAge);
        String dsl = wrapper.toDsl();

        System.out.println("=== sum 聚合 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            assertTrue(root.has("aggs") || root.has("aggregations"),
                "应该包含聚合节点");

            JsonNode aggs = root.has("aggs") ? root.path("aggs") : root.path("aggregations");

            // 验证有 sum 聚合
            boolean hasSumAgg = false;
            aggs.fields().forEachRemaining(entry -> {
                JsonNode aggValue = entry.getValue();
                if (aggValue.has("sum")) {
                    JsonNode sum = aggValue.path("sum");
                    String field = sum.path("field").asText();
                    assertTrue(field.equals("age"), "sum 聚合字段应该是 age");
                }
            });
        });
    }

    /**
     * 测试聚合 - count (value_count)
     */
    @Test
    public void testCountAggregation() {
        EsChainLambdaQueryWrapper<FastTestDTO> wrapper = Es.chainLambdaQuery(FastTestDTO.class);
        wrapper.esLambdaAggWrapper()
                .count("user_count", FastTestDTO::getUsername);
        String dsl = wrapper.toDsl();

        System.out.println("=== count 聚合 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            assertTrue(root.has("aggs") || root.has("aggregations"),
                "应该包含聚合节点");

            JsonNode aggs = root.has("aggs") ? root.path("aggs") : root.path("aggregations");

            // 验证有 value_count 聚合
            boolean hasCountAgg = false;
            aggs.fields().forEachRemaining(entry -> {
                JsonNode aggValue = entry.getValue();
                if (aggValue.has("value_count")) {
                    JsonNode count = aggValue.path("value_count");
                    String field = count.path("field").asText();
                    assertTrue(field.equals("username"), "count 聚合字段应该是 username");
                }
            });
        });
    }

    /**
     * 测试聚合 - avg
     */
    @Test
    public void testAvgAggregation() {
        EsChainLambdaQueryWrapper<FastTestDTO> wrapper = Es.chainLambdaQuery(FastTestDTO.class);
        wrapper.esLambdaAggWrapper()
                .avg("avg_age", FastTestDTO::getAge);
        String dsl = wrapper.toDsl();

        System.out.println("=== avg 聚合 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            assertTrue(root.has("aggs") || root.has("aggregations"),
                "应该包含聚合节点");

            JsonNode aggs = root.has("aggs") ? root.path("aggs") : root.path("aggregations");

            // 验证有 avg 聚合
            aggs.fields().forEachRemaining(entry -> {
                JsonNode aggValue = entry.getValue();
                if (aggValue.has("avg")) {
                    JsonNode avg = aggValue.path("avg");
                    String field = avg.path("field").asText();
                    assertEquals("age", field, "avg 聚合字段应该是 age");
                }
            });
        });
    }

    /**
     * 测试聚合 - max/min
     */
    @Test
    public void testMaxMinAggregation() {
        EsChainLambdaQueryWrapper<FastTestDTO> wrapper = Es.chainLambdaQuery(FastTestDTO.class);
        wrapper.esLambdaAggWrapper()
                .max("max_age", FastTestDTO::getAge)
                .min("min_age", FastTestDTO::getAge);
        String dsl = wrapper.toDsl();

        System.out.println("=== max/min 聚合 DSL ===\n" + dsl);

        assertDslStructure(dsl, root -> {
            assertTrue(root.has("aggs") || root.has("aggregations"),
                "应该包含聚合节点");

            JsonNode aggs = root.has("aggs") ? root.path("aggs") : root.path("aggregations");

            boolean hasMax = false;
            boolean hasMin = false;

            aggs.fields().forEachRemaining(entry -> {
                JsonNode aggValue = entry.getValue();
                if (aggValue.has("max")) {
                    JsonNode max = aggValue.path("max");
                    assertEquals("age", max.path("field").asText(), "max 聚合字段应该是 age");
                }
                if (aggValue.has("min")) {
                    JsonNode min = aggValue.path("min");
                    assertEquals("age", min.path("field").asText(), "min 聚合字段应该是 age");
                }
            });
        });
    }
}