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
@TableName("es_reindex_task")
public class EsReindexTaskVO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 来源客户端
     */
   
    private String sourceClient;
    
    /**
     * 目标客户端
     */
    private String targetClient;
    /**
     * 源索引
     */
    private String sourceIndex;
    
    /**
     * 目标索引
     */
    private String targetIndex;
    
    /**
     * 任务id
     */
    private String taskId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 创建人
     */
    private Long createUid;
    
    /**
     * 创建人
     */
    private String taskJson;
    
    
    /**
     * 类型
     */
    private Integer type;
    private Boolean completed;
    
}
