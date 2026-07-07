package com.dev24.admin.ne.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev24.client.ne.service.NeService;
import com.dev24.client.ne.vo.NeVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/admin/*")
@Log4j
@AllArgsConstructor
public class AdminNeController {
		
	private NeService neService;

	@RequestMapping(value="/neList", method=RequestMethod.GET)
	public String adminNeList(Model model){
		log.info("adminNeList ȣ�� �Ϸ�");
		List<NeVO> neList = neService.neList();
		
		model.addAttribute("neList", neList);
		
		return "admin/adminNeList";
	}
	
	@RequestMapping(value="/neDetail")
	public String neDetail (@RequestParam("ne_num") Integer ne_num, Model model) {
		log.info("neDetail ȣ�� �Ϸ�");
		
		NeVO nvo = neService.neDetail(ne_num);
		
		model.addAttribute("nvo", nvo);
		
		return "admin/adminNeDetail";
	}

	@RequestMapping(value="/neDelete", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.POST)
	public ResponseEntity<String> neDelete(
			@RequestParam int ne_num, 
			@RequestParam int replyCnt, 
			Model model
	) throws Exception 
	{
		
		log.info("neDelete ȣ�� �Ϸ�");
		
		ResponseEntity<String> entity;
		
		int result = neService.neDelete(ne_num, replyCnt);
		
		if(result > 0) {
			entity = new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
		} else {
			entity = new ResponseEntity<String>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return entity;
	}
	
	
	@RequestMapping("/neInsertForm")
	public String neInsertForm() {
		log.info("neInsertForm ȣ�� ����");
		return "admin/adminNeInsertForm";
	}
	
	@RequestMapping("/neInsert")
	public String neInsert(@ModelAttribute("data") NeVO nevo, Model model) throws Exception{
		log.info("neInsert ȣ�� ����");
		
		int result = neService.neInsert(nevo);
		log.info("result: " + result);
		
		return "redirect:/admin/neList";
	}
	
}
