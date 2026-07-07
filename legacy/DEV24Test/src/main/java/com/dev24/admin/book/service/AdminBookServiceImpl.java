package com.dev24.admin.book.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.admin.book.dao.AdminBookDAO;
import com.dev24.client.book.vo.BookVO;

import lombok.Setter;

@Service
public class AdminBookServiceImpl implements AdminBookService{
	
	@Setter(onMethod_ = @Autowired)
	private AdminBookDAO adminBookDAO;
	
	@Override
	@Transactional
	public int updateBookState(BookVO bvo) {
		int result = adminBookDAO.updateBookState(bvo);
		return result;
	}
}
