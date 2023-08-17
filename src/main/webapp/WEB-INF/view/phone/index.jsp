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
var selectedFloorValue="";
$(function(){
	showSSTJDiv(false);
	summaryOnlineData();
});

function showSSTJDiv(flag){
	var xssstjButImg=$("#xssstj_but_img");
	var sstjDiv=$("#sstj_div");
	if(flag){
		xssstjButImg.css("display","none");
		sstjDiv.css("display","block");
	}
	else{
		xssstjButImg.css("display","block");
		sstjDiv.css("display","none");
	}
}

function summaryOnlineData(){
	$.post("summaryOnlineData",
		function(data){
			var entityResult=data.entityResult;
			var childAreaList=data.childAreaList;
			console.log(entityResult);
			initFloorSel(entityResult,childAreaList);
			//initSSDWCanvas(0);
		}
	,"json");
}

function initFloorSel(entityResult,childAreaList){
	var floorSel=$("#floor_sel");
	floorSel.empty();
	var name=entityResult.name;
	var summary=entityResult.summary;
	var online=summary.online;
	floorSel.append("<option value=\"\">"+name+" ("+online.total+")</option>");
	
	var children=entityResult.children;
	for(var i=0;i<children.length;i++){
		var child=children[i];
		var optionValue;
		var childAreaId=child.areaId;
		var childName=child.name;
		/*
		switch (childName) {
		case "道路":
			optionValue=0;
			break;
		case "厂房一层":
			optionValue=1;
			break;
		case "厂房二层":
			optionValue=2;
			break;
		case "厂房三层":
			optionValue=3;
			break;
		}
		*/
		for(var j=0;j<childAreaList.length;j++){
			var childArea=childAreaList[j];
			if(childAreaId==childArea.id){
				optionValue=childArea.value;
				floorSel.append("<option value=\""+optionValue+"\" "+(selectedFloorValue==optionValue?"selected":"")+">"+childName+" ("+child.summary.online.total+")</option>");
				break;
			}
		}
	}
	//console.log(JSON.stringify(children));
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.load_map_div{
	width: 100%;
	height:100%;
	background-color: rgba(0,0,0,0.5);
	position: fixed;
	display:none;
	z-index: 1;
}
.load_map_div .text_div{
	width: 100%;
	color:#fff;
	text-align:center;
	font-size:25px;
	top:45%;
	position: absolute;
}
.xssstj_but_img{
	width:30px;
	height:25px;
	margin-top:10px;
	right:10px;
	position: fixed;
	z-index: 1;
}
.sstj_div{
	width: 100%;
	padding:1px;
	background-color: #F6F6F6;
	position: fixed;
}
.sstj_div .row_close_div{
	width: 100%;
	height: 24px;
}
.sstj_div .row_label_div{
	width: 100%;
	margin: 10px 0 10px;
}
.sstj_div .close_but_div{
	margin-top: 3px;
	margin-right: 20px;
	color: #636468;
	float: right;
}
.sstj_div .dtrs_span,.sstj_div .duty_span,.sstj_div .label_span{
	margin-left: 15px;
	color: #636468;
	font-size: 15px;
}
.sstj_div .floor_sel,.sstj_div .duty_sel{
	width: 150px;
	height: 25px;
	line-height: 25px;
	margin-left:25px;
	color: #636468;
}
.sstj_div .label_list_div{
	width: 150px;
	height: 270px;
	margin-top: -25px;
	margin-left: 104px;
	border: #999 solid 1px;
	border-radius:5px;
	overflow: auto; 
}
.sstj_div .label_list_div .item_div{
	width: 100%;
	height: 30px;
	line-height: 30px;
	color: #636468;
}
.sstj_div .label_list_div .item_div .select_cb{
	margin-top: 10px;
}
.sstj_div .label_list_div .item_div .name_span{
	margin-left: 5px;
}
</style>
<title>辰麒人员定位安全管理平台</title>
</head>
<body>
<div class="load_map_div" id="load_map_div">
	<div class="text_div">地图加载中</div>
</div>
<img class="xssstj_but_img" id="xssstj_but_img" alt="" src="<%=basePath %>resource/image/005.png" onclick="showSSTJDiv(true);">
<div class="sstj_div" id="sstj_div">
	<div class="row_close_div">
		<div class="close_but_div" onclick="showSSTJDiv(false);">X</div>
	</div>
	<div class="row_dtrs_div">
		<span class="dtrs_span">地图人数</span>
		<select class="floor_sel" id="floor_sel" onchange="initSSDWCanvas(0);">
		</select>
	</div>
	<div class="row_label_div">
		<span class="label_span">标签</span>
		<div class="label_list_div" id="label_list_div">
		</div>
	</div>
</div>
</body>
</html>