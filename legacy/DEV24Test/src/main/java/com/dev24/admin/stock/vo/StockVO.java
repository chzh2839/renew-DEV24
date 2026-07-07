package com.dev24.admin.stock.vo;

import com.dev24.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class StockVO extends CommonVO{
	private int stk_incp=0;
	private String b_name="";
	private String b_author="";
	private int b_num=0;
	private int stk_qty=0;
	private int stk_salp=0;
	private int catetwo_num=0;
	private int cateone_num=0;
	private int adm_num=0;
	private String stk_regdate="";
	private String adm_name="";
}
