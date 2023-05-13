## 什么是 Es-Plus

Es-Plus 是Elasticsearch Api增强工具 - 只做增强不做改变，简化`CRUD`操作.

## 特点

- **无侵入**：Es-Plus 在 rest-high-level-client 的基础上进行扩展，只做增强不做改变.支持原生rest-high-level-client
- **融合mybatis-plus语法和ES-Rest-Api**: 适用于习惯mybatis-plus语法和会原生es语句操作的人群
- **优雅的聚合封装**：让es的聚合操作变得更简易
- **内置es所有分词器**：提供es所有的分词器和可配置定义filters
- **自动reindex功能**：es索引库属性的改变会导致es需要重建索引.重建索引的数据迁移由框架自动完成.使用了读写锁,确保reindex过程中额外生成的数据也能同步(但会有删除数据的冗余)
-

## 引入
``` xml
      <dependency>
            <groupId>io.github.zhaohaoh</groupId>
            <artifactId>es-plus-spring-boot-starter</artifactId>
            <version>Latest Version</version>
        </dependency>
```

## 快速开始

###   application.peoperties配置

```properties
# es地址 多个逗号分隔
es-plus.address=xxx.xxx.xxx.xxx:9200
# 是否异步reindex
es-plus.global-config.reindex-async=false
# 是否开启自动reindex. 如果没有开启也会自动对新增的字段添加映射
es-plus.global-config.index-auto-move=false
# 查询最大数量的限制
es-plus.global-config.search-size=5000
# 索引添加统一的环境后缀 测试环境
es-plus.global-config.global-suffix=_test
# 索引全局默认分词器    默认值ep_standard   可选 #ep_ik_max_word,ep_ik_smart,ep_simple,ep_keyword,ep_stop,ep_whitespace,ep_pattern,ep_language,ep_snowball
es-plus.global-config.default-analyzer=ep_ik_max_word
# 自定义全局refresh策略
es-plus.global-config.refresh-policy=wait_until
# 全局默认获取es的id的字段 默认id
es-plus.global-config.global-es-id=id
es-plus.username=
es-plus.password=
```

### 实体类 没有配置@EsField会根据java自动映射.获取不到映射则设置为Object
```java
@Data
@EsIndex(index = "sys_user1")
public class SysUser  {
    @EsId
    private Long id;
    @EsField(type = EsFieldType.STRING) 
    private String username;
    
    /**
     * 昵称
     */ 
    private String nickName;

    private String phone;
    
    /**
     * 真实姓名
     */ 
    private String realName;
    
    /**
     * 是否锁定
     */  
    private Integer lockState; 
    @EsField(type = EsFieldType.NESTED)
    private SysRole  sysRole; 
    
    private List<Long> ids;
}
```

### 查询
```java
@Service
public class SysUserEsService extends EsServiceImpl<SysUser>{
    
    
    public void search() {
        // 声明语句嵌套关系是must
        EsResponse<SysUser> esResponse = esChainQueryWrapper().must()
                .terms(SysUser::getUsername, "admin", "hzh", "shi")
                // 多个must嵌套
                .must(a ->
                        // 声明内部语句关系的should
                        a.should()
                                .term(SysUser::getRealName, "dasdsad")
                                .term(SysUser::getPhone, "1386859111"))
                // 查询
                .list();
        List<SysUser> list = esResponse.getList();
    }

    public void agg() {
        // 声明语句嵌套关系是must
        EsChainQueryWrapper<SysUser> esChainQueryWrapper = esChainQueryWrapper().must()
                .terms(SysUser::getUsername, "admin", "hzh", "shi")
                // 多个must嵌套
                .must(a ->
                        // 声明内部语句关系的should
                        a.should()
                                .term(SysUser::getRealName, "dasdsad")
                                .term(SysUser::getPhone, "1386859111"));

        esChainQueryWrapper.esLambdaAggWrapper()
                // terms聚合并且指定数量10000
                .terms(SysUser::getUsername, a -> a.size(10000))
                // 在terms聚合的基础上统计lock数量
                .subAggregation(t -> t.count(SysUser::getLockSate));
        EsResponse<SysUser> esResponse = esChainQueryWrapper
                // 查询
                .list();
        List<SysUser> list = esResponse.getList();

        EsAggregationsResponse<SysUser> esAggregationsReponse = esResponse.getEsAggregationsReponse();
        
        // 以下方法选一种
        Terms terms = esAggregationsReponse.getTerms(SysUser::getUsername);
        Map<String, Long> termsAsMap = esAggregationsReponse.getTermsAsMap(SysUser::getUsername);
    }
    //嵌套对象的查询
    public void nested() {
        EsChainLambdaQueryWrapper<SamplesNestedDTO> asChainQueryWrap = new EsChainLambdaQueryWrapper<>(SamplesNestedDTO.class);
        asChainQueryWrap.should().term(SamplesNestedDTO::getUsername, "hzh");
        asChainQueryWrap.terms(SamplesNestedDTO::getUsername, "term");
        // 声明语句嵌套关系是must
        EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper = esChainQueryWrapper().must()
                .nestedQuery( SamplesEsDTO::getSamplesNesteds, (esQueryWrap) -> {
                    esQueryWrap.mustNot().term("state", false);
                    esQueryWrap.mustNot().term("id", 2L);
                });
        EsResponse<SamplesEsDTO> esResponse = queryWrapper.list();

        // 查询
        List<SamplesEsDTO> list = esResponse.getList();
    }
}
```

## 自动Reindex
#### 如何开启:
es-plus.global-config.auto-reindex=true
#### 开启异步reindex
es-plus.global-config.reindex-async=true
#### 注意事项
reindex会有部分删除数据的冗余.但是通过锁保证了新增和更新数据的错误.但是依然建议在业务低峰期执行.

- [流程图](https://github.com/zhaohaoh/es-plus/blob/master/reindex%E6%B5%81%E7%A8%8B%E5%9B%BE.md)

## 作者
 微信:huangzhaohao1995

# 版权 | License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)


