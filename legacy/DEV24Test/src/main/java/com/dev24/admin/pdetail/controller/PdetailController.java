package com.dev24.admin.pdetail.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev24.admin.pdetail.service.PdetailService;
import com.dev24.admin.pdetail.vo.AdminPdetailViewVO;
import com.dev24.client.mypage.orderhistory.service.OrderhistoryService;
import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller("admin.pdetailController")
@AllArgsConstructor
@RequestMapping("/admin/*")
@Log4j
public class PdetailController {
	private PdetailService pdetailService;
	private OrderhistoryService orderhistoryService;

	/** 구매 상세 페이지 출력*/
	@RequestMapping(value="/admin/pdetailList", method = {RequestMethod.GET, RequestMethod.POST})
	public String pdetailList(@ModelAttribute("data") AdminPdetailViewVO pdvo, Model model, HttpSession session) {
		log.info("pdetailList 호출 성공");
		int p_num = pdvo.getP_num();
		log.info("p_num : "+p_num);
		List<AdminPdetailViewVO> plist = pdetailService.pdetailList(pdvo);
		model.addAttribute("plist", plist);
		session.setAttribute("p_num", p_num);
		
		log.info(plist);
		
		return "admin/pdetailList";
	}
	
	
	
	/** mypage - orderstate update
	 * @param String
	 * @ResponseBody */
	@ResponseBody
	@RequestMapping(value="/orderstateUpdate", method= {RequestMethod.GET})
	public String orderstateUpdate(@ModelAttribute("data") OrderhistoryVO ohvo) {
		log.info("orderstateUpdate - admin 호출 성공");
		
		String resultData = "";
		int result = 0;
		result = orderhistoryService.orderstateUpdate(ohvo);
		if(result == 0) {
			resultData = "FAIL";
		}else {
			resultData = "SUCCESS";
		}
		
		return resultData;
	}
	
	
}
