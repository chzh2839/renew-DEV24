package com.dev24.client.qna.vo;

import com.dev24.common.vo.CommonVO;

import lombok.Data;

@Data
public class QnaVO extends CommonVO{
	
	private	int 	q_num       =0;		//Q&A 글번호
	private int 	c_num       =0;	//회원번호
	private String  c_nickname    ="";	//회원별명
	private String  q_content     ="";	//Q&A 글내용
	private String  q_title       ="";	//Q&A 글제목
	private String  q_writedate   =""; //Q&A 글작성일
	private String  q_category    ="";  //Q&A 카테고리                         
	private String  q_imgurl      ="";		//Q&A 이미지 첨부파일 경로
	private int     q_readcnt     =0;	//글 조회수
	private int     q_repIndent   =0;	//답변글 작성 시 사용(답변글의 순서 지정)
	private int     q_repRoot     =0;	//답변글 작성 시 사용(원래글의 번호참도)
	private int     q_repStep     =0;	//답변글 작성 시 사용(답변글의 들여쓰기 지정)
		   
}
