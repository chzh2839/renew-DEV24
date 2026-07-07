package com.dev24.client.mypage.orderhistory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.login.vo.LoginVO;
import com.dev24.client.mypage.orderhistory.service.OrderhistoryService;
import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;
import com.dev24.client.pdetail.vo.PdetailVO;
import com.dev24.client.refund.service.RefundService;
import com.dev24.client.refund.vo.RefundVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/mypage/*")
@Log4j
@AllArgsConstructor
public class OrderhistoryController {
	
	private OrderhistoryService orderhistoryService;
	private RefundService refundService;
	
	/** mypage order history list print
	 * @param String */
	@RequestMapping(value="/orderHistory", method= {RequestMethod.GET})
	public String orderHistory(@ModelAttribute("data") OrderhistoryVO ohvo, Model model, HttpSession session) {
		log.info("orderHistory 호출 성공");
		
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		ohvo.setC_num(c_num);
		
		// 주문내역 조회
		List<OrderhistoryVO> ohlist = orderhistoryService.orderhistoryList(ohvo);
		model.addAttribute("ohvo", ohlist);
		
		return "mypage/orderHistory";
	}
	
	
	/** mypage - orderstate update
	 * @param String
	 * @ResponseBody */
	@ResponseBody
	@RequestMapping(value="/orderstateUpdate", method= {RequestMethod.GET})
	public String orderstateUpdate(@ModelAttribute("data1") OrderhistoryVO ohvo, @ModelAttribute("data2") RefundVO rfvo, HttpSession session) {
		log.info("orderstateUpdate get 호출 성공");
		
		String resultData = "";
		int result = 0;
		int refundInsert = 0;
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		rfvo.setC_num(c_num);
		rfvo.setRf_reason("배송 전 주문취소");
		
		if(ohvo.getPd_orderstate().equals("pConfirm")) {
			result = orderhistoryService.orderstateUpdate(ohvo);
			log.info("구매확정 result : "+result);
			
			if(result == 0) {
				resultData = "FAIL";
			}else {
				resultData = "SUCCESS";
			}
		}else {
			rfvo.setRf_orderstate(ohvo.getPd_orderstate());
			
			result = orderhistoryService.orderstateUpdate(ohvo);
			log.info("주문취소 result : "+result);
			
			if(result != 0) {
				//배송예정에서 바로 주문취소 되어도 refund테이블 추가
				refundInsert = refundService.refundInsert(rfvo); 
				log.info("refundresult : "+refundInsert);

				if(refundInsert == 0) {
					resultData = "FAIL";
				}else {
					resultData = "SUCCESS";
				}
			}else {
				resultData = "FAIL";
			}
		}
		
		return resultData;
	
	}
	

}
