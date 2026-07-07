package com.dev24.admin.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.dev24.admin.book.service.AdminBookService;
import com.dev24.client.book.service.BookService;
import com.dev24.client.book.vo.BookVO;
import com.dev24.common.pagination.Pagination;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/admin/book/*")
@Log4j
@AllArgsConstructor
@SessionAttributes({"adm_id", "adm_num"})
public class AdminBookController {

	private BookService bookService;
	
	private AdminBookService adminBookService;

	/***
	 * 관리자 페이지 도서리스트 출력 메서드
	 * @param cateOne_num
	 * @param cateTwo_num
	 * @param page
	 * @param startPage
	 * @param listRange
	 * @param model
	 */
	@RequestMapping(value = "/{category}", method = RequestMethod.GET)
	public String adminBookList(
			@PathVariable String category,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "1") int startPage,
			@RequestParam(required = false, defaultValue = "20") int listRange,
			@RequestParam(required = false, defaultValue = "best") String b_sort,
			@RequestParam(required = false, defaultValue = "all") String b_stateKeyword,
			@RequestParam(required = false, defaultValue = "") String b_searchKeyword,
			@RequestParam(required = false, defaultValue = "") String b_searchSelect,
			Model model
	) {
		
		log.info("bookList 호출 성공");

		int cateOne_num = 0;
		int cateTwo_num = 0;
		
		if (!category.substring(0, 1).isEmpty())
			cateOne_num = Integer.parseInt(category.substring(0, 1));
		if (!category.substring(1).isEmpty())
			cateTwo_num = Integer.parseInt(category.substring(1, 2));

		BookVO bvo = new BookVO();
		bvo.setCateOne_num(cateOne_num);
		bvo.setCateTwo_num(cateTwo_num);
		
		// Pagination 객체 생성
		int bookLength = bookService.getBookListCnt(bvo);
		Pagination pagination = new Pagination(bookLength, startPage, page, cateOne_num, cateTwo_num, listRange, b_sort, b_stateKeyword);
		pagination.setB_searchSelect(b_searchSelect);
		pagination.setB_searchKeyword(b_searchKeyword);

		// 얻어낸 pagination객체를 통해 bookList() 호출
		List<BookVO> bList = bookService.bookList(pagination);
		log.info(pagination.toString());

		model.addAttribute("pagination", pagination);
		model.addAttribute("bList", bList);

		return "admin/adminBookList";

	}
	
	/***
	 * 도서 등록 폼 출력
	 * @return
	 */
	@RequestMapping(value="/bookInsertForm")
	public String bookInsertForm() {
		return "admin/bookInsertForm";
	}
	
	/***
	 * 도서정보 작성 후 등록버튼 클릭시 Insert문 실행
	 * @param bvo
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/bookInsert")
	public ResponseEntity<String> bookInsert(@ModelAttribute("data") BookVO bvo, Model model) throws Exception {
		log.info("bookInsert 호출 성공");
		log.info("bvo" + bvo);
		
		int result = 0;
		String url = "";
		
		result = bookService.bookInsert(bvo);
		ResponseEntity<String> entity;
		
		if(result == 1) {
			url = "/admin/book/detail/00";
			entity = new ResponseEntity<String>("redirect:" + url, HttpStatus.OK);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		return entity;
	}
	
	@PostMapping(value="/updateBookState", produces = "text/plain; charset=utf8")
	public ResponseEntity<String> updateBookState(
									@RequestBody Map<String, Object> data,
									Model model
		) {
		BookVO bvo = new BookVO();
		
		log.info("updateBookState 호출 성공");
//		log.info(data.toString());

//		log.info(dataList.get(0).get("b_stateKeyword") + "");
		log.info(data.get("b_stateKeyword").toString());
		log.info(data.get("bNumList").toString());
		
		String b_stateKeyword = data.get("b_stateKeyword").toString();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Integer> bNumList = (List)data.get("bNumList");
		log.info(bNumList.toString());
		
		bvo.setB_stateKeyword(b_stateKeyword);
		bvo.setBNumList(bNumList);
		
		ResponseEntity<String> entity;
		int result = adminBookService.updateBookState(bvo);
		
		if (result > 0) {
			entity = new ResponseEntity<String>(result+"", HttpStatus.OK);
		} else {
			entity = new ResponseEntity<String>(result+"", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return entity;
	}
	
	@RequestMapping(value="/detail/{b_num}", method = {RequestMethod.GET, RequestMethod.POST})
	public String adminBookDetail(@PathVariable int b_num ,Model model) {
		BookVO vo = bookService.bookDetail(b_num);
		log.info("adminBookDetail 호출 성공");
		model.addAttribute("vo", vo);
		return "admin/adminBookDetail";
	}
	
	@RequestMapping(value="/bookUpdateForm/{b_num}", method = {RequestMethod.GET, RequestMethod.POST})
	public String bookUpdateForm (@PathVariable int b_num, Model model) {
		BookVO vo = bookService.bookDetail(b_num);
		log.info("bookUpdateForm 호출 성공");
		model.addAttribute("vo", vo);
		return "admin/bookUpdateForm";
	}
	
	/***
	 * 도서정보 수정
	 * @param bvo
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/bookUpdate")
	public String bookUpdate(@ModelAttribute("data") BookVO bvo, Model model) throws Exception {
		log.info("bookUpdate 호출 성공");
		log.info("bvo" + bvo);
		
		int result = 0;
		String url = "";
		
		result = bookService.bookUpdate(bvo);
		ResponseEntity<String> entity;
		
		return "/admin/book/detail/00";
	}
	
}
