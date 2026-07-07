package com.dev24.admin.pdetail.service;

import java.util.List;

import com.dev24.admin.pdetail.vo.AdminPdetailViewVO;

public interface PdetailService {
	public List<AdminPdetailViewVO> pdetailList(AdminPdetailViewVO pdvo);
	
}
