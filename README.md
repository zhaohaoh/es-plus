## 什么是 Es-Plus

Es-Plus 是Elasticsearch Api增强工具 - 只做增强不做改变，简化`CRUD`操作.

## 特点

- **无侵入**：Es-Plus 在 rest-high-level-client 的基础上进行扩展，只做增强不做改变.支持原生rest-high-level-client
- **融合mybatis-plus语法和ES-Rest-Api**: 适用于习惯mybatis-plus语法和会原生es语句操作的人群
- **优雅的聚合封装**：让es的聚合操作变得更简易
- **内置es所有分词器**：提供es所有的分词器和可配置定义filters
- **自动reindex功能**：es索引库属性的改变会导致es需要重建索引.重建索引的数据迁移由框架自动完成.使用了读写锁,确保reindex过程中额外生成的数据也能同步(但会有删除数据的冗余)
- **兼容es多版本**: 同时支持es6.7和es7.8双版本
- **优雅的nested嵌套查询**: 使用lambda表达式封装实现更优雅的嵌套查询
- **静态链式es编程**: 支持使用静态类，无需指定对应实体类即可执行。可以简单快速对es的索引进行增删改查。
- **多数据源es**: 通用@EsIndex指定默认数据源
- **自定义es执行前后拦截器**: @EsInterceptors 具体用法见下面的例子


## 引入
目前使用版本0.2.8
本次更新
- **多数据源es**: 通用@EsIndex指定默认数据源
- **自定义es执行前后拦截器**: @EsInterceptors 具体用法见下面的例子
优化了查询api的使用，更贴近es的查询语法

``` xml
      <dependency>
            <groupId>io.github.zhaohaoh</groupId>
            <artifactId>es-plus-spring-boot-starter</artifactId>
            <version>Latest Version</version>
        </dependency>
```

## 简单两步! 快速开始!

###  第一步 application.peoperties配置

```properties
# es地址 多个逗号分隔   默认数据源 master
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

# es多版本  
es-plus.global-config.version=7


##es多数据源   local是数据源名称，可自定义 

es-plus.client-properties.local.address=localhost:9100

```

### 第二步 静态链式编程
```java
public class SamplesEsService extends EsServiceImpl<SamplesEsDTO> {
    // 无实体类使用指定index索引直接保存 查询同理
    public void update() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "fsdfsfds");
        map.put("id", "d73d1b4e46244b0db766987759d6e");
        Es.chainUpdate(Map.class).index("sys_user2ttt").save(map);
    }

    public void newSelect() {
        EsResponse<SamplesEsDTO> aaaaa = Es.chainLambdaQuery(SamplesEsDTO.class).term(SamplesEsDTO::getUsername, "hzh").list();
        System.out.println(aaaaa);
    }
}
```

## ORM映射方式

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

### 常规查询
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
        //lambda写法
        EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper = esChainQueryWrapper().must()
                .nestedQuery( SamplesEsDTO::getSamplesNesteds,SamplesNestedDTO.class, (esQueryWrap) -> {
                    esQueryWrap.mustNot().term(SamplesNestedDTO::getState, false);
                    esQueryWrap.mustNot().term(SamplesNestedDTO::getId, 2L);
                });
        EsResponse<SamplesEsDTO> esResponse = queryWrapper.list();

        // 查询
        List<SamplesEsDTO> list = esResponse.getList();
    }
    
    
    //优雅的nested嵌套查询
    //三级嵌套对象附加innerHits查询方法  一级对象SamplesEsDTO 二级对象SamplesNestedDTO 三级对象 SamplesNestedInnerDTO
    public void nested() {
        //获取二级查询条件
        Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> innerConsumer = getSamplesNestedConsumer();
        //   InnerHit
        InnerHitBuilder innerHitBuilder = new InnerHitBuilder("test");
        innerHitBuilder.setSize(10);
        //一级查询条件
        EsChainLambdaQueryWrapper<SamplesEsDTO> queryWrapper = esChainQueryWrapper().must()
                .nestedQuery(SamplesEsDTO::getSamplesNesteds, SamplesNestedDTO.class,
                        innerConsumer, ScoreMode.None,innerHitBuilder);

        EsResponse<SamplesEsDTO> esResponse = queryWrapper.list();
        // 查询
        List<SamplesEsDTO> list = esResponse.getList();
    }

    /**
     *  获取二级嵌套查询对象
     */
    private Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> getSamplesNestedConsumer() {
        Consumer<EsLambdaQueryWrapper<SamplesNestedDTO>> innerConsumer = (esQueryWrap) -> {
            esQueryWrap.must().term(SamplesNestedDTO::getUsername, "3");
            InnerHitBuilder innerHitBuilder1 = new InnerHitBuilder();
            innerHitBuilder1.setSize(100);
            Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> innerInnerConsumer = getSamplesNestedInnerConsumer();
            esQueryWrap.must().nestedQuery(SamplesNestedDTO::getSamplesNestedInner, SamplesNestedInnerDTO.class,
                    innerInnerConsumer, ScoreMode.None, innerHitBuilder1);
        };
        return innerConsumer;
    }

    /**
     *  获取三级嵌套查询对象
     */
    private Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> getSamplesNestedInnerConsumer() {
        Consumer<EsLambdaQueryWrapper<SamplesNestedInnerDTO>> innerInnerConsumer = (innerQuery) -> {
            innerQuery.must().term(SamplesNestedInnerDTO::getUsername, 3);
        };
        return innerInnerConsumer;
    }
    
    
}
```

## 最新拦截器案例
```java

@Component
@EsInterceptors(value = {
        //需要拦截的类名和方法。 类EsPlusClient和EsPlusIndexClient。增删改查数据类,索引增删改类。参数中还可以指定索引名。
        @InterceptorElement(type = EsPlusClient.class, methodName = "search")
})
public class EsSearchAfterInterceptor implements EsInterceptor {

    @Override
    public void before(String index, Method method, Object[] args) {
        Integer page = null;
        Integer size = null;
        EsParamWrapper esParamWrapper = null;
        EsQueryParamWrapper esQueryParamWrapper = null; 
        for (Object arg : args) {
            if (arg instanceof EsParamWrapper) {
                esParamWrapper = (EsParamWrapper) arg; 
                esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper();
                page = esQueryParamWrapper.getPage();
                size = esQueryParamWrapper.getSize();
                if (esQueryParamWrapper.getEsSelect()!= null && !esQueryParamWrapper.getEsSelect().getFetch()){
                    return;
                }
                if (esQueryParamWrapper.getSearchAfterValues()!=null){
                    return;
                }
                break;
            }
        }
        if (esParamWrapper == null || page == null | size == null) {
            return;
        } 
        
        //执行你的逻辑 
    }

    @Override
    public void after(String index, Method method, Object[] args, Object result) {
        Integer page = null;
        Integer size = null; 
        for (Object arg : args) {
            if (arg instanceof EsParamWrapper) {
                EsParamWrapper  esParamWrapper = (EsParamWrapper) arg;
                EsQueryParamWrapper esQueryParamWrapper = esParamWrapper.getEsQueryParamWrapper(); 
                page = esQueryParamWrapper.getPage();
                size = esQueryParamWrapper.getSize();
                if (esQueryParamWrapper.getEsSelect()!= null && !esQueryParamWrapper.getEsSelect().getFetch()){
                    return;
                }
                break;
            }
        }
        int endIndex = page * size;
        EsResponse response = (EsResponse) result;


        //执行你的逻辑
         
    }
 

}
```


## Es版本
遇到版本冲突使用6.7.0和7.8.0

## 自动Reindex   reindex功能默认关闭，暂不建议生产开启。
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


