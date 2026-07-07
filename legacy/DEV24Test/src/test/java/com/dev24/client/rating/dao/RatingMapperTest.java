package com.dev24.client.rating.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.pdetail.vo.PdetailVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class RatingMapperTest {
		
	@Setter(onMethod_ = @Autowired)
	private RatingDAO ratingDAO;
	
	@Test
	public void testUpdateSalesCnt () {
		log.info("testUpdateSalesCnt");
		
		List<PdetailVO> list = new ArrayList<>();
		PdetailVO pvo;
		
		for(int i = 5 ; i < 6 ; i++) {
			pvo = new PdetailVO();
			pvo.setB_num(i);
			list.add(pvo);
		}
		
		int result = ratingDAO.updateSalesCnt(list);
		
		log.info("result : " + result);
	}

}
