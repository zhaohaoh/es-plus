package com.es.plus.adapter.util;

import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;

import java.lang.reflect.Field;

/**
 * 注释解析工具
 *
 * @author hzh
 * @date 2023/07/25
 */
public class AnnotationResolveUtil {

    public  static EsFieldInfo resolveEsField(EsField esField){
        if (esField == null) {
            return null;
        }
        EsFieldInfo esFieldInfo = new EsFieldInfo();
        esFieldInfo.setEsId(false);
        esFieldInfo.setAnalyzer(esField.analyzer());
        esFieldInfo.setFieldData(esField.fieldData());
        esFieldInfo.setIndex(esField.index());
        esFieldInfo.setCopyTo(esField.copyTo());
        esFieldInfo.setName(esField.name());
        esFieldInfo.setExist(esField.exist());
        esFieldInfo.setChild(esField.child());
        esFieldInfo.setNormalizer(esField.normalizer());
        esFieldInfo.setStore(esField.store());
        esFieldInfo.setParent(esField.parent());
        esFieldInfo.setType(esField.type());
        esFieldInfo.setDateFormat(esField.dateFormat());
        esFieldInfo.setTimeZone(esField.timeZone());
        esFieldInfo.setEsFormat(esField.esFormat());
        esFieldInfo.setSearchAnalyzer(esField.searchAnalyzer());
        esFieldInfo.setEagerGlobalOrdinals(esField.eagerGlobalOrdinals());
        esFieldInfo.setIgnoreAbove(esField.ignoreAbove());
        return esFieldInfo;
    }

    public  static EsFieldInfo resolveEsId(EsId esId){
        if (esId == null) {
            return null;
        }
        EsFieldInfo esFieldInfo = new EsFieldInfo();
        esFieldInfo.setEsId(true);
        esFieldInfo.setName(esId.name());
        esFieldInfo.setExist(true);
        return esFieldInfo;
    }

    public  static EsFieldInfo resolveField(Field field){
        if (field == null) {
            return null;
        }
        EsFieldInfo esFieldInfo = new EsFieldInfo();
        esFieldInfo.setEsId(false);
        esFieldInfo.setName(field.getName());
        esFieldInfo.setExist(true);
        return esFieldInfo;
    }

}
