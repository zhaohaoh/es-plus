package com.es.plus.core.wrapper.core;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
import com.es.plus.adapter.pojo.es.EpBoolQueryBuilder;
import com.es.plus.core.ClientContext;

import java.util.Map;

import static com.es.plus.constant.EsConstant.MASTER;

public class EsIndexWrapper implements IEsIndexWrapper {
    private String index;
    private EsPlusClientFacade esPlusClientFacade = ClientContext.getClient(MASTER);


    public EsIndexWrapper() {
    }

    public EsIndexWrapper(EsPlusClientFacade esPlusClientFacade) {
        if (esPlusClientFacade != null) {
            this.esPlusClientFacade = esPlusClientFacade;
        }
    }

    public EsIndexWrapper index(String index) {
        this.index = index;
        return this;
    }

    @Override
    public EsIndexWrapper createIndex(Class<?> tClass) {
        esPlusClientFacade.createIndex(index, tClass);
        return this;
    }

    @Override
    public EsIndexWrapper createIndex() {
        esPlusClientFacade.createIndex(index);
        return this;
    }

    @Override
    public EsIndexWrapper createIndexMapping(Class<?> tClass) {
        esPlusClientFacade.createIndexMapping(index, tClass);
        return this;
    }
    
    @Override
    public EsIndexWrapper createIndex(String index,String alias,EsSettings esSettings,Map<String,Object> mappings) {
        esPlusClientFacade.createIndex(index,alias,esSettings,mappings );
        return this;
    }


    @Override
    public EsIndexWrapper putMapping(Class<?> tClass) {
        esPlusClientFacade.putMapping(index, tClass);
        return this;
    }

    @Override
    public EsIndexWrapper putMapping(Map<String, Object> mappingProperties) {
        esPlusClientFacade.putMapping(index, mappingProperties);
        return this;
    }

    @Override
    public boolean deleteIndex(String index) {
        esPlusClientFacade.deleteIndex(index);
        return false;
    }

    @Override
    public EsIndexResponse getIndex() {
        return esPlusClientFacade.getIndex(index);
    }
    
    @Override
    public EsIndexResponse getIndex(String indexName) {
        return esPlusClientFacade.getIndex(indexName);
    }
    
    @Override
    public String getIndexStat() {
        return esPlusClientFacade.getIndexStat();
    }
    
    @Override
    public EsIndexResponse getMappings(String indexName) {
            return esPlusClientFacade.getMappings(indexName);
    }
    
    @Override
    public EsAliasResponse getAliasIndex(String alias) {
        return esPlusClientFacade.getAliasIndex(alias);
    }

    @Override
    public boolean indexExists() {
        return esPlusClientFacade.indexExists(index);
    }

    @Override
    public boolean replaceAlias(String indexName, String oldAlias, String alias) {
        return esPlusClientFacade.replaceAlias(indexName, oldAlias, alias);
    }

    @Override
    public boolean reindex(String oldIndexName, String reindexName, EsQueryWrapper esQueryWrapper) {
        EpBoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        esPlusClientFacade.reindex(oldIndexName, reindexName, queryBuilder);
        return true;
    }
    
    @Override
    public boolean reindex(String oldIndexName, String reindexName) {
        esPlusClientFacade.reindex(oldIndexName, reindexName);
        return true;
    }
    
    @Override
    public boolean reindex(String oldIndexName, String reindexName,Map<String,Object> changeMapping) {
        esPlusClientFacade.reindex(oldIndexName, reindexName,changeMapping);
        return true;
    }

    @Override
    public boolean updateSettings(EsSettings esSettings) {
        esPlusClientFacade.updateSettings( esSettings,index);
        return true;
    }

    @Override
    public boolean updateSettings(Map<String, Object> esSettings) {
        esPlusClientFacade.updateSettings( esSettings,index);
        return true;
    }

    @Override
    public boolean ping() {
        return esPlusClientFacade.ping();
    }

    @Override
    public EsIndexWrapper createAlias(String alias) {
        esPlusClientFacade.createAlias(index, alias);
        return this;
    }

    @Override
    public EsIndexWrapper removeAlias(String alias) {
        esPlusClientFacade.removeAlias(index, alias);
        return this;
    }
    
    @Override
    public EsIndexWrapper createAlias(String index, String alias) {
        esPlusClientFacade.createAlias(index, alias);
        return this;
    }
    
    @Override
    public EsIndexWrapper removeAlias(String index, String alias) {
        esPlusClientFacade.removeAlias(index, alias);
        return this;
    }
    
    @Override
    public boolean forceMerge(int maxSegments, boolean onlyExpungeDeletes, boolean flush, String... index) {
        return esPlusClientFacade.forceMerge(maxSegments, onlyExpungeDeletes, flush, index);
    }
    
    
    /**
     * 获取索引健康
     * @param index
     * @return
     */
    @Override
    public String getIndexHealth() {
       return esPlusClientFacade.getIndexHealth();
    }
    
    /**
     * 获取索引集群节点信息
     * @return
     */
    @Override
    public String getNodes() {
        return esPlusClientFacade.getNodes();
    }
    
    @Override
    public String getCmd(String cmd) {
        return esPlusClientFacade.getCmd(cmd);
    }
}
