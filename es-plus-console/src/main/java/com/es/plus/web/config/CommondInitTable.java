package com.es.plus.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;

@Component
public class CommondInitTable {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void init() throws SQLException {
        // 初始化数据库表
        createTables();
        
        // 注意：ES客户端初始化已移除，现在使用HTTP方式直接连接ES
        // 客户端配置通过数据库管理，由EsRestService动态使用
    }
    
    /**
     * 初始化表
     */
    private void createTables() throws SQLException {
        List<String> tables = jdbcTemplate.queryForList("SELECT name FROM sqlite_master ", String.class);
        if (!tables.contains("es_client")) {
            jdbcTemplate.execute("CREATE TABLE \"es_client\" (\n" + "  \"id\" INTEGER NOT NULL,\n" + "  \"unikey\" TEXT,\n" + "  \"version\" TEXT,\n"
                    + "  \"name\" TEXT,\n" + "  \"address\" TEXT,\n" + "  \"schema\" TEXT,\n" + "  \"username\" TEXT,\n"
                    + "  \"password\" TEXT,\n" + "  \"createTime\" DATE,\n" + "  PRIMARY KEY (\"id\")\n" + ");");
            
        }
        
        
        if (!tables.contains("es_reindex_task")) {
            jdbcTemplate.execute("CREATE TABLE \"es_reindex_task\" (\n" + "  \"id\" INTEGER NOT NULL,\n"
                    + "  \"source_client\" TEXT,\n" + "  \"source_index\" TEXT,\n" + "  \"target_index\" TEXT,\n"
                    + "  \"task_id\" TEXT,\n" + "  \"create_time\" DATE,\n" + "  \"create_uid\" integer,\n"
                    + "  \"target_client\" TEXT,\n" + "  \"type\" integer,\n" + "  \"task_json\" TEXT,\n"
                    + "  PRIMARY KEY (\"id\")\n" + ");");
        }
        
    }
    
    
}