
/** 함수명: chkSubmit(유효성 체크 대상, 메시지 내용)
 * 출력영역: alert으로.
 * 예시 :  if(!chkData("#keyword","검색어를")) return;
 * common.js를 만들 때는 주석을 잘 달아 놓으면 다른 개발자가 사용시 편리하다.
 ********************************************************/
 function chkSubmit(item, msg) {
	if($(item).val().replace(/\s/g,"")=="") {
		alert(msg+" 입력해주세요.");
		$(item).val("");
		$(item).focus();
		return false; //값이 비어있을 경우 false를 반환
	} else {
		return true;
	}
}


/** 함수명: chkData(유효성 체크 대상, 메시지 내용)
 * 출력영역: alert으로.
 * 예시 : if(!chkData("#keyword","검색어를")) return;
 * // chkSubmit과의 차이는 함수내에서 객체화를 시킨다.
 * common.js를 만들 때는 주석을 잘 달아 놓으면 다른 개발자가 사용시 편리하다.
 ********************************************************/
function chkData(item, msg) {
	if($(item).val().replace(/\s/g,"")=="") {
		alert(msg+" 입력해주세요.");
		$(item).val("");
		$(item).focus();
		return false; //값이 비어있을 경우 false를 반환
	} else {
		return true;
	}
}

/***************************
 정규표현식 문자포맷 지정 및 체크
 chkTextFormat(체크대상, 메시지 내용, 포맷) */
function chkTextFormat(item, msg, format){
	if($(item).val().match(format)==null){
		alert(msg+" 형식에 맞지 않습니다.");
		$(item).val("");
		$(item).focus();
		return false;
	}else{
		return true;
	}
}


/** 함수명: chkForm(유효성 체크 대상, 메시지 내용)
 * 출력영역: placeholder 속성을 이용.
 * 예시 : if(!chkForm("#keyword","검색어를")) return;
 ********************************************************/
function checkForm(item,msg) {
	var message = "";
	if($(item).val().replace(/\s/g,"")=="") {
		message = msg + "입력해 주세요.";
		$(item).attr("placeholder",message);
		return false;
	} else {
	return true;
	}
}


/** 함수명: formCheck(유효성 체크 대상, 출력 영역, 메시지 내용)
 * 출력영역: 매개변수 두번째 출력 영역에,
 * 예시 : if(!formCheck($('#keyword'),$('#msg'),"검색어를")) return;
 ********************************************************/
function formCheck(main, item, msg){
	if(main.val().replace(/\s/g,"")==""){
		item.css("color","#000099").html(msg+"입력해 주세요");
		main.val("");
		return false;
	}else{
		return true;
	}
}


/************************
 * addComma () : 천단위 콤마 찍기
 * *******/
function addComma(value){
     value = value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
     return value;
}

/************************
 * unComma () : 천단위 콤마 없애기
 * *******/
function unComma(value){
	value = value.replace(/[^\d]+/g, '');
     return value;
 }


/** 함수명: chkFile(파일명) 
 * 설명: 이미지 파일 여부를 확인하기 위해 확장자 확인 함수. */ 
function chkFile(item){
	/*
		배열내의 값을 찾아서 인덱스를 반환(요소가 없을 경우-1반환)
		jQuery.inArray(찾을 값, 검색 대상의 배열)
	*/
	var ext = item.val().split('.').pop().toLowerCase();
	if(jQuery.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
		alert('gif, png, jpg, jpeg 파일만 업로드 할수 있습니다.');
		return false;
	}else {
		return true;
	}
}

/* 배열: 유효성 체크 시 필요한 정규식으로 배열을 초기화.
 * pattern = [ 아이디 , 비밀번호, 핸드폰번호]
 * 함수명: inputVerify(배열 인덱스번호, 비교할 값, 출력영역) 
 * */ 
var pattern = [
     "((?=.*[a-zA-Z])(?=.*[0-9]).{6,10})",
     "((?=.*[a-zA-Z])(?=.*[0-9@#$%]).{8,12})",
     "^\\d{3}-\\d{3,4}-\\d{4}", "^[가-힣]*$"];
function inputVerify(index, data, printarea){
	var data_regExp = new RegExp(pattern[index]);
	var match = data_regExp.exec($(data).val());
	if(match==null){
		$(printarea).html("입력값이 형식에 맞지 않습니다. 다시 입력해 주세요.");
		$(data).val("");
		return false;
	} else {
		return true;
	}
}

function isNumber(input) {
	if($(input).val()!=""){
		var num_regExp = new RegExp("[0-9]$","i");	//only number
		var match = num_regExp.exec($(input).val());
		//alert(match);
		if(match==null){
			alert("숫자를 입력해주세요");
			$(input).val($(input).val().substr(0, $(input).val().length-1));
		}
	}
}

/* 함수명: getDateFormat(날자 데이터) 
 * 설명 : dataValue의 값을 년-월-일 형식(예시: 2018-01-01)으로 반환.*/ 
function getDateFormat(dateValue){
	var year = dateValue.getFullYear();
	
	var month = dateValue.getMonth()+1;
	month = (month<10) ? "0"+month : month;
	
	var day = dateValue.getDate();
	day = (day<10) ? "0"+day : day;
	
	var result = year+"-"+month+"-"+day;
	return result;
}

/* Form 요소를 JSON으로 변환하는 함수. */
$.fn.serializeObject = function() {
		var obj = null;
		
		try {
			if ( this[0].tagName && this[0].tagName.toUpperCase() == "FORM" ) {
				var arr = this.serializeArray();
				if( arr ) {
					obj = {};
					$.each(arr, function() {
						obj[this.name] = this.value;
					});				
				}
		  	}
		} catch(e) {alert(e.message);
		} finally  {}
		return obj;
};