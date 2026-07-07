<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page trimDirectiveWhitespaces="true" %>
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
		
		<link rel="stylesheet" href="/resources/include/css/style_cart.css">
		<link rel="stylesheet" href="/resources/include/css/style_refundForm.css">
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<script>
			$(function(){
				var failMsg = "${failMsg}";
				if(failMsg!=""){
					alert(failMsg);
					failMsg = "";
				}
				
				/* 취소 버튼 처리 */
				$("#refundCancelBtn").click(function(){
					location.href="/mypage/orderHistory";
				});
				
				/* 환불신청 버튼 처리 */
				$("#refundInsertBtn").click(function(){
					// 유효성체크
					if(!chkData("#bank_name", "은행명을")) return;
					if(!chkData("#accountname", "예금주명을")) return;
					if(!chkData("#accountnumber", "계좌번호를")) return;
           			if(!chkData("#rf_reason_area", "환불사유를")) return;
           			
           			// 전달 값 form에 저장
           			var rf_price = $(".lastrow td span").text();
           			rf_price = unComma(rf_price);
           			var b_num = $(".t_refundItem tbody tr").attr("data-num");
           			var p_num = "${ohvo.p_num}";
           			var pd_num = $(".t_refundItem tbody tr").attr("data-pd");
           			var rf_reason = $("#rf_reason_area").val();
           			var rf_qty = $("td.td_qty").text();
           			
           			console.log(rf_price);
           			console.log(b_num);
           			console.log(pd_num);
           			console.log(rf_reason);
           			console.log("rf_qty : "+rf_qty);
           			
           			$("#rf_price").val(rf_price);
           			$("#rf_reason").val(rf_reason);
           			$("#b_num").val(b_num);
           			$("#p_num").val(p_num);
           			$("#pd_num").val(pd_num);
           			$("#rf_qty").val(rf_qty);

           			$("#f_refund").attr({
        				"action" : "/refund/refundInsert",
        				"method" : "post"
        			});
        			$("#f_refund").submit();
           			
				});
				
			});// 최상위 종료
		</script>

	</head>
	<body>
		<div id="content_wrap">
	      <div id="refund_wrap">
	          <div class="tit_refund"><h3>환불신청</h3></div>
	          
	         <form name="f_refund" id="f_refund">
	         	<input type="hidden" name="rf_price" id="rf_price" />
	         	<input type="hidden" name="p_num" id="p_num" />
	         	<input type="hidden" name="pd_num" id="pd_num" />
	         	<input type="hidden" name="b_num" id="b_num" />
	         	<input type="hidden" name="rf_reason" id="rf_reason" />
	         	<input type="hidden" name="rf_qty" id="rf_qty" />
	         	<input type="hidden" name="pd_orderstate" id="pd_orderstate" value="reRequest" />
 			</form>
 			
	          <!--구매내역 정보 가져오기-->
	          <table class="table t_refundForm" border="1">
	              <tr>
	                  <th>주문자</th>
	                  <td>${pvo.p_sender}</td>
	                  <th>주문자 연락처</th>
	                  <td>${pvo.p_senderphone}</td>
	              </tr>
	              <tr>
	                  <th>수령자</th>
	                  <td>${pvo.p_receiver}</td>
	                  <th>수령자 연락처</th>
	                  <td>${pvo.p_receivephone}</td>
	              </tr>
	              <tr>
	                  <th>배송주소</th>
	                  <td colspan="3">우편번호 : ${pvo.p_zipcode}<br/>${pvo.p_address}</td>
	              </tr>
	              <tr>
	                  <th colspan="4" class="td_refundAcccount">환불받을통장</th>
	              </tr>
	              <tr>
	                  <th>은행명</th>
	                  <td>
	                      <select id="bank_name" name="bank_name" class="form-control">
	                          <option value="">은행을 선택하세요</option>
	                          <option value="kb">국민은행</option>
	                          <option value="sh">신한은행</option>
	                          <option value="kakao">카카오뱅크</option>
	                          <option value="keb">하나은행</option>
	                          <option value="bnk">부산은행</option>
	                      </select>
	                  </td>
	                  <th>예금주</th>
	                  <td>
	                      <input type="text" id="accountname" class="form-control" />
	                  </td>
	              </tr>
	              <tr>
	                  <th>계좌번호</th>
	                  <td colspan="3"><input type="text" id="accountnumber" class="form-control" /></td>
	              </tr>
	              <tr>
	                  <th>환불사유</th>
	                  <td colspan="3">
	                      <textarea rows="10" cols="100" class="form-control" id="rf_reason_area"></textarea>
	                  </td>
	              </tr>
	              
	          </table>
	          
	         
	          
	          
	          <!--환불신청할 도서 선택-->
	          <table class="table t_refundItem" border="1">
	               <colgroup>
	                   <col width="50%" /> <!--상품명-->
	                   <col width="10%" /> <!--단가-->
	                   <col width="10%" /> <!--수량-->
	                   <col width="15%" /> <!--가격-->
	               </colgroup>
	                <thead>
	                    <tr>
	                        <th>상품명</th>
	                        <th>단가</th>
	                        <th>수량</th>
	                        <th>가격</th>
	                    </tr>
	                </thead>
	                <tbody>
	                    <tr data-num="${ohvo.b_num}" data-pd="${ohvo.pd_num}">
	                        <td class="td_book">
	                            <span class="td_bookimg"><img src="${ohvo.listcover_imgurl}"/></span>
	                            <span class="td_bookname">${ohvo.b_name}</span>
	                        </td>
	                        <td><span><fmt:formatNumber value="${ohvo.singlePrice}" pattern="#,###" /></span>원</td>
	                        <td class="td_qty">${ohvo.pd_qty}</td>
	                        <td><span><fmt:formatNumber value="${ohvo.pd_price}" pattern="#,###" /></span>원</td>
	                    </tr>
	                    
	                    <tr class="lastrow">
	                        <td colspan="4">환불신청 총 금액 : <span><fmt:formatNumber value="${ohvo.pd_price}" pattern="#,###" /></span>원</td>
	                    </tr>
	                </tbody>
	            </table>
	          
	            <p class="info">주문자 조회를 위해 빈칸을 모두 작성해 주셔야 하며, 신원 확인 안 되는 상품은 처리가 안 된입니다. 고객 변심으로 인한 환불은 왕복 택배비 고객님 부담이며, 재포장하여 상품 판매 가능한 경우에만 가능합니다. 상품 수령 후 3일 이내에만 환불 신청이 가능합니다.<br/>
	            포장 파손, 오염, 향수, 택 제거 등 상품 사용 흔적이 있거나 이벤트, 특가 할인, 세일 상품의 경우는 환불승인이 어려울 수 있습니다.</p>
	            
	            <div id="btnArea">
	               <input type="button" class="btn btn-success" id="refundInsertBtn" value="환불신청" />
	               <input type="button" class="btn btn-default" id="refundCancelBtn" value="취소" />
	           </div><!--btnArea-->
	      </div> <!--refund_wrap-->
	    </div> <!-- content_wrap -->
	</body>
</html>