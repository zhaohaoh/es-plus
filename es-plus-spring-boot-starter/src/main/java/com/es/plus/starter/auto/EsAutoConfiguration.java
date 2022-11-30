package com.es.plus.starter.auto;

import com.es.plus.client.EsPlusClientFacade;
import com.es.plus.client.EsPlusIndexRestClient;
import com.es.plus.client.EsPlusRestClient;
import com.es.plus.config.GlobalConfigCache;
import com.es.plus.lock.ELockClient;
import com.es.plus.lock.EsLockClient;
import com.es.plus.lock.EsLockFactory;
import com.es.plus.properties.EsParamHolder;
import com.es.plus.starter.properties.AnalysisProperties;
import com.es.plus.starter.properties.EsProperties;
import com.es.plus.util.XcontentBuildUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Map;

import static com.es.plus.constant.Analyzer.*;


/**
 * @Author: hzh
 * @Date: 2022/6/13 16:04
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ComponentScan(basePackages = "com.es.plus")
@EnableConfigurationProperties(EsProperties.class)
public class EsAutoConfiguration implements InitializingBean {
    @Autowired
    private EsProperties esProperties;

    /**
     * es 外观
     *
     * @param restHighLevelClient 其他高水平客户
     * @param esLock              es锁
     * @return {@link EsPlusClientFacade}
     */
    @Bean
    public EsPlusClientFacade esPlusClientFacade(RestHighLevelClient restHighLevelClient, EsLockFactory esLock) {
        EsPlusRestClient esPlusRestClient = new EsPlusRestClient(restHighLevelClient, esLock);
        EsPlusIndexRestClient esPlusIndexRestClient = new EsPlusIndexRestClient(restHighLevelClient);
        return new EsPlusClientFacade(esPlusRestClient, esPlusIndexRestClient, esLock);
    }

    /**
     * es锁
     *
     * @param esLockClient es锁定客户
     * @return {@link EsLockFactory}
     */
    @Bean
    public EsLockFactory esLock(ELockClient esLockClient) {
        return new EsLockFactory(esLockClient);
    }

    /**
     * es 锁客户端
     */
    @Bean
    public ELockClient esPlusLockClient(RestHighLevelClient restHighLevelClient) {
        return new EsLockClient(restHighLevelClient);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalConfigCache.GLOBAL_CONFIG = esProperties.getGlobalConfig();

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

        analysis.forEach((analyzerName, analyzer) -> {
            Map<String, Object> analyzerMap = XcontentBuildUtils.buildAnalyzer(analyzer.getTokenizer(), analyzer.getFilters(), analyzer.getTokenizer());
            EsParamHolder.putAnalysis(analyzerName, analyzerMap);
        });

    }
}
