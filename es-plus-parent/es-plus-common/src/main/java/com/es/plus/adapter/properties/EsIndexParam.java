package com.es.plus.adapter.properties;


import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.util.SpelUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class EsIndexParam {
    
    /**
     * 索引名 创建的索引后缀+S0或者S1 但这里存的默认和别名一样
     */
    private String[] index;
    
    /**
     * 索引别名  默认index就是索引别名
     */
    private String[] alias;
    
    /**
     * 类型
     */
    private String type;
    
    /**
     * 索引配置
     */
    private EsSettings esSettings;
    
    /**
     * 索引映射
     */
    private Map<String, Object> mappings;
    
    /**
     * 打分字段
     */
    private String scoreField;
    
    /**
     * 子索引的类
     */
    private Class<?> childClass;
    
    /**
     * 索引是否已经创建
     */
    private boolean exists = false;
    
    /**
     * 客户端实例
     */
    private String clientInstance;
    
    /**
     * 是否使用了动态索引名
     */
    private Boolean dynamicIndex;
    
    /**
     * 动态索引名前缀
     */
    private String dynamicIndexPrefix;
    
    /**
     * 动态索引名中间的SPEL表达式
     */
    private String dynamicIndexSpel;
    
    /**
     * 动态索引名后缀
     */
    private String dynamicIndexSuffix;
    
    /**
     * 前一个索引名
     */
    private String preIndex;
    
    
    /**
     * 前一个索引
     */
    public String getPreIndex() {
       return preIndex;
    }
    
    /**
     *  获取原来的index名字
     */
    public String getOriIndex() {
        return index[0] + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
    }
    
    /**
     * 动态解析索引。根据spel表达式
     *
     * @return
     */
    public String[] getIndex() {
        List<String> indexs = new ArrayList<>();
        if (dynamicIndex) {
            for (String idx : index) {
                String spelValue = SpelUtil.parseSpelValue(dynamicIndexSpel);
                if (spelValue == null) {
                    idx = idx + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
                }else{
                    idx = dynamicIndexPrefix + spelValue + dynamicIndexSuffix;
                    if (!Objects.equals(preIndex, idx)) {
                        preIndex = idx;
                    }
                    idx = idx + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
                }
                indexs.add(idx);
            }
        }else{
            indexs = Arrays.stream(index).map(idx->idx + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix()).collect(Collectors.toList());
        }
    
        return indexs.toArray(new String[0]);
    }
    
    /**
     * 获取别名
     */
    public String[] getAlias() {
        if (alias == null) {
            return null;
        }
        return Arrays.stream(alias).map(s -> s + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix()).toArray(String[]::new);
    }
}
