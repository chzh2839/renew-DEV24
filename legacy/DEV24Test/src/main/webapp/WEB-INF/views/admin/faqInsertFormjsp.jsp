<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
		
		<title>qwriteForm</title>
		
		<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
      	<link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
      	
      	<style type="text/css">
      		#boardList .rCount{font-size:10px; color:red;}
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
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		
		<script>

			function processText() {
			 var txtBox = document.getElementById("faq_content");
			 var lines = txtBox.value.split("\n");

			 // generate HTML version of text
			 var resultString  = "<p>";
			 for (var i = 0; i < lines.length; i++) {
			   resultString += lines[i] + "<br />";
			 }
			 resultString += "</p>";

			}
		
		
		$(function(){
			
			/* 저장 버튼 클릭시 처리이벤트 */
			$("#faqInsertBtn").click(function(){
				//입력값 체크
				if(!chkData("#faq_title","제목을")) return;
				else if (!chkData($('#faq_category'),"카테고리 내용을")) return;
				else if (!chkData($('#faq_content'),"작성할 내용을")) return;
				else{
					$("#faq_insertForm").attr({
      					"method":"post",
      					"action":"/admin/faqInsert"
      				});
      				$("#faq_insertForm").submit();
				}
			});
			
			//"다시쓰기" 버튼 제어
			$("#faqCancelBtn").click(function(){	
  				$("#faq_insertForm").each(function(){
  					this.reset();
  				});
  			});
			
			//"목록" 버튼 제어
			$(function(){
				$("#faqListBtn").click(function(){
					location.href="/admin/faqList"
				});
			});
			
			
		}); //$종료
		

		
		</script>
		
		
	</head>
	<body>
			<body>
		<div class="container">
			<div class="text-center">
				<form id="faq_insertForm" name="faq_insertForm" class="form-horizontal">
					<table class="table table-bordered">
						<colgroup>
							<col width="20%">
							<col width="80%">
						</colgroup>
						<tbody>
							<tr>
								<td>카테고리</td>
								<td class="text-left"><input type="text" name="faq_category" id="faq_category" class="form-control" maxlength="10"></td>
							</tr>
							<tr>
								<td>글제목</td>
								<td class="text-left"><input type="text" name="faq_title" id="faq_title" class="form-control" maxlength="20"></td>
							</tr>
							<tr>
								<td>내용</td>
								<td><textarea name="faq_content" id="faq_content" class="form-control" rows="8"></textarea></td>
							</tr>
							
							
						</tbody>
					</table>
					
					<div class="text-right">
	  					<input type="button" value="저장" class="btn btn-success" id="faqInsertBtn">
	  					<input type="button" value="취소" class="btn btn-success" id="faqCancelBtn">
	  					<input type="button" value="목록" class="btn btn-success" id="faqListBtn">
					</div>
				</form>
			</div>
		</div>
		
	</body>
	</body>
</html>
    