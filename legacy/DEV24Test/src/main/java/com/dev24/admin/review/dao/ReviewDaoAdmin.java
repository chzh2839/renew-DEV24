package com.dev24.admin.review.dao;

import java.util.List;

import com.dev24.admin.review.vo.AdminReviewViewVO;

public interface ReviewDaoAdmin {
	public List<AdminReviewViewVO> reviewList(AdminReviewViewVO revo);
}
