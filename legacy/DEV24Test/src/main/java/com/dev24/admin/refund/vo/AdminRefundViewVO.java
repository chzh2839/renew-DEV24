package com.dev24.admin.refund.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AdminRefundViewVO extends CommonVO {
	private int rf_num = 0;
	private int b_num = 0;
	private String b_name = "";
	private int c_num = 0;
	private String c_id = "";
	private int rf_price = 0;
	private String rf_reason = "";
	private String rf_confirmdate = "";
	private String rf_orderstate = "";
	
	// for refund confirm logic
	//private String pd_orderstate = "";
	//private int pd_num = 0;
}
