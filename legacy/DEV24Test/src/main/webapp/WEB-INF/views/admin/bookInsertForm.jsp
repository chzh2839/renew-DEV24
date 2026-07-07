<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>

<html lang="ko">
	<head>
		<meta charset="UTF-8" />
		<!-- html4 : 파일의 인코딩 방식 지정 -->
		<!--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />-->
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		<link rel="shortcut icon" href="../image/icon.png" />
		<link rel="apple-touch-icon" href="../image/icon.png" />
		
	    <!-- Bootstrap core CSS -->
	    <link href="/resources/include/dist/css/bootstrap.min.css" rel="stylesheet">
	    <link href="/resources/include/dist/css/bootstrap-theme.min.css" rel="stylesheet">
    	<link rel="stylesheet" href="/resources/include/css/adminPage.css">
    	<!-- font -->
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/resources/include/js/common.js"></script>
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		<style>
			textarea{
				resize: none;
			}
			.text-right {
				text-align: right;
			}
		</style>
		<script>
			$(function(){
				/* 저장 버튼 클릭시 처리 이벤트 */
				$("#bookInsert").click(function(){
					//입력값 체크
					if (!chkSubmit("#b_name", "도서명을")) return;
					if (!chkSubmit("#b_author", "저자를")) return;
					if (!chkSubmit("#b_pub", "출판사를")) return;
					if (!chkSubmit("#b_date", "출간날짜를")) return;
					if (!chkSubmit("#b_price", "가격을")) return;
					if (!chkSubmit("#cateOne_num", "대분류를")) return;
					if (!chkSubmit("#cateTwo_num", "소분류를")) return;
					
					//개행, 들여쓰기, 공백 치환
					var b_info = $("#b_info").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
											
					var b_authorinfo = $("#b_authorinfo").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
					
					var b_list = $("#b_list").val()
											.replace(/ /g, '&nbsp;')
											.replace(/\t/g, '&nbsp;&nbsp;&nbsp;')
											.replace(/(\n|\r\n)/g, '<br/>');
					
					$("#b_info").val(b_info);
					$("#b_authorinfo").val(b_authorinfo);
					$("#b_list").val(b_list);
					
					console.log($("#b_list").val());
					
					var requestParam = new XMLHttpRequest();
					
					//도서 입력값 bookInsert로 전송
					$("#f_bookInsert").attr({
						"method" : "post",
						"encType" : "multipart/form-data",
						"action" : "/admin/book/bookInsert"
					});
					$("#f_bookInsert").submit();
				});
					
					$("#bookInsertResetBtn").click(function(){
						$("#f_bookInsert").each(function(){
							this.reset();
						});
					});
					
					$("#bookListBtn").click(function(){
						location.href="admin/book/0/0"
					});
			});
		</script>
	</head>
	<body>
		<div id="content_wrap">
			<form action="" class="form-group" id="f_bookInsert">
				<div class="container-fluid">
					<h2 id="tit">도서 등록</h2>
					<table class="table table-condensed">
						<%-- <colgroup>
							<col width="20%" />
							<col width="80%" />
						</colgroup> --%>
						<tr>
							<td></td>
							<td>
								<select name="cateOne_num" id="cateOne_num">
									<option >대분류</option>
									<option value="1" selected>도서</option>
									<option value="2">ebook</option>
								</select>
							</td>
							<td>
								<select name="cateTwo_num" id="cateTwo_num">
									<option >소분류</option>
									<option value="1" selected>프로그래밍 언어</option>
									<option value="2">OS/데이터베이스</option>
									<option value="3">웹</option>
									<option value="4">컴퓨터 입문</option>
									<option value="5">네트워크/해킹/보안</option>
									<option value="6">IT</option>
									<option value="7">컴퓨터 시험</option>
									<option value="8">웹/컴퓨터 기초</option>
								</select>
							</td>
							<td>
								<label for="b_date">출간날짜</label>
								<input type="date" name="b_date" id="b_date" value='2020-11-11'/>
							</td>
							<td>
								<label for="b_price">가격</label>
								<input type="number" name="b_price" id="b_price" value="25200"/>
							</td>
						</tr>
						<tr>
							<td></td>
							<td><label for="b_title">작가</label></td>
							<td><input type="text" id="b_author" name="b_author" class="form-control" value='이형석, 장남수, 전상환, 정상욱'/></td>
							<td><label for="b_pub" class="text-right">출판사</label></td>
							<td><input type="text" id="b_pub" name="b_pub" class="form-control" value="위키북스"/></td>
						</tr>
						<tr>
							<td colspan="1"><label for="b_name">책제목</label></td>
							<td class="text-left" colspan="4"><input type="text" id="b_name" name="b_name" class="form-control" value="직장인을 위한 데이터 분석 실무 with 파이썬 (개정판)" /></td>
						</tr>
						<tr>
							<td><label for="b_content">목차</label></td>
							<td colspan="4"><textarea name="b_list" id="b_list" cols="30" rows="10" class="form-control" rows="8">
							
							목차
▣ 01장: 준비하기
1.1 실습 자료 내려받기
1.2 파이썬 준비
____1.2.1 아나콘다 내려받기
____1.2.2 아나콘다 설치
____1.2.3 주피터 노트북 준비
____1.2.4 주피터 노트북 시작하기
____1.2.5 주피터 노트북 사용하기
1.3 파이썬 맛보기
____1.3.1 파이썬 코드 입력 및 실행
____1.3.2 값 입력 및 출력
____1.3.3 리스트
____1.3.4 반복문
____1.3.5 문자열
____1.3.6 조건문
____1.3.7 함수

▣ 02장: 데이터 분석 기초
2.1 pandas 기초
____2.1.1 pandas란?
____2.1.2 데이터 불러오기(read_excel)
____2.1.3 데이터 선택 ① - 칼럼 기준
____2.1.4 데이터 선택 ② - 로우 기준
____2.1.5 데이터 통합 ① - 옆으로 통합(merge)
____2.1.6 데이터 통합 ② - 아래로 통합(append)
____2.1.7 데이터 저장(to_excel)
____2.1.8 데이터 집계(pivot_table)
2.2 웹 크롤링 기초
____2.2.1 selenium과 크롬드라이버 설치
____2.2.2 크롬드라이버 활용하기
____2.2.3 웹 페이지 접속
____2.2.4 웹 페이지(HTML) 다운로드
____2.2.5 HTML 구조 살펴보기
____2.2.6 크롬 브라우저에서 웹 페이지의 HTML 살펴보기
____2.2.7 BeautifulSoup을 이용한 정보 찾기
____2.2.8 HTML 정보 찾기 ① - 태그 속성 활용
____2.2.9 HTML 정보 찾기 ② - 상위 구조 활용
____2.2.10 정보 가져오기 ① - 태그 그룹에서 하나의 태그 선택하기
____2.2.11 정보 가져오기 ② - 선택한 태그에서 정보 가져오기
____2.2.12 멜론 노래 순위 정보 크롤링
____2.2.13 selenium을 활용한 크롤링

▣ 03장: 데이터 분석 맛보기
3.1 여러 음원 서비스의 순위 수집/정리하기
____3.1.1 멜론 크롤링 결과를 엑셀로 저장하기
____3.1.2 벅스 크롤링 결과를 엑셀 파일로 저장하기
____3.1.3 지니 크롤링 결과를 엑셀 파일로 저장하기
____3.1.4 멜론, 벅스, 지니 크롤링 엑셀 파일 통합하기
3.2 유튜브 랭킹 데이터 수집과 시각화
____3.2.1 유튜브 랭킹 데이터 수집하기
____3.2.2 유튜브 랭킹 데이터 시각화하기
____3.2.3 결론

▣ 04장: 코로나 바이러스(COVID19)의 영향으로 중국인 관광객이 얼마나 줄었을까
4.1 외국인 출입국 통계 데이터 구하기
4.2 데이터 불러오기 및 전처리
____4.2.1 불러올 데이터의 형태 파악
____4.2.2 파이썬에서 엑셀 데이터 불러오기
____4.2.3 데이터 전처리
____4.2.4 데이터 전처리 과정을 함수로 만들기
____4.2.5 반복문을 통해 다수의 엑셀 데이터를 불러와서 합치기
____4.2.6 통합 데이터를 엑셀 파일로 저장하기
____4.2.7 국적별 필터링된 데이터를 엑셀 파일로 저장하기
4.3 데이터 시각화
____4.3.1 데이터 시각화의 중요성
____4.3.2 시계열 그래프 그리기
____4.3.3 히트맵 그래프 그리기
4.4 시각화 해석하기
4.5 정리

▣ 05장: 가장 뜨는 제주도 핫플레이스는 어디일까?
5.1 인스타그램 크롤링
____5.1.1 크롤링 과정
____5.1.2 인스타그램 접속 후 로그인하기
____5.1.3 인스타그램 검색 결과 URL을 만들어 접속하기
____5.1.4 첫 번째 게시글 열기
____5.1.5 게시글 정보 가져오기
____5.1.6 다음 게시글 열기
____5.1.7 여러 게시글 정보 수집하기
____5.1.8 수집 데이터 저장
____5.1.9 여러 엑셀 파일의 중복을 제거한 후 통합 저장
5.2 워드 클라우드
____5.2.1 워드 클라우드를 만드는 과정
____5.2.2 해시태그 데이터 불러오기
____5.2.3 해시태그 출현 빈도 집계
____5.2.4 막대차트로 해시태그 살펴보기
____5.2.5 워드 클라우드 그리기
5.3 지도 시각화
____5.3.1 지도 시각화 과정
____5.3.2 데이터 준비
____5.3.3 카카오 검색 API 가입
____5.3.4 카카오 로컬 API를 활용한 장소 검색
____5.3.5 위치 정보별 인스타 게시량 정리
____5.3.6 folium을 이용한 지도 시각화 ① - 개별 표시
____5.3.7 folium을 이용한 지도 시각화 ② - 그룹으로 표시
5.4 특정 단어를 포함한 게시글 찾기
____5.4.1 원하는 게시글 찾기
____5.4.2 데이터 준비하기
____5.4.3 단어 선택하기
5.5 정리

▣ 06장: 왜 우리 동네에는 스타벅스가 없을까?
6.1 데이터 수집
____6.1.1 크롤링을 이용한 서울시 스타벅스 매장 목록 데이터 생성
____6.1.2 서울열린데이터광장의 OPEN API를 활용한 공공데이터 수집
6.2 데이터 전처리
____6.2.1 서울시 스타벅스 매장 목록, 인구, 사업체 데이터에 시군구명, 시군구코드 추가
____6.2.2 스타벅스 분석 데이터 만들기
6.3 데이터 시각화
____6.3.1 스타벅스 매장분포 시각화
____6.3.2 시군구별 스타벅스 매장 수 시각화
____6.3.3 스타벅스 매장 수와 인구수 비교
____6.3.4 스타벅스 매장 수와 사업체 수 비교
6.4 정리

▣ 07장: 어떤 무선청소기가 인기가 좋을까?
7.1 데이터 수집 1 - 한 페이지 크롤링
____7.1.1 다나와 소개
____7.1.2 다나와 검색 페이지 접속
____7.1.3 다나와 검색 웹 페이지에서 상품 정보 가져오기
7.2 데이터 수집 2 - 여러 페이지에 걸친 다나와 검색 페이지 크롤링
____7.2.1 다나와 검색 결과 페이지 URL 분석
____7.2.2 주피터 노트북의 진행표시줄 처리
____7.2.3 여러 페이지에 걸친 상품 정보 수집
____7.2.4 수집 데이터 저장
7.3 다나와 크롤링 데이터 전처리
____7.3.1 다나와 크롤링 데이터 불러오기
____7.3.2 회사명, 모델명 정리
____7.3.3 스펙 목록 데이터 살펴보기
____7.3.4 스펙 목록에서 카테고리, 사용시간, 흡입력을 추출해서 정리
____7.3.5 무선청소기 사용시간 단위 통일시키기
____7.3.6 무선 청소기 흡입력 단위 통일시키기
____7.3.7 다나와 전처리 결과를 엑셀로 저장
7.4 무선청소기 모델별 비교 분석
____7.4.1 데이터 살펴보기
____7.4.2 가성비 좋은 제품 살펴보기
____7.4.3 데이터 시각화
____7.4.4 인기 제품의 데이터 시각화
7.5 정리
							
							
							</textarea></td>
						</tr>
						<tr>
							<td><label for="b_content">책 소개</label></td>
							<td colspan="4"><textarea name="b_info" id="b_info" cols="30" rows="10" class="form-control" rows="8">
							
						데이터 분석은 좋은 질문에서 시작합니다
이 책에서는 누구나 궁금했던 그 질문에 대해 데이터로 답해 다. 
이 책은 파이썬을 처음 접하는 마케팅, 영업, 기획 실무 담당자들이 파이썬을 활용한 데이터 분석에 재미있게 빠져들 수 있도록 
실제 업무에 활용할 수 있거나 흥미로운 예제로 구성되어있다. 
이 책을 마치고 나면 데이터를 기반으로 좋은 질문에 답할 수 있는 실력을 키울 수 있을 것이다.	
							
							</textarea></td>
						</tr>
						<tr>
							<td><label for="b_content">저자 소개</label></td>
							<td colspan="4"><textarea name="b_authorinfo" id="b_authorinfo" cols="30" rows="10" class="form-control" rows="8">
							
							
							저 : 이형석
주로 안드로이드 앱 개발자로 활동하다가 현재 회사인 '망고플레이트'에 입사하면서 본격적으로 안드로이드, 백엔드, 프런트엔드 업무까지 담당하는 등 
다양한 개발 경험을 쌓고 있다. 맛집 데이터 정보를 효율적으로 수집 및 관리하기 위해 파이썬을 이용하면서 
우연히 데이터 분석 스터디 모임인 Play with Data를 알게 됐고 모임을 준비하고 진행하면서 많은 것을 배우고 있다.
							
							
							</textarea></td>
						</tr>
						<tr>
							<td>
								미니 커버 사진(listcover)
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="listcoverFile" id="listcoverFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
						<tr>
							<td>
								커버사진(detailcover)
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="detailcoverFile" id="detailcoverFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
						<tr>
							<td>
								상세 이미지(detail)
							</td>
							<td class="text-left" colspan="5">
								<input type="file" name="detailFile" id="detailFile" class="margin_top btn-block" accept="image" />
							</td>
						</tr>
					</table>
					<div class="text-right">
						<button type="button" id="bookInsert" class="btn btn-success" >저장</button>
						<button type="button" id="bookInsertResetBtn" class="btn btn-default" >입력정보 초기화</button>
						<button type="button" id="bookListBtn" class="btn btn-default">목록</button>
					</div>
				</div>
			</form>
		</div><!-- content_wrap -->
	</body>
</html>
