<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
		
		<!-- <link rel="stylesheet" href="/resources/include/css/style_mypage.css" /> -->
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<script>
			$(function(){
				var successMsg = "${successMsg}";
				if(successMsg!=""){
					alert(successMsg);
					successMsg = "";
				}
				
				
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
				
				/* 주문상태 정렬 */
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
				
				
				/* 도서명 클릭 시 도서상세페이지 이동 */
				$(".td_title").click(function(){
					var b_num = $(this).parents("tr").attr("data-num");
					location.href="/book/detail/"+b_num;
				});
				
				
			}); // 최상위 종료
			
			
			
			/* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				
				$("#f_search").attr({
					"method" : "get",
					"action" : "/mypage/refundHistory"
				});
				$("#f_search").submit();
			}
			
		</script>		

	</head>
	<body>
		 <!--****************** 환불내역 조회 부분 시작****************-->
          <div id="content_mypage">
            <div id="refundHistory">
                <div class="tit_mypage">
                    <h3>환불내역조회</h3>
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
                               <option value="rf_num">환불번호</option>
                               <option value="b_name">책이름</option>
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
                        <option value="reRequest">환불승인대기</option>
                        <option value="rfConfirm">승인완료</option>
                        <option value="cancel">주문취소</option>
                    </select>
                </div> <!--orderby 주문일자 정렬기준-->
             </form>
             
                
                <table class="table" border="1">
                	<colgroup>
	                   <col width="10%" />
	                   <col width="15%" />
	                   <col width="28%" /> 
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="12%" />
	                   <col width="15%" />
	               </colgroup>
                    <thead>
                        <tr>
                            <th>환불번호</th>
                            <th>주문일자</th>
                            <th>환불내역</th>
                            <th>환불금액</th>
                            <th>환불수량</th>
                            <th>주문상태</th>
                            <th>환불신청/승인일자</th> <!--rf_confirmdate-->
                        </tr>
                    </thead>
                    <tbody>
                    	<c:choose>
                    		<c:when test="${not empty rfhvo}">
                    			<c:forEach var="rfhvo" items="${rfhvo}">
                    				<tr data-num="${rfhvo.b_num}">
			                            <td class="td_num">${rfhvo.rf_num}</td>
			                            <td>${rfhvo.p_buydate}</td>
			                            <td class="td_title">${rfhvo.b_name}</td>
			                            <td><fmt:formatNumber value="${rfhvo.rf_price}" pattern="#,###" /></td>
			                            <td>${rfhvo.rf_qty}</td>
			                            <td class="td_orderdate">
			                            	<c:choose>
				                           	<c:when test="${rfhvo.rf_orderstate == 'reRequest'}">
				                           		환불승인대기
				                           	</c:when>
				                           	<c:when test="${rfhvo.rf_orderstate == 'cancel'}">
				                           		주문취소
				                           	</c:when>
				                           	<c:otherwise>
				                           		승인완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                            <td>${rfhvo.rf_confirmdate}</td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="7">최근 주문내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                    </tbody>
                </table>
                
                
                <div id="orderstate_explain">
                    <div id="tit_explain"><span>주문상태</span></div>
                    
                    <ul>
                       <li class="state">환불승인대기<span>환불 검토 중</span></li>
                        <li class="stateFlow"> &gt; </li>
                        <li class="state">승인완료<span>환불 검토완료</span></li>
                        <li class="stateFlow"> &gt; </li>
                        <li class="state">주문취소<span>환불처리완료</span></li>
                    </ul>
                    
                    <!--'배송예정' [→ '주문취소'] → '배송중' → '구매확정' [→ '반품/환불신청' → '승인대기' → '승인완료' → '주문취소'] -->
                </div> <!--orderstate_explain-->
                
            </div> <!--refundHistory-->
         </div>
            <!--****************** 환불내역 조회 부분 끝****************-->
	</body>
</html>