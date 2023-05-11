package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.constant.EsFieldType;
import lombok.Data;


@Data
public class SamplesNestedDTO {

    private Long id;
    @EsField(type = EsFieldType.KEYWORD)
    private String username;
    @EsField(type = EsFieldType.TEXT)
    private String email;
    @EsField(type = EsFieldType.BOOLEAN)
    private Boolean state;
    @EsField(type = EsFieldType.BOOLEAN)
    private Boolean aaaqedd;
    @EsField(type = EsFieldType.BOOLEAN)
    private Boolean ffffddd;
    @EsField(type = EsFieldType.BOOLEAN)
    private Boolean aaaa;
    @EsField(type = EsFieldType.BOOLEAN)
    private Boolean ccccc;
}
