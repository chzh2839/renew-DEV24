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
		
		<title>join</title>
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css" />
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/js/join.js"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" /> -->
		
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
			div.form-group-sm{
				margin-bottom:10px;
			}
			div.form-group-sm > *{
				float:left;
			}
			div.form-group-sm:after{
				clear:both;
				display:block;
				content:'';
			}
			div.col-sm-3{
				margin-left:20px;
				margin-right:20px;
			}
			input[type="checkbox"]{
				margin-left:10px;
			}
			div.col-sm-offset-2{
				text-align : center;
			}
			#emailConfirmBtn{
				margin-left:10px;
			}
			
		</style>
		
		<script>
		function codeCheck(){
			var codeNumber = '<c:out value="${codeNumber}" />';
			if(codeNumber != ''){
				switch (parseInt(codeNumber)) {
					case 1:
						alert("이미 가입된 회원입니다!");
						break;
					case 2:
						alert("회원가입 처리가 실패하였습니다. 잠시 후 다시 시도해 주십시오.");
						break;
				} 
			}
		}
		</script>
		
	</head>
	<body>
		<div class="contentContainer">
			<div class="well">
				<form id="customerForm" class="form-horizontal">
					<input type="hidden" name="c_email" id="c_email" />
					<input type="hidden" name="c_pinno" id="c_pinno" />
					<div class="form-group form-group-sm">
						<label for="c_id" class="col-sm-2 control-label">사용자 ID</label>
						<div class="col-sm-3">
							<input type="text" id="c_id" name="c_id" maxlength="12" class="form-control" placeholder="USER ID" />
						</div>
						<div class="col-sm-2">
							<input type="button" id="idConfirmBtn" value="아이디 중복체크" class="btn btn-primary" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>				
					</div>
					<div class="form-group form-group-sm">
						<label for="c_passwd" class="col-sm-2 control-label">비밀 번호</label>
						<div class="col-sm-3">
							<input type="password" id="c_passwd" name="c_passwd" maxlength="15" class="form-control" placeholder="PASSWORD" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					<div class="form-group form-group-sm">
						<label for="userPwCheck" class="col-sm-2 control-label">비밀번호 확인</label>
						<div class="col-sm-3">
							<input type="password" id="userPwCheck" name="userPwCheck" maxlength="15" class="form-control" placeholder="PASSWORD CONFIRM" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					<div class="form-group form-group-sm">
						<label for="c_phone" class="col-sm-2 control-label">핸드폰 번호</label>
						<div class="col-sm-3">
							<input type="text" id="c_phone" name="c_phone" maxlength="15" class="form-control" placeholder="PHONE Number" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					<div class="form-group form-group-sm">
						<label for="birth" class="col-sm-2 control-label">생년월일</label>
						<div class="col-sm-3">
							<input type="text" id="birth" name="birth" maxlength="6" class="form-control" placeholder="주민등록 번호 6자리" />
						</div>
						<div class="col-sm-2">
							<input type="text" id="gender" name="gender" maxlength="15" class="form-control" placeholder="주민등록 번호 7번째 1자리" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					<div class="form-group form-group-sm">
						<label for="c_name" class="col-sm-2 control-label">회원이름</label>
						<div class="col-sm-3">
							<input type="text" id="c_name" name="c_name" maxlength="10" class="form-control" placeholder="NAME" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					<div class="form-group form-group-sm">
						<label for="c_nickname" class="col-sm-2 control-label">회원별명</label>
						<div class="col-sm-3">
							<input type="text" id="c_nickname" name="c_nickname" maxlength="10" class="form-control" placeholder="NICKNAME" />
						</div>
						<div class="col-sm-2">
							<input type="button" id="nickConfirmBtn" value="별명 중복체크" class="btn btn-primary" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
				
					<div class="form-group form-group-sm">
						<label for="emailName" class="col-sm-2 control-label">회원 이메일</label>
						<div class="col-sm-3">
							<input type="text" id="emailName" name="emailName" maxlength="60" class="form-control" placeholder="EMAIL" />
							<p class="form-control-static error"></p>
						</div>
						<div class="col-sm-2">
								<select id="emailDomain" class="form-control">
									<option value="naver.com">네이버</option>
									<option value="daum.net">다음</option>
									<option value="gmail.com">gmail</option>
								</select>
						</div>
						<div class="col-sm-2">
							<input type="button" id="emailConfirmBtn" value="이메일 중복체크" class="btn btn-primary" />
						</div>
					</div>
					
					<div class="form-group form-group-sm">
						<label for="c_address" class="col-sm-2 control-label">주소</label>
						<div class="col-sm-3">
							<input type="text" id="c_address" name="c_address" maxlength="30" class="form-control" placeholder="ADDRESS" />
						</div>
						<div class="col-sm-5">
							<p class="form-control-static error">
							</p>
						</div>	
					</div>
					

					<div class="form-group form-group-sm">
                       <label for="cInterest" class="col-sm-2 control-label">관심 분야</label>
                       <label><input type="checkbox" name="cInterest" value="1" />프로그래밍 언어</label>
                       <label><input type="checkbox" name="cInterest" value="2" />OS/데이터베이스</label>
                  	   <label><input type="checkbox" name="cInterest" value="3" />웹사이트</label>
                       <label><input type="checkbox" name="cInterest" value="4" />네트워크/해킹/보안</label>
					   <label><input type="checkbox" name="cInterest" value="5" />컴퓨터입문/활용</label>
					   <label><input type="checkbox" name="cInterest" value="6" />IT전문서</label>
					   <label><input type="checkbox" name="cInterest" value="7" />컴퓨터수험서</label>
					   <label><input type="checkbox" name="cInterest" value="8" />웹/컴퓨터입문&활용</label>
                    </div>
                        
                    <div class="form-group form-group-sm">
                         <label for="cNletter" class="col-sm-2 control-label">뉴스레터 동의</label>
                        <label><input type="checkbox" name="cNletter" value="0" />email</label>
						<label><input type="checkbox" name="cNletter" value="1" />문자</label>
                    </div>
					
					<div class="form-group">
						<div class="col-sm-offset-2 col-sm-6">
							<input type="button" value="확인" id="joinInsert" class="btn btn-success" />
							<input type="button" value="재작성" id="joinReset" class="btn btn-default" />
							<input type="button" value="취소" id="joinCancelt" class="btn btn-default" />
						</div>
					</div>

					
				</form>
			</div>	
			
		</div>
		
		
	</body>
</html>
    