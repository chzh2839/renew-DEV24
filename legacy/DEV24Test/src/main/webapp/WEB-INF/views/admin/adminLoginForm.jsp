<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="description" content="">
	    <meta name="author" content="">
	    
		<title>DEV24 Admin Login</title>
		
		 <link rel="stylesheet" type="text/css" href="/resources/include/css/style_boot.css" />
		 <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		 <script type="text/javascript" src="/resources/include/js/common.js"></script>
		 
		 <style>
			 body{
			 	width:100%;
			 	height:100vh;
			 	font-size:20px;
			 	text-align: center;
			 	background-color: #f4eeff;
			 }
		 	#wrap{
		 		width:50%;
		 		position: absolute;
			    top: 50%;
			    left: 50%;
			    transform: translate(-50%, -50%);
		 	}
		 	table.table{
		 		width:100%;
		 		border: 3px solid #ccc;
		 		border-radius: 10px;
		 		padding:10px;
			    background-color: #fff;
		 	}
		 	
		 	.table tr td.h{
		 		text-align: right;
		 	}
		 	
		 	.table tr{
		 		height: 60px;
		 	}
		 	
		 	.table tr td{
		 		line-height: 60px;
		 	}
		 	
		 	.table tr td input.form-control{
		 		width:80%;
		 		height:40px;
		 		font-size:20px;
		 	}
		 	
		 	button.btn{
		 		width:100px;
		 		font-size:20px;
		 	}
		 	
		 </style>
		 
		 <script type="text/javascript">
		 
		 function boardPwdConfirm(){
				if(!chkSubmit("#adm_passwd", "비밀번호를")) return;
				else{
					$.ajax({
						url: "/admin/pwdConfirm", //전송 url
						type:"post", 
						data:$("#f_pwd").serialize(), 
						dataTyp:"text", 
						error:function(){
							alert("비밀번호 또는 아이디가 일치하지 않습니다. 다시한번 확인해주세요.");
						}, 
						success: function(resultData){
							var goUrl="";
							if(resultData == "fail"){
								location.href="/admin/adminLoginForm";
							}else if (resultData=="success"){
									location.href="/admin/adminIndex";
									
							}
								$("#f_data").attr("action", goUrl);
								$("#f_data").submit();
							}
						});
				}
			}
		 
		 	$(function(){
		 		
		 		//로그인시 엔터키 기능 작동
		 		$("#adm_passwd").on("keypress", function(e){
		 			if(e.keyCode == "13"){
		 				$("#login").click();
		 			}
		 		});
		 		
		 		$("#login").click(function(){
		 			boardPwdConfirm();
		 			//console.log("아이디="+ $("#adm_id").val()+ "adm_passwd="+ $("#adm_passwd").val());
		 		});
		 		
		 		$("#cancel").click(function(){
		 			$("#f_pwd").trigger("reset");
		 		});
		 		
		 		
		 		$(document).ready(function() {
		 	        window.history.pushState(null, "", window.location.href);        
		 	        window.onpopstate = function() {
		 	            window.history.pushState(null, "", window.location.href);
		 	        };
		 	    });
		 		
		 		
		 		
		 	});
		 </script>
		 
	</head>
	<body>
		<div id="wrap">
			<h3>DEV24 관리자 로그인</h3>
			<form id="f_pwd">
				<table class="table">
					<tr>
						<td class="h">아이디</td>
						<td><input type="text" name="adm_id" id="adm_id" class="form-control"></td>
					</tr>
					<tr>
						<td class="h">비밀번호</td>
						<td><input type="password" name="adm_passwd" id="adm_passwd" class="form-control"></td>
					</tr>
					<tr>
						<td colspan="2">
							<button type="button" name="login" id="login" class="btn btn-success">로그인</button>
							<button type="button" name="cancel" id="cancel" class="btn btn-default">취소</button>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</body>
</html>