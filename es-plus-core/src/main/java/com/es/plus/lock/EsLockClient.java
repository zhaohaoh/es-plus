package com.es.plus.lock;

import com.es.plus.config.GlobalConfigCache;
import com.es.plus.exception.EsException;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.util.JsonUtils;
import com.es.plus.core.wrapper.core.EsQueryWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public class EsLockClient implements ELockClient {

    private final RestHighLevelClient restHighLevelClient;

    public EsLockClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public UpdateResponse updateByScript(String index, String id, Script painless) {
        UpdateRequest updateRequest = new UpdateRequest(index, GlobalConfigCache.GLOBAL_CONFIG.getType(), id);
        updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.script(painless);
        try {
            return restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public UpdateResponse upsertByScript(String index, String id, Map<String, Object> insertBody, Script painless) {
        UpdateRequest updateRequest = new UpdateRequest(index, GlobalConfigCache.GLOBAL_CONFIG.getType(), id);
        updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.script(painless);
        updateRequest.upsert(insertBody);
        try {
            return restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
    }

    @Override
    public UpdateResponse update(String index, Object esData) {
        UpdateRequest updateRequest = new UpdateRequest(index, GlobalConfigCache.GLOBAL_CONFIG.getType(), EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
        //乐观锁重试次数
         updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.setRefreshPolicy(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy());

        try {
            return restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
    }


    @Override
    public <T> SearchResponse search(String index, EsQueryWrapper<T> esQueryWrapper) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(esQueryWrapper.getQueryBuilder());
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(e);
        }
        return response;
    }


}
