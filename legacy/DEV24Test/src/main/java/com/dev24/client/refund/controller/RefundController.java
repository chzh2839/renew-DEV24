package com.dev24.client.refund.controller;


import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev24.client.login.vo.LoginVO;
import com.dev24.client.mypage.orderhistory.service.OrderhistoryService;
import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;
import com.dev24.client.purchase.vo.PurchaseVO;
import com.dev24.client.refund.service.RefundService;
import com.dev24.client.refund.vo.RefundVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller("client.refundController")
@AllArgsConstructor
@RequestMapping("/refund/*")
@Log4j
public class RefundController {
	
	private RefundService refundService;
	private OrderhistoryService orderhistoryService;
	
	/** go to refundForm page **/
	@RequestMapping(value="/refundForm", method= {RequestMethod.GET})
	public String refundForm(@ModelAttribute("data1") PurchaseVO pvo, @ModelAttribute("data2") OrderhistoryVO ohvo, Model model) {
		log.info("refundForm 호출 성공");
		
		PurchaseVO purchase = null;
		purchase = refundService.getPurchaseInfo(pvo);
		
		OrderhistoryVO book = null;
		book = refundService.getRefundItems(ohvo);

		//log.info(book);
		//log.info("ohvo.getB_num() => "+ohvo.getB_num());
		//log.info("ohvo.getP_num() => "+ohvo.getP_num());
		model.addAttribute("pvo", purchase);
		model.addAttribute("ohvo", book);
		
		return "refund/refundForm";
	}
	
	
	/** refund insert **/
	@RequestMapping(value="/refundInsert", method= {RequestMethod.POST})
	public String refundInsert(@ModelAttribute("data") RefundVO rfvo, OrderhistoryVO ohvo, HttpSession session, RedirectAttributes ras) {
		log.info("refundInsert 호출 성공");
		
		int result = 0;
		int update = 0;
		String path = "";
		
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		rfvo.setC_num(c_num);
		rfvo.setRf_num(ohvo.getPd_num());
		rfvo.setRf_orderstate(ohvo.getPd_orderstate());
		
		log.info("c_num => "+c_num);
		log.info("rfvo => "+rfvo);
		
		result = refundService.refundInsert(rfvo);
		
		if(result == 0) {
			path = "/refund/refundForm";
		}else {
			ohvo.setC_num(c_num);
			update = orderhistoryService.orderstateUpdate(ohvo);
			
			if(update == 0) {
				ras.addFlashAttribute("failMsg", "환불신청 실패. 잠시 후 다시 시도해주세요.");
				path = "/refund/refundForm";
			}else {
				ras.addFlashAttribute("successMsg", "환불신청이 완료되었습니다.");
				path = "/mypage/refundHistory";
			}
		}
		return "redirect:"+path;
	}
	
	
}
