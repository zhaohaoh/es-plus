package com.es.plus.core.wrapper.core;

import com.es.plus.adapter.EsPlusClientFacade;
import com.es.plus.adapter.config.GlobalConfigCache;
import com.es.plus.adapter.params.EsAliasResponse;
import com.es.plus.adapter.params.EsIndexResponse;
import com.es.plus.adapter.params.EsSettings;
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
        this.index = index + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
    }

    public EsIndexWrapper index(String index) {
        this.index = index + GlobalConfigCache.GLOBAL_CONFIG.getGlobalSuffix();
        return this;
    }

    @Override
    public EsIndexWrapper createIndex(Class<?> tClass) {
        esPlusClientFacade.createIndex(index, tClass);
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
    public EsIndexWrapper createIndexMapping(Class<?> tClass) {
        esPlusClientFacade.createIndexMapping(index, tClass);
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
    public EsAliasResponse getAliasIndex(String alias) {
        return esPlusClientFacade.getAliasIndex(alias);
    }

    @Override
    public boolean indexExists() {
        return esPlusClientFacade.indexExists(index);
    }

    @Override
    public boolean replaceAlias(String oldIndexName, String newIndexName, String alias) {
        return esPlusClientFacade.replaceAlias(oldIndexName, newIndexName, alias);
    }

    @Override
    public boolean reindex(String oldIndexName, String reindexName, Long currentTime) {
        esPlusClientFacade.reindex(oldIndexName, reindexName, currentTime);
        return false;
    }

    @Override
    public boolean updateSettings(EsSettings esSettings) {
        esPlusClientFacade.updateSettings(index, esSettings);
        return false;
    }

    @Override
    public boolean updateSettings(Map<String, Object> esSettings) {
        esPlusClientFacade.updateSettings(index, esSettings);
        return false;
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
}