package com.dev24.admin.pdetail.dao;

import java.util.List;

import com.dev24.admin.pdetail.vo.AdminPdetailViewVO;

public interface PdetailDaoAdmin {
	public List<AdminPdetailViewVO> pdetailList(AdminPdetailViewVO pdvo);
}
