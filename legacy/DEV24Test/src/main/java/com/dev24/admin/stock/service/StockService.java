package com.dev24.admin.stock.service;

import java.util.List;

import com.dev24.admin.stock.vo.StockDetailVO;
import com.dev24.admin.stock.vo.StockVO;
import com.dev24.client.book.vo.BookVO;


public interface StockService {
	public List<StockVO> stockList(StockVO svo);
	public List<BookVO> stockBInfoList(BookVO bvo);
	public StockDetailVO getStockDetail(StockDetailVO sdvo);
	public int stockInsert(StockVO svo);
}
