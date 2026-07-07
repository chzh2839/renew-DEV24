<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>     
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
		
		<title>qnaList</title>
		
		<!-- <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
      	<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" /> -->
      	<link rel="stylesheet" href="/resources/include/css/style_boot.css">
  	  <link rel="stylesheet" href="/resources/include/css/style_board_detail.css">
      	
      	<style type="text/css">
      		#boardList .rCount{font-size:10px; color:red;}
      		
      		.required{color:red;}
      		
      		#q_content{
				height:200px;
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
		
		<script type="text/javascript">
		
		$(function(){
			
			var conText = $("#q_content").val();
			$("#q_content").val("\n\n\n\n>>>>>>>>>>기존글내용>>>>>>>>>>>>>\n\n" + conText);
			
			
			
			/* 답변 저장 버튼 클릭시 처리이벤트 */
			$("#qnaInsert").click(function(){
				//입력값 체크
				if(!chkData("#c_nickname","이름을")) return;
				else if (!chkData($('#q_title'),"제목을")) return;
				else if (!chkData($('#q_content'),"작성할 내용을")) return;
				else{
					$("#q_replyForm").attr({
      					"method":"post",
      					"action":"/qna/qinsertReply"
      				});
      				$("#q_replyForm").submit();
				}
			});
			
			//"다시쓰기" 버튼 제어
			$("#resetBtn").click(function(){	
  				$("#q_replyForm").each(function(){
  					this.reset();
  				});
  			});
			
			//"목록" 버튼 제어
			$(function(){
				$("#boardListBtn").click(function(){
					location.href="/qna/qnaList"
				});
			});
			
			
		}); //$종료
		
		
		</script>
		
		
		
	</head>
	<body>
	<div id="content_wrap">
		<form id="q_replyForm" name="q_replyForm">
			<!-- 답변글 필요 -->
			<input type="hidden" name="q_num" value="${qreplyData.q_num}">
			<input type="hidden" name="c_num" value="${qreplyData.c_num}">
			<input type="hidden" name="q_repRoot" value="${qreplyData.q_repRoot}">
			<input type="hidden" name="q_repStep" value="${qreplyData.q_repStep}">
			<input type="hidden" name="q_repIndent" value="${qreplyData.q_repIndent}">
			<div>
				<div>
					원래글번호${qreplyData.q_num} &nbsp;(조회수: ${qreplyData.q_readcnt})
				</div>
				<label>작성자</label>
				<div>
					<input type="text" name="c_nickname" id="c_nickname" class="form-control"  required="required">
				</div>
				<label>카테고리</label>
				<div>
					<input type="text" name="q_category" id="q_category" value="${qreplyData.q_category}" class="form-control"  required="required">
				</div>
				<label>글제목</label>
				<div>
					<input type="text" name="q_title" id="q_title" value="[답변]${qreplyData.q_title}" class="form-control"  required="required">
				</div>
				<label>글내용</label>
				<div>
					<textarea name="q_content" id="q_content" class="form-control" rows="7" cols="100" maxlength="2000" required="required" >${qreplyData.q_content}</textarea>
				</div>
				
				<p align="right">
	  			<button type="button" id="resetBtn" class="btn btn-primary btn-sm">다시쓰기</button>
	  			<input type="button" value="저장" class="btn btn-primary btn-sm" id="qnaInsert">
	  			<input type="button" value="목록" class="btn btn-primary btn-sm" id="boardListBtn">
				</p>
				
			</div>
			
		</form>
	</div>	
	</body>
</html>
    