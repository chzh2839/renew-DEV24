package com.dev24.client.login.vo;

import com.dev24.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LoginVO extends CommonVO {
	private int c_num;
	private String c_id = "";
	private String c_passwd = "";
	private String c_nickname = "";
}
