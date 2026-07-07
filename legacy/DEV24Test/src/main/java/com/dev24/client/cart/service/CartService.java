package com.dev24.client.cart.service;

import java.util.List;

import com.dev24.client.cart.vo.CartVO;


public interface CartService {
	public List<CartVO> cartList(CartVO cvo);
	public int cartUpdate(CartVO cvo);
	public int cartDelete(int crt_num);
	public int addToCart(List<CartVO> cvoList);
	public int getCrtNum();
	public int buySingleItem(CartVO cvo);
}
