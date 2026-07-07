<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>DEV 24 Purchase Detail Admin page</title>
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
         #admin_search{
         	float : right;
         }
         
         .listTable tbody tr:hover{
         	background-color:rgba(0,0,0,0.1);
         }
         </style>
		<script>
    
	        $(function(){
	        	// 환불 건의 결제 금액 식별(글자색 빨간색)
	            
	            /* 금액 콤마 찍기 */
				var l = $(".listTable").find("tbody tr").length;
				var sum = 0;

	            for(var i=0; i<l; i++){
	            	var price = $(".listTable").find("tbody tr").eq(i).find("td.td_price").text();
	                $(".listTable").find("tbody tr").eq(i).find("td.td_price").text(addComma(price));
	            	
	               /* 환불금액 표시 */
	            	var tr = $(".listTable > tbody > tr").eq(i);
	                var td = tr.find(".td_refund").text();
	                if(td!=""){ // 환불번호가 입력되어있으면!
	                    var text = tr.find(".td_price").text();
	                    tr.find(".td_price").css("color","red").text("-"+text);
	           		}else{
	           			sum += parseInt(unComma(price));
	           			$("#total_count").text(addComma(l)); // 전체 구매 횟수
	    				$("#total_price").text(addComma(sum)); // 전체 금액
	           		}
	              
	                
	                
	                /* 배송예정인 건 '배송중'처리 */
					var state = $(".listTable > tbody > tr").eq(i).find(".td_orderstate").text();
		            if(state == "preShipping"){
		            	$(".listTable > tbody > tr").eq(i).find(".td_orderstate").css({
		            		"font-weight":"bold",
	            			"color":"orange"
		            	}).hover(function(event){
		            		$(this).css({"color":"blue","cursor":"pointer"});
		            		
		            		var b_num = $(this).siblings("td.td_bnum").text();
		            		$(".listTable > tbody > tr").each(function(){
		            			if($(this).find("td.td_bnum").text() == b_num){
		            				$(this).find("td.td_orderstate").css({"color":"blue"});
		            			}
		            		});
		            		
		            	}, function(event){
		            		$(this).css("color","orange");
		            		
		            		var b_num = $(this).siblings("td.td_bnum").text();
		            		$(".listTable > tbody > tr").each(function(){
		            			if($(this).find("td.td_bnum").text() == b_num){
		            				$(this).find("td.td_orderstate").css({"color":"orange"});
		            			}
		            		});
		            	});
		            	
		            	
		            	$(".td_orderstate").click(function(){
		            		var pd_orderstate = "shipping";
							var b_num = $(this).siblings("td.td_bnum").text();
							var p_num = $(this).parents("tr").attr("data-num");
							var pd_num = $(this).siblings("td.td_pdnum").text();
							
							$.ajax({
								url:"/admin/orderstateUpdate",
								type:"get",
								data : "b_num="+b_num+"&pd_orderstate="+pd_orderstate+"&p_num="+p_num+"&pd_num="+pd_num,
								dataType:"text",
								error:function(){
									alert("시스템 오류. 관리자에게 문의하세요.");
								},
								success:function(result){
									console.log("result => "+result);
									location.href="/admin/pdetailList?p_num="+p_num;
								}
							});
		            	});
		            	
		            	
		            }
	            }
	           
				
	            
	            /* 메인으로 이동 버튼 */
				$("#goMainBtn").click(function(){
					location.href="/admin/adminIndex";
				});
	            
	            /* 구매관리 페이지로 이동 버튼 */
				$("#goPurchaseBtn").click(function(){
					location.href="/admin/purchaseList";
				});
	            
				/* 구매번호 출력 */
	            $("#pnum span").text("${p_num}");
	            console.log("p_num : ${p_num}");

				
				/* 키워드 검색 시 */
				$("#searchTextBtn").click(function(){
					if($("#search").val()!="all"){ // 검색 조건이 '전체'일 때 빼고는 유효성 체크!
						if(!chkData("#keyword", "검색어를"))return;
					}
					goPage(); // 검색 완료 후 purchaseList 페이지 재호출하는 함수
				});
				
				
				/* 검색 후 검색대상과 검색단어 출력 */
				if("${data.keyword}" != ""){
					$("#keyword").val("${data.keyword}");
					$("#search").val("${data.search}");
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
				
				
				
	        }); // 최상위 종료
	        
	        
	        /* 검색을 위한 실질적인 처리 함수 */
			function goPage(){
				if($("#search").val()=="all"){
					$("#keyword").val("");
				}
				
				
				$("#f_searchText").attr({
					"method" : "get",
					"action" : "/admin/pdetailList"
				});
				$("#f_searchText").submit();
			}
	        
	        
	    </script>
		

	</head>
	<body>
		<div id="container">
	       <div id="upper">
	            <div class="center">
	                <h2 id="tit">구매상세관리</h2>
	
	                <div id="admin_search">
	                    <form name="f_searchText" id="f_searchText" class="form-inline">
	                    	<input type="hidden" name="p_num" id="p_num" value="${p_num}" />	                    
	                        <div class="form-group">
	                            <label>검색조건</label>
	                            <select name="search" id="search" class="form-control">
	                                <option value="all">전체</option>
	                                <option value="pd_num">구매상세번호</option>
	                                <option value="b_num">도서번호</option>
	                                <option value="b_name">도서명</option>
	                                <option value="c_id">주문자ID</option>
	                                <option value="p_pmethod">결제방법</option>
	                                <option value="pd_orderdate">주문상태</option>
	                                <option value="rf_num">환불번호</option>
	                            </select>
	                            <input type="text" name="keyword" id="keyword" class="form-control" />
	                        </div>
	                        <input type="button" id="searchTextBtn" value="검색" class="btn btn-default" />
	                    </form>
	                </div><!-- admin_search -->
	
	                <div id="btnArea">
	                   <input type="button" class="btn btn-info" id="goPurchaseBtn" value="구매관리로 이동" />
	                   <input type="button" class="btn btn-success" id="goMainBtn" value="관리자페이지" />
	                </div><!--btnArea-->
	            </div><!--center-->
	        </div><!--upper-->
	        
	        
	        <div id="content_wrap">
	           
	            <table class="listTable table table-striped" border="1">
	           		<caption id="pnum">구매번호 : <span></span></caption> <!--p_num-->
	                <thead>
	                    <tr>
	                        <th>구매상세번호</th> <!--pd_num-->
	                        <th>도서번호</th> <!--b_num-->
	                        <th>도서명</th> <!--b_name-->
	                        <th>주문자ID</th>
	                        <th>결제방법</th>
	                        <th>수량</th> <!--무조건 1권-->
	                        <th>결제금액</th> <!--pd_price(단가)-->
	                        <th>구매날짜</th> <!--p_buydate-->
	                        <th>주문상태</th> <!--pdetail.pd_orderstate-->
	                        <th>환불번호</th> <!--주문상태 환불신청 건에 한해 rf_num-->
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty plist}">
	                			<c:forEach var="list" items="${plist}">
	                				<tr data-num="${list.p_num}">
				                        <td class="td_pdnum">${list.pd_num}</td>
				                        <td class="td_bnum">${list.b_num}</td>
				                        <td>${list.b_name}</td>
				                        <td>${list.c_id}</td>
				                        <td>${list.p_pmethod}</td>
				                        <td>${list.pd_qty}</td>
				                        <td class="td_price">${list.pd_price}</td>
				                        <td>${list.p_buydate}</td>
				                        <td class="td_orderstate">${list.pd_orderstate}</td>
				                        <c:choose>
					                        <c:when test="${list.rf_num == 0}">
					                        	<td class="td_refund"></td>
					                        </c:when>
					                        <c:otherwise>
						                        <td class="td_refund">${list.rf_num}</td>
					                        </c:otherwise>
				                        </c:choose>
				                    </tr>
	                			</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr>
	                				<td colspan="10">내역이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>
	                </tbody>
	            </table>
	        
	        </div><!--content_wrap-->
	        
	        
	        <div id="down">
	            <div class="center">
	                <label class="totalCount">총 도서개수 : <strong><span id="total_count"></span>권</strong></label>
	                <label class="totalPrice">총 결제금액 : <strong><span id="total_price"></span>원</strong></label>
	            </div><!--center-->
	        </div><!--down-->
	        
	    </div><!--container-->
	</body>
</html>