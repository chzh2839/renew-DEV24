package com.dev24.client.bookimg.dao;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.bookimg.vo.BookImgVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class BookImgMapperTest {

	@Setter(onMethod_ = @Autowired)
	private BookImgDAO bookImgDAO;
	
	@Test
	public void testBookImgList() {
		BookImgVO bivo;
		ArrayList<BookImgVO> bookList = bookImgDAO.bookImgList();
		log.info("도서이미지 가져오기");
//		for (int i = 0 ; i < bookList.size() ; i++) {
//			bvo = bookList.get(i);
//			log.info(bvo.toString());
//					
//		}
		String first = bookList.get(0).toString();
		String last = bookList.get(bookList.size() - 1).toString();
		
		log.info(first);
		log.info(last);
	}
	
}
