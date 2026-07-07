<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>DEV 24 Refund Admin page</title>
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
		<script>
    
	        $(function(){
	        	var rows = $(".listTable > tbody > tr").length;
	            console.log(rows);
	            
	            // 환불승인대기 건 식별하기
	            for(var i=0; i<rows; i++){
	                var orderstate = $("tbody td.td_orderstate").eq(i);
	                if(orderstate.text()=="reRequest"){
	                    orderstate.css({
	                        "color":"red",
	                        "font-weight":"bold"
	                    });
	                };
	            };
	            
	            
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
				
				
				/* 승인처리 버튼 처리 */
				$(".refundConfirmBtn").click(function(){
					var rf_num = $(this).parents("td").siblings(".td_rfnum").text();
					var rf_orderstate = "rfConfirm";
					console.log(rf_num);
					
					$.ajax({
						url:"/admin/refundStateUpdate",
						type:"get",
						data : "rf_num="+rf_num+"&rf_orderstate="+rf_orderstate,
						dataType:"text",
						error:function(){
							alert("시스템 오류. 관리자에게 문의하세요.");
						},
						success:function(result){
							console.log("result => "+result);
							location.href="/admin/refundList";
						}
					});
					
				});
				
				
				/* 주문취소완료 버튼 처리 */
				$(".orderCancelBtn").click(function(){
					var rf_num = $(this).parents("td").siblings(".td_rfnum").text();
					var rf_orderstate = "cancel";
					console.log(rf_num);
					
					$.ajax({
						url:"/admin/refundStateUpdate",
						type:"get",
						data : "rf_num="+rf_num+"&rf_orderstate="+rf_orderstate,
						dataType:"text",
						error:function(){
							alert("시스템 오류. 관리자에게 문의하세요.");
						},
						success:function(result){
							console.log("result => "+result);
							location.href="/admin/refundList";
						}
					});
					
				});
				

				
	        }); // 최상위 종료
	        
	        /* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				
				$("#f_searchText").attr({
					"method" : "get",
					"action" : "/admin/refundList"
				});
				$("#f_searchText").submit();
			}
	        
    </script>
		

	</head>
	<body>
		<div id="container">
	       <div id="upper">
	            <div class="center">
	                <h2 id="tit">환불관리</h2>
	
	                <div id="admin_search">
	                    <form name="f_searchText" id="f_searchText" class="form-inline">
	                        <div class="form-group">
	                            <label>검색조건</label>
	                            <select name="search" id="search" class="form-control">
	                                <option value="all">전체</option>
	                                <option value="rf_num">환불번호</option>
	                                <option value="b_num">도서번호</option>
	                                <option value="b_name">도서명</option>
	                                <option value="c_id">주문자ID</option>
	                                <option value="rf_reason">환불사유</option>
	                                <option value="rf_orderdate">주문상태</option>
	                            </select>
	                            <input type="text" name="keyword" id="keyword" class="form-control" />
	                        </div>
	                        <div class="form-group">
	                            <label>승인날짜</label>
	                            <input type="date" name="date_start" id="date_start" class="form-control" /> ~ 
	                            <input type="date" name="date_end" id="date_end" class="form-control" />
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
	                   <col width="21%" /> 
	                   <col width="7%" />
	                   <col width="8%" />
	                   <col width="20%" />
	                   <col width="8%" />
	                   <col width="10%" />
	                   <col width="10%" />
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>환불번호</th> <!--rf_num-->
	                        <th>도서번호</th> <!--b_num-->
	                        <th>도서명</th> <!--b_name-->
	                        <th>주문자ID</th>
	                        <th>환불금액</th> <!--rf_price(단가)-->
	                        <th>환불사유</th> <!--rf_reason-->
	                        <th>승인날짜</th> <!--rf_confirmdate-->
	                        <th>주문상태</th> <!--rf_orderstate-->
	                        <th></th>
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty list}">
	                			<c:forEach var="list" items="${list}">
	                				<tr>
				                        <td class="td_rfnum">${list.rf_num}</td>
				                        <td>${list.b_num}</td>
				                        <td>${list.b_name}</td>
				                        <td>${list.c_id}</td>
				                        <td class="td_price">${list.rf_price}</td>
				                        <td class="text-left">${list.rf_reason}</td>
				                        <td>${list.rf_confirmdate}</td>
				                        <td class="td_orderstate">${list.rf_orderstate}</td>
				                        <td>
				                        	<c:choose>
				                        		<c:when test="${list.rf_orderstate == 'reRequest'}">
				                        			<input type="button" value="승인처리" class="btn btn-primary refundConfirmBtn" />
				                        		</c:when>
				                        		<c:when test="${list.rf_orderstate == 'rfConfirm'}">
				                        			<input type="button" value="취소처리" class="btn btn-primary orderCancelBtn" />
				                        		</c:when>
				                        		<c:otherwise>
				                        		</c:otherwise>
				                        	</c:choose>
				                        </td>
				                    </tr>
	                			</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr>
	                				<td colspan="9">내역이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>
	                </tbody>
	            </table>
	        
	        </div><!--content_wrap-->
	        
	        
	        <div id="down">
	            <div class="center">
	                <label class="totalCount">총 환불건수 : <strong><span id="total_count"></span>건</strong></label>
	                <label class="totalPrice">총 환불금액 : <strong><span id="total_price"></span>원</strong></label>
	            </div><!--center-->
	        </div><!--down-->
	        
	    </div><!--container-->
	</body>
</html>