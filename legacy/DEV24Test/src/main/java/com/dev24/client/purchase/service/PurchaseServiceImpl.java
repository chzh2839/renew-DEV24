package com.dev24.client.purchase.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.client.cart.dao.CartDAO;
import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.customer.dao.CustomerDAO;
import com.dev24.client.customer.vo.CustomerVO;
import com.dev24.client.pdetail.dao.PdetailDAO;
import com.dev24.client.pdetail.vo.PdetailVO;
import com.dev24.client.purchase.dao.PurchaseDAO;
import com.dev24.client.purchase.vo.PurchaseVO;
import com.dev24.client.rating.dao.RatingDAO;

import lombok.AllArgsConstructor;

@Service("client.purchaseService")
@AllArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
	private PurchaseDAO purchaseDAO;
	
	private CustomerDAO customerDAO;
	private PdetailDAO pdetailDAO;
	private CartDAO cartDAO;
	private RatingDAO ratingDAO;

	// 구매화면 출력(체크 상품 가져오기)
	@Override
	public List<CartVO> purchaseForm(List<CartVO> cvoList) {
		List<CartVO> list = null;
		list = purchaseDAO.purchaseForm(cvoList);
		return list;
	}

	// 주문자 정보 출력
	@Override
	public CustomerVO getSenderInfo(int c_num) {
		CustomerVO cvo = null;
		cvo = customerDAO.getSenderInfo(c_num);
		return cvo;
	}

	// 구매 삽입
	@Override
	public int purchaseInsert(PurchaseVO pvo) {
		int result = 0;
		result = purchaseDAO.purchaseInsert(pvo);
		return result;
	}

	// 구매 상세 자동 삽입
	@Override
	@Transactional
	public int pdetailInsert(List<PdetailVO> pdvoList) {
		//pdetail
		int result = 0;
		result= pdetailDAO.pdetailInsert(pdvoList);
		
		//update Rating.salesCnt
		ratingDAO.updateSalesCnt(pdvoList);
		
		return result;
	}

	// p_num 구하기
	@Override
	public int getMaxPnum() {
		int result = 0;
		result = purchaseDAO.getMaxPnum();
		return result;
	}

	// 구매 완료한 상품 삭제
	@Override
	public int purchasedItemDelete(List<CartVO> cvoList) {
		int result = 0;
		result = cartDAO.purchasedItemDelete(cvoList);
		return result;
	}
	
	

}
