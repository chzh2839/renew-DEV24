<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>

		<div id="sidemenu_wrap">
           <div id="tit_mypage">
               <div id="mypage_img">
                   <span>${login.c_nickname}님, 반갑습니다.</span>
                   <img src="/resources/image/member.jpg" />
               </div>
           </div> <!--tit_mypage-->
           
           <div id="info_mypage">
               <div class="info">
                  <p class="tit">주문내역</p>
                   <ul>
                       <li><a href="/mypage/orderHistory">주문내역조회</a></li>
                       <li><a href="/mypage/refundHistory">환불신청/취소내역조회</a></li>
                       <li><a href="/cart/cartList">장바구니 바로가기</a></li>
                   </ul>
               </div>

               <div class="info">
                  <p class="tit">나의 정보</p>
                   <ul>
                       <li><a href="/mypage/modify">회원정보수정</a></li>
                       <li><a href="#">회원탈퇴</a></li>
                   </ul>
               </div>

                <div id="info2">
                   <a href="/mypage/qnaHistory"><span>나의 문의</span></a>
                   <!-- <a href="#"><span>나의 리뷰</span></a> -->
               </div>
               
           </div><!--info_mypage-->
       </div> <!--sidemenu_wrap-->