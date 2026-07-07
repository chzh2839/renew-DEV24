<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>     
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
		
		<title>FAQ MAIN</title>
		
      	<link rel="stylesheet" type="text/css" href="/resources/include/css/style_boot.css" />
      	<link rel="stylesheet" type="text/css" href="/resources/include/css/style_board_content.css" />
      	
      	<style type="text/css">
      	#title{
      		height :300px;
      	}
      		#tit_content h2{
		text-align: center;
		color: #424874;
		line-height: 50px;
		font-size: 40px;
		font-weight: 500;
		display: block;
		padding-top : 60px;
		}
		
		#tit_content h3{
		text-align: center;
		line-height: 50px;
		font-weight: 500;
		display: block;
		color: #245580;
		font-size: 44px;
		padding-bottom : 30px;		
		}
		
		.faqMainBoxArea {
	    margin: 0 auto;
	    padding: 57px 0 60px;
	    width: 1130px;
	    border: solid 1px #d8d8d8;
	    background-color: #fff;
		}
		
      	.col-lg-4 {
	    width: 19.333333%;
		}
		
		.text-center{
			text-align: center;
		}
		#keyword{
			height:26px;
		}
		.list-inline{
			width: 560px;
		    margin: 0 auto;
		}
		.list-inline > li{
			float : left;
		}
		.list-inline:after{
			clear : both;
			display:block;
			content:'';
		}
		.list-inline > li:after{
			content : '|';
			margin-left: 10px;
			margin-right: 10px;
		}
		.list-inline > li:last-child:after{
			content : '';
		}
		
		
		.tit h2:before{
		    content: '';
		    display: inline-block;
		    width: 7px;
		    height: 20px;
		    background-color: #a6b1e1;
		    margin-right: 10px;
		    border-radius: 5px;
		}
		
		.tit h2{
		    height: 20px;
		    display: inline-block;
		    margin-top : 30px;
		}
		
		#qna_wrap > div:first-child{
			float : left;
		}
		#qna_wrap > div:last-child{
			float : right;
		}
		#qna_wrap:after{
			clear : both;
			display:block;
			content:'';
		}
		.col-md-4{
			width:45%;
			margin-top:20px;
			padding : 20px;
			background-color: #eee;
   			border-radius: 20px;
		}
		.col-md-4 > h2{
			margin-bottom:10px;
		}
		.col-md-4 a{
			margin-top:10px;
		}

      	</style>
		
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/css/bootstrap.min.css"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		
		<script>
		
		
			$(function(){
				
				/* 검색 후 검색 대상과 검색 단어 출력 
				if('${data.keyword}'!=""){
					$("#keyword").val('${data.keyword}');
					$("#search").val('${data.search}');
				} */
				
				if('${param.keyword}'!=""){
					$("#keyword").val('${param.keyword}');
					$("#search").val('${param.search}');
				}
				
				
				/* 입력 양식 enter 제거 */
				$("#keyword").bind("keydown",function(event){
					if(event.keyCode == 13) {
						event.preventDefault();
					}
				});
				
				
				
				/* 제목 클릭 시 상세 페이지 이동을 위한 처리 이벤트 */
				$(".goDetail").click(function(){
					//var num = $(this).parents("tr").children().eq(0).html();
					var num = $(this).parents("tr").attr("data-num");
					//console.log("num=" +num);
					$("#num").val(num); //post 방식을 사용하기 떄문에 form태그 안에 num값을 넘겨준다.
					$("#detailForm").attr({
						"method":"post",
						"action":"/faq/faqMainDetail"
					});
					$("#detailForm").submit();
				});
				
				
				
				
				/* 검색대상이 변경될 때마다 처리 이벤트 */
				$("#search").change(function(){
					if($("#search").val()=="all"){
						$("#keyword").val("전체 데이터 조회합니다.");
					}else if($("#search").val()!="all"){
						$("#keyword").val("");
						$("#keyword").focus();
					}
				});
				
			});
			
			/* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				$("#f_search").attr({
					"method":"post",
					"action":"/faq/faqMainDetail"
				});
				$("#f_search").submit();
			}
			
		</script>
		
		
		
		
		
	</head>
	<body>
	
	<div id="content_wrap" >
	 
      <div id="title" style="background-image: URL(/resources/image/dev24image1.jpg);">
      	<div id="tit_content" class="text-center">
	        <h2>DEV24 고객센터입니다.</h2>
	        <h3>무엇이든 물어보세요</h3>
        </div>
        <form name="detailForm" id="detailForm">
				<input type="hidden" name="num" id="num" />
		</form>
        
       <%-- =============검색기능 시작================== --%>	
			<div id="faqSearch" class="text-center">
				<form id="faqMain_search" name="faqMain_search" class="form-inline">
					<div class="form-group">
						<input type="text" name="keyword" id="keyword" value="검색어를 입력하세요" class="form-control" />
						<button type="button" title="검색" id="searchData" class="btn btn-success"><span class="glyphicon glyphicon-hand-right">검색</span></button>
						<!-- <button type="button" class="btn btn-success">이동하기 <span class="glyphicon glyphicon-hand-right"></span></button> -->
					</div>
				</form>
			</div><br/>
			<%-- =============검색기능 끝================== --%>
			
			<div class="text-center">
			     <ul class="list-inline">
	            	<li>인기 검색어</li>
	                <li><a href="">북클럽 </a></li>
	                <li><a href="">배송 </a></li>
	                <li><a href="">회원정보 확인/변경 </a></li>
	                <li><a href="">중고매장 </a></li>
	                <li><a href="">공연예매 수수료 </a></li>
	            </ul>
			</div>
      </div>
    
	
	<!-- ####################00000 FAQ 영역 시작 00000#################### -->
	
		<div id="content">
			 <div class="tit">
                 <h2>FAQ</h2>
             </div>
			<div class="table-responsive">
		            <table class="table table-striped" style="text-align:center">
		              <colgroup>
	                      <col width="20%" />
	                      <col width="20%" />
	                      <col width="20%" /> 
	                      <col width="20%" />
	                      <col width="20%" />
	                  </colgroup>
		              <thead>
		                <tr>
		                  <th><a href="">상품</a></th>
		                  <th><a href="">구매환불</a></th>
		                  <th><a href="">게시판</a></th>
		                  <th><a href="">회원관리</a></th>
		                  <th><a href="">기타 </a></th>
		                </tr>
		              </thead>
		              <tbody>
		                <tr>
		                  <td><a href="">도서 </a></td>
		                  <td><a href="">구매상세</a></td>
		                  <td><a href="">이용수칙 </a></td>
		                  <td><a href="">회원가입 </a></td>
		                  <td><a href="">DEV24? </a></td>
		                </tr>
		                <tr>
		                  <td><a href="">ebook</a></td>
		                  <td><a href="">주문 </a></td>
		                  <td><a href="">회원정지</a></td>
		                  <td><a href="">회원정보확인 </a></td>
		                  <td></td>
		                </tr>
		                <tr>
		                  <td><a href="">절판 </a></td>
		                  <td><a href="">환불승인</a></td>
		                  <td><a href="">회원관리 </a></td>
		                  <td><a href="">회원탈퇴</a></td>
		                  <td></td>
		                </tr>
		                
		
		              </tbody>
		            </table>
		          </div>
			
			
			<!-- ####################00000 FAQ 영역 끝 00000#################### -->
			
			<!-- ####################00000 QNA 영역 시작 00000#################### -->
			
			<div class="tit">
                 <h2>QNA</h2>
             </div>
			
			<div id="qna_wrap">
				<div class="col-md-4">
			          <h2>질문 게시판</h2>
			          <p>DEV24는 고객 분들의 다양한 의견을 수렴하기 위해서, 질문게시판을 운용하고 있습니다.<br/> 문의가 있으실 경우 이곳을 통해 문의주시기 바랍니다.</p>
			          <p><a class="btn btn-default" href="/qna/qnaList" role="button">게시판 이동하기 »</a></p>
			     </div>
			     <div class="col-md-4">
			          <h2>공지사항 게시판</h2>
			          <p>DEV24 도서 쇼핑몰 공지사항 게시판 입니다. DEV24의 소식을 발빠르게 전달하도록 하겠습니다.</p>
			          <p><a class="btn btn-default" href="#" role="button">게시판 이동하기 »</a></p>
			     </div>
			</div>
			
			<!-- ####################00000 QNA 영역 끝 00000#################### -->
			
		</div><!-- /content -->
	
	</div> <!-- /content_wrap -->
	</body>
</html>
    