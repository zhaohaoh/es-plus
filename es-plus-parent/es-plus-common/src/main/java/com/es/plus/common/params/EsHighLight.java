package com.es.plus.common.params;

/**
 * @Author: hzh
 * @Date: 2022/9/5 15:26
 */
public class EsHighLight {
    /**
     * 高亮前缀
     */
    private String preTag;

    /**
     * 高亮后缀
     */
    private String postTag;

    /**
     * 高亮字段
     */
    private String field;

    private Integer fragmentSize = 1000;

    public EsHighLight() {
    }

    /**
     * 构造高亮对象
     */
    public EsHighLight(String field) {
        this.field = field;
        this.preTag = "<em color=\"red\">";
        this.postTag = "</em>";
    }

    public EsHighLight(String preTag, String postTag, String field) {
        this.field = field;
        this.preTag = preTag;
        this.postTag = postTag;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public Integer getFragmentSize() {
        return fragmentSize;
    }

    public void setFragmentSize(Integer fragmentSize) {
        this.fragmentSize = fragmentSize;
    }


}
