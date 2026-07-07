package com.dev24.admin.purchase.dao;

import java.util.List;

import com.dev24.admin.purchase.vo.AdminPurchaseViewVO;

public interface PurchaseDaoAdmin {
	public List<AdminPurchaseViewVO> purchaseList(AdminPurchaseViewVO pvo);
}
