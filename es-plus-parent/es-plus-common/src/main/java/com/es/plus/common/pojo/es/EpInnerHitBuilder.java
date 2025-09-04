package com.es.plus.common.pojo.es;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义内部命中构建器，用于替代Elasticsearch的InnerHitBuilder
 */
@Data
public class EpInnerHitBuilder {
    private String name;
    private Boolean ignoreUnmapped;
    private Integer from;
    private Integer size;
    private Boolean explain;
    private Boolean version;
    private Boolean seqNoAndPrimaryTerm;
    private Boolean trackScores;
    private List<String> storedFieldNames; // 对应StoredFieldsContext
    private EpQueryBuilder query;
    private List<EpSortBuilder> sorts;
    private List<String> docValueFields; // 对应FetchDocValuesContext.FieldAndFormat
    private Set<ScriptField> scriptFields; // 对应SearchSourceBuilder.ScriptField
    private EpHighlightBuilder highlightBuilder;
    private EpFetchSourceContext fetchSourceContext;
   
 
    
    // ScriptField类，对应SearchSourceBuilder.ScriptField
    public static class ScriptField {
        
        public ScriptField(String fieldName, EpScript script, boolean useDocValueFields) {
            this.fieldName = fieldName;
            this.script = script;
            this.useDocValueFields = useDocValueFields;
        }
        
        private String fieldName;
        private EpScript script;
        private boolean useDocValueFields;
        
        public String getFieldName() {
            return fieldName;
        }
        
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        
        public EpScript getScript() {
            return script;
        }
        
        public void setScript(EpScript script) {
            this.script = script;
        }
        
        public boolean isUseDocValueFields() {
            return useDocValueFields;
        }
        
        public void setUseDocValueFields(boolean useDocValueFields) {
            this.useDocValueFields = useDocValueFields;
        }
    }
    
    public EpInnerHitBuilder() {
    }
    
    public EpInnerHitBuilder(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public EpInnerHitBuilder setName(String name) {
        this.name = name;
        return this;
    }
    
    public Boolean getIgnoreUnmapped() {
        return ignoreUnmapped;
    }
    
    public EpInnerHitBuilder setIgnoreUnmapped(Boolean ignoreUnmapped) {
        this.ignoreUnmapped = ignoreUnmapped;
        return this;
    }
    
    public Integer getFrom() {
        return from;
    }
    
    public EpInnerHitBuilder setFrom(Integer from) {
        this.from = from;
        return this;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public EpInnerHitBuilder setSize(Integer size) {
        this.size = size;
        return this;
    }
    
    public Boolean getExplain() {
        return explain;
    }
    
    public EpInnerHitBuilder setExplain(Boolean explain) {
        this.explain = explain;
        return this;
    }
    
    public Boolean getVersion() {
        return version;
    }
    
    public EpInnerHitBuilder setVersion(Boolean version) {
        this.version = version;
        return this;
    }
    
    
    
    public Boolean getTrackScores() {
        return trackScores;
    }
    
    public EpInnerHitBuilder setTrackScores(Boolean trackScores) {
        this.trackScores = trackScores;
        return this;
    }
    
    public List<String> getStoredFieldNames() {
        return storedFieldNames;
    }
    
    public EpInnerHitBuilder setStoredFieldNames(List<String> storedFieldNames) {
        this.storedFieldNames = storedFieldNames;
        return this;
    }
    
    public EpQueryBuilder getQuery() {
        return query;
    }
    
    public EpInnerHitBuilder setQuery(EpQueryBuilder query) {
        this.query = query;
        return this;
    }
    
    public List<EpSortBuilder> getSorts() {
        return sorts;
    }
    
    public EpInnerHitBuilder setSorts(List<EpSortBuilder> sorts) {
        this.sorts = sorts;
        return this;
    }
    
    public List<String> getDocValueFields() {
        return docValueFields;
    }
    
    public EpInnerHitBuilder setDocValueFields(List<String> docValueFields) {
        this.docValueFields = docValueFields;
        return this;
    }
    
    
    public Set<ScriptField> getScriptFields() {
        return scriptFields;
    }
    
    public EpInnerHitBuilder setScriptFields(Set<ScriptField> scriptFields) {
        this.scriptFields = scriptFields;
        return this;
    }
    
    public EpInnerHitBuilder addScriptField(String name, EpScript script) {
        if (this.scriptFields == null) {
            this.scriptFields = new HashSet<>();
        }
        this.scriptFields.add(new ScriptField(name, script, false));
        return this;
    }
    
    public EpHighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }
    
    public EpInnerHitBuilder setHighlightBuilder(EpHighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
        return this;
    }
    
    public EpFetchSourceContext getFetchSourceContext() {
        return fetchSourceContext;
    }
    
    public EpInnerHitBuilder setFetchSourceContext(EpFetchSourceContext fetchSourceContext) {
        this.fetchSourceContext = fetchSourceContext;
        return this;
    }
    
   
}
