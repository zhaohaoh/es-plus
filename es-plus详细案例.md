# Es-Plus 使用案例文档

## 1. 项目简介

Es-Plus 是一个强大的 Elasticsearch API 增强工具，旨在简化 Elasticsearch 的 CRUD 操作。

### 核心特性

- **无侵入**：在 rest-high-level-client 基础上扩展，只做增强不做改变
- **融合 MyBatis-Plus 语法**：适用于熟悉 MyBatis-Plus 的开发者
- **优雅的聚合封装**：让 ES 聚合操作更加简单
- **Lambda 表达式支持**：类型安全的查询构建
- **静态链式编程**：无需依赖注入，直接使用静态类操作
- **自动 Reindex**：索引结构变更时自动迁移数据
- **多版本支持**：兼容 ES 6.7、7.8、8.17
- **Nested 嵌套查询**：优雅的多级嵌套对象查询
- **多数据源**：支持多个 ES 集群连接
- **拦截器支持**：自定义 ES 执行前后的拦截逻辑

---

## 2. 环境配置

### 2.1 Maven 依赖

```xml
<dependency>
    <groupId>io.github.zhaohaoh</groupId>
    <artifactId>es-plus-spring-boot-starter</artifactId>
    <version>Latest Version</version>
</dependency>
```

### 2.2 配置文件 (application.properties)

```properties
# ES 地址（多个逗号分隔）- 默认数据源 master
es-plus.address=localhost:9200

# ES 认证信息
es-plus.username=
es-plus.password=

# ES 版本配置（7 或 8）
es-plus.global-config.version=7

# 是否开启自动 reindex（默认 false，生产环境慎用）
es-plus.global-config.index-auto-move=false

# 是否异步 reindex
es-plus.global-config.reindex-async=false

# 查询最大数量限制
es-plus.global-config.search-size=5000

# 索引统一环境后缀
es-plus.global-config.global-suffix=_test

# 全局默认分词器
# 可选：ep_ik_max_word, ep_ik_smart, ep_simple, ep_keyword, ep_stop, ep_whitespace, ep_pattern
es-plus.global-config.default-analyzer=ep_ik_max_word

# 自定义全局 refresh 策略
es-plus.global-config.refresh-policy=wait_until

# 全局默认 ES ID 字段
es-plus.global-config.global-es-id=id

# 多数据源配置（local 是数据源名称，可自定义）
es-plus.client-properties.local.address=localhost:9100
es-plus.client-properties.local.username=
es-plus.client-properties.local.password=
```

---

## 3. 实体类定义

### 3.1 基础实体类示例

```java
package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.annotation.Score;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 快速测试实体类
 * 演示常见字段类型的映射
 */
@Data
@EsIndex(index = "fast_test", alias = "fast_test_alias", tryReindex = true)
public class FastTestDTO {

    // ES 文档 ID
    @EsId
    private Long id;

    // KEYWORD 类型：精确匹配、聚合、排序
    @EsField(type = EsFieldType.KEYWORD, ignoreAbove = 512)
    private String username;

    // TEXT 类型：全文检索
    @EsField(type = EsFieldType.TEXT)
    private String text;

    // LONG 类型：数值类型
    @EsField(type = EsFieldType.LONG)
    private Long age;

    // KEYWORD 数组
    @EsField(type = EsFieldType.KEYWORD)
    private List<String> testList;

    // DATE 类型：日期类型（支持多种格式）
    @EsField(
        type = EsFieldType.DATE,
        esFormat = "yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis",
        dateFormat = "yyyy-MM-dd HH:mm:ss",
        timeZone = "+0"
    )
    private Date createTime;

    // 自定义字段名
    @EsField(type = EsFieldType.TEXT, name = "username_test")
    private String usernameTest;

    // 评分字段
    @Score
    private Float score;
}
```

### 3.2 嵌套对象实体类

```java
package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.Analyzer;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 包含嵌套对象的实体类
 */
@Data
@EsIndex(index = "sys_user2ttt", alias = "sys_user2ttt_alias", tryReindex = true)
public class SamplesEsDTO {

    @EsId
    private Long id;

    @EsField(type = EsFieldType.KEYWORD, normalizer = Analyzer.EP_NORMALIZER)
    private String username;

    // copyTo：将字段值复制到另一个字段
    @EsField(copyTo = "keyword")
    private String email;

    @EsField(copyTo = "keyword")
    private String phone;

    @EsField(type = EsFieldType.KEYWORD)
    private String keyword;

    @EsField(copyTo = "keyword")
    private String nickName;

    // store：是否单独存储字段值
    @EsField(store = true)
    private int sex;

    private Boolean lockState;

    @EsField(
        type = EsFieldType.DATE,
        esFormat = "yyyy-MM-dd'T'HH:mm:ss'Z' || yyyy-MM-dd HH:mm:ss || yyyy-MM-dd || yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    )
    private Date date;

    // NESTED 类型：嵌套对象数组
    @EsField(type = EsFieldType.NESTED)
    private List<SamplesNestedDTO> samplesNesteds;

    @EsField(type = EsFieldType.NESTED)
    private List<SamplesNestedDTO> samplesNestedObjects;
}
```

### 3.3 嵌套子对象

```java
package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.List;

/**
 * 二级嵌套对象
 */
@Data
public class SamplesNestedDTO {

    private Long id;

    @EsField(type = EsFieldType.KEYWORD)
    private String username;

    @EsField(type = EsFieldType.KEYWORD)
    private String email;

    private Boolean state;

    // 三级嵌套
    @EsField(type = EsFieldType.NESTED)
    private List<SamplesNestedInnerDTO> samplesNestedInner;
}
```

---

## 4. 服务类定义

### 4.1 继承 EsServiceImpl

```java
package com.es.plus.samples.service;

import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.samples.dto.FastTestDTO;
import org.springframework.stereotype.Service;

@Service
public class FastTestService extends EsServiceImpl<FastTestDTO> {
    // 继承后自动拥有 CRUD 方法
}
```

---

## 5. 普通查询案例

### 5.1 Term 精确查询

**适用场景**：精确匹配 KEYWORD 类型字段

```java
/**
 * Term 查询：精确匹配
 * 适用于 KEYWORD 类型字段
 */
@Test
public void termQuery() {
    // 方式一：使用 Service
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    // 方式二：使用静态类
    EsResponse<FastTestDTO> response2 = Es.chainLambdaQuery(FastTestDTO.class)
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    List<FastTestDTO> list = response.getList();
    System.out.println("查询结果：" + list);
}
```

### 5.2 Terms 多值查询

**适用场景**：查询字段值在指定集合中的文档

```java
/**
 * Terms 查询：字段值在指定集合中
 */
@Test
public void termsQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .terms(FastTestDTO::getUsername, "酷酷的", "小明", "小红")
        .search();

    System.out.println("查询到 " + response.getList().size() + " 条数据");
}
```

### 5.3 Match 全文检索

**适用场景**：TEXT 类型字段的分词匹配

```java
/**
 * Match 查询：全文检索（会分词）
 * 适用于 TEXT 类型字段
 */
@Test
public void matchQuery() {
    // 搜索包含"苹果"关键词的文本
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .match(FastTestDTO::getText, "苹果")
        .search();

    // 打印评分和结果
    for (FastTestDTO dto : response.getList()) {
        System.out.println("文档ID: " + dto.getId() + ", 评分: " + dto.getScore());
    }
}
```

### 5.4 MultiMatch 多字段匹配

**适用场景**：在多个字段中查找关键词

```java
/**
 * MultiMatch 查询：在多个字段中搜索
 * 只要有一个字段匹配即可
 */
@Test
public void multiMatchQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .multiMatch("苹果", FastTestDTO::getText, FastTestDTO::getUsername)
        .search();

    System.out.println("在 text 和 username 字段中搜索'苹果'，结果数：" + response.getList().size());
}
```

### 5.5 Match Phrase 短语匹配

**适用场景**：精确短语匹配，要求词序一致

```java
/**
 * Match Phrase 查询：短语匹配
 * 最能代替 wildcard 的查询方式，推荐使用
 * 词语必须按顺序出现
 */
@Test
public void matchPhraseQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .matchPhrase(FastTestDTO::getText, "第二篇文章苹果")
        .search();

    System.out.println("短语匹配结果：" + response.getList());
}
```

### 5.6 Range 范围查询

**适用场景**：数值、日期范围查询

```java
/**
 * Range 查询：范围查询
 * 适用于数值和日期类型
 */
@Test
public void rangeQuery() throws ParseException {
    // 日期范围查询
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of("+8")));
    Date startDate = sdf.parse("2023-07-01 08:00:00");
    Date endDate = new Date();

    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .range(FastTestDTO::getCreateTime, startDate.getTime(), endDate.getTime())
        .search();

    System.out.println("时间范围查询结果：" + response.getList().size() + " 条");

    // 数值范围查询
    EsResponse<FastTestDTO> response2 = fastTestService.esChainQueryWrapper()
        .ge(FastTestDTO::getAge, 18)  // 大于等于 18
        .le(FastTestDTO::getAge, 60)  // 小于等于 60
        .search();

    System.out.println("年龄 18-60 范围查询结果：" + response2.getList().size() + " 条");
}
```

### 5.7 Fuzzy 模糊查询

**适用场景**：容错查询，允许拼写错误

```java
/**
 * Fuzzy 查询：模糊查询（容错查询）
 * 允许一定程度的拼写错误
 */
@Test
public void fuzzyQuery() {
    // EpFuzziness.ONE：允许1个字符的差异
    // EpFuzziness.TWO：允许2个字符的差异
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .fuzzy(FastTestDTO::getUsername, "苦苦的", EpFuzziness.ONE)
        .search();

    // 可以查询到"酷酷的"（酷和苦差一个字符）
    System.out.println("模糊查询结果：" + response.getList());
}
```

### 5.8 Wildcard 通配符查询

**适用场景**：使用通配符进行匹配（性能较差，慎用）

```java
/**
 * Wildcard 查询：通配符查询
 * * 表示任意字符，? 表示单个字符
 * 注意：性能较差，建议使用 matchPhrase 代替
 */
@Test
public void wildcardQuery() {
    EsResponse<FastTestDTO> response = Es.chainLambdaQuery(FastTestDTO.class)
        .wildcard(FastTestDTO::getText, "*苹果*")
        .search();

    System.out.println("通配符查询结果：" + response.getList().size() + " 条");

    // 警告：通配符查询在长字符串上性能很差
    // 80 字符可能需要 400 毫秒以上
}
```

### 5.9 条件查询（动态查询）

**适用场景**：根据条件动态添加查询条件，常用于多条件搜索

```java
/**
 * 条件查询：根据参数是否为空动态添加查询条件
 * 第一个参数为 boolean 类型，true 时才添加该条件
 */
@Test
public void conditionalQuery() {
    String keyword = "苹果";
    String username = null;  // 为 null，不添加该条件
    Integer minAge = 18;
    Integer maxAge = null;   // 为 null，不添加该条件

    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .must()
        // 第一个参数是条件，只有为 true 时才添加该查询
        .match(keyword != null && !keyword.isEmpty(), FastTestDTO::getText, keyword)
        .term(username != null && !username.isEmpty(), FastTestDTO::getUsername, username)
        .ge(minAge != null, FastTestDTO::getAge, minAge)
        .le(maxAge != null, FastTestDTO::getAge, maxAge)
        .search();

    System.out.println("条件查询结果：" + response.getList());
}

/**
 * 条件查询实战：构建灵活的搜索接口
 */
@Test
public void dynamicSearchExample() {
    // 模拟前端传来的搜索参数（可能为空）
    String keyword = "苹果";
    String username = "";     // 空字符串
    Integer minAge = null;    // null
    Long id = 100L;

    EsResponse<FastTestDTO> response = Es.chainLambdaQuery(FastTestDTO.class)
        .must()
        // 关键词不为空时，才进行全文检索
        .match(keyword != null && !keyword.isEmpty(), FastTestDTO::getText, keyword)
        // 用户名不为空时，才进行精确匹配
        .term(username != null && !username.isEmpty(), FastTestDTO::getUsername, username)
        // 最小年龄不为 null 时，才添加范围条件
        .ge(minAge != null, FastTestDTO::getAge, minAge)
        // ID 不为 null 时，才进行精确匹配
        .term(id != null, FastTestDTO::getId, id)
        .search();

    System.out.println("动态搜索结果：" + response.getList().size() + " 条");
}

/**
 * 条件查询 + 嵌套：更复杂的场景
 */
@Test
public void conditionalWithNested() {
    String titleKeyword = "手机";
    String descKeyword = null;  // 不搜索描述
    List<String> tags = Arrays.asList("电子产品", "数码");
    Integer minPrice = 100;

    EsResponse<ProductDTO> response = Es.chainLambdaQuery(ProductDTO.class)
        .must()
        // 嵌套 should：标题或描述包含关键词
        .must(wrapper -> {
            var shouldWrapper = wrapper.should();
            // 标题关键词不为空时才搜索标题
            shouldWrapper.match(titleKeyword != null && !titleKeyword.isEmpty(),
                ProductDTO::getTitle, titleKeyword);
            // 描述关键词不为空时才搜索描述
            shouldWrapper.match(descKeyword != null && !descKeyword.isEmpty(),
                ProductDTO::getDescription, descKeyword);
        })
        // 标签列表不为空时才添加 terms 条件
        .terms(tags != null && !tags.isEmpty(), ProductDTO::getTags, tags.toArray())
        // 最低价格不为 null 时才添加范围条件
        .ge(minPrice != null, ProductDTO::getPrice, minPrice)
        .search();

    System.out.println("复杂条件查询结果：" + response.getList());
}
```

**条件查询最佳实践**：

1. **字符串判断**：`str != null && !str.isEmpty()`
2. **对象判断**：`obj != null`
3. **集合判断**：`list != null && !list.isEmpty()`
4. **数值判断**：`num != null`（注意使用包装类型，如 Integer、Long）

### 5.10 Must 布尔查询（AND）

**适用场景**：所有条件必须满足

```java
/**
 * Must 查询：所有条件必须满足（AND 逻辑）
 */
@Test
public void mustQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .must()  // 声明使用 must 逻辑
        .match(FastTestDTO::getText, "苹果")
        .ge(FastTestDTO::getAge, 18)
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    System.out.println("Must 查询（所有条件都满足）：" + response.getList());
}
```

### 5.10 Should 布尔查询（OR）

**适用场景**：满足任一条件即可

```java
/**
 * Should 查询：满足任一条件即可（OR 逻辑）
 */
@Test
public void shouldQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .should()  // 声明使用 should 逻辑
        .term(FastTestDTO::getUsername, "酷酷的")
        .term(FastTestDTO::getUsername, "小明")
        .term(FastTestDTO::getUsername, "小红")
        .search();

    System.out.println("Should 查询（满足任一条件）：" + response.getList().size() + " 条");
}
```

### 5.11 MustNot 布尔查询（NOT）

**适用场景**：排除符合条件的文档

```java
/**
 * MustNot 查询：排除符合条件的文档（NOT 逻辑）
 */
@Test
public void mustNotQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .mustNot()  // 排除条件
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    System.out.println("MustNot 查询（排除username=酷酷的）：" + response.getList().size() + " 条");
}
```

### 5.12 Filter 过滤查询

**适用场景**：不影响评分的过滤条件，性能更好

```java
/**
 * Filter 查询：过滤条件（不计算评分，性能更好）
 * 适用于精确匹配、范围查询等不需要评分的场景
 */
@Test
public void filterQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .filter()  // 声明使用 filter
        .term(FastTestDTO::getUsername, "酷酷的")
        .ge(FastTestDTO::getAge, 18)
        .search();

    System.out.println("Filter 查询（不计算评分）：" + response.getList());
}
```

### 5.13 组合布尔查询

**适用场景**：复杂的组合查询逻辑

```java
/**
 * 组合布尔查询：must、should、mustNot、filter 组合使用
 * 注意：这些条件需要通过嵌套的方式组合，不能直接在同一级混用
 */
@Test
public void combinedBoolQuery() {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        // 必须满足：年龄 >= 18
        .must()
        .ge(FastTestDTO::getAge, 18)
        // 嵌套 filter 条件：创建时间在指定范围内（不计算评分）
        .must(wrapper ->
            wrapper.filter()
                .range(FastTestDTO::getCreateTime, "2023-01-01", "2024-12-31")
        )
        // 嵌套 mustNot 条件：排除黑名单用户
        .must(wrapper ->
            wrapper.mustNot()
                .term(FastTestDTO::getUsername, "黑名单用户")
        )
        // 嵌套 should 条件：text 包含"苹果"或"香蕉"
        .must(wrapper ->
            wrapper.should()
                .match(FastTestDTO::getText, "苹果")
                .match(FastTestDTO::getText, "香蕉")
        )
        .search();

    System.out.println("组合查询结果：" + response.getList().size() + " 条");
}
```

### 5.14 布尔查询嵌套规则说明

**重要提示**：在 Es-Plus 中，`must()`、`should()`、`mustNot()`、`filter()` 这些布尔查询条件**不能在同一级别直接混用**，必须通过嵌套的方式组合。

```java
// ❌ 错误示例：在同一级混用多个布尔条件
EsResponse<FastTestDTO> wrong = fastTestService.esChainQueryWrapper()
    .should()
    .match(FastTestDTO::getText, "苹果")
    .filter()  // ❌ 错误！不能直接切换到 filter
    .term(FastTestDTO::getUsername, "张三")
    .mustNot()  // ❌ 错误！不能直接切换到 mustNot
    .term(FastTestDTO::getDeleteState, true)
    .search();

// ✅ 正确示例：使用嵌套方式组合
EsResponse<FastTestDTO> correct = fastTestService.esChainQueryWrapper()
    // 使用 must 作为最外层
    .must()
    // 嵌套 should：满足任一关键词
    .must(wrapper ->
        wrapper.should()
            .match(FastTestDTO::getText, "苹果")
            .match(FastTestDTO::getText, "香蕉")
    )
    // 嵌套 filter：精确匹配条件
    .must(wrapper ->
        wrapper.filter()
            .term(FastTestDTO::getUsername, "张三")
    )
    // 嵌套 mustNot：排除条件
    .must(wrapper ->
        wrapper.mustNot()
            .term(FastTestDTO::getDeleteState, true)
    )
    .search();
```

**嵌套规则总结**：

1. **同一类型条件可以连续调用**：
```java
// ✅ 正确：连续使用 must 条件
EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
    .must()
    .term(FastTestDTO::getAge, 18)
    .term(FastTestDTO::getUsername, "张三")
    .search();
```

2. **不同类型条件必须嵌套**：
```java
// ✅ 正确：通过 must(wrapper -> ...) 嵌套其他类型
EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
    .must()
    .term(FastTestDTO::getAge, 18)
    .must(wrapper ->  // 嵌套一个新的条件组
        wrapper.should()
            .match(FastTestDTO::getText, "苹果")
            .match(FastTestDTO::getText, "香蕉")
    )
    .search();
```

3. **多层嵌套**：
```java
// ✅ 正确：可以多层嵌套
EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
    .must()
    .must(wrapper1 ->
        wrapper1.should()
            .match(FastTestDTO::getText, "关键词1")
            .should(wrapper2 ->  // 第三层嵌套
                wrapper2.must()
                    .term(FastTestDTO::getStatus, 1)
            )
    )
    .search();
```

### 5.15 嵌套布尔查询实战

**适用场景**：更复杂的嵌套逻辑

```java
/**
 * 嵌套布尔查询：在 must 中嵌套 should
 * 实现：(A AND B) AND (C OR D)
 */
@Test
public void nestedBoolQuery() {
    EsResponse<SamplesEsDTO> response = samplesEsService.esChainQueryWrapper()
        .must()
        .terms(SamplesEsDTO::getUsername, "admin", "hzh", "shi")
        // 嵌套 should 条件
        .must(a ->
            a.should()
                .term(SamplesEsDTO::getNickName, "张三")
                .term(SamplesEsDTO::getPhone, "13868591111")
        )
        .search();

    System.out.println("嵌套布尔查询结果：" + response.getList());
}
```

### 5.15 Nested 嵌套对象查询

**适用场景**：查询嵌套对象数组中的元素

```java
/**
 * Nested 查询：嵌套对象查询
 * 用于查询 NESTED 类型的字段
 */
@Test
public void nestedQuery() {
    // 方式一：使用字符串字段名
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper1 =
        samplesEsService.esChainQueryWrapper().must()
            .nestedQuery("samplesNesteds", esQueryWrap -> {
                // 在嵌套查询内部，mustNot 可以连续使用（同一类型）
                esQueryWrap.mustNot()
                    .term("state", false)
                    .term("id", 2L);
            });
    EsResponse<SamplesEsDTO> response1 = queryWrapper1.search();

    // 方式二：使用 Lambda 表达式（推荐，类型安全）
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper2 =
        samplesEsService.esChainQueryWrapper().must()
            .nestedQuery(
                SamplesEsDTO::getSamplesNesteds,
                SamplesNestedDTO.class,
                esQueryWrap -> {
                    // 在嵌套查询内部，mustNot 可以连续使用（同一类型）
                    esQueryWrap.mustNot()
                        .term(SamplesNestedDTO::getState, false)
                        .term(SamplesNestedDTO::getId, 2L);
                }
            );
    EsResponse<SamplesEsDTO> response2 = queryWrapper2.search();

    System.out.println("嵌套查询结果：" + response2.getList());
}
```

### 5.16 三级嵌套查询（带 InnerHits）

**适用场景**：多级嵌套对象查询，并获取匹配的嵌套文档

```java
/**
 * 三级嵌套查询 + InnerHits
 * 可以获取匹配的嵌套文档内容
 */
@Test
public void threeNested() {
    // 配置 InnerHits（用于返回匹配的嵌套文档）
    EpInnerHitBuilder innerHitBuilder = new EpInnerHitBuilder("test");
    innerHitBuilder.setSize(10);
    innerHitBuilder.setFetchSourceContext(new EpFetchSourceContext(true));

    // 一级查询条件
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper =
        samplesEsService.esChainQueryWrapper().must().fetch(true)
            // 二级嵌套查询
            .nested("samplesNesteds", esQueryWrap -> {
                esQueryWrap.must().term("username", "3");

                // 配置二级 InnerHits
                EpInnerHitBuilder innerHitBuilder1 = new EpInnerHitBuilder();
                innerHitBuilder1.setSize(100);

                // 三级嵌套查询
                esQueryWrap.must().nested("samplesNesteds.samplesNestedInner",
                    innerQuery -> {
                        innerQuery.must().term("username", "3");
                    }, EpScoreMode.None, innerHitBuilder1);

            }, EpScoreMode.None, innerHitBuilder);

    EsResponse<SamplesEsDTO> response = queryWrapper.search();

    // 获取 InnerHits 结果
    EsHits innerHits = response.getInnerHits();
    List<EsHit> esHitList = innerHits.getEsHitList();

    for (EsHit esHit : esHitList) {
        // 获取二级嵌套对象
        long innerHitsTotal = esHit.getInnerHitsTotal("test");
        List<SamplesNestedDTO> nestedList = esHit.getInnerList(SamplesNestedDTO.class, "test");

        // 获取三级嵌套对象
        EsHits esHitEsHits = esHit.getEsInnerHits("test");
        for (EsHit hit : esHitEsHits.getEsHitList()) {
            List<SamplesNestedInnerDTO> innerList =
                hit.getInnerList(SamplesNestedInnerDTO.class, "samplesNesteds.samplesNestedInner");
            System.out.println("三级嵌套对象：" + innerList);
        }
    }

    System.out.println("三级嵌套查询结果：" + response.getList());
}
```

### 5.17 分页查询

**适用场景**：分页获取数据

```java
/**
 * 分页查询
 */
@Test
public void pageQuery() {
    int page = 1;  // 页码（从 1 开始）
    int size = 10; // 每页数量

    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .match(FastTestDTO::getText, "苹果")
        .searchPage(page, size);

    System.out.println("第 " + page + " 页，共 " + response.getTotal() + " 条");
    System.out.println("当前页数据：" + response.getList().size() + " 条");
}
```

### 5.18 Scroll 滚动查询

**适用场景**：大数据量遍历（深度分页）

```java
/**
 * Scroll 滚动查询：适用于大数据量遍历
 * 比深度分页性能更好
 */
@Test
public void scrollQuery() {
    String scrollId = null;
    int page = 3;  // 滚动次数
    int size = 100; // 每次获取数量

    for (int i = 0; i < page; i++) {
        EsResponse<SamplesEsDTO> response =
            samplesEsService.esChainQueryWrapper().must()
                .sortByAsc("id")
                .scroll(size, scrollId);

        scrollId = response.getScrollId();
        System.out.println("第 " + (i + 1) + " 次滚动，获取 " + response.getList().size() + " 条");
    }
}
```

### 5.19 SearchAfter 深度分页

**适用场景**：深度分页（性能优于传统分页）

```java
/**
 * SearchAfter 查询：深度分页的高性能方案
 * 通过上一页的排序值获取下一页
 */
@Test
public void searchAfterQuery() {
    // 第一页
    EsResponse<SamplesEsDTO> response1 = Es.chainLambdaQuery(SamplesEsDTO.class)
        .orderBy("ASC", SamplesEsDTO::getId)
        .searchAfter(null);

    System.out.println("第一页：" + response1.getList().size() + " 条");

    // 第二页（使用上一页的尾部排序值）
    Object[] tailSortValues = response1.getTailSortValues();
    EsResponse<SamplesEsDTO> response2 = Es.chainLambdaQuery(SamplesEsDTO.class)
        .orderBy("ASC", SamplesEsDTO::getId)
        .searchAfter(tailSortValues);

    System.out.println("第二页：" + response2.getList().size() + " 条");
}
```

### 5.20 排序查询

**适用场景**：结果排序

```java
/**
 * 排序查询
 */
@Test
public void sortQuery() {
    // 单字段排序
    EsResponse<FastTestDTO> response1 = fastTestService.esChainQueryWrapper()
        .sortByAsc(FastTestDTO::getAge)
        .search();

    // 多字段排序
    EsResponse<FastTestDTO> response2 = fastTestService.esChainQueryWrapper()
        .sortByDesc(FastTestDTO::getCreateTime)
        .sortByAsc(FastTestDTO::getAge)
        .search();

    System.out.println("排序结果：" + response2.getList());
}
```

### 5.21 Count 统计数量

**适用场景**：只统计数量，不返回文档

```java
/**
 * Count 查询：统计文档数量
 */
@Test
public void countQuery() {
    // 方式一：使用 count 方法
    long count1 = fastTestService.count(null);
    System.out.println("总文档数：" + count1);

    // 方式二：带条件的 count
    long count2 = Es.chainLambdaQuery(FastTestDTO.class)
        .term(FastTestDTO::getUsername, "酷酷的")
        .count();

    System.out.println("username=酷酷的 的文档数：" + count2);
}
```

### 5.22 指定返回字段

**适用场景**：只返回部分字段，减少网络传输

```java
/**
 * 指定返回字段：使用 includes 和 excludes
 */
@Test
public void selectFieldsQuery() {
    // 方式一：使用 includes 指定需要返回的字段
    EsResponse<FastTestDTO> response1 = fastTestService.esChainQueryWrapper()
        .includes(FastTestDTO::getId, FastTestDTO::getUsername, FastTestDTO::getAge)
        .search();

    // 只有 id、username、age 有值，其他字段为 null
    System.out.println("指定返回字段：" + response1.getList());

    // 方式二：使用 excludes 指定需要排除的字段
    EsResponse<FastTestDTO> response2 = fastTestService.esChainQueryWrapper()
        .excludes(FastTestDTO::getText, FastTestDTO::getCreateTime)
        .search();

    // 除了 text 和 createTime，其他字段都有值
    System.out.println("排除字段：" + response2.getList());

    // 方式三：includes 和 excludes 组合使用
    EsResponse<FastTestDTO> response3 = fastTestService.esChainQueryWrapper()
        .includes(FastTestDTO::getId, FastTestDTO::getUsername, FastTestDTO::getText)
        .excludes(FastTestDTO::getText)  // 排除优先级更高
        .search();

    // 只返回 id 和 username（text 被 excludes 排除了）
    System.out.println("组合使用：" + response3.getList());
}
```

### 5.23 Profile 性能分析

**适用场景**：分析查询性能

```java
/**
 * Profile 查询：性能分析
 * 可以查看查询各阶段的耗时
 */
@Test
public void profileQuery() {
    EsResponse<SamplesEsDTO> response = samplesEsService.esChainQueryWrapper()
        .must()
        .terms(SamplesEsDTO::getUsername, "admin", "hzh", "shi")
        .profile()  // 启用性能分析
        .search();

    // 在响应中可以获取性能分析信息
    System.out.println("Profile 结果：" + response);
}
```

### 5.24 静态链式查询（无需注入 Service）

**适用场景**：快速查询，无需创建 Service 类

```java
/**
 * 静态链式查询：使用 Es 静态类直接操作
 * 无需注入 Service，适合快速操作
 */
@Test
public void staticChainQuery() {
    // 查询有实体类的索引
    EsResponse<FastTestDTO> response1 = Es.chainLambdaQuery(FastTestDTO.class)
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    // 查询没有实体类的索引（使用 Map）
    EsResponse<Map> response2 = Es.chainQuery(Map.class)
        .index("sys_user2ttt_s0")
        .term("username", "admin")
        .search();

    System.out.println("静态查询结果：" + response2.getList());
}
```

---

## 6. 聚合查询案例

### 6.1 Terms 聚合（分组统计）

**适用场景**：按字段值分组统计

```java
/**
 * Terms 聚合：类似 SQL 的 GROUP BY
 * 统计每个值的文档数量
 */
@Test
public void termsAggregation() {
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper =
        samplesEsService.esChainQueryWrapper().must()
            .ge(SamplesEsDTO::getId, 1);

    // 配置聚合
    queryWrapper.esLambdaAggWrapper()
        .terms(SamplesEsDTO::getUsername, e -> e.size(100));  // 返回前 100 个桶

    EsResponse<SamplesEsDTO> response = queryWrapper.search();

    // 获取聚合结果
    EsAggregationsResponse<SamplesEsDTO> aggResponse = response.getEsAggsResponse();

    // 方式一：获取 Terms 对象
    Terms terms = aggResponse.getTerms(SamplesEsDTO::getUsername);
    for (Terms.Bucket bucket : terms.getBuckets()) {
        String key = bucket.getKeyAsString();
        long count = bucket.getDocCount();
        System.out.println(key + ": " + count + " 条");
    }

    // 方式二：获取 Map（更简单）
    Map<String, Long> termsAsMap = aggResponse.getTermsAsMap(SamplesEsDTO::getUsername);
    System.out.println("Terms 聚合结果：" + termsAsMap);
}
```

### 6.2 子聚合（Sum、Count、Avg 等）

**适用场景**：在分组基础上进行统计计算

```java
/**
 * 子聚合：在 Terms 聚合的每个桶中进行统计
 * 类似 SQL：SELECT username, SUM(id), COUNT(*) FROM table GROUP BY username
 */
@Test
public void subAggregation() {
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper =
        samplesEsService.esChainQueryWrapper().must()
            .ge(SamplesEsDTO::getId, 1);

    // 配置聚合和子聚合
    queryWrapper.esLambdaAggWrapper()
        .terms(SamplesEsDTO::getUsername, e -> e.size(100))
        // 在每个 username 分组中统计 id 的总和
        .subAgg(t -> t.sum(SamplesEsDTO::getId))
        // 在每个 username 分组中统计文档数量
        .subAgg(t -> t.count(SamplesEsDTO::getId));

    EsResponse<SamplesEsDTO> response = queryWrapper.search();

    // 获取聚合结果
    EsPlusAggregations<SamplesEsDTO> aggResponse =
        (EsPlusAggregations<SamplesEsDTO>) response.getEsAggsResponse();

    // 获取原生 Aggregations 对象进行复杂处理
    Aggregations aggregations = aggResponse.getAggregations();
    System.out.println("子聚合结果：" + aggregations.getAsMap());
}
```

### 6.3 Filter 聚合

**适用场景**：对特定条件的文档进行聚合

```java
/**
 * Filter 聚合：只对满足条件的文档进行聚合
 */
@Test
public void filterAggregation() {
    EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper =
        samplesEsService.esChainQueryWrapper().must()
            .ge(SamplesEsDTO::getId, 1);

    // 配置 Filter 聚合
    queryWrapper.esLambdaAggWrapper()
        .filter(SamplesEsDTO::getUsername, () -> {
            // 定义过滤条件
            EsWrapper<SamplesEsDTO> filterWrapper = samplesEsService.esChainQueryWrapper();
            filterWrapper.match(SamplesEsDTO::getEmail, "test@example.com");
            return filterWrapper;
        })
        .terms(SamplesEsDTO::getUsername, e -> e.size(100));

    EsResponse<SamplesEsDTO> response = queryWrapper.search();
    System.out.println("Filter 聚合结果：" + response.getEsAggsResponse());
}
```

### 6.4 普通聚合（非 Lambda）

**适用场景**：不使用 Lambda，直接使用字符串字段名

```java
/**
 * 普通聚合：使用字符串字段名
 * 适用于动态字段或 Map 类型
 */
@Test
public void normalAggregation() {
    EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(Map.class)
        .index("sys_user2ttt_alias")
        .must()
        .match("username", "HZH")
        .term("email", "abc");

    // 使用字符串字段名进行聚合
    queryWrapper.esAggWrapper().terms("keyword");

    EsResponse<Map> response = queryWrapper.search();

    // 获取聚合结果
    Map<String, Long> termsAsMap = response.getEsAggsResponse().getTermsAsMap("keyword");
    System.out.println("聚合结果：" + termsAsMap);
}
```

### 6.5 独立聚合查询（不返回文档）

**适用场景**：只需要聚合结果，不需要文档数据

```java
/**
 * 独立聚合查询：只返回聚合结果
 * 使用 aggregations() 方法代替 search()
 */
@Test
public void aggregationOnly() {
    EsChainLambdaQueryWrapper<FastTestDTO> queryWrapper =
        fastTestService.esChainQueryWrapper();

    // 配置聚合
    EsAggWrapper<FastTestDTO> aggWrapper = queryWrapper.esAggWrapper();
    aggWrapper.terms("username");

    // 只执行聚合，不返回文档（性能更好）
    EsAggResponse<FastTestDTO> aggResponse = queryWrapper.aggregations();

    System.out.println("只返回聚合结果：" + aggResponse);
}
```

---

## 7. 增删改操作案例

### 7.1 保存单条文档

**适用场景**：新增文档

```java
/**
 * 保存单条文档
 */
@Test
public void saveDocument() {
    // 使用 Service 保存
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setText("我的个人介绍，我是一篇文章，用于搜索");
    dto.setAge(25L);
    dto.setUsername("酷酷的");
    dto.setCreateTime(new Date());

    fastTestService.save(dto);

    // 使用静态类保存
    Es.chainUpdate(FastTestDTO.class).save(dto);

    System.out.println("保存成功，文档ID：" + dto.getId());
}
```

### 7.2 更新单条文档

**适用场景**：更新已存在的文档

```java
/**
 * 更新单条文档
 */
@Test
public void updateDocument() {
    FastTestDTO dto = new FastTestDTO();
    dto.setId(800000005L);
    dto.setText("我改成果了2222");

    // 使用 Service 更新
    fastTestService.update(dto);

    // 使用静态类更新
    Es.chainUpdate(FastTestDTO.class).update(dto);

    System.out.println("更新成功");
}
```

### 7.3 保存或更新

**适用场景**：存在则更新，不存在则新增

```java
/**
 * 保存或更新：存在则更新，不存在则新增
 */
@Test
public void saveOrUpdate() {
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setText("苹果手机很好用");
    dto.setAge(133L);
    dto.setUsername("酷酷的11111");
    dto.setCreateTime(new Date());

    // 根据 ID 判断是否存在，存在则更新，不存在则新增
    Es.chainUpdate(FastTestDTO.class).saveOrUpdate(dto);

    System.out.println("保存或更新成功");
}
```

### 7.4 批量保存

**适用场景**：批量新增文档

```java
/**
 * 批量保存
 */
@Test
public void saveBatch() {
    List<FastTestDTO> list = new ArrayList<>();

    for (int i = 800000010; i < 800000020; i++) {
        FastTestDTO dto = new FastTestDTO();
        dto.setId((long) i);
        dto.setText("特殊的8");
        dto.setAge(18L);
        dto.setUsername("特殊的8");
        dto.setCreateTime(new Date());
        list.add(dto);
    }

    // 使用 Service 批量保存
    fastTestService.saveBatch(list);

    // 使用静态类批量保存
    Es.chainUpdate(FastTestDTO.class).saveBatch(list);

    System.out.println("批量保存成功，共 " + list.size() + " 条");
}
```

### 7.5 批量删除

**适用场景**：根据 ID 删除多条文档

```java
/**
 * 批量删除
 */
@Test
public void deleteBatch() {
    List<String> ids = Arrays.asList("800000006", "800000007", "800000008");

    // 使用 Service 删除
    fastTestService.removeByIds(ids);

    // 使用静态类删除
    Es.chainUpdate(FastTestDTO.class).removeByIds(ids);

    System.out.println("批量删除成功，共 " + ids.size() + " 条");
}
```

### 7.6 删除全部

**适用场景**：删除索引中的所有文档

```java
/**
 * 删除全部文档
 */
@Test
public void deleteAll() {
    samplesEsService.esChainUpdateWrapper().remove();

    System.out.println("已删除索引中的所有文档");
}
```

### 7.7 UpdateByQuery 条件更新

**适用场景**：根据查询条件批量更新

```java
/**
 * UpdateByQuery：根据条件批量更新
 * 类似 SQL：UPDATE table SET email='bbbbbb' WHERE username MATCH 'ggghhh'
 */
@Test
public void updateByQuery() {
    // 方式一：使用 UpdateWrapper
    EsLambdaUpdateWrapper<SamplesEsDTO> updateWrapper = new EsLambdaUpdateWrapper<>();
    updateWrapper.match(SamplesEsDTO::getUsername, "ggghhh")
                 .set(SamplesEsDTO::getEmail, "bbbbbb");

    samplesEsService.updateByQuery(updateWrapper);

    // 方式二：使用静态类
    EpBulkResponse response = Es.chainUpdate(FastTestDTO.class)
        .terms("id", "800000005", "800000004")
        .set("text", "新结果哦")
        .updateByQuery();

    System.out.println("条件更新成功，影响文档数：" + response.getUpdated());
}
```

### 7.8 异步批量保存

**适用场景**：大批量数据异步写入，提高性能

```java
/**
 * 异步批量保存：使用 BulkProcessor 异步写入
 * 需要在实体类上添加 @BulkProcessor 注解
 */
@Test
public void saveBatchAsync() {
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setText("asdasdasdsa");

    // 异步保存（写入缓冲区，定时批量提交）
    Es.chainUpdate(FastTestDTO.class)
        .saveBatchAsyncProcessor(Collections.singletonList(dto));

    System.out.println("已提交到异步队列");
}
```

### 7.9 异步批量更新

**适用场景**：大批量数据异步更新

```java
/**
 * 异步批量更新
 */
@Test
public void updateBatchAsync() {
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setText("ssssss");

    Es.chainUpdate(FastTestDTO.class)
        .updateBatchAsyncProcessor(Collections.singletonList(dto));

    System.out.println("已提交到异步更新队列");
}
```

### 7.10 异步批量保存或更新

**适用场景**：大批量数据异步保存或更新

```java
/**
 * 异步批量保存或更新
 */
@Test
public void saveOrUpdateBatchAsync() {
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setText("bvvbdfbfd");

    Es.chainUpdate(FastTestDTO.class)
        .saveOrUpdateBatchAsyncProcessor(Collections.singletonList(dto));

    System.out.println("已提交到异步保存或更新队列");
}
```

### 7.11 保存 Map（无实体类）

**适用场景**：操作没有实体类的索引

```java
/**
 * 保存 Map：无需实体类，直接操作索引
 */
@Test
public void saveMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", "fsdfsfds");
    map.put("id", "d73d1b4e46244b0db766987759d6e");

    // 指定索引保存
    Es.chainUpdate(Map.class).index("sys_user2ttt").save(map);

    System.out.println("Map 保存成功");
}
```

### 7.12 保存嵌套对象

**适用场景**：保存包含嵌套对象的文档

```java
/**
 * 保存嵌套对象
 */
@Test
public void saveNested() {
    SamplesEsDTO dto = new SamplesEsDTO();
    dto.setEmail("test@example.com");
    dto.setUsername("testuser");
    dto.setDate(new Date());
    dto.setId(5L);

    // 创建嵌套对象列表
    List<SamplesNestedDTO> nestedList = new ArrayList<>();

    SamplesNestedDTO nested1 = new SamplesNestedDTO();
    nested1.setEmail("3");
    nested1.setUsername("3");
    nested1.setId(1L);
    nested1.setState(false);
    nestedList.add(nested1);

    SamplesNestedDTO nested2 = new SamplesNestedDTO();
    nested2.setEmail("4");
    nested2.setUsername("4");
    nested2.setId(2L);
    nested2.setState(true);
    nestedList.add(nested2);

    dto.setSamplesNesteds(nestedList);

    // 保存
    samplesEsService.save(dto);

    System.out.println("嵌套对象保存成功");
}
```

---

## 8. 索引管理案例

### 8.1 创建索引

**适用场景**：创建新索引

```java
/**
 * 创建索引
 */
@Test
public void createIndex() {
    // 方式一：根据实体类创建索引和映射
    Es.chainIndex()
        .createIndex(FastTestDTO.class)
        .putMapping(FastTestDTO.class);

    // 方式二：手动指定索引名
    Es.chainIndex()
        .index("my_custom_index")
        .createIndex()
        .putMapping(FastTestDTO.class);

    System.out.println("索引创建成功");
}
```

### 8.2 删除索引

**适用场景**：删除索引及其所有数据

```java
/**
 * 删除索引
 */
@Test
public void deleteIndex() {
    Es.chainIndex().deleteIndex("fast_test_s1");

    System.out.println("索引删除成功");
}
```

### 8.3 判断索引是否存在

**适用场景**：检查索引是否存在

```java
/**
 * 判断索引是否存在
 */
@Test
public void indexExists() {
    boolean exists = Es.chainIndex().index("fast_test").indexExists();

    System.out.println("索引是否存在：" + exists);
}
```

### 8.4 Reindex 数据迁移

**适用场景**：索引结构变更后迁移数据

```java
/**
 * Reindex：将数据从旧索引迁移到新索引
 * 适用于索引映射变更的场景
 */
@Test
public void reindex() {
    // 定义新的映射
    String mappingJson = "{\n" +
        "    \"properties\": {\n" +
        "        \"text\": {\n" +
        "            \"type\": \"keyword\"\n" +
        "        }\n" +
        "    }\n" +
        "}";

    Map<String, Object> mappingMap = JsonUtils.toMap(mappingJson);
    Map<String, Object> changeMapping = (Map<String, Object>) mappingMap.get("properties");

    // 从 fast_test_s1 迁移到 reindex_test
    Es.chainIndex().reindex("reindex_test", "fast_test_s1", changeMapping);

    System.out.println("Reindex 完成");
}
```

### 8.5 更新索引 Settings

**适用场景**：修改索引配置（如慢查询日志）

```java
/**
 * 更新索引 Settings
 */
@Test
public void updateSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.put(EsSettingsConstants.QUERY_INFO, "0s");  // 慢查询 INFO 级别阈值
    settings.put(EsSettingsConstants.QUERY_WARN, "0s");  // 慢查询 WARN 级别阈值
    settings.put(EsSettingsConstants.SEARCH_LEVEL, "info"); // 慢查询日志级别

    samplesEsService.updateSettings(settings);

    System.out.println("Settings 更新成功");
}
```

### 8.6 获取索引 Mapping

**适用场景**：查看索引的映射结构

```java
/**
 * 获取索引 Mapping
 */
@Test
public void getMapping() {
    // 使用 ES 原生 API 获取
    // GetMappingsResponse mapping = esClient.getMapping("fast_test");

    System.out.println("获取 Mapping");
}
```

---

## 9. 高级功能案例

### 9.1 静态链式编程（无需注入）

**适用场景**：快速操作，无需创建 Service

```java
/**
 * 静态链式编程：使用 Es 静态类
 * 无需注入 Service，适合快速操作和工具类
 */
@Test
public void staticChainProgramming() {
    // 查询
    EsResponse<FastTestDTO> queryResult = Es.chainLambdaQuery(FastTestDTO.class)
        .term(FastTestDTO::getUsername, "酷酷的")
        .search();

    // 新增
    FastTestDTO dto = new FastTestDTO();
    dto.setId(1L);
    dto.setUsername("张三");
    Es.chainUpdate(FastTestDTO.class).save(dto);

    // 更新
    Es.chainUpdate(FastTestDTO.class)
        .term(FastTestDTO::getId, 1L)
        .set(FastTestDTO::getUsername, "李四")
        .updateByQuery();

    // 删除
    Es.chainUpdate(FastTestDTO.class).removeByIds(Collections.singletonList("1"));

    // 索引操作
    boolean exists = Es.chainIndex().index("fast_test").indexExists();

    System.out.println("静态链式编程完成");
}
```

### 9.2 多数据源

**适用场景**：连接多个 ES 集群

```java
/**
 * 多数据源：连接多个 ES 集群
 *
 * 配置文件中定义多个数据源：
 * es-plus.client-properties.local.address=localhost:9100
 * es-plus.client-properties.dz.address=localhost:9200
 */
@Test
public void multiDataSource() {
    // 方式一：在实体类上使用 @EsIndex 指定数据源
    // @EsIndex(index = "fast_test", client = "local")

    // 方式二：在查询时指定数据源
    EsPlusClientFacade dzClient = ClientContext.getClient("dz");
    EsChainQueryWrapper<Map> queryWrapper = Es.chainQuery(dzClient, Map.class)
        .nestedQuery("sourceGoodsInfo", a -> a.term("relatedStatus", 1))
        .index("yph_product_dy");

    EsResponse<Map> response = queryWrapper.search();
    System.out.println("多数据源查询结果：" + response.getList());
}
```

### 9.3 自定义拦截器

**适用场景**：在 ES 操作前后添加自定义逻辑（如日志、监控）

```java
/**
 * 自定义拦截器：拦截 ES 操作
 * 可用于：日志记录、性能监控、参数修改等
 */
@Component
@EsInterceptors(value = {
    // 指定要拦截的类和方法
    @InterceptorElement(type = EsPlusClient.class, methodName = "search")
})
public class EsSearchAfterInterceptor implements EsInterceptor {

    @Override
    public void before(String index, Method method, Object[] args) {
        // 执行前拦截
        System.out.println("ES 查询前拦截，索引：" + index);

        // 获取查询参数
        for (Object arg : args) {
            if (arg instanceof EsParamWrapper) {
                EsParamWrapper esParamWrapper = (EsParamWrapper) arg;
                EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();

                Integer page = esQueryParamWrapper.getPage();
                Integer size = esQueryParamWrapper.getSize();

                System.out.println("分页参数：page=" + page + ", size=" + size);

                // 可以修改参数
                // esQueryParamWrapper.setSize(100);
            }
        }
    }

    @Override
    public void after(String index, Method method, Object[] args, Object result) {
        // 执行后拦截
        System.out.println("ES 查询后拦截，索引：" + index);

        // 获取查询结果
        if (result instanceof EsResponse) {
            EsResponse response = (EsResponse) result;
            System.out.println("查询结果数量：" + response.getList().size());
            System.out.println("查询总数：" + response.getTotal());
        }
    }
}
```

**拦截器使用示例**：

```java
// 拦截器配置
@EsInterceptors(value = {
    // 拦截 EsPlusClient 的 search 方法
    @InterceptorElement(type = EsPlusClient.class, methodName = "search"),
    // 拦截 EsPlusClient 的 save 方法
    @InterceptorElement(type = EsPlusClient.class, methodName = "save"),
    // 拦截指定索引的操作
    @InterceptorElement(type = EsPlusClient.class, methodName = "search", index = "fast_test")
})
public class MyEsInterceptor implements EsInterceptor {
    // 实现拦截逻辑
}
```

---

## 10. 完整示例

### 10.1 用户搜索功能完整示例

```java
package com.es.plus.samples.service;

import com.es.plus.common.params.EsResponse;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.samples.dto.SamplesEsDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户搜索服务
 */
@Service
public class UserSearchService extends EsServiceImpl<SamplesEsDTO> {

    /**
     * 综合搜索用户（带条件判断）
     * @param keyword 搜索关键词
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @param excludeUsernames 排除的用户名列表
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果
     */
    public EsResponse<SamplesEsDTO> searchUsers(
            String keyword,
            Integer minAge,
            Integer maxAge,
            List<String> excludeUsernames,
            int page,
            int size) {

        return esChainQueryWrapper()
            // 必须满足：关键词匹配（username 或 nickName 或 email）
            // 只有 keyword 不为空时才添加该条件
            .must(keyword != null && !keyword.isEmpty(), wrapper ->
                wrapper.should()
                    .match(SamplesEsDTO::getUsername, keyword)
                    .match(SamplesEsDTO::getNickName, keyword)
                    .match(SamplesEsDTO::getEmail, keyword)
            )
            // 必须满足：年龄范围（使用 filter 不计算评分）
            // 只有年龄参数不为 null 时才添加
            .must((minAge != null || maxAge != null), wrapper -> {
                var filterWrapper = wrapper.filter();
                filterWrapper.ge(minAge != null, SamplesEsDTO::getSex, minAge);
                filterWrapper.le(maxAge != null, SamplesEsDTO::getSex, maxAge);
            })
            // 必须满足：排除指定用户和已删除用户
            .must(wrapper -> {
                var mustNotWrapper = wrapper.mustNot();
                // 只有排除列表不为空时才添加
                mustNotWrapper.terms(excludeUsernames != null && !excludeUsernames.isEmpty(),
                    SamplesEsDTO::getUsername, excludeUsernames.toArray());
                // 始终排除已删除用户
                mustNotWrapper.term(SamplesEsDTO::getDeleteState, true);
            })
            // 排序
            .sortByDesc(SamplesEsDTO::getDate)
            // 分页查询
            .searchPage(page, size);
    }

    /**
     * 统计用户分布
     */
    public void statisticsUsers() {
        EsResponse<SamplesEsDTO> response = esChainQueryWrapper()
            .esLambdaAggWrapper()
            // 按性别统计
            .terms(SamplesEsDTO::getSex)
            // 按锁定状态统计
            .terms(SamplesEsDTO::getLockState)
            .search();

        // 获取聚合结果
        System.out.println(response.getEsAggsResponse());
    }
}
```

### 10.2 电商商品搜索完整示例

```java
/**
 * 电商商品搜索服务
 */
@Service
public class ProductSearchService extends EsServiceImpl<ProductDTO> {

    /**
     * 商品综合搜索（带条件判断）
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param brandIds 品牌ID列表
     * @param sortBy 排序字段（price/sales/createTime）
     * @param sortOrder 排序方向（ASC/DESC）
     * @param page 页码
     * @param size 每页数量
     */
    public EsResponse<ProductDTO> searchProducts(
            String keyword,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            List<Long> brandIds,
            String sortBy,
            String sortOrder,
            int page,
            int size) {

        var queryWrapper = esChainQueryWrapper().must();

        // 关键词搜索（在标题和描述中搜索）
        // 只有关键词不为空时才添加
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.must(wrapper ->
                wrapper.should()
                    .match(ProductDTO::getTitle, keyword)
                    .match(ProductDTO::getDescription, keyword)
            );
        }

        // 嵌套 filter 条件（不需要评分，性能更好）
        queryWrapper.must(wrapper -> {
            var filterWrapper = wrapper.filter();

            // 分类筛选（只有 categoryId 不为 null 时才添加）
            filterWrapper.term(categoryId != null, ProductDTO::getCategoryId, categoryId);

            // 价格范围筛选（条件判断）
            filterWrapper.ge(minPrice != null, ProductDTO::getPrice, minPrice);
            filterWrapper.le(maxPrice != null, ProductDTO::getPrice, maxPrice);

            // 品牌筛选（只有列表不为空时才添加）
            filterWrapper.terms(brandIds != null && !brandIds.isEmpty(),
                ProductDTO::getBrandId, brandIds.toArray());

            // 只显示上架的商品（始终添加）
            filterWrapper.term(ProductDTO::getStatus, 1);
        });

        // 排序（根据参数动态排序）
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("ASC".equalsIgnoreCase(sortOrder)) {
                queryWrapper.sortByAsc(sortBy);
            } else {
                queryWrapper.sortByDesc(sortBy);
            }
        }

        // 分页查询
        return queryWrapper.searchPage(page, size);
    }

    /**
     * 商品聚合统计
     */
    public void aggregateProducts() {
        esChainQueryWrapper()
            .esLambdaAggWrapper()
            // 按分类统计商品数量
            .terms(ProductDTO::getCategoryId, e -> e.size(100))
            // 在每个分类下统计平均价格
            .subAgg(t -> t.avg(ProductDTO::getPrice))
            // 在每个分类下统计总销量
            .subAgg(t -> t.sum(ProductDTO::getSales))
            .search();
    }
}
```

---

## 11. 最佳实践

### 11.1 查询性能优化

1. **使用 Filter 代替 Must**：不需要评分的条件使用 filter，性能更好
2. **避免使用 Wildcard**：尽量使用 matchPhrase 代替 wildcard
3. **合理使用分页**：深度分页使用 searchAfter 或 scroll
4. **只查询需要的字段**：使用 includes 指定返回字段，或使用 excludes 排除不需要的字段
5. **布尔查询嵌套使用**：must/should/filter/mustNot 需要通过嵌套方式组合

```java
// 性能优化示例（正确的嵌套方式）
EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
    // 使用 must 包裹多个条件
    .must()
    .ge(FastTestDTO::getAge, 18)
    // 使用嵌套的方式添加 filter（不计算评分，性能好）
    .must(wrapper ->
        wrapper.filter()
            .term(FastTestDTO::getUsername, "张三")
            .range(FastTestDTO::getAge, 18, 60)
    )
    // 使用嵌套方式添加 should
    .must(wrapper ->
        wrapper.should()
            .matchPhrase(FastTestDTO::getText, "关键词1")
            .matchPhrase(FastTestDTO::getText, "关键词2")
    )
    // 只返回需要的字段
    .includes(FastTestDTO::getId, FastTestDTO::getUsername, FastTestDTO::getAge)
    // 分页查询
    .searchPage(1, 10);
```

### 11.2 索引设计建议

1. **KEYWORD vs TEXT**：
   - 精确匹配、聚合、排序 → KEYWORD
   - 全文检索 → TEXT

2. **合理使用 Nested**：
   - 需要独立查询的数组对象 → NESTED
   - 简单数组 → 普通字段

3. **copyTo 优化查询**：
   - 多字段搜索时，将字段复制到一个统一字段

```java
// 实体类定义
@Data
@EsIndex(index = "samples")
public class SamplesEsDTO {
    @EsField(copyTo = "keyword")
    private String email;

    @EsField(copyTo = "keyword")
    private String phone;

    @EsField(type = EsFieldType.KEYWORD)
    private String keyword; // 包含 email 和 phone 的值
}

// 查询时只需查询一个字段
EsResponse<SamplesEsDTO> response = samplesEsService.esChainQueryWrapper()
    .term(SamplesEsDTO::getKeyword, "搜索值")
    .search();
```

### 11.3 异常处理

```java
try {
    EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
        .term(FastTestDTO::getUsername, "张三")
        .search();

    if (response.getList().isEmpty()) {
        System.out.println("未找到数据");
    }
} catch (Exception e) {
    System.err.println("ES 查询异常：" + e.getMessage());
    e.printStackTrace();
}
```

---

## 12. 常见问题

### 12.1 为什么查询不到数据？

1. **KEYWORD 字段大小写敏感**：使用 normalizer 或 TEXT 类型
2. **TEXT 字段被分词**：使用 keyword 子字段或 term 改为 match
3. **索引不存在**：检查索引名称和环境后缀

### 12.2 如何调试查询语句？

使用 Profile 功能查看查询详情：

```java
EsResponse<FastTestDTO> response = fastTestService.esChainQueryWrapper()
    .term(FastTestDTO::getUsername, "张三")
    .profile()  // 启用性能分析
    .search();

System.out.println(response); // 包含详细的查询信息
```

### 12.3 Reindex 失败怎么办？

1. 检查新旧索引映射是否兼容
2. 检查数据类型转换是否正确
3. 建议在低峰期执行 reindex
4. 生产环境建议手动 reindex

---

## 13. 总结

Es-Plus 提供了一套简洁、优雅的 Elasticsearch 操作 API，主要特点：

- **类似 MyBatis-Plus 的语法**：降低学习成本
- **Lambda 表达式支持**：类型安全，IDE 友好
- **静态链式编程**：无需注入，随时使用
- **丰富的查询方法**：覆盖 ES 常用查询
- **优雅的聚合封装**：简化复杂聚合操作
- **完善的索引管理**：创建、删除、Reindex 一应俱全

通过本文档的案例，你可以快速掌握 Es-Plus 的使用方法，提高 Elasticsearch 开发效率。

---

## 附录：常用 API 速查

### 查询 API （精简，功能远不止于此）

| 方法 | 说明 | 示例 |
|------|------|------|
| `term()` | 精确匹配 | `.term(FastTestDTO::getUsername, "张三")` |
| `term(condition, ...)` | 条件精确匹配 | `.term(name != null, FastTestDTO::getUsername, name)` |
| `terms()` | 多值匹配 | `.terms(FastTestDTO::getUsername, "张三", "李四")` |
| `terms(condition, ...)` | 条件多值匹配 | `.terms(list != null && !list.isEmpty(), FastTestDTO::getTags, list.toArray())` |
| `match()` | 全文检索 | `.match(FastTestDTO::getText, "苹果")` |
| `match(condition, ...)` | 条件全文检索 | `.match(keyword != null && !keyword.isEmpty(), FastTestDTO::getText, keyword)` |
| `matchPhrase()` | 短语匹配 | `.matchPhrase(FastTestDTO::getText, "苹果手机")` |
| `multiMatch()` | 多字段匹配 | `.multiMatch("苹果", FastTestDTO::getText, FastTestDTO::getUsername)` |
| `range()` | 范围查询 | `.range(FastTestDTO::getAge, 18, 60)` |
| `ge()` | 大于等于 | `.ge(FastTestDTO::getAge, 18)` |
| `ge(condition, ...)` | 条件大于等于 | `.ge(minAge != null, FastTestDTO::getAge, minAge)` |
| `le()` | 小于等于 | `.le(FastTestDTO::getAge, 60)` |
| `le(condition, ...)` | 条件小于等于 | `.le(maxAge != null, FastTestDTO::getAge, maxAge)` |
| `fuzzy()` | 模糊查询 | `.fuzzy(FastTestDTO::getUsername, "张三", EpFuzziness.ONE)` |
| `wildcard()` | 通配符查询 | `.wildcard(FastTestDTO::getText, "*苹果*")` |
| `must()` | AND 逻辑 | `.must().term(...).term(...)` |
| `must(condition, ...)` | 条件 AND 嵌套 | `.must(keyword != null, wrapper -> wrapper.should()...)` |
| `should()` | OR 逻辑 | `.should().term(...).term(...)` |
| `mustNot()` | NOT 逻辑 | `.mustNot().term(...)` |
| `filter()` | 过滤条件 | `.filter().term(...)` |
| `nestedQuery()` | 嵌套查询 | `.nestedQuery(...)` |
| `includes()` | 指定返回字段 | `.includes(FastTestDTO::getId, FastTestDTO::getUsername)` |
| `excludes()` | 排除返回字段 | `.excludes(FastTestDTO::getText, FastTestDTO::getCreateTime)` |
| `searchPage()` | 分页查询 | `.searchPage(1, 10)` |
| `sortByAsc()` | 升序排序 | `.sortByAsc(FastTestDTO::getAge)` |
| `sortByDesc()` | 降序排序 | `.sortByDesc(FastTestDTO::getCreateTime)` |
| `scroll()` | 滚动查询 | `.scroll(100, scrollId)` |
| `searchAfter()` | SearchAfter分页 | `.searchAfter(sortValues)` |
| `count()` | 统计数量 | `.count()` |
| `profile()` | 性能分析 | `.profile()` |

**注意**：大部分查询方法都支持条件参数形式，第一个参数为 boolean 条件，只有为 true 时才添加该查询。

### 聚合 API （精简，功能远不止于此）

| 方法 | 说明 | 示例 |
|------|------|------|
| `terms()` | 分组聚合 | `.esLambdaAggWrapper().terms(FastTestDTO::getUsername)` |
| `sum()` | 求和 | `.subAgg(t -> t.sum(FastTestDTO::getAge))` |
| `avg()` | 平均值 | `.subAgg(t -> t.avg(FastTestDTO::getAge))` |
| `count()` | 计数 | `.subAgg(t -> t.count(FastTestDTO::getId))` |
| `filter()` | 过滤聚合 | `.filter(...)` |

### 更新 API （精简，功能远不止于此）

| 方法 | 说明 | 示例 |
|------|------|------|
| `save()` | 保存 | `.save(dto)` |
| `update()` | 更新 | `.update(dto)` |
| `saveOrUpdate()` | 保存或更新 | `.saveOrUpdate(dto)` |
| `saveBatch()` | 批量保存 | `.saveBatch(list)` |
| `removeByIds()` | 批量删除 | `.removeByIds(ids)` |
| `updateByQuery()` | 条件更新 | `.set(...).updateByQuery()` |

### 索引 API （精简，功能远不止于此）

| 方法 | 说明 | 示例 |
|------|------|------|
| `createIndex()` | 创建索引 | `Es.chainIndex().createIndex(FastTestDTO.class)` |
| `deleteIndex()` | 删除索引 | `Es.chainIndex().deleteIndex("fast_test")` |
| `indexExists()` | 判断索引是否存在 | `Es.chainIndex().index("fast_test").indexExists()` |
| `reindex()` | 数据迁移 | `Es.chainIndex().reindex(...)` |

---

**文档版本**: 1.0
**最后更新**: 2025-09-30
**官方仓库**: [https://github.com/zhaohaoh/es-plus](https://github.com/zhaohaoh/es-plus)