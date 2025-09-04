package com.es.plus.es6.client;

import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.lock.ELockClient;
import com.es.plus.common.pojo.es.EpScript;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.JsonUtils;
import com.es.plus.es6.convert.EpQueryConverter;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EsLockClient implements ELockClient {
    
    private final RestHighLevelClient restHighLevelClient;
    
    public EsLockClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }
    
    @Override
    public boolean updateByScript(String index, String id, EpScript painless) {
        UpdateRequest updateRequest = new UpdateRequest(index, GlobalConfigCache.GLOBAL_CONFIG.getType(),id);
        updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.script(EpQueryConverter.toEsScript(painless));
        updateRequest.timeout("10s");
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            byte op = update.getResult().getOp();
            if (op == DocWriteResponse.Result.NOOP.getOp()) {
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new EsException(e);
        }
    }
    
    @Override
    public boolean upsertByScript(String index, String id, Map<String, Object> insertBody, EpScript painless) {
        UpdateRequest updateRequest = new UpdateRequest(index, GlobalConfigCache.GLOBAL_CONFIG.getType(),id);
        updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.script(EpQueryConverter.toEsScript(painless));
        updateRequest.upsert(insertBody);
        updateRequest.timeout("10s");
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            byte op = update.getResult().getOp();
            if (op == DocWriteResponse.Result.NOOP.getOp()) {
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new EsException(e);
        }
    }
    
    @Override
    public String update(String index, Object esData) {
        UpdateRequest updateRequest = new UpdateRequest(index,GlobalConfigCache.GLOBAL_CONFIG.getType(), GlobalParamHolder.getDocId(index,esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
        //乐观锁重试次数
        updateRequest.retryOnConflict(GlobalConfigCache.GLOBAL_CONFIG.getMaxRetries());
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.valueOf(GlobalConfigCache.GLOBAL_CONFIG.getRefreshPolicy().name()));
        updateRequest.timeout("10s");
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            return update.toString();
        } catch (IOException e) {
            throw new EsException(e);
        }
    }
    
    
    @Override
    public  Map<String,Object> search(String index, String key,String value) {
        TermQueryBuilder termQuery = QueryBuilders.termQuery(key, value);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(termQuery);
        searchRequest.source(searchSourceBuilder);
        
        SearchResponse response;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = response.getHits();
            if (searchHits != null && searchHits.getHits() != null && searchHits.getHits().length > 0) {
                for (SearchHit hit : response.getHits().getHits()) {
                    return hit.getSourceAsMap();
                }
            }
        } catch (IOException e) {
            throw new EsException(e);
        }
        return new HashMap<>();
    }
    
    
}