package com.dev24.admin.purchase.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AdminPurchaseViewVO extends CommonVO {
	private int p_num = 0;
	private int c_num = 0;
	private String c_id = "";
	private String p_pmethod = ""; // 결제방법
	private int sales_price = 0; // 매출금액(구매금액-환불금액)
	private String p_buydate = ""; // 구매일
	private String isRefund = ""; // 환불여부 Y/N

}
