package com.es.plus.adapter.pojo.es;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义脚本类，用于替代Elasticsearch的Script类
 */
public class EpScript {
    
    private String script;
    private String lang;
    private Map<String, Object> params;
    private ScriptType scriptType;
    
    public EpScript() {
    }
    
    public EpScript(String script) {
        this.script = script;
        this.scriptType = ScriptType.INLINE;
    }
    
    public EpScript(ScriptType scriptType, String lang, String script, Map<String, Object> params) {
        this.scriptType = scriptType;
        this.lang = lang;
        this.script = script;
        this.params = params != null ? params : new HashMap<>();
    }
    
    /**
     * 创建内联脚本
     * @param script 脚本内容
     * @return EpScript实例
     */
    public static EpScript inline(String script) {
        return new EpScript(ScriptType.INLINE, null, script, null);
    }
    
    /**
     * 创建内联脚本
     * @param script 脚本内容
     * @param params 脚本参数
     * @return EpScript实例
     */
    public static EpScript inline(String script, Map<String, Object> params) {
        return new EpScript(ScriptType.INLINE, null, script, params);
    }
    
    /**
     * 创建内联脚本
     * @param lang 脚本语言
     * @param script 脚本内容
     * @param params 脚本参数
     * @return EpScript实例
     */
    public static EpScript inline(String lang, String script, Map<String, Object> params) {
        return new EpScript(ScriptType.INLINE, lang, script, params);
    }
    
    /**
     * 设置脚本内容
     * @param script 脚本内容
     * @return this
     */
    public EpScript script(String script) {
        this.script = script;
        return this;
    }
    
    /**
     * 设置脚本语言
     * @param lang 脚本语言
     * @return this
     */
    public EpScript lang(String lang) {
        this.lang = lang;
        return this;
    }
    
    /**
     * 设置脚本参数
     * @param params 脚本参数
     * @return this
     */
    public EpScript params(Map<String, Object> params) {
        this.params = params;
        return this;
    }
    
    /**
     * 添加单个参数
     * @param key 参数名
     * @param value 参数值
     * @return this
     */
    public EpScript param(String key, Object value) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }
    
    /**
     * 设置脚本类型
     * @param scriptType 脚本类型
     * @return this
     */
    public EpScript scriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
        return this;
    }
    
    /**
     * 获取脚本内容
     * @return 脚本内容
     */
    public String getScript() {
        return script;
    }
    
    /**
     * 获取脚本语言
     * @return 脚本语言
     */
    public String getLang() {
        return lang;
    }
    
    /**
     * 获取脚本参数
     * @return 脚本参数
     */
    public Map<String, Object> getParams() {
        return params;
    }
    
    /**
     * 获取脚本类型
     * @return 脚本类型
     */
    public ScriptType getScriptType() {
        return scriptType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpScript epScript = (EpScript) o;
        return Objects.equals(script, epScript.script) &&
                Objects.equals(lang, epScript.lang) &&
                Objects.equals(params, epScript.params) &&
                scriptType == epScript.scriptType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(script, lang, params, scriptType);
    }
    
    @Override
    public String toString() {
        return "EpScript{" +
                "script='" + script + '\'' +
                ", lang='" + lang + '\'' +
                ", params=" + params +
                ", scriptType=" + scriptType +
                '}';
    }
    
    /**
     * 脚本类型枚举
     */
    public enum ScriptType {
        INLINE,
        STORED
    }
}
