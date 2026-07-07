package com.dev24.client.bookimg.vo;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class BookImgVO {
	
	//BOOKIMG 테이블에 들어갈 데이터필드
	private int b_num = 0;
	private String listcover_imgurl 	= "";
	private String detailcover_imgurl 	= "";
	private String detail_imgurl 		= "";
	
	//물리적 데이터를 저장하기 위한 필드
	private MultipartFile listcoverFile;
	private MultipartFile detailcoverFile;
	private MultipartFile detailFile;
}
