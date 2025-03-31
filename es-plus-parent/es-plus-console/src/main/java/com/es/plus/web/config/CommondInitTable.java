package com.es.plus.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Component
public class CommondInitTable {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void init() throws SQLException, IOException {
        
        //初始化数据库表
        createTables();
    }
    
    /**
     * 初始化表
     */
    private void createTables() throws SQLException {
        List<String> tables = jdbcTemplate.queryForList("SELECT name FROM sqlite_master ", String.class);
        if (tables.contains("es_client")) {
            return;
        }
        
        jdbcTemplate.execute("CREATE TABLE \"es_client\" (\n" + "  \"id\" INTEGER NOT NULL,\n" + "  \"address\" TEXT,\n"
                + "  \"schema\" TEXT,\n" + "  \"username\" TEXT,\n" + "  \"password\" TEXT,\n"
                + "  \"createTime\" DATE,\n" + "  PRIMARY KEY (\"id\")\n" + ");");
        
        
    }
    
    
}
