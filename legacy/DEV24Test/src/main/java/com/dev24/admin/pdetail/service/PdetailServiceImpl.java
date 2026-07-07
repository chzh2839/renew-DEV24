package com.dev24.admin.pdetail.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dev24.admin.pdetail.dao.PdetailDaoAdmin;
import com.dev24.admin.pdetail.vo.AdminPdetailViewVO;

import lombok.AllArgsConstructor;

@Service("admin.pdetailService")
@AllArgsConstructor
public class PdetailServiceImpl implements PdetailService {
	
	private PdetailDaoAdmin pdetailDaoAdmin;

	// 구매 상세 내용 조회
	@Override
	public List<AdminPdetailViewVO> pdetailList(AdminPdetailViewVO pdvo) {
		List<AdminPdetailViewVO> list = null; 
		list = pdetailDaoAdmin.pdetailList(pdvo);
		return list;
	}




}
