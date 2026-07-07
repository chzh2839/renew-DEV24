package com.dev24.admin.review.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev24.admin.review.service.ReviewService;
import com.dev24.admin.review.vo.AdminReviewViewVO;
import com.dev24.client.review.vo.ReviewVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller("admin.reviewController")
@AllArgsConstructor
@RequestMapping("/admin/*")
@Log4j
public class ReviewController {
	private ReviewService reviewService;
	
	/**************************************************
	 * review list print on admin page
	 * *******/
	@GetMapping(value="/reviewList")
	public String reviewList(@ModelAttribute("data") AdminReviewViewVO revo, Model model){
		log.info("reviewList 메서드 호출 성공");
		List<AdminReviewViewVO> list = reviewService.reviewList(revo);
		model.addAttribute("list", list);
		return "admin/reviewList";
	}
	
	
	
	/**************************************************
	 * review detail print on admin page
	 * *******/
	@GetMapping(value="/reviewDetail")
	public String reviewDetail(@ModelAttribute("data") ReviewVO revo, Model model){
		log.info("reviewDetail 메서드 호출 성공");
		ReviewVO list = reviewService.reviewDetail(revo.getRe_num());
		ReviewVO bookInfo = reviewService.getBookInfo(revo.getB_num());
		model.addAttribute("bookInfo", bookInfo);
		model.addAttribute("list", list);
		return "admin/reviewDetail";
	}
	
	
	/************************************************
	 * delete review by admin
	 * ****************/
	@PostMapping(value="/reviewDelete")
	public String reviewDelete(@ModelAttribute("data") ReviewVO revo, RedirectAttributes ras) throws Exception{
		log.info("reviewDelete 호출 성공");
		
		int result = 0;
		String path = "";
		result = reviewService.reviewDelete(revo);
		log.info("result : "+result);
		
		if(result == 0) {
			ras.addFlashAttribute("failMsg", "삭제 실패. 잠시 후 다시 시도해주세요.");
			path = "/admin/reviewDetail?re_num="+revo.getRe_num()+"&b_num="+revo.getB_num();
		}else {
			ras.addFlashAttribute("successMsg", "리뷰 삭제가 완료되었습니다.");
			path = "/admin/reviewList";
		}
		
		return "redirect:"+path;
	}
	
	
	
}
