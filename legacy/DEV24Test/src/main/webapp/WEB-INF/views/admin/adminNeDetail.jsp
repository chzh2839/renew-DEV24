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

		var ne_num;
		var nevo;
		var dataJSON;
    
		$(function(){
			ne_num = $("#ne_num").val();
			nevo = {
				"ne_num" : ne_num
			};
			dataJSON = JSON.stringify(nevo);
			
			
			console.log("ne_num: " + ne_num);
			console.log("nevo: " + nevo);
			console.log("dataJSON: " + dataJSON);
			
			$("#boardListBtn").click(function(){
				location.href="/admin/neList"
			});
			
			$("#neDeleteBtn").click(function(){
				
				//댓글개수 리턴해주는 함수 호출 후 replyCheckResult에 담기
				replyCheck();
				
			});
			
		});
     
     
     	//댓글 개수 체크 함수
     	function replyCheck() {
			var resultData;
			if(confirm(ne_num + "번 게시글을 삭제하시겠습니까?")){
				$.ajax({
					url : "/admin/necmt/replyCheck",
					async: "false",
					type : "post",
					data : "ne_num="+$("#ne_num").val(),
					dataType : "text",
					success : function(result) {
						resultData = result;
						console.log("data: " + result);
						if (result > 0){
							if(confirm("댓글이 존재하는 게시글입니다.\n삭제하시겠습니까?")){
								neDelete(result);
							} else {
								return;
							}
						} else {
							neDelete(result);
						}
					},
					error : function(){
						alert("오류\n관리자에게 문의하세요.");
						resultData = -1;
					}
				});
			}
			console.log("resultData : " + resultData);
		}
     	 
     	//게시글 삭제 함수
     	function neDelete(replyCnt) {
			$.ajax({
				url : "/admin/neDelete",
				type : "post",
				data : "ne_num="+$("#ne_num").val()+"&replyCnt="+replyCnt,
				dataType : "text",
				success : function(data) {
					alert("게시글이 삭제되었습니다.");
					location.href="/admin/neList"
				},
				error : function(){
					alert("오류\n관리자에게 문의하세요.");
				}
			});
     	}
     	
    </script>
    
    <style type="text/css">
    	#neContent {
    		height: 300px;
    	}
    	#content_wrap {
    		margin: 0 0 0 18% !important;
    	}
    </style>
    
</head>
<body>
    <!--*************************************************************-->
    
    
    <div id="content_wrap">
    	<form name="nevo" id="nevo" method="post">
    		<input type="hidden" id="ne_num" name="ne_num" value="${nvo.ne_num}"/>
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
            <input type="button" id="neDeleteBtn" value="삭제" class="btn btn-primary" />
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
					${nvo.ne_content}
					</td>
				</tr>
				
				<%-- <tr>
					<th>이 미 지</th>
					<td colspan="3"><img src="${detail.fb_img_url}"/></td>
				</tr> --%>
			</table>
		</form>
		<jsp:include page="adminNecmt.jsp"/>
    </div> <!-- content_wrap -->
    
    <!--*************************************************************-->
    
   
    
</body>
</html>