<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
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
		
		<link rel="stylesheet" href="/resources/include/css/style_review.css" />
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		
		<!-- 페이지 네비게이션은 게시물의 총수를 이용하여 목록 수 만큼 페이지를 구현.  -->
		<script type="text/javascript" src="/resources/include/js/pagenavigator.js"></script>		
		
		<style type="text/css">
			.review_table{
			    border: 2px solid #ccc;
    			width: 98%;
			}
			.review_table td{
				padding-left:20px !important;
			}
			.review_table *{
				font-size: 14px !important;
			}
			#review_tab *	{
				font-size: 16px !important;
			}
			tr.tr_revcmtInsert{
				border-bottom: none !important;
			}
			
			.re_imgurl{
				width:300px;
				display:block;
			}
			.re_imgurl img{
				width:100%;
			}
			
			.td_content > span{
				float : left;
			}
			.td_content:after{
				clear : both;
				display : block;
				content : '';
			}
			.td_content > span:first-child{
				margin-right : 10px;
			}
			
			#orderby_when{
				float:right;
				margin:15px;
			}
			.reviewUpdateBtn, .reviewDeleteBtn{
				margin-left : 30px;
			}
			.pagination > li{
				display : inline-block;
				margin-right : 10px;
			}
			.pagination > li:first-child:before{
				content:'**  ';
				
			}
			.pagination > li:last-child:after{
				content:'  **';
				
			}
			.pagination li.active > a{
				text-decoration : underline;
			}
			    
		</style>
		
		<script type="text/javascript">
			$(function(){
				var b_num = ${vo.b_num};
				var reviewType_sort = "";
				var orderby_when = $("#orderby_when").val();
				var pageNum = 1;
				var paging = $(".paging");
								
				console.log("pageNum : "+pageNum);
				
				/* 리뷰 리스트 출력 (페이징) */
				function pagenav(){
					listAll(b_num, reviewType_sort, orderby_when, pageNum).then(function(count){ 
						showPage(pageNum, count, paging);
					});
				}
				pagenav();
				
				 paging.on("click",".pagination li a", function(e){
					e.preventDefault();
					var targetPageNum = $(this).attr("href");
					console.log("targetPageNum: " + targetPageNum);
					pageNum = targetPageNum;
					$("#pageNum").val(targetPageNum);
					pagenav();
			     });
				 

				
				/* 리뷰 메뉴 버튼 클래스 추가 제거 */
				$("#review_tab > div").click(function(){
					$(this).addClass("on").siblings("div").removeClass("on");
				});
				
				
				/* 이미지, 텍스트 리뷰별 리스트 출력 */
				$(".re_type > div").click(function(){
					var id = $(this).attr("id");
					reviewType_sort = id;
					orderby_when = $("#orderby_when").val();
					console.log("id:"+reviewType_sort);
					listAll(b_num, reviewType_sort, orderby_when, 1).then(function(count){ 
						showPage(1, count, $(".paging"));
					});
				});
				
				
				/* 리스트 정렬 */
				if("${data.orderby_when}" != ""){
					$("#orderby_when").val("${data.orderby_when}");
				}
				$("#orderby_wh	en").change(function(){
					var reviewType_sort = $("#review_tab > div.on").attr("id");
					orderby_when = $("#orderby_when").val();
					console.log("orderby_when : "+orderby_when);
					listAll(b_num, reviewType_sort, orderby_when, 1).then(function(count){ 
						showPage(1, count, $(".paging"));
					});
				});
				
				
				/* 리뷰 삭제 처리 */
				$(document).on("click",".reviewDeleteBtn",function(){
					var re_num = $(this).parents("tr").attr("data-renum");
					var re_score = $(this).siblings("span.re_score").text();
					var re_imgurl = $(this).parents("tr.tr1").siblings("tr.tr2").find("span.re_imgurl").attr("data-url");
					
					console.log("re_imgurl : "+re_imgurl);
					
					orderby_when = $("#orderby_when").val();
					var reviewType_sort = $("#review_tab > div.on").attr("id");
					
					if(confirm("선택하신 댓글을 삭제하시겠습니까?")){
						$.ajax({
							url : "/review/reviewDelete?re_num="+re_num+"&re_score="+re_score+"&b_num="+b_num+"&re_imgurl="+re_imgurl,
							type : "delete",
							headers : {
								"X-HTTP-Method-Override" : "DELETE"
							},
							dataType : "text",
							error : function(){
								alert("시스템 오류. 관리자에게 문의하세요.");
							},
							success : function(result){
								console.log("result : "+result);
								if(result=="SUCCESS"){
									alert("리뷰 삭제가 완료되었습니다.");
									listAll(b_num, reviewType_sort, orderby_when, pageNum).then(function(count){ 
										showPage(pageNum, count, $(".paging"));
									});
								}
							}
						});
					}
					
				});
				
				
				/* 리뷰 수정 처리 */
				$(document).on("click",".reviewUpdateBtn",function(){
					var re_num = $(this).parents("tr").attr("data-renum");
					location.href="/review/updateForm?re_num="+re_num+"&b_num="+b_num;					
				});
				

			}); // 최상위 종료
			
			
			/* 리뷰 리스트 출력 함수 */
			function listAll(b_num, reviewType_sort, orderby_when, pageNum){
				$("#review_area").html("");
				var url = "/review/all/"+b_num; //+".json"
				console.log("reviewType_sort : "+ reviewType_sort);
				var data = "reviewType_sort="+reviewType_sort+"&orderby_when="+orderby_when+"&pageNum="+pageNum;
				
				var def = new $.Deferred();
				var count = 0;
				
				// getJSON(요청url, 파라미터값, success fn, fail fn)
				$.getJSON(url, data, function(data){ // success
					console.log("list count : "+data.length); // 리뷰 개수
					replyCnt = data.length;
					$(data).each(function(index){
						var re_num = this.re_num;
						var re_score = this.re_score;
						var c_num = this.c_num;
						var c_nickname = this.c_nickname;
						var re_writedate = this.re_writedate;
						var re_type = this.re_type;
						var re_content = this.re_content;
						var pd_num = this.pd_num;
						var re_imgurl = this.re_imgurl;
						
						if(index==0){count = this.r_count;}
						
						console.log("count : "+ this.r_count);
						//console.log("re_writedate : "+ re_writedate);
						//console.log("re_score : "+ re_score);
						//console.log("re_content : "+ re_content);
						//console.log("re_imgurl : "+ re_imgurl);
						
						re_content = re_content.replace(/(\r\n|\r|\n)/g,"<br/>");
						
						addItem(re_num, c_nickname, re_score, re_content, re_writedate, re_imgurl, c_num)
					});
					def.resolve(count); 
				
				}).fail(function(){ // error
					alert("리뷰 목록을 불러오는데 실패했습니다. 잠시 후에 다시 시도해 주세요.");
				});
				return def.promise();
			}
			
			/** 새로운 글을 화면에 추가하기(보여주기) 위한 함수 */
			function addItem(re_num, c_nickname, re_score, re_content, re_writedate, re_imgurl, c_num) {
				// 새로운 글이 추가될 div태그 객체 => $("<div>")
				var wrapper_table = $("<table>");
				wrapper_table.addClass("table review_table");
				
				var new_tr1 = $("<tr>");
				new_tr1.addClass("tr1");
				new_tr1.attr({"data-num": c_num, "data-reNum":re_num});
				var td1 = $("<td>");

				var nickname_span = $("<span>");
				nickname_span.addClass("c_nickname");
				nickname_span.html(c_nickname);
				var date_span = $("<span>");
				date_span.addClass("re_writedate");
				date_span.html(re_writedate);
				
				var td2 = $("<td>");
				td2.text("평점 : ");
				var score_span = $("<span>");
				score_span.addClass("re_score");
				score_span.html(re_score);
				
				// 수정하기 버튼
				var upBtn = $("<button>");
				upBtn.attr({"type" : "button"});
				upBtn.attr("data-btn","upBtn");
				upBtn.addClass("btn btn-primary gap reviewUpdateBtn");
				upBtn.html("수정하기");
				
				// 삭제하기 버튼
				var delBtn = $("<button>");
				delBtn.attr({"type" : "button"});
				delBtn.attr("data-btn","delBtn");
				delBtn.addClass("btn btn-primary gap reviewDeleteBtn");
				delBtn.html("삭제하기");
				
				var loginedCnum = "${login.c_num}";
				console.log("loginedCnum : "+loginedCnum);
				console.log("c_num : "+c_num);
				
				td1.append(nickname_span).append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(re_writedate);
				if(loginedCnum == c_num){
					td2.append(score_span).append(" / 5점").append(upBtn).append(delBtn);
				}else{
					td2.append(score_span).append(" / 5점");
				}
				new_tr1.append(td1).append(td2);
				
				var new_tr2 = $("<tr>");
				new_tr2.addClass("tr2");
				var td3 = $("<td>");
				td3.attr("colspan", "2");
				td3.addClass("td_content");
				
				var img_span = $("<span>");
				img_span.addClass("re_imgurl");
				img_span.attr("data-url", re_imgurl);
				if(re_imgurl != ""){
					var img = $("<img>");
					img.attr("src", "/uploadStorage/review/"+re_imgurl);
					img_span.append(img);
				}
				
				var content_span = $("<span>");
				content_span.addClass("re_content");
				content_span.html(re_content);
				
				td3.append(img_span).append("&nbsp;&nbsp;&nbsp;&nbsp").append(content_span);
				new_tr2.append(td3);
				
				wrapper_table.append(new_tr1).append(new_tr2);
				$("#review_area").append(wrapper_table);

			}
			
		</script>

	</head>
	<body>
		<div id="content_wrap">
		
			<div id="review_wrap">
	           <div id="review_tab" class="re_type">
	               <div class="review_type on" id="">전체</div>
	               <div class="review_type" id="image">이미지리뷰</div>
	               <div class="review_type" id="text">텍스트리뷰</div>
	           </div>
	
			<form name="f_search" id="f_search">
				<input type="hidden" name="pageNum" id="pageNum" value="${data.pageNum}" />
			    <input type="hidden" name="amount" id="amount" value="5" />
			
				<select name="orderby_when" id="orderby_when">
                   <option value="last">최신글순</option>
                   <option value="high">평점높은순</option>
                   <option value="low">평점낮은순</option>
               </select>

	            <nav class="paging text-center"> <!-- 페이징 처리 부분 -->
				</nav> 
	          </form> 
	            
	            
	            <div id="review_area">
	            	등록된 리뷰가 없습니다.
	            </div>  <!-- review_area -->
	            
	        </div> <!-- review_wrap -->
        </div> <!-- content_wrap -->
	</body>
</html>