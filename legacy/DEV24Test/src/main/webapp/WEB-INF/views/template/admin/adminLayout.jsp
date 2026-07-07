<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>DEV24 Admin Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link href="/resources/include/dist/css/bootstrap.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="/resources/include/dist/css/dashboard.css" rel="stylesheet">
	<link rel="stylesheet" href="/resources/include/css/adminPage.css">

    <script src="/resources/include/dist/js/ie-emulation-modes-warning.js"></script>
    
	<script src="https://kit.fontawesome.com/a333e3670c.js" crossorigin="anonymous"></script>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    
    <script type="text/javascript" src="/resources/include/js/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/resources/include/js/jquery-3.5.1.min.js"></script>
    <script type="text/javascript" src="/resources/include/js/holder.js"></script>
    <style type="text/css">
    	div.sidebar{
    		width:260px;
    	}
    	
    	
		*{font-family: 'Noto Sans KR', sans-serif;}
    </style>
    
    <script> 
       Holder.addTheme("blue", {
         bg: "#0099ff",
         fg: "#ffffff",
         size: 20,
         font: "Helvetica Neue, Helvetica, Arial, sans-serif",
         fontweight: "normal"
       });
       
       Holder.addTheme("green", {
           bg: "#00cc99",
           fg: "#ffffff",
           size: 20,
           font: "Helvetica Neue, Helvetica, Arial, sans-serif",
           fontweight: "normal"
         });
       
       Holder.addTheme("red", {
           bg: "#ff0000",
           fg: "#ffffff",
           size: 20,
           font: "Helvetica Neue, Helvetica, Arial, sans-serif",
           fontweight: "normal"
        });

   </script>
    
    <script type="text/javascript">
       $(function(){
          var date = new Date();
           $(".date").text(date);   
           $("#today").text(date);  
           
           $(".nav-sidebar > li").click(function(){
         	  $(this).siblings("li").removeClass("active");
         	  $(this).addClass("active");
            });
            
       });
       
       
    </script>
    
  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
    <tiles:insertAttribute name="header" />
    </nav>

    <div class="container-fluid">
     <%--  <div class="row"> --%>
    	<tiles:insertAttribute name="menu" />
    	<tiles:insertAttribute name="adminbody" />
      <%-- </div> --%>
    </div>

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script src="/resources/include/dist/js/bootstrap.min.js"></script>
    <script src="/resources/include/dist/js/docs.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="/resources/include/dist/js/ie10-viewport-bug-workaround.js"></script>
  </body>
</html>