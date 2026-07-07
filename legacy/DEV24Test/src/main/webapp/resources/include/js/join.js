// 비밀번호와 비밀번호 확인 일치 여부 확인
function passwordCheck(){
	if($("#c_passwd").val() != $("#userPwCheck").val()){
		alert("비밀번호와 비밀번호 확인이 일치하지 않습니다");
		$("#userPwCheck").focus();
		$("#userPwCheck").val("");
		return false;
	}else{		
		return true;
	}
}
// 아이디에 비밀번호 포함 여부 확인
function idPwdCheck(){
	var userId = $("#c_id").val();
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

var idConfirm = 1;
$(function(){
	codeCheck();
	// 사용자에게 요구사항에 대한 문자열로 배열 초기화. 
	var message = ["영문,숫자만 가능. 6 ~ 12자로 입력해 주세요",
	               "영문,숫자,특수문자만 가능. 8 ~ 15자 입력해 주세요.",
	               "비밀번호와 비밀번호 확인란은 값이 일치해야 합니다.",
	               "- 포함 입력해 주세요. 예시) 010-0000-0000"];
	
	$('.error').each(function(index){
		$('.error').eq(index).html(message[index]);
	});
	
	$('#c_id, #c_passwd, #userPwCheck, #c_phone').bind("focus",function(){
		var idx = $("#c_id, #c_passwd, #userPwCheck, #c_phone").index(this);
		//console.log("대상 : "+ idx );
		$(this).parents(".form-group").find(".error").html(message[idx]);
	});
	
	$("#idConfirmBtn").click(function(){
		if (!formCheck($('#c_id'), $('.error:eq(0)'), "아이디를"))	return;
		else if (!inputVerify(0,'#c_id', '.error:eq(0)'))	return;
		else{
			$.ajax({
				url : "/customer/userIdConfirm",  
				type : "post",                
				data : "c_id="+$("#c_id").val(),
				error : function(){  
					alert('사이트 접속에 문제로 정상 작동하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
				},
				success : function(resultData){ 
					console.log("resultData : "+resultData);
					if(resultData=="1"){
						$("#c_id").parents(".form-group").find(".error").html("현재 사용 중인 아이디입니다.");
					}else if(resultData=="2"){
						$("#c_id").parents(".form-group").find(".error").html("사용 가능한 아이디입니다.");
						idConfirm = 2;
					} 
				}
			});
		}
	});
	
	
	$("#nickConfirmBtn").click(function(){
		if (!formCheck($('#c_nickname'), $('.error:eq(6)'), "별명을"))	return;
		//else if (!inputVerify(0,'#c_nickname', '.error:eq(6)'))	return;
		else{
			$.ajax({
				url : "/customer/userNickConfirm",  
				type : "post",                
				data : "c_nickname="+$("#c_nickname").val(),
				error : function(){  
					alert('사이트 접속에 문제로 정상 작동하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
				},
				success : function(resultData){ 
					console.log("resultData : "+resultData);
					if(resultData=="1"){
						$("#c_nickname").parents(".form-group").find(".error").html("현재 사용 중인 별명입니다.");
					}else if(resultData=="2"){
						$("#c_nickname").parents(".form-group").find(".error").html("사용 가능한 별명입니다.");
						idConfirm = 2;
					} 
				}
			});
		}
	});
	
	
	$("#emailConfirmBtn").click(function(){
		$("#c_email").val($("#emailName").val()+"@"+$("#emailDomain").val());
		if (!formCheck($('#emailName'), $('.error:eq(7)'), "이메일을"))	return;
		//else if (!inputVerify(0,'#c_nickname', '.error:eq(6)'))	return;
		else{
			$.ajax({
				url : "/customer/userEmailConfirm",  
				type : "post",                
				data : "c_email="+$("#c_email").val(),
				error : function(){  
					alert('사이트 접속에 문제로 정상 작동하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
				},
				success : function(resultData){ 
					console.log("resultData : "+resultData);
					if(resultData=="1"){
						$("#emailName").parents(".form-group").find(".error").html("현재 사용 중인 이메일입니다.");
					}else if(resultData=="2"){
						$("#emailName").parents(".form-group").find(".error").html("사용 가능한 이메일입니다.");
						idConfirm = 2;
					} 
				}
			});
		}
	});
	
	
	
	
	/* 확인 버튼 클릭 시 처리 이벤트 */
	$("#joinInsert").click(function(){
		//입력값 체크
		if (!formCheck($('#c_id'), $('.error:eq(0)'), "아이디를"))	return;
		else if (!inputVerify(0,'#c_id', '.error:eq(0)'))	return;
		else if (!formCheck($('#c_passwd'), $('.error:eq(1)'), "비밀번호를"))	return;
		else if (!inputVerify(1,'#c_passwd', '.error:eq(1)'))	return;
		else if (!idPwdCheck()) return;
		else if (!formCheck($('#userPwCheck'), $('.error:eq(2)'), "비밀번호 확인을"))	return;
		else if (!inputVerify(1,'#userPwCheck', '.error:eq(2)'))	return;
		else if (!passwordCheck()) return;
		else if (!formCheck($('#c_phone'), $('.error:eq(3)'), "전화번호를"))	return;
		else if (!inputVerify(2,'#c_phone', '.error:eq(3)'))	return;
		else if (!formCheck($('#c_name'), $('.error:eq(5)'), "이름을"))	return;
		else if (!formCheck($('#c_nickname'), $('.error:eq(6)'), "별명을"))	return;
		else if (!formCheck($('#emailName'), $('.error:eq(7)'), "이메일 주소를"))	return;
		else if (idConfirm!=2){ alert("아이디 중복 체크 진행해 주세요."); return;}		
		else {  
			$("#c_email").val($("#emailName").val()+"@"+$("#emailDomain").val());
			$("#c_pinno").val($("#birth").val()+"-"+$("#gender").val());
			$("#customerForm").attr({
				"method":"post",
				"action":"/customer/join"
			});
			$("#customerForm").submit();
		}
	});
	
	$("#joinCancel").click(function(){
		location.href="/customer/login";
	});
	
	$("#joinReset").click(function(){
		$("#customerForm").each(function(){
			this.reset();
		}); 
	});
});
