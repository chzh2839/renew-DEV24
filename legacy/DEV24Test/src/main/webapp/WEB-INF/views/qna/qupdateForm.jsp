<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>      
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>qnaUpdateForm</title>
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css">
		<link rel="stylesheet" href="/resources/include/css/style_board_content.css">
		
      	<style type="text/css">
      		#boardList .rCount{font-size:10px; color:red;}
      		
      		.required{color:red;}
      		
      		#q_content{
				height:200px;
			}
			#content{
				margin-bottom: 150px;
			}
      	</style>
		
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/css/bootstrap.min.css"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<script>
			$(function(){
				
				/* 수정 버튼 클릭 시 처리 이벤트  */
				$("#qnaUpdateBtn").click(function(){
					//입력값 체크
					if(!chkData("#q_title","제목을"))return;
					else if(!chkData("#q_content","작성할 내용을")) return;
					else{
						$("#q_updateForm").attr({
							"method":"post",
							"action":"/qna/qnaUpdate"
						});
						$("#q_updateForm").submit();
					}
				});
				
				//"취소" 버튼 제어
				$("#qnaCancelBtn").click(function(){	
	  				$("#q_updateForm").each(function(){
	  					this.reset();
	  				});
	  			});
				
				//"목록" 버튼 제어
				$(function(){
					$("#qnaListBtn").click(function(){
						location.href="/qna/qnaList"
					});
				});
				
				
				
			}); //end $ jQuery
			
			
		</script>
		
		
	
</head>
<body>
	
	<div id="content">
		
			<%-- ================= 상세정보 보여주기 시작=============== --%>
			<div class="contentTB text-center">
				<form name="q_updateForm" id="q_updateForm" method="post">
				<input type="hidden" id="q_num" name="q_num" value="${updateData.q_num}" />
				
				<table class="table table-bordered">
					<colgroup>
						<col width="17%">
						<col width="33%">
						<col width="17%">
						<col width="33%">
					</colgroup>
					<tbody>
						<tr>
							<td>글번호</td>
							<td class="text-left">${updateData.q_num}</td>
							<td>작성일</td>
							<td class="text-left">${updateData.q_writedate}</td>
						</tr>
						<tr>
							<td>작성자</td>
							<td colspan="3" class="text-left">${login.c_nickname}</td>
						</tr>
						<tr>
							<td>글제목</td>
							<td colspan="3" class="text-left">
								<input type="text" name="q_title" id="q_title" value="${updateData.q_title}"  />
							</td>
						</tr>
						<tr>
							<td>글카테고리</td>
							<td colspan="3" class="text-left">
								<input type="text" name="q_category" id="q_category" value="${updateData.q_category}"  />
							</td>
						</tr>
						<tr class="table-height">
							<td>내 용</td>
							<td colspan="3" class="text-left">
							<textarea name="q_content" id="q_content" rows="8" class="form-control">${updateData.q_content}</textarea>
							</td>
						</tr>
			
					</tbody>
				</table>
			</form>
			</div>
			
			<div class="contentBtn text-right">
				<input type="button" id="qnaUpdateBtn" class="btn btn-primary btn-sm" value="수정" />	
				<input type="button" id="qnaCancelBtn" class="btn btn-primary btn-sm"  value="취소" />	
				<input type="button" id="qnaListBtn" class="btn btn-primary btn-sm" value="목록" />	
			</div>
		
		</div>
	
</body>
</html>