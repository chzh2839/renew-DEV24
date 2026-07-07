<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
	<h1 class="page-header">오늘의 DEV24 현황</h1>
	<h3 class="date"></h3>
	<br />

	<%--도서 관련 dashboard 윗부분 --%>
	<div class="row placeholders">
		<div class="col-xs-6 col-sm-3 placeholder">
			<c:set var="admin" value="${adminIndex}" />
			<%-- select sum(stk_qty) from stock; --%>
			<img
				data-src="holder.js/200x200?theme=blue&text=${adminIndex.stk_cnt}권"
				class="img-responsive" alt="Generic placeholder thumbnail">
			<h4>재고현황</h4>
			<span class="text-muted">총 보유 도서량</span>
		</div>

		<div class="col-xs-6 col-sm-3 placeholder">
			<%-- 
            select count(p_num) from purchase 
         where p_buydate >= (select to_date(sysdate, 'YY/MM/DD') from dual);
         
         select *from purchase
         where trunc(p_buydate) = TO_DATE(sysdate,'yy/mm/dd');
         
         select count(p_num) from purchase
         where trunc(p_buydate) = TO_DATE('20/10/28','yy/mm/dd');
            --%>
			<%-- <c:set var="purchase" scope="session" value="2154"/> --%>


			<img data-src="holder.js/200x200?theme=green&text=${admin.p_cnt}건"
				class="img-responsive" alt="Generic placeholder thumbnail">
			<h4>구매현황</h4>
			<span class="text-muted">오늘 구매 현황</span>
		</div>
		<div class="col-xs-6 col-sm-3 placeholder">
			<img data-src="holder.js/200x200?theme=red&text= ${admin.rf_cnt}건"
				class="img-responsive">
			<h4>환불신청수</h4>
			<span class="text-muted">오늘 환불신청</span>
		</div>

		<div class="col-xs-6 col-sm-3 placeholder">


			<%--
                  ----- 테이블 inner join 으로 날짜별 구매확정한 가격 가져오기----- 
                  select pdetail.pd_price, purchase.p_buydate
               from purchase 
               inner join pdetail 
               on pdetail.c_num=purchase.c_num and purchase.p_num=pdetail.p_num
               where trunc(p_buydate) = to_date('20/10/28', 'yy/mm/dd') and pdetail.pd_orderstate='confirm';
                  
                  
                  ---- 설정 날짜의 매출의 합을  가져오기 -----
                  select sum(pd_price) from (select pdetail.pd_price, purchase.p_buydate
               from purchase 
               inner join pdetail 
               on pdetail.c_num=purchase.c_num and purchase.p_num=pdetail.p_num 
               where trunc(p_buydate) = to_date('20/10/28', 'yy/mm/dd') and pdetail.pd_orderstate='confirm');
               
               --%>


			<%-- 
              holder.js 의 값중 매출 가격 정리 테스트 용도로 사용한 코드.. 
            <c:set var="testmoney" value="5416876000"/>
            <img data-src="holder.js/300x200?theme=blue&text=${testmoney/10000} 만원" class="img-responsive">
            --%>

			<c:set var="testmoney" value="541687612" />

			<img
				data-src="holder.js/200x200?theme=blue&text=${adminIndex.sal_cnt/10000}만원"
				class="img-responsive">
			<h4>매출</h4>
			<span class="text-muted">오늘 매출금액</span>
		</div>

		<%-- 게시판 관련 dashboard 밑부분 --%>
		<div class="col-xs-6 col-sm-3 placeholder">

			<%--
               select count(re_num) from review where trunc(re_writedate) = to_date(sysdate, 'yy/mm/dd');
            select count(re_num) from review where trunc(re_writedate) = to_date('20/10/29', 'yy/mm/dd');
             --%>
			<img
				data-src="holder.js/200x200?theme=blue&text=${adminIndex.rev_cnt}개"
				class="img-responsive" alt="Generic placeholder thumbnail">
			<h4>리뷰 현황</h4>
			<span class="text-muted">리뷰글 현황</span>
		</div>


		<div class="col-xs-6 col-sm-3 placeholder">
			<%-- select count(q_num) from qna where trunc(q_writedate) = TO_DATE(sysdate,'yy/mm/dd'); --%>
			<img
				data-src="holder.js/200x200?theme=green&text=${adminIndex.qna_cnt}개"
				class="img-responsive" alt="Generic placeholder thumbnail">
			<h4>QnA 현황</h4>
			<span class="text-muted">오늘 QnA 신청 현황</span>
		</div>

		<div class="col-xs-6 col-sm-3 placeholder">
			<%--select count(ne_num) from neboard; --%>
			<img
				data-src="holder.js/200x200?theme=red&text=${adminIndex.ne_cnt}개"
				class="img-responsive">
			<h4>공지사항 이벤트 현황</h4>
			<span class="text-muted">공지사항 및 이벤트 글 수</span>
		</div>

		<div class="col-xs-6 col-sm-3 placeholder">
			<%-- select count(fb_num) from freeboard; --%>
			<img
				data-src="holder.js/200x200?theme=blue&text=${adminIndex.fb_cnt}개"
				class="img-responsive" alt="Generic placeholder thumbnail">
			<h4>자유게시판 현황</h4>
			<span class="text-muted">총 게시글 수</span>
		</div>

		<br>

		<%--   <img data-src="holder.js/300x200?text=Add \n line breaks \n anywhere.">  --%>


		<%-- <div class="circle">
               야이 새끼야!!!!!
               야임마~!!!!!
            </div> --%>

		<%--  <img data-src="holder.js/200x200?theme=blue&text=야마!" class="img-responsive" alt="Generic placeholder thumbnail">
            <img data-src="holder.js/200x200?theme=green&text=야마!" class="img-responsive" alt="Generic placeholder thumbnail">
            <img data-src="holder.js/200x200?theme=red&text=야마!" class="img-responsive" alt="Generic placeholder thumbnail">  --%>

		<%---asdfasdfasdfsadfasdfasdfsadfasdf --%>

	</div>

	<%-- 매출 현황 테이블의 제목 --%>

	<%-- 
         ======================당일 매출 순번 테이블 sql 문 ==============================
         
         create view purchaseinfo as
         select purchase.p_num, purchase.p_sender, purchase.c_num, purchase.p_buydate, pdetail.b_num from 
         purchase 
         inner join pdetail 
         on pdetail.c_num=purchase.c_num and purchase.p_num=pdetail.p_num;

         select*from purchaseinfo;
         먼저 테이블의 뷰를 생성해준다.. 
         
         select b_num, count(p_num)
         from purchaseinfo 
         group by b_num
         having count(p_num) >= (select max(count(b_num)) from purchaseinfo group by b_num); 
         
         /*현재 날짜와 연동이 안됨... 날짜까지 설정을 해주는 구문을 만들어야함...*/
         
         =========================================================================
      --%>
	<h2 class="sub-header">오늘 최대 매출 도서</h2>
	<p class="date"></p>
	<div class="table-responsive">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>도서명</th>
					<th>작가</th>
					<th>출판사</th>
					<th>판매가격</th>
					<th>분류</th>
				</tr>
			</thead>
			<tbody>
				<%-- jstl 테스팅을 위한 변수 선언 --%>
				<c:set var="title" value="강자바의 자바 조지기" />
				<c:set var="author" value="강자바" />
				<c:set var="publisher" value="강자바 컴퍼니" />
				<c:set var="price" value="25000" />
				<c:set var="category" value="프로그래밍 언어" />
				<%-- <c:forEach begin="0" end="10" varStatus="loop"> </c:forEach> --%>
				<tr>
					<td>${title}</td>
					<td>${author}</td>
					<td>${publisher}</td>
					<td>${price}</td>
					<td>${category}</td>
				</tr>

				<tr>
					<td>김서버의 서버 구축의 정석</td>
					<td>김서버</td>
					<td>김서버 컴퍼니</td>
					<td>25000</td>
					<td>네트워크/해킹/보안</td>
				</tr>

				<tr>
					<td>최디비의 디비하고 디비자기</td>
					<td>최디비</td>
					<td>최디비 컴퍼니</td>
					<td>25000</td>
					<td>OS/데이터베이스</td>
				</tr>

				<tr>
					<td>양풀스택의 풀스택 웹개발자되기</td>
					<td>양풀스택</td>
					<td>양풀스택 컴퍼니</td>
					<td>50000</td>
					<td>웹사이트</td>
				</tr>

				<tr>
					<td>고편집의 영상 편집 입문</td>
					<td>고편집</td>
					<td>고편집 코퍼레이션</td>
					<td>35000</td>
					<td>컴퓨터 입문/활용</td>
				</tr>


			</tbody>
		</table>
	</div>
</div>