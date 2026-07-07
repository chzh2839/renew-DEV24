package com.dev24.client.cart.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.cart.vo.CartVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class CartMapperTests {
	@Setter(onMethod_ = @Autowired)
	private CartDAO cartdao;
	
	@Test
	public void testCartList() {
		log.info("cartList 메서드 호출");
		CartVO cvvo = new CartVO();
		List<CartVO> list = cartdao.cartList(cvvo);
		for(CartVO vo : list) {
			log.info(vo);
		}
	}
}
