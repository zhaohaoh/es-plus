package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import com.es.plus.annotation.EsId;
import com.es.plus.annotation.EsIndex;
import com.es.plus.constant.EsFieldType;
import lombok.Data;

import java.util.Date;


@Data
@EsIndex(index = "uu_land" )
public class SpuEsDTO {
    // id  不添加注解也会默认获取.
    @EsId
    private Long landId;
    @EsField(name = "apply_did")
    private Integer apply_did;
    @EsField(name = "resourceID")
    private Integer resourceId;
    @EsField(name = "area_town")
    private Integer area_town;
    @EsField(name = "area_city")
    private Integer area_city;
    /**
     * 身份证
     */
    @EsField(name = "venus_id")
    private Integer venus_id;

    /**
     * 性别0男1女
     */
    @EsField
    private Integer terminal;

    @EsField
    private String title;

    @EsField(name = "operater_id")
    private Integer operaterId;

    @EsField(name = "oversea")
    private Integer oversea;

    @EsField(name = "uptime")
    private Date uptime;

    @EsField(name = "order_flag")
    private Integer order_flag;

    @EsField(name = "salerid")
    private Integer salerid;


    @EsField(name = "oversea")
    private Integer sub_type;

    @EsField(name = "addtime")
    private Date addtime;

    @EsField(name = "p_type")
    private Integer p_type;


    @EsField(name = "area_province")
    private Integer area_province;
    @EsField(name = "verify_status")
    private Integer verify_status;
    @EsField(type = EsFieldType.KEYWORD)
    private String letters;
    @EsField(name = "status")
    private Integer status;
    @EsField(type = EsFieldType.NESTED)
    private TicketEsDTO ticketEsDTO;
}
