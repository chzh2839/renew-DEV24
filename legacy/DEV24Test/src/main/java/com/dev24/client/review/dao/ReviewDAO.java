package com.dev24.client.review.dao;

import java.util.List;

import com.dev24.client.review.vo.ReviewVO;


public interface ReviewDAO {
	public List<ReviewVO> reviewList(ReviewVO revo);
	public ReviewVO getBookInfo(int b_num);
	public int reviewInsert(ReviewVO revo);
	public int ratingUpdate(ReviewVO revo);
	public int ratingMinus(ReviewVO revo);
	public int reviewDelete(ReviewVO revo);
	public ReviewVO reviewUpdateForm(int re_num);
	public int reviewUpdate(ReviewVO revo);
	
	public int reviewListCnt(ReviewVO revo);
}
