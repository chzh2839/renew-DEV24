package com.dev24.client.bookimg.dao;

import java.util.ArrayList;

import com.dev24.client.bookimg.vo.BookImgVO;

public interface BookImgDAO {
	public ArrayList<BookImgVO> bookImgList();
	public int bookImgInsert(BookImgVO bvo);
	public int bookImgUpdate(BookImgVO bvo);
}
