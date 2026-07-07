package com.dev24.client.mypage.refundhistory.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev24.client.login.vo.LoginVO;
import com.dev24.client.mypage.refundhistory.service.RefundhistoryService;
import com.dev24.client.mypage.refundhistory.vo.RefundhistoryVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/mypage/*")
@Log4j
@AllArgsConstructor
public class RefundhistoryController {
	
	private RefundhistoryService refundhistoryService;

	/** refund history print - mypage */
	@RequestMapping(value="/refundHistory", method= {RequestMethod.GET})
	public String refundHistory(@ModelAttribute("data") RefundhistoryVO rfhvo, Model model, HttpSession session) {
		log.info("refundHistory 호출 성공");
		
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		rfhvo.setC_num(c_num);
		
		List<RefundhistoryVO> list = refundhistoryService.refundhistoryList(rfhvo);
		model.addAttribute("rfhvo", list);
		
		return "mypage/refundHistory";
	}
}
