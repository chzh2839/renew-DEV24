package com.dev24.client.ne.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.ne.vo.NeVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class NeMapperTest {
	
	@Setter(onMethod_ = @Autowired)
	private NeDAO neDAO;
	
//	@Test
//	public void testNeList() {
//		log.info("testNeList() 시작");
//		
//		List<NeVO> neList = neDAO.neList();
//		
//		for(NeVO nevo : neList) {
//			log.info(nevo.toString());
//		}
//	}
//	
//	@Test
//	public void testNeDetail() {
//		log.info("testNeDetail() 시작");
//		
//		NeVO nvo = neDAO.neDetail(2);
//		
//		log.info(nvo.toString());
//	}
	
	@Test
	public void testNeInsert() {
		log.info("testNeInsert 시작");
		
		NeVO nevo = new NeVO();
		
		nevo.setNe_cate("공지사항");
		nevo.setNe_content("공지사항 테스트입니다.");
		nevo.setNe_imgurl(null);
		nevo.setNe_title("공지사항 테스트");
		
		neDAO.neInsert(nevo);
		
		log.info(neDAO.neList());
	}
}
