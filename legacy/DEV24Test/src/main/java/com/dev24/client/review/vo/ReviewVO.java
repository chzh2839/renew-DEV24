package com.dev24.client.review.vo;

import org.springframework.web.multipart.MultipartFile;

import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;
import com.dev24.common.vo.CommonVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ReviewVO extends CommonVO {
	private int re_num = 0;
	private int re_score = 0; // 1 unit
	private int c_num = 0;
	private String c_nickname = "";
	private String re_writedate = "";
	private String re_type = ""; // IN('text', 'image')
	private String re_content = "";
	private int b_num = 0;
	private int pd_num = 0;
	private String re_imgurl = ""; // fileName
	private MultipartFile file; // for file upload
	
	// review form page for book info print
	private String b_name = "";
	private String b_author = "";
	private String b_pub = ""; // publish company
	private int b_price = 0;
	private String listcover_imgurl = "";
	
	private String reviewType_sort = "";
	
	// rating table elements
	private int ra_num = 0; // sum of re_score
	private int ra_count = 0; // count of reviews
	
	// review count for paging
	private int r_count = 0;
	
}
