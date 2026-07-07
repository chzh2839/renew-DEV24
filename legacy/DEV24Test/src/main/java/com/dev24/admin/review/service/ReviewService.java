package com.dev24.admin.review.service;

import java.util.List;

import com.dev24.admin.review.vo.AdminReviewViewVO;
import com.dev24.client.review.vo.ReviewVO;

public interface ReviewService {
	public List<AdminReviewViewVO> reviewList(AdminReviewViewVO revo);
	public ReviewVO reviewDetail(int re_num);
	public ReviewVO getBookInfo(int b_num);
	public int reviewDelete(ReviewVO revo) throws Exception;
}
