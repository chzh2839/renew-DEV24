<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
    	<style>
    		.contentHeaderCartMsg > *{
    			display: inline-block;
    		}
    		.topLeftDiv, .topRightDiv {
    			display: inline-block;
    		}
    		.selectedCartBtn {
    			width: 120px;
				color : #dcd6f7;
				font-size: 14px;
				margin-top: 5px;
				background-color: #D0B7DA;
				color:  #424874;
    		}
    		.bottom > div {
    			display: inline-block;
    		}
    		.bottomLeftDiv{
    			margin-top: 10px;
    		}
    		.bottomRightDiv > button:hover{
    			opacity: 0.8;
    		}
    		.sort{
    			display: inline-block;
    			margin: 0 10px;
    		}
    		.sort:hover {
    			cursor: pointer;
    		}
    		.pageNum{
    			display: inline-block;
    		}
    		.pageNumBtn {
    			display: inline-block;
    			width: 25px;
    		}
    		.pageBtn{
    			display: inline-block;
    			font-weight: bold;
    		}
    		.pageBtn * {
    			font-size: 20px;
    		}
    		.pageBtn {
    			cursor: pointer;
    		}
    		.contentWrap {
    			text-align: center;
    		}
    		.listWrap {
    			display: inline-block;
    			margin-top: 20px;
    			width: 1000px;
    		}
    		.listWrap * {
    			/* border: 1px solid;  */
    		}
    		.bookWrap {
    			width: 85%;
    			height: 168px;
    			display: inline-block;
    			border: 1px solid lightgray;
    			margin-bottom: 12px;
    			padding: 8px 10px;
    		}
    		.bookWrap:hover{
    			box-shadow: 2px 2px 6px 1px lightgrey;
    		}
    		.listcoverWrap {
			   height: 100%;
			   float: left;
			   display: inline-block;
			   /* border-right: solid #f4eeff 25px; */
   			}
    		.listcover {
    			width: 120px;
    			cursor: pointer;
    			border-right: solid #f4eeff 25px;
    		}
    		.listcover:hover {
    			opacity: 0.7;
    		}
    		/* .lineDiv {
    			width: 1px;
    			background-color: #dcd6f7;
    			display: inline-block;
    		} */
    		.booktext {
    			/* border: 1px solid; */
    			display: inline-block;
			    height: 100%;
			    width: 400px;
			    float: left;
			    padding-left: 25px;
			    text-align: left;
       		}
       		.b_name {
       			font-weight: 900;
       			font-size: 26px;
       			color: #736794;
       			overflow: hidden;
       			text-overflow: ellipsis;
       			width: 390px;
       		}
       		.b_nameText {
    			max-width: 500px;
       			font-weight: 1000;
       			font-size: 20px;
       			color: #736794;
       			padding-top: 10px;
       			text-overflow: ellipsis;
    			white-space: nowrap;
       			overflow: hidden;
    			cursor: pointer;
       		}
       		.authorPub {
       			font-size: 14px;
       			padding-top: 5px;
       		}
       		.priceWrap {
       			padding: 0 0 5px 0;
       			font-size: 20px;
       			font-weight: bold;
       		}
       		.won {
				color: #959595;
       			font-size: 14px;
       			padding-top: 3px;
       		}
       		.authorPub, .priceWrap {
       			color: #959595;
    			padding-top: 5px;
       		}
       		.b_rating *{
       			font-size: 22px;
       		}
       		.fa-star, .fa-star-half-alt, .avgVal {
       			color: #736794;
       		}
       		.avgVal {
       			font-size: 16px;
       			font-weight: bold;
       			color: #736794;
       			padding-left: 20px;
       		}
       		.btnWrap{
       			display: inline-block;
       			justify-content: center;
       			width: 237px;
       			height: 100%;
       			float: left;
       			text-align: left;
       		}
       		.cntWrap {
       			margin-top: 22px;
       		}
 			.crt_qty {
 				width: 100px;
 				height: 22px;
 				font-size: 16px;
 				text-align: center;
 				color: #5A5A5B;
 				border: 1px solid lightgray;
 				border-radius: 4px;
 			}
 			.upBtn, .downBtn {
 				height: 24px;
 				width: 50px;
 				background-color: #dcd6f7;
 				color: #5D0781;
 				border: none;
 				border-radius: 4px;
 			}
 			.fa-caret-up, .fa-caret-down {
 				width: 50px;
 				height: 100%;
 				font-size: 18px;
 			}
 			input[type="number"]::-webkit-outer-spin-button,
			input[type="number"]::-webkit-inner-spin-button {
			    -webkit-appearance: none;
			    margin: 0;
			}
			.cartBtn, .buyBtn {
				display: block;
			}
			.buyBtn, .cartBtn {
				color : #dcd6f7;
				width: 210px;
				font-size: 15px;
				margin-top: 5px;
			}
			.buyBtn {
				background-color: #424874/* 71459C */;
				color: #f4eeff;
			}
			.cartBtn {
				background-color: #D0B7DA;
				color:  #424874;
			}
			.buyBtn:hover, .cartBtn:hover, .upBtn:hover, .downBtn:hover {
				opacity: 0.8;
				cursor: pointer;
			}
			.cartMsg{
				font-size: 12px;
				position: absolute;
				background-color: #dcd6f7;
				width: 210px;
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
				width: 65px;
			}
			.goCartBtn {
				color: white;
			}
			.noCartBtn{
				color: black;
			}
			
			.chkWrap {
				display: inline-block;
				height: 100%;
			}
			.checkbox {
				width: 15px;
				height: 15px;
				display: block;
				margin-top: 76px;
			} 
			.checkbox:checked {
				background-color: #5D0781;
			}
			.contentFooter {
				margin-bottom: 150px;
			}


			/*사이드바*/
			#content_wrap{
			    width: 1200px;
			    margin: 0 auto;
			}
			
			#leftmenu{
			    width: 150px;
			    background-color: #f4eeff;
			    border: 2px solid #424874;
			    padding: 5px 10px;
			    box-sizing: border-box;
			    text-align: center;
			    margin-top: 20px;
			    border-radius: 10px;
			    color: #424874;
			    position: fixed;
			}
			
			#leftmenu .book_cate{
			    font-size: 17px;
			    letter-spacing: 3px;
			    width: 80%;
			    border-bottom: 1px solid #424874;
			    margin: 0 auto;
			    padding-bottom: 5px;
			    font-weight: bold;
			}
			
			
			#leftmenu ul{
			    padding-top: 5px;
			}
			
			#leftmenu .book_cate:nth-child(3){
			    margin-top: 20px;
			}
			
			#leftmenu ul > li{
			    line-height: 30px;
			    cursor: pointer;
			}
			
			
			#leftmenu ul > li:hover{
			    background-color: #f4eeff;
			    font-weight: bold;
			    color: #424874;
			}


			/*정렬*/
    		.sort{
    			display: inline-block;
    			margin: 0 10px;
    		}
    		
    		.sort:hover {
    			cursor: pointer;
    		}
    		
    		.b_nameText:hover {
    			cursor: pointer;
    		}
    		
    		.form-control {
    			width: auto;
    			display: inline-block;
    		}
    		ul.sort, ul.sort * {
    			display: inline-block !important;
    		}
    		
    		.b_sort > .nav-link:hover {
    			text-shadow : 1px 1px 1px lightgray;
    		}
    		
    		/*품절, 절판 메세지*/
    		.stateWrap {
    			text-align: center;
    		}
    		
    		.stateText {
    			color: gray;
   			    font-size: 24px;
   				padding-top: 5px;
    		}
    		
    		.stateImage {
    			width: 60%;
    			padding-top: 40px
    		}
    	</style>
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script>
			var sortArr = ["best", "new", "old", "lowPrice", "highPrice"];
			
    		$(function() {
    			
    			//pagination 정보  변수에 담기
				//listRangeSelect 받아온 값으로 활성화
    			var listRange = Number($("#listRange").val());
				$("#listRangeSelect").val(listRange).attr("selected", "true");
				
    			var page = Number($("#page").val());//현재페이지
    			var startPage = Number($("#startPage").val());//지금 길이의 시작페이지
    			var endPage = Number($("#endPage").val());
    			var pageLength = Number($("#pageLength").val());
    			var cateOne_num = Number(${pagination.cateOne_num});
    			var cateTwo_num = Number(${pagination.cateTwo_num});
    			var range = Number($("#range").val());
    			var category = Number(window.location.pathname.substr(6, 8));// "/book/00" 에서  "00"만 추출
    			
    			//정렬 버튼 클릭시 연결 동작
				$(".b_sort > .nav-link").click(function(){
					var index = $(".nav-link").index(this);
					$("#b_sort").val(sortArr[index]);
					$("#page").val(1);
					$("#startPage").val(1);
					goURL(category);
				});

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
    				
    				var dataNum = $(this).parents(".bookWrap").attr("data-num");
    				var crt_qty = $(".crt_qty:eq("+index+")").val();
    				var b_price = $(".b_price:eq("+index+")").html();
    				
    				cvo.b_num = dataNum;
    				cvo.crt_qty = crt_qty;
    				cvo.crt_price = b_price * crt_qty;
    				console.log(cvo.b_price);
    				
    				cvoList.push(cvo);
    				
    				var data = JSON.stringify(cvoList);
    				
    				var result = addCart(data);
    				if(result == 'SUCCESS'){
						$(".cartMsg:eq("+index+")").css("display", "block");
    				}
    			});
    			
    			$(".selectedCartBtn").click(function(){
    				var isQtyZero = false;
    				var cvoList = new Array();
    				var cvo;
    				if ($(".checkbox:checked").length == 0){
						alert("선택된 도서가 없습니다.");
						return;
					}
    				$(".checkbox:checked").each(function(idx){
    					cvo = new Object();
    					
        				var index = $(".checkbox").index(this);
    					var dataNum = $(this).parents(".bookWrap").attr("data-num");
        				var crt_qty = $(".crt_qty:eq("+index+")").val();
        				var b_price = $(".b_price:eq("+index+")").html();
        				
        				if (crt_qty < 1){
    						alert("수량을 입력해 주세요.");
    						isQtyZero = true;
    						return;
        				}
        				
    					cvo.b_num = dataNum;
    					cvo.crt_qty = crt_qty;
    					cvo.crt_price = b_price*crt_qty;
    					
    					console.log(cvo.toString());
    					cvoList.push(cvo);
    				});
    				
    				if(isQtyZero)
    					return;

					var cartJsonArr = JSON.stringify(cvoList);
					console.log(cartJsonArr);
    				var result = addCart(cartJsonArr);
    				if(result == 'SUCCESS')
						$(".contentHeaderCartMsg").css("display", "block");
    			});
    			
    			
    			$(".goCartBtn").click(function(){
    				location.href="/cart/cartList";
    			});
    			$(".noCartBtn").click(function(){
    				$(this).parent().parent("div").css("display", "none");
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
				
    			

				/*	페이징 처리 관련 */
				if (page > pageLength){
					page = pageLength;
				}
				
				if (startPage-1 <= 0){
					$(".prevBtn").css("cursor", "default");
					$(".prevRangeBtn").css("cursor", "default");
				}
	
				if(startPage+10 > pageLength){
					$(".nextBtn").css("cursor", "default");
					$(".nextRangeBtn").css("cursor", "default");
				}
				
				$(".prevBtn").click(function(){
					if (startPage > 1){
						if (startPage-10 > 0){
						startPage = startPage-10;
							$("#startPage").val(startPage);
							$("#page").val(startPage);
							goURL(category);
						} else {
							$("#startPage").val(1);
							$("#page").val(1);
							goURL(category);
						}
					}
				});
	
				$(".nextBtn").click(function(){
					if(startPage+10 <= pageLength){
						$("#startPage").val(startPage+10);
						$("#page").val(startPage);
						goURL(category);
					}
				});
	
				$(".prevRangeBtn").click(function(){
					if (startPage-1 > 1){
						$("#page").val(1);
						$("#startPage").val(1);
						goURL(category);
					}
				});
	
				$(".nextRangeBtn").click(function(){
					if(startPage+10 <= pageLength){
						$("#page").val(pageLength);
						if (pageLength%range != 0){
							$("#startPage").val(parseInt(pageLength - (pageLength%range) + 1));
						}
						if (pageLength%range == 0){
							$("#startPage").val(parseInt(pageLength - 9));
						}
						goURL(category);
					}
						
				});
				
				$(".pageNumBtn").click(function(){
					var page = $(this).html();
					$("#page").val(page);
					goURL(category);
				});
				
				$(".pageNum[data-num='"+page+"'] > a.pageNumBtn")
											.css("font-weight", "bold")
											.css("text-decoration", "underline");
				
				$(".b_name").click(function(){
					var b_num = $(this).parents(".bookWrap").attr("data-num");
					location.href = "/book/detail/"+b_num;
				});
	
				// #listRange 값이 바뀔 때마다 맞춰 출력
				$("#listRangeSelect").change(function(){
					var listRangeSelect = $("#listRangeSelect").val();
					$("#listRange").val(listRangeSelect);
					goURL(category);
				});
				//페이징 처리 종료
    			
				$("#searchBtn").click(function(){
					
					var b_searchSelect = $("#searchSelect").val();
					var b_searchKeyword = $("#searchKeyword").val();
					console.log(b_searchKeyword);
					console.log(b_searchSelect);
					
					if(!chkData("#searchKeyword", "검색어를")) return;
					
					$("#b_searchSelect").val(b_searchSelect);
					$("#b_searchKeyword").val(b_searchKeyword);
					$("#b_sort").val("");
					$("#b_startPage").val(1);
					$("#b_stateKeyword").val("all");
					
					goURL("00");
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
    				
    				var b_num = $(".bookWrap:eq("+index+")").attr("data-num");
    				var crt_qty = $(".crt_qty:eq("+index+")").val();
    				var b_price = $(".b_price:eq("+index+")").html();
    				
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
    				console.log("order() 호출 전");
    				order(cvoToJSON); 
    				console.log("order() 호출 후");
    				
    			});
    			
    			//category에 맞는 leftbar 강조
    			$("#"+category).css("font-weight", "900")
    						   .css("text-shadow", "2px 2px 6px grey");
    			
    			
    			//정렬버튼 클릭후 해당버튼 강조
    			$(".b_sort > .nav-link").each(function(){
    				var idx = $(".b_sort > .nav-link").index(this);
    				if (sortArr[idx] == $("#b_sort").val()){
		    			$(".b_sort > .nav-link:eq("+idx+")").css("font-weight", "900")
						   									.css("text-decoration", "underline");
    				}
    			});
    		
    			$(".sort").click(function(){
    				var sort = $(this).val();
    				console.log(sort);
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
	         
			//패이지 이동 URI값 조합 함수
			function goURL(category){
				var url = "/book/"+category;
				$("#goURL").attr({
						"method" : "get",
						"action" : url
				});
				
				$("#goURL").submit();
			};
    	</script>
	</head>
	<body>
		<div class="contentWrap">
			<form id="goURL" name="goURL">
				<input type="hidden" name="page" id="page" value="${pagination.page}"/>
				<input type="hidden" name="startPage" id="startPage" value="${pagination.startPage}" />
				<input type="hidden" name="endPage" id="endPage" value="${pagination.endPage}" />
				<input type="hidden" name="pageLength" id="pageLength" value="${pagination.pageLength}" />
				<input type="hidden" name="range" id="range" value="${pagination.range}" />
				<input type="hidden" name="listRange" id="listRange" value="${pagination.listRange}" />
				<input type="hidden" name="b_sort" id="b_sort" value="${ pagination.b_sort }" />
				<input type="hidden" name="b_searchKeyword" id="b_searchKeyword" value=""/>
				<input type="hidden" name="b_searchSelect" id="b_searchSelect" value=""/>
			</form>
		<div class="contentHeader">
			<div class="top">
				<div class="topLeftDiv">
					<ul class="nav sort justify-content-center">
						<li class="nav-item sort b_sort">
							<a class="nav-link sort active" href="#">판매순</a>
						</li>
						<li class="nav-item sort b_sort">
							<a class="nav-link sort active" href="#">신상품순</a>
						</li>
						<li class="nav-item sort b_sort">
							<a class="nav-link sort active" href="#">오래된순</a>	
						</li>
						<li class="nav-ite sortm b_sort">
							<a class="nav-link sort active" href="#">낮은 가격순</a>
						</li>
						<li class="nav-item sort b_sort">
							<a class="nav-link sort active" href="#">높은 가격순</a>
						</li>
					</ul>
				</div>
				<div class="topRightDiv">
					<select id="listRangeSelect">
						<option value="20">20개씩 보기</option>
						<option value="40">40개씩 보기</option>
					</select>
				</div>
			</div>
			<div class="bottom">
				<!-- pagination -->
				<div class="bottomLeftDiv">
					<div class="paginationBox">
<%-- 						<ul class="pagination">
							<li class="pageBtn prevRangeBtn" >
								<i class="fas fa-angle-double-left"></i>
							</li>
							<li class="pageBtn prevBtn" >
								<i class="fas fa-angle-left"></i>
							</li>
							<c:forEach var="i" begin="${pagination.startPage}" end="${pagination.endPage}">
								<li class="pageNum" data-num="${i}"><a class="pageNumBtn" href="#">${i}</a></li>
							</c:forEach>
							<li class="pageBtn nextBtn">
								<i class="fas fa-angle-right"></i>
							</li>
							<li class="pageBtn nextRangeBtn">
								<i class="fas fa-angle-double-right"></i>
							</li>
						</ul> --%>
						<ul class="pagination">
							<li class="pageBtn prevRangeBtn page-item" >
								<a class="page-link"  href="#">
									<i class="fas fa-angle-double-left"></i>
								</a>
							</li>
							<li class="pageBtn prevBtn page-item" >
								<a class="page-link"  href="#">
									<i class="fas fa-angle-left page-link"></i>
								</a>
							</li>
							<c:forEach var="i" begin="${pagination.startPage}" end="${pagination.endPage}">
								<li class="pageNum page-item" data-num="${i}"><a class="pageNumBtn page-link" href="#">${i}</a></li>
							</c:forEach>
							<li class="pageBtn nextBtn page-item">
								<a class="page-link"  href="#">
									<i class="fas fa-angle-right page-link"></i>
								</a>
							</li>
							<li class="pageBtn nextRangeBtn page-item">
								<a class="page-link"  href="#">
									<i class="fas fa-angle-double-right page-link"></i>
								</a>
							</li>
						</ul>
					</div>
				</div>
				<!-- content header buttons -->
				<div class="bottomRightDiv">
					<button type="button" class="btn text-right selectedCartBtn" name="cartBtn" >장바구니 담기</button>
				</div>
			</div>
			<div class="contentHeaderCartMsg" style="display: none;">
				<p class="cartMsgText">
					<span class="cartMsgTextBold">상품이 장바구니에 담겼습니다.</span>
					바로 확인하시겠습니까?
				</p>
				<div class="contentHeaderCartBtnWrap">
					<button type="button" class="btn btn-primary goCartBtn">예</button>
					<button type="button" class="btn btn-default noCartBtn">아니오</button>
				</div>	
			</div>
		</div>
			
		<div id="content_wrap"> 
     
	       <div id="leftmenu">
	          <div class="book_cate"><a  id="10"  href="/book/10">일반도서</a></div>
	           <ul>
	               <li id="11"><a href="/book/11">프로그래밍 언어</a></li>
	               <li id="12"><a href="/book/12">OS/데이터베이스</a></li>
	               <li id="13"><a href="/book/13">웹사이트</a></li>
	               <li id="14"><a href="/book/14">컴퓨터 입문/활용</a></li>
	               <li id="15"><a href="/book/15">네트워크/해킹/보안</a></li>
	             </ul>
	            <div class="book_cate"><a id="20" href="/book/20">eBook</a></div>
	           <ul>
	                <li><a id="26" href="/book/26">IT전문서</a></li>
	                <li><a id="27" href="/book/27">컴퓨터 수험서</a></li>
	                <li><a id="28" href="/book/28">웹/컴퓨터 입문	&#38;활용</a></li>
	          </ul>
	       </div>
	       
	       <div id="div">
		       <h1 id="listTitle"></h1>
				<div class="listWrap">
						<c:choose>
							<c:when test="${ not empty bookList }">
								<c:forEach var="bl" items="${ bookList }">
									<div class="bookWrap" data-num="${ bl.b_num }">
										<div class="listcoverWrap text-left">
											<img class="listcover" src="${bl.listcover_imgurl}">
										</div>
										<div class="lineDiv"></div>
										<div class="booktext text-left">
											<h1 class="b_name" title="${ bl.b_name }">
												<span class="b_nameText" >${ bl.b_name }</span>
											</h1>
											<p class="authorPub">${ bl.b_author } 저 | ${ bl.b_pub } <br/>${ bl.b_date }</p>
											<p class="priceWrap">
												<span class="b_price" style="display: none" >${ bl.b_price }</span>
													<fmt:formatNumber value="${ bl.b_price }"/>
												<span class="won">원</span>
											</p>
											<p class="b_rating">
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
										</div>
										<div class="btnWrap">
											<c:if test="${ empty bl.b_state }">
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
											</c:if>
											<div class="stateWrap">
												<c:if test="${ bl.b_state == 'outOfPrint' }">
													<img src="/resources/image/footer_logo.png" class="stateImage"/>
													<h1 class="stateText">절판된 도서입니다.</h1>
												</c:if>
												<c:if test="${ bl.b_state == 'soldOut' }">
													<img src="/resources/image/footer_logo.png" class="stateImage"/>
													<h1 class="stateText">품절된 도서입니다.</h1>
												</c:if>
											</div>
										</div>
										<div class="chkWrap">
											<input type="checkbox" class="checkbox" value="${ bl.b_num }" />
										</div>
									</div>
								</c:forEach>
							</c:when>
						</c:choose>
					</div>
			</div> <!-- 여기에 도서 리스트 넣으세요. 이름바꿔도됨 -->
	        
	    </div> <!-- content_wrap -->

		<div></div>
		<div class="contentFooter">
			<div class="top">
				<span class="sort devPowSort">DEV24 랭킹순</span>
				<span class="sort bestSort">판매량순</span>
				<span class="sort newSort">신상품순</span>
				<span class="sort lowPriceSort">낮은가격순</span>
				<span class="sort highPriceSort">높은가격순</span>
			</div>
		</div>
	</div>
	</body>
</html>
