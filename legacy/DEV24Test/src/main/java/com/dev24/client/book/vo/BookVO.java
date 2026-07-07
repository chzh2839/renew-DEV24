package com.dev24.client.book.vo;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**************************************************************
 * equals와 hashCode 메소드 자동 생성 시 부모 클래스의 필드까지 감안할지 안 할지에 대해서 설정시
 * callSuper = true 부모 클래스 필드 값들도 동일 한지 체크
 * callSuper = false 부모 클래스 필드 값들도 동일 한지 체크안함
 **************************************************************/

@Data
@EqualsAndHashCode(callSuper = false)
public class BookVO {
	private int b_num				=0;
	private String b_name			="";
	private String b_date			="";
	private String b_list			="";
	private String b_author			="";
	private String b_pub			="";
	private String b_authorinfo		="";
	private String b_info			="";
	private String b_state			=""; //도서상태 : null(등록), unreg, oop(out of print)
	private int b_price				=0;
	private int cateOne_num			=0;
	private int cateTwo_num			=0;
	
	//평점
	private double ra_sum			=0.0;
	private double ra_count			=0.0;
	private double ra_avg			= ra_sum * ra_count;
	
	//이미지
	private String listcover_imgurl 	=""; 
	private String detailcover_imgurl 	="";
	private String detail_imgurl		="";
	
	//물리적인 이미지를 저장하기 위한 도서등록 이미지 파일정보
	//listcover/detailcover/detail 의 key값을 가질 수 있다.
	private MultipartFile listcoverFile;
	private MultipartFile detailcoverFile;
	private MultipartFile detailFile;
	
	//json에 저장된 url데이터 보관용
	private String url;
	
	// 정렬에 따라 츨력하기 위한 정보 (기본값 = "best")
	// dev24 / best / new / old / lowPrice / highPrice
	private String b_sort = "best";
	
	//b_salesRate : b_num 별로 가지는 sum(pd_qty)
	//판매량을 조회할 수 있다.
	private int salescnt;
	
	/*************************************************************
	 * 등록/미등록/절판 여부를 판단하기 위한 필드
	 * null 또는 "" 또는 "all"	:모두 조회
	 * "null"				:등록상품만 조회
	 * "unreg"				:미등록 상품만 조회
	 * "outOfPrint"				:절판 상품만 조회
	 * "reg or oop"			:등록 과 절판 모두 출력
	 *************************************************************/
	private String b_stateKeyword = "all";
	
	//관리자페이지에서 도서 체크 후 일괄 처리에 사용하는 항목s
	private List<Integer> bNumList;
	
	/*************************************************************
	 * 검색관련
	 * 	- searchSelect : select박스를 통해 어떤 항목을 검색할지 결정
	 * 					all, b_name, b_author, b_pub, b_info
	 *  - searchKeyword : 입력한 검색어
	 ************************************************************/
	private String b_searchSelect = "";
	private String b_searchKeyword = "";
}
