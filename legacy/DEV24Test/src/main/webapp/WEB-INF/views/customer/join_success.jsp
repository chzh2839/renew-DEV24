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
		
		<title>회원가입 완료 화면</title>
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="/resources/js/html5shiv.js"></script>
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
		</style>
		
	</head>
	<body>
		<div class="contentContainer">	
			<div class="well">
				<div class="page-header">
					<h1>회원가입이 완료되었습니다!!<span class="label label-success">New</span></h1>
				</div>
				<h4>박수짝짝짝짝짝X1000</h4>
				<h4>나의 사이트에 오신것을 격하게 환영합니다~!!</h4>
				<h6>(8초후에 자동으로 로그인 화면으로 이동합니다.)</h6>
			</div>
		</div>
		
	</body>
</html>
    