<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
		
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<script>
			$(function(){
				/* 도서명 클릭 시 도서상세페이지 이동 */
				$(".td_title").click(function(){
					var b_num = $(this).parents("tr").attr("data-num");
					location.href="/book/detail/"+b_num;
				});
			});
		</script>

	</head>
	<body>
		<div id="content_mypage">
           <div class="contentArea">
                <div class="tit_mypage">
                    <h3>최근 주문내역</h3>
                    <a href="/mypage/orderHistory">&gt;&nbsp; 더보기</a>
                </div>

                <table class="table" border="1">
                    <thead>
                        <tr>
                            <th>주문일자</th>
                            <th>주문번호</th>
                            <th>주문내역</th>
                            <th>주문상태</th>
                        </tr>
                    </thead>
                    <tbody>
                    	<c:choose>
                    		<c:when test="${not empty ohvo}">
                    			<c:forEach var="ohvo" items="${ohvo}" begin="0" end="3" step="1">
                    				<tr data-num="${ohvo.b_num}">
			                            <td>${ohvo.p_buydate}</td>
			                            <td class="td_num">${ohvo.p_num}</td>
			                            <td class="td_title">${ohvo.b_name}</td>
			                            <td>
			                            	<c:choose>
				                           	<c:when test="${ohvo.pd_orderstate == 'preShipping'}">
				                           		배송예정
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'shipping'}">
				                           		배송중
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'pConfirm'}">
				                           		구매확정
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'reRequest'}">
				                           		환불승인대기
				                           	</c:when>
				                           	<c:when test="${ohvo.pd_orderstate == 'cancel'}">
				                           		주문취소
				                           	</c:when>
				                           	<c:otherwise>
				                           		승인완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="4">최근 주문내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                        
                    </tbody>
                </table>
            </div> <!-- contentArea -->
            
            <div class="contentArea">
                <div class="tit_mypage">
                    <h3>환불신청내역</h3>
                    <a href="/mypage/refundHistory">&gt;&nbsp;더보기</a>
                </div>

                <table class="table" border="1">
                    <thead>
                        <tr>
                            <th>주문일자</th>
                            <th>환불번호</th>
                            <th>주문내역</th>
                            <th>주문상태</th>
                            <th>환불신청/승인일</th>
                        </tr>
                    </thead>
                    <tbody>
                    	<c:choose>
                    		<c:when test="${not empty rfhvo}">
                    			<c:forEach var="rfhvo" items="${rfhvo}" begin="0" end="3" step="1">
                    				<tr data-num="${rfhvo.b_num}">
			                            <td>${rfhvo.p_buydate}</td>
			                            <td class="td_num">${rfhvo.rf_num}</td>
			                            <td class="td_title">${rfhvo.b_name}</td>
			                            <td>
			                            	<c:choose>
				                           	<c:when test="${rfhvo.rf_orderstate == 'reRequest'}">
				                           		환불승인대기
				                           	</c:when>
				                           	<c:when test="${rfhvo.rf_orderstate == 'cancel'}">
				                           		주문취소
				                           	</c:when>
				                           	<c:otherwise>
				                           		승인완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                            <td>${rfhvo.rf_confirmdate}</td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="5">최근 환불내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                    </tbody>
                </table>
            </div> <!-- contentArea -->
            
            <div class="contentArea">
                <div class="tit_mypage">
                    <h3>나의 문의</h3>
                    <a href="/mypage/qnaHistory">&gt;&nbsp;더보기</a>
                </div>

                <table class="table" border="1">
                	<colgroup>
	                   <col width="20%" />
	                   <col width="20%" />
	                   <col width="50%" />
	                   <col width="10%" /> 
	               </colgroup>
                    <thead>
                        <tr>
                            <th>카테고리</th>
                            <th>문의일자</th>
                            <th>문의내역</th>
                            <th>답변상태</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                    		<c:when test="${not empty qvo}">
                    			<c:forEach var="qvo" items="${qvo}">
                    				<tr data-num="${qvo.q_num}">
                    					<td>${qvo.q_category}</td>
			                            <td>${qvo.q_writedate}</td>
			                            <td class="td_title">${qvo.q_title}</td>
			                            <td class="td_state">
			                            	<c:choose>
				                           	<c:when test="${qvo.q_repRoot == 0}">
				                           		답변대기
				                           	</c:when>
				                           	<c:otherwise>
				                           		답변완료
				                           	</c:otherwise>
				                           	</c:choose>
			                            </td>
			                        </tr>
                    			</c:forEach>
                    		</c:when>
                    		<c:otherwise>
                    			<tr>
                    				<td colspan="4">최근 문의내역이 없습니다.</td>
                    			</tr>
                    		</c:otherwise>
                    	</c:choose>
                    </tbody>
                </table>
            </div> <!-- contentArea -->
            
        </div><!--content_mypage-->
	</body>
</html>