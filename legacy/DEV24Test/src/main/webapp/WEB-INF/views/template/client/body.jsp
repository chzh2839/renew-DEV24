<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
<script src="https://unpkg.com/swiper/swiper-bundle.js"></script>
<script src="https://unpkg.com/swiper/swiper-bundle.min.js"></script>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <title>Swiper demo</title>
  <meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1">

  <!-- Link Swiper's CSS -->
  <link rel="stylesheet" href="/resources/include/css/swiper-bundle.css">

  <!-- Demo styles -->
  <style>
     #content-wrap {
        width: 100%;
        text-align: center;
     }
    
     .bodyWrap {
        display: block;
        width: 100%;
        text-align: center;
     }
     .content {
        display: inline-block;
        width: 1200px
     }
    .swiper-container {
      width: 950px;
      height: 255px;
      float: right;
    }
    
    .swiper-container * {
       z-index: 9999;
    }

    .swiper-slide {
      text-align: center;
      font-size: 18px;
      background: #fff;

      /* Center slide text vertically */
      display: -webkit-box;
      display: -ms-flexbox;
      display: -webkit-flex;
      display: flex;
      -webkit-box-pack: center;
      -ms-flex-pack: center;
      -webkit-justify-content: center;
      justify-content: center;
      -webkit-box-align: center;
      -ms-flex-align: center;
      -webkit-align-items: center;
      align-items: center;
     /* width : 230px !important; */
      height: auto;
      width: 250px;
      background-color: #fafafa;
     /* align-items: flex-start; */
    }
    
    .swiperImage {
       width: 170px;
       box-shadow: 6px 6px 5px -4px grey;
      transition: all ease 0.2s;
    }
    
    .swiperImage:hover {
      transform: scale(1.05);
    }
    
    .swiper-left{
        box-shadow: 6px 6px 17px -2px grey;
       display: inline-block;
       width: 190px;
       height: 139px;
       margin: 15px 0;
       background-color: #424874;
       padding-top: 80px;
       font-size: 20px;
       color: #f4eeff;
    }
   #content_wrap{
      width:1200px;
      margin : 0 auto;
   }
   #row1{
      width: 100%;
      height: 300px;
      margin-top : 30px;
   }
   #row1 > div{
      float:left;
   }
   #row1{
      clear : both;
      display : block;
      content:'';
   }
   #main_menu{
      width:calc(50% - 40px);
      border: 4px solid #a6b1e1;
       padding: 20px;
       border-radius: 10px;
       height: 220px;
       line-height: 30px;
   }
   #main_menu > ul{
      float:left;
       width:50%;
   }
   #main_menu > ul > li{text-align : center;}
   #main_menu > ul > li >a{
      font-size: 18px;
   }
   #main_menu:after{
      clear : both;
      display : block;
      content:'';
   }
   #row1 #main_img{
      width:calc(50% - 20px);
      float:right;
      height: 268px;
           background-image: url("/resources/image/main_image.jpg");
           background-size: cover;
       background-repeat: no-repeat;
       background-position: 0px -60px;
       position: relative;
   }
   #main_img > p{
      position: absolute;
      top:50%;
       left: 50%;
       transform: translate(-50%, -50%);
       color:#fff;
       font-size:35px;
       width: 100%;
       text-align: center;
       font-weight: bold;
       letter-spacing: 5px;
       text-shadow: 6px 7px 5px rgba(0,0,0,0.7);
   }
   li.all{
      font-weight: bold;
      padding-bottom: 10px;
   }
   li.all:after{
       content: '';
       display: block;
       border: 1px solid #ccc;
       margin: 0px 30px 0px 30px;
       background-color:#ccc;
       margin-top: 10px;
   }
   #main_menu ul li:hover{
      text-decoration: underline;
   }
  </style>
</head>

<body>
   <div class="bodyWrap" >
      <div id="row1">
         <div id="main_menu">
             <ul class="book_menu">
                  <li class="all"><a href="/book/10">일반도서 전체</a></li>
                  <li class=""><a href="/book/11">프로그래밍 언어</a></li>
                  <li><a href="/book/12">OS/데이터베이스</a></li>
                  <li><a href="/book/13">웹사이트</a></li>
                  <li><a href="/book/14">컴퓨터 입문/활용</a></li>
                  <li><a href="/book/15">네트워크/해킹/보안</a></li>
                </ul>
                <ul class="ebook_menu">
                    <li class="all"><a href="/book/20">eBook 전체</a></li>
                   <li><a href="/book/26">IT전문서</a></li>
                   <li><a href="/book/27">컴퓨터 수험서</a></li>
                   <li><a href="/book/28">웹/컴퓨터 입문&활용</a></li>
                </ul>
         </div>
         <div id="main_img">
            <p>우리 모두 책을 읽읍시다.</p>
         </div>
      
      </div> <!-- row1 -->
      <div class="content">
         <div class="swiperWrap">
              <div class="swiper-left">
                    DEV24의 <br/>
                    추천 개발자 서적
              </div>
                 <!-- Swiper -->
              <div class="swiper-container pull-right">
                <div class="swiper-wrapper">
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[0].b_num}"><img class="swiperImage" alt="" src="${ bvoList[0].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[1].b_num}"><img class="swiperImage" alt="" src="${ bvoList[1].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[2].b_num}"><img class="swiperImage" alt="" src="${ bvoList[2].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[3].b_num}"><img class="swiperImage" alt="" src="${ bvoList[3].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[4].b_num}"><img class="swiperImage" alt="" src="${ bvoList[4].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[5].b_num}"><img class="swiperImage" alt="" src="${ bvoList[5].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[6].b_num}"><img class="swiperImage" alt="" src="${ bvoList[6].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[7].b_num}"><img class="swiperImage" alt="" src="${ bvoList[7].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[8].b_num}"><img class="swiperImage" alt="" src="${ bvoList[8].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[9].b_num}"><img class="swiperImage" alt="" src="${ bvoList[9].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[10].b_num}"><img class="swiperImage" alt="" src="${ bvoList[10].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[11].b_num}"><img class="swiperImage" alt="" src="${ bvoList[11].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[12].b_num}"><img class="swiperImage" alt="" src="${ bvoList[12].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[13].b_num}"><img class="swiperImage" alt="" src="${ bvoList[13].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[14].b_num}"><img class="swiperImage" alt="" src="${ bvoList[14].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[15].b_num}"><img class="swiperImage" alt="" src="${ bvoList[15].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[16].b_num}"><img class="swiperImage" alt="" src="${ bvoList[16].listcover_imgurl }" /></a></div>
                  <div class="swiper-slide"><a href="/book/detail/${bvoList[17].b_num}"><img class="swiperImage" alt="" src="${ bvoList[17].listcover_imgurl }" /></a></div>
                </div>
                <!-- Add Arrows -->
                <div class="swiper-button-next"></div>
                <div class="swiper-button-prev"></div>
                <!-- Add Pagination -->
                <div class="swiper-pagination"></div>
              </div>
         </div>
     </div>
   </div>



  <!-- Initialize Swiper -->
  <script>
    var hold = new Swiper('.hold', {
      slidesPerView: 1,
      spaceBetween: 0,
      autoplay: stop,
      pagination: {
        el: '.swiper-pagination',
        clickable: true,
      },
    });
    
   var main = new Swiper('.swiper-container', {
     slidesPerView: 5,
     spaceBetween: 5,
     slidesPerGroup: 1,
     loop: true,
     loopFillGroupWithBlank: true,
     pagination: {
       el: '.swiper-pagination',
       type: 'progressbar',
       clickable: true,
     },
     navigation: {
       nextEl: '.swiper-button-next',
       prevEl: '.swiper-button-prev',
     }, 
     autoplay: {
          delay: 4000,
        },
   });
 </script>