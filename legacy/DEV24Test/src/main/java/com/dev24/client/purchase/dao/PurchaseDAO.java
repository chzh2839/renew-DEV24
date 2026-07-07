package com.dev24.client.purchase.dao;

import java.util.List;

import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.purchase.vo.PurchaseVO;

public interface PurchaseDAO {
	public List<CartVO> purchaseForm(List<CartVO> cvoList);
	public int purchaseInsert(PurchaseVO pvo);
	//public int purchaseInsert(List<PurchaseVO> pvoList);
	public int getMaxPnum();
}
