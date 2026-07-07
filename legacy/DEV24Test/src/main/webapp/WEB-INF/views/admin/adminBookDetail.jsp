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
    		
    		.updateBtn {
    			width: 99%;
    			height: 54px;
    			font-size: 25px;
    			margin-bottom: 10px;
    		}
    		
    		.stateBtn {
    			width: 32%;
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
    			
    			//상태에 해당하는 버튼 비활성화
    			if($(".bookState").html() == "등록"){
    				$("#regBtn").attr("disabled", "true");
    			}
    			if($(".bookState").html() == "절판"){
    				$("#outOfPrintBtn").attr("disabled", "true");
    			}
    			if($(".bookState").html() == "품절"){
    				$("#soldOutBtn").attr("disabled", "true");
    			}
    			
    			$(".updateBtn").click(function(){
    				location.href="/admin/book/bookUpdateForm/"+$("#b_num").val();
    			});
    			
				//선택항목 등록/절판/품절 처리
				$(".stateBtn").click(function(){
					var stateKeyword = $(this).val();
					console.log(stateKeyword);

					var bvo = new Object();
					var bNumList = new Array();
					var b_num = $("#b_num").val();
					
					bNumList.push(Number(b_num));
					
					bvo.bNumList = bNumList;
					bvo.b_stateKeyword = stateKeyword;
					
					var data = JSON.stringify(bvo);
					
					console.log(bvo.toString());
					console.log(data.toString());
					updateBookState(data);
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
    			
    			
    		});//onload

			//b_state 컬럼 업데이트 함수
			function updateBookState(data){
				
				$.ajax({
					url : "/admin/book/updateBookState",
					type : "POST",
					data : data,
					headers : {
						"Content-Type" : "application/json",
						"X-HTTP-Method-Override" : "POST"
					}, 
					dataType : "text",
					success : function(result) {
						alert(result + "권의 도서가 정상처리 되었습니다.");
						location.reload();
					},
					error : function(){
						alert("도서 등록에 실패하였습니다.");
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
							<p>도서번호: <span id="bookNum">${ vo.b_num }</span></p>
							<p id="bookNum">도서상태: 
							<c:if test="${ empty vo.b_state }">
								<span class="bookState" style="color:blue">등록</span>
							</c:if>
							<c:if test="${ vo.b_state == 'unreg' }">
								<span class="bookState" style="color:red">미등록</span>
							</c:if>
							<c:if test="${ vo.b_state == 'outOfPrint' }">
								<span class="bookState" style="color:black">절판</span>
							</c:if>
							<c:if test="${ vo.b_state == 'soldOut' }">
								<span class="bookState" style="color:orange">품절</span>
							</c:if>
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
							<div class="btnWrap">
								<button type="button" class="btn btn-success text-right updateBtn">수정</button>
								<button type="button" class="btn btn-default text-right stateBtn" id="regBtn" value="reg">등록</button>
								<button type="button" class="btn btn-default text-right stateBtn" id="outOfPrintBtn" value="outOfPrint">절판</button>
								<button type="button" class="btn btn-default text-right stateBtn" id="soldOutBtn" value="soldOut">품절</button>
							</div><!-- btnWrap -->
						</div>
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
