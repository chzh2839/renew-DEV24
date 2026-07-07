<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags/" %>  
<!DOCTYPE html>
<html>
	<head>
		 <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap.min.css" />
         <link rel="stylesheet" type="text/css" href="/resources/include/dist/css/bootstrap-theme.css" />
         <link rel="stylesheet" href="/resources/include/css/adminPage.css">
         
         <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
         <script type="text/javascript" src="/resources/include/dist/js/bootstrap.js"></script>
         <script type="text/javascript" src="/resources/include/js/common.js"></script>
         
		<meta charset="UTF-8">
		<title>DEV24 Admin FreeboardList</title>
		
		<style type="text/css">
			#table{ padding:10px;}
		</style>
		
		<script type="text/javascript">
			$(function(){
				$(".deleteBtn").click(function(){
					var fb_num = $(this).parents("tr").attr("data-num");
					$("#fb_num").val(fb_num);
					console.log("글번호="+fb_num);
					
					$.ajax({
						url:"/freeboard/freeboardDelete", 
						type:"post",
						data:"fb_num="+fb_num, 
						dataType:"text",
						error: function(){
							alert("시스템 오류 입니다");
						}, 
						success: function(){
							var confirmation = confirm("정말로 이 사용자의 글을 삭제하시겠습니까?");
							if(confirmation==true){
								alert("글 삭제 완료!");
								location.href="/admin/freeboardAdmin";
							}else{
								return;
							}
						}
						
					});
				});	/*.deleteBtn 기능 구현 함수 끗*/
				
				$("#searchBtn").click(function(){
					if($("#search").val()!="all"){
						if(!chkSubmit("#keyword", "검색어를")) return;
					}
					goPage();
				});
				
				$(".goDetail").click(function(){
					var fb_num= $(this).parents("tr").attr("data-num");
					$("#fb_num").val(fb_num);
					console.log("글번호: "+fb_num);
					//상세 페이지로 이동하기 위해 form 추가(id:detailForm)
					
					$("#detailForm").attr({
						"method":"get", 
						"action":"/admin/freeboardAdminDetail"
					});
					$("#detailForm").submit();
				});
				
				
				//페이징 처리를 위한 구문. 
				$(".paginate_button a").click(function(e){
					e.preventDefault();
					$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
					goPage();
				});
				
				
			});
			
			 function goPage(){
					$("#searchForm").attr({
						"method":"get", 
						"action":"/admin/freeboardAdmin"
					});
					$("#searchForm").submit();
			}
			
		</script>
		
	</head>
	<body>
		<div id="content_wrap">
			<div id="upper">
				<div class="center">
					<h2 id="tit">자유 게시판 관리자 페이지</h2>
					
					<form id="searchForm">
					<input type="hidden" name="pageNum" value="${pageMarker.cvo.pageNum}"/>
			    	<input type="hidden" name="amount" value="${pageMarker.cvo.amount}"/>
						<div class="form-inline" id="searchCategory">
							<select id="search" name="search">
								<option value="all">전체</option>
								<option value="fb_title">글제목</option>
								<option value="fb_author">작성자</option>
								<option value="fb_content">글내용</option>
								<option value="fb_num">글번호</option>
							</select>
							<input type="text" id="keyword" name="keyword" placeholder="검색대상을 입력해주세요"/>
							<button type="button" class="btn btn-primary btn-sm" id="searchBtn">검색</button>
						</div> <!-- searchCategory -->
					</form> <!-- searchForm -->
					
					
				</div> <!-- center -->
			</div> <!-- upper -->
			
			<form id="detailForm">
				<input type="hidden" id="fb_num" name="fb_num"/>
			</form>
			
			<div id="table">
				<table class="table table-striped listTable" border="1">
					<thead>
						<tr>
							<th>글번호</th>
							<th>제목</th>
							<th>작성자</th>
							<th>날짜</th>
							<th>조회수</th>
							<th>관리</th>
						</tr>
					</thead>
					<tbody>
						<c:choose>
							<c:when test="${not empty adminfbList}">
								<c:forEach var="free" items="${adminfbList}" varStatus="status">
									<tr class="text-center" data-num="${free.fb_num}">
										<td>${free.fb_num}</td>
										<td class="goDetail">
											${free.fb_title}
										</td>
										<td>${free.fb_author}</td>
										<td>${free.fb_writeday}</td>
										<td>${free.fb_readcnt}</td>
										<td><input type="button" class="deleteBtn" value="글삭제" /></td>
									</tr>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<td colspan="6" class="text-center">등록된 게시글이 없습니다.</td>
							</c:otherwise>
						</c:choose>
					</tbody>
				</table>
			</div> <!-- table -->
			
			<div class="text-center">
 				 <tag:pagination pageNum="${pageMarker.cvo.pageNum}" amount="${pageMarker.cvo.amount}" 
				startPage="${pageMarker.startPage}" endPage="${pageMarker.endPage}" prev="${pageMarker.prev}" next="${pageMarker.next}" />
 			</div>
 			
		</div> <!-- cotent_wrap -->
	</body>
</html>
