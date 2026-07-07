package com.dev24.client.mypage.refundhistory.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class RefundhistoryVO extends CommonVO{
	private String rf_orderstate = "";
	private String p_buydate = "";
	private int c_num = 0;
	private int p_num = 0;
	private int rf_num = 0;
	private int b_num = 0;
	private String b_name = "";
	private String rf_confirmdate = "";
	private int rf_price = 0;
	private int rf_qty = 0;
}
