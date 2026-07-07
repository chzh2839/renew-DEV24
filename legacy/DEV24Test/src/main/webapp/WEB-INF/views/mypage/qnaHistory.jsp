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
		
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<script>
			var stateUpdateBtn = 0;
		
			$(function(){
				
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
				$("#purchaseSearchBtn").click(function(){
					if($("#search").val()!="all"){ // 검색 조건이 '전체'일 때 빼고는 유효성 체크!
						if(!chkData("#keyword", "검색어를"))return;
					}
					goPage();
				});
				
				
				/* 검색 후 검색대상과 검색단어 출력 */
				if("${data.keyword}" != ""){
					$("#keyword").val("${data.keyword}");
					$("#search").val("${data.search}");
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
				if("${data.orderby_when}" != ""){
					$("#orderby_when").val("${data.orderby_when}");
				}
				if("${data.orderby_state}" != ""){
					$("#orderby_state").val("${data.orderby_state}");
				}

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
				
				
				/* 최근순/과거순 정렬 */
				$("#orderby_when").change(function(){
					goPage();
				});
				
				/* 문의상태 정렬 */
				$("#orderby_state").change(function(){
					goPage();
				});
				
				
				/* 1주, 1달, 3달, 6달 기준 검색 */
				$("#orderdate_row1 > ul > li").click(function(){
					$(this).addClass("on").siblings("li").removeClass("on");
					var searchDate = $(this).attr("data-date");
					if(searchDate == "week"){
						today.setDate(today.getDate() - 7);
					}else if(searchDate == "month"){
						today.setMonth(today.getMonth() - 1);
					}else if(searchDate == "threeMonth"){
						today.setMonth(today.getMonth() - 3);
					}else if(searchDate == "halfMonth"){
						today.setMonth(today.getMonth() - 6);
					}
					//console.log("today: "+today);
					
					var year = today.getFullYear();
					var month = String(today.getMonth()+1);
					var day = String(today.getDate()); // 일
					
					if(month.length == 1){ 
						  month = "0" + month; 
					} 
					if(day.length == 1){ 
					  day = "0" + day; 
					}
					
					var searchFullDate = year + "-" + month + "-" + day;
					//console.log("searchFullDate : "+searchFullDate);
	
					$("#date_start").val(searchFullDate);
					$("#date_end").val(fullToday);
					
					goPage();
				});
				

				/* 내역 클릭 시 상세페이지 이동 */
				$(".td_title").click(function(){
					var q_num = $(this).parents("tr").attr("data-num");
					location.href="/qna/qnaDetail?q_num="+q_num;
				});
				
			}); // 최상위 종료
			
			
			/* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				
				$("#f_search").attr({
					"method" : "get",
					"action" : "/mypage/qnaHistory"
				});
				$("#f_search").submit();
			}
			
			
		</script>

	</head>
	<body>
		<div id="content_mypage">
            
            <!--****************** 문의내역 조회 부분 시작****************-->
            
            <div id="qnaHistory">
                <div class="tit_mypage">
                    <h3>문의내역조회</h3>
                </div>
                
               <form name="f_search" id="f_search">
                <div id="orderdate">
                   <div id="orderdate_rows">
                        <div id="orderdate_row1">
                            <ul>
                                <li data-date="week">최근1주일</li>
                                <li data-date="month">1개월</li>
                                <li data-date="threeMonth">3개월</li>
                                <li data-date="halfMonth">6개월</li>
                            </ul>
                            <div id="dateRange">
                                <input type="date" id="date_start" name="date_start" />
                                &nbsp;~&nbsp;
                                <input type="date" id="date_end" name="date_end" />
                            </div> <!--dateRange-->
                        </div><!--orderdate_row1-->

                        <div id="orderdate_row2">
                            <select name="search" id="search" class="form-control">
                               <option value="all">전체</option>
                               <option value="q_category">카테고리</option>
                               <option value="q_title">문의내역</option>
                           </select>
                           <input type="text" id="keyword" name="keyword" class="form-control" />
                           <input type="button" id="purchaseSearchBtn" class="btn btn-primary searchBtn" value="조회" />
                        </div><!--orderdate_row2-->
                    
                    </div> <!--orderdate_rows-->
                    
                </div><!--orderdate 상단 주문일자 조회-->
               
                <div id="orderby">
                    <span>정렬기준</span>
                    <select name="orderby_when" id="orderby_when">
                        <option value="last">최근순</option>
                        <option value="past">과거순</option>
                    </select>
                    <span>주문상태</span>
                    <select name="orderby_state" id="orderby_state">
                        <option value="all">전체</option>
                        <option value="replied">답변완료</option>
                        <option value="waiting">답변대기</option>
                    </select>
                </div> <!--orderby 일자 정렬기준-->
              </form>
                
                <table class="table" border="1">
                	<colgroup>
	                   <col width="20%" />
	                   <col width="20%" />
	                   <col width="50%" />
	                   <col width="10%" /> 
	               </colgroup>
                    <thead>
                        <tr>
                            <th>카테고리</th>
                            <th>문의일자</th>
                            <th>문의내역</th>
                            <th>답변상태</th>
                        </tr>
                    </thead>
                    <tbody>
                    	<c:choose>
                    		<c:when test="${not empty qvo}">
                    			<c:forEach var="qvo" items="${qvo}">
                    				<tr data-num="${qvo.q_num}">
                    					<td>${qvo.q_category}</td>
			                            <td>${qvo.q_writedate}</td>
			                            <td class="td_title">${qvo.q_title}</td>
			                            <td class="td_state">
			                            	<c:choose>
				                           	<c:when test="${qvo.q_repRoot == 0}">
				                           		답변대기
				                           	</c:when>
				                           	<c:otherwise>
				                           		답변완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="4">최근 문의내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                    </tbody>
                </table>

                
            </div>
            
            
        </div><!--content_mypage-->
	</body>
</html>