package com.dev24.client.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.client.cart.dao.CartDAO;
import com.dev24.client.cart.vo.CartVO;

import lombok.Setter;

@Service
public class CartServiceImpl implements CartService {

	@Setter(onMethod_ = @Autowired)
	private CartDAO cartDAO;
	
	// 장바구니에 담긴 도서 출력
	@Override
	public List<CartVO> cartList(CartVO cvo) {
		List<CartVO> list = null;
		list = cartDAO.cartList(cvo);
		return list;
	}

	// 장바구니 수량 변경
	@Override
	public int cartUpdate(CartVO cvo) {
		int result = 0;
		result = cartDAO.cartUpdate(cvo);
		return result;
	}

	// 장바구니 상품 삭제 
	@Override
	public int cartDelete(int crt_num) {
		int result = 0;
		result = cartDAO.cartDelete(crt_num);
		return result;
	}
	
	//장바구니 추가
	@Override
	public int addToCart(List<CartVO> cvoList) {
		int result = cartDAO.addToCart(cvoList);
		return result;
	}
	
	@Override
	public int getCrtNum() {
		int result = cartDAO.getCrtNum();
		return result;
	}
	
	@Override
	public int buySingleItem(CartVO cvo) {
		int result = cartDAO.buySingleItem(cvo);
		return result;
	}

}
