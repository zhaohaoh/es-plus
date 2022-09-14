# es-plus

##  框架实现elasticsearch简单封装

## 简单案例

### 服务类继承

[![vv11VU.png](https://s1.ax1x.com/2022/09/14/vv11VU.png)](https://imgse.com/i/vv11VU)

### 实体类

@EsId必须标注es文档id  不标注默认取id 建议加上

[![vv13aF.png](https://s1.ax1x.com/2022/09/14/vv13aF.png)](https://imgse.com/i/vv13aF)

如果只需要KEYWORD类型必须加上注解。影响term查询

@Esfield非必填。不填自动映射类型。除了NESTED  其他特殊的复杂类型暂不支持

### 查询

#### 不建议的方式

不建议直接无参构造new对象。会导致term lamda表达式查询的问题。除非都用字符串匹配name.

建议有参构造

[![vv1854.png](https://s1.ax1x.com/2022/09/14/vv1854.png)](https://imgse.com/i/vv1854)

#### 根据id查询

[![vv1QbT.png](https://s1.ax1x.com/2022/09/14/vv1QbT.png)](https://imgse.com/i/vv1QbT)

#### 聚合查询

```java
// 构建es查询对象
EsQueryWrapper<SysUser> esQueryWrapper = new EsQueryWrapper<>(SysUser.class);
// 获取es聚合查询对象
EsAggregationWrapper<SysUser>esAggregationWrapper=esQueryWrapper.getEsAggregationWrapper();
// 时间范围聚合
DateHistogramAggregationBuilder dateAggBuilder = esAggregationWrapper
    .dateHistogram(SysUser::getDate)
	.fixedInterval(DateHistogramInterval.days(100));
// 时间范围聚合后进行求和
dateAggBuilder.subAggregation(esAggregationWrapper.sum(SysUser::getNum));
// 年龄最大值聚合
MaxAggregationBuilder max = esAggregationWrapper.max(SysUser::getAge);
// 添加前面的聚合
esAggregationWrapper.add(dateAggBuilder)
    .add(max)
    // 增加一个对前面的时间范围聚合求平均值
    .add(esAggregationWrapper.avgBucket(SysUser::getNum, "date_date_histogram>num_sum"));
```
