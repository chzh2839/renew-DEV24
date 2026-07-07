<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>  
<c:set var="email" value="${fn:split(customer.c_email,'@')}" />  

    
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
		
		<title>Update Member!</title>
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/js/modify.js"></script>
      	
      	<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<style>
			.well{
				margin-left:230px;
			}
			.tit_mypage{
				padding-top : 40px;
				padding-left:220px;
			}
			#memberForm{
				padding-top:30px;
			}
			#memberForm > div{
				margin-bottom: 10px;
			}
			.col-sm-3, .col-sm-2, .col-sm-5{
				display:inline-block;
			}
			.col-sm-5 p{
				font-size:13px;
			}
		</style>
		<script>
		function codeCheck(){
			var codeNumber = '<c:out value="${codeNumber}" />';
			if(codeNumber != ""){
				alert("기존 비밀번호 검증에 실패하였습니다. \n기존 비밀번호를 다시 확인해 주세요. ");
			}
		} 
		
		loginUserId = "${customer.c_id}";
		function emailCheck(){
			var email = "${c_email[1]}";
			$("#emailDomain").val(email).prop("selected", "true");
		}
		</script>
		
	</head>
	<body>
		<div class="contentContainer">
			<div class="tit_mypage">
                <h3>회원정보수정</h3>
            </div>
		
			<div class="well">
				<form id="memberForm" class="form-horizontal">
					<input type="hidden" name="c_num" id="c_num" value="${customer.c_num}" />
					<input type="hidden" name="c_email" id="c_email" />
					<div class="form-group form-group-sm">
						<label for="c_id" class="col-sm-2 control-label">사용자 ID : </label>
						<div class="col-sm-3">
							${customer.c_id}
						</div>
					</div>
					<div class="form-group form-group-sm">
						<label for="oldUserPw" class="col-sm-2 control-label">기존 비밀 번호 : </label>
						<div class="col-sm-3">
							<input type="password" id="oldUserPw" name="oldUserPw" maxlength="15" class="form-control" placeholder="기존 비밀번호 입력" >
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error"></p>
						</div>
					</div>
					<div class="form-group form-group-sm">
						<label for="userPw" class="col-sm-2 control-label">변경할 비밀 번호 : </label>
						<div class="col-sm-3">
							<input type="password" id="c_passwd" name="c_passwd" maxlength="15" class="form-control" placeholder="변경할 비밀번호 입력" >
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error"></p>
						</div>
					</div>
					<div class="form-group form-group-sm">
						<label for="userPwCheck" class="col-sm-2 control-label">변경할 비밀번호 확인 : </label>
						<div class="col-sm-3">
							<input type="password"  id="userPwCheck" name="userPwCheck" maxlength="15" class="form-control" placeholder="변경할 비밀번호 입력 확인" >
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error"></p>
						</div>
					</div>
					<div class="form-group form-group-sm">
						<label for="c_phone" class="col-sm-2 control-label">핸드폰 번호 : </label>
						<div class="col-sm-3">
							<input type="text" id="c_phone" name="c_phone" maxlength="15" class="form-control" value="${customer.c_phone}">	
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error"></p>
						</div>
					</div>	
					<div class="form-group form-group-sm">
						<label for="birth" class="col-sm-2 control-label">생년월일 : </label>
						<div class="col-sm-3">	
							${customer.c_pinno}******
						</div>
					</div>										
					<div class="form-group form-group-sm">
						<label for="userName" class="col-sm-2 control-label">회원이름 : </label>
						<div class="col-sm-3">
							${customer.c_name}
						</div>						
					</div>
					<div class="form-group form-group-sm">
						<label for="c_nickname" class="col-sm-2 control-label">회원별명 : </label>
						<div class="col-sm-3">
							${customer.c_nickname}
						</div>						
					</div>
					<div class="form-group form-group-sm">
						<label for="c_address" class="col-sm-2 control-label">주소 : </label>
						<div class="col-sm-3">
							${customer.c_address}
						</div>						
					</div>
					<div class="form-group form-group-sm">
						<label for="emailName" class="col-sm-2 control-label">회원 이메일 : </label>
						<div class="col-sm-3">
							<input type="text" id="emailName" name="emailName" maxlength="60" class="form-control" value="${c_email[0]}"> @ 
	 					</div>
	 					<div class="col-sm-2">
	 						<select id="emailDomain" class="form-control">
	 							<option value="naver.com">네이버</option>
	 							<option value="daum.net">다음</option>
	 							<option value="nate.com">네이트</option>																	
							</select> 
						</div>
						<div class="col-sm-3">
							<p class="form-control-static error"></p>
						</div>
					</div>
					<div class="form-group">	
						<div class="col-sm-offset-2 col-sm-6">
							<input type="button" value="확인" id="modify" class="btn btn-success" /> 
							<input type="button" value="재작성" id="modifyReset" class="btn btn-default" />
							<input type="button" value="취소" id="modifyCancel" class="btn btn-default" />						
						</div>	
					</div>																										
				</form>
			</div>
		</div>
		
	</body>
</html>
    