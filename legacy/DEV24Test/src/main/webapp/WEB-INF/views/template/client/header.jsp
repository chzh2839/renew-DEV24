<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
<style>
	#afterLogin{
		clear:both;
		margin-top:10px;
	}
</style>
	<header>
        <div id="header_wrap">
            <div id="logo"><a href="/"><img src="/resources/image/logo.png" alt="로고"></a></div>
			 
           <nav>
		      <ul id="gnb">
                <li class="dropBox" id="book">
                    <span>일반도서</span>
                    <ul class="dropmenu">
                      <li><a class="bookLink" id="" href="/book/10">일반도서 전체</a></li>
                      <li class=""><a href="/book/11">프로그래밍 언어</a></li>
                      <li><a href="/book/12">OS/데이터베이스</a></li>
                      <li><a href="/book/13">웹사이트</a></li>
                      <li><a href="/book/14">컴퓨터 입문/활용</a></li>
                      <li><a href="/book/15">네트워크/해킹/보안</a></li>
                    </ul> <!-- dropmenu for book -->
                </li>
		        <li class="dropBox" id="ebook">
		            <span>eBook</span>
		          <ul class="dropmenu">
		          	  <li><a class="bookLink" href="/book/20">eBook 전체</a></li>
                      <li><a href="/book/26">IT전문서</a></li>
                      <li><a href="/book/27">컴퓨터 수험서</a></li>
                      <li><a href="/book/28">웹/컴퓨터 입문&활용</a></li>
                </ul> <!-- dropmenu for ebook -->      
		      </li>
		        <li>
		        	<span>커뮤니티</span>
		        	<ul class="dropmenu">
		        		<li><a href="/freeboard/freeboardList">자유게시판</a></li>
		        		<li><a href="/ne/neList">공지사항/이벤트</a></li>
		        	</ul>
		        </li> <!-- 자유게시판, 공지사항/이벤트 -->
		        <li><a href="/faq/faqMain">고객지원</a></li> <!-- 주문내역/배송조회, QnA, FAQ -->
		      </ul> <!-- gnb -->
		      
		      <ul id="util">
				<c:if test="${empty login.c_id}">
				    <li id="toLogin"><a href="/customer/login"><i class="far fa-user"></i>로그인</a></li>
				    <li id="toJoin"><a href="/customer/join"><i class="fas fa-user-tie"></i>회원가입</a></li>
			    </c:if>
			    <c:if test="${not empty login.c_id}">
				    <li id="toJoin"><a href="/customer/logout"><i class="fas fa-user-tie"></i>로그아웃</a></li>
				    <li id="toMypage"><a href="/mypage/mypage"><i class="fas fa-user-tie"></i>마이페이지</a></li>
				    <li><a href="/cart/cartList"><i class="fas fa-shopping-cart"></i>장바구니</a></li>
				    
				    <li id="afterLogin"><strong>[ ${login.c_nickname}] 님 반갑습니다.</strong></li>
			    </c:if>
			    
			    
			 </ul> <!-- util -->
			   
		    </nav> <!-- nav -->
       
           <div id="h_search">
               <div id="h_search_select">
                   <select name="h_searchmenu" id="h_searchmenu">
                       <option value="">전체검색</option>
                       <option value="">검색조건1</option>
                       <option value="">검색조건2</option>
                       <option value="">검색조건3</option>
                   </select>
               </div>
               
               <div id="h_search_input"><input type="text" name="" id="h_searchtext" placeholder="검색어를 입력하세요." /></div>
               
               <button type="button" id="h_searchBtn"><i class="fas fa-search"></i></button>
               
           </div><!-- search -->
        </div><!-- header_wrap -->
    </header>