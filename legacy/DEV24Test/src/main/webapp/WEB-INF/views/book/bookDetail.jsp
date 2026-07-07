<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:useBean id="now" class="java.util.Date" />
<!DOCTYPE html>

<html lang="ko">
	<head>
		<meta charset="UTF-8" />
		<!-- html4 : 파일의 인코딩 방식 지정 -->
		<!--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />-->
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
    	<style>
    		#contentWrap {
    			margin-bottom: 150px;
    			text-align: center;
    			font-size: 18px;
    			color: #959595;
    			
    		}
    		#bookDetailWrap {
    			display: inline-block;
    			width: 1000px;
    		}
    		#contentWrap * {
    			/* border: 1px solid; */
    			font-size: 15px;
    		}
    		
    		#contentWrap h1 {
			    font-size: 25px;
			    font-weight: bold;
				color: #777;
			}
    		
    		#detailCover {
    			width: 100%; 
    		}
    		#detailTopleft {
    			display: inline-block;
    			width: 38%;
    		}
    		#detailTopRight {
    			display: inline-block;
    			width: 56%;
    		}
    		#detailTopleft {
    			
    		}
    		#detailTopRight {
    			text-align: left;
    			margin-left: 40px;
    		}
    		#b_name {
    			font-size: 38px !important;
    			padding: 52px 0 22px 0;
    			color: #736794 !important;
    		}
    		#subTextWrap > span {
    			margin: 0 5px;
    			font-size: 16px;
    			padding-bottom: 5px;
    		}
    		
    		#subTextWrap {
    			padding-bottom: 5px;
    		}
    		
    		#topRightTopWrap {
    			border-bottom: 1px solid lightgrey;
    		}
    		
    		#priceWrap {
    			margin: 20px 0;
    		}
    		
    		#b_price{
    			font-size: 30px;
    			font-weight: bold;
    			color: #736794;
    		}
    		
    		#won {
    			font-size: 22px;
    		}
    		
    		#topRightMiddleWrap {
    			margin: 10px 0 10px 0;
    			border-bottom: 1px solid lightgrey;
    		}
    		
    		#deliveryWrap {
    			margin-top: 15px;
    		}
    		
    		.values {
    			color: #736794;
    		}
    		
    		#topRightMiddleWrap * {
    			font-size: 18px;
    			padding-bottom: 15px;
    		}
    		
    		#b_rating * {
    			font-size: 28px;
    		}
    		
    		
    		#detailTopWrap{
    			margin: 0 auto;
    			width: 99%;
    		}
    		
    		#detailTopWrap > div{
    			float:left;
    			
    		}
    		
    		#detailTopWrap:after{
    			display:block;
    			content : '';
    			clear:both;
    		}
    		#b_rating {
    			text-align: left;
    		}
    		
    		#choolgoWrap > p {
    			display: inline-block;
    		}
    		
    		.btnWrap {
				font-size: 16px;
				margin: 29px 0 0 0;
    		}
    		
    		.cntWrap {
    			margin-bottom: 10px;
    		}
    		
 			.upBtn, .downBtn {
 				height: 27px;
 				width: 50px;
 				background-color: #dcd6f7;
 				color: #5D0781;
 				border: none;
 				border-radius: 4px;
 			}
 			
 			.crt_qty {
 				    height: 27px;
    				text-align: center;
 			}
 			
 			.fa-caret-up, .fa-caret-down {
 				width: 50px;
 				height: 100%;
 				font-size: 25px;
 			}
 			input[type="number"]::-webkit-outer-spin-button,
			input[type="number"]::-webkit-inner-spin-button {
			    -webkit-appearance: none;
			    margin: 0;
			}
    		
    		.cartBtn, .buyBtn {
    			width: 99%;
    			height: 54px;
    			font-size: 25px;
    			margin-bottom: 10px;
    		}
    		
    		.buyBtn {
    			background-color: #424874;
    			color: #dcd6f7;
    		}
    		
    		.cartBtn {
    			background-color: #D0B7DA;
    			color: #424874;
    		}
    		
			.buyBtn:hover, .cartBtn:hover, .upBtn:hover, .downBtn:hover {
				opacity: 0.8;
				cursor: pointer;
			}
    		
    		#detailMiddleWrap {
    			width: 99%;
    		}
    		
    		.middleContentWrap {
    			text-align: left;
    		}
    		
    		#middleTextWrap {
    			max-height: 300px;
    			overflow: hidden;
    		}

			#b_info {
				margin: 0 0 25px 0;		
			}
    		#b_list {
    			height: 357px;
    		}
    		
    		.middleContentTitle {
    			text-align: left;
    			border-top: 1px solid gray;
    			font-size: 20px;
    			margin: 5px 0 15px;
    		}
    		
    		.dropmenu {
    			position: absolute;
			    width: 25%;
			    z-index: 1;
    		}
    		
    		.moreBtnWrap{
			    background-image: url(/resources/image/blurImage.png);
			    background-repeat: repeat-x;
			    background-position-y: -25px;
			    text-align: center;
			    font-size: 22px;
    		    position: relative;
			    top: -22px;
			    z-index: 9999;
    		}
    		.moreBtn{
    			display: inline-block;
    			width: 100px;
    			height: 30px;
    		}
    		
    		.moreBtn:hover {
    			cursor: pointer;
			    font-weight: bold;
    		}
    		
    		/* cartMsg */
    		.contentHeaderCartMsg > *{
    			display: inline-block;
    		}
    		
			.cartMsg{
				font-size: 12px;
				position: absolute;
				background-color: #dcd6f7;
				width: 550px;
				border-radius: 4px;
				padding: 5px;
				box-sizing: border-box;
			}
			.cartMsgText{
				color: #424874;
				text-align: center;
			}
			.cartMsgTextBold {
				font-weight: bold;
			}
			.cartBtnWrap {
				text-align: center;
			}
			.goCartBtn, .noCartBtn {
				background-color:#424874;
				color: #dcd6f7;
				width: 20%; 	
			}
    	</style>
		
    	<script>
    		$(function(){
    			//수량 실시간 갑지 (1~99)
    			$(".crt_qty").on("propertychange change keyup paste input", function(){
    				crt_qtyRange(this);
    			});
    			
    			$(".cartBtn").click(function(){
    				var index = $(".cartBtn").index(this);
    				if(!crt_qtyRange(".crt_qty:eq("+index+")")) return;
    				/* if ($(".crt_qty:eq("+index+")").val("", 0) == 0){
    					alert("수량을 입력해 주세요.");
    					$(".crt_qty:eq("+index+")").val(0);
    					return;
    				} */
    				if ($(".crt_qty:eq("+index+")").val() == 0){
    					alert("수량을 입력해 주세요.");
    					$(".crt_qty:eq("+index+")").val(0);
    					return;
    				}
    				
    				var cvo = new Object();
    				var cvoList = new Array();
    				
    				var b_num = $("#b_num").val();
    				var crt_qty = $(".crt_qty").val();
    				var b_price = $("#b_price").html().replace(",", "");
    				
    				cvo.b_num = b_num;
    				cvo.crt_qty = crt_qty;
    				cvo.crt_price = b_price * crt_qty;
    				console.log(cvo.toString());
    				
    				cvoList.push(cvo);
    				
    				var data = JSON.stringify(cvoList);
    				
    				var result = addCart(data);
    				if(result == 'SUCCESS')
						$(".cartMsg:eq("+index+")").css("display", "block");
    			});
    			
    			$(".upBtn").click(function(){
    				console.log("upBtn");
    				var n = $(".upBtn").index(this);
    				var crt_qty = $(".crt_qty:eq("+n+")");
    				$(".crt_qty:eq("+n+")").val(crt_qty.val()*1 + 1);
    				if (!crt_qtyRange(crt_qty)) return;
    			});

    			$(".downBtn").click(function(){
    				var n = $(".downBtn").index(this);
    				var crt_qty = $(".crt_qty:eq("+n+")");
    				$(".crt_qty:eq("+n+")").val(crt_qty.val()*1 - 1);
    				if (!crt_qtyRange(crt_qty)) return;
    			});
    			

    			$(".goCartBtn").click(function(){
    				location.href="/cart/cartList";
    			});
    			
    			$(".noCartBtn").click(function(){
    				$(this).parent().parent("div").css("display", "none");
    			});
    			
    			
    			var bListMoreBtn = "off";
    			$(".moreBtn").click(function(){
    				if (bListMoreBtn == "off"){
    					console.log(bListMoreBtn);
    					$("#b_list").css("height", "auto");
    					
    					$("#middleTextWrap").css("overflow", "auto")
    						   				.css("max-height", "none")
    						   				.css("margin-bottom", "20px");
    					
    					$(".moreBtn").html("줄여보기");
    					
    					$(".moreBtnWrap").css("position", "static")
										.css("top", "0")
										.css("z-index", "0");
    									
    					
    					bListMoreBtn = "on";
    				} else {
    					console.log(bListMoreBtn);
    					$("#b_list").css("height", "350px");
    					
    					$("#middleTextWrap").css("overflow", "hidden")
						   					.css("max-height", "300px")
    						   				.css("margin-bottom", "0");
    					
    					$(".moreBtn").html("더보기");
    					
    					$(".moreBtnWrap").css("position", "relative")
										 .css("top", "-22px")
										 .css("z-index", "9999");
    					
    					bListMoreBtn = "off";
    				}
    			});
    			
    			/*구매버튼 클릭시 이벤트*/
    			$(".buyBtn").click(function(){
    				
    				var index = $(".buyBtn").index(this);
    				
    				if(!crt_qtyRange(".crt_qty:eq("+index+")")) return;
    				
    				if ($(".crt_qty:eq("+index+")").val() == 0){
    					alert("수량을 입력해 주세요.");
    					$(".crt_qty:eq("+index+")").val(0);
    					return;
    				}
    				
    				var b_num = $("#b_num").val();
    				var crt_qty = $(".crt_qty").val();
    				var b_price = $("#b_price").html().replace(",", "");
    				
    				var cvo = {
		    				"b_num" : b_num,
		    				"crt_qty" : crt_qty,
		    				"crt_price" : b_price * crt_qty
					};
    				
    				var cvoToJSON = JSON.stringify(cvo);
    				var crt_num = buySingleItem(cvoToJSON);
    				if(cvo.crt_num == -1) return; 
    				
    				cvo = {
		    				"b_num" : b_num,
		    				"crt_qty" : crt_qty,
		    				"crt_price" : b_price * crt_qty,
    						"crt_num" : crt_num
					};
    				
    				cvoToJSON = JSON.stringify(cvo);
    				order(cvoToJSON); 
    			});
    			
    			
    		});//onload
    		
    		//false : range에서 벗어남
    		function crt_qtyRange(selector) {

				if ($(selector).val() < 0) {
					alert("수량은 1개부터 99개 까지 입력 가능합니다."); 
					$(selector).val("0");
					return false;
				}
				if ($(selector).val() > 99) {
					alert("수량은 1개부터 99개 까지 입력 가능합니다."); 
					$(selector).val("99");
					return false;
				}
				return true;
			};
			
			//장바구니 추가 함수
			function addCart(data){
				var returnVal = "";
				
				$.ajax({
					url : "/cart/addToCart",
					type : "POST",
					data : data,
					async: false,
					headers : {
						"Content-Type" : "application/json",
						"X-HTTP-Method-Override" : "POST"
					},
					dataType : "text",
					success: function (result) {
						returnVal = 'SUCCESS';
					},
					error : function(){
						alert("장바구니 담기에 실패했습니다.\n관리자에게 문의해 주세요.");
						returnVal = 'FAIL';
					}
				});
				console.log(returnVal);
				return returnVal;
			};
			
			/*cart테이블에 데이터를 올리고 crt_num을 가져오기 위한 ajax 함수*/
			function buySingleItem(data) {
				var returnVal = 0;
	            $.ajax({
					url : "/cart/buySingleItem",
					type : "POST",
					data : data,
					async: false,
					headers : {
						"Content-Type" : "application/json",
						"X-HTTP-Method-Override" : "POST"
					},
					success : function(result) {
						returnVal = result;
						console.log("success: "+returnVal)
						return result;
					},
					error : function(){
						alert("구매화면으로 이동이 실패했습니다.\n관리자에게 문의해 주세요.");
						returnVal = -1;
						console.log("error: "+returnVal)
					}
	            });
				return returnVal;
			};
			
    		/* 체크된 상품 주문하기 눌렀을 때 */
    		 function order(data){
    			console.log("order 호출");
	            $.ajax({
	               url : "/purchase/purchaseSingleItem.json",
	               type : "post",
	               data : data,
	               async: false,
	               headers : { 
	                  "Content-Type" : "application/json",
	                  "X-HTTP-Method-Override" : "POST"
	               }, 
	               dataType : "text",
	               success: function (result) {
	            	   location.href="/purchase/SingleItemPurchaseForm";
	               },
	               error : function(){
	                  alert("시스템 오류 발생. \n관리자에게 문의해 주세요.");
	               }
	            });
	         };
    	</script>
	</head>
	<body>
		<form >
			<input type="hidden" name="b_num" id="b_num" value="${vo.b_num}"/>
			<input type="hidden" name="crt_qty" id="crt_qty" value="" />
		</form>
		<div id="contentWrap">
			<div id="bookDetailWrap">
				<div id="detailTopWrap">
					<div id="detailTopleft">
						<img id="detailCover" src="${vo.detailcover_imgurl}"/>
						<p id="bigImgBtnWrap">
							<button type="button" class="btn btn-default" id="bigImg">크게보기</button>
						</p>
					</div><!-- detailTopLeft -->
					<div id="detailTopRight">
						<div id="topRightTopWrap">
							<h1 id="b_name">${vo.b_name}</h1>
							<p id="subTextWrap">
								<span id="b_author">${vo.b_author}</span> | 
								<span id="b_pub">${vo.b_pub}</span> | 
								<span id="b_date">${vo.b_date}</span>
							</p>
							<p id="b_rating">
								<c:set var="avg" value="${(bl.ra_sum/bl.ra_count)}" />
								<c:set var="devided" value="${(bl.ra_sum/bl.ra_count)/2}" />
								<c:choose>
									<c:when test="${ avg != Double.NaN }">
										<c:forEach var="i" begin="1" end="5" step="1">
											<c:if test="${ i <= devided }">
												<i class="fas fa-star"></i>
											</c:if>
											<c:if test="${ i > devided }">
												<c:choose>
													<c:when test="${ i>devided && (devided-i+1)>0}">
														<i class="fas fa-star-half-alt"></i>
													</c:when>
													<c:otherwise>
														<i class="far fa-star"></i>
													</c:otherwise>
												</c:choose>
											</c:if>
										</c:forEach>
									</c:when>
									<c:otherwise>
										<i class="far fa-star"></i>
										<i class="far fa-star"></i>
										<i class="far fa-star"></i>
										<i class="far fa-star"></i>
										<i class="far fa-star"></i>
									</c:otherwise>
								</c:choose>
								<c:if test="${ avg == Double.NaN }">
									<c:set var="avg" value="0"/>
								</c:if>
								<span class="avgVal">${avg}</span>
							</p>
						<div id="priceWrap">
							<span id="b_price"><fmt:formatNumber value="${vo.b_price}" /></span><span id="won">원</span>
						</div>
						</div>
						<div id="topRightMiddleWrap">
							<div id="deliveryWrap">
								<span id="deliName" class="names">배송료 : </span>
								<span id="deliVal" class="values">무료</span>
							</div>
							<div id="choolgoWrap">
								<span id="choolgoName" class="names">출고 예정일 : </span>
								<span id="choolgoVal" class="values">
									<fmt:formatDate value="${now}" var="curr" pattern="HH"/>
									<c:choose>
										<c:when test="${curr < 17}"><span class="choolgoDay">오늘 출고</span></c:when>
									<c:otherwise><span class="choolgoDay">내일 출고</span></c:otherwise>
									</c:choose>
								</span>						
									(17시 이전 주문시 당일 출고)
							</div>
						</div>
						
						<div class="btnWrap">
							<div class="cntWrap">
								<input type="number" value="1" class="crt_qty" min="0" max="99"/>
								<button type="button" class="upBtn" ><i class="fas fa-caret-up"></i></button>
								<button type="button" class="downBtn" ><i class="fas fa-caret-down"></i></button>
							</div>
							<!-- <input type="number" class="number text-right" name="cartCnt" value="0" min="0"/> -->
							
							<button type="button" class="btn text-right cartBtn">장바구니 담기</button>
							<div class="cartMsg" style="display: none;">
								<p class="cartMsgText">
									<span class="cartMsgTextBold">상품이 장바구니에 담겼습니다.</span><br />
									바로 확인하시겠습니까?
								</p>
								<div class="cartBtnWrap">
									<button type="button" class="btn goCartBtn">예</button>
									<button type="button" class="btn noCartBtn">아니오</button>
								</div>											
							</div>
							<button type="button" class="btn text-right buyBtn">구매</button>
							
						</div><!-- btnWrap -->
					</div><!-- detailTopRight -->
				</div><!-- detailTopWrap -->
				<div id="detailMiddleWrap">
					<c:if test="${not empty vo.b_info}">
						<div id="b_info" class="middleContentWrap">
							<h1 class='middleContentTitle'>책 소개</h1>
							${vo.b_info}
						</div>
					</c:if>
					<c:if test="${not empty vo.detail_imgurl}">
						<div id="detailImg" class="middleContentWrap">
						<h1 class="middleContentTitle">상세 이미지</h1>
							<img src="${vo.detail_imgurl}" />
						</div>
					</c:if>
					<c:if test="${not empty vo.b_list}">
						<div id="b_list" class="middleContentWrap">
							<h1 class='middleContentTitle'>목차</h1>
							<div id="middleTextWrap">
								${vo.b_list}
							</div>
							<div class="moreBtnWrap">
								<div class="moreBtn">더보기</div>
							</div>
						</div>
					</c:if>
					<c:if test="${not empty vo.b_authorinfo}">
						<div id="b_info" class="middleContentWrap">
							<h1 class='middleContentTitle'>작가 소개</h1>
							${vo.b_authorinfo}
						</div>
					</c:if>
				</div>
				
				
			</div><!-- bookDetailWrap -->

			<%-- 게시판 댓글 --%>
			<jsp:include page="../review/review.jsp" />

		</div><!-- contentWrap -->
	</body>
</html>
