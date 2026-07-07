package com.dev24.client.mypage.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.dev24.client.customer.service.CustomerService;
import com.dev24.client.customer.vo.CustomerVO;
import com.dev24.client.login.vo.LoginVO;
import com.dev24.client.mypage.orderhistory.service.OrderhistoryService;
import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;
import com.dev24.client.mypage.refundhistory.service.RefundhistoryService;
import com.dev24.client.mypage.refundhistory.vo.RefundhistoryVO;
import com.dev24.client.qna.service.QnaService;
import com.dev24.client.qna.vo.QnaVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/mypage/*")
@Log4j
@AllArgsConstructor
public class MypageController {
	
	private OrderhistoryService orderhistoryService;
	private RefundhistoryService refundhistoryService;
	private CustomerService customerService;
	private QnaService qnaService;
	
	/************************************************
	 *  mypage main print
	 *  ****************/
	@RequestMapping(value="/mypage", method= {RequestMethod.GET})
	public String mypage(OrderhistoryVO ohvo, RefundhistoryVO rfhvo, QnaVO qvo, Model model, @SessionAttribute("login") LoginVO lvo) {
		log.info("mypage 호출 성공");
			
		if(lvo == null){
			return "redirect:/customer/login";
		}
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		ohvo.setC_num(c_num);
		rfhvo.setC_num(c_num);
		qvo.setC_num(c_num);
		
		// 주문내역 조회
		List<OrderhistoryVO> ohlist = orderhistoryService.orderhistoryList(ohvo);
		model.addAttribute("ohvo", ohlist);
		
		// 환불내역 조회
		List<RefundhistoryVO> rfhlist = refundhistoryService.refundhistoryList(rfhvo);
		model.addAttribute("rfhvo", rfhlist);
		
		// 나의 문의 조회
		List<QnaVO> qnalist = qnaService.myQnaList(qvo);
		model.addAttribute("qvo", qnalist);
		
		return "mypage/mypage";
	}

	
	/**************************************************************
	 * 회원 수정 폼
	 * @SessionAttribute: 메소드에 @SessionAttribute가 있을 경우 파라미터로 지정된 이름으로 등록된 세션 정보를 읽어와서 변수에 할당한다.
	 **************************************************************/
	@RequestMapping(value="/modify", method = RequestMethod.GET)	
	public ModelAndView customerModify(@SessionAttribute("login") LoginVO login){
		log.info("modify get 방식에 의한 메서드 호출 성공");
		ModelAndView mav=new ModelAndView();

		if(login==null){
			mav.setViewName("customer/login");	
			return mav;
		}
		
		CustomerVO vo = customerService.customerSelect(login.getC_id());             
		mav.addObject("customer", vo);
		mav.setViewName("mypage/modify");	
		return mav;
	} 
	
	
	/****************************************************
	 * my qna history page print
	 * *******************/
	@RequestMapping(value="/qnaHistory", method= {RequestMethod.GET})
	public String qnaHistory(@ModelAttribute("data") QnaVO qvo, Model model, HttpSession session) {
		log.info("qnaHistory 호출 성공");
		
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		qvo.setC_num(c_num);
		
		List<QnaVO> qnalist = qnaService.myQnaList(qvo);
		model.addAttribute("qvo", qnalist);
		
		return "mypage/qnaHistory";
	}

}
