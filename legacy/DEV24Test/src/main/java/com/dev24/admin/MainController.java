package com.dev24.admin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev24.client.book.service.BookService;
import com.dev24.client.book.vo.BookVO;
import com.dev24.common.pagination.Pagination;

import lombok.Setter;

@Controller
public class MainController {
	final static Logger logger = LoggerFactory.getLogger(MainController.class);
	
	@Setter(onMethod_ = @Autowired)
	private BookService bookService;
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String index(Model model) {
		
		Pagination pagination = new Pagination(0, 0, 1, 1, 1, 18, "best", "reg");
		List<BookVO> bvoList = bookService.bookList(pagination);

		model.addAttribute("bvoList", bvoList);
		return "index";
	}
	
	
}
