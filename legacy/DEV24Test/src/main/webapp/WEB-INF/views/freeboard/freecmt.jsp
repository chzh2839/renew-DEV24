 <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>freecmt.jsp</title>
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css">
    	<link rel="stylesheet" href="/resources/include/css/style_board_detail.css">
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
		
		<script type="text/javascript">
			
			$(function(){
				
				var fb_num = ${freeDetail.fb_num};
				var fbc_author = $("#fbc_author").val();
			
				listAll(fb_num);
				
				$("#replyInsertFormBtn").click(function(){
					console.log(fb_num);
					console.log($("#fbc_content").val());
					console.log($("#fbc_author").val());
					console.log($("#c_num").val());
					
					if(!chkSubmit("#fbc_content", "댓글 내용을")) return;
					else if(!chkSubmit("#fbc_author", "로그인 후 댓글을"))  return;
					var insertUrl="/freecmt/freecmtInsert";
					var value = JSON.stringify({
						fb_num: fb_num,
						//fb_num:${freeDetail.fb_num},
						fbc_content: $("#fbc_content").val(),
						//fbc_author: $("#fbc_author").val(),
						fbc_author: fbc_author,
						c_num:$("#c_num").val()
					});
					
					
					
					$.ajax({
						url:insertUrl, 
						type:"post", 
						headers:{
							"Content-Type":"application/json", 
							"X-HTTP-Method-Override":"POST", 
						}, 
						dataType:"text", 
						data : value,
						error: function(){
							/*if(fbc_author != null){
								alert("시스템 오류입니다. 관리자에게 문의하세요.");
							}else if(fbc_author == null){
								alert("로그인 후 댓글 등록을 해주세요.");
							}*/
							alert("시스템 오류입니다. 관리자에게 문의하세요.");
						},
						success: function(result){
							if(result == "SUCCESS"){
								alert("댓글 등록 성공!");
								//dataReset();
								$("#commentContent").trigger("reset");
								listAll(fb_num);
							}
						}
					}); 
					
				});
			});
			
			
			function listAll(fb_num){
				$("#reviewList").html("");
				var url="/freecmt/all/"+fb_num+".json";
				
				$.getJSON(url, function(data){
					console.log("list count: "+data.length);
					replyCnt = data.length;
					
					//$(data) 에 있는 원소만큼 each 레코드의 번호, 이름, 내용, 날짜를 가져오기.. 
					$(data).each(function(){
						var fbc_num = this.fbc_num;
						var fbc_author = this.fbc_author;
						var fbc_content = this.fbc_content;
						var fbc_writeday = this.fbc_writeday;
						fbc_content = fbc_content.replace(/(\r\n|\r|\n)/g, "<br>");
						addItem(fbc_num, fbc_author, fbc_content, fbc_writeday);
					});
					
				}).fail(function(){
					alert('댓글을 불러오는데 실패했습니다. 잠시후에 다시 시도해주세요..')
				});
			}
			
			/*카페에서 받아온 함수의 소스..*/
			/* 새로운 글을 화면에 추가하기(보여주기) 위한 함수*/
			function addItem(fbc_num, fbc_author, fbc_content, fbc_writeday) {
				// 새로운 글이 추가될 div태그 객체
				var wrapper_div = $("<div class='wrapper'>");
				wrapper_div.attr("data-num", fbc_num);
				wrapper_div.addClass("panel panel-default");
				
				var new_div = $("<div>");
				new_div.addClass("panel-heading");
			
				// 작성자 정보의 이름
				var name_span = $("<span>");
				name_span.addClass("name");
				name_span.html(fbc_author + "님");
			
				// 작성일시
				var date_span = $("<span class='fbc_writeday'>");
				date_span.html(" / " + fbc_writeday + " ");
			
				// 수정하기 버튼
				var upBtn = $("<button>");
				upBtn.attr({"type" : "button"});
				upBtn.attr("data-btn","upBtn");
				upBtn.addClass("btn btn-primary gap");
				upBtn.html("수정하기");
				
				
				// 삭제하기 버튼
				var delBtn = $("<button>");
				delBtn.attr({"type" : "button"});
				delBtn.attr("data-btn","delBtn");
				delBtn.addClass("btn btn-primary gap");
				delBtn.html("삭제하기"); 
 		
				// 내용 
				var content_div = $("<div>");
				content_div.html(fbc_content);
				content_div.addClass("panel-body");
				
			
				// 조립하기
				//new_div.append(name_span).append(date_span).append(upBtn).append(delBtn);
				//new_div.append(name_span).append(date_span);
				new_div.append(name_span).append(date_span);
				wrapper_div.append(new_div).append(content_div).append("<br>");
				$("#reviewList").append(wrapper_div);
			}
		</script>
		
		<style type="text/css">
			#replyContainer{
				align-content: center;
			}
			
			/*#replyBtnArea{
				width:1200px;
				display:inline-block;
				align-content:center;
				margin:auto;
			}*/
			
			#reviewList{
				margin:30px;
				margin-top: 20px;
			}
			
			#replyContainer{margin-bottom:20px;}
			
			#replyInsertFormBtn{margin-top:-30px;}
			
			.text-right btnArea{
				margin-bottom: 10px;				
			}
			
		</style>
		
	</head>
	<body>
		<div id="replyContainer">
			<div class="text-right btnArea">
				<form id="commentContent">
					<input type="hidden" name="fb_num" id="fb_num"/>
					<input type="hidden" name="c_num" id="c_num" value="${login.c_num}"/>
					<input type="hidden" name="fbc_author" id="fbc_author" value="${login.c_nickname }"/>
					<textarea id="fbc_content" name="fbc_content" cols="130" style="resize:none"></textarea>
					<input type="button" class="btn btn-success" value="댓글등록" id="replyInsertFormBtn"/>
				</form>
			</div>

			 <div id="reviewList"></div>
			  
		</div> 
	</body>
</html>