<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<!-- 문서 유형 : 현재 웹 문서가 어떤 HTML 버전에 맞게 작성되었는지를 알려준다. -->

<!--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
     DTD 선언문이 HTML 페이지의 가장 첫 라인에 명시되어야 웹 브라우저가 HTML 버전을 인식.
     HTML태그나 CSS를 해당 버전에 맞도록 처리하므로 웹 표준 준수를 위하여 반드시 명시되어야 한다.-->
<html lang="ko">
   <head>
      <meta charset="UTF-8" />
      <meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
      
      <title>DEV 24 Stock Admin page</title>
         
         <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
         <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
    
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
		 <!-- 부트스트랩  -->         
      <!--[if lt IE 9]>
      <script src="../js/html5shiv.js"></script>
      <![endif]-->
      
      <script type="text/javascript">
     	$(function(){
     		//재고관리 페이지로 돌아가는 버튼 
     		$("#stockListBtn").click(function(){
				location.href="/admin/stockList";
			});
     		
     		//관리자 메인 페이지로 돌아가는 버튼 
     		$("#goHomeBtn").click(function(){
     			location.href="/admin/adminIndex";
     		})
     		
     	}); 
	  </script>
      
      
      <style type="text/css">
			.panel-body{background-color: white;}     
			#keyword, #search, #searchTerm, #searchData, #searchTerm, #category, #stk_regdate {height:33px;}
			
			.searchCategory{padding:15px; float:left;}
			
			#table{ padding:10px;}
			
			.bookStockImg{
				display: block;
				margin-left: auto;
				margin-right: auto;
				width: 30%;
				height:30%;
			}
			
			#title{
				text-align: center;
			}
			
			.stkDetail {
            cursor: pointer;
         	}
			  
			#searchArea{
			margin:10px;
			/*float:right;*/ 
			} 
			
			td{ text-align: left;}
			
			#bodydiv{
				margin:20px;
				width:1200px;
				float:right;
			}
			
      </style>
      
      
      
   </head>
   <body>
   		
   		<div id="bodydiv">
   		
   		<form id="detailForm" name="detailForm">
			<input type="hidden" id="stk_incp" name="stk_incp"/>
		</form>
   		
		<!-- model form -->
		<h1 id="title">재고 상세 페이지</h1>
		
		 <!-- 여기서부터가 우리가 입력할 body 부분 시작. 재고 리스트를 여기에 출력 -->
		<div>
			<input type="button" name="goHomeBtn" id="goHomeBtn" class="btn btn-s btn-success" value="관리자페이지 "/>
	   		<input type="button" name="stockListBtn" id="stockListBtn" class="btn btn-s btn-success" value="재고관리 페이지"/>
		</div>
			   
		<div>
			<div id="table">
				<h1>도서 상세 정보</h1>
				<table class="table table-striped">
					<%-- 도서 대분류 및 소분류명을 변환해주는 jstl 함수 --%>
					<c:choose>
						<c:when test="${stockDetail.cateone_num == 1}">
							<c:set var="cateOne" scope="session" value="일반도서" />
						</c:when>
						<c:otherwise>
							<c:set var="cateOne" scope="session" value="ebook" />
						</c:otherwise>
					</c:choose>

					<%-- 도서 소분류코드 번호를 변환해주는 jstl 함수  --%>
					<c:choose>
						<c:when test="${stockDetail.catetwo_num == 1}">
							<c:set var="cateTwo" scope="session" value="프로그래밍 언어" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 2}">
							<c:set var="cateTwo" scope="session" value="OS/데이터베이스" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 3}">
							<c:set var="cateTwo" scope="session" value="웹프로그래밍" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 4}">
							<c:set var="cateTwo" scope="session" value="컴퓨터 입문/활용" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 5}">
							<c:set var="cateTwo" scope="session" value="네트워크/해킹/데이터베이스" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 6}">
							<c:set var="cateTwo" scope="session" value="IT 전문서" />
						</c:when>
						<c:when test="${stockDetail.catetwo_num == 7}">
							<c:set var="cateTwo" scope="session" value="컴퓨터 수험서" />
						</c:when>
						<c:otherwise>
							<c:set var="cateTwo" scope="session" value="웹/컴퓨터/입문 활용" />
						</c:otherwise>
					</c:choose>

					<tr>
						<td>도서코드</td>
						<td>${stockDetail.stk_incp}</td>
					</tr>
					
					<tr>
						<td>제목</td>
						<td>${stockDetail.b_name}</td>
					</tr>
					
					<tr>
						<td>작가</td>
						<td>${stockDetail.b_author}</td>
					</tr>
					
					<tr>
						<td>출판사</td>
						<td>${stockDetail.b_pub}</td>
					</tr>
					
					<tr>
						<td>판매가격</td>
						<td>${stockDetail.b_price}원</td>
					</tr>
					
					<tr>
						<td>대분류</td>
						<td>${cateOne}</td>
					</tr>
					
					<tr>
						<td>소분류</td>
						<td>${cateTwo}</td>
					<tr>
						<td>이미지</td>
						<td><img src="${stockDetail.listcover_imgurl}"/></td>
					</tr>
					</tbody>
				</table>
			</div>
		</div>
		
		</div>
	
   </body>
</html>
    