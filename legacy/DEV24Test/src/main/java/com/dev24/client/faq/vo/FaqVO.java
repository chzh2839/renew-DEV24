package com.dev24.client.faq.vo;

import com.dev24.common.vo.CommonVO;

import lombok.Data;

@Data
public class FaqVO extends CommonVO {
	
	private	int 	faq_num       =0;		//FAQ 글번호
	private String  faq_content     ="";	//FAQ 글내용
	private String  faq_title       ="";	//FAQ 글제목
	private String  faq_writedate   =""; 	//FAQ 글작성일
	private String  faq_category    ="";  //FAQ 카테고리                         
	private int     faq_readcnt     =0;	//글 조회수
	
	
}
