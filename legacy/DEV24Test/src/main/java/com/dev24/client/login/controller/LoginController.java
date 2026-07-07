package com.dev24.client.login.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import com.dev24.client.book.service.BookService;
import com.dev24.client.book.vo.BookVO;
import com.dev24.client.login.service.LoginService;
import com.dev24.client.login.vo.LoginVO;
import com.dev24.common.pagination.Pagination;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
/* @SessionAttributes 파라미터로 지정된 이름과 같은 이름이 @ModelAttribute에 지정되어 있을 경우 메소드가 반환되는 값은 세션에 저장된다. */
@SessionAttributes("login")
@RequestMapping("/customer/*")
@Log4j
@AllArgsConstructor
public class LoginController {

	private LoginService loginService;
	private BookService bookService;
	/* @SessionAttributes의 파라미터와 같은 이름이 @ModelAttribute에 있을 경우 세션에 있는 객체를 가져온 후, 클라이언트로 전송받은 값을 설정한다. */
	@ModelAttribute("login")
	public LoginVO login() {
		return new LoginVO();
	}
	
	/**************************************************************
	 * 로그인 폼 처리
	 **************************************************************/
	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String loginForm() {
		log.info("login.do get 호출 성공");
		return "customer/login";
	}
	
	/**************************************************************
	 * 로그인 처리
	 * 참고 : 로그인 실패시 횟수 제한을 제어하지 않은 처리
	 **************************************************************/
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ModelAndView loginInsert(@ModelAttribute LoginVO lvo, ModelAndView mav) {
		log.info("login.do post 호출 성공");

		String c_id = lvo.getC_id();
		String c_passwd = lvo.getC_passwd();
		LoginVO loginCheckResult = loginService.loginSelect(c_id, c_passwd);
		
		Pagination pagination = new Pagination(0, 0, 1, 1, 1, 18, "best", "reg");
		List<BookVO> bvoList = bookService.bookList(pagination);

		// 입력받은 아이디와 비밀번호로 DB 확인 시 일치 데이터가 존재하지 않으면
		if(loginCheckResult == null){
			mav.addObject("codeNumber", 1);	
			mav.setViewName("customer/login");			
			return mav; 
		}else { // 일치하면
			mav.addObject("login", loginCheckResult);
			mav.addObject("bvoList", bvoList);
			mav.setViewName("/index");
			return mav;
		}  
	} 
	
	
	/**************************************************************
	 * 로그아웃 처리 메서드
	 **************************************************************/
	@RequestMapping("/logout")
	public String logout(SessionStatus sessionStatus){
		sessionStatus.setComplete();
		return "redirect:/";
	}
	
	
	
	
}
