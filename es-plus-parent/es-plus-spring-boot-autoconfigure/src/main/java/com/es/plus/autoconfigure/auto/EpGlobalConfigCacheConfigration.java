package com.es.plus.autoconfigure.auto;

import com.es.plus.autoconfigure.properties.AnalysisProperties;
import com.es.plus.autoconfigure.properties.EsProperties;
import com.es.plus.common.config.GlobalConfigCache;
import com.es.plus.common.exception.EsException;
import com.es.plus.common.properties.GlobalParamHolder;
import com.es.plus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;

import static com.es.plus.constant.Analyzer.ASCIIFOLDING;
import static com.es.plus.constant.Analyzer.EP_IK_MAX_WORD;
import static com.es.plus.constant.Analyzer.EP_IK_SMART;
import static com.es.plus.constant.Analyzer.EP_KEYWORD;
import static com.es.plus.constant.Analyzer.EP_LANGUAGE;
import static com.es.plus.constant.Analyzer.EP_NORMALIZER;
import static com.es.plus.constant.Analyzer.EP_PATTERN;
import static com.es.plus.constant.Analyzer.EP_SIMPLE;
import static com.es.plus.constant.Analyzer.EP_SNOWBALL;
import static com.es.plus.constant.Analyzer.EP_STANDARD;
import static com.es.plus.constant.Analyzer.EP_STOP;
import static com.es.plus.constant.Analyzer.EP_WHITESPACE;
import static com.es.plus.constant.Analyzer.IK_MAX_WORD;
import static com.es.plus.constant.Analyzer.IK_SMART;
import static com.es.plus.constant.Analyzer.KEYWORD;
import static com.es.plus.constant.Analyzer.LANGUAGE;
import static com.es.plus.constant.Analyzer.LOWERCASE;
import static com.es.plus.constant.Analyzer.PATTERN;
import static com.es.plus.constant.Analyzer.SIMPLE;
import static com.es.plus.constant.Analyzer.SNOWBALL;
import static com.es.plus.constant.Analyzer.STANDARD;
import static com.es.plus.constant.Analyzer.STEMMER;
import static com.es.plus.constant.Analyzer.STOP;
import static com.es.plus.constant.Analyzer.WHITESPACE;

/**
 * 初始化配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EsProperties.class)
@AutoConfigureBefore({EsClientConfiguration.class, Es8ClientConfiguration.class})
public class EpGlobalConfigCacheConfigration implements InitializingBean {
    @Autowired
    private EsProperties esProperties;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalConfigCache.GLOBAL_CONFIG = esProperties.getGlobalConfig();
        setAnalysis();
    }
    
    private void setAnalysis() {
        // 内置分词器初始化
        Map<String, AnalysisProperties> analysis = esProperties.getAnalysis();
        analysis.computeIfAbsent(EP_STANDARD, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(STANDARD);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_IK_MAX_WORD, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(IK_MAX_WORD);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_IK_SMART, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(IK_SMART);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_KEYWORD, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(KEYWORD);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_SIMPLE, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(SIMPLE);
            return analysisProperties;
        });
        
        analysis.computeIfAbsent(EP_LANGUAGE, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(LANGUAGE);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_PATTERN, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(PATTERN);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_SNOWBALL, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(SNOWBALL);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_STOP, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(STOP);
            return analysisProperties;
        });
        analysis.computeIfAbsent(EP_WHITESPACE, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{STEMMER, LOWERCASE, ASCIIFOLDING});
            analysisProperties.setTokenizer(WHITESPACE);
            return analysisProperties;
        });
        
        Map<String, AnalysisProperties> normalizers = esProperties.getNormalizers();
        normalizers.computeIfAbsent(EP_NORMALIZER, a -> {
            AnalysisProperties analysisProperties = new AnalysisProperties();
            analysisProperties.setFilters(new String[]{LOWERCASE});
            return analysisProperties;
        });
        
        
        analysis.forEach((analyzerName, analyzer) -> {
            Map<String, Object> analyzerMap =  buildAnalyzer(analyzer.getTokenizer(), analyzer.getFilters(), analyzer.getTokenizer());
            GlobalParamHolder.putAnalysis(analyzerName, analyzerMap);
        });
        
        
        normalizers.forEach((name, normalizer) -> {
            Map<String, Object> map =  buildAnalyzer(normalizer.getTokenizer(), normalizer.getFilters(), normalizer.getTokenizer());
            GlobalParamHolder.putAnalysis(name, map);
        });
    }
    
    public static Map<String, Object> buildAnalyzer(String type, String[] filters, String tokenizer) {
        XContentBuilder xContentBuilder = null;
        try {
            xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject();
            if (!ArrayUtils.isEmpty(filters)) {
                xContentBuilder.field("filter", filters);
            }
            xContentBuilder.field("type", type)
                    .field("tokenizer", tokenizer)
                    .endObject();
        } catch (IOException e) {
            throw new EsException(e);
        }
        BytesReference.bytes(xContentBuilder);
        return JsonUtils.toMap(xContentBuilder.getOutputStream().toString());
    }
    
}
