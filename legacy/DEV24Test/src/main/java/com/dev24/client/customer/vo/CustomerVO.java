package com.dev24.client.customer.vo;

import java.sql.Timestamp;

import com.dev24.client.login.vo.LoginVO;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false)
public class CustomerVO extends LoginVO{
	public int c_num;
	private String oldUserPw;
	private String c_pinno;
	private String c_name;
	private String c_email;
	private String c_phone;
	private String c_address;
	private String c_interest;
	private String c_nletter;
	private Timestamp c_joinDate;
}
