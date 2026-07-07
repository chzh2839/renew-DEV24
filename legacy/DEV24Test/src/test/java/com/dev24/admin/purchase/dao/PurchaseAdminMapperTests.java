package com.dev24.admin.purchase.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.admin.pdetail.dao.PdetailDaoAdmin;
import com.dev24.admin.pdetail.vo.AdminPdetailViewVO;
import com.dev24.admin.purchase.dao.PurchaseDaoAdmin;
import com.dev24.admin.purchase.vo.AdminPurchaseViewVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class PurchaseAdminMapperTests {
	@Setter(onMethod_ = @Autowired)
	private PurchaseDaoAdmin purchaseDaoAdmin;
	
	@Setter(onMethod_ = @Autowired)
	private PdetailDaoAdmin pdetailDaoAdmin;

	/*@Test
	public void testPurchaseList() {
		log.info("purchaseList 메서드 호출");
		AdminPurchaseViewVO pvo = new AdminPurchaseViewVO();
		pvo.setDate_start("2020-11-01");
		pvo.setDate_end("2020-11-07");
		pvo.setRefundCheck("Y");
		pvo.setSearch("p_num");
		pvo.setKeyword("1");
		List<AdminPurchaseViewVO> list = purchaseDaoAdmin.purchaseList(pvo);
		for(AdminPurchaseViewVO vo : list) {
			log.info(vo);
		}
	}*/
	
	@Test
	public void testPdetailList() {
		log.info("pdetailList 메서드 호출");
		AdminPdetailViewVO pdvo = new AdminPdetailViewVO();
		pdvo.setP_num(76);
		List<AdminPdetailViewVO> list = pdetailDaoAdmin.pdetailList(pdvo);
		for(AdminPdetailViewVO vo : list) {
			log.info(vo);
		}
	}
}
