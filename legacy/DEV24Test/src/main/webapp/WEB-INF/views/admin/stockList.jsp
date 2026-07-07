<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
         <link rel="stylesheet" href="/resources/include/css/adminPage.css">
    
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
		 <!-- 부트스트랩  -->         
      <!--[if lt IE 9]>
      <script src="../js/html5shiv.js"></script>
      <![endif]-->
      
      <script type="text/javascript">
      
	      $(function(){
	    	  var d = new Date();

	    	  var month = d.getMonth()+1;
	    	  var day = d.getDate();
	    	  var time = d.getTime();
	    	  
	    	  var now = new Date(Date.now());
	    	  var formatted = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds();
	    	  if(now.getHours() == 0){
	    		  formatted = "0"+now.getHours()+":"+now.getMinutes()+":"+now.getSeconds();
	    	  }
	    	  if(now.getMinutes()<10){
	    		  formatted = now.getHours()+":"+"0"+now.getMinutes()+":"+now.getSeconds();
	    	  }
	    	  if(now.getSeconds()<10){
	    		  formatted = now.getHours()+":"+"0"+now.getMinutes()+":"+"0"+now.getSeconds();
	    	  }
	    		  
	    	  
	    	  // 20:10:58
	    	  var output = d.getFullYear() + '/' +
	    	      (month<10 ? '0' : '') + month + '/' +
	    	      (day<10 ? '0' : '') + day+':' +formatted;
	    	  
	    	  $("#date").text(output); //가공된 형식으로 출력한다. (2020/11/01[12:36:42])
	    	  //$("#date").text(d);
	    	  
	    	  $("#goHome").click(function(){
	    		  location.href="/admin/adminIndex";
	    	  });
	    	  
	    	  	    	      
	    	  // 재고 확인용 함수 chkStock 
	    	  
	    	 function chkStock(item, msg) {
				if($(item).val().replace(/\s/g,"")=="" || parseInt($(item).val()) <0 ) {
					alert(msg+" 입력해주세요.");
					item.val("");
					item.focus();
					return false; //값이 비어있을 경우 false를 반환
				} else {
					return true;
				}
			}
	    	  
	    	  //재고 등록 값 전달하는 ajax
	    	  
	    	  $("#submitBtn").click(function(){
	    		 console.log($("#b_num").val()); 
	    		 console.log($("#stk_qty").val()); 
	    		 console.log($("#adm_num").val()); 
	    		 console.log($("#stk_salp").val()); 
	    		 var stk_incp = $("#b_num").val();
	    		 
	    		 if(!chkSubmit("#stk_qty", "입고수량을")) return;
	    		 else if (!chkSubmit("#stk_salp", "판매가격을"))return;
	    		 else if(!chkStock("#stk_qty", "판매")) return;
	    			 
	    		 else{
	    			 $.ajax({
	    				url: "/admin/stockInsert", 
	    				type: "post",
	    				data: {
	    					b_num : $("#b_num").val(), 
	    					stk_qty: $("#stk_qty").val(), 
	    					adm_num: $("#adm_num").val(), 
	    					stk_salp: $("#stk_salp").val(), 
	    					stk_incp:stk_incp
	    				},
	    				dataType:"text",
	    				success:function(){
	    					alert("재고 입력 완료");
	    					location.href="/admin/stockList";
	    				},
	    			 });
	    		 }
	    	  });
	    	  
	    	 

				/*검색 대상이 변경될 때마다 처리 이번트*/
				$("#searchData").change(function(){
					if($("#search").val() == "all"){
						$("#keyword").val("전체 데이터 조회 합니다.");
					}else if ($("#search").val()!="all"){
						$("keyword").val();
						$("#keyword").focus();
					}
				});
	    	  
	    	  /*검색 버튼 클릭시 처리 이번트*/
	    	  
	    	  	//도서명, 작가, 도서코드 검색버튼
				$("#searchData").click(function(){
					if($("#search").val()!="all"){
						if(!chkSubmit("#keyword", "검색어를")) return;
					}
					goPage();
				});
	    	  
	    	  //도서 카테고리 검색 버튼
	    	  $("#searchStkCate").click(function(){
	    		 goCate(); 
	    	  });
	    	  
				$("#searchDate").click(function(){
					console.log($("#date_start").val()); 
						
					$("#dateSearch").attr({
						"method":"get", 
						"action":"/admin/stockList"
					});
						
					$("#dateSearch").submit();
				});
	    	  
	    	  
	    	  
	    	  //도서 제목을 클릭시 도서코드를 전달해주는 구문
	    	  $(".stkDetail").click(function(){
	    		 var stockcode= $(this).parents("tr").attr("data-num");
	    		 $("#stk_incp").val(stockcode);
	    		 console.log("도서 코드= "+ $("#stk_incp").val());
	    		 
	    		 $("#detailForm").attr({
						"method":"get", 
						"action":"/admin/stockDetail"
					});
					$("#detailForm").submit()
	    	  });
	    	  
	    	  
	    	  $(".stkbInfo").click(function(){
	    		  var stockcode= $(this).parents("tr").attr("data-num");
		    		 $("#stk_incp").val(stockcode);
		    		 console.log("도서 코드= "+ stockcode);
	    	  });
	    	  
	    	  
	    	  
	    	  $("#stockInsertBtn").click(function(){
	    		 location.href="/admin/stockInsertForm";
	    	  });
	    	  
	    	  
	    	  
	      });
	      
	      function selectBstate(){
	    	 $("#b_state").attr({
	    		"method":"get", 
	    		"action":"/admin/stockList"
	    	});
	    	$("#b_state").submit();
	      }
	      
	      /*검색 버튼을 위한 함수*/
	      function goPage(){
				$("#searchForm").attr({
					"method":"get", 
					"action":"/admin/stockList"
				});
				$("#searchForm").submit();
			}
			
	      /*책 소분류별 검색버튼을 위한 함수*/
	      function goCate(){
	    	  $("#categorySearch").attr({
					"method":"get", 
					"action":"/admin/stockList"
				});
				$("#categorySearch").submit();
	      }
      	
	      
	     
	     </script>
      
      
      <style type="text/css">
			#table{ padding:10px;}

			.stkDetail {
            cursor:pointer;
         	}
			
			td{ text-align: left;}
			
			#admin_search > div{
				float: left;
    			margin-right: 30px;
			}
			
      </style>
      
      
      
   </head>
   <body>
	 <!-- 여기서부터가 우리가 입력할 body 부분 시작. 재고 리스트를 여기에 출력 -->
	<div id="content_wrap"> 
		<div id="upper">
			<div class="center">
			
				<h2 id="tit">도서재고관리</h2>
			
				<div id="admin_search">
			   		<form id="detailForm" name="detailForm">
						<input type="hidden" id="stk_incp" name="stk_incp"/>
					</form>
	
					<div class="searchCategory form-inline">
						<form name="searchForm" id="searchForm" class="form-group">
							
							<label>검색조건</label>
							<select id="search" name="search" class="form-control">
								<option value="all">전체</option>
								<option value="b_name">도서명</option>
								<option value="b_author">작가</option>
								<option value="stk_incp">도서코드</option>
							</select> 
							<input type="text" id="keyword" name="keyword" placeholder="검색어/코드 를 입력해주세요" class="form-control"/>
							<button type="button" class="btn btn-primary btn-sm" id="searchData">검색</button>
						</form>
					</div> <!-- searchCategory -->
						
					<div class="searchCategory form-inline">
						<form id="categorySearch" name="categorySearch" class="form-group">
							<label for="category">도서 카테고리</label>
								<select name="category" id="category" class="form-control">
									<option value="pl">프로그래밍 언어</option>
									<option value="osdb">OS/데이터베이스</option>
									<option value="webp">웹프로그래밍</option>
									<option value="com">컴퓨터 입문/활용</option>
									<option value="net">네크워크/해킹/데이터베이스</option>
									<option value="it">IT 전문서</option>
									<option value="compt">컴퓨터 수험서</option>
									<option value="webc">웹/컴퓨터/입문 활용</option>
								</select>
							<input type="button" name="searchStkCate" id="searchStkCate" value="검색" class="btn btn-info" />
						</form>
					</div>
					
					<div class="searchCategory form-inline">
						<form id="dateSearch" name="dateSearch" class="form-group">
							<label for ="stk_regdate">등록일자 검색</label>
							<input type="date" id="stk_regdate" name="stk_regdate"/>
							<button type="button" class="btn btn-primary btn-sm" id="searchDate">검색</button>
						</form>
					</div>
					
					<div id="btnArea">
						<input type="button" value="재고등록" id="stockInsertBtn" class="btn btn-s btn-success"/>
					 	<input type="button" name="goHome" id="goHome" class="btn btn-s btn-success" value="관리자페이지 "/>
					</div> <!--btnArea-->	
				
				</div> <!-- admin_search -->
						
				</div><!-- center -->	
			</div> 
				<%-- 검색기능 끝 --%>
				
				
			<%-- 도서 재고 현황 테이블 시작 --%>
			
		<div id="table">			 			 
				<table class="table table-striped listTable" border="1">
					<thead>
					    <tr>
					    	<th>도서코드</th>
					    	<th>제목</th>
					    	<th>작가</th>
					    	<th>재고수량</th>
					    	<th>입고가격</th>
					    	<th>대분류</th>
					    	<th>소분류</th>
					    	<th>등록자</th>
					    	<th>등록일자</th>
					 	</tr>
				    </thead>
				    <tbody>
				    	<c:choose>
				    		<c:when test="${not empty stockList }">
						     	<c:forEach var="book" items="${stockList}" varStatus="status" >
						     	
						     	<%-- 도서 대분류 및 소분류명을 변환해주는 jstl 함수 --%>							    		
							    		<c:choose>
							    			<c:when test="${book.cateone_num == 1}">
							    				<c:set var="cateOne" scope="session" value="일반도서"/>		
							    			</c:when>
							    			<c:otherwise>
							    				<c:set var="cateOne" scope="session" value="ebook"/>
							    			</c:otherwise>
							    		</c:choose>
						     	
						     <%-- 도서 소분류코드 번호를 변환해주는 jstl 함수  --%>
							    		<c:choose>
							    			<c:when test="${book.catetwo_num == 1}">
							    				<c:set var="cateTwo" scope="session" value="프로그래밍 언어"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 2}">
							    				<c:set var="cateTwo" scope="session" value="OS/데이터베이스"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 3}">
							    				<c:set var="cateTwo" scope="session" value="웹프로그래밍"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 4}">
							    				<c:set var="cateTwo" scope="session" value="컴퓨터 입문/활용"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 5}">
							    				<c:set var="cateTwo" scope="session" value="네트워크/해킹/데이터베이스"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 6}">
							    				<c:set var="cateTwo" scope="session" value="IT 전문서"/>
							    			</c:when>
							    			<c:when test="${book.catetwo_num == 7}">
							    				<c:set var="cateTwo" scope="session" value="컴퓨터 수험서"/>
							    			</c:when>
							    			<c:otherwise>
							    				<c:set var="cateTwo" scope="session" value="웹/컴퓨터/입문 활용"/>
							    			</c:otherwise>
							    		</c:choose>	
						     
							    	<tr data-num="${book.stk_incp}">
							    		<td> ${book.stk_incp}</td>
							    		<td class="stkDetail">${book.b_name}</td>
							    		<td>${book.b_author }</td>
							    		<td>${book.stk_qty} 권</td>
							    		<td><fmt:formatNumber value="${book.stk_salp}"/></td>
							    		<td>${cateOne}</td>
							    		<td>${cateTwo}</td>
							    		<td>${book.adm_name}</td>
							    		<td>${book.stk_regdate}</td>
							    	</tr>
							    	</c:forEach>
							   </c:when>
							   <c:otherwise>
							   		<td colspan="9" class="text-center">현재 재고가 없는 책입니다.</td>
							   </c:otherwise> 	
				    	</c:choose>
				    </tbody>
				  </table>
			</div>
	 	
	</div> <!-- container -->
   </body>
</html>
    