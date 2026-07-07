package com.dev24.client.login.service;

import com.dev24.client.login.vo.LoginVO;

public interface LoginService {
	public LoginVO userIdSelect(String c_id);
	public LoginVO loginSelect(String c_id, String c_passwd);
}
