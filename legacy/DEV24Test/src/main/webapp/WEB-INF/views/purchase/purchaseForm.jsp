<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
		
		<link rel="stylesheet" href="/resources/include/css/style_purchase.css">
		<link rel="stylesheet" href="/resources/include/css/style_cart.css">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
    	<style>
    	.price_text span{
    		font-size : 25px;
    	}
    	</style>
    	<script type="text/javascript">
    		
    		$(function(){
    			/* 각 상품 금액 콤마 찍기 */
    			var l = $(".t_orderItems tbody tr").length;
    			var price = 0;
    			var sumPrice = 0;
    			var bookp = 0;
    			var ebookp = 0;
    			var total = 0;
    			for(var i=0; i<l; i++){
    				price = $(".t_orderItems tbody tr").eq(i).find(".td_price span").text();
    				$(".t_orderItems tbody tr").eq(i).find(".td_price span").text(addComma(price));
    				
    				sumPrice = $(".t_orderItems tbody tr").eq(i).find(".td_sumPrice span").text();
    				$(".t_orderItems tbody tr").eq(i).find(".td_sumPrice span").text(addComma(sumPrice));
    			}
    			
    			/* 상품 합계 구하기 */
    			for(var i=0; i<l; i++){
	    			var cate = $(".t_orderItems tbody tr").eq(i).attr("data-cate");
	    			//console.log("cate : "+cate);
	    			sumPrice = $(".t_orderItems tbody tr").eq(i).find(".td_sumPrice span").text();
	    			
    				if(cate == 1){
    					bookp += parseInt(unComma(sumPrice));
    				}else if(cate == 2){
    					ebookp += parseInt(unComma(sumPrice));
    				}
    			}
    			total = bookp + ebookp; // 최종 결제 금액
           		$("#totalprice_wrap").find(".book_price span").text(addComma(bookp));
           		$("#totalprice_wrap").find(".ebook_price span").text(addComma(ebookp));
           		$("#totalprice_wrap").find(".last_price span").text(addComma(total));
           		$(".finalprice strong span").text(addComma(total));
           		
           		
           		/* 결제하기 버튼 클릭 시 */
           		$("#purchaseSuccessBtn").click(function(){
           			
           			// 유효성 체크
           			if(!chkData("#p_receiver", "받는분 이름을")) return;
           			if(!chkData("#p_zipcode", "우편번호를")) return;
           			if(!chkData("#p_address", "배송주소를")) return;
           			if(!chkData("#p_receivephone1", "받는분 연락처를")) return;
           			if(!chkData("#p_receivephone2", "받는분 연락처를")) return;
           			if(!chkData("#p_receivephone3", "받는분 연락처를")) return;
           			if(!chkData("#p_sender", "보내는분 이름을")) return;
           			if(!chkData("#p_senderphone1", "보내는분 연락처를")) return;
           			if(!chkData("#p_senderphone2", "보내는분 연락처를")) return;
           			if(!chkData("#p_senderphone3", "보내는분 연락처를")) return;
           			if(!chkData("#c_email", "보내는분 메일주소를")) return;
           			if(!$("input[name='p_pmethod']").is(":checked")){
           				alert("결제방법을 선택해주세요.");
           				return;
           			}
           			if(!$("#agreeCheck").is(":checked")){
           				alert("구매 동의사항에 체크해주세요.");
           				return;
           			}
           			
           			// 전화번호 조립하기(받는분)
           			var rph1 = $("#p_receivephone1").val();
        			var rph2 = $("#p_receivephone2").val();
        			var rph3 = $("#p_receivephone3").val();
        			var full_rph = rph1+"-"+rph2+"-"+rph3;
        			$("#p_receivephone").val(full_rph);
        			console.log($("#p_receivephone").val());
        			
        			// 전화번호 조립하기(보내는분)
           			var sph1 = $("#p_senderphone1").val();
        			var sph2 = $("#p_senderphone2").val();
        			var sph3 = $("#p_senderphone3").val();
        			var full_sph = sph1+"-"+sph2+"-"+sph3;
        			$("#p_senderphone").val(full_sph);
        			console.log($("#p_senderphone").val());
        			
        			
        			// ****** 결제하기 로직 시작
        			var pdvo;
        			var pdvoList = new Array();
        			var crtnumList = new Array();
        			
        			
        			var p = $(".finalprice strong span").text();
           			p = unComma(p);
           			$("#p_price").val(parseInt(p));
       				
           			// purchase
					var p_receiver = {"p_receiver" : $("#p_receiver").val()};
					var p_price = {"p_price" : $("#p_price").val()};
					var p_zipcode = {"p_zipcode" : $("#p_zipcode").val()};
					var p_pmethod = {"p_pmethod" : $("input[name='p_pmethod']:checked").val()};
					var p_address = {"p_address" : $("#p_address").val()};
					var p_sender = {"p_sender" : $("#p_sender").val()};
					var p_receivephone = {"p_receivephone": $("#p_receivephone").val()};
					var p_senderphone = {"p_senderphone" : $("#p_senderphone").val()};
					
					//console.log(p_price);
					//console.log(typeof(p_price));
					//console.log("p_pmethod : " + p_pmethod);
					
					pdvoList.push(p_receiver);
					pdvoList.push(p_price);
					pdvoList.push(p_zipcode);
					pdvoList.push(p_pmethod);
					pdvoList.push(p_address);
					pdvoList.push(p_sender);
					pdvoList.push(p_receivephone);
					pdvoList.push(p_senderphone);
					
        			for(var i=0; i<l; i++){ // pdetail
        				pdvo = new Object();
        			
        				var crt_num = $(".t_orderItems tbody tr").eq(i).attr("data-num");
        				var b_num = $(".t_orderItems tbody tr").eq(i).attr("data-bnum");
        				var pd_price = $(".t_orderItems tbody tr").eq(i).find(".td_sumPrice span").text();
        				pd_price = unComma(pd_price);
        				var pd_qty = $(".t_orderItems tbody tr").eq(i).find(".td_qty").text();
        				console.log("pd_qty : "+typeof(crt_qty));
        				console.log("b_num : "+b_num);
        				console.log("pd_price : " + pd_price);
        				pd_qty = parseInt(pd_qty);
        				
        				pdvo.b_num = b_num;
        				pdvo.p_num = b_num;
        				pdvo.pd_price = pd_price;
        				pdvo.pd_qty = pd_qty;
        				
        				pdvoList.push(pdvo);
        				
        				var c = {"crt_num":crt_num};
        				crtnumList.push(c);
        				
        			}
    	            
    	            var data = JSON.stringify(pdvoList);
    	            console.log(data); 
    	            
    	            order(data);
           		}); // 결제 버튼 이벤트 종료

    		}); // 최상위 종료
    		
    		
    		/* 체크된 상품 주문하기 눌렀을 때 */
	   		 function order(data){
	            $.ajax({
	               url : "/purchase/purchaseInsert",
	               type : "post",
	               data : data,
	               headers : {
	                  "Content-Type" : "application/json",
	                  "X-HTTP-Method-Override" : "POST"
	               }, 
	               dataType : "text",
	               success: function (result) {

	            	   $.ajax({
	    	               url : "/purchase/pdetailInsert",
	    	               type : "post",
	    	               data : data,
	    	               headers : {
	    	                  "Content-Type" : "application/json",
	    	                  "X-HTTP-Method-Override" : "POST"
	    	               }, 
	    	               dataType : "text",
	    	               success: function (result) {
	    	            	   console.log("result :"+result);
    	            			
    	            			var crtnumList = new Array();
    	            			var l = $(".t_orderItems tbody tr").length;
    	            			console.log(l);
	    	            	   for(var i=0; i<l; i++){ // crt_num
	    	        				var crt_num = $(".t_orderItems tbody tr").eq(i).attr("data-num");
	    	        				var c = {"crt_num":crt_num};
	    	        				crtnumList.push(c);
	    	        			}
	    	            	   var cartNum = JSON.stringify(crtnumList);
	    	            	   console.log(cartNum);
	    	            	   $.ajax({
	    	    	               url : "/purchase/purchasedItemDelete",
	    	    	               type : "post",
	    	    	               data : cartNum,
	    	    	               headers : {
	    	    	                  "Content-Type" : "application/json",
	    	    	                  "X-HTTP-Method-Override" : "POST"
	    	    	               }, 
	    	    	               dataType : "text",
	    	    	               success: function (result) {
	    	    	            	   console.log("result :"+result);
	        	            			// 폼 데이터 전달
	        	            			$("#f_purchase").attr({
	        	            				"action":"/purchase/purchasefinish",
	        	            				"method":"post"
	        	            			});
	        	            			$("#f_purchase").submit();
	    	    	            	   
	    	    	               },
	    	    	               error : function(){
	    	    	                  alert("장바구니 삭제 쪽 - 시스템 오류 발생. \n관리자에게 문의해 주세요.");
	    	    	               }
	    	    	            });
	    	            	   
	    	               },
	    	               error : function(){
	    	                  alert("구매상세 쪽 - 시스템 오류 발생. \n관리자에게 문의해 주세요.");
	    	               }
	    	            });
	            	   
	               },
	               error : function(){
	                  alert("시스템 오류 발생. \n관리자에게 문의해 주세요.");
	               }
	            });
	         };
    	
    	</script>

	</head>
	<body>
		<div id="content_wrap">
	       <div class="purchase_wrap">
	          <!--*** 상품확인 영역 ***-->
	           <div class="tit_cart">
	                <h3>상품확인</h3>
	            </div>
	            
	            <table class="table t_orderItems" border="1">
	               <colgroup>
	                   <col width="50%" /> <!--상품명-->
	                   <col width="10%" /> <!--가격-->
	                   <col width="10%" /> <!--수량-->
	                   <col width="10%" /> <!--합계-->
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>상품명</th>
	                        <th>가격</th>
	                        <th>수량</th>
	                        <th>합계</th>
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty list}">
	                			<c:forEach var="item" items="${list}">
			                		<tr class="tr" data-num="${item.crt_num}" data-cate="${item.cateone_num}" data-bnum="${item.b_num}">
				                        <td class="td_book">
				                            <span class="td_bookimg"><img src="${item.listcover_imgurl}"/></span>
				                            <span class="td_bookname">${item.b_name}</span>
				                        </td>
				                        <td class="td_price"><span>${item.b_price}</span>원</td>
				                        <td class="td_qty">${item.crt_qty}</td>
				                        <td class="td_sumPrice"><span>${item.crt_price}</span>원</td>
				                    </tr>
			                	</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr>
	                				<td colspan="5">담긴 상품이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>      
	                </tbody>
	            </table>
	            
	            <div id="totalprice_wrap">
	               <div class="price">
	               <p class="price_tit">총 도서금액</p>
	               <p class="price_text book_price"><span></span>원</p>
	           </div>
	           <div class="calc">+</div>
	           <div class="price">
	               <p class="price_tit">총 eBook금액</p>
	               <p class="price_text ebook_price"><span></span>원</p>
	           </div>
	           <div class="calc">=</div>
	           <div class="price">
	               <p class="price_tit">총 결제금액</p>
	               <p class="price_text last_price"><span></span>원</p>
	           </div>
	           </div><!--totalprice_wrap-->
	           
	           <%-- 배송주소 영역 --%>
			   <jsp:include page="deliveryInfo.jsp" />

	            <!--*** 최종확인 영역 ***-->
	           <div id="lastcheck_wrap">
	               <div class="finalprice">최종 결제금액 <strong><span></span>원</strong></div>
	               <p>주문하실 상품, 가격, 배송정보, 할인정보 등을 확인하였으면, 구매에 동의하시겠습니까?</p>
	               <label><input type="checkbox" name="agreeCheck" id="agreeCheck" />동의합니다.(전자상거래법 제8조 제2항)</label>
	           </div><!--lastcheck_wrap-->
	           
	           
	           
	           <input type="button" name="purchaseSuccessBtn" id="purchaseSuccessBtn" class="btn btn-success" value="결제하기" />
	       </div><!--purchase_wrap-->
	
	    </div> <!-- content_wrap -->
	</body>
</html>