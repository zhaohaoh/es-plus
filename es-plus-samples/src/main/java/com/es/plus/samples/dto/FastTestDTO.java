package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.Date;

/**
 * 快速测试
 * 举例4种类型索引
 */
@Data
@EsIndex(index = "fast_test",tryReindex = true,alias = "fast_test_alias")
public class FastTestDTO {
    @EsId
    private Long id;
    @EsField(type = EsFieldType.KEYWORD)
    private String username;
    @EsField(type = EsFieldType.TEXT)
    private String text;
    @EsField(type = EsFieldType.INTEGER)
    private Integer age;
    @EsField(type = EsFieldType.DATE, esFormat = "yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis",dateFormat = "yyyy-MM")
    private Date createTime;
}
