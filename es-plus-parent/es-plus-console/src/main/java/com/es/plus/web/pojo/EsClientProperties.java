package com.es.plus.web.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: hzh
 * @Date: 2023/2/1 16:54
 */
@Data
@TableName("es_client")
public class EsClientProperties {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 自定义唯一key 英文标识
     */
    private String unikey;
    /**
     * 自定义链接名称 中文
     */
    private String name;
    
    /**
     * 集群地址，多个用,隔开
     */
    private String address;

    /**
     * 模式
     */
    private String schema = "http";
    
    /**
     * 用户名称
     */

    private String username;

    /**
     * 密码
     */
    private String password;
 
}
