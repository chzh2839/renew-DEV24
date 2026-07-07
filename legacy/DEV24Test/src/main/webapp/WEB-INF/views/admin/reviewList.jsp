<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
   		 <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
    
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
		<style>
         .listTable tbody tr:hover{
         	background-color:rgba(0,0,0,0.1);
         }
         </style>
		<script type="text/javascript">
			$(function(){
				var successMsg = "${successMsg}";
				if(successMsg!=""){
					alert(successMsg);
					successMsg = "";
				}
				
				
				/* 금액 콤마 찍기 */
				var l = $(".listTable").find("tbody tr").length;
				var sum = 0;
				for(var i=0; i<l; i++){
					var price = $(".listTable").find("tbody tr").eq(i).find("td.td_price").text();
					sum += parseInt(price);
					$(".listTable").find("tbody tr").eq(i).find("td.td_price").text(addComma(price));
				}
    			$("#total_count").text(addComma(l)); // 전체 구매 횟수
				$("#total_price").text(addComma(sum)); // 전체 금액
				
				
				/* 메인으로 이동 버튼 */
				$("#goMainBtn").click(function(){
					location.href="/admin/adminIndex";
				});
				
				
				
				/* 검색할 날짜 기본값 설정 */
				var today = new Date(); // 오늘날짜
				var year = today.getFullYear();
				var month = String(today.getMonth()+1);
				var day = String(today.getDate()); // 일
				
				if(month.length == 1){ 
					  month = "0" + month; 
				} 
				if(day.length == 1){ 
				  day = "0" + day; 
				}
				
				var fullToday = year + "-" + month + "-" + day;
				console.log(fullToday);
				
				/* 키워드 검색 시 */
				$("#searchTextBtn").click(function(){
					if($("#search").val()!="all"){ // 검색 조건이 '전체'일 때 빼고는 유효성 체크!
						if(!chkData("#keyword", "검색어를"))return;
					}
					goPage(); // 검색 완료 후 purchaseList 페이지 재호출하는 함수
				});
				
				
				/* 검색 후 검색대상과 검색단어 출력 */
				//console.log("keyword : ${data.keyword}");
				//console.log("search : ${data.search}");
				if("${data.keyword}" != ""){
					$("#keyword").val("${data.keyword}");
					$("#search").val("${data.search}");
				}
				if("${data.typeCheck}" != ""){
					$("input[value='${data.typeCheck}']").prop("checked", true);
				}
				if("${data.date_start}" != ""){
					$("input[name='date_start']").val("${data.date_start}");
				}else{
					$("#date_start").val(fullToday);
				}
				if("${data.date_end}" != ""){
					$("input[name='date_end']").val("${data.date_end}");
				}else{
					$("#date_end").val(fullToday);
				}

				console.log("${data.date_start}");
				console.log("${data.date_end}");
				/* 입력양식 enter제거 */
				$("#keyword").bind("keydown", function(event){
					if(event.keyCode == 13){
						event.preventDefault();
					}
				});
				
				/* 검색 조건 '전체'로 했을 때 */
				$("#search").change(function(){
					if($("#search").val()=='all'){
						$("#keyword").val("전체 데이터 조회");
					}else if($("#search").val()!='all'){
						$("#keyword").val("");
						$("#keyword").focus();
					}
				});
				
				
				/* 자세히 버튼 눌렀을 때 해당 리뷰상세 페이지로 이동 */
				$(".reviewDetailBtn").click(function(){
					var re_num = $(this).parents("td").siblings(".td_renum").text();
					var b_num = $(this).parents("td").siblings(".td_bnum").text();
					
					location.href="/admin/reviewDetail?re_num="+re_num+"&b_num="+b_num;
				});
				
			}); // 최상위 종료
			
			
			/* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				
				$("#f_searchText").attr({
					"method" : "get",
					"action" : "/admin/reviewList"
				});
				$("#f_searchText").submit();
			}
			
		</script>
		

	</head>
	<body>
		<div id="container">
	       <div id="upper">
	            <div class="center">
	                <h2 id="tit">리뷰관리</h2>
	
	                <div id="admin_search">
	                    <form name="f_searchText" id="f_searchText" class="form-inline">
	                        <div class="form-group">
	                            <label>검색조건</label>
	                            <select name="search" id="search" class="form-control">
	                                <option value="all">전체</option>
	                                <option value="re_num">리뷰번호</option>
	                                <option value="b_num">도서번호</option>
	                                <option value="b_name">도서명</option>
	                                <option value="c_nickname">작성자</option>
	                                <option value="p_num">구매번호</option>
	                            </select>
	                            <input type="text" name="keyword" id="keyword" class="form-control" />
	                        </div>
	                        <div class="form-group">
	                            <label>등록날짜</label>
	                            <input type="date" name="date_start" id="date_start" class="form-control" /> ~ 
	                            <input type="date" name="date_end" id="date_end" class="form-control" />
	                        </div>
	                        <div class="form-group">
	                            <label>리뷰종류</label>
	                            <input type="radio" name="typeCheck" value="image" />image
	                            <input type="radio" name="typeCheck" value="text" />text
	                            <input type="radio" name="typeCheck" value="ALL" checked="checked" />ALL
	                        </div>
	                        <input type="button" id="searchTextBtn" value="검색" class="btn btn-default" />
	                    </form>
	                </div><!-- admin_search -->
	
	                <div id="btnArea">
	                   <input type="button" class="btn btn-success" id="goMainBtn" value="관리자페이지" />
	                </div><!--btnArea-->
	            </div><!--center-->
	        </div><!--upper-->
	        
	        
	        <div id="content_wrap">
	            <table class="listTable table table-striped" border="1">
	            	<colgroup>
	                   <col width="7%" />
	                   <col width="7%" />
	                   <col width="25%" /> 
	                   <col width="10%" />
	                   <col width="9%" />
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>리뷰번호</th> <!--re_num-->
	                        <th>도서번호</th> <!--b_num-->
	                        <th>도서명</th> <!--b_name-->
	                        <th>작성자</th>
	                        <th>구매번호</th>
	                        <th>등록일</th>
	                        <th>리뷰종류</th>
	                        <th></th>
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty list}">
	                			<c:forEach var="list" items="${list}">
	                				<tr>
				                        <td class="td_renum">${list.re_num}</td>
				                        <td class="td_bnum">${list.b_num}</td>
				                        <td>${list.b_name}</td>
				                        <td>${list.c_nickname}</td>
				                        <td>${list.p_num}</td>
				                        <td>${list.re_writedate}</td>
				                        <td>${list.re_type}</td>
				                        <td>
				                        	<input type="button" value="자세히" class="btn btn-primary reviewDetailBtn" />
				                        </td>
				                    </tr>
	                			</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr>
	                				<td colspan="8">내역이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>
	                </tbody>
	            </table>
	        
	        </div><!--content_wrap-->
	        
	        
	        <div id="down">
	            <div class="center">
	                <label class="totalCount">총 리뷰 수 : <strong><span id="total_count"></span>건</strong></label>
	            </div><!--center-->
	        </div><!--down-->
	        
	    </div><!--container-->
	</body>
</html>