package com.es.plus.web.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: hzh
 * @Date: 2023/2/1 16:54
 */
@Data
@TableName("es_reindex_task")
public class EsReindexTask {
    @TableId(type = IdType.AUTO)
    private Long id;
 
    /**
     * 来源客户端
     */
    @TableField(value = "source_client")
    private String sourceClient;
    
    /**
     * 目标客户端
     */
    @TableField(value = "target_client")
    private String targetClient;
    
    /**
     * 源索引
     */
    @TableField(value = "source_index")
    private String sourceIndex;
    
    /**
     * 目标索引
     */
    @TableField(value = "target_index")
    private String targetIndex;
    
    /**
     * 任务类型 1=同集群reindex 2=跨集群数据迁移
     */
    @TableField(value = "type")
    private Integer type;
    /**
     * 任务id
     */
    @TableField(value = "task_id")
    private String taskId;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Long createTime;
    
    /**
     * 创建人
     */
    @TableField(value = "create_uid")
    private Long createUid;
    /**
     * 任务明细
     */
    @TableField(value = "task_json")
    private String taskJson;
    
  
}
