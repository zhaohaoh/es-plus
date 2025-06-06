package com.es.plus.web.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.autoconfigure.properties.ClientProperties;
import com.es.plus.autoconfigure.util.ClientUtil;
import com.es.plus.starter.auto.EsClientConfiguration;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommondInitTable {
    
    @Autowired
    private EsClientMapper esClientMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EsClientConfiguration esClientConfiguration;
 
    @PostConstruct
    public void init() throws SQLException, IOException {
        
        //初始化数据库表
        createTables();
        
        //初始化客户端
        createClient();
    }
    
    private void createClient() {
        List<EsClientProperties> esClientProperties = esClientMapper.selectList(Wrappers.lambdaQuery());
        Map<String, ClientProperties> map = esClientProperties.stream()
                .collect(Collectors.toMap(EsClientProperties::getUnikey,c->{
                    ClientProperties clientProperties = new ClientProperties();
                    BeanUtils.copyProperties(c, clientProperties);
                    return clientProperties;
                }));
        
        map.forEach((k, v) -> {
            ClientUtil.initAndPutEsPlusClientFacade(k,v,null);
        });
    }
    
    /**
     * 初始化表
     */
    private void createTables() throws SQLException {
        List<String> tables = jdbcTemplate.queryForList("SELECT name FROM sqlite_master ", String.class);
        if (!tables.contains("es_client")) {
            jdbcTemplate.execute("CREATE TABLE \"es_client\" (\n" + "  \"id\" INTEGER NOT NULL,\n" + "  \"unikey\" TEXT,\n"
                    + "  \"name\" TEXT,\n" + "  \"address\" TEXT,\n" + "  \"schema\" TEXT,\n" + "  \"username\" TEXT,\n"
                    + "  \"password\" TEXT,\n" + "  \"createTime\" DATE,\n" + "  PRIMARY KEY (\"id\")\n" + ");");
            
        }
        
  
        if (!tables.contains("es_reindex_task")) {
            jdbcTemplate.execute("CREATE TABLE \"es_reindex_task\" (\n" + "  \"id\" INTEGER NOT NULL,\n"
                    + "  \"es_client_name\" TEXT,\n" + "  \"source_index\" TEXT,\n" + "  \"target_index\" TEXT,\n"
                    + "  \"task_id\" TEXT,\n" + "  \"create_time\" DATE,\n" + "  \"create_uid\" integer,\n"
                    + "  PRIMARY KEY (\"id\")\n" + ");");
        }
        
    }
    
    
   
}
