<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
	String basePath=request.getScheme()+"://"+request.getServerName()+":"
		+request.getServerPort()+request.getContextPath()+"/";
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="<%=basePath %>resource/js/jquery-3.3.1.js"></script>
<script src="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Cesium.js"></script>  
<link href="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Widgets/widgets.css" rel="stylesheet">
<!-- 
<script src="<%=basePath %>resource/cesiumjs/releases/1.56.1/Build/Cesium/Cesium.js"></script>
<link href="<%=basePath %>resource/cesiumjs/releases/1.56.1/Build/Cesium/Widgets/widgets.css" rel="stylesheet">
 -->
<title>Insert title here</title>
<script>  
var path='<%=basePath%>';
var milkTruckEnLong=119.566;
var milkTruckEnLat=37.04075799355465;

var viewer;
Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJkZWIzYTUxYy0xMmRkLTRiYTEtODE1My1kMjE1NzAyZDQwMmIiLCJpZCI6NzMyNDUsImlhdCI6MTYzNjY5NTEzOX0.rgwvu7AcuwqpYTO3kTKuZ7Pzebn1WNu2x8bKiqgbTcM'; 
$(function(){
	initViewer();
	loadTileset();
	//initStaffImg();
	getStaffPositionList();
	//loadEntitiesFlagImg();
	//loadEntitiesRectImg(-150.0, 10.0, -120.0, 30.0,'http://localhost:8080/PositionPhZY/upload/IMG_20200823_151415.jpg',3);
	setInterval(() => {
		refreshEntities();
	}, 500);
});

function refreshEntities(){
	$.post(path+"phone/getStaffPositionList",
		function(data){
			if(data.status=="ok"){
				var positionList=data.positionList;
				for(var i=0;i<positionList.length;i++){
					var position=positionList[i];
					var staffEn=viewer.entities.getById("staff"+position.staffId);
					var staffNameEn=viewer.entities.getById("staffName"+position.staffId);
					//if(position.staffName=="高路路"){
						console.log("longitude===="+position.longitude+","+position.latitude+","+position.staffId+","+position.staffName+","+position.floor);
						if(staffEn!=undefined)
							staffEn.position=Cesium.Cartesian3.fromDegrees(position.longitude,position.latitude,position.z);
						if(staffNameEn!=undefined)
							staffNameEn.position=Cesium.Cartesian3.fromDegrees(position.longitude,position.latitude,position.z+40);
						//milkTruckEnLong-=0.00001;
					//}
				}
			}
		}
	,"json");
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

function initViewer(){
	viewer = new Cesium.Viewer('cesiumContainer');
	
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

function loadEntitiesFlagImg(){
	viewer.entities.add({
	    rectangle : {
	        coordinates : Cesium.Rectangle.fromDegrees(-100.0, 20.0, -90.0, 30.0),
	        material : new Cesium.StripeMaterialProperty({
	            evenColor: Cesium.Color.WHITE,
	            oddColor: Cesium.Color.BLUE,
	            repeat: 5
	        })
	    }
	});
}

//https://blog.csdn.net/sinat_31213021/article/details/120024000
function loadEntitiesRectImg(west,south,east,north,url,index){
	var rectImg=viewer.entities.add({
	    rectangle : {
	        coordinates : Cesium.Rectangle.fromDegrees(west,south,east,north),
	        material : url
	    }
	});
	if(index==1)
		rectImg1=rectImg;
	else if(index==2)
		rectImg2=rectImg;
}

function loadTileset(){
	var tileset = new Cesium.Cesium3DTileset({
	   url: "http://localhost:8080/PositionPhZY/upload/b3dm/tileset.json",
	   shadows:Cesium.ShadowMode.DISABLED,//去除阴影
	});
	console.log(tileset)
	viewer.scene.primitives.add(tileset);
	tileset.readyPromise.then(function(tileset) {
	   viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0, -0.5, 0));
	   //viewer.scene.primitives.remove(tileset);
	}).otherwise(function(error) {
	    throw(error);
	});
	
	var position = Cesium.Cartesian3.fromDegrees(milkTruckEnLong,milkTruckEnLat, 20);
	   var heading = Cesium.Math.toRadians(135);
	   var pitch = 0;
	   var roll = 0;
	   var hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll);
	   var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
	 
	   /*
	   var entity = viewer.entities.add({
		   id:"milkTruck",
	       position : position,
	       orientation : orientation,
	       model : {
	           uri: "http://localhost:8080/PositionPhZY/upload/CesiumMilkTruck.gltf",
	           //uri: "http://localhost:8080/PositionPhZY/upload/Cesium_Air.glb",
	           minimumPixelSize : 128,
	           maximumScale : 20000
	       }
	   });
	   //viewer.trackedEntity = entity;
	   */
	   
	   //https://wenku.baidu.com/view/4cdb49d5fa0f76c66137ee06eff9aef8941e4864.html?_wkts_=1691826732887&bdQuery=cesium%E6%B7%BB%E5%8A%A0%E5%9B%BE%E7%89%87
	   /*
	   viewer.entities.add({
		   id:"boy",
	       position : position,
	       billboard:{
	    	   image:'http://localhost:8080/PositionPhZY/upload/staff.jpg',
	    	   color:Cesium.Color.WHITE.withAlpha(0.8),
	    	   width:40,
	    	   height:40,
	    	   verticalOrigin:Cesium.VerticalOrigin.CENTER,
	    	   horizontalOrigin:Cesium.HorizontalOrigin.CENTER
	       }
	   });
	   */
	   
	
	/*
	tileset = new Cesium.Cesium3DTileset({
	   url: "http://localhost:8080/PositionPhZY/upload/model2/tileset.json",
	   shadows:Cesium.ShadowMode.DISABLED,//去除阴影
	});
	viewer.scene.primitives.add(tileset);
	tileset.readyPromise.then(function(tileset) {
	   viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0, -0.5, 0));
	   var cartographic = Cesium.Cartographic.fromCartesian(tileset.boundingSphere.center);
	   console.log(cartographic);
	   setTimeout(function(){
		   //viewer.scene.primitives.remove(tileset);
		   //viewer.scene.primitives.removeAll();
	   },"10000");
	}).otherwise(function(error) {
	    throw(error);
	});
	*/
}

//https://www.yht7.com/news/263361
function initStaffImg(longitude,latitude,staffId,staffName,floor){
	var position = Cesium.Cartesian3.fromDegrees(longitude,latitude, 0);
	var position2 = Cesium.Cartesian3.fromDegrees(longitude,latitude, 20);
    var heading = Cesium.Math.toRadians(135);
    var pitch = 0;
    var roll = 0;
    var hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll);
    var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
	
    console.log("staffId==="+staffId)
    /*
    //设置物体不被遮挡:https://blog.csdn.net/github_36091081/article/details/120082847
    viewer.entities.add(new Cesium.Entity({
        point: new Cesium.PointGraphics({
          color: new Cesium.Color(1, 1, 0),
          pixelSize: 10,
          outlineColor: new Cesium.Color(0, 1, 1),
          disableDepthTestDistance:Number.POSITIVE_INFINITY
        }),
       	position: Cesium.Cartesian3.fromDegrees(longitude, latitude, 1 + 0.5)
    }));
    */
    
    viewer.entities.add({
	  id:"staff"+staffId,
      position: position,
      billboard: {
          //图标
          image: 'http://localhost:8080/PositionPhZY/upload/staff.jpg',
          width: 40,
          height: 40,
          scale: 1,
          pixelOffset: new Cesium.Cartesian2(0, -30),
          disableDepthTestDistance:Number.POSITIVE_INFINITY
        },
    });
	
	viewer.entities.add({
	  id:"staffName"+staffId,
      position: position2,
      label: { //文字标签
          text: staffName+"("+floor+"层)",
          font: '500 20px Helvetica',// 15pt monospace
          scale: 1,
          style: Cesium.LabelStyle.FILL,
          fillColor: Cesium.Color.WHITE,
          // pixelOffset: new Cesium.Cartesian2(0, 0),   //偏移量
          eyeOffset: new Cesium.Cartesian3(0.0, 10.0, -15.0),
          // horizontalOrigin: Cesium.HorizontalOrigin.LEFT,
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          showBackground: true,
          disableDepthTestDistance:Number.POSITIVE_INFINITY,
          // heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
          // disableDepthTestDistance: Number.POSITIVE_INFINITY,
          // backgroundColor: new Cesium.Color(26 / 255, 196 / 255, 228 / 255, 1.0)   //背景顔色
      },
    });
}
 </script>
</head>
<body>
<div id="cesiumContainer" style="width: 1500px; height:700px"></div>  

</body>
</html>