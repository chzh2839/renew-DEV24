<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Insert title here</title>
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/image/icon.png" />
		<link rel="apple-touch-icon" href="/resources/image/icon.png" />
		
		<!--IE8이하 브라우저에서 HTML5를 인식하기 위해서는 아래의 패스필터를 적용하면 된다.(조건부주석) -->
		<!--[if lt IE 9]>
			<script src="/resources/js/html5shiv.js"></script>
		<![endif]-->
		
		<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
         <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
          <link rel="stylesheet" href="/resources/include/css/adminPage.css">
          <link rel="stylesheet" href="/resources/include/css/style_board_content.css" />
   		 <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
    
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
	
		<style>
			.listWrap {
    			margin : 0 auto;
    			margin-top: 20px;
    			width: 1000px;
    			background-color:#fff;
    		}
    		.bookWrap {
    			width: 100%;
    			height: 200px;
    			border: 1px solid lightgray;
    			margin-bottom: 30px;
    		}
    		.listcoverWrap {
			   width: 120px;
			   float: left;
			   margin : 10px;
   			}
    		.listcover {
    			width: 100%;
    			border-right: solid #f4eeff 25px;
    		}
    		.booktext {
			    width: 400px;
			    float: left;
			    padding-left: 40px;
			    text-align: left;
			    margin-top:10px;
       		}
       		.b_name {
       			font-weight: 900;
       			font-size: 26px;
       			color: #736794;
       			overflow: hidden;
       			text-overflow: ellipsis;
       			width: 390px;
       		}
       		.b_nameText {
    			max-width: 500px;
       			font-weight: 1000;
       			font-size: 20px;
       			color: #736794;
       			padding-top: 10px;
       			text-overflow: ellipsis;
    			white-space: nowrap;
       			overflow: hidden;
       		}
       		.authorPub {
       			font-size: 14px;
       			padding-top: 5px;
       		}
       		.priceWrap {
       			padding: 3px 0 10px;
       			font-size: 20px;
       			font-weight: bold;
       		}
       		.won {
				color: #959595;
       			font-size: 14px;
       			padding-top: 3px;
       		}
       		.authorPub, .priceWrap {
       			color: #959595;
    			padding-top: 10px;
       		}
       		textarea#re_content{
       			height: 200px;
       		}
       		.stars{display: inline-block; margin-right: 30px;}
       		.stars li{
       			display : inline-block;
       			cursor : pointer;
       		}
       		.td_nickname{
       			padding-left:20px;
       		}
       		.imgFile{
       			width:300px;
       			vertical-align: middle;
			    margin-bottom: 10px;
			    margin-left: 10px;
       		}
       		.td_img span{
       			display:block;
       		}
       		#content{
       			padding-top:80px;
       		}
       		.listcoverWrap{
       			width: 150px;
       		}
       		table.table th{
       			text-align: center !important;
       		}
		</style>
		<script>
			$(function(){
				var failMsg = "${failMsg}";
				if(failMsg!=""){
					alert(failMsg);
					failMsg = "";
				}
				
				/* 리뷰관리 페이지로 이동 */
				$("#goReviewBtn").click(function(){
					location.href="/admin/reviewList";
				});
				
				/* 메인으로 이동 */
				$("#goMainBtn").click(function(){
					location.href="/admin/adminIndex";
				});
				
				/* 평점에 따른 별 개수 출력 */
				var re_score = "${list.re_score}";
				var getEmpty = '<i class="far fa-star"></i>';
				var getStar = '<i class="fas fa-star"></i>';
				for(var i=0; i<re_score; i++){
					$(".stars > li").eq(i).html(getStar);
				}
				
				/* 리뷰 삭제 처리 */
				$("#reviewDeleteBtn").click(function(){
					$("#f_writeForm").attr({
						"method" : "post",
						"action" : "/admin/reviewDelete"
					});
					$("#f_writeForm").submit();
				});
				
			});
		</script>
		
	</head>
	<body>
		<div id="content_wrap">
			<div id="upper">
	            <div class="center">
	                <h2 id="tit">리뷰관리</h2>
	
	                <div id="btnArea">
	                   <input type="button" class="btn btn-info" id="goReviewBtn" value="리뷰관리로 이동" />
	                   <input type="button" class="btn btn-success" id="goMainBtn" value="관리자페이지" />
	                </div><!--btnArea-->
	            </div><!--center-->
	        </div><!--upper-->
	        
	       
	        <div id="content">
	        	<%-- 책정보 출력 부분 --%>
	        	<div class="listWrap">
					<div class="bookWrap" data-num="${bookInfo.b_num}">
						<div class="listcoverWrap text-left">
							<img class="listcover" src="${bookInfo.listcover_imgurl}">
						</div>
						<div class="lineDiv"></div>
						<div class="booktext text-left">
							<h1 class="b_name" title="${bookInfo.b_name}">
								<span class="b_nameText" >${bookInfo.b_name}</span>
							</h1>
							<span class="authorPub">${bookInfo.b_author} 저 | ${bookInfo.b_pub}</span>
							<p class="priceWrap">
								<span class="b_price" style="display: none" >${bookInfo.b_price}</span>
									<fmt:formatNumber value="${bookInfo.b_price}" pattern="#,###"/>
								<span class="won">원</span>
							</p>
						</div>
					</div>
				</div>
				
				<div id="button_wrap">
	                <input type="button" id="reviewDeleteBtn" class="btn btn-default" value="삭제" />
	            </div>   
	        
	            <form id="f_writeForm">
	            	<input type="hidden" name="re_score" id="re_score" value="${list.re_score}" />
	            	<input type="hidden" name="b_num" id="b_num" value="${list.b_num}" />
	            	<input type="hidden" name="re_num" id="re_num" value="${list.re_num}" />
	            	<input type="hidden" name="re_imgurl" id="re_imgurl" value="${list.re_imgurl}" />
	            
	                <table class="table" border="1">
	                    <colgroup>
	                        <col width="20%" />
	                        <col width="80%" />
	                    </colgroup>
	                    <tr class="tr_title">
	                        <th>평점</th>
	                        <td>
	                        	<ul class="stars">
		                        	<li data-score="1"><i class="far fa-star"></i></li>
									<li data-score="2"><i class="far fa-star"></i></li>
									<li data-score="3"><i class="far fa-star"></i></li>
									<li data-score="4"><i class="far fa-star"></i></li>
									<li data-score="5"><i class="far fa-star"></i></li>
								</ul>
								<span id="score">${list.re_score}점</span>
	                        </td>
	                    </tr>
	                    <tr>
	                        <th>작성자</th>
	                        <td class="td_nickname">${list.c_nickname}</td>
	                    </tr>
	                    <tr>
	                        <th>리뷰내용</th>
	                        <td>${list.re_content}</td>
	                    </tr>
	                    <tr>
	                    	<th>이미지첨부</th>
	                    	<td class="td_img">
	                    		<c:if test="${not empty list.re_imgurl}">
		                    		<img src="/uploadStorage/review/${list.re_imgurl}" class="imgFile" data-url="${list.re_imgurl}" />
	                    		</c:if>
	                    	</td>
	                    </tr>
	                </table>
	            </form>
	        
	        </div> <!-- content (width : 1200px) -->
	    </div> <!-- content_wrap -->
	</body>
</html>