package com.dev24.admin.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.admin.admin.dao.AdminDAO;
import com.dev24.admin.admin.vo.AdminVO;

import lombok.Setter;
@Service
public class AdminServiceImpl implements AdminService {
	
	@Setter(onMethod_=@Autowired)
	private AdminDAO adminDAO;
	
	@Override
	public AdminVO adminPasswdChk(AdminVO avo) {
		AdminVO advo = new AdminVO();
		advo = adminDAO.adminPasswdChk(avo);
		return advo;
	}

}
