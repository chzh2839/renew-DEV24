<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<link rel="stylesheet" href="/resources/include/css/style_boot.css">
		<link rel="stylesheet" href="/resources/include/css/style_board_content.css">
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/common.js"></script>
		
		<script type="text/javascript">
			$(function(){
			/* 저장 버튼 클릭시 처리 이벤트 */
				$("#neInsertBtn").click(function(){
					//입력값 체크
					if (!chkSubmit("#ne_cate", "분류를")) return;
					if (!chkSubmit("#ne_title", "제목을")) return;
					if (!chkSubmit("#ne_content", "내용을")) return;
					
					//개행, 들여쓰기, 공백 치환
					var ne_content = $("#ne_content").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
					
					//치환한 문자열 재삽입
					$("#ne_content").val(ne_content);
					
					//도서 입력값 bookInsert로 전송
					$("#f_neInsert").attr({
						"method" : "post",
						"encType" : "multipart/form-data",
						"action" : "/admin/neInsert"
					});
					$("#f_neInsert").submit();
				});
					
					$("#neResetBtn").click(function(){
						$("#f_neInsert").each(function(){
							this.reset();
						});
					});
					
					$("#neListBtn").click(function(){
						location.href="admin/neList";
					});
			});
		</script>
		
		<style type="text/css">
			
			textarea{
				resize: none;
			}
			.text-right {
				text-align: right;
			}
		</style>
		
		<title>freeboardWriteForm.jsp</title>
		
	</head>
	
	<body>
		<div id="content_wrap">
			<form action="" class="form-group" id="f_neInsert">
				<div class="container-fluid">
					<h2 id="tit">공지사항/이벤트 게시글 등록</h2>
					<table class="table table-condensed">
						<colgroup>
							<col width="20%" />
							<col width="80%" />
						</colgroup> 
						<tr>
							<td>
								<label for="ne_cate">분류</label>
							</td>
							<td>
								<select name="ne_cate" id="ne_cate">
									<option selected="selected">분류</option>
									<option value="notice">공지사항</option>
									<option value="event">이벤트</option>
								</select>
							</td>
						</tr>
						<tr>
							<td ><label for="ne_title">제목</label></td>
							<td class="text-left" colspan="4">
								<input type="text" id="ne_title" name="ne_title" class="form-control" value="" />
							</td>
						</tr>
						<tr>
							<td><label for="ne_content">내용</label></td>
							<td >
								<textarea name="ne_content" id="ne_content" cols="30" rows="10" class="form-control" rows="8"></textarea>
							</td>
						</tr>
						<tr>
							<td></td>
							<td class="text-left" >
								<input type="file" name="imgFile" id="imgFile" class="margin_top btn btn-block" accept="image" />
							</td>
						</tr>
					</table>
					<div class="text-right">
						<button type="button" id="neInsertBtn" class="btn btn-success" >저장</button>
						<button type="button" id="neResetBtn" class="btn btn-default" >입력정보 초기화</button>
						<button type="button" id="neListBtn" class="btn btn-default">목록</button>
					</div>
				</div>
			</form>
		</div><!-- content_wrap -->
	</body>
</html>