package com.es.plus.samples.controller;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

public class LuceneReader {
    
    
    private String dir;
    
    public LuceneReader(String path) {
        this.dir = path;
    }
    
    /**
     * 获取写入
     *
     * @return
     * @throws IOException
     */
    public IndexWriter getWriter() throws IOException {
        //写入索引文档的路径
        Directory directory = FSDirectory.open(Paths.get(dir));
        //中文分词器 SmartChineseAnalyzer
        Analyzer analyzer = new SimpleAnalyzer();
        //保存用于创建IndexWriter的所有配置。
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, iwConfig);
    }
    
    /**
     * 获取读取
     *
     * @return
     * @throws Exception
     */
    public IndexReader getReader() throws Exception {
        //写入索引文档的路径
        Directory directory = FSDirectory.open(Paths.get(dir));
        return DirectoryReader.open(directory);
    }
    
    
    /**
     * 根据字段和值查询
     *
     * @param field
     * @param q
     * @throws Exception
     */
    public void searchForField(String field, String q) throws Exception {
        IndexReader reader = getReader();
        // 建立索引查询器
        IndexSearcher is = new IndexSearcher(reader);
        //中文分词器(查询分词器要和存储分词器一致) SmartChineseAnalyzer
        Analyzer analyzer = new SimpleAnalyzer();
        // 建立查询解析器
        QueryParser parser = new QueryParser(field, analyzer);
        // 根据传进来的p查找
        Query query = parser.parse(q);
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        TopDocs hits = is.search(query, 10);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        
        // 遍历hits.scoreDocs，得到scoreDoc
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println("docId:" + scoreDoc.doc + "," + doc);
            BytesRef source = doc.getBinaryValue("_source");
            System.out.println("source:" + source.utf8ToString());
        }
        
        // 关闭reader
        reader.close();
    }
    
    public static void main(String[] args) {
        LuceneReader searchEsIndex = new LuceneReader("D:\\docker\\elasticsearch\\data1\\nodes\\0\\indices\\V-obFy2jSJCnRcq0VUr_Cg\\1\\index");
        try {
            searchEsIndex.searchForField("*", "*");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
