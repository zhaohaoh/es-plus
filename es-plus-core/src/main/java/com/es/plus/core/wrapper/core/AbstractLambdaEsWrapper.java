package com.es.plus.core.wrapper.core;


import com.es.plus.adapter.properties.EsEntityInfo;
import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.adapter.properties.GlobalParamHolder;
import com.es.plus.adapter.tools.LambdaUtils;
import com.es.plus.adapter.tools.SFunction;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

import static com.es.plus.constant.EsConstant.DOT;

public abstract class AbstractLambdaEsWrapper<T, R> {
    protected String parentFieldName;
    protected Class<T> tClass;

    protected final String[] nameToString(R... functions) {
        return Arrays.stream(functions).map(this::nameToString).toArray(String[]::new);
    }

    /**
     * 名字转es名称
     */
    protected String nameToString(R function) {
        if (function instanceof String) {
            return parentFieldName != null ? parentFieldName + DOT + function : (String) function;
        }

        String fieldName = LambdaUtils.getFieldName((SFunction<T, ?>) function);
        EsFieldInfo indexField = GlobalParamHolder.getIndexField(tClass, fieldName);
        String column = indexField != null && StringUtils.isNotBlank(indexField.getName()) ? indexField.getName() : fieldName;
        return parentFieldName != null ? parentFieldName + DOT + column : column;
    }

    /**
     * 名字转实体类字段名称  因为es有嵌套对象，所有需要区分
     */
    protected String nameToFieldName(R function) {
        if (function instanceof String) {
            //手动传入的字符串是es的名字
            String esName = (String) function;
            //如果包含.说明是多级字段
            boolean contains = StringUtils.contains(esName, ".");
            if (contains) {
                //获取最后的字段  user.account.money  取money来获取实体类字段
                String last = StringUtils.substringAfterLast(esName, ".");
                EsEntityInfo esEntityInfo = GlobalParamHolder.getEsEntityInfo(tClass);
                if (esEntityInfo != null) {
                    Map<String, String> mappingFieldMap = esEntityInfo.getMappingFieldMap();
                    String fieldName = mappingFieldMap.get(last);
                    return StringUtils.isBlank(fieldName) ? esName : fieldName;
                }
            }else{
                return esName;
            }
        }

        return LambdaUtils.getFieldName((SFunction<T, ?>) function);
    }
}
