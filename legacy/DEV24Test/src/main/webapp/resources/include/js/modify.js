var loginUserId = "";
$(function(){
	codeCheck();
	emailCheck();
	
	var message = ["기존 비밀번호를 입력해 주세요.",
	               "영문,숫자,특수문자만 가능. 8 ~ 15자 입력해 주세요.",
	               "비밀번호와 비밀번호 확인란은 값이 일치해야 합니다.",
	               "- 포함 입력해 주세요. 예시) 010-0000-0000"];
	$('.error').each(function(index){
		$('.error').eq(index).html(message[index]);
	});
	
	$('#oldUserPw, #c_passwd, #userPwCheck, #c_phone').bind("focus",function(){
		var idx = $("#oldUserPw, #c_passwd, #userPwCheck, #c_phone").index(this);
		//console.log("대상 : "+ idx );
		$(this).parents(".form-group").find(".error").html(message[idx]);
	});	
	
	/* 확인 버튼 클릭 시 처리 이벤트 */
	$("#modify").click(function(){
		//입력값 체크
		if (!formCheck($('#oldUserPw'), $('.error:eq(0)'), "기존 비밀번호를"))	return;
		else if (!inputVerify(1,'#oldUserPw', '.error:eq(0)'))	return;
		else if (!formCheck($('#c_phone'), $('.error:eq(3)'), "전화번호를"))	return;
		else if (!inputVerify(2,'#c_phone', '.error:eq(3)'))	return;
		else if (!formCheck($('#emailName'), $('.error:eq(4)'), "이메일 주소를"))	return;
		else { 			
			if($('#c_passwd').val()!=""){
				if (!inputVerify(1,'#c_passwd', '.error:eq(1)'))	return;
				if (!idPwdCheck()) return;
			}
			if($('#userPwCheck').val()!=""){
				if (!inputVerify(1,'#userPwCheck', '.error:eq(2)'))	return;
			}
			if($('#c_passwd').val()!="" && $('#userPwCheck').val()!=""){
				if (!passwordCheck()) return;
			}
			$("#c_email").val($("#emailName").val()+"@"+$("#emailDomain").val());
			$("#memberForm").attr({
				"method":"post",
				"action":"/customer/modify"
			});
			$("#memberForm").submit();
		}
	});	

	$("#modifyReset").click(function(){
		$("#memberForm").each(function(){
			this.reset();
		}); 
	});
	
	$("#modifyCancel").click(function(){
		location.href="/customer/login";
	});
});

function passwordCheck(){
	if($("#c_passwd").val() != $("#userPwCheck").val()){
		alert("패스워드 입력이 일치하지 않습니다");
		$("#c_passwd").val("");
		$("#userPwCheck").val("");
		$("#c_passwd").focus();
		return false;
	}		
	return true;
}

function idPwdCheck(){
	var userId = loginUserId;
	var userPw = $("#c_passwd").val();
	if( userPw.indexOf(userId) > -1 ){
		alert("비밀번호에 아이디를 포함할 수 없습니다.");
		$("#c_passwd").val("");
		$("#c_passwd").focus();
		return false;
	}else{		
		return true;
	}
}
