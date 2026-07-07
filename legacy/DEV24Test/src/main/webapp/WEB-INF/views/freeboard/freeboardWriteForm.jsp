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
				$("#freeboardInsert").click(function(){
					if(!chkSubmit($('#fb_title'), "제목을")) return;
					if(!chkSubmit($('#fb_content'), "내용을")) return;
					else{
						$("#f_writeForm").attr({
							"method":"post", 
							"action":"/freeboard/freeboardInsert"
						});
						$("#f_writeForm").submit();
					}
					
					/*else{
						$.ajax({
							url:"/freeboard/freeboardInsert", 
							type:"post", 
							data:{
								
								c_num: $("#c_num").val(), 
								
							}
						});
					}*/
				});
			});
		</script>
		
		<style type="text/css">
			#content{width:1000px;}
			
			#fb_content{
				height: 200px;
			}
			
			#fb_title{
				width:690px;
			}
			
		</style>
		
		<title>freeboardWriteForm.jsp</title>
		
	</head>
	
	<body>
		<div id="content">
			
			<form id="f_writeForm" name="f_writeForm" class="form-horizontal">
			<input type="hidden" name="c_num" id="c_num" value="${login.c_num}"/>
			<input type="hidden" name="fb_author" id="fb_author" value="${login.c_nickname}"/>
				<table class="table table-bordered">
				<%--<colgroup>
						<col width="20%"/>
						<col width="80%"/>
					</colgroup>--%>
					
					<tbody>
					<%-- <tr>
						<td>작성자</td>
						<td class="text-left"><input type="text" name="fbc_author" id="fbc_author" class="form-control"/></td>
					</tr> --%>
					<tr>
						<td>작성자</td>
						<td><input type="text" value="${login.c_nickname}" class="form-control" readonly="readonly"/></td>
					</tr>
					
					<tr>
						<td>글제목</td>
						<td class="text-left"><input type="text" name="fb_title" id="fb_title" class="form-control"/><br/></td>
					</tr>
					
					<tr>
						<td>글내용</td>
						<td><textarea rows="100" cols="100" name="fb_content" id="fb_content" class="form-control" rows="8" style="resize:none"></textarea></td>
					</tr>
				
					<%--<tr>
						<td>비밀번호</td>
						<td><input type="password" name="fb_pwd" id="fb_pwd" maxlength="16"/></td>
					</tr>  --%>
					
					</tbody>		
				</table>	
			</form>
			<input type="button" value="글 등록" class="btn btn-success" id="freeboardInsert"/>
		</div>
		
		<br/>
		<br/>
		
	</body>
</html>