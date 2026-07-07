<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Insert title here</title>
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/image/icon.png" />
		<link rel="apple-touch-icon" href="/resources/image/icon.png" />
		
		<!--IE8이하 브라우저에서 HTML5를 인식하기 위해서는 아래의 패스필터를 적용하면 된다.(조건부주석) -->
		<!--[if lt IE 9]>
			<script src="/resources/js/html5shiv.js"></script>
		<![endif]-->
		
		<link rel="stylesheet" href="/resources/include/css/style_purchaseFinished.css">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		

	</head>
	<body>
		<div id="content_wrap">
	      <p>구매가 완료되었습니다.</p>
	      
	       <table class="table" border="1">
	               <colgroup>
	                   <col width="20%" />
	                   <col width="30%" />
	                   <col width="20%" />
	                   <col width="30%" />
	               </colgroup>
	                  <tr>
	                    <th>수령자</th>
	                    <td>${pvo.p_receiver}</td>
	                    <th>수령자연락처</th>
	                    <td>${pvo.p_receivephone}</td>
	                </tr>
	                  <tr>
	                    <th>우편번호</th>
	                    <td colspan="3">${pvo.p_zipcode}</td>
	                </tr>
	                   <tr>
	                    <th>배송주소</th>
	                    <td colspan="3">${pvo.p_address}</td>
	                </tr>
	                  <tr>
	                    <th>구매금액</th>
	                    <td colspan="3">
	                    	<fmt:formatNumber value="${pvo.p_price}" pattern="#,###" />
	                    </td>
	                </tr>
	            </table>
	        
	    </div> <!-- content_wrap -->
	</body>
</html>