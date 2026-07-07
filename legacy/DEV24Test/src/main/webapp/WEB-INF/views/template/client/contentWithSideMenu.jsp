<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title><tiles:getAsString name="title" /></title>
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/image/icon.png" />
		<link rel="apple-touch-icon" href="/resources/image/icon.png" />
		
		<!--IE8이하 브라우저에서 HTML5를 인식하기 위해서는 아래의 패스필터를 적용하면 된다.(조건부주석) -->
		<!--[if lt IE 9]>
			<script src="/resources/js/html5shiv.js"></script>
		<![endif]-->
		
		<link rel="stylesheet" href="/resources/include/css/style_boot.css" />
		<link rel="stylesheet" href="/resources/include/css/style_headerfooter.css" />
		<link rel="stylesheet" href="/resources/include/css/style_mypage.css" />
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<script>
        $(function(){
           // gnb 메뉴 클릭 시
           $("#gnb > li").click(function(){
               var i = $(this).index();
               console.log(i);
               
               $(this).siblings("li").removeClass("on");
               $(this).addClass("on");
               
               $("#gnb > li > ul").removeClass("on");
               $("#gnb > li > ul").eq(i).addClass("on");
           });

            // 하위메뉴 마우스 커서 이동으로 메뉴 이동
            $(".dropmenu > li").mouseover(function(){
               $(this).siblings("li").removeClass("on");
               $(this).addClass("on");
           });
            $(".dropmenu").mouseleave(function(){
               $(".dropmenu > li").removeClass("on");
                $("#gnb > li > ul").removeClass("on");
           });
          
        });
    </script> 

	</head>
	<body>
		<tiles:insertAttribute name="header" />
		
		<div id="content_wrap">
		
		<tiles:insertAttribute name="leftmenu" />
		<tiles:insertAttribute name="body" />
		
		</div> <!-- content_wrap -->
		
		<tiles:insertAttribute name="footer" />
	</body>
</html>