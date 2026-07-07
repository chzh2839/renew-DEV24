package com.dev24.admin.stock.controller;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev24.admin.stock.service.StockService;
import com.dev24.admin.stock.vo.StockDetailVO;
import com.dev24.admin.stock.vo.StockVO;
import com.dev24.client.book.vo.BookVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/admin/*")
@AllArgsConstructor
public class StockController {
	
	private StockService stockService;
	
	@RequestMapping(value="/stockList", method=RequestMethod.GET)
	public String stockList(@ModelAttribute("data") StockVO svo, BookVO bvo, Model model) {
		log.info("stockList ï¿½ìƒ‡?•°ï¿? ï¿½ê½¦?¨ï¿½!");
		List<StockVO> stockList = stockService.stockList(svo);
		List<BookVO> bookstockList = stockService.stockBInfoList(bvo);
		model.addAttribute("stockList", stockList);
		model.addAttribute("bookstockList", bookstockList);
		return "admin/stockList";
	}
	
	@RequestMapping(value="/stockDetail", method=RequestMethod.GET)
	public String getStockDetail(@ModelAttribute("data") StockDetailVO sdvo, Model model) {
		log.info("stockDetail ï¿½ìƒ‡?•°ï¿? ï¿½ê½¦?¨ï¿½!");
		
		StockDetailVO stockDetail = stockService.getStockDetail(sdvo);
		model.addAttribute("stockDetail", stockDetail);
		
		return "admin/stockDetail";
	}
	
	@RequestMapping(value="/stockInsertForm", method = RequestMethod.GET)
	public String stockInsertForm( @ModelAttribute("data") BookVO bvo, Model model) {
		log.info("stockInsertForm ?˜¸ì¶? ?„±ê³?");
		
		log.info(bvo.getB_state());
		
		StockVO svo = new StockVO();
		
	List<StockVO> stockList = stockService.stockList(svo);
		List<BookVO> bookstockList = stockService.stockBInfoList(bvo);
		model.addAttribute("stockList", stockList);
		model.addAttribute("bookstockList", bookstockList);

		
		return "admin/stockInsertForm";
	}
	
	@RequestMapping(value="/stockInsert", method=RequestMethod.POST)
	public String stockInsert(StockVO svo, Model model) {
		log.info("stockInsert ?˜¸ì¶? ?„±ê³?");
		
		int result =0;
		String url="";
		
		result=stockService.stockInsert(svo);
		if(result==1) {
			url="/admin/stockList";
		}else {
			url="/admin/stockList";
		}
		//return "redirect:"+url;
		return "redirect:"+url;
	}
	
}
