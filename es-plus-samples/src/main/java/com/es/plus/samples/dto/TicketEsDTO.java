package com.es.plus.samples.dto;

import com.es.plus.annotation.EsField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
* 门票查询对象
 */
@Data
public class TicketEsDTO {
	/**
	 * 门票id
	 */
	private Long id;
	/**
	 * 门票名称
	 */
	private String title;
	/**
	 * 状态 1上架 2下架 6删除
	 */
//	private Integer status;
	/**
	 * 状态集合
	 */
	private Integer  status;

	/**
	 * 门票过期最大日期
	 */
	@EsField(name = "max_expiration_date")
	private Date max_expiration_date;
	/**
	 * 最小更新时间
	 */
	@EsField(name = "min_update_time")
	private Date min_update_time;
	/**
	 * 最大更新时间
	 */
	@EsField(name = "max_update_time")
	private Date max_update_time;
	/**
	 * 销售渠道
	 */
	private List<Integer> shop;
	/**
	 * 商品id
	 */
	private Integer gid;
	/**
	 * 市场价
	 */
	private Float tprice;
	/**
	 * pid
	 */
	private Long pid;
	/**
	 * 订单有效期开始时间
	 */
	@EsField(name = "order_start")
	private Date order_start;
	/**
	 * 订单有效期结束时间
	 */
	@EsField(name = "order_end")
	private Date order_end;
	/**
	 * 期票 0否 1是
	 */
	@EsField(name = "pre_sale")
	private Integer preSale;
	/**
	 * 是否支持抱团
	 */
	@EsField(name = "order_flag")
	private Integer orderFlag;
}