package com.dev24.client.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.dev24.client.customer.service.CustomerService;
import com.dev24.client.customer.vo.CustomerVO;
import com.dev24.client.login.service.LoginService;
import com.dev24.client.login.vo.LoginVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping(value="/customer/*")
@Log4j
@AllArgsConstructor
public class CustomerController {
	
	private CustomerService customerService;
	
	private LoginService loginService;
	
	
	/**************************************************************
	 * 회원 가입 폼
	 **************************************************************/
	@RequestMapping(value="/join", method = RequestMethod.GET)
	public String joinForm(Model model) {
		log.info("join get 방식에 의한 메서드 호출 성공");
		return "customer/join";
	}
	
	/*************************************************
	 * 사용자 아이디 중복 체크 메서드
	 *************************************************/ 
	@ResponseBody
	@RequestMapping(value="/userIdConfirm", method=RequestMethod.POST)
	public String userIdConfirm(@RequestParam("c_id") String c_id){
		int result = customerService.userIdConfirm(c_id);
		return String.valueOf(result);
	}
	
	/*************************************************
	 * 사용자 별명 중복 체크 메서드
	 *************************************************/ 
	@ResponseBody
	@RequestMapping(value="/userNickConfirm", method=RequestMethod.POST)
	public String userNickConfirm(@RequestParam("c_nickname") String c_nickname){
		int result = customerService.userNickConfirm(c_nickname);
		return String.valueOf(result);
	}
	
	
	/*************************************************
	 * 사용자 이메일 중복 체크 메서드
	 *************************************************/ 
	@ResponseBody
	@RequestMapping(value="/userEmailConfirm", method=RequestMethod.POST)
	public String userEmailConfirm(@RequestParam("c_email") String c_email){
		int result = customerService.userEmailConfirm(c_email);
		return String.valueOf(result);
	}
	
	
	
	
	
	
	/**************************************************************
	 * 회원 가입 처리(AOP 예외 처리 전)
	 **************************************************************/
	@RequestMapping(value="/join", method = RequestMethod.POST)
	public ModelAndView customerInsert(@ModelAttribute CustomerVO cvo, @RequestParam String[] cInterest, @RequestParam String[] cNletter) {
		log.info("join post 방식에 의한 메서드 호출 성공");
		ModelAndView mav = new ModelAndView();
		//log.info(cvo.toString());
			
		int result = 0;
		String c_interest ="";
		String c_nletter  ="";
		
		for (String s : cInterest )
			c_interest += s + " ";
		for (String s : cNletter )
			c_nletter += s + " ";
		
		cvo.setC_interest(c_interest);
		cvo.setC_nletter(c_nletter);
		
		result = customerService.customerInsert(cvo);
		
		
		switch(result) {
			case 1:
				mav.addObject("codeNumber", 1); // userId already exist 
				mav.setViewName("customer/join");
				break;
			case 3:
				mav.addObject("codeNumber", 3);
				mav.setViewName("customer/join_success"); // success to add new member; move to login page
				break;
			default: 
				mav.addObject("codeNumber", 2); // failed to add new member
				mav.setViewName("customer/join");
				break;
		}
		return mav;
	} 
	
	
	/**************************************************************
	 * 회원 수정 폼
	 * @SessionAttribute: 메소드에 @SessionAttribute가 있을 경우 파라미터로 지정된 이름으로 등록된 세션 정보를 읽어와서 변수에 할당한다.
	 **************************************************************/
	/*@RequestMapping(value="/modify", method = RequestMethod.GET)	
	public ModelAndView customerModify(@SessionAttribute("login") LoginVO login){
		log.info("modify get 방식에 의한 메서드 호출 성공");
		ModelAndView mav=new ModelAndView();

		if(login==null){
			mav.setViewName("customer/login");	
			return mav;
		}
		
		CustomerVO vo = customerService.customerSelect(login.getC_id());             
		mav.addObject("customer", vo);
		mav.setViewName("/mypage/modify");	
		return mav;
	} */

	/**************************************************************
	 * 회원 수정 처리(AOP 예외 처리 전)
	 **************************************************************/
	@RequestMapping(value="/modify", method = RequestMethod.POST)	
	public ModelAndView customerModifyProcess(CustomerVO cvo, @SessionAttribute("login") LoginVO login, ModelAndView mav){
		log.info("modify post 방식에 의한 메서드 호출 성공");

		if(login==null){
			mav.setViewName("customer/login");	
			return mav;
		}
		
		cvo.setC_id(login.getC_id());    
		CustomerVO vo = customerService.customerSelect(cvo.getC_id());           

		if (loginService.loginSelect(cvo.getC_id(), cvo.getOldUserPw()) == null ) {
			mav.addObject("codeNumber", 1);
			mav.addObject("customer",vo);
			mav.setViewName("customer/modify");
			return mav;
		} 

		customerService.customerUpdate(cvo);
		mav.setViewName("redirect:/customer/logout");
		return mav;	
		
	}

	/**************************************************************
	 * 회원 탈퇴 처리(AOP 예외 처리 전)
	 **************************************************************/
	@RequestMapping("/delete")	
	public ModelAndView customerDelete(@SessionAttribute("login") LoginVO login){
		log.info("delete.do get방식에 의한 메서드 호출 성공");
		
		ModelAndView mav=new ModelAndView();
		
		if(login==null){
			mav.setViewName("customer/login");	
			return mav;
		}
		
		int errCode = customerService.customerDelete(login.getC_id());
		switch(errCode) {
		case 2:
			mav.setViewName("redirect:/customer/logout");
			break;
		case 3:
			mav.addObject("codeNumber", 3);
			mav.setViewName("customer/login");
			break;
		}
	    return mav;	
	}
	
	
	
	
	
}
