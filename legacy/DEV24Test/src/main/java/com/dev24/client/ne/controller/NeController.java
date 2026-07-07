package com.dev24.client.ne.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev24.client.ne.service.NeService;
import com.dev24.client.ne.vo.NeVO;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("ne/*")
@Log4j
@AllArgsConstructor
public class NeController {

	@Setter(onMethod_ = @Autowired)
	private NeService neService;
	
	@RequestMapping("/neList")
	public String neList (Model model) {
		log.info("neList 호출 완료");
		
		List<NeVO> neList = neService.neList();
		
		model.addAttribute("neList", neList);
		
		return "ne/neList";
	}
	
	@RequestMapping("/neDetail")
	public String neDetail(int ne_num, Model model) {
		log.info("neDetail 호출 완료");
		
		NeVO nvo = neService.neDetail(ne_num);
		log.info(nvo);
		model.addAttribute("nvo", nvo);
		
		return "ne/neDetail";
	}

}
