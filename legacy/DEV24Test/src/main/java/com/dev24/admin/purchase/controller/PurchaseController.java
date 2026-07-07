package com.dev24.admin.purchase.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dev24.admin.purchase.service.PurchaseService;
import com.dev24.admin.purchase.vo.AdminPurchaseViewVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller("admin.purchaseController")
@AllArgsConstructor
@RequestMapping("/admin/*")
@Log4j
public class PurchaseController {
	private PurchaseService purchaseService;

	@GetMapping(value="/purchaseList")
	public String purchaseList(@ModelAttribute("data") AdminPurchaseViewVO pvo, Model model){
		log.info("purchaseList 메서드 호출 성공");
		List<AdminPurchaseViewVO> list = purchaseService.purchaseList(pvo);
		model.addAttribute("list", list);
		return "admin/purchaseList";
	}
	
	

}
