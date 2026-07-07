package com.dev24.admin.stock.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.admin.stock.vo.StockDetailVO;
import com.dev24.admin.stock.vo.StockVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class StockMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private StockDAO stockDAO;
	
	/*@Test
	public void testStockList() {
		StockVO svo = new StockVO();
		List <StockVO> list = stockDAO.stockList(svo);
		for(StockVO vo : list) {
			log.info(vo);
		}
	}*/
	
	@Test
	public void testStockDetail() {
		StockDetailVO sdvo = new StockDetailVO();
		sdvo.setStk_incp(3);
		StockDetailVO stock = stockDAO.getStockDetail(sdvo);
		log.info(stock.toString());
	}
}
