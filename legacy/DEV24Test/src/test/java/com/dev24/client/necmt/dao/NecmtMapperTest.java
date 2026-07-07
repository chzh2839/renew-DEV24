package com.dev24.client.necmt.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.necmt.vo.NecmtVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class NecmtMapperTest {

	@Setter(onMethod_ = @Autowired)
	private NecmtDAO necmtDAO;

	
//	  @Test 
//	  public void testNecmtList() { 
//		  log.info("testNecmtList() ï¿½ï¿½ï¿½ï¿½");
//	 
//		  List<NecmtVO> list = necmtDAO.necmtList();
//	  
//		  for (NecmtVO vo : list) {
//			  log.info(vo.toString()); 
//		  }
//	  }
	
	@Test
	public void testReplyCheck() {
		log.info("testReplyCheck ½ÃÀÛ");
		
		log.info("´ñ±Û°³¼ö Ã¼Å©: " + necmtDAO.replyCheck(2));
		
		
	}
	 

}
