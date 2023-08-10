package com.positionPhZY3_1.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;
import com.positionPhZY3_1.util.Constant;

//http://120.224.131.123:81/#/login
public class PhoneController {

	public static final String ADDRESS_URL="http://120.224.131.123:81/positionApi/";
	
	@Autowired
	private EpLoginClientService epLoginClientService;
	
	public void switchTenant(HttpServletRequest request) {
		String clientSecret=null;
		String tenantId=request.getAttribute("tenantId").toString();
		String username=request.getAttribute("username").toString();
		String password=request.getAttribute("password").toString();
		
		if(Constant.TENANT_ID_ZBXQHGYXGS.equals(tenantId)&&
		   Constant.USER_ID_ZBXQHGYXGS.equals(username)&&
		   Constant.PASSWORD_ZBXQHGYXGS.equals(password)) {
			tenantId=Constant.TENANT_ID_ZBXQHGYXGS;
			clientSecret=Constant.CLIENT_SECRET_ZBXQHGYXGS;
		}
		
		request.setAttribute("tenantId", tenantId);
		request.setAttribute("clientSecret", clientSecret);
	}
	
	public JSONObject requestApi(String apiMethod, String params, JSONObject bodyParamJO, String requestMethod, HttpServletRequest request) {

		JSONObject resultJO = null;
		try {
			StringBuffer sbf = new StringBuffer(); 
			String strRead = null; 
			String serverUrl=ADDRESS_URL+apiMethod+params;
			
			String clientId = request.getAttribute("tenantId").toString();
			
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
					
				if(!StringUtils.isEmpty(access_token))
					connection.setRequestProperty("Authorization", "Bearer "+access_token);
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
