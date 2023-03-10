package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.Analyzer;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@EsIndex(index = "sys_user2ttt" )
public class SamplesEsDTO {
    // id  不添加注解也会默认获取.
    @EsId
    private Long id;
    @EsField(type = EsFieldType.KEYWORD,normalizer = Analyzer.EP_NORMALIZER)
    private String username;
    @EsField(copyTo = "keyword")
    private String email;
    @EsField(copyTo = "keyword")
    private String phone;
    @EsField(type = EsFieldType.KEYWORD)
    private String keyword;

    private String password;
    /**
     * 身份证
     */
    @EsField(copyTo = "keyword")
    private String idCard;

    /**
     * 性别0男1女
     */
    @EsField(store = true)
    private int sex;


    private String avatar;
    @EsField(copyTo = "keyword")
    private String nickName;

    private Boolean lockState;
    private LocalDateTime unlockTime;
    private Boolean deleteState;
    @EsField(type = EsFieldType.TEXT)
    private String aaaaa;
    //
//    @TableField(exist = false)
//    @IgnoreSwaggerParameter
    @EsField(type = EsFieldType.NESTED)
    private SamplesNestedDTO samplesNesteds;
}
