package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.annotation.Score;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 快速测试
 * 举例4种类型索引
 */
@Data
@EsIndex(index = "fast_test_s0",tryReindex = true ,alias = "fast_test_alias")
public class FastTestDTO {
    @EsId
    private Long id;
    @EsField(type = EsFieldType.KEYWORD)
    private String username;
    @EsField(type = EsFieldType.TEXT)
    private String text;
    @EsField(type = EsFieldType.INTEGER)
    private Integer age;
    
    @EsField(type = EsFieldType.KEYWORD)
    private List<String> testList;
    @EsField(type = EsFieldType.DATE, esFormat = "yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis",dateFormat = "yyyy-MM")
    private Date createTime;

    @EsField(type = EsFieldType.KEYWORD,name = "username_test")
    private String usernameTest;
    @EsField(type = EsFieldType.KEYWORD,name = "usernameTest1")
    private String username_test1;
    @EsField(type = EsFieldType.KEYWORD,name = "usernameTest13")
    private String username_test13;
    @Score
    private Float score;
}
