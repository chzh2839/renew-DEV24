<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		<link rel="stylesheet" href="/resources/include/css/style_purchase.css">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
    	<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    	<style>
    		#p_senderphone{display: inline;}
    	</style>
    	<script type="text/javascript">
    		$(function(){
    			getSenderInfo(2); // 임의의 회원번호 입력
    			
    			
    			/* 주문자와동일 체크했을 때 */
    			$("#sameAsSender").click(function(){
    				if($("#sameAsSender").is(":checked")){
    					var sender = $("#p_sender").val();
    					var sphone1 = $("#p_senderphone1").val();
    					var sphone2 = $("#p_senderphone2").val();
    					var sphone3 = $("#p_senderphone3").val();
    					$("#p_receiver").val(sender);
    					$("#p_receivephone1").val(sphone1);
    					$("#p_receivephone2").val(sphone2);
    					$("#p_receivephone3").val(sphone3);
    				}
    			});
    			
    			
    			/* 우편번호 찾기 버튼 */
    			$("#zipcodeSearchBtn").click(function(){
    				getPostCode();
    			});
    			
    			
    		}); // 최상위 종료
    		
    		/* 배송정보 주문자 정보 보여주는 함수 */
    		function getSenderInfo(c_num){
				var url = "/purchase/"+c_num; //+".json"
				
				// getJSON(요청url, 파라미터값, success fn, fail fn)
				$.getJSON(url, function(data){ // success
					$("#p_sender").val(data.c_name);
					$("#c_email").val(data.c_email);
					
					var phoneNumber = data.c_phone.split("-");
					$("#p_senderphone1").val(phoneNumber[0]);
					$("#p_senderphone2").val(phoneNumber[1]);
					$("#p_senderphone3").val(phoneNumber[2]);
				
				}).fail(function(){ // error
					alert("주문자 정보를 불러오지 못했습니다. 다시 로그인해주세요.");
				});
			}
			
			/* 우편번호 찾기 버튼 시 실행하는 함수 */
			function getPostCode(){
				new daum.Postcode({
			        oncomplete: function(data) {
			            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분입니다.
						
			        	// 도로명 주소의 노출 규칙에 따라 주소를 조합한다.
		                // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
		                var fullRoadAddr = data.roadAddress; // 도로명 주소 변수
		                var extraRoadAddr = ''; // 도로명 조합형 주소 변수
		 
		                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
		                // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
		                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
		                    extraRoadAddr += data.bname;
		                }
		                // 건물명이 있고, 공동주택일 경우 추가한다.
		                if(data.buildingName !== '' && data.apartment === 'Y'){
		                   extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
		                }
		                // 도로명, 지번 조합형 주소가 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
		                if(extraRoadAddr !== ''){
		                    extraRoadAddr = ' (' + extraRoadAddr + ')';
		                }
		                // 도로명, 지번 주소의 유무에 따라 해당 조합형 주소를 추가한다.
		                if(fullRoadAddr !== ''){
		                    fullRoadAddr += extraRoadAddr;
		                }
		 
		                // 우편번호와 주소 정보를 해당 필드에 넣는다.
		                //console.log(data.zonecode);
		                //console.log(fullRoadAddr);
		                
		                $("input[name='p_zipcode']").val(data.zonecode);
		                $("input[name='p_address']").val(fullRoadAddr);
		                
			        }
			    }).open();
			}
			
    	
    	</script>

	</head>
	<body>
				<div class="tit_cart">
	                <h3>배송주소</h3>
	            </div>
	            
	      			<form id="f_purchase" name="f_purchase">
	      			      
		            <div id="delivery_wrap">
		            	<input type="hidden" name="p_receivephone" id="p_receivephone" />
		            	<input type="hidden" name="p_senderphone" id="p_senderphone" />
		            	<input type="hidden" name="p_price" id="p_price" />
		            
		                <table class="table t_receiver">
		                    <tr>
		                        <th>받는분</th>
		                        <td>
		                            <input type="text" name="p_receiver" id="p_receiver" maxlength="10" class="form-control" />
		                            <label><input type="checkbox" name="sameAsSender" id="sameAsSender" />주문자와 동일</label>
		                        </td>
		                    </tr>
		                    <tr class="tr_address">
		                        <th>배송주소</th>
		                        <td><input type="text" name="p_zipcode" id="p_zipcode" class="form-control" readonly="readonly" />
		                            <input type="button" id="zipcodeSearchBtn" class="btn btn-default" value="우편번호 찾기" /></td>
		                    </tr>
		                    <tr>
		                        <th></th>
		                        <td class="td_detailAddress">상세주소 : <input type="text" name="p_address" id="p_address" class="form-control" /></td>
		                    </tr>
		                    <tr>
		                        <th>핸드폰번호</th>
		                        <td class="td_phone">
		                            <input type="text" name="p_receivephone1" id="p_receivephone1" maxlength="3" class="form-control" /> - 
		                            <input type="text" name="p_receivephone2" id="p_receivephone2" maxlength="4" class="form-control" /> - 
		                            <input type="text" name="p_receivephone3" id="p_receivephone3" maxlength="4" class="form-control" />
		                        </td>
		                    </tr>
		                </table> <!--받는사람-->
		                
		                <table class="table t_sender">
		                    <tr>
		                        <th colspan="2" class="sender_tit">주문고객</th>
		                    </tr>
		                    <tr>
		                        <th>이름</th>
		                        <td><input type="text" name="p_sender" id="p_sender" maxlength="10" class="form-control" /></td>
		                    </tr>
		                    <tr>
		                        <th>핸드폰번호</th>
		                        <td class="td_phone">
		                            <input type="text" name="p_senderphone1" id="p_senderphone1" class="form-control" maxlength="3" /> - 
		                            <input type="text" name="p_senderphone2" id="p_senderphone2" class="form-control" maxlength="4" /> - 
		                            <input type="text" name="p_senderphone3" id="p_senderphone3" class="form-control" maxlength="4" />
		                        </td>
		                    </tr>
		                    <tr>
		                        <th>이메일</th>
		                        <td>
		                            <input type="text" name="c_email" id="c_email" class="form-control" />
		                        </td>
		                    </tr>
		                </table><!--보내는사람-->

	            	</div><!--delivery_wrap-->
	            
	            	<!--*** 결제방법 영역 ***-->
		           <div class="tit_cart">
		                <h3>결제방법</h3>
		            </div>
		            
		            <div id="payment_wrap">
		                <ul>
		                    <li>
		                        <label><input type="radio" name="p_pmethod" id="p_pmethod_card" value="신용카드" />신용카드</label>
		                    </li>
		                    <li>
		                        <label><input type="radio" name="p_pmethod" id="p_pmethod_toss" value="계좌이체" />계좌이체</label>
		                    </li>
		                    <li>
		                        <label><input type="radio" name="p_pmethod" id="p_pmethod_cash" value="무통장입금" />무통장입금</label>
		                    </li>
		                    <li>
		                        <label><input type="radio" name="p_pmethod" id="p_pmethod_phone" value="핸드폰결제" />핸드폰결제</label>
		                    </li>
		                </ul>
		            </div><!--payment_wrap-->
		            
		         </form>
		            
		          
		            
	</body>
</html>