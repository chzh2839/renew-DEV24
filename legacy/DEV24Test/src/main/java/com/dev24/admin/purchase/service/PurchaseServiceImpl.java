package com.dev24.admin.purchase.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dev24.admin.purchase.dao.PurchaseDaoAdmin;
import com.dev24.admin.purchase.vo.AdminPurchaseViewVO;

import lombok.AllArgsConstructor;

@Service("admin.purchaseService")
@AllArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
	
	private PurchaseDaoAdmin purchaseDAO;
	

	// 구매 관리 목록 리스트 출력
	@Override
	public List<AdminPurchaseViewVO> purchaseList(AdminPurchaseViewVO pvo) {
		List<AdminPurchaseViewVO> list = null;
		list = purchaseDAO.purchaseList(pvo);
		return list;
	}

	

}
