package com.es.plus.common.lock;

import com.es.plus.common.pojo.es.EpScript;

import java.util.Map;

public interface ELockClient {
    /**
     * 更新Es数据根据script语音更新
     */
    Map<String,Object> search(String index, String key,String value);
    /**
     * 更新Es数据根据script语音更新
     */
    boolean updateByScript(String index, String id, EpScript painless);
    
    /**
     * 新增或更新
     */
    boolean upsertByScript(String index, String id, Map<String, Object> insertBody, EpScript painless);
    
    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    String update(String index, Object esData);
}
