package com.es.plus.adapter.pojo.es;

import java.util.List;
import java.util.Map;

/**
 * 自定义高亮构建器，用于替代Elasticsearch的HighlightBuilder
 */
public class EpHighlightBuilder {
    private List<String> fields;
    private String preTags;
    private String postTags;
    private Boolean requireFieldMatch;
    private String fragmentSize;
    private Integer numberOfFragments;
    private Map<String, Object> options;

    public List<String> getFields() {
        return fields;
    }

    public EpHighlightBuilder setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public String getPreTags() {
        return preTags;
    }

    public EpHighlightBuilder setPreTags(String preTags) {
        this.preTags = preTags;
        return this;
    }

    public String getPostTags() {
        return postTags;
    }

    public EpHighlightBuilder setPostTags(String postTags) {
        this.postTags = postTags;
        return this;
    }

    public Boolean getRequireFieldMatch() {
        return requireFieldMatch;
    }

    public EpHighlightBuilder setRequireFieldMatch(Boolean requireFieldMatch) {
        this.requireFieldMatch = requireFieldMatch;
        return this;
    }

    public String getFragmentSize() {
        return fragmentSize;
    }

    public EpHighlightBuilder setFragmentSize(String fragmentSize) {
        this.fragmentSize = fragmentSize;
        return this;
    }

    public Integer getNumberOfFragments() {
        return numberOfFragments;
    }

    public EpHighlightBuilder setNumberOfFragments(Integer numberOfFragments) {
        this.numberOfFragments = numberOfFragments;
        return this;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public EpHighlightBuilder setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }
}
