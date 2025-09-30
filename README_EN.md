# Es-Plus - Elasticsearch Java Client Enhancement Tool | MyBatis-Plus Style API

<p align="center">
  <a href="https://github.com/zhaohaoh/es-plus/actions/workflows/build.yml">
    <img src="https://github.com/zhaohaoh/es-plus/actions/workflows/build.yml/badge.svg" alt="Build Status">
  </a>
  <a href="https://search.maven.org/artifact/io.github.zhaohaoh/es-plus-spring-boot-starter">
    <img src="https://img.shields.io/maven-central/v/io.github.zhaohaoh/es-plus-spring-boot-starter.svg?label=Maven%20Central" alt="Maven Central">
  </a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
  </a>
  <a href="https://github.com/zhaohaoh/es-plus">
    <img src="https://img.shields.io/github/stars/zhaohaoh/es-plus.svg?style=social" alt="GitHub stars">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Elasticsearch-6.x%20%7C%207.x%20%7C%208.x-blue" alt="ES Version">
  <img src="https://img.shields.io/badge/Java-8%20%7C%2017%20%7C%2021-orange" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-green" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Style-MyBatis--Plus-yellowgreen" alt="Style">
</p>

<p align="center">
  <strong>Simplify Elasticsearch Development | Lambda Query | Non-Invasive | 60% Less Code</strong>
</p>

**Keywords**: Elasticsearch Java Client, ES ORM, MyBatis-Plus Style, Lambda Query, Spring Boot Integration, Method Chaining, Elasticsearch Wrapper

[中文文档](README.md)

---

## 📖 Introduction

Es-Plus is an **Elasticsearch Java Client Enhancement Tool** that adopts **MyBatis-Plus** style API design, providing Java developers with an elegant Elasticsearch operation experience.

- 🎯 **Object-Oriented**: MyBatis-Plus style Lambda chaining queries
- 🚀 **Non-Invasive**: Built on RestHighLevelClient and Elasticsearch Java Client
- 🔧 **Out-of-the-Box**: Spring Boot Starter one-click integration
- 📦 **Multi-Version Support**: Compatible with Elasticsearch 6.7 / 7.8 / 8.17
- 💡 **Simplified Development**: 60% less code, goodbye to cumbersome native APIs

Perfect for Java developers using **Elasticsearch + Spring Boot**, especially teams familiar with **MyBatis-Plus**.

## 🔍 Why Es-Plus?

If you're searching for:
- ✅ Elasticsearch Java wrapper library
- ✅ Elasticsearch ORM framework
- ✅ MyBatis-Plus style ES client
- ✅ Elasticsearch Lambda query tool
- ✅ Spring Boot Elasticsearch simplification tool
- ✅ Elasticsearch RestHighLevelClient enhancement

**Es-Plus is what you need!**

## ✨ Features

- **Non-Invasive**: Built on rest-high-level-client and elasticsearch-java (ES8), enhancement without modification
- **MyBatis-Plus Syntax**: Suitable for developers familiar with MyBatis-Plus syntax
- **Elegant Aggregation**: Simplifies Elasticsearch aggregation operations
- **Built-in Analyzers**: Provides all ES analyzers and configurable filters
- **Multi-Version Support**: Supports ES 6.7, 7.8, 8.17
- **Nested Queries**: Elegant nested queries using Lambda expressions
- **Static Chaining**: Direct static class operations without dependency injection
- **Multi-Datasource**: Specify default datasource via @EsIndex
- **Custom Interceptors**: @EsInterceptors annotation for pre/post execution logic
- **ES Console**: Navicat-like ES query editor tool

## 📦 Maven Dependency

**ES 6.x / 7.x:**
```xml
<dependency>
    <groupId>io.github.zhaohaoh</groupId>
    <artifactId>es-plus-spring-boot-starter</artifactId>
    <version>Latest Version</version>
</dependency>
```

**ES 8.x:**
```xml
<dependency>
    <groupId>io.github.zhaohaoh</groupId>
    <artifactId>es8-plus-spring-boot-starter</artifactId>
    <version>Latest Version</version>
</dependency>
```

## 🚀 Quick Start

### Step 1: Configuration

Configure Elasticsearch connection in `application.properties`:

```properties
es-plus.address=xxx.xxx.xxx.xxx:9200
es-plus.global-config.search-size=5000
es-plus.global-config.default-analyzer=ep_ik_max_word
```

### Step 2: Add Index Scan Annotation

```java
@SpringBootApplication
@EsIndexScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Step 3: Use Static Chain API

```java
@Service
public class UserService extends EsServiceImpl<User> {
    public void query() {
        EsResponse<User> response = Es.chainLambdaQuery(User.class)
            .term(User::getUsername, "admin")
            .list();
    }
}
```

## 💡 Query Comparison: es-plus vs Native ES

### es-plus (10 lines)
```java
EsResponse<User> response = userService.esChainQueryWrapper()
    .must()
    .term(User::getUsername, "admin")
    .ge(User::getAge, 18)
    .match(User::getText, "keyword")
    .sortByDesc(User::getCreateTime)
    .searchPage(1, 10);
List<User> list = response.getList();
```

### Native ES (25 lines)
```java
SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
boolQuery.must(QueryBuilders.termQuery("username", "admin"));
boolQuery.must(QueryBuilders.rangeQuery("age").gte(18));
boolQuery.must(QueryBuilders.matchQuery("text", "keyword"));
sourceBuilder.query(boolQuery);
sourceBuilder.sort("createTime", SortOrder.DESC);
sourceBuilder.from(0);
sourceBuilder.size(10);

SearchRequest request = new SearchRequest("user");
request.source(sourceBuilder);
SearchResponse response = client.search(request, RequestOptions.DEFAULT);

List<User> list = new ArrayList<>();
for (SearchHit hit : response.getHits().getHits()) {
    list.add(JSON.parseObject(hit.getSourceAsString(), User.class));
}
```

**💡 60% less code, more concise and intuitive**

## 📄 License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)