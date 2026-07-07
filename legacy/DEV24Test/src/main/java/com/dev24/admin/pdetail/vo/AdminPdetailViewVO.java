package com.dev24.admin.pdetail.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AdminPdetailViewVO extends CommonVO {
	private int p_num = 0;
	private int pd_num = 0;
	private int b_num = 0;
	private String b_name = "";
	private int c_num = 0;
	private String c_id = "";
	private String p_pmethod = "";
	private int pd_price = 0;
	private String p_buydate = "";
	private String pd_orderstate = "";
	private int rf_num = 0;
	private int pd_qty = 0;
}
