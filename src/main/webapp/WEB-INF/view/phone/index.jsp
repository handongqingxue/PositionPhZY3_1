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
<!-- 
<script src="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Cesium.js"></script>  
<link href="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Widgets/widgets.css" rel="stylesheet"> 
https://cesium.com/downloads/
-->
<script src="<%=basePath %>resource/cesiumjs/releases/1.56.1/Build/Cesium/Cesium.js"></script>
<link href="<%=basePath %>resource/cesiumjs/releases/1.56.1/Build/Cesium/Widgets/widgets.css" rel="stylesheet">
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
var selectedFloorValue="";

var viewer;
Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJkZWIzYTUxYy0xMmRkLTRiYTEtODE1My1kMjE1NzAyZDQwMmIiLCJpZCI6NzMyNDUsImlhdCI6MTYzNjY5NTEzOX0.rgwvu7AcuwqpYTO3kTKuZ7Pzebn1WNu2x8bKiqgbTcM';
$(function(){
	showSSTJDiv(false);
	summaryOnlineData();
	initLabelListDiv();
	initViewer();
	loadTileset();
	getStaffPositionList();
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

function initViewer(){
	viewer = new Cesium.Viewer('cesium_div');
	
	//获取经纬度、高度链接：https://www.cnblogs.com/telwanggs/p/11289455.html
	
	//https://blog.csdn.net/qq_36213352/article/details/122465031
	var scene=viewer.scene;
	//scene.morphTo2D(0);
	
	//获取事件触发所在的  html Canvas容器
    var canvas=scene.canvas;

    //获取事件句柄
    var handler=new Cesium.ScreenSpaceEventHandler(canvas);

    //处理事件函数
    handler.setInputAction(function(movement){

        //拾取笛卡尔坐标
        var ellipsoid=viewer.scene.globe.ellipsoid;//全局椭球体
        var cartesian=viewer.scene.camera.pickEllipsoid(movement.endPosition,ellipsoid)//拾取鼠标在椭圆上的结束点笛卡尔坐标点
        //转化笛卡尔坐标 为经纬度
        var mesDom=document.getElementById('mes');
        if(cartesian){
            var cartographic=ellipsoid.cartesianToCartographic(cartesian);//笛卡尔坐标转制图坐标
            //var coordinate="经度:"+Cesium.Math.toDegrees(cartographic.longitude).toFixed(2)+",纬度:"+Cesium.Math.toDegrees(cartographic.latitude).toFixed(2)+
                    "相机高度:"+Math.ceil(viewer.camera.positionCartographic.height);
            var coordinate="经度:"+Cesium.Math.toDegrees(cartographic.longitude)+",纬度:"+Cesium.Math.toDegrees(cartographic.latitude)+
            "相机高度:"+Math.ceil(viewer.camera.positionCartographic.height);
			console.log("coordinate==="+coordinate);
        }else{
        	
        }
    },Cesium.ScreenSpaceEventType.MOUSE_MOVE);//监听的是鼠标滑动事件
}

function loadTileset(){
	var tileset = new Cesium.Cesium3DTileset({
	   url: "http://192.168.1.100:8080/PositionPhZY/upload/b3dm/tileset.json",
	   shadows:Cesium.ShadowMode.DISABLED,//去除阴影
	});
	//console.log(tileset)
	viewer.scene.primitives.add(tileset);
	tileset.readyPromise.then(function(tileset) {
	   viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0, -0.5, 0));
	   //viewer.scene.primitives.remove(tileset);
	}).otherwise(function(error) {
	    throw(error);
	});
}

//https://www.yht7.com/news/263361
function initStaffImg(longitude,latitude,staffId,staffName,floor){
	var staffPosition = Cesium.Cartesian3.fromDegrees(longitude,latitude, 0);
	var staffNamePosition = Cesium.Cartesian3.fromDegrees(longitude,latitude, 20);
	
    viewer.entities.add({
	  id:"staff"+staffId,
      position: staffPosition,
      billboard: {
        //图标
        image: 'http://192.168.1.100:8080/PositionPhZY/upload/staff.jpg',
        width: 40,
        height: 40,
        scale: 1,
        pixelOffset: new Cesium.Cartesian2(0, -30),
        disableDepthTestDistance:Number.POSITIVE_INFINITY
      },
    });
	
	viewer.entities.add({
	  id:"staffName"+staffId,
      position: staffNamePosition,
	  label: { //文字标签
	    text: staffName+"("+floor+"层)",
	    font: '500 20px Helvetica',// 15pt monospace
	    scale: 1,
	    style: Cesium.LabelStyle.FILL,
	    fillColor: Cesium.Color.WHITE,
        eyeOffset: new Cesium.Cartesian3(0.0, 10.0, -15.0),
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
        showBackground: true,
        disableDepthTestDistance:Number.POSITIVE_INFINITY,
	  },
  });
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

function initLabelListDiv(){
	var labelListDiv=$("#label_list_div");
	labelListDiv.empty();
	
	var labelList=[];
	labelList.push({id:"BTR",labelChecked:false,name:"基站"});
	labelList.push({id:"BTI",labelChecked:false,name:"信标"});
	labelList.push({id:"BTG",labelChecked:false,name:"网关"});
	labelList.push({id:"staff",labelChecked:true,name:"人员"});
	labelList.push({id:"car",labelChecked:false,name:"车辆"});
	
	for (var i = 0; i < labelList.length; i++) {
		var item=labelList[i];
		var itemStr="<div class=\"item_div\">";
				itemStr+="<input class=\"select_cb\" id=\"select_cb"+item.id+"\" type=\"checkbox\" "+(item.labelChecked?"checked":"")+" onclick=\"initSSDWCanvas(0);\"/>";
				itemStr+="<span class=\"name_span\">"+item.name+"</span>";
			itemStr+="</div>";
		labelListDiv.append(itemStr);
	}
}

function getStaffPositionList(){
	$.post(path+"phone/getStaffPositionList",
		function(data){
			if(data.status=="ok"){
				var positionList=data.positionList;
				for(var i=0;i<positionList.length;i++){
					var position=positionList[i];
					//if(position.staffName=="高路路"){
						initStaffImg(position.longitude,position.latitude,position.staffId,position.staffName,position.floor);
					//}
				}
			}
		}
	,"json");
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
	z-index: 1;
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
.cesium_div{
	/*
	width: 4431px;
	height: 2573px;
	*/
	width: 720.52px;
	height: 670.49px;
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
<div class="cesium_div" id="cesium_div">
</div>
</body>
</html>