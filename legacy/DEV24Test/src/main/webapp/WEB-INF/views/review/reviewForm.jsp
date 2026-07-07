<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		
		<link rel="stylesheet" href="/resources/include/css/style_board_content.css" />
		<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
		
		<script src="/resources/include/js/jquery-1.12.4.min.js"></script>
    	<script src="/resources/include/js/jquery-3.5.1.min.js"></script>
    	<script src="/resources/include/js/jquery.form.min.js"></script>
    	<script src="/resources/include/js/common.js"></script>
		<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>
		<style>
			.listWrap {
    			margin : 0 auto;
    			margin-top: 20px;
    			width: 1000px;
    			background-color:#fff;
    		}
    		.bookWrap {
    			width: 100%;
    			height: 200px;
    			border: 1px solid lightgray;
    			margin-bottom: 30px;
    		}
    		.listcoverWrap {
			   width: 120px;
			   float: left;
			   margin : 10px;
   			}
    		.listcover {
    			width: 100%;
    			border-right: solid #f4eeff 25px;
    		}
    		.booktext {
			    width: 400px;
			    float: left;
			    padding-left: 40px;
			    text-align: left;
			    margin-top:10px;
       		}
       		.b_name {
       			font-weight: 900;
       			font-size: 26px;
       			color: #736794;
       			overflow: hidden;
       			text-overflow: ellipsis;
       			width: 390px;
       		}
       		.b_nameText {
    			max-width: 500px;
       			font-weight: 1000;
       			font-size: 20px;
       			color: #736794;
       			padding-top: 10px;
       			text-overflow: ellipsis;
    			white-space: nowrap;
       			overflow: hidden;
       		}
       		.authorPub {
       			font-size: 14px;
       			padding-top: 5px;
       		}
       		.priceWrap {
       			padding: 3px 0 10px;
       			font-size: 20px;
       			font-weight: bold;
       		}
       		.won {
				color: #959595;
       			font-size: 14px;
       			padding-top: 3px;
       		}
       		.authorPub, .priceWrap {
       			color: #959595;
    			padding-top: 10px;
       		}
       		textarea#re_content{
       			height: 200px;
       		}
       		.stars{display: inline-block; margin-right: 30px;}
       		.stars li{
       			display : inline-block;
       			cursor : pointer;
       		}
       		.td_nickname{
       			padding-left:20px;
       		}
       		.td_img span{
       			display:block;
       		}
		</style>
		
		<script type="text/javascript">
			$(function(){
				var onOff = 0;
				/* 별 평점 클릭 */
				$(".stars > li").click(function(){
					var index = $(this).index();
					var getEmpty = '<i class="far fa-star"></i>';
					var getStar = '<i class="fas fa-star"></i>';
					var score = $(this).attr("data-score");
					console.log(score);
					$("#score").text(score+"점");

					$(".stars > li").eq(index).html(getStar);
					$(".stars > li").eq(index).prevAll("li").html(getStar);
					$(".stars > li").eq(index).nextAll("li").html(getEmpty);

				});
				
				
				/* 글자수 제한 */
				$("#re_content").keyup(function(){
					var content = $(this).val();
					$("#counter").html("("+ content.length +" / 최대 1000자)"); // 글자 수 실시간 카운팅
					if(content.length > 1000){
						alert("최대 1000자까지 입력 가능합니다.");
						$(this).val(content.substring(0,1000));
						$("#counter").html("(1000 / 최대 1000자)");
					}
				});
				
				
				/* 취소 버튼 클릭 처리 */
				$("#reviewCancelBtn").click(function(){
					location.href="/mypage/orderHistory";
				});
				
				
				/* 저장 버튼 클릭 처리 */
				$("#reviewInsertBtn").click(function(){
					var re_type = "";
					
					// 유효성 체크
					if($("#score").text() == ""){
						alert("평점을 입력해주세요.");
						return;
					}
					if(!chkData("#re_content", "내용을")) return;
					if($("#file").val() != ""){
						if(!chkFile($("#file"))) return;
						//console.log($("#re_imgurl").val());
						
						re_type = "image";
					}else{
						re_type = "text";
					}
					
					if(confirm("평점은 수정할 수 없습니다. 리뷰 등록하시겠습니까?")){
						var re_score = $("#score").text().substring(0,1);
						var c_nickname = $(".td_nickname").text();
						var b_num = $(".bookWrap").attr("data-num");
						var pd_num = "${pd_num}";
						
						$("#re_score").val(re_score);
						$("#c_nickname").val(c_nickname);
						$("#b_num").val(b_num);
						$("#pd_num").val(pd_num);
						$("#re_type").val(re_type);
						
						
						/* JSON.stringify() : JavaScript 값이나 객체를 JSON문자열로 변환 */
						var value = JSON.stringify({
								re_score : re_score,
								c_nickname : c_nickname,
								b_num : b_num,
								pd_num : pd_num,
								re_type : re_type,
								re_content : $("#re_content").val()
						});
						
						if(re_type=="image"){
							$("#f_writeForm").ajaxForm({
								url : "/review/reviewInsert",
								type : "post",
								enctype: 'multipart/form-data',
								dataType : "text",
								error : function(){
									alert("시스템 오류입니다. 관리자에게 문의하세요.");
								},
								success : function(result){
									if(result=="SUCCESS"){
										alert("리뷰 등록이 완료되었습니다.");
										
										location.href="/book/detail/"+b_num;
									}
								}
							});
							$("#f_writeForm").submit();
							
						}else if(re_type=="text"){
							var insertUrl = "/review/reviewInsert";
							$.ajax({
								url : insertUrl,
								type : "post",
								/* headers : { // 전달할 값이 json형태이기 때문에 headers 필요!
									"Content-Type" : "application/json",
									"X-HTTP-Method-Override" : "POST"
								}, */
								dataType : "text",
								data : $("#f_writeForm").serialize(),
								error : function(){
									alert("시스템 오류입니다. 관리자에게 문의하세요.");
								},
								success : function(result){
									if(result=="SUCCESS"){
										alert("리뷰 등록이 완료되었습니다.");
										
										location.href="/book/detail/"+b_num;
									}
								}
							});
						}
						
						
					}

							
				});
				
			});// 최상위 종료
			
			
		</script>

	</head>
	<body>
		
        <div id="title">
            <div id="tit_content">
                <h3>리뷰 작성</h3>
            </div>
        </div>
	        
	    <div id="content_wrap">
	       
	        <div id="content">
	        	<%-- 책정보 출력 부분 --%>
	        	<div class="listWrap">
					<div class="bookWrap" data-num="${bookInfo.b_num}">
						<div class="listcoverWrap text-left">
							<img class="listcover" src="${bookInfo.listcover_imgurl}">
						</div>
						<div class="lineDiv"></div>
						<div class="booktext text-left">
							<h1 class="b_name" title="${bookInfo.b_name}">
								<span class="b_nameText" >${bookInfo.b_name}</span>
							</h1>
							<span class="authorPub">${bookInfo.b_author} 저 | ${bookInfo.b_pub}</span>
							<p class="priceWrap">
								<span class="b_price" style="display: none" >${bookInfo.b_price}</span>
									<fmt:formatNumber value="${bookInfo.b_price}" pattern="#,###"/>
								<span class="won">원</span>
							</p>
						</div>
					</div>
				</div>
	        
	            <form id="f_writeForm">
	            	<input type="hidden" name="re_score" id="re_score" />
	            	<input type="hidden" name="c_nickname" id="c_nickname" />
	            	<input type="hidden" name="b_num" id="b_num" />
	            	<input type="hidden" name="pd_num" id="pd_num" />
	            	<input type="hidden" name="re_type" id="re_type" />
	            
	                <table class="table" border="1">
	                    <colgroup>
	                        <col width="20%" />
	                        <col width="80%" />
	                    </colgroup>
	                    <tr class="tr_title">
	                        <th>평점</th>
	                        <td>
	                        	<ul class="stars">
		                        	<li data-score="1"><i class="far fa-star"></i></li>
									<li data-score="2"><i class="far fa-star"></i></li>
									<li data-score="3"><i class="far fa-star"></i></li>
									<li data-score="4"><i class="far fa-star"></i></li>
									<li data-score="5"><i class="far fa-star"></i></li>
								</ul>
								<span id="score"></span>
	                        </td>
	                    </tr>
	                    <tr>
	                        <th>작성자</th>
	                        <td class="td_nickname">${login.c_nickname}</td>
	                    </tr>
	                    <tr>
	                        <th>리뷰내용</th>
	                        <td><textarea id="re_content" name="re_content" rows="20" cols="100" class="form-control" placeholder="1000자 이내로 작성해주세요."></textarea>
	                        <br/>
	                        <span style="color:#aaa" id="counter">(0 / 최대 1000자)</span></td>
	                    </tr>
	                    <tr>
	                    	<th>이미지첨부</th>
	                    	<td class="td_img">
	                    		<input type="file" name="file" id="file" class="form-control" />
	                    		<span>사진은 1장만 업로드할 수 있습니다.</span>
	                    	</td>
	                    </tr>
	                </table>
	            </form>
	
	            <div id="button_wrap">
	                <input type="button" id="reviewInsertBtn" class="btn btn-success" value="저장" />
	                <input type="button" id="reviewCancelBtn" class="btn btn-default" value="취소" />
	            </div>    
	        
	        </div> <!-- content (width : 1200px) -->
	    </div> <!-- content_wrap -->
	    
	</body>
</html>