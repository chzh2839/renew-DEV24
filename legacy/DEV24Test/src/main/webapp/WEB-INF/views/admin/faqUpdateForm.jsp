<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>      
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>faqUpdateForm</title>
		
		<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
      	<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
      	
      	<style type="text/css">
      		#boardList .rCount{font-size:10px; color:red;}
      		
      		.required{color:red;}
      	</style>
		
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/css/bootstrap.min.css"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/image/icon.png" />
		<link rel="apple-touch-icon" href="/resources/image/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<script>
			$(function(){
				
				/* 수정 버튼 클릭 시 처리 이벤트  */
				$("#faqUpdateBtn").click(function(){
					//입력값 체크
					if(!chkData("#faq_title","제목을"))return;
					else if(!chkData("#faq_content","작성할 내용을")) return;
					else{
						$("#faq_updateForm").attr({
							"method":"post",
							"action":"/admin/faqUpdate"
						});
						$("#faq_updateForm").submit();
					}
				});
				
				//"취소" 버튼 제어
				$("#faqCancelBtn").click(function(){	
	  				$("#faq_updateForm").each(function(){
	  					this.reset();
	  				});
	  			});
				
				//"목록" 버튼 제어
				$(function(){
					$("#faqListBtn").click(function(){
						location.href="/admin/faqList"
					});
				});
				
				
				
			}); //end $ jQuery
			
			
		</script>
		
		
	
</head>
<body>
	
	<div class="contentContainer container">
		
			<%-- ================= 상세정보 보여주기 시작=============== --%>
			<div class="contentTB text-center">
				<form name="faq_updateForm" id="faq_updateForm" method="post">
				<input type="hidden" id="faq_num" name="faq_num" value="${updateData.faq_num}" />
				
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
							<td class="text-left">${updateData.faq_num}</td>
							<td>작성일</td>
							<td class="text-left">${updateData.faq_writedate}</td>
						</tr>
						<tr>
							<td>글제목</td>
							<td colspan="3" class="text-left">
								<input type="text" name="faq_title" id="faq_title" value="${updateData.faq_title}"  />
							</td>
						</tr>
						<tr>
							<td>글카테고리</td>
							<td colspan="3" class="text-left">
								<input type="text" name="faq_category" id="faq_category" value="${updateData.faq_category}"  />
							</td>
						</tr>
						<tr class="table-height">
							<td>내 용</td>
							<td colspan="3" class="text-left">
							<textarea name="faq_content" id="faq_content" rows="8" class="form-control">${updateData.faq_content}</textarea>
							</td>
						</tr>
			
					</tbody>
				</table>
			</form>
			</div>
			
			<div class="contentBtn text-right">
				<input type="button" id="faqUpdateBtn" class="btn btn-primary btn-sm" value="수정" />	
				<input type="button" id="faqCancelBtn" class="btn btn-primary btn-sm"  value="취소" />	
				<input type="button" id="faqListBtn" class="btn btn-primary btn-sm" value="목록" />	
			</div>
		
		</div>
	
</body>
</html>