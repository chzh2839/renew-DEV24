package com.dev24.admin.purchase.service;

import java.util.List;

import com.dev24.admin.purchase.vo.AdminPurchaseViewVO;

public interface PurchaseService {
	public List<AdminPurchaseViewVO> purchaseList(AdminPurchaseViewVO pvo);
	
}
