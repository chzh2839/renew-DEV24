package com.dev24.client.bookimg.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.client.bookimg.dao.BookImgDAO;
import com.dev24.client.bookimg.vo.BookImgVO;

import lombok.Setter;

@Service
public class BookImgServiceImpl implements BookImgService{
	
	@Setter(onMethod_ = @Autowired)
	private BookImgDAO bookImgDAO;
	
	@Override
	public ArrayList<BookImgVO> bookImgList() {
		ArrayList<BookImgVO> imgList = bookImgDAO.bookImgList();
		return imgList;
	}
}
