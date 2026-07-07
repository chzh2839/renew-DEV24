package com.dev24.admin.qna.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev24.admin.qna.service.AdminQnaService;
import com.dev24.client.qna.vo.QnaVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/admin/*")
@AllArgsConstructor

public class AdminQnaController {

	private AdminQnaService adminQnaService;
	
	//QNA 글목록 구현하기 (페이징 처리 목록 조회)
		@RequestMapping(value="/qnaList", method=RequestMethod.GET)
		public String adminQnaList(@ModelAttribute("data") QnaVO qvo, Model model) {
			//@ModelAttribute("data") QnaVO qvo, Model model 이 먼저 만들어져 있어야 호출이 가능하다.
			//전체 레코드 조회
			List<QnaVO> qnaList = adminQnaService.qnaList(qvo);
			model.addAttribute("qnaList",qnaList);
			
			return "admin/qnaList";
		}
		
		
		
		/*****************************************************
		 * Q&A 글쓰기 폼 출력하기 글 목록 구현하기 (페이징 처리 목록 조회)
		 ******************************************************/
		@RequestMapping(value="/qwriteForm")
		public String adminWriteForm(@ModelAttribute("data") QnaVO qvo) {
			
			return "admin/qwriteForm";
		}
		
		/*****************************************************
		 * Q&A 글쓰기 구현하기
		 ******************************************************/
		@RequestMapping(value="/qnaInsert", method=RequestMethod.POST)
		//@PostMapping("/qnaInsert")
		public String adminQnaInsert(QnaVO qvo,Model model) {
			
			int result = 0; //호출여부 확인용도
			String url = "";
			
			result = adminQnaService.qnaInsert(qvo); //서비스에서 전달받은 결과 값을 저장
			if(result==1) {
				url = "/admin/qnaList";
			} else {
				url = "/admin/qwriteForm";
			}
			
			return "redirect:"+url;
		}
		
		
		
		/*****************************************************
		 * Q&A 글 삭제하기
		 ******************************************************/
		@RequestMapping(value="/qnaDelete")
		//@PostMapping("/qnaInsert")
		public String adminQnaDelete(QnaVO qvo, RedirectAttributes ras) {
			
			int result = 0; //호출여부 확인용도
			String url = "";
			
			result = adminQnaService.qnaDelete(qvo.getQ_num()); 
			ras.addFlashAttribute("qnaVO",qvo); //실질적으로 번호밖에 가지고 있지 않음
			
			if(result==1) {
				url = "/admin/qnaList";
			} else {
				url = "/admin/qnaDetail";
			}
			
			return "redirect:"+url;
		}
		
		
		
		
		/*****************************************************
		 * 글 상세 페이지 구현하기
		 ******************************************************/
		@RequestMapping(value="/qnaDetail", method=RequestMethod.GET)
		//@PostMapping("/qnaDetail")
		public String adminQnaDetail(@ModelAttribute("data") QnaVO qvo, Model model) {
			
			adminQnaService.qnaCount(qvo.getQ_num());
			QnaVO detail = adminQnaService.qnaDetail(qvo);
			model.addAttribute("detail", detail);
			
			return "admin/qnaDetail";
		}
		
		
		
		/**********************************************************************************
		 * 글 수정 폼 출력하기
		 * @param q_num
		 * @param QnaVO
		 *********************************************************************************/
		@RequestMapping(value="/qupdateForm")
		public String adminQupdateForm(@ModelAttribute("data") QnaVO qvo, Model model) {
			log.info("q_num =" + qvo.getQ_num());
			
			QnaVO updateData = adminQnaService.qupdateForm(qvo);
			
			model.addAttribute("updateData",updateData);
			return "admin/qupdateForm";
		}
		
		/**********************************************************************************
		 * 글 수정 구현하기
		 * @param QnaVO
		 * 참고: RedirectAttributes 객체는 리다이렉트 시점(return "redirect:/경로")에
		 * 한번만 사용되는 데이터를 전송할 수 있는 addFlashAttribute()라는 기능을 지원한다.
		 * addFlashAttribute() 메서드는 브라우저까지 전송되기는 하지만,
		 * URI상에는 보이지 않는 숨겨진 데이터의 형태로 전달된다.
		 *********************************************************************************/
		@RequestMapping(value="/qnaUpdate", method=RequestMethod.POST)
		public String adminQnaUpdate(@ModelAttribute QnaVO qvo, RedirectAttributes ras) {
			
			int result = 0;
			String url ="";
			
			result = adminQnaService.qnaUpdate(qvo);
			ras.addFlashAttribute("data",qvo);
			
			if(result==1) {
				//아래 url은 수정 후 상세페이지로 이동
				//url = "/board/boardDeatil?b_num="+bvo.getB_num(); 
				url = "/admin/qnaDetail";
			}else {
				//url = "/board/updateForm?b_num="+bvo.getB_num(); 
				url="/admin/qupdateForm";
			}
			
			return "redirect:"+url;
			
		}
		

		/**********************************************************************************
		 * 답변 글 폼 출력하기
		 * @param q_num
		 * @param QnaVO
		 *********************************************************************************/
		@RequestMapping(value="/qreplyForm", method=RequestMethod.POST)
		public String adminQreplyForm(@ModelAttribute("data") QnaVO qvo, Model model) {
			QnaVO qreplyData = adminQnaService.qnaDetail(qvo);
			model.addAttribute("qreplyData",qreplyData);
			return "admin/qreplyForm";
		}
		
		/**********************************************************************************
		 * 답변 글 작성하기
		 * @param q_num
		 * @param QnaVO
		 *********************************************************************************/
		@RequestMapping(value="/qinsertReply",method=RequestMethod.POST)
		public String adminQinsertReply(@ModelAttribute("data") QnaVO qvo, Model model) {
			
			
			int result = 0; //호출여부 확인용도
			String url = "";
			
			result = adminQnaService.replyInsert(qvo); //서비스에서 전달받은 결과 값을 저장
			if(result==1) {
				url = "/admin/qnaList";
			} else {
				url = "/admin/qreplyForm";
			}
			
			return "redirect:"+url;
		}
	
	
	
}
