package com.es.plus.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.es.plus.web.mapper.EsClientMapper;
import com.es.plus.web.pojo.EsClientProperties;
import com.es.plus.web.service.EsRestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/es/client")
public class EsClientController {
    
    @Autowired
    private EsClientMapper esClientMapper;
    
    @Autowired
    private EsRestService esRestService;
    
    
    @PostMapping("list")
    public List<EsClientProperties> list() {
        List<EsClientProperties> esClientProperties = esClientMapper.selectList(Wrappers.lambdaQuery());
        return esClientProperties;
    }
    
    @GetMapping("get")
    public EsClientProperties get(Long id) {
        EsClientProperties esClientProperties = esClientMapper.selectOne(
                Wrappers.<EsClientProperties>lambdaQuery().eq(EsClientProperties::getId, id));
        return esClientProperties;
    }
    
    @PostMapping("save")
    public void save(@RequestBody EsClientProperties esClientProperties) {
        log.debug("保存ES客户端配置: {}", esClientProperties.getName());
        
        try {
            // 验证地址格式
            validateAddress(esClientProperties);
            
            // 检查是否已存在相同unikey的配置
            LambdaQueryWrapper<EsClientProperties> wrapper = Wrappers.<EsClientProperties>lambdaQuery()
                    .eq(EsClientProperties::getUnikey, esClientProperties.getUnikey());
            EsClientProperties existingConfig = esClientMapper.selectOne(wrapper);
            
            if (existingConfig != null) {
                // 更新现有配置
                BeanUtils.copyProperties(esClientProperties, existingConfig);
                esClientMapper.updateById(existingConfig);
                log.info("更新ES客户端配置: unikey={}", esClientProperties.getUnikey());
            } else {
                // 新增配置
                esClientMapper.insert(esClientProperties);
                log.info("新增ES客户端配置: unikey={}", esClientProperties.getUnikey());
            }
            
        } catch (Exception e) {
            log.error("保存ES客户端配置失败", e);
            throw new RuntimeException("配置保存失败: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("testClient")
    public boolean testClient(@RequestBody EsClientProperties esClientProperties) {
        log.debug("测试ES客户端连接: {}", esClientProperties.getName());
        
        try {
            // 验证地址格式
            validateAddress(esClientProperties);
            
            // 使用HTTP方式测试连接
            return testHttpConnection(esClientProperties);
            
        } catch (Exception e) {
            log.error("测试ES客户端连接失败", e);
            throw new RuntimeException("连接测试失败: " + e.getMessage(), e);
        }
    }
    
    @DeleteMapping("delete")
    public void delete(Long id) {
        try {
            esClientMapper.deleteById(id);
            log.info("删除ES客户端配置: id={}", id);
        } catch (Exception e) {
            log.error("删除ES客户端配置失败: id={}", id, e);
            throw new RuntimeException("删除失败: " + e.getMessage(), e);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 验证ES地址格式
     */
    private void validateAddress(EsClientProperties esClientProperties) {
        String address = esClientProperties.getAddress();
        if (StringUtils.isBlank(address)) {
            throw new RuntimeException("ES地址不能为空");
        }
        
        String[] addresses = address.split(",");
        for (String addr : addresses) {
            addr = addr.trim();
            
            // 检查是否包含协议
            if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
                addr = esClientProperties.getSchema() + "://" + addr;
            }
            
            // 验证地址格式
            try {
                String[] parts = addr.replace("http://", "").replace("https://", "").split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("地址格式错误，应为 host:port 格式");
                }
                
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                if (StringUtils.isBlank(host)) {
                    throw new RuntimeException("主机地址不能为空");
                }
                
                if (port < 1 || port > 65535) {
                    throw new RuntimeException("端口号必须在1-65535之间");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("端口号必须是数字");
            }
        }
    }
    
    /**
     * 测试HTTP连接
     */
    private boolean testHttpConnection(EsClientProperties esClientProperties) {
        try {
            // 直接使用客户端配置测试连接，无需从数据库查询
            return esRestService.pingWithConfig(esClientProperties);
        } catch (Exception e) {
            log.error("HTTP连接测试失败: address={}", esClientProperties.getAddress(), e);
            return false;
        }
    }
}