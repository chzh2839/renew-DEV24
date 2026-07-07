package com.dev24.client.login.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.client.login.dao.LoginDAO;
import com.dev24.client.login.vo.LoginVO;

import lombok.Setter;

@Service
public class LoginServiceImpl implements LoginService {
	
	@Setter(onMethod_=@Autowired)
	private LoginDAO loginDAO;
	
	
	@Override
	public LoginVO userIdSelect(String c_id) {
		return loginDAO.userIdSelect(c_id);
	}
	
	
	/* 로그인 처리 */
	@Override
	public LoginVO loginSelect(String c_id, String c_passwd) {
		
		LoginVO lvo = new LoginVO();
		lvo.setC_id(c_id);
		lvo.setC_passwd(c_passwd);
		
		LoginVO vo = loginDAO.loginSelect(lvo);
		
		return vo;
	}

}
