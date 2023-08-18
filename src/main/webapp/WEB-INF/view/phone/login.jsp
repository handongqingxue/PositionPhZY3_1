<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
	String basePath=request.getScheme()+"://"+request.getServerName()+":"
		+request.getServerPort()+request.getContextPath()+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<script type="text/javascript" src="<%=basePath %>resource/js/jquery-3.3.1.js"></script>
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
$(function(){
	
});

function runAndroidFunction(flag){
	if(flag=="getPageName"){
		AndroidFunction.getPageName('${param.page}');
	}
}

function checkLogin(){
	if(checkTenantId()){
		if(checkUsername()){
			if(checkPassword()){
				login();
			}
		}
	}
}

function login(){
	var tenantId = $("#tenantId_inp").val();
	var username = $("#username_inp").val();
	var password = $("#password_inp").val();
	$.post("login",
		{tenantId:tenantId,username:username,password:password},
		function(data){
			//alert(JSON.stringify(data));
			if(data.status=="ok"){
				location.href=phonePath+"goPage?page=index";
			}
			else{
				alert(data.message);
			}
		}
	,"json");
}

function focusTenantId(){
	var tenantId = $("#tenantId_inp").val();
	if(tenantId=="租户编号不能为空"){
		$("#tenantId_inp").val("");
		$("#tenantId_inp").css("color", "#555555");
	}
}

//验证租户编号
function checkTenantId(){
	var tenantId = $("#tenantId_inp").val();
	if(tenantId==null||tenantId==""||tenantId=="租户编号不能为空"){
		$("#tenantId_inp").css("color","#E15748");
    	$("#tenantId_inp").val("租户编号不能为空");
    	return false;
	}
	else
		return true;
}

function focusUsername(){
	var userId = $("#username_inp").val();
	if(userId=="用户名不能为空"){
		$("#username_inp").val("");
		$("#username_inp").css("color", "#555555");
	}
}

//验证用户名
function checkUsername(){
	var userId = $("#username_inp").val();
	if(userId==null||userId==""||userId=="用户名不能为空"){
		$("#username_inp").css("color","#E15748");
    	$("#username_inp").val("用户名不能为空");
    	return false;
	}
	else
		return true;
}

//验证密码
function checkPassword(){
	var password = $("#password_inp").val();
	if(password==null||password==""){
	  	alert("密码不能为空");
	  	return false;
	}
	else
		return true;
}
</script>
<style type="text/css">
body{
	margin:0;
	background-color:#fff;
}
.main_div{
	width: 300px;
	height: 250px;
	margin:65px auto 0;
}
.title_h1{
    font-size:18px;
    text-align: center;
}
.tenantId_div,.username_div,.password_div {
    width: 230px;
    height: 48px;
    line-height: 48px;
    margin: 20px auto 0;
}
.tenantId_img,.username_img{
	width: 20px;
	height:26px;
}
.passwor_img{
	width: 24px;
	height:23px;
}
.tenantId_inp_div,.username_inp_div,.password_inp_div{
    width: 200px;
    height: 48px;
    margin-top: -48px;
    margin-left: 28px;
    background-color: #fff;
    border-bottom: 3px solid #eee;
}
.tenantId_inp,.username_inp,.password_inp{
    width: 180px;
    height: 45px;
    margin-top: 10px;
    padding-left: 10px;
    padding-right: 10px;
    border: 0;
}
.loginBut_div{
    width: 230px;
    height: 38px;
    line-height: 38px;
    margin: 25px auto 0;
    font-size: 16px;
    color: #fff;
    text-align: center;
    background-color: #1777FF;
    border-radius: 4px;
}
</style>
<title>Insert title here</title>
</head>
<body>
<div class="main_div">
    <h1 class="title_h1">人员定位系统手机版</h1>
    <div class="tenantId_div">
        <img class="tenantId_img" alt="" src="<%=basePath %>resource/image/001.png">
        <div class="tenantId_inp_div">
            <input class="tenantId_inp" id="tenantId_inp" placeholder="请输入租户编号" value="${requestScope.tenantId }" onfocus="focusTenantId()" onblur="checkTenantId()"/>
        </div>
    </div>
    <div class="username_div">
        <img class="username_img" alt="" src="<%=basePath %>resource/image/001.png">
        <div class="username_inp_div">
            <input class="username_inp" id="username_inp" placeholder="请输入用户名" value="${requestScope.username }" onfocus="focusUsername()" onblur="checkUsername()"/>
        </div>
    </div>
    <div class="password_div">
        <img class="passwor_img" alt="" src="<%=basePath %>resource/image/002.png">
        <div class="password_inp_div">
            <input class="password_inp" id="password_inp" type="password" placeholder="请输入密码" value="${requestScope.password }" onblur="checkPassword()"/>
        </div>
    </div>
    <div class="loginBut_div" onclick="checkLogin()">登录</div>
</div>
</body>
</html>