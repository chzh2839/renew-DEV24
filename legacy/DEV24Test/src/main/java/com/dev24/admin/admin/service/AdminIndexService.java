package com.dev24.admin.admin.service;


import com.dev24.admin.admin.vo.AdminIndexVO;

public interface AdminIndexService {
	public AdminIndexVO adminIndex();
	public int pCount();
	public int stkCount();
	public int rfCount();
	public int salCount();
	public int revCount();
	public int qnaCount();
	public int neCount();
	public int fbCount();
	
}
