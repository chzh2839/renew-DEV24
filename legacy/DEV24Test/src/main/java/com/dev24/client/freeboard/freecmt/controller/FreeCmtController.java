package com.dev24.client.freeboard.freecmt.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.dev24.client.freeboard.freecmt.service.FreeCmtService;
import com.dev24.client.freeboard.freecmt.vo.FreeCmtVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping(value="/freecmt")
@AllArgsConstructor
@SessionAttributes({ "c_id", "c_num", "c_nickname" })
public class FreeCmtController {
	
	private FreeCmtService freecmtService;
	
	@GetMapping(value="/all/{fb_num}", produces= {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<List<FreeCmtVO>> freeCmtList(@PathVariable("fb_num") Integer fb_num, Model model){
		log.info("freeCmtList 호출 성공");
		ResponseEntity<List<FreeCmtVO>> entity = null;
		//List<FreeCmtVO> freecmtList = freecmtService.freeCmtList(fb_num);
		//model.addAttribute("freecmtList", freecmtList);
		entity = new ResponseEntity<>(freecmtService.freeCmtList(fb_num), HttpStatus.OK);
		return entity;
	}
	
	/*@RequestMapping(value="/all/{fb_num}", method=RequestMethod.GET)
	public String freeboardList(@ModelAttribute("data") FreeCmtVO fbcvo, Model model) {
		
		return "";
	}*/
	
	@PostMapping(value="/freecmtInsert", consumes="application/json", produces= {MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> freeCmtInsert(@RequestBody FreeCmtVO fcmtvo, Model model){
		log.info("freecmtInsert 호출 성공!");
		log.info("FreeCmtVO"+fcmtvo);
		int result =0;
		
		/*model.addAttribute("c_id", "javakhang");
		model.addAttribute("c_num", 2);
		model.addAttribute("c_nickname", "javaman");*/
		
		result = freecmtService.freeCmtInsert(fcmtvo);
		return result==1? new ResponseEntity<String>("SUCCESS", HttpStatus.OK):
						new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
	
	}
}
