package com.dev24.client.purchase.service;

import java.util.List;

import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.customer.vo.CustomerVO;
import com.dev24.client.pdetail.vo.PdetailVO;
import com.dev24.client.purchase.vo.PurchaseVO;

public interface PurchaseService {
	public List<CartVO> purchaseForm(List<CartVO> cvoList);
	
	// 주문자 정보 출력
	public CustomerVO getSenderInfo(int c_num);
	
	public int purchaseInsert(PurchaseVO pvo);
	public int pdetailInsert(List<PdetailVO> pdvoList);
	
	public int getMaxPnum();
	
	// 구매 완료한 상품 삭제
	public int purchasedItemDelete(List<CartVO> cvoList);
}
