package com.dev24.admin.review.vo;

import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AdminReviewViewVO extends CommonVO {
	private int re_num = 0;
	private int c_num = 0;
	private String c_id = "";
	private String c_nickname = "";
	private int b_num = 0;
	private String b_name = "";
	private int pd_num = 0;
	private int p_num = 0;
	private String re_writedate = "";
	private String re_type = "";
}
