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
		
		<link rel="stylesheet" href="/resources/include/css/style_cart.css">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
    	<style>
    	.price_text span{
    		font-size:25px;
    	}
    	</style>
    	<script>
    		$(function(){
    			checkAllDefault("#book_table", "${cartList1}");
    			checkAllDefault("#ebook_table", "${cartList2}");    			
    			priceFormat("#book_table");
    			priceFormat("#ebook_table");
    			lastPriceSum();
				    			
    			/* checkAll을 체크하면 상품 전체 체크하기 */
    			$(".checkAll").click(function(){
    				var chk = $(this).prop("checked");
    				var table = $(this).parents("table").attr("id");
    				//console.log(chk);
    				if(chk){ // 전체 체크
    					$(this).parents("table").find(".checkItem").prop("checked", true);
    					sumItem("#"+table);
    				}else{ // 전체 체크 해제
    					$(this).parents("table").find(".checkItem").prop("checked", false);
    					sumItem("#"+table);
    				}
    				lastPriceSum();
    			});
    			
    			
    			/* 체크박스를 하나씩 누를 때 이벤트 */
    			$(".checkItem").click(function(){
    				// 전체선택 체크박스를 풀기
    				$(this).parents("table").find(".checkAll").prop("checked", false);
    				
    				var price = $(this).parents("tr").find(".td_price span").text();
					var lastprice = $(this).parents("table").find(".lastrow span").text();
					price = price.replace(",", "");
					price = parseInt(price);
					lastprice = lastprice.replace(",", "");
					lastprice = parseInt(lastprice);
					
					console.log("price : "+price);
					console.log("lastprice : "+lastprice);
    				
    				if($(this).prop("checked")){ // 체크한 상품 금액 더하기
    					lastprice = lastprice + price;
    					lastprice= addComma(lastprice);
    					$(this).parents("table").find(".lastrow span").text(lastprice);

    				}else{ // 체크 해제한 상품 금액 빼기
    					lastprice = lastprice - price;
    					lastprice = addComma(lastprice);
    					$(this).parents("table").find(".lastrow span").text(lastprice);
    					
    				}
    				lastPriceSum();
			    });
    			
    			
    			/**** 장바구니 수량 변경(ajax) ****/
    			$(".updateQtyBtn").click(function(){
    				var crt_num = $(this).parents("tr").attr("data-num");
    				//console.log(crt_num);
    				
    				// 수량 유효성 체크
    				var qty = $(this).siblings(".checkQty").val();
    				if(qty<=0){
    					alert("0보다 작은 수량을 입력할 수 없습니다.");
    					return;
    				}
    				else{
    					$.ajax({
    						url : "/cart/"+crt_num,
    						type : "put",
    						headers : {
    							"Content-Type" : "application/json",
    							"X-HTTP-Method-Override" : "PUT"
    						},
    						data : JSON.stringify({ // 변경된 수량 json형태로 전달
    							crt_qty : qty
    						}),
    						dataType : "text",
    						error : function(){
    							alert("시스템 오류. 관리자에게 문의하세요.");
    						},
    						success : function(result){
    							console.log("result : "+result);
    							if(result=="SUCCESS"){
    								location.href="/cart/cartList";
    							}
    						}
    					});
    				}

    			});
    			
    			
    			
    			/* 상품 삭제버튼 클릭 시 처리 이벤트(ajax) */
    			$(".deleteItemBtn").click(function(){
    				var crt_num = $(this).parents("tr").attr("data-num");
    				console.log(crt_num);
    				
    				if(confirm("해당 상품을 삭제하시겠습니까?")){
    					$.ajax({
        					url : "/cart/"+crt_num,
        					type : "delete",
        					headers : {
        						"X-HTTP-Method-Override" : "DELETE"
        					},
        					datatype : "text",
        					error : function(){
        						alert("시스템 오류. 관리자에게 문의하세요.");
        					},
        					success : function(result){
        						alert("삭제되었습니다.");
        						location.href="/cart/cartList";
        					}
        				});
    				}
    			});
    			
    			
    			
    			/** 주문하기 버튼 클릭 **/
    			$("#purchaseItemFormBtn").click(function(){
    				if(!$("input[name='checkItem']").is(":checked")){
    					alert("선택한 상품이 없습니다.");
    					return;
    				}
    				
   	                var cvoList = new Array(); // 체크된 상품의 장바구니 번호 배열로 받기
   	                var cvo;

   					var book_tr = $("#book_table").find("tr.tr").length;
   					var ebook_tr = $("#ebook_table").find("tr.tr").length;
    	            $("#book_table").find("tr.tr").find(".checkItem:checked").each(function(index){
    	            	var dataNum = $(this).parents("tr.tr").attr("data-num");
    	            	var index = $(".checkItem").index(this);
    	            	cvo = new Object();
    	            	
    	            	cvo.crt_num = dataNum;
    	            	console.log(dataNum);
    	            	
    	            	cvoList.push(cvo);
    	            });
	    	            
    	        	$("#ebook_table").find("tr.tr").find(".checkItem:checked").each(function(index){
    	            	cvo = new Object();
    	            	
    	            	var index = $(".checkItem").index(this);
    	            	var dataNum = $(this).parents("tr.tr").attr("data-num");
    	            	
    	            	cvo.crt_num = dataNum;
    	            	console.log("dataNum : "+dataNum);
    	            	
    	            	cvoList.push(cvo);
    	            });
   	    	        
   	    	        var cartJsonArr = JSON.stringify(cvoList);
   	                console.log(cartJsonArr);
   	                
   	             	order(cartJsonArr);

    			});// 주문하기 이벤트 끝
    			
    			
    			/* 상품 1개만 주문 */
    			$(".orderItemBtn").click(function(){
    				var dataNum = $(this).parents("tr.tr").attr("data-num");
    				var index = $(".checkItem").index(this);
    				
    				var cvoList = new Array();
    				var cvo = new Object();    	
    				
    				cvo.crt_num = dataNum;
	            	console.log(dataNum);
	            	
	            	cvoList.push(cvo);
	            	
    				var cvoJson = JSON.stringify(cvoList);
	            	
    				order(cvoJson);
    			});
    			
    			
    			/** 쇼핑계속 버튼 클릭 **/
    			$("#goMainpageBtn").click(function(){
    				location.href="/"; // 메인으로 이동
    			});
    			
 
    		}); // 최상위 종료
    		
    		
    		/* 페이지 로딩되자마자 체크박스 자동 체크
			   checkAllDefault(table명, 받은 카트리스트명) */
    		function checkAllDefault(table, list){
    			var cart = list;
    	        if (cart == 'false') {
    	            $(table).find(".checkAll").prop("checked", false);
    	        } else {
    	        	$(table).find(".checkAll").prop("checked", true);
    	        	$(table).find(".checkItem").prop("checked", true);
    	            sumItem(table);
    	        }
    		}
    		

    		/* 체크된 상품의 금액 합계 */
    		function sumItem(table){
    	        var sum = 0;
    	        var tr_length = $(table).find("tbody tr.tr").length;
    	        //console.log(tr_length);
    	        for(var i=0; i < tr_length; i++){
    	            if($(table).find("tr.tr").eq(i).find(".checkItem").is(":checked") == true){
    	            	var price = $(table).find("tr.tr").eq(i).find("td.td_price span").text();
        				price = parseInt(unComma(price));
    					//console.log(price);
        				console.log(typeof(price));
        				sum += price;
    	            }
    	        }
    			$(table).find(".lastrow span").text(sum);
    			priceFormat(table);
    		}
    		
    		
    		/* 금액 addComma함수 이용해서 콤마 추가하기 */
    		function priceFormat(table){
    	        var tr_length = $(table).find("tbody tr.tr").length;
    	        //console.log(tr_length);
    	        
    	        for(var i=0; i < tr_length; i++ ){
    	        	var price = $(table).find("tr.tr").eq(i).find("td.td_price span").text();
    				$(table).find("tr.tr").eq(i).find("td.td_price span").text(addComma(price)); // 천단위 콤마 추가    	            
    	        }
    	        
    	        var lastprice = $(table).find(".lastrow span").text();
    			$(table).find(".lastrow span").text(addComma(lastprice));
    		}
    		
    		/* 도서, 이북, 최종선택금액 출력(화면 하단에) */
    		function lastPriceSum(){
    			var book = $("#book_table").find(".lastrow span").text();
       			var ebook = $("#ebook_table").find(".lastrow span").text();
           		//console.log("책 총 금액 : "+book);
           		//console.log("ebook책 총 금액 : "+ebook);
           		$(".book_price span").text(book);
           		$(".ebook_price span").text(ebook);
           		
           		// 최종 선택 금액
           		var bookp = $(".book_price span").text();
           		var ebookp = $(".ebook_price span").text();
           		bookp = parseInt(unComma(bookp));
           		ebookp = parseInt(unComma(ebookp));
           		var lastp = bookp + ebookp;
           		
           		$(".last_price span").text(addComma(lastp));
    		}
    		
    		
    		/* 체크된 상품 주문하기 눌렀을 때 */
    		 function order(data){
	            $.ajax({
	               url : "/purchase/purchaseItems.json",
	               type : "post",
	               data : data,
	               headers : {
	                  "Content-Type" : "application/json",
	                  "X-HTTP-Method-Override" : "POST"
	               }, 
	               dataType : "text",
	               success: function (result) {
	            	   location.href="/purchase/purchaseForm";
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
		
	       	<div class="cart_wrap">
	           <div class="tit_cart">
	                <h3>장바구니</h3>
	            </div>            
                        
	            <table class="table" id="book_table" border="1">
	               <colgroup>
	                   <col width="5%" /> <!--체크박스-->
	                   <col width="50%" /> <!--상품명-->
	                   <col width="10%" /> <!--가격-->
	                   <col width="10%" /> <!--수량-->
	                   <col width="15%" /> <!--배송예정일-->
	                   <col width="10%" /> <!--주문-->
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>
	                            <input type="checkbox" class="checkAll" name="checkAll" checked="checked" />
	                        </th>
	                        <th>상품명</th>
	                        <th>가격</th>
	                        <th>수량</th>
	                        <th>배송예정일</th>
	                        <th>주문</th>
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty cartList1}">
		                		<c:forEach var="cart" items="${cartList1}">
		                			 <tr class="tr" data-num="${cart.crt_num}">
					                      <td class="td_check"><input type="checkbox" class="checkItem" name="checkItem" /></td>
					                      <td class="td_book">
					                         <span class="td_bookimg"><img src="${cart.listcover_imgurl}" /></span>
					                         <span class="td_bookname">${cart.b_name}</span>
					                      </td>
					                      <td class="td_price"><span>${cart.crt_price}</span>원</td>
					                      <td><input type="number" class="checkQty form-control" name="checkQty" value="${cart.crt_qty}" min="1" /><br/>
					                          <input type="button" class="btn btn-default updateQtyBtn" value="변경" /></td>
					                      <td>1일 이내 출고예정</td>
					                      <td class="td_orderBtn">
					                          <input type="button" class="btn btn-success orderItemBtn" value="주문하기" />
					                          <input type="button" class="btn btn-default deleteItemBtn" value="삭제하기" />
					                      </td>
					                  </tr>
		                		</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr class="tr">
	                				<td colspan="6">담긴 상품이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>
	                   
	                    <tr class="lastrow">
	                        <td colspan="6">DEV24배송 상품 총 금액 : <span></span>원</td>
	                    </tr>
	                </tbody>
            	</table>
           
       		</div><!--cart_wrap 일반도서 카트-->
        
        
	        <div class="cart_wrap">
	           <div class="tit_cart">
	                <h3>디지털 카트</h3>
	            </div>
            
	            <table class="table" id="ebook_table" border="1">
	               <colgroup>
	                   <col width="5%" /> <!--체크박스-->
	                   <col width="50%" /> <!--상품명-->
	                   <col width="10%" /> <!--가격-->
	                   <col width="10%" /> <!--수량-->
	                   <col width="15%" /> <!--배송예정일-->
	                   <col width="10%" /> <!--주문-->
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>
	                            <input type="checkbox" class="checkAll" name="checkAll" />
	                        </th>
	                        <th>상품명</th>
	                        <th>가격</th>
	                        <th>수량</th>
	                        <th>배송예정일</th>
	                        <th>주문</th>
	                    </tr>
	                </thead>
	                <tbody>
	                	<c:choose>
	                		<c:when test="${not empty cartList2}">
		                		<c:forEach var="cart" items="${cartList2}">
		                			 <tr class="tr" data-num="${cart.crt_num}">
					                      <td><input type="checkbox" class="checkItem" name="checkItem" /></td>
					                      <td class="td_book">
					                          <span class="td_bookimg"><img src="${cart.listcover_imgurl}" /></span>
					                          <span class="td_bookname">${cart.b_name}</span>
					                      </td>
					                      <td class="td_price"><span>${cart.crt_price}</span>원</td>
					                      <td>1</td>
					                      <td>무배송<br/>결제 후 즉시 다운로드</td>
					                      <td class="td_orderBtn">
					                          <input type="button" class="btn btn-success orderItemBtn" value="주문하기" />
					                          <input type="button" class="btn btn-default deleteItemBtn" value="삭제하기" />
					                      </td>
					                  </tr>
		                		</c:forEach>
	                		</c:when>
	                		<c:otherwise>
	                			<tr class="tr">
	                				<td colspan="6">담긴 상품이 없습니다.</td>
	                			</tr>
	                		</c:otherwise>
	                	</c:choose>
	 
	                    <tr class="lastrow">
	                        <td colspan="6">디지털카트 상품 총 금액 : <span></span>원</td>
	                    </tr>
	                </tbody>
            	</table>
           
      	  </div><!--cart_wrap 이북 카드-->
       
       
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
       
	       <div id="btnArea">
	           <input type="button" class="btn btn-success" id="purchaseItemFormBtn" value="주문하기" />
	           <input type="button" class="btn btn-warning" id="goMainpageBtn" value="쇼핑계속" />
	       </div><!--btnArea-->

    	</div> <!-- content_wrap -->
	</body>
</html>