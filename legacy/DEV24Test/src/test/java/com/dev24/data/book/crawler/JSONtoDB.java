package com.dev24.data.book.crawler;

import java.io.File;
import java.util.List;

import com.dev24.client.book.dao.BookDAO;
import com.dev24.client.book.vo.BookVO;
import com.dev24.data.book.dao.InserBookDataDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONtoDB {
	
//	@Setter(onMethod_ = @Autowired)
//	private BookDAO bookDAO;

	/********************************************************************
	 * JSON에 담아놓은 데이터를 DB로 옮기는 메소드. DAO를 호출해주는 작업을 한다.
	 * 
	 * @param filePath: 저장되어있는 경로 + 파일명
	 ********************************************************************/
	public void jsonToDB(String filePath, BookDAO bookDAO) {
		ObjectMapper mapper = new ObjectMapper();
		InserBookDataDAO inserBookDataDAO = new InserBookDataDAO();
		
		
		try {
			File file = new File(filePath);
			
			//저장 경로 체크용
//			System.out.println(file.getAbsolutePath());
			
			// JSON to VO
			List<BookVO> voList = mapper.readValue(file, new TypeReference<List<BookVO>>() {
			});

			// InserBookDataDAO.bookInsert()호출
			inserBookDataDAO.booksInsert(voList);
//			bookDAO.mergeBookData(voList);
//			bookDAO.mergeBookImgData(voList);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
