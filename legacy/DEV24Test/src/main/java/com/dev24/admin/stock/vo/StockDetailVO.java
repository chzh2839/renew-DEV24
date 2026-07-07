package com.dev24.admin.stock.vo;

import lombok.Data;

@Data
public class StockDetailVO {
	private int stk_incp=0;
	//private int b_num=0;
	private String b_name="";
	private String b_date="";
	private String b_author="";
	private String b_pub="";
	private int b_price=0;
	private int cateone_num=0;
	private int catetwo_num=0;
	private String listcover_imgurl="";
}
