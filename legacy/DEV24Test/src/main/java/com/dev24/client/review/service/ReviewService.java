package com.dev24.client.review.service;

import java.util.List;

import com.dev24.client.review.vo.ReviewVO;

public interface ReviewService {
//	public List<ReviewVO> reviewList(ReviewVO revo);
	public String reviewList(ReviewVO revo);
	public ReviewVO getBookInfo(int b_num);
	public int reviewInsert(ReviewVO revo) throws Exception;
	public int reviewDelete(ReviewVO revo) throws Exception;
	public ReviewVO reviewUpdateForm(int re_num);
	public int reviewUpdate(ReviewVO revo) throws Exception;
	
	public int reviewListCnt(ReviewVO revo);
}
