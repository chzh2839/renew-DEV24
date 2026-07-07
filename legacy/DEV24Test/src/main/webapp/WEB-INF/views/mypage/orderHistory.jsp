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
		
		<!-- <link rel="stylesheet" href="/resources/include/css/style_mypage.css" /> -->
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
				
				
				
				/* 구매확정 버튼 처리 */
				$(".pConfirmBtn").click(function(){
					var b_num = $(this).parents("tr").attr("data-num");
					var rf_num = $(this).parents("tr").attr("data-pd");
					var pd_num = $(this).parents("tr").attr("data-pd");
					var rf_price = $(this).parents("td").siblings("td.td_price").text();
					var rf_qty = $(this).parents("td").siblings("td.td_qty").text();
					var p_num = $(this).parents("td").siblings("td.td_num").text();
					rf_price = unComma(rf_price);
					rf_qty = unComma(rf_qty);
					
					$("#pd_orderstate").val("pConfirm");
					$("#b_num").val(b_num);
					$("#rf_num").val(rf_num);
					$("#pd_num").val(pd_num);
					$("#rf_price").val(rf_price);
					$("#rf_qty").val(rf_qty);
					$("#p_num").val(p_num);
					
					updateState(1);
				});
				
				/* 구매취소 버튼 처리 */
				$(".orderCancelBtn").click(function(){
					var b_num = $(this).parents("tr").attr("data-num");
					var rf_num = $(this).parents("tr").attr("data-pd");
					var pd_num = $(this).parents("tr").attr("data-pd");
					var rf_price = $(this).parents("td").siblings("td.td_price").text();
					var rf_qty = $(this).parents("td").siblings("td.td_qty").text();
					var p_num = $(this).parents("td").siblings("td.td_num").text();
					rf_price = unComma(rf_price);
					rf_qty = unComma(rf_qty);
					
					$("#pd_orderstate").val("cancel");
					$("#b_num").val(b_num);
					$("#rf_num").val(rf_num);
					$("#pd_num").val(pd_num);
					$("#rf_price").val(rf_price);
					$("#rf_qty").val(rf_qty);
					$("#p_num").val(p_num);
					
					updateState(2);
				});
				
				
				/* 환불신청 버튼 처리 */
				$(".refundInsertFormBtn").click(function(){
					var p_num = $(this).parents("td").siblings("td.td_num").text();
					var b_num = $(this).parents("tr").attr("data-num");
					var pd_num = $(this).parents("tr").attr("data-pd");
						
					if(confirm("해당 상품의 환불신청을 진행하시겠습니까?")){
						location.href="/refund/refundForm?p_num="+p_num+"&b_num="+b_num+"&pd_num="+pd_num;
					}
					
				});
				
				
				/* 리뷰 작성 버튼 처리 */
				$(".reviewInsertFormBtn").click(function(){
					var b_num = $(this).parents("tr").attr("data-num");
					var pd_num = $(this).parents("tr").attr("data-pd");
					console.log(b_num);
					
					location.href="/review/reviewForm?b_num="+b_num+"&pd_num="+pd_num;
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
					"action" : "/mypage/orderHistory"
				});
				$("#f_search").submit();
			}
			
			
			/* 주문상태 수정 ajax처리 함수 */
			function updateState(btnNum){
				stateUpdateBtn = btnNum;
				console.log("stateUpdateBtn : "+stateUpdateBtn);
				
				var pd_orderstate = $("#pd_orderstate").val();
				var b_num = $("#b_num").val();
				var p_num = $("#p_num").val();
				var rf_num = $("#rf_num").val();
				var pd_num = $("#pd_num").val();
				var rf_price = $("#rf_price").val();
				var rf_qty = $("#rf_qty").val();

				if(stateUpdateBtn == 1){ // 구매확정 처리
					$.ajax({
						url:"/mypage/orderstateUpdate",
						type:"get",
						data : "pd_orderstate="+pd_orderstate+"&b_num="+b_num+"&p_num="+p_num+"&pd_num="+pd_num,
						dataType:"text",
						error:function(){
							alert("시스템 오류. 관리자에게 문의하세요.");
						},
						success:function(result){
							stateUpdateBtn = 0;
							console.log("result => "+result);
							location.href="/mypage/orderHistory";
						}
					});
				}else if(stateUpdateBtn == 2){ // 주문취소 처리
					$.ajax({
						url:"/mypage/orderstateUpdate",
						type:"get",
						data: "pd_orderstate="+pd_orderstate+"&b_num="+b_num+"&p_num="+p_num+"&rf_num="+rf_num+"&rf_price="+rf_price+"&rf_qty="+rf_qty+"&pd_num="+pd_num,
						dataType:"text",
						error:function(){
							alert("시스템 오류. 관리자에게 문의하세요.");
						},
						success:function(result){
							stateUpdateBtn = 0;
							console.log("result => "+result);
							location.href="/mypage/orderHistory";
						}
					});
				}
			}
			
		</script>


	</head>
	<body>
		<div id="content_mypage">
			<form name="f_updateState" id="f_updateState">
			<!-- 구매취소로 인한 환불 테이블 추가 -->
				<!-- <input type="hidden" name="rf_orderstate" id="rf_orderstate" value="cancel" /> -->
				<input type="hidden" name="b_num" id="b_num" value="" />
				<input type="hidden" name="rf_num" id="rf_num" value="" />
				<input type="hidden" name="rf_price" id="rf_price" value="" />
				<input type="hidden" name="rf_qty" id="rf_qty" value="" />

			<!-- 	구매확정으로 인한 주문상태 변경 -->
				<input type="hidden" name="pd_orderstate" id="pd_orderstate" value="" />
				<input type="hidden" name="p_num" id="p_num" value="" />
				<input type="hidden" name="pd_num" id="pd_num" value="" />
				
			</form>
            
            <!--****************** 주문내역 조회 부분 시작****************-->
            
            <div id="orderHistory">
                <div class="tit_mypage">
                    <h3>주문내역조회</h3>
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
                               <option value="all">주문전체</option>
                               <option value="p_sender">주문자</option>
                               <option value="p_receiver">수령자</option>
                               <option value="p_num">주문번호</option>
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
                        <option value="preShipping">배송예정</option>
                        <option value="shipping">배송중</option>
                        <option value="pConfirm">구매확정</option>
                    </select>
                </div> <!--orderby 주문일자 정렬기준-->
              </form>
                
                <table class="table" border="1">
                	<colgroup>
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="20%" /> 
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	                   <col width="10%" />
	               </colgroup>
                    <thead>
                        <tr>
                            <th>주문번호</th>
                            <th>주문일자</th>
                            <th>주문내역</th>
                            <th>주문금액</th>
                            <th>주문수량</th>
                            <th>주문상태</th>
                            <th>주문자</th>
                            <th>수령자</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    	<c:choose>
                    		<c:when test="${not empty ohvo}">
                    			<c:forEach var="ohvo" items="${ohvo}">
                    				<tr data-num="${ohvo.b_num}" data-pd="${ohvo.pd_num}">
			                            <td class="td_num">${ohvo.p_num}</td>
			                            <td>${ohvo.p_buydate}</td>
			                            <td class="td_title">${ohvo.b_name}</td>
			                            <td class="td_price"><fmt:formatNumber value="${ohvo.pd_price}" pattern="#,###" /></td>
			                            <td class="td_qty"><fmt:formatNumber value="${ohvo.pd_qty}" pattern="#,###" /></td>
			                            <td class="td_orderdate">
			                            	<c:choose>
				                           	<c:when test="${ohvo.pd_orderstate == 'preShipping'}">
				                           		배송예정
				                                <input type="button" class="btn btn-default orderCancelBtn" value="구매취소" />
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'shipping'}">
				                           		배송중
				                                <input type="button" class="btn btn-info refundInsertFormBtn" value="환불신청" />
				                                <input type="button" class="btn btn-default pConfirmBtn" value="구매확정" />
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'pConfirm'}">
				                           		구매확정
				                                <input type="button" class="btn btn-info refundInsertFormBtn" value="환불신청" />
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'reRequest'}">
				                           		환불승인대기
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'cancel'}">
				                           		주문취소
				                           	</c:when>
				                           	<c:otherwise>
				                           		승인완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                            <td>${ohvo.p_sender}</td>
			                            <td>${ohvo.p_receiver}</td>
			                            <td class="td_buttons">
			                            	<c:if test="${ohvo.pd_orderstate == 'pConfirm'}">
			                            		<input type="button" class="btn btn-success reviewInsertFormBtn" value="리뷰작성" /> <!--구매확정 시에만 가능-->
			                            	</c:if>
			                            </td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="9">최근 주문내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                    </tbody>
                </table>
                
                
                <div id="orderstate_explain">
                    <div id="tit_explain"><span>주문상태</span></div>
                    
                    <ul>
                        <li class="state">배송예정<span>주문취소가능</span></li>
                        <li class="stateFlow"> &gt; </li>
                        <li class="state">배송중<span>환불신청가능</span></li>
                        <li class="stateFlow"> &gt; </li>
                        <li class="state">구매확정<span>환불신청가능</span></li>
                    </ul>
                    
                    <!--'배송예정' [→ '주문취소'] → '배송중' → '구매확정' [→ '반품/환불신청' → '승인대기' → '승인완료' → '주문취소'] -->
                </div> <!--orderstate_explain-->
                
            </div> <!--orderHistory-->
            
            <!--****************** 주문내역 조회 부분 끝****************-->
            
        </div><!--content_mypage-->
	</body>
</html>