package com.dev24.client.qna.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev24.client.login.vo.LoginVO;
import com.dev24.client.qna.service.QnaService;
import com.dev24.client.qna.vo.QnaVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/qna/*")
@AllArgsConstructor

public class QnaController { //서비스의 참조값을 담을 수 있는 필드를 생성

	private QnaService qnaService; //외부에서 인스턴스 값을 주입
	
	//QNA 글목록 구현하기 (페이징 처리 목록 조회)
	@RequestMapping(value="/qnaList", method=RequestMethod.GET)
	public String qnaList(@ModelAttribute("data") QnaVO qvo, Model model) {
		//@ModelAttribute("data") QnaVO qvo, Model model 이 먼저 만들어져 있어야 호출이 가능하다.
		log.info("qnaList 호출 성공");
		//전체 레코드 조회
		List<QnaVO> qnaList = qnaService.qnaList(qvo);
		model.addAttribute("qnaList",qnaList);
		
		return "qna/qnaList";
	}
	
	
	
	/*****************************************************
	 * Q&A 글쓰기 폼 출력하기 글 목록 구현하기 (페이징 처리 목록 조회)
	 ******************************************************/
	@RequestMapping(value="/qwriteForm")
	public String writeForm(@ModelAttribute("data") QnaVO qvo) {
		log.info("qwriteForm 호출 성공");
		
		return "qna/qwriteForm";
	}
	
	/*****************************************************
	 * Q&A 글쓰기 구현하기
	 ******************************************************/
	@RequestMapping(value="/qnaInsert", method=RequestMethod.POST)
	//@PostMapping("/qnaInsert")
	public String qnaInsert(QnaVO qvo,Model model, @SessionAttribute("login")LoginVO lvo) {
		log.info("qnaInsert 호출 성공");
		
		int result = 0; //호출여부 확인용도
		String url = "";
//		log.info(qvo.getC_num());
		log.info(lvo.getC_id());
		qvo.setC_num(lvo.getC_num());
		result = qnaService.qnaInsert(qvo); //서비스에서 전달받은 결과 값을 저장
		if(result==1) {
			url = "/qna/qnaList";
		} else {
			url = "/qna/qwriteForm";
		}
		
		return "redirect:"+url;
	}
	
	
	
	/*****************************************************
	 * Q&A 글 삭제하기
	 ******************************************************/
	@RequestMapping(value="/qnaDelete")
	//@PostMapping("/qnaInsert")
	public String qnaDelete(QnaVO qvo, RedirectAttributes ras) {
		log.info("qnaDelete 호출 성공");
		
		int result = 0; //호출여부 확인용도
		String url = "";
		
		result = qnaService.qnaDelete(qvo.getQ_num()); 
		ras.addFlashAttribute("qnaVO",qvo); //실질적으로 번호밖에 가지고 있지 않음
		
		if(result==1) {
			url = "/qna/qnaList";
		} else {
			url = "/qna/qnaDetail";
		}
		
		return "redirect:"+url;
	}
	
	
	
	
	/*****************************************************
	 * 글 상세 페이지 구현하기
	 ******************************************************/
	@RequestMapping(value="/qnaDetail", method=RequestMethod.GET)
	//@PostMapping("/qnaDetail")
	public String qnaDetail(@ModelAttribute("data") QnaVO qvo, Model model) {
		log.info("qnaDetail 호출 성공");
		
		qnaService.qnaCount(qvo.getQ_num());
		QnaVO detail = qnaService.qnaDetail(qvo);
		model.addAttribute("detail", detail);
		
		return "qna/qnaDetail";
	}
	
	
	
	/**********************************************************************************
	 * 글 수정 폼 출력하기
	 * @param q_num
	 * @param QnaVO
	 *********************************************************************************/
	@RequestMapping(value="/qupdateForm")
	public String qupdateForm(@ModelAttribute("data") QnaVO qvo, Model model) {
		log.info("qupdateForm 호출 성공");
		log.info("q_num =" + qvo.getQ_num());
		
		QnaVO updateData = qnaService.qupdateForm(qvo);
		
		model.addAttribute("updateData",updateData);
		return "qna/qupdateForm";
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
	public String qnaUpdate(@ModelAttribute QnaVO qvo, RedirectAttributes ras) {
		log.info("qnaUpdate 호출 성공");
		
		int result = 0;
		String url ="";
		
		result = qnaService.qnaUpdate(qvo);
		ras.addFlashAttribute("data",qvo);
		
		if(result==1) {
			//아래 url은 수정 후 상세페이지로 이동
			//url = "/board/boardDeatil?b_num="+bvo.getB_num(); 
			url = "/qna/qnaDetail";
		}else {
			//url = "/board/updateForm?b_num="+bvo.getB_num(); 
			url="/qna/qupdateForm";
		}
		
		return "redirect:"+url;
		
	}
	

	/**********************************************************************************
	 * 답변 글 폼 출력하기
	 * @param q_num
	 * @param QnaVO
	 *********************************************************************************/
	@RequestMapping(value="/qreplyForm", method=RequestMethod.POST)
	public String qreplyForm(@ModelAttribute("data") QnaVO qvo, Model model) {
		log.info("qreplyForm 호출 성공");
		QnaVO qreplyData = qnaService.qnaDetail(qvo);
		model.addAttribute("qreplyData",qreplyData);
		return "qna/qreplyForm";
	}
	
	/**********************************************************************************
	 * 답변 글 작성하기
	 * @param q_num
	 * @param QnaVO
	 *********************************************************************************/
	@RequestMapping(value="/qinsertReply",method=RequestMethod.POST)
	public String qinsertReply(@ModelAttribute("data") QnaVO qvo, Model model) {
		log.info("qinsertReply 호출 성공");
		
		log.info("q_reproot:" + qvo.getQ_repRoot());
		log.info("q_repIndent:" + qvo.getQ_repIndent());
		log.info("q_repStep:" + qvo.getQ_repStep());
		
		
		int result = 0; //호출여부 확인용도
		String url = "";
		
		result = qnaService.replyInsert(qvo); //서비스에서 전달받은 결과 값을 저장
		if(result==1) {
			url = "/qna/qnaList";
		} else {
			url = "/qna/qreplyForm";
		}
		
		return "redirect:"+url;
	}
	
}
