package com.es.plus.lock;

import com.es.plus.core.wrapper.EsQueryWrapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.script.Script;

import java.util.Map;

public interface ELockClient {
    /**
     * 更新Es数据根据script语音更新
     */
    <T> SearchResponse search(String index, EsQueryWrapper<T> esQueryWrapper);
    /**
     * 更新Es数据根据script语音更新
     */
    UpdateResponse updateByScript(String index, String id, Script painless);

    /**
     * 新增或更新
     */
    UpdateResponse upsertByScript(String index, String id, Map<String, Object> insertBody, Script painless);

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    UpdateResponse update(String index, Object esData);
}
