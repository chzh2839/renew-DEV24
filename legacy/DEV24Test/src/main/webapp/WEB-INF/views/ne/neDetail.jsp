<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>board_detail</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
    
    <link rel="stylesheet" href="/resources/include/css/style_boot.css">
    <link rel="stylesheet" href="/resources/include/css/style_board_detail.css">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
    <!--<link rel="stylesheet" href="dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="dist/css/bootstrap-theme.css" />-->
    
    <script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    <script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
    <script type="text/javascript" src="/resources/include/js/common.js"></script>
    <script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
    
    <script>
     $(function(){
			$("#boardListBtn").click(function(){
				location.href="/ne/neList"
			});
        });
    </script>
    
    <style type="text/css">
    	#neContent {
    		height: 300px;
    	}
    </style>
    
</head>
<body>
    <!--*************************************************************-->
    
    
    <div id="content_wrap">
    	<c:set var="detail" value="${freeDetail}"/>
    	<form name="f_data" id="f_data" method="post">
    		<input type="hidden" id="fb_num" name="fb_num" value="${detail.fb_num}"/>
 		</form>
        
 		<%-- <div id="pwdChk" class="authArea">
			<form name="f_pwd" id="f_pwd">
				<!--<input type="hidden" name="num" id="num" value="${detail.num}" />-->
				<label for="passwd" id="l_pwd">비밀번호 : &nbsp;</label>
				<input type="password" name="passwd" id="passwd" />
				
				<button type="button" class="btn btn-default" id="pwdBtn">확인</button>
				<button type="button" class="btn btn-default" id="pwdCancelBtn">취소</button>
				<span id="msg"></span>
			</form>
		</div> --%>
		
		<div class="text-right btnArea">
            <input type="button" id="boardListBtn" value="글목록" class="btn btn-primary" />
		</div>
	
		<form id="detail">
			<table summary="게시판 상세 페이지" class="table" border="0">
				<tr>
					<th>글 번 호</th>
					<td>${nvo.ne_num}</td>
					<th class="th_date">작 성 일</th>
					<td>${nvo.ne_date}</td>
				</tr>
				
				<tr>
					<th>조회수</th>
					<td>${nvo.ne_readcnt}</td>
				</tr>
				
				<tr>
					<th>글 제 목</th>
					<td colspan="3">${nvo.ne_title}</td>
				</tr>
				<tr>
					<th>작 성 자</th>
					<td colspan="3">DEV24</td>
				</tr>
				<tr>
					<th>글 내 용</th>
					<td colspan="3" id="neContent" >
					<c:if test="${ not empty nvo.ne_imgurl }">
						<img alt="" src="/uploadStorage/neboard/${ nvo.ne_imgurl }" style="height: 250px;"><br/>
					</c:if>
						${nvo.ne_content}</td>
				</tr>
				
				<%-- <tr>
					<th>이 미 지</th>
					<td colspan="3"><img src="${detail.fb_img_url}"/></td>
				</tr> --%>
			</table>
		</form>
		<jsp:include page="necmt.jsp"/>
    </div> <!-- content_wrap -->
    
    <!--*************************************************************-->
    
   
    
</body>
</html>