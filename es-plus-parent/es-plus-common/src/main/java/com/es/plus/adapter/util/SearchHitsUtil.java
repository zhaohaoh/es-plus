package com.es.plus.adapter.util;

import com.es.plus.adapter.properties.EsIndexParam;
import com.es.plus.adapter.properties.GlobalParamHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class SearchHitsUtil {
    
    
    public static <T> List<T> parseList(Class<T> tClass, SearchHit[] hitArray) {
        List<T> result = new ArrayList<>();
        if (hitArray != null && hitArray.length > 0) {
            Arrays.stream(hitArray).filter(hit -> StringUtils.isNotBlank(hit.getSourceAsString())).map(hit -> {
            
//                sourceAsMap.put("index", hit.getIndex());
                T bean = JsonUtils.toBean(hit.getSourceAsString(), tClass);
                if (tClass.equals(Map.class)) {
                    return bean;
                }
                //设置高亮
                SearchHitsUtil.setHighLishtField(hit, bean);
                //设置分数
                SearchHitsUtil.setScore(hit, bean);
                return bean;
            }).forEach(result::add);
        }
        return result;
    }
    
    /**
     * 设置高亮
     *
     * @param hit  打击
     * @param bean 豆
     */
    public static  <T> void setHighLishtField(SearchHit hit, T bean) {
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        if (highlightFields != null && highlightFields.size() > 0) {
            highlightFields.forEach((k, v) -> {
                Text[] texts = v.fragments();
                StringBuilder highlightStr = new StringBuilder();
                for (Text text : texts) {
                    highlightStr.append(text);
                }
                try {
                    //高亮字段重新put进去
                    Field field = bean.getClass().getDeclaredField(k);
                    field.setAccessible(true);
                    field.set(bean, highlightStr.toString());
                } catch (Exception e) {
                    log.error("es-plus HighlightFields Exception", e);
                }
            });
            
        }
    }
    
    /**
     * 设置分数
     */
    public static <T> void setScore(SearchHit hit, T bean) {
        float score = hit.getScore();
        if (!Float.isNaN(score)) {
            EsIndexParam esIndexParam = GlobalParamHolder.getAndInitEsIndexParam(bean.getClass());
            try {
                if (StringUtils.isNotBlank(esIndexParam.getScoreField())) {
                    Field field = bean.getClass().getDeclaredField(esIndexParam.getScoreField());
                    field.setAccessible(true);
                    field.set(bean, score);
                }
            } catch (Exception e) {
                log.error("setScore ", e);
            }
        }
    }
    
}
