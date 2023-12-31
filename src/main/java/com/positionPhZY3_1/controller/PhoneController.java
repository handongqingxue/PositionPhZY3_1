package com.positionPhZY3_1.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;
import com.positionPhZY3_1.util.Constant;

//http://120.224.131.123:81/#/login
@Controller
@RequestMapping(PhoneController.MODULE_NAME)
public class PhoneController {

	private static final String ADDRESS_URL="http://"+Constant.SERVICE_IP_STR+":"+Constant.SERVICE_PORT_STR+"/positionApi/";
	public static final String MODULE_NAME="/phone";
	
	@Autowired
	private EpLoginClientService epLoginClientService;
	@Autowired
	private StaffService staffService;
	@Autowired
	private PositionService positionService;
	@Autowired
	private AreaService areaService;
	@Autowired
	private DeviceService deviceService;

	@RequestMapping(value="/goPage")
	public String goPage(HttpServletRequest request) {

		//http://localhost:8080/PositionPhZY3_1/phone/goPage?page=login
		//http://localhost:8080/PositionPhZY3_1/phone/goPage?page=testMap
		//http://localhost:8080/PositionPhZY3_1/phone/goPage?page=index
		String url = null;
		String page = request.getParameter("page");
		if("testMap".equals(page)){
			url=MODULE_NAME+"/testMap";
		}
		else if("login".equals(page)){
			setLoginInfoInRequest(request);
			url=MODULE_NAME+"/login";
		}
		else {
			url=MODULE_NAME+"/"+page;
		}
		return url;
	}
	
	private void setLoginInfoInRequest(HttpServletRequest request) {
		request.setAttribute("tenantId", Constant.TENANT_ID_CYSRHSWKJYXGS);
		request.setAttribute("username", Constant.USER_ID_CYSRHSWKJYXGS);
		request.setAttribute("password", Constant.PASSWORD_CYSRHSWKJYXGS);
	}

	@RequestMapping(value="/summaryOnlineData")
	@ResponseBody
	public Map<String, Object> summaryOnlineData() {

		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		Map<String, Object> soerMap=positionService.summaryOnlineEntity();
		
		List<Area> areaList=areaService.querySelectData();
		
		jsonMap.put("entityResult", soerMap);
		jsonMap.put("childAreaList", areaList);
		
		return jsonMap;
	}
	
	@RequestMapping(value="/getStaffPositionList")
	@ResponseBody
	public Map<String, Object> getStaffPositionList() {
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		List<Position> positionList=positionService.getStaffPositionList();

		if(positionList.size()==0) {
			jsonMap.put("status", "no");
			jsonMap.put("info", "暂无信息");
		}
		else {
			jsonMap.put("status", "ok");
			jsonMap.put("positionList", positionList);
		}
		
		return jsonMap;
	}

	@RequestMapping(value="/login")
	@ResponseBody
	public Map<String, Object> login(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String tenantId=request.getParameter("tenantId");
		String username=request.getParameter("username");
		String password=request.getParameter("password");
		
		int epFlag=0;
		if(Constant.TENANT_ID_CYSRHSWKJYXGS.equals(tenantId)&&
		   Constant.USER_ID_CYSRHSWKJYXGS.equals(username)&&
		   Constant.PASSWORD_CYSRHSWKJYXGS.equals(password)) {
			epFlag=Constant.CYSRHSWKJYXGS;
			request.setAttribute("epFlag", epFlag);
			oauthToken(request);
			
			resultMap.put("status", "ok");
		}
		else {
			resultMap.put("status", "no");
			resultMap.put("message", "用户不存在");
		}
		
		return resultMap;
	}
	
	@RequestMapping(value="/exit")
	public String exit(HttpServletRequest request) {
		
		//HttpSession session = request.getSession();
		//session.removeAttribute("loginUser");
		
		return MODULE_NAME+"/login";
	}
	
	/**
	 * 2.1 获取token
	 * @param serviceIp
	 * @param servicePort
	 * @param clientId
	 * @param clientSecret
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/oauthToken")
	@ResponseBody
	public Map<String, Object> oauthToken(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			
			String apiMethod="oauth/token";
			String params="?client_id="+Constant.CLIENT_ID_STR+"&grant_type=client_credentials&client_secret="+Constant.CLIENT_SECRET_STR;
			resultJO = requestApi(apiMethod,params,bodyParamJO,"GET",request);
			String resultStr = resultJO.toString();
			System.out.println("resultJO==="+resultStr);
				
			String status=resultJO.get("status").toString();
			String access_token=resultJO.get("access_token").toString();
			System.out.println("status==="+status);
			System.out.println("access_token==="+access_token);

			String clientId = request.getAttribute("tenantId").toString();
			EpLoginClient elc=new EpLoginClient(access_token,clientId);
			epLoginClientService.add(elc);
			
			HttpSession session = request.getSession();
			Object epLoginClientObj = session.getAttribute("epLoginClient"+clientId);
			if(epLoginClientObj!=null) {
				EpLoginClient epLoginClient = (EpLoginClient)epLoginClientObj;
				epLoginClient.setAccess_token(access_token);
				epLoginClient.setClient_id(clientId);
			}

			resultMap=JSON.parseObject(resultStr, Map.class);
			//resultMap.put("access_token", access_token);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/apiStaffDataList")
	@ResponseBody
	public Map<String, Object> apiStaffDataList(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("type", 1);
			bodyParamJO.put("orgId", 100);
			bodyParamJO.put("pageNum", 1);
			bodyParamJO.put("pageSize", 200);
			
			String apiMethod="api/staff/dataList";
			String params="";
			resultJO = requestApi(apiMethod,params,bodyParamJO,"POST",request);
			resultMap=JSON.parseObject(resultJO.toString());
			/*{"msg":"操作成功","code":200,"data":{"records":[
			 * {"id":1998,"name":"王合参","sex":1,"phone":null,"photo":"/","jobNumber":"34058124","tagId":"BTT34058124","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1997,"name":"明建成","sex":1,"phone":null,"photo":"/","jobNumber":"34058114","tagId":"BTT34058114","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1996,"name":"张春芝","sex":1,"phone":"","photo":"/","jobNumber":"34058116","tagId":"BTT34058116","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1995,"name":"王召霞","sex":1,"phone":null,"photo":"/","jobNumber":"34058108","tagId":"BTT34058108","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1994,"name":"姜建东","sex":1,"phone":null,"photo":"/","jobNumber":"ZY34058097","tagId":"BTT34058097","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1992,"name":"袭祥鹏","sex":1,"phone":null,"photo":"/","jobNumber":"ZY34058109","tagId":"BTT34058109","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":176,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1989,"name":"于恒全","sex":0,"phone":"","photo":"/","jobNumber":"8043","tagId":"BTT34058043","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1988,"name":"郑当卫","sex":0,"phone":"","photo":"/","jobNumber":"8044","tagId":"BTT34058044","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1987,"name":"马福州","sex":0,"phone":"","photo":"/","jobNumber":"8045","tagId":"BTT34058045","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1986,"name":"梁术祥","sex":0,"phone":"","photo":"/","jobNumber":"8046","tagId":"BTT34058046","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":173,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1985,"name":"朱爱东","sex":0,"phone":"","photo":"/","jobNumber":"8047","tagId":"BTT34058047","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":173,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1984,"name":"王培同","sex":0,"phone":"","photo":"/","jobNumber":"8048","tagId":"BTT34058048","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1983,"name":"姜爱强","sex":0,"phone":"","photo":"/","jobNumber":"8049","tagId":"BTT34058049","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1982,"name":"陈子云","sex":0,"phone":"","photo":"/","jobNumber":"8050","tagId":"BTT34058050","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1981,"name":"王立松","sex":0,"phone":"","photo":"/","jobNumber":"8051","tagId":"BTT34058051","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1980,"name":"赵中智","sex":0,"phone":"","photo":"/","jobNumber":"8052","tagId":"BTT34058052","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1979,"name":"顾红涛","sex":0,"phone":"","photo":"/","jobNumber":"8053","tagId":"BTT34058053","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1976,"name":"刘利波","sex":0,"phone":"","photo":"/","jobNumber":"8056","tagId":"BTT34058056","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1974,"name":"齐秀丽","sex":2,"phone":"","photo":"/","jobNumber":"8058","tagId":"BTT34058058","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},
			 * {"id":1973,"name":"李承良","sex":0,"phone":"","photo":"/","jobNumber":"8059","tagId":"BTT34058059","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1971,"name":"陈登兰","sex":2,"phone":"","photo":"/","jobNumber":"8061","tagId":"BTT34058061","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1970,"name":"栾顺云","sex":0,"phone":"","photo":"/","jobNumber":"8062","tagId":"","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1969,"name":"陈公正","sex":0,"phone":"","photo":"/","jobNumber":"8063","tagId":"BTT34058063","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1968,"name":"闫建芳","sex":2,"phone":"","photo":"/","jobNumber":"8064","tagId":"BTT34058064","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1967,"name":"姜红书","sex":1,"phone":"","photo":"/","jobNumber":"8065","tagId":"BTT34058065","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1965,"name":"穆龙国","sex":0,"phone":"","photo":"/","jobNumber":"8067","tagId":"BTT34058067","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1964,"name":"陈永兴","sex":0,"phone":"","photo":"/","jobNumber":"8068","tagId":"","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1963,"name":"李莉","sex":0,"phone":"","photo":"/","jobNumber":"8069","tagId":"BTT34058069","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1962,"name":"任令义","sex":0,"phone":"","photo":"/","jobNumber":"8070","tagId":"BTT34058070","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1961,"name":"代颖华","sex":0,"phone":"","photo":"/","jobNumber":"8071","tagId":"BTT34058071","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1960,"name":"马云英","sex":0,"phone":"","photo":"/","jobNumber":"8072","tagId":"BTT34058072","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1959,"name":"王显丽","sex":0,"phone":"","photo":"/","jobNumber":"8073","tagId":"BTT34058073","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1958,"name":"孙燕燕","sex":0,"phone":"","photo":"/","jobNumber":"8074","tagId":"BTT34058074","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1957,"name":"任英华","sex":0,"phone":"","photo":"/","jobNumber":"8075","tagId":"BTT34058075","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1956,"name":"孔宪玲","sex":0,"phone":"","photo":"/","jobNumber":"8076","tagId":"BTT34058076","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1954,"name":"苗迎兰","sex":0,"phone":"","photo":"/","jobNumber":"8078","tagId":"BTT34058078","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1953,"name":"姜卫波","sex":0,"phone":"","photo":"/","jobNumber":"8079","tagId":"BTT34058079","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1951,"name":"姜廷连","sex":0,"phone":"","photo":"/","jobNumber":"8081","tagId":"BTT34058081","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1949,"name":"刘春梅","sex":0,"phone":"","photo":"/","jobNumber":"8083","tagId":"BTT34058083","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1948,"name":"王淑平","sex":0,"phone":"","photo":"/","jobNumber":"8084","tagId":"BTT34058084","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1947,"name":"王亚丽","sex":0,"phone":"","photo":"/","jobNumber":"8085","tagId":"BTT34058085","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1946,"name":"李爱香","sex":0,"phone":"","photo":"/","jobNumber":"8086","tagId":"BTT34058086","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1944,"name":"肖卫波","sex":0,"phone":"","photo":"/","jobNumber":"8088","tagId":"BTT34058088","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1943,"name":"于友正","sex":0,"phone":"","photo":"/","jobNumber":"8089","tagId":"BTT34058089","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":176,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1942,"name":"李会生","sex":0,"phone":"","photo":"/","jobNumber":"8090","tagId":"BTT34058090","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1941,"name":"陈和德","sex":0,"phone":"","photo":"/","jobNumber":"8091","tagId":"BTT34058091","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1940,"name":"郑永生","sex":0,"phone":"","photo":"/","jobNumber":"8092","tagId":"BTT34058092","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1939,"name":"郇昌伟","sex":0,"phone":"","photo":"/","jobNumber":"8093","tagId":"BTT34058093","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":173,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1938,"name":"刘同和","sex":0,"phone":"","photo":"/","jobNumber":"8094","tagId":"","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1937,"name":"王秋伟","sex":0,"phone":"","photo":"/","jobNumber":"8095","tagId":"BTT34058095","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1934,"name":"高明宽","sex":1,"phone":"","photo":"/","jobNumber":"8098","tagId":"BTT34058098","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1933,"name":"崔旭辉","sex":2,"phone":"","photo":"/","jobNumber":"8099","tagId":"BTT34058099","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":177,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1932,"name":"刘丽萍","sex":0,"phone":"","photo":"/","jobNumber":"8100","tagId":"BTT34058100","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1931,"name":"郑小云","sex":0,"phone":"","photo":"/","jobNumber":"8101","tagId":"BTT34058101","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1930,"name":"柳春日","sex":0,"phone":"","photo":"/","jobNumber":"8102","tagId":"BTT34058102","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1929,"name":"孙美红","sex":0,"phone":"","photo":"/","jobNumber":"8104","tagId":"","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":161,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1928,"name":"卞永军","sex":0,"phone":"","photo":"/","jobNumber":"8105","tagId":"BTT34058105","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1927,"name":"梁述祯","sex":0,"phone":"","photo":"/","jobNumber":"8106","tagId":"BTT34058106","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1926,"name":"何亚楠","sex":2,"phone":"","photo":"/","jobNumber":"8107","tagId":"BTT34058107","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1923,"name":"韩冰","sex":0,"phone":"","photo":"/","jobNumber":"8110","tagId":"BTT34058110","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":171,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1922,"name":"姜娟","sex":0,"phone":"","photo":"/","jobNumber":"8111","tagId":"BTT34058111","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":171,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1918,"name":"李学杰","sex":1,"phone":"","photo":"/","jobNumber":"8115","tagId":"BTT34058115","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1915,"name":"李建才","sex":0,"phone":"","photo":"/","jobNumber":"8118","tagId":"BTT34058118","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1914,"name":"胡俊卿","sex":0,"phone":"","photo":"/","jobNumber":"8119","tagId":"BTT34058119","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":176,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1913,"name":"郝继东","sex":0,"phone":"","photo":"/","jobNumber":"8120","tagId":"BTT34058120","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1912,"name":"宋广英","sex":0,"phone":"","photo":"/","jobNumber":"8121","tagId":"BTT34058121","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1911,"name":"徐松波","sex":1,"phone":"","photo":"/","jobNumber":"8122","tagId":"BTT34058122","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1910,"name":"陈文杰","sex":0,"phone":"","photo":"/","jobNumber":"8123","tagId":"BTT34058123","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1908,"name":"王洪涛","sex":0,"phone":"","photo":"/","jobNumber":"8125","tagId":"BTT34058125","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1907,"name":"李元超","sex":1,"phone":"","photo":"/","jobNumber":"8126","tagId":"BTT34058126","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1906,"name":"谢娟","sex":2,"phone":"","photo":"/","jobNumber":"8127","tagId":"","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1905,"name":"谢正伟","sex":0,"phone":"","photo":"/","jobNumber":"8128","tagId":"BTT34058128","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":163,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1904,"name":"刘全军","sex":1,"phone":"","photo":"/","jobNumber":"8129","tagId":"BTT34058129","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1903,"name":"高路路","sex":2,"phone":"","photo":"/","jobNumber":"8130","tagId":"BTT34058130","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1902,"name":"宋丽英","sex":2,"phone":"","photo":"/","jobNumber":"8131","tagId":"BTT34058131","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1901,"name":"李希清","sex":0,"phone":"","photo":"/","jobNumber":"8132","tagId":"BTT34058132","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":169,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1900,"name":"苗维平","sex":1,"phone":"","photo":"/","jobNumber":"8133","tagId":"BTT34058133","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1899,"name":"林永生","sex":1,"phone":"","photo":"/","jobNumber":"8134","tagId":"BTT34058134","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":173,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1898,"name":"曹春良","sex":0,"phone":"","photo":"/","jobNumber":"8135","tagId":"BTT34058135","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1897,"name":"徐爱莲","sex":2,"phone":"","photo":"/","jobNumber":"8136","tagId":"BTT34058136","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1896,"name":"闫军","sex":0,"phone":"","photo":"/","jobNumber":"8137","tagId":"BTT34058137","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1895,"name":"李孟祥","sex":1,"phone":"","photo":"/","jobNumber":"8138","tagId":"BTT34058138","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1894,"name":"姜爱强","sex":1,"phone":"","photo":"/","jobNumber":"8139","tagId":"BTT34058139","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1893,"name":"卢振苇","sex":1,"phone":"","photo":"/","jobNumber":"8140","tagId":"BTT34058140","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1892,"name":"徐爱玲","sex":2,"phone":"","photo":"/","jobNumber":"8141","tagId":"BTT34058141","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":162,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1891,"name":"姜丽辉","sex":2,"phone":"","photo":"/","jobNumber":"8142","tagId":"BTT34058142","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1890,"name":"8143","sex":0,"phone":"","photo":"/","jobNumber":"8143","tagId":"BTT34058143","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":162,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1889,"name":"李来军","sex":1,"phone":"","photo":"/","jobNumber":"8144","tagId":"BTT34058144","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":174,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1888,"name":"高礼清","sex":1,"phone":"","photo":"/","jobNumber":"8145","tagId":"BTT34058145","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":168,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1887,"name":"8146","sex":0,"phone":"","photo":"/","jobNumber":"8146","tagId":"BTT34058146","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":162,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1886,"name":"马召恩","sex":1,"phone":"","photo":"/","jobNumber":"8147","tagId":"BTT34058147","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1885,"name":"王德强","sex":1,"phone":"","photo":"/","jobNumber":"8148","tagId":"BTT34058148","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1884,"name":"李继昌","sex":1,"phone":"","photo":"/","jobNumber":"8149","tagId":"BTT34058149","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1883,"name":"姜占修","sex":1,"phone":"","photo":"/","jobNumber":"8150","tagId":"BTT34058150","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1882,"name":"初晓","sex":1,"phone":"","photo":"/","jobNumber":"8151","tagId":"BTT34058151","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1881,"name":"李前前","sex":1,"phone":"","photo":"/","jobNumber":"8152","tagId":"BTT34058152","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1880,"name":"侯晓光","sex":1,"phone":"","photo":"/","jobNumber":"8153","tagId":"BTT34058153","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":175,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1879,"name":"付子风","sex":1,"phone":"","photo":"/","jobNumber":"8154","tagId":"BTT34058154","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1878,"name":"郑斋柱","sex":1,"phone":"","photo":"/","jobNumber":"8155","tagId":"BTT34058155","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null},{"id":1877,"name":"闫代江","sex":1,"phone":"","photo":"/","jobNumber":"8156","tagId":"BTT34058156","post":"员工","station":null,"security":null,"entityIconId":1,"deptId":172,"orgId":100,"type":1,"deleted":null,"remark":null,"fileId":null}],"total":187,"size":100,"current":1,"orders":[],"optimizeCountSql":true,"hitCount":false,"countId":null,"maxLimit":null,"searchCount":true,"pages":2}}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/apiAreaAllList")
	@ResponseBody
	public Map<String, Object> apiAreaAllList(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			
			String apiMethod="api/area/allList";
			String params="/100?access_token=86aeb9db-262c-4bee-94e5-63aa7f64b77d";
			resultJO = requestApi(apiMethod,params,bodyParamJO,"GET",request);
			resultMap=JSON.parseObject(resultJO.toString());
			/*
			 * {
			    "msg": "操作成功",
			    "code": 200,
			    "data": [
			        {
			            "id": 1,
			            "parentId": null,
			            "name": "总图"
			        },
			        {
			            "id": 2,
			            "parentId": 1,
			            "name": "道路"
			        },
			        {
			            "id": 3,
			            "parentId": 1,
			            "name": "F1"
			        },
			        {
			            "id": 4,
			            "parentId": 1,
			            "name": "F2"
			        },
			        {
			            "id": 5,
			            "parentId": 1,
			            "name": "F3"
			        },
			        {
			            "id": 6,
			            "parentId": 1,
			            "name": "F4"
			        }
			    ]
			}
			 * */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/apiBuildingAllList")
	@ResponseBody
	public Map<String, Object> apiBuildingAllList(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			
			String apiMethod="api/building/allList";
			String params="/100?access_token=c022270c-84ef-41fa-b98a-184d38122fe3";
			resultJO = requestApi(apiMethod,params,bodyParamJO,"GET",request);
			resultMap=JSON.parseObject(resultJO.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	@RequestMapping(value="/insertDeviceData")
	@ResponseBody
	public Map<String, Object> insertDeviceData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			int epFlag = Integer.valueOf(request.getParameter("epFlag"));
			System.out.println("epFlag==="+epFlag);
			request.setAttribute("epFlag", epFlag);
			Map<String, Object> deviceListMap = apiDeviceList(request);
			String status = deviceListMap.get("status").toString();
			if("ok".equals(status)) {
				Object dataObj = deviceListMap.get("data");
				com.alibaba.fastjson.JSONObject dataJO = null;
				com.alibaba.fastjson.JSONArray recordJA = null;
				if(dataObj!=null) {
					dataJO=(com.alibaba.fastjson.JSONObject)dataObj;
					recordJA = dataJO.getJSONArray("records");
					
				}
				List<Device> deviceList = JSON.parseArray(recordJA.toString(),Device.class);
				String databaseName = request.getAttribute("databaseName").toString();
				int count=deviceService.add(deviceList,databaseName);
				if(count==0) {
					resultMap.put("status", "no");
					resultMap.put("message", "初始化设备信息失败");
				}
				else {
					resultMap.put("status", "ok");
					resultMap.put("message", "初始化设备信息成功");
				}
			}
			else {
				boolean success=reOauthToken(request);
				System.out.println("success==="+success);
				if(success) {
					Thread.sleep(1000*60);//避免频繁操作，休眠60秒后再执行
					resultMap=insertDeviceData(request);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/apiDeviceList")
	@ResponseBody
	public Map<String, Object> apiDeviceList(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("deviceType", "BTG");
			bodyParamJO.put("orgId", 100);
			bodyParamJO.put("pageNum", 1);
			bodyParamJO.put("pageSize", 200);
			
			String apiMethod="api/device/list";
			String params="";
			resultJO = requestApi(apiMethod,params,bodyParamJO,"POST",request);
			resultMap=JSON.parseObject(resultJO.toString());
			/*
			 {
			    "msg": "操作成功",
			    "code": 200,
			    "data": {
			        "records": [
			            {
			                "id": 11863,
			                "deviceId": "BTR8BFCA195",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":669,\"address\":\"192.168.124.2:49152\",\"labelId\":7263,\"firmVer\":\"0314\",\"timeDiff\":0,\"modularState\":\"0000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0004\",\"locationsignaltype\":0,\"flowId\":123310,\"deviceId\":\"BTR8BFCA195\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080453547,
			                "x": 93.7799911556,
			                "y": 58.85684075280001,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.5650291936478,
			                "latitude": 37.04300990011995
			            },
			            {
			                "id": 11825,
			                "deviceId": "BTRB9B96309",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":377,\"address\":\"192.168.124.2:49152\",\"labelId\":7270,\"firmVer\":\"0314\",\"modularState\":\"0000\",\"state\":0,\"hardVer\":\"0004\",\"locationsignaltype\":0,\"flowId\":100859,\"deviceId\":\"BTRB9B96309\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080402092,
			                "x": 317.12473672220005,
			                "y": 115.34925553800002,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56753979469103,
			                "latitude": 37.04351889073835
			            },
			            {
			                "id": 11810,
			                "deviceId": "BTR21044952",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":409,\"address\":\"192.168.124.2:49152\",\"labelId\":7266,\"firmVer\":\"0314\",\"timeDiff\":0,\"modularState\":\"0000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0004\",\"flowId\":266870,\"deviceId\":\"BTR21044952\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080551168,
			                "x": 254.4462978777,
			                "y": 166.3736569744,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56683525042256,
			                "latitude": 37.043978678316556
			            },
			            {
			                "id": 11788,
			                "deviceId": "BTR7262E316",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":648,\"address\":\"192.168.124.2:49152\",\"labelId\":7265,\"firmVer\":\"0314\",\"timeDiff\":0,\"modularState\":\"0000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0004\",\"locationsignaltype\":0,\"flowId\":84118,\"deviceId\":\"BTR7262E316\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080586419,
			                "x": 196.2974490813,
			                "y": 158.1415806889,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56618159944603,
			                "latitude": 37.04390451503328
			            },
			            {
			                "id": 11713,
			                "deviceId": "BTRFEFD48EF",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":624,\"address\":\"192.168.124.2:49152\",\"labelId\":7264,\"timeDiff\":0,\"firmVer\":\"0314\",\"modularState\":\"0000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0004\",\"locationsignaltype\":0,\"deviceId\":\"BTRFEFD48EF\",\"flowId\":121811}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080436099,
			                "x": 184.9384083217,
			                "y": 72.68717419040001,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56605389206207,
			                "latitude": 37.043134508384625
			            },
			            {
			                "id": 11691,
			                "deviceId": "BTR0AED48FC",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":685,\"address\":\"192.168.124.2:49152\",\"labelId\":7267,\"firmVer\":\"0314\",\"timeDiff\":0,\"modularState\":\"0000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0004\",\"locationsignaltype\":0,\"flowId\":136184,\"deviceId\":\"BTR0AED48FC\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080351890,
			                "x": 277.4437913168,
			                "y": 164.5669416517,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56709376363915,
			                "latitude": 37.04396239193032
			            },
			            {
			                "id": 11687,
			                "deviceId": "BTR4CDF163B",
			                "areaId": null,
			                "deviceType": "BTR",
			                "volt": null,
			                "voltUnit": "mV",
			                "options": "{\"labelId\":7269,\"deviceId\":\"BTR4CDF163B\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": null,
			                "x": null,
			                "y": null,
			                "z": null,
			                "r": null,
			                "areaName": null,
			                "ip": null,
			                "port": null,
			                "longitude": null,
			                "latitude": null
			            },
			            {
			                "id": 11686,
			                "deviceId": "BTRE1696617",
			                "areaId": null,
			                "deviceType": "BTR",
			                "volt": null,
			                "voltUnit": "mV",
			                "options": "{\"labelId\":7268,\"deviceId\":\"BTRE1696617\"}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": null,
			                "x": null,
			                "y": null,
			                "z": null,
			                "r": null,
			                "areaName": null,
			                "ip": null,
			                "port": null,
			                "longitude": null,
			                "latitude": null
			            },
			            {
			                "id": 11685,
			                "deviceId": "BTR26054F00",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":17,\"address\":\"192.168.124.2:49152\",\"labelId\":7262,\"firmVer\":\"0314\",\"modularState\":\"0000\",\"state\":0,\"hardVer\":\"0004\",\"deviceId\":\"BTR26054F00\",\"flowId\":339245}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080357381,
			                "x": 99.4851150181,
			                "y": 136.75376707520002,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56509333418241,
			                "latitude": 37.04371180987256
			            },
			            {
			                "id": 11678,
			                "deviceId": "BTRFA9CFCA4",
			                "areaId": 1,
			                "deviceType": "BTR",
			                "volt": 0,
			                "voltUnit": "mV",
			                "options": "{\"initMark\":47,\"address\":\"192.168.124.2:49152\",\"firmVer\":\"0136\",\"netState\":1,\"modularState\":\"0013\",\"deviceFlag\":1,\"deviceId\":\"BTRFA9CFCA4\",\"areaId\":1,\"labelId\":\"1106\",\"timeDiff\":0,\"crc\":\"0000000000000000\",\"overtime\":\"3600000\",\"isSend\":true,\"state\":0,\"hardVer\":\"0010\",\"validity\":\"00000000\",\"locationsignaltype\":0,\"flowId\":1188410}",
			                "orgId": null,
			                "createTime": null,
			                "createBy": null,
			                "updateTime": null,
			                "updateBy": null,
			                "deleted": null,
			                "remark": null,
			                "time": 1692080614901,
			                "x": 62.9300208919,
			                "y": 174.7192791397,
			                "z": 0.0,
			                "r": 0.0,
			                "areaName": "总图",
			                "ip": null,
			                "port": null,
			                "longitude": 119.56468242448767,
			                "latitude": 37.04405391105554
			            }
			        ],
			        "total": 10,
			        "size": 10,
			        "current": 1,
			        "orders": [],
			        "optimizeCountSql": true,
			        "hitCount": false,
			        "countId": null,
			        "maxLimit": null,
			        "searchCount": true,
			        "pages": 1
			    }
			}
			 * */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	@RequestMapping(value="/insertStaffData")
	@ResponseBody
	public Map<String, Object> insertStaffData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			int epFlag = Integer.valueOf(request.getParameter("epFlag"));
			System.out.println("epFlag==="+epFlag);
			request.setAttribute("epFlag", epFlag);
			Map<String, Object> staffListMap = apiStaffDataList(request);
			String status = staffListMap.get("status").toString();
			if("ok".equals(status)) {
				Object dataObj = staffListMap.get("data");
				com.alibaba.fastjson.JSONObject dataJO = null;
				com.alibaba.fastjson.JSONArray recordJA = null;
				if(dataObj!=null) {
					dataJO=(com.alibaba.fastjson.JSONObject)dataObj;
					recordJA = dataJO.getJSONArray("records");
					
				}
				List<Staff> staffList = JSON.parseArray(recordJA.toString(),Staff.class);
				String databaseName = request.getAttribute("databaseName").toString();
				int count=staffService.add(staffList,databaseName);
				if(count==0) {
					resultMap.put("status", "no");
					resultMap.put("message", "初始化员工信息失败");
				}
				else {
					resultMap.put("status", "ok");
					resultMap.put("message", "初始化员工信息成功");
				}
			}
			else {
				boolean success=reOauthToken(request);
				System.out.println("success==="+success);
				if(success) {
					Thread.sleep(1000*60);//避免频繁操作，休眠60秒后再执行
					resultMap=insertStaffData(request);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	@RequestMapping(value="/insertAreaData")
	@ResponseBody
	public Map<String, Object> insertAreaData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			int epFlag = Integer.valueOf(request.getParameter("epFlag"));
			System.out.println("epFlag==="+epFlag);
			request.setAttribute("epFlag", epFlag);
			Map<String, Object> areaListMap = apiAreaAllList(request);
			String status = areaListMap.get("status").toString();
			if("ok".equals(status)) {
				Object dataObj = areaListMap.get("data");
				com.alibaba.fastjson.JSONArray areaJA = null;
				if(dataObj!=null) {
					areaJA=(com.alibaba.fastjson.JSONArray)dataObj;
				}
				List<Area> areaList = JSON.parseArray(areaJA.toString(),Area.class);
				String databaseName = request.getAttribute("databaseName").toString();
				int count=areaService.add(areaList,databaseName);
				if(count==0) {
					resultMap.put("status", "no");
					resultMap.put("message", "初始化地图信息失败");
				}
				else {
					resultMap.put("status", "ok");
					resultMap.put("message", "初始化地图信息成功");
				}
			}
			else {
				boolean success=reOauthToken(request);
				System.out.println("success==="+success);
				if(success) {
					Thread.sleep(1000*60);//避免频繁操作，休眠60秒后再执行
					resultMap=insertAreaData(request);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	public boolean reOauthToken(HttpServletRequest request) {
		//switchEnterprise(EP_FLAG,request);
		Map<String, Object> resultMap = oauthToken(request);
		String status = resultMap.get("status").toString();
		if("ok".equals(status))
			return true;
		else
			return false;
	}
	
	public void switchService(HttpServletRequest request) {
		String serviceIp=null;
		int servicePort=0;
		String tenantId=null;
		String databaseName=null;
		Object epFlagObj = request.getAttribute("epFlag");
		String epFlagStr = null;
		if(epFlagObj==null)
			epFlagStr = "6";
		else
			epFlagStr = epFlagObj.toString();
		int epFlag=Integer.valueOf(epFlagStr);
		switch (epFlag) {
		/*
		case Constant.WFPXHGYXGS:
			serviceIp=Constant.SERVICE_IP_WFPXHGYXGS;
			servicePort=Constant.SERVICE_PORT_WFPXHGYXGS;
			tenantId=Constant.TENANT_ID_WFPXHGYXGS;
			databaseName=Constant.DATABASE_NAME_WFPXHGYXGS;
			break;
		case Constant.SDFLXCLKJYXGS:
			serviceIp=Constant.SERVICE_IP_SDFLXCLKJYXGS;
			servicePort=Constant.SERVICE_PORT_SDFLXCLKJYXGS;
			tenantId=Constant.TENANT_ID_SDFLXCLKJYXGS;
			databaseName=Constant.DATABASE_NAME_SDFLXCLKJYXGS;
			break;
			*/
		case Constant.CYSRHSWKJYXGS:
			serviceIp=Constant.SERVICE_IP_CYSRHSWKJYXGS;
			servicePort=Constant.SERVICE_PORT_CYSRHSWKJYXGS;
			tenantId=Constant.TENANT_ID_CYSRHSWKJYXGS;
			databaseName=Constant.DATABASE_NAME_CYSRHSWKJYXGS;
			break;
		}
		request.setAttribute("serviceIp", serviceIp);
		request.setAttribute("servicePort", servicePort);
		request.setAttribute("tenantId", tenantId);
		request.setAttribute("databaseName", databaseName);
	}
	
	public void switchTenant(HttpServletRequest request) {
		String tenantId=null;
		String clientSecret=null;
		int epFlag=Integer.valueOf(request.getAttribute("epFlag").toString());
		switch (epFlag) {
		case Constant.CYSRHSWKJYXGS:
			tenantId=Constant.TENANT_ID_CYSRHSWKJYXGS;
			clientSecret=Constant.CLIENT_SECRET_CYSRHSWKJYXGS;
			break;
		}
		
		request.setAttribute("tenantId", tenantId);
		request.setAttribute("clientSecret", clientSecret);
	}
	
	public JSONObject requestApi(String apiMethod, String params, JSONObject bodyParamJO, String requestMethod, HttpServletRequest request) {

		JSONObject resultJO = null;
		try {
			switchService(request);
			
			StringBuffer sbf = new StringBuffer(); 
			String strRead = null; 
			String serverUrl=ADDRESS_URL+apiMethod+params;

			String serviceIp = request.getAttribute("serviceIp").toString();
			String servicePort = request.getAttribute("servicePort").toString();
			String clientId = request.getAttribute("tenantId").toString();

			serverUrl=serverUrl.replaceAll(Constant.SERVICE_IP_STR, serviceIp);
			serverUrl=serverUrl.replaceAll(Constant.SERVICE_PORT_STR, servicePort);
			if(apiMethod.contains("oauth/token")) {
				switchTenant(request);
				String clientSecret = request.getAttribute("clientSecret").toString();
				serverUrl=serverUrl.replaceAll(Constant.CLIENT_ID_STR, clientId);
				serverUrl=serverUrl.replaceAll(Constant.CLIENT_SECRET_STR, clientSecret);
			}
			
			//System.out.println("serverUrl==="+serverUrl);
			URL url = new URL(serverUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			
			//connection.setInstanceFollowRedirects(false); 
			
			HttpSession session = request.getSession();
			if(!apiMethod.contains("oauth/token")) {
				String access_token = null;
				Object epLoginClientObj = session.getAttribute("epLoginClient");
				if(epLoginClientObj!=null) {
					EpLoginClient epLoginClient = (EpLoginClient)epLoginClientObj;
					access_token = epLoginClient.getAccess_token();
				}
				
				if(access_token==null) {
					Object client_idObj = request.getAttribute("tenantId");
					if(client_idObj!=null) {
						String client_id = client_idObj.toString();
						if(client_id!=null)
							access_token = epLoginClientService.getTokenByClientId(client_id);
					}
				}
					
				if(!StringUtils.isEmpty(access_token)) {
					connection.setRequestProperty("Authorization", "Bearer "+access_token);
				}
			}
			connection.setRequestMethod(requestMethod);//请求方式
			connection.setDoInput(true); 
			connection.setDoOutput(true); 
			//header内的的参数在这里set    
			//connection.setRequestProperty("key", "value");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.connect(); 
			
			//https://blog.csdn.net/zhengaog/article/details/118405244
			if("POST".equals(requestMethod)) {
				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
				//body参数放这里
				String bodyParamStr = bodyParamJO.toString();
				//System.out.println("bodyParamStr==="+bodyParamStr);
				writer.write(bodyParamStr);
				writer.flush();
			}
			
			InputStream is = connection.getInputStream(); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
			while ((strRead = reader.readLine()) != null) { 
				sbf.append(strRead); 
				sbf.append("\r\n"); 
			}
			reader.close(); 
			
			connection.disconnect();
			String result = sbf.toString();
			System.out.println("result==="+result);
			if(result.contains("DOCTYPE")) {
				resultJO = new JSONObject();
				resultJO.put("status", "no");
			}
			else if(result.contains("error")) {
				resultJO = new JSONObject(result);
				resultJO.put("status", "no");
			}
			else {
				resultJO = new JSONObject(result);
				resultJO.put("status", "ok");
				
				if(apiMethod.contains("oauth/token")) {
					if(!checkTokenInSession(request)) {
						String access_token = resultJO.getString("access_token");
						EpLoginClient elc=new EpLoginClient();
						elc.setAccess_token(access_token);
						elc.setClient_id(clientId);
						session.setAttribute("epLoginClient"+clientId, elc);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception.....");
			resultJO = new JSONObject();
			resultJO.put("status", "no");
			e.printStackTrace();
		}
		finally {
			return resultJO;
		}
	}
	
	public boolean checkTokenInSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clientId=request.getAttribute("tenantId").toString();
		Object epLoginClientObj = session.getAttribute("epLoginClient"+clientId);
		if(epLoginClientObj==null)
			return false;
		else {
			EpLoginClient epLoginClient = (EpLoginClient)epLoginClientObj;
			if(epLoginClient==null)
				return false;
			else {
				String access_token = epLoginClient.getAccess_token();
				if(StringUtils.isEmpty(access_token))
					return false;
				else
					return true;
			}
		}
	}
}
