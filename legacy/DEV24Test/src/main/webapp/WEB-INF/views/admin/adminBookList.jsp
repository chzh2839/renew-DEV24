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
		
	    <!-- Bootstrap core CSS -->
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
    	<style>
    		#content_wrap{padding-top:30px;}
    	
    		.bookWrap *{
    			overflow: hidden;
			    text-overflow: ellipsis;
    		}
    		.contentHeaderCartMsg > *{
    			display: inline-block;
    		}
    		.topLeftDiv, .topRightDiv {
    			display: inline-block;
    		}
    		.selectedCartBtn, .selectedBuyBtn {
    			width: 110px;
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
		</style>
    	<script>
			$(function() {
				//listRangeSelect 받아온 값으로 활성화
    			var listRange = Number($("#listRange").val());
				$("#listRangeSelect").val(listRange).attr("selected", "true");
				
				//stateSelect 받아온 값으로 활성화
    			var stateSelect = $("#b_stateKeyword").val();
				$("#stateSelect").val(stateSelect).attr("selected", "true");
				
    			var page = Number($("#page").val());//현재페이지
    			var startPage = Number($("#startPage").val());//지금 길이의 시작페이지
    			var endPage = Number($("#endPage").val());
    			var pageLength = Number($("#pageLength").val());
    			var cateOne_num = Number(${pagination.cateOne_num});
    			var cateTwo_num = Number(${pagination.cateTwo_num});
    			var range = Number($("#range").val());
    			var category = Number(window.location.pathname.substr(12, 14));// "/admin/book/00" 에서  "00"만 추출

    			//버튼 클릭 여부에 따라 실행할 기본 uri 틀
				var uri = "/admin/book/"+cateOne_num+cateTwo_num+"?page=";
				
				//카테고리 선택에 따른 소분류 select 박스 출력
				//먼저 대분류 값을 지정한 후 동적태그 생성 후 소분류 값 지정
				$("#cateOne_num").val(cateOne_num).attr("selected", "true");
				if (cateOne_num == 0) {
					$("#cateTwo_num").html("<option value='0'>소분류</option>");
				} else if (cateOne_num == 1){
					$("#cateTwo_num").append("<option value='1'>프로그래밍 언어</option>")
									 .append("<option value='2'>OS/데이터베이스</option>")
									 .append("<option value='3'>웹사이트</option>")
									 .append("<option value='4'>컴퓨터 입문/활용</option>")
									 .append("<option value='5'>네트워크/해킹/보안</option>");
				} else {//2
					$("#cateTwo_num").append("<option value='6'>IT 전문서</option>")
									 .append("<option value='7'>컴퓨터 수험서</option>")
									 .append("<option value='8'>웹/컴퓨터 입문&활용</option>");
				}
				
				//동적 태그 생성 후 선정
				$("#cateTwo_num").val(cateTwo_num).attr("selected", "true");
				
				//정렬버튼 클릭시 연결 동작
				$(".b_sort > .nav-link").click(function(){
					var index = $(".nav-link").index(this);
					var sortArr = ["best", "new", "old", "lowPrice", "highPrice"];
					$("#b_sort").val(sortArr[index]);
					goURL(category);
				});
				
				$("#checkAll").click(function(){
					if ($("#checkAll").prop("checked")) {
						$(".checkbox").prop("checked", true);
					} else {
						$(".checkbox").prop("checked", false);
					}
				});
				
				/* 대분류, 소분류 선택에 따른 페이지 이동 처리 */
				$("#cateOne_num").change(function(){
					var cateOneSelected = $("#cateOne_num").select().val();
					$("#startPage").val(1);
					$("#page").val(1);
					goURL(cateOneSelected);
				});
				$("#cateTwo_num").change(function(){
					$("#startPage").val(1);
					$("#page").val(1);
					var cateTwoSelected = $("#cateTwo_num").select().val();
					goURL(cateOne_num + cateTwoSelected + "");
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
						$("#startPage").val(parseInt(pageLength - (pageLength%range) + 1));
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
					location.href = "/admin/book/detail/"+b_num;
				});
	
				// #listRange 값이 바뀔 때마다 맞춰 출력
				$("#listRangeSelect").change(function(){
					var listRangeSelect = $("#listRangeSelect").val();
					$("#listRange").val(listRangeSelect);
					goURL(category);
				});
				//페이징 처리 종료
				
				//#stateSelect 값이 바뀔 때마다 맞춰 출력
				$("#stateSelect").change(function(){
					var stateSelect = $("#stateSelect").val();
					$("#b_stateKeyword").val(stateSelect);
					goURL(category);
				});
				
				//선택항목 등록/절판/품절 처리
				$(".updateBookStateBtn").click(function(){
					if ($(".checkbox:checked").length < 1){
						alert("선택한 도서가없습니다. \n도서를 선택해 주세요");
						return;
					}
					
					var isSame = false;//이미 처리된 상품을 똑같은 처리를 하는지 판단하는 변수
					
					var b_stateKeyword = $(this).val();
					var b_stateKeywordKR;

					if(b_stateKeyword == 'reg'){
						b_stateKeywordKR = '등록';
					}
					if(b_stateKeyword == 'soldOut'){
						b_stateKeywordKR = '품절';
					}
					if(b_stateKeyword == 'outOfPrint'){
						b_stateKeywordKR = '절판';
					}

					var bvo = new Object();
					var bNumList = new Array();
					
					$(".checkbox:checked").each(function(){
						
						var index = $(".checkbox").index(this);
						
						if ($("span.b_state:eq("+index+")").html().trim() == b_stateKeywordKR){
							alert("선택한 항목 중 이미 적용된 상품이 있습니다.\n적용되지 않은 상품을 선택해 주십시오");
							isSame = true;
							return;
						}
						
						var b_num = $(".bookWrap:eq("+index+")").attr("data-num");		
						bNumList.push(Number(b_num));
						
					});
					if(isSame) return;
					
					bvo.bNumList = bNumList;
					bvo.b_stateKeyword = b_stateKeyword;
					
					var data = JSON.stringify(bvo);
					
					console.log(bvo.toString());
					console.log(data.toString());
					updateBookState(data);
				});
				
				$("#searchBtn").click(function(){
					
					var b_searchSelect = $("#searchSelect").val();
					var b_searchKeyword = $("#searchKeyword").val();
					console.log(b_searchKeyword);
					console.log(b_searchSelect);
					
					if(!chkData("#searchKeyword", "검색어를")) return;
					
					$("#b_searchSelect").val(b_searchSelect);
					$("#b_searchKeyword").val(b_searchKeyword);
					$("#b_sort").val("");
					$("#startPage").val(1);
					$("#page").val(1);
					$("#b_stateKeyword").val("all");
					
					goURL("00");
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
			
			//패이지 이동 URI값 조합 함수
			function goURL(category){
				var url = "/admin/book/"+category;
				$("#goURL").attr({
						"method" : "get",
						"action" : url
				});
				
				$("#goURL").submit();
			};

		</script>
	</head>
	<body>
		<div id="content_wrap">
			<form id="goURL" name="goURL">
				<input type="hidden" name="page" id="page" value="${pagination.page}"/>
				<input type="hidden" name="startPage" id="startPage" value="${pagination.startPage}" />
				<input type="hidden" name="endPage" id="endPage" value="${pagination.endPage}" />
				<input type="hidden" name="pageLength" id="pageLength" value="${pagination.pageLength}" />
				<input type="hidden" name="range" id="range" value="${pagination.range}" />
				<input type="hidden" name="listRange" id="listRange" value="${pagination.listRange}" />
				<input type="hidden" name="b_sort" id="b_sort" value="" />
				<input type="hidden" name="b_stateKeyword" id="b_stateKeyword" value="${pagination.b_stateKeyword}" />
				<input type="hidden" name="b_searchKeyword" id="b_searchKeyword" value=""/>
				<input type="hidden" name="b_searchSelect" id="b_searchSelect" value=""/>
			</form>
			<div class="contentHeader">
				<div class="contentHeaderTop">
					<form id="searchForm">
						<select name="searchSelect" id="searchSelect" class='form-control'>
							<option value="all" selected="selected">전체</option>
							<option value="b_name">책제목</option>
							<option value="b_author">저자</option>
							<option value="b_pub">출판사</option>
							<option value="b_info">책정보</option>
						</select>
						<input type="text" name="searchKeyword" id="searchKeyword" class='form-control' value=''/>
						<button type="button" name="searchBtn" id='searchBtn' class='btn btn-default'>검색</button>
					</form>
				</div>
				<div class="contentHeaderBottom">
					<!-- pagination -->
					<div class="bottomLeftDiv">
						<div class="paginationBox">
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
							<select name="stateSelect" id="stateSelect" class="pull-right form-control">
								<option value="">도서상태</option>
								<option value="all">모두</option>
								<option value="reg">등록</option>
								<option value="unreg">미등록</option>
								<option value="regOrOop">등록/절판</option>
								<option value="regOrOopOrSoldOut">등록/절판/품절</option>
								<option value="outOfPrint">절판</option>
								<option value="soldOut">품절</option>
							</select>
							<select id="listRangeSelect" class="pull-right form-control">
								<option value="20">20개씩 보기</option>
								<option value="40">40개씩 보기</option>
							</select>
						</div>
					</div><!-- bottomLeftDiv -->
					<div class="topBtnWrap">
						<select name="cateOne_num" id="cateOne_num" class="form-control">
							<optgroup label="대분류">
							</optgroup>
							<option value="0">모든 도서</option>
							<option value="1">book</option>
							<option value="2">eBook</option>
						</select>
						<select name="cateTwo_num" id="cateTwo_num" class="form-control">
							<option value="0">소분류</option>
						</select>
						<button type="button" class="btn btn-default" id="cateView" >보기</button>
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
						<button type="button" class="btn btn-default pull-right updateBookStateBtn" id="selectedOOP" value="outOfPrint" >절판</button>
						<button type="button" class="btn btn-default pull-right updateBookStateBtn" id="selectedREG" value="reg" >등록</button>
						<button type="button" class="btn btn-default pull-right updateBookStateBtn" id="selectedREG" value="soldOut" >품절</button>
					</div>
				</div><!-- bottom -->
			</div> <!-- contentHeader -->
			<h1 id="listTitle"></h1>
			<table class="listWrap table table-hover">
				<colgroup>
					<col width="4%"/>
					<col width="40%"/>
					<col width="20%"/>
					<col width="10%"/>
					<col width="11%"/>
					<col width="5%"/>
					<col width="4%"/>
					<col width="5%"/>
					<col width="1%"/>
				</colgroup>
				<tr>
					<th class="text-center">도서코드</th>
					<th class="text-center">도서명</th>
					<th class="text-center">저자</th>
					<th class="text-center">출판사</th>
					<th class="text-center">출간 날짜</th>
					<th class='text-center'>가격</th>
					<th class="text-center">판매수량</th>
					<th class="text-center">상태</th>
					<th class="text-left">
						<input type="checkbox" id="checkAll"/>
					</th>
				</tr>
				<c:choose>
					<c:when test="${ not empty bList }">
						<c:forEach var="bl" items="${ bList }">
							<tbody>
								<tr class="bookWrap" data-num="${ bl.b_num }">
									<td class="b_num text-center">
										${bl.b_num}
									</td>
									<td class="b_name text-left" title="${ bl.b_name }">
										<span class="b_nameText" >${ bl.b_name }</span>
									</td>
									<td class="b_author text-center">${ bl.b_author }</td>
									<td class="b_pub text-center">${ bl.b_pub }</td>
									<td class="b-date text-center">${ bl.b_date }</td>
									<td class="b_price text-center">
										<span class="b_price_hidden"  style="display: none" >${ bl.b_price }</span>
										<fmt:formatNumber value="${ bl.b_price }"/>
									</td>
									<td class="salescnt text-center">
										<span class="salescnt">${ bl.salescnt }</span>
									</td>
									<td class="b_state text-center">
											<c:if test="${ empty bl.b_state }">
												<span class="b_state" style="color:blue">
													등록
												</span>
											</c:if>
											<c:if test="${ bl.b_state == 'outOfPrint' }">
												<span class="b_state" style="color:gray">
													절판
												</span>
											</c:if>
											<c:if test="${ bl.b_state == 'unreg' }">
												<span class="b_state" style="color:red">
													미등록
												</span>
											</c:if>
											<c:if test="${ bl.b_state == 'soldOut' }">
												<span class="b_state" style="color:orange">
													품절
												</span>
											</c:if>
									</td>
									<td class="chkWrap">
										<input type="checkbox" class="checkbox text-center" value="${ bl.b_num }" />
									</td>
								</tr><!-- .bookWrap -->
							</tbody>
						</c:forEach>
					</c:when>
				</c:choose>
			</table><!-- .listWrap -->
		</div><!-- content_wrap -->
	</body>
</html>
