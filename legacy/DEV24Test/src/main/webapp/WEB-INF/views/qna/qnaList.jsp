<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>     
<!DOCTYPE html>
<!-- 문서 유형 : 현재 웹 문서가 어떤 HTML 버전에 맞게 작성되었는지를 알려준다. -->

<!--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
     DTD 선언문이 HTML 페이지의 가장 첫 라인에 명시되어야 웹 브라우저가 HTML 버전을 인식.
     HTML태그나 CSS를 해당 버전에 맞도록 처리하므로 웹 표준 준수를 위하여 반드시 명시되어야 한다.-->
<html lang="ko">
	<head>
		<meta charset="UTF-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no" />
		
		<title>qnaList</title>
		
		<link rel="stylesheet" type="text/css" href="/resources/include/css/style_boot.css" />
      	<link rel="stylesheet" type="text/css" href="/resources/include/css/style_board_content.css" />
      	
      	<style type="text/css">
      		#boardList .rCount{font-size:10px; color:red;}
      		
      		.required{color:red;}
      	</style>
		
		
		<script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
      	<script type="text/javascript" src="/resources/include/js/common.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/js/bootstrap.min.js"></script>
      	<script type="text/javascript" src="/resources/include/dist/css/bootstrap.min.css"></script>
		
      	
		<!-- 모바일 웹 페이지 설정 -->
		<link rel="shortcut icon" href="/resources/images/icon.png" />
		<link rel="apple-touch-icon" href="/resources/images/icon.png" />
		<!-- 모바일 웹 페이지 설정 끝 -->
		
		<!--[if lt IE 9]>
		<script src="../js/html5shiv.js"></script>
		<![endif]-->
		
		<script>
		$(function(){
			
			/* 검색 후 검색 대상과 검색 단어 출력 */
			var word = "<c:out value='${data.keyword}' />";
			var value = "";
			if(word!=""){
				$("#keyword").val("<c:out value='${data.keyword}' />");
				$("#search").val("<c:out value='${data.search}' />");
				
				if($("#search").val()!='q_content'){
					//:contains()는 특정 텍스트를 포함한 요소반환
					if($("#search").val()=='q_title') value = "#list tr td.goDetail";
					else if($("#search").val()=='c_nickname') value = "#list tr td.name";
					console.log($(value+":contains('"+word+"')").html());
					
					$(value+":contains('"+word+"')").each(function(){
						var regex = new RegExp(word,'gi');
						$(this).html($(this).html().replace(regex, "<span class='required'>"+word+"</span>"))
					});
				}
			}// main first-If end
						
			
			//"글쓰기" 버튼 클릭시 처리 이벤트
			$(function(){
				$("#qwriteFormBtn").click(function(){
					location.href="/qna/qwriteForm"
				});
			});
			
			/* 제목 클릭시 상세 페이지 이동을 위한 처리 이벤트 */
			$(".goDetail").click(function(){
				var q_num = $(this).parents("tr").attr("data-num");
				$("#q_num").val(q_num);
				//console.log("글번호: " + q_num);
				// 상세 페이지로 이동하기 위해 form 추가 (id: detailForm)
				$("#qdetailForm").attr({
					"method":"get",
					"action":"/qna/qnaDetail"
				});
				$("#qdetailForm").submit();
			});
			
			
			/* 검색 대상이 변경될 때마다 처리 이벤트 */
			$("#search").change(function(){
				if($("#search").val()=="all"){
					$("#keyword").val("전체 데이터 조회합니다.");
				}else if($("#search").val()!="all"){
					$("#keyword").val("");
					$("#keyword").focus();
				}
			});
			
			/* 검색 버튼 클릭 시 처리 이벤트 */
			$("#searchData").click(function(){
				if($("#search").val()!="all"){
					if(!chkData("#keyword","검색어를")) return;
				}
				goPage();
			});
			
		}); //$종료
		
		/* 검색을 위한 실질적인 함수 처리 */
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			$("#q_search").attr({
				"method":"get",
				"action":"/qna/qnaList"
			});
			$("#q_search").submit();
		}
		
		
		</script>
		
		
		
	</head>
	<body>
		<div class="content_wrap">
			<div id="title">
	            <div id="tit_content">
	                <h3>QNA</h3>
	            </div>
	        </div>
			
			<form name="qdetailForm" id="qdetailForm">
				<input type="hidden" name="q_num" id="q_num" />
			</form>
			
			<div id="content">
				<%-- =============검색기능 시작================== --%>	
				<div id="board_search" class="text-right">
					<form id="q_search" name="q_search" class="form-inline">
						<div class="form-group">
							<label>검색조건</label>
							<select id="search" name="search" class="form-control">
								<option value="all">전체</option>
								<option value="q_title">제목</option>
								<option value="q_content">내용</option>
								<option value="c_nickname">작성자</option>
							</select>
							<input type="text" name="keyword" id="keyword" value="검색어를 입력하세요" class="form-control" />
							<button type="button" id="searchData" class="btn btn-primary">검색</button>
						</div>
					</form>
				</div>
				<%-- =============검색기능 끝================== --%>
				
				<%-- ================== 리스트 시작 ======================= --%>
				<div id="qnaList">
					<table summary="Q&A 게시판 리스트" class="table table-hover">
						<colgroup>
	                      <col width="10%" />
	                      <col width="40%" />
	                      <col width="20%" /> 
	                      <col width="20%" />
	                      <col width="10%" />
	                  </colgroup>
						<thead>
							<tr>
								<th class="text-center">번호</th>
								<th class="text-center">제목</th>
								<th>작성자</th>
								<th>작성일</th>
								<th>조회수</th>
							</tr>
						</thead>
						<tbody id="list" class="table-striped">
							<c:choose>
								<c:when test="${not empty qnaList}" >
									<c:forEach var="qna" items="${qnaList}" varStatus="status">
										<tr class="text_center" data-num="${qna.q_num }">
											<td>${qna.q_num}</td>
											<%-- <td class="tal"><span class="goDetail">${vo.title}</span></td> --%>
											<td class="tal">
												<c:if test="${qna.q_repStep>0}">
													<c:forEach begin="1" end="${qna.q_repIndent}">
														&nbsp;&nbsp;&nbsp;												
													</c:forEach>
													<img src="/resources/image/rep.gif" />
												</c:if>
												<span class="goDetail">${qna.q_title}</span>
											</td>
											
											<td>${qna.c_nickname}</td>
											<td>${qna.q_writedate}</td>
											<td>${qna.q_readcnt}</td>
										</tr>	
									</c:forEach>
								</c:when>
								<c:otherwise>
									<tr>
										<td colspan="5" class="text-center">등록된 게시물이 존재하지 않습니다.</td>
									</tr>
								</c:otherwise>	
							</c:choose>
						</tbody>		
					</table>
				</div>
				<!-- =========================== 리스트 종료 ======================== -->
				
				
				
				<%------------- 글쓰기 버튼 출력시작 ----------------%>
				<div class="contentBtn text-right" id="button_wrap">
					<input type="button" value="글쓰기" id="qwriteFormBtn" class="btn btn-success" />
				</div>
			
			
			</div>
		</div>
	</body>
</html>
    