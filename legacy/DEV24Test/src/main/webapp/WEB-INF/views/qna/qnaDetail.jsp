<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<!DOCTYPE html>
<!-- 문서 유형 : 현재 웹 문서가 어떤 HTML 버전에 맞게 작성되었는지를 알려준다. -->

<!--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
     DTD 선언문이 HTML 페이지의 가장 첫 라인에 명시되어야 웹 브라우저가 HTML 버전을 인식.
     HTML태그나 CSS를 해당 버전에 맞도록 처리하므로 웹 표준 준수를 위하여 반드시 명시되어야 한다.-->
<html lang="ko">
	<head>
		<meta charset="UTF-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		
		<title>qnaDetail</title>
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css">
 	   <link rel="stylesheet" href="/resources/include/css/style_board_detail.css">
      	
		
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
				goUrl = "/qna/qupdateForm"
				$("#q_data").attr("action", goUrl);
				$("#q_data").submit();
			});
			
			/* 삭제 버튼 클릭 시 처리 이벤트  */
			$("#qnaDeleteBtn").click(function(){
				if(confirm("정말 삭제하시겠습니까?")){
					goUrl = "/qna/qnaDelete"
					$("#q_data").attr("action", goUrl);
					$("#q_data").submit();
				}
			});
			
	
			//"목록" 버튼 제어
			$("#qnaListBtn").click(function(){
				location.href="/qna/qnaList"
			});
			
			
			/* 답변 버튼 클릭 시 처리 이벤트 */
			$("#qnaReplyBtn").click(function(){
				$("#q_data").attr({
					"method":"post",
					"action":"/qna/qreplyForm"
				});
				$("#q_data").submit();
			});
			
			
		});
			
					
			
		</script>
		
		
		
	</head>
	<body>
		<div id="content_wrap">
		
		<!-- <div align="center"><h3>글상세</h3></div> -->
		
		<form name="q_data" id="q_data" method="post">
			<input type="hidden" name="q_num" value="${detail.q_num}" />
			<!-- 이 부분은 id를 주면 안됨, 아래에서 이미 아이디를 사용중이기 때문에 -->
		</form>
				
				
			<div class="btnArea col-md-4 text-right">
			<c:if test="${login.c_num != null}">
				<input type="button" id="qnaUpdateBtn" class="btn btn-primary btn-sm" value="수정" />	
				<input type="button" id="qnaDeleteBtn" class="btn btn-primary btn-sm"  value="삭제" />	
				<input type="button" id="qnaReplyBtn" class="btn btn-primary btn-sm"  value="답변" />
			</c:if>		
				<input type="button" id="qnaListBtn" class="btn btn-primary btn-sm" value="목록" />	
			</div>
			
			
			<%-- ================= 상세정보 보여주기 시작=============== --%>
			<table class="table table-bordered">
				<colgroup>
					<col width="17%">
					<col width="33%">
					<col width="17%">
					<col width="33%">
				</colgroup>
				<tbody>
					<tr>
						<td class="text-left">글번호</td>
						<td>${detail.q_num} (조회수:${detail.q_readcnt})</td>
						<td class="text-left">작성일</td>
						<td>${detail.q_writedate}</td>
					</tr>
					<tr>
						<td class="ac">글제목</td>
						<td colspan="3">${detail.q_title }</td>
					</tr>
					<tr>
						<td class="ac">작성자</td>
						<td colspan="3">${detail.c_nickname }</td>
					</tr>
					<tr class="table-height">
						<td class="text-left">글내용</td>
						<td colspan="3">${detail.q_content }</td>
					</tr>
				
					
				</tbody>
			</table>
				
		</div>
	</body>
</html>
    