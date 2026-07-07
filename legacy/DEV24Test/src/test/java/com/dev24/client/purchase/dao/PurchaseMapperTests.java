package com.dev24.client.purchase.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.purchase.vo.PurchaseVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class PurchaseMapperTests {
	@Setter(onMethod_ = @Autowired)
	private PurchaseDAO purchaseDAO;
	
	/*@Test
	public void testPurchaseForm() {
		log.info("purchaseForm ¸Þ¼­µå È£Ãâ");
		CartVO cvo = new CartVO();
		cvo.setCrt_num(6);
		List<CartVO> result = purchaseDAO.purchaseForm(cvo);
		for(CartVO vo : result) {
			log.info("purchaseForm °á°ú : "+ vo);			
		}
	}*/
	
	//@Test
	/*public void testPurchaseInsert() {
		log.info("purchaseInsert ï¿½Þ¼ï¿½ï¿½ï¿½ È£ï¿½ï¿½");
		PurchaseVO pvo = new PurchaseVO();
		pvo.setC_num(2);
		pvo.setP_address("test");
		pvo.setP_pmethod("test");
		pvo.setP_price(1000);
		pvo.setP_receivephone("test");
		pvo.setP_receiver("test");
		pvo.setP_sender("test");
		pvo.setP_senderphone("test");
		pvo.setP_zipcode("test");
		int result = purchaseDAO.purchaseInsert(pvo);
		log.info(result);
	}*/

}
