<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>stockInsertForm</title>
		
		 <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
         <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
    	<link rel="stylesheet" href="/resources/include/css/adminPage.css">
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
         
         <style type="text/css">
         	
         	#content_wrap{
         		width:50%;
         		margin-left:300px;
         	}
         	
         	.content{
         		margin-top:20px;
         	}
         	
         	.content label{
         		display : block;
         		margin-top:20px;
         	}
         	#bstatebtn{margin-top:-5px;}
         	#b_stateSelect, #stk_qty, #admin_name{width:200px; display : inline;}
         	#stk_qty, #admin_name{margin-left:10px;}
         </style>
         
         <script type="text/javascript">
         	$(function(){
         		
       	       var date = new Date();
    	       $(".date").text(date);   
         	   $("#today").text(date);   
         		
         		/*재고 등록 버튼 기능*/
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
         		
         		$("#bstatebtn").click(function(){
         			
         			//console.log("에라이 씨팔");
         			console.log($("#b_state").val());
         			
         			var b_state;
         			
         			if ($("#b_stateSelect").val() == 'all')
         				b_state = "";
         			if ($("#b_stateSelect").val() == 'unreg')
         				b_state = "unreg";
         			
         			$("#b_state").val(b_state);
         			
         			location.href="/admin/stockInsertForm?b_state="+b_state;
         		});
         		
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
         		
         	});
         	
         </script>
         
        
		
	</head>
	
	<body>
		<!-- <div class="container-fluid"> -->
		<!-- <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"> -->
		<div id="container"> 
			<div id="content_wrap">
				<%-- <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"> --%>
				<form id="hiddenForm">
					<input type="hidden" name="b_state" id="b_state" value="" />
				</form>
			
				
				<form role="form" name="stockInsertForm" >
						<h2>재고등록 화면</h2>
					<div class="form-group content">
						<label>상품정보 등록 상태</label>
						<select class="form-control" name="b_stateSelect" id="b_stateSelect">
							<option value="all">등록</option>
							<option value="unreg" id="unreg">미등록</option>
						</select>
						<input type="button" value="선택" id="bstatebtn" name="bstatebtn" class="btn btn-default" /> 
						
						<label>상품코드</label>
						<select class="form-control" name="b_num" id="b_num">
							<c:choose>
								<c:when test="${not empty bookstockList}">
									<c:forEach var="bookinfo" items="${bookstockList}">
										<option value="${bookinfo.b_num}">${bookinfo.b_name} /
											${bookinfo.b_author} / ${bookinfo.b_pub}</option>
									</c:forEach>
								</c:when>
							</c:choose>
						</select>
					</div>
					
					<div class="form-group">
						<label>입고수량</label> <input class="form-control" placeholder="재고수량 입력"
							type="number" min="1" name="stk_qty" id="stk_qty">
					</div>
					
					<div class="form-group">
						<label for="exampleInputFile">재고 등록자명</label> <input type="hidden"value="${adm_num}" name="adm_num" id="adm_num" />
						<p id="admin_name">${adm_name}관리자</p>
						<p class="help-block" style="color: red;">도서의 재고는 한번 입력시 수정이
							불가합니다. 신중히 등록을 해주세요</p>
					</div>
			
					<div class="form-group">
						<label>등록일자</label>
						<p class="date"></p>
					</div>
			
					<div class="form-group">
						<label>입고가격</label> <input type="number" class="form-control"
							placeholder="판매가격 입력 " name="stk_salp" id="stk_salp" />
					</div>
					<input type="button" class="btn btn-default" value="도서등록"
						name="submitBtn" id="submitBtn" />
				
				</form>
			</div><!-- content_wrap -->
		</div><!-- container -->
				
	</body>
</html>