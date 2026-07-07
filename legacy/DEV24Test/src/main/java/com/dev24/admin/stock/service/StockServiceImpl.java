package com.dev24.admin.stock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.admin.stock.dao.StockDAO;
import com.dev24.admin.stock.vo.StockDetailVO;
import com.dev24.admin.stock.vo.StockVO;
import com.dev24.client.book.vo.BookVO;

import lombok.Setter;

@Service
public class StockServiceImpl implements StockService {
	
	@Setter(onMethod_=@Autowired )
	private StockDAO stockdao;
	
	@Override
	public List<StockVO> stockList(StockVO svo) {
		
		List<StockVO> list = null;
		list = stockdao.stockList(svo);
		return list;
	}

	@Override
	public List<BookVO> stockBInfoList(BookVO bvo) {
		List<BookVO> list =null;
		list = stockdao.stockBInfoList(bvo);
		return list;
	}

	@Override
	public StockDetailVO getStockDetail(StockDetailVO sdvo) {
		StockDetailVO detail = null;
		detail = stockdao.getStockDetail(sdvo);
		return detail;
	}

	@Override
	public int stockInsert(StockVO svo) {
		int result =0;
		result = stockdao.stockInsert(svo);
		return result;
	}
	
	

}
