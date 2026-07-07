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
		
		<title>Login</title>
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/js/login.js"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<style>
			.contentContainer{
				width:1200px;
				margin:0 auto;
				padding-bottom : 150px;
				padding-top : 40px;
			}
			.well{
				width: 40%;
				margin : 0 auto;
			}
			input[type="text"], input[type="password"]{
				width:100%;
				margin-bottom : 20px;
			}
			.col-sm-offset-2{
				text-align : center;
				
			}
		</style>
		<script>
		function codeCheck(){
			var codeNumber = '<c:out value="${codeNumber}" />';
			if(codeNumber != ""){
				// 명확한 자료형 명시를 위해 codeNumber의 타입을 정수형으로 변환.
				switch (parseInt(codeNumber)) {
					case 1:
						alert("아이디 또는 비밀번호 일치 하지 않거나 존재하지 않는 \n회원입니다. 다시 로그인해 주세요.");
						break;
				}
			}
		}
		</script>
		
		
	</head>
	<body>
		<div class="contentContainer">
		<div class="well">
		<c:if test= "${login.c_id == null or login.c_id == ''}">
			<form id="loginForm" class="form-horizontal">
					<div class="form-group">
						<label for="c_id" class="col-sm-2 control-label">아이디</label>
						<div class="col-sm-4">
							<input type="text" id="c_id" name="c_id" class="form-control" placeholder="ID">
						</div>
						<p class="form-control-static error"></p>
					</div>
					<div class="form-group">
						<label for="c_passwd" class="col-sm-2 control-label">비밀번호</label>
						<div class="col-sm-4">
							<input type="password" id="c_passwd" name="c_passwd" class="form-control" placeholder="PASSWORD">
						</div>
						<p class="form-control-static error"></p>
					</div>
					<div class="form-group">
							<div class="col-sm-offset-2 col-sm-6">
								<input type="button" value="로그인" id="loginBtn" class="btn btn-success" />
								<input type="button" value="회원가입" id="joinBtn" class="btn btn-default" />
							</div>
					</div>
			</form>
		</c:if>
		<%-- <c:if test="${login.c_id != null and login.c_id !=''}">
			<fieldset id="loginAfter">
				<legend><strong>[ ${login.c_nickname}] 님 반갑습니다.</strong></legend>
				<span id="customerMenu" class="tac">
					<a href="/customer/logout">로그아웃</a>&nbsp;&nbsp;&nbsp;
					<a href="/customer/modify">정보수정(비밀번호 변경)</a>&nbsp;&nbsp;&nbsp;
					<a href="/customer/delete">회원탈퇴</a>
				</span>
			</fieldset>
		
		</c:if>
		 --%>
		
		</div>
		
		</div>
	</body>
</html>
    