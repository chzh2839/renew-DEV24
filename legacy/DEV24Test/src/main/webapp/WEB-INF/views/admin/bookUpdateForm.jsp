<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%  pageContext.setAttribute("enter", "\n"); %>
<!DOCTYPE html>

<html lang="ko">
	<head>
		<meta charset="UTF-8" />
		<!-- html4 : 파일의 인코딩 방식 지정 -->
		<!--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />-->
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		<link rel="shortcut icon" href="../image/icon.png" />
		<link rel="apple-touch-icon" href="../image/icon.png" />
		
	    <!-- Bootstrap core CSS -->
	    <link href="/resources/include/dist/css/bootstrap.min.css" rel="stylesheet">
	    <link href="/resources/include/dist/css/bootstrap-theme.min.css" rel="stylesheet">
    	<link rel="stylesheet" href="/resources/include/css/adminPage.css">
    	<!-- font -->
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/common.js"></script>
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<style>
			textarea{
				resize: none;
			}
			.text-right {
				text-align: right;
			}
		</style>
		<script>
			$(function(){
				/* 저장 버튼 클릭시 처리 이벤트 */
				$("#bookInsert").click(function(){
					//입력값 체크
					if (!chkSubmit("#b_name", "도서명을")) return;
					if (!chkSubmit("#b_author", "저자를")) return;
					if (!chkSubmit("#b_pub", "출판사를")) return;
					if (!chkSubmit("#b_date", "출간날짜를")) return;
					if (!chkSubmit("#b_price", "가격을")) return;
					if (!chkSubmit("#cateOne_num", "대분류를")) return;
					if (!chkSubmit("#cateTwo_num", "소분류를")) return;
					
					//개행, 들여쓰기, 공백 치환
					var b_info = $("#b_info").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
											
					var b_authorinfo = $("#b_authorinfo").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
					
					var b_list = $("#b_list").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
					
					$("#b_info").val(b_info);
					$("#b_authorinfo").val(b_authorinfo);
					$("#b_list").val(b_list);
					
					var requestParam = new XMLHttpRequest();
					
					var files = $("#listcoverFile").val() 
								+ $("#detailcoverFile").val()
								+ $("#detailFile").val();
					
					//도서 입력값 bookInsert로 전송
					if(files.replace(/\s/g, "") == ""){
						$("#listcoverFile").attr("disabled", "true");
						$("#detailcoverFile").attr("disabled", "true");
						$("#detailFile").attr("disabled", "true");
						
						$("#f_bookInsert").attr({
							"method" : "post",
							"action" : "/admin/book/bookUpdate"
						});
					}else {
						$("#f_bookInsert").attr({
							"method" : "post",
							"encType" : "multipart/form-data",
							"action" : "/admin/book/bookUpdate"
						});
						
					}
					$("#f_bookInsert").submit();
				});
					
					$("#bookInsertResetBtn").click(function(){
						$("#f_bookInsert").each(function(){
							this.reset();
						});
					});
					
					$("#bookListBtn").click(function(){
						location.href="admin/book/0/0"
					});
			});
		</script>
	</head>
	<body>
		<div id="content_wrap">
			<form action="" class="form-group" id="f_bookInsert">
				<div class="container-fluid">
					<input type="hidden" name="b_num" value="${ vo.b_num }" />
					<h2 id="tit">도서 수정</h2>
					<table class="table table-condensed">
						<%-- <colgroup>
							<col width="20%" />
							<col width="80%" />
						</colgroup> --%>
						<tr>
							<td><label>도서번호</label>&nbsp;&nbsp;&nbsp;${ vo.b_num }</td>
							<td>
								<select name="cateOne_num" id="cateOne_num">
									<option >대분류</option>
									<option value="1" selected>도서</option>
									<option value="2">ebook</option>
								</select>
							</td>
							<td>
								<select name="cateTwo_num" id="cateTwo_num">
									<option >소분류</option>
									<option value="1" selected>프로그래밍 언어</option>
									<option value="2">OS/데이터베이스</option>
									<option value="3">웹</option>
									<option value="4">컴퓨터 입문</option>
									<option value="5">네트워크/해킹/보안</option>
									<option value="6">IT</option>
									<option value="7">컴퓨터 시험</option>
									<option value="8">웹/컴퓨터 기초</option>
								</select>
							</td>
							<td>
								<label for="b_date">출간날짜</label>
								<input type="date" name="b_date" id="b_date" value='2020-11-11'/>
							</td>
							<td>
								<label for="b_price">가격</label>
								<input type="number" name="b_price" id="b_price" value="25200"/>
							</td>
						</tr>
						<tr>
							<td></td>
							<td><label for="b_title">작가</label></td>
							<td><input type="text" id="b_author" name="b_author" class="form-control" value='${ vo.b_author }'/></td>
							<td><label for="b_pub" class="text-right">출판사</label></td>
							<td><input type="text" id="b_pub" name="b_pub" class="form-control" value="${ vo.b_pub }"/></td>
						</tr>
						<tr>
							<td colspan="1"><label for="b_name">책제목</label></td>
							<td class="text-left" colspan="4"><input type="text" id="b_name" name="b_name" class="form-control" value="${ vo.b_name }" /></td>
						</tr>
						<tr>
							<td><label for="b_content">목차</label></td>
							<td colspan="4"><textarea name="b_list" id="b_list" cols="30" rows="10" class="form-control" rows="8">
${ fn:replace(fn:replace(vo.b_list, '&nbsp;', ' '), '<br/>', enter) }
							</textarea></td>
						</tr>
						<tr>
							<td><label for="b_content">책 소개</label></td>
							<td colspan="4"><textarea name="b_info" id="b_info" cols="30" rows="10" class="form-control" rows="8">
${ fn:replace(fn:replace(vo.b_info, '&nbsp;', ' '), '<br/>', enter) }
							</textarea></td>
						</tr>
						<tr>
							<td><label for="b_content">저자 소개</label></td>
							<td colspan="4"><textarea name="b_authorinfo" id="b_authorinfo" cols="30" rows="10" class="form-control" rows="8">
${ fn:replace(fn:replace(vo.b_authorinfo, '&nbsp;', ' '), '<br/>', enter) }
							</textarea></td>
						</tr>
						<tr>
							<td>
								미니 커버 사진(listcover)<br/>
								<span class='imgMsg' style="color:red">*수정시에만 업로드</span>
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="listcoverFile" id="listcoverFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
						<tr>
							<td>
								커버사진(detailcover)<br/>
								<span class='imgMsg' style="color:red">*수정시에만 업로드</span>
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="detailcoverFile" id="detailcoverFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
						<tr>
							<td>
								상세 이미지(detail)<br/>
								<span class='imgMsg' style="color:red">*수정시에만 업로드</span>
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="detailFile" id="detailFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
					</table>
					<div class="text-right">
						<button type="button" id="bookInsert" class="btn btn-success" >저장</button>
						<button type="button" id="bookInsertResetBtn" class="btn btn-default" >입력정보 초기화</button>
						<button type="button" id="bookListBtn" class="btn btn-default">목록</button>
					</div>
				</div>
			</form>
		</div><!-- content_wrap -->
	</body>
</html>
