package com.dev24.common.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class CommonVO {
	
	private String search="";
	private String keyword="";
	
	private String category="";
	
	private String date_start = ""; // default sysdate
	private String date_end = ""; // default sysdate
	
	private String refundCheck = ""; // is refund or not => for adminPage
	private String typeCheck = ""; // is refund or not => for adminPage
	
	private String orderby_when = ""; // orderby
	private String orderby_state = ""; // orderstate
	
	private int pageNum=0;
	private int amount=0;
	
	public CommonVO() {
		this(1,10);
	}
	
	public CommonVO (int pageNum, int amount) {
		this.pageNum = pageNum;
		this.amount = amount;
	}

}
