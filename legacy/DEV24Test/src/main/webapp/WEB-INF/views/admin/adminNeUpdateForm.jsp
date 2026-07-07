<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css">
	    <link rel="stylesheet" href="/resources/include/css/style_board_detail.css">
	    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
	    <!--<link rel="stylesheet" href="dist/css/bootstrap.min.css" />
	    <link rel="stylesheet" href="dist/css/bootstrap-theme.css" />-->
	    
	    <script src="/resources/include/js/jquery-1.12.4.min.js"></script>
	    <script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
	    <script type="text/javascript" src="/resources/include/js/common.js"></script>
	    <script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
    
		<meta charset="UTF-8">
		<title>freeboardUpdateForm</title>
		
		<script type="text/javascript">
			$(function(){
				$("#boardListBtn").click(function(){
					location.href="/freeboard/freeboardList";
				});
				
				$("#boardUpdateFormBtn").click(function(){
					
					console.log($("#fb_num").val());
					console.log($("#fb_title").val());
					console.log($("#fb_content").val());
					
					if(!chkSubmit("#fb_title", "제목을")) return;
					else if (!chkSubmit("#fb_content", "내용을")) return;
					else{
						$("#updateForm").attr({
							"method":"post", 
							"action":"/freeboard/freeboardUpdate"
						});
						$("#updateForm").submit();
					}
						
				});
				
				var textarea = $('#fb_content').val();
				var replaced = ""; 
				replaced =textarea.replaceAll("<br>", "");
				$("#fb_content").val(replaced);
				

				$("#boardCancelBtn").click(function(){
					$("#detail").each(function(){
						this.reset();
					});
				});
				
				
			});
			
		</script>
		
		<style>
			#boardUpdateFormBtn, #boardCancelBtn{float:right; margin:10px;}
			
			br{display:none;}
		</style>
		
	</head>
	<body>
		    <!--*************************************************************-->
    
    
    <div id="content_wrap">
    	<c:set var="updateData" value="${updateData}"/>
    	
    	
        <%-- <div id="pwdChk" class="authArea">
			<form name="f_pwd" id="f_pwd">
				<!--<input type="hidden" name="num" id="num" value="${detail.num}" />-->
				<label for="passwd" id="l_pwd">비밀번호 : &nbsp;</label>
				<input type="password" name="passwd" id="passwd" />
				
				<button type="button" class="btn btn-default" id="pwdBtn">확인</button>
				<button type="button" class="btn btn-default" id="pwdCancelBtn">취소</button>
				<span id="msg"></span>
			</form>
		</div> --%>
		
		<div class="text-right btnArea">
		    <!-- <input type="button" id="boardUpdateFormBtn" value="글수정" class="btn btn-success" />
            <input type="button" id="boardDeleteBtn" value="글삭제" class="btn btn-success" /> -->
            <!-- <input type="button" id="boardReplyBtn" value="글답변" class="btn btn-success" /> -->
            <input type="button" id="boardListBtn" value="글목록" class="btn btn-primary" />
		</div>
	
			
	
		<form id="updateForm">
			<input type="hidden" id="fb_num" name="fb_num" value="${updateData.fb_num}"/>
			<table summary="게시판 상세 페이지" class="table" border="0">
				<tr>
					<th>글 번 호</th>
					<td>${updateData.fb_num}</td>
					<th class="th_date">작 성 일</th>
					<td>${updateData.fb_writeday}</td>
				</tr>
				<tr>
					<th>글 제 목</th>
					<td colspan="3"><input type="text" name="fb_title" id="fb_title" value="${updateData.fb_title}"/></td>
				</tr>
				<tr>
					<th>작 성 자</th>
					<td colspan="3">${updateData.fb_author}</td>
				</tr>
				<tr>
					<th>글 내 용</th>
					<td colspan="3"><textarea name="fb_content" id="fb_content" rows="8" cols="130" style="resize:none">${updateData.fb_content}</textarea></td>
				</tr>
				
				<%-- <tr>
					<th>이 미 지</th>
					<td colspan="3"><img src="${detail.fb_img_url}"/></td>
				</tr> --%>
			</table>
			<input type="button" id="boardUpdateFormBtn" value="글수정" class="btn btn-success" />
			<input type="button" value="취소" id="boardCancelBtn" class="btn btn-success"/>
		</form>
    </div> <!-- content_wrap -->
    
    <!--*************************************************************-->
	</body>
</html>