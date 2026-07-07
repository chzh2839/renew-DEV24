package com.dev24.admin.necmt.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev24.client.necmt.dao.NecmtDAO;
import com.dev24.client.necmt.service.NecmtService;
import com.dev24.client.necmt.vo.NecmtVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/admin/*")
@Log4j
@AllArgsConstructor
public class AdminNecmtController {
	
	private NecmtService necmtService;
	
	@RequestMapping(value = "/necmt/{ne_num}", produces = MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE)
	public ResponseEntity<List<NecmtVO>> necmtList(@PathVariable("ne_num") Integer ne_num, Model model) {
		log.info("necmtList 호출 완료");

		List<NecmtVO> necmtList = necmtService.necmtList(ne_num);
		for (NecmtVO vo : necmtList)
			log.info(vo.toString());
		ResponseEntity<List<NecmtVO>> entity = new ResponseEntity<List<NecmtVO>>(necmtList, HttpStatus.OK);

		return entity;
	}
	

	@RequestMapping(value = "/necmt/replyCheck", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.POST)
	public ResponseEntity<String> replyCheck (@RequestParam int ne_num, Model model) {
		log.info("replyCheck 호출 완료");
		ResponseEntity<String> entity;
		
//		log.info("ne_num: " + nevo.getNe_num());
		log.info(ne_num);
		int result = necmtService.replyCheck(ne_num);
		log.info("result: " + result);
		
		if(result > -1) {
			entity = new ResponseEntity<String>(result+"", HttpStatus.OK);
		}else {
			entity = new ResponseEntity<String>(result+"", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return entity;
	}
	
	@RequestMapping(value="/necmt/replyDelete", produces = MediaType.TEXT_PLAIN_VALUE, method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> replyDelete (@RequestParam int necmt_num, Model model){
		log.info("replyDelete 호출 완료");
		log.info("necmt_num: " + necmt_num);
		
		ResponseEntity<String> entity;
		
		int result = necmtService.necmtDeleteNecmtNum(necmt_num);

		log.info("result: "+result);
		if (result > 0) {
			entity = new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
		} else {
			entity = new ResponseEntity<String>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return entity;
	}
}
