package com.dev24.admin.freeboard.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev24.client.freeboard.service.FreeBoardService;
import com.dev24.client.freeboard.vo.FreeBoardVO;
import com.dev24.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/admin/*")
@AllArgsConstructor
public class AdminFreeBoardController {
	
	private FreeBoardService freeboardService;
	
	@RequestMapping(value="/freeboardAdmin", method=RequestMethod.GET)
	public String freeboardAdmin(@ModelAttribute("data") FreeBoardVO fbvo, Model model) {
		List<FreeBoardVO> adminfbList = freeboardService.freeboardList(fbvo);
		model.addAttribute("adminfbList", adminfbList);
		
		int total = freeboardService.freeboardListCnt(fbvo);
		model.addAttribute("pageMarker" , new PageDTO(fbvo, total));
		
		return"admin/freeboardAdmin";
	}
	
	@RequestMapping(value="/freeboardAdminDetail", method=RequestMethod.GET)
	public String freeboardAdminDetail(@ModelAttribute("data") FreeBoardVO fbvo, Model model) {
		
		FreeBoardVO adminfreeDetail = freeboardService.freeboardDetail(fbvo);
		model.addAttribute("adminfreeDetail", adminfreeDetail);
		
		return "admin/freeboardAdminDetail";
	}
	
}
