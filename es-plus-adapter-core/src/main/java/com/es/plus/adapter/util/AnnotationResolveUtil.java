package com.es.plus.adapter.util;

import com.es.plus.adapter.properties.EsFieldInfo;
import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;

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
        esFieldInfo.setEsFormat(esField.esFormat());
        esFieldInfo.setSearchAnalyzer(esField.searchAnalyzer());
        esFieldInfo.setEagerGlobalOrdinals(esField.eagerGlobalOrdinals());
        return esFieldInfo;
    }

    public  static EsFieldInfo resolveEsId(EsId esId){
        if (esId == null) {
            return null;
        }
        EsFieldInfo esFieldInfo = new EsFieldInfo();
        esFieldInfo.setEsId(true);
        esFieldInfo.setName(esId.name());
        return esFieldInfo;
    }
}
