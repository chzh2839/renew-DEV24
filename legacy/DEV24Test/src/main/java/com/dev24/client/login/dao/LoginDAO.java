package com.dev24.client.login.dao;

import com.dev24.client.login.vo.LoginVO;

public interface LoginDAO {
	public LoginVO userIdSelect(String c_id);
	public LoginVO loginSelect(LoginVO lvo);
}
