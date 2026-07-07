package com.dev24.client.mypage.orderhistory.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class OrderhistoryVO extends CommonVO {
	// order_history_view
	private String p_buydate = "";
	private int p_num = 0;
	private int b_num = 0;
	private String b_name = "";
	private String pd_orderstate = "";
	private int c_num = 0;
	private int pd_qty = 0;
	private String p_sender = "";
	private String p_receiver = "";
	private int pd_price = 0;
	private int pd_num = 0;
	
	// refundForm print elements
	private int singlePrice = 0;
	private String listcover_imgurl = "";
}
