package com.dev24.client.pdetail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev24.client.pdetail.service.PdetailService;
import com.dev24.client.pdetail.service.PdetailServiceImpl;
import com.dev24.client.pdetail.vo.PdetailVO;
import com.dev24.client.purchase.vo.PurchaseVO;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/purchase/*")
@Log4j
public class PdetailController {
	/*@Setter(onMethod_ = @Autowired)
	private PdetailService pdetailService;
	
	public PdetailController() {
		pdetailService = new PdetailServiceImpl();
	}*/
	
	/****************************
	 * 구매상세 테이블 삽입
	 * **********/
	//@RequestMapping(value="/pdetailInsert", method= {RequestMethod.POST})
	/*public String pdetailInsert(PdetailVO pdvo) {
		log.info("pdetailInsert 호출 성공");
		
		int result = 0;
		result = pdetailService.pdetailInsert(pdvo);
		
		return result == 1 ? "purchase/pdetailInsert" : "purchase/purchaseForm";
	}*/
}
