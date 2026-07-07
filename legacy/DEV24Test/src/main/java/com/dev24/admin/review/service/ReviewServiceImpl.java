package com.dev24.admin.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.admin.review.dao.ReviewDaoAdmin;
import com.dev24.admin.review.vo.AdminReviewViewVO;
import com.dev24.client.review.dao.ReviewDAO;
import com.dev24.client.review.vo.ReviewVO;
import com.dev24.common.file.FileUploadUtil;

import lombok.AllArgsConstructor;

@Service("admin.reviewService")
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private ReviewDaoAdmin reviewDaoAdmin;
	private ReviewDAO reviewDAO;
	
	// review list print
	@Override
	public List<AdminReviewViewVO> reviewList(AdminReviewViewVO revo) {
		List<AdminReviewViewVO> list = null;
		list = reviewDaoAdmin.reviewList(revo);
		return list;
	}
	
	// review detail form print
	@Override
	public ReviewVO reviewDetail(int re_num) {
		ReviewVO revo = null;
		revo = reviewDAO.reviewUpdateForm(re_num);
		return revo;
	}

	// at review detail page, book info print
	@Override
	public ReviewVO getBookInfo(int b_num) {
		ReviewVO revo = reviewDAO.getBookInfo(b_num);
		return revo;
	}

	// review delete with image file
	@Transactional
	@Override
	public int reviewDelete(ReviewVO revo) throws Exception {
		int result = 0;
		int resultRating = 0;
		//log.info("revo.getRe_imgurl() > "+revo.getRe_imgurl());
		if(revo.getRe_imgurl() != "") {
			FileUploadUtil.fileDelete(revo.getRe_imgurl());
		}
		result = reviewDAO.reviewDelete(revo);
		
		// rating table update after delete from review
		if(result == 1) {
			resultRating = reviewDAO.ratingMinus(revo); 
		}else {
			resultRating = 0;
		}
		
		return resultRating;
	}

}
