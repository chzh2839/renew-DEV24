$(function(){
	// 아래 함수는 서버로 받아 온 코드값에 대한 처리 함수(login.jsp 파일에 정의.)
	codeCheck();
	$('#c_id, #c_passwd').bind("keyup",function(){
		$(this).parents("div").find(".error").html("");
	});
	
	/* 로그인 버튼 클릭 시 처리 이벤트 */
	$("#loginBtn").click(function(){
		if (!formCheck($('#c_id'), $('.error:eq(0)'), "아이디를"))	return;
		else if (!inputVerify(0,'#c_id','.error:eq(0)'))	return;
		else if (!formCheck($('#c_passwd'),$('.error:eq(1)'), "비밀번호를"))	return;
		else if (!inputVerify(1,'#c_passwd', '.error:eq(1)'))	return;
		else {
			$("#loginForm").attr({
				"method":"POST",
				"action":"/customer/login"
			});
			$("#loginForm").submit();
		}
	});
	
	/* 회원가입 버튼 클릭 시 처리 이벤트 */
	$("#joinBtn").click(function(){
		location.href="/customer/join";
	});
});

