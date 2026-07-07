package com.dev24.admin.faq.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev24.admin.faq.service.AdminFaqSerivce;
import com.dev24.client.faq.vo.FaqVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/admin/*")
@AllArgsConstructor
public class AdminFaqController {
	
	private AdminFaqSerivce adminFaqService;
	
	
	
	//faq 글목록 구현하기 (페이징 처리 목록 조회)
			@RequestMapping(value="/faqList", method=RequestMethod.GET)
			public String faqList(@ModelAttribute("data") FaqVO fvo, Model model) {
				log.info("faqList 호출 성공");
				//전체 레코드 조회
				List<FaqVO> faqList = adminFaqService.faqList(fvo);
				model.addAttribute("faqList",faqList);
				
				return "admin/faqList";
			}
			
			
			
			/*****************************************************
			 * Q&A 글쓰기 폼 출력하기 글 목록 구현하기 (페이징 처리 목록 조회)
			 ******************************************************/
			@RequestMapping(value="/faqInsertForm")
			public String faqInsertForm(@ModelAttribute("data") FaqVO fvo) {
				log.info("faqInsertForm 호출 성공");
				
				return "admin/faqInsertForm";
			}
			
			/*****************************************************
			 * Q&A 글쓰기 구현하기
			 ******************************************************/
			@RequestMapping(value="/faqInsert", method=RequestMethod.POST)
			//@PostMapping("/faqInsert")
			public String faqInsert(FaqVO fvo,Model model) {
				log.info("faqInsert 호출 성공");
				
				int result = 0; //호출여부 확인용도
				String url = "";
				
				result = adminFaqService.faqInsert(fvo); //서비스에서 전달받은 결과 값을 저장
				if(result==1) {
					url = "/admin/faqList";
				} else {
					url = "/admin/faqInsertForm";
				}
				
				return "redirect:"+url;
			}
			
			
			
			/*****************************************************
			 * Q&A 글 삭제하기
			 ******************************************************/
			@RequestMapping(value="/faqDelete")
			//@PostMapping("/faqInsert")
			public String faqDelete(FaqVO fvo, RedirectAttributes ras) {
				log.info("faqDelete 호출 성공");
				
				int result = 0; //호출여부 확인용도
				String url = "";
				
				result = adminFaqService.faqDelete(fvo.getFaq_num()); 
				ras.addFlashAttribute("faqVO",fvo); //실질적으로 번호밖에 가지고 있지 않음
				
				if(result==1) {
					url = "/admin/faqList";
				} else {
					url = "/admin/faqDetail";
				}
				
				return "redirect:"+url;
			}
			
			
			
			
			/*****************************************************
			 * 글 상세 페이지 구현하기
			 ******************************************************/
			@RequestMapping(value="/faqDetail", method=RequestMethod.GET)
			//@PostMapping("/faqDetail")
			public String faqDetail(@ModelAttribute("data") FaqVO fvo, Model model) {
				log.info("faqDetail 호출 성공");
				
				adminFaqService.faqCount(fvo.getFaq_num());
				FaqVO detail = adminFaqService.faqDetail(fvo);
				model.addAttribute("detail", detail);
				
				return "admin/faqDetail";
			}
			
			
			
			/**********************************************************************************
			 * 글 수정 폼 출력하기
			 * @param q_num
			 * @param faqVO
			 *********************************************************************************/
			@RequestMapping(value="/faqUpdateForm")
			public String faqUpdateForm(@ModelAttribute("data") FaqVO fvo, Model model) {
				log.info("faqUpdateForm 호출 성공");
				
				FaqVO updateData = adminFaqService.faqupdateForm(fvo);
				
				model.addAttribute("updateData",updateData);
				return "admin/faqUpdateForm";
			}
			
			/**********************************************************************************
			 * 글 수정 구현하기
			 * @param faqVO
			 * 참고: RedirectAttributes 객체는 리다이렉트 시점(return "redirect:/경로")에
			 * 한번만 사용되는 데이터를 전송할 수 있는 addFlashAttribute()라는 기능을 지원한다.
			 * addFlashAttribute() 메서드는 브라우저까지 전송되기는 하지만,
			 * URI상에는 보이지 않는 숨겨진 데이터의 형태로 전달된다.
			 *********************************************************************************/
			@RequestMapping(value="/faqUpdate", method=RequestMethod.POST)
			public String faqUpdate(@ModelAttribute FaqVO fvo, RedirectAttributes ras) {
				log.info("faqUpdate 호출 성공");
				
				int result = 0;
				String url ="";
				
				result = adminFaqService.faqUpdate(fvo);
				ras.addFlashAttribute("data",fvo);
				
				if(result==1) {
					//아래 url은 수정 후 상세페이지로 이동
					//url = "/board/boardDeatil?b_num="+bvo.getB_num(); 
					url = "/admin/faqDetail";
				}else {
					//url = "/board/updateForm?b_num="+bvo.getB_num(); 
					url="/admin/faqUpdateForm";
				}
				
				return "redirect:"+url;
				
			}
	
	

	
}
