package com.positionPhZY3_1.service.serviceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY3_1.dao.*;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;

@Service
public class PositionServiceImpl implements PositionService {

	@Autowired
	private PositionMapper positionDao;

	@Override
	public int add(Position position, String databaseName) {
		// TODO Auto-generated method stub
		int count=positionDao.getCountByTagId(position.getTagId(),databaseName);
		if(count==0)
			positionDao.add(position,databaseName);
		else
			count=positionDao.edit(position,databaseName);
		return count;
	}

	@Override
	public List<Position> queryELList(String databaseName) {
		// TODO Auto-generated method stub
		return positionDao.queryELList(databaseName);
	}

	@Override
	public List<Position> getStaffPositionList() {
		// TODO Auto-generated method stub
		return positionDao.getStaffPositionList();
	}

	@Override
	public Map<String, Object> summaryOnlineEntity() {
		// TODO Auto-generated method stub
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Map<String, Object> summaryMap = new HashMap<String, Object>();
		
		Map<String, Object> onlineMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> childrenMapList = new ArrayList<Map<String, Object>>();

		Map<String, Object> childMap=null;
		Map<String, Object> childSummaryMap=null;
		Map<String, Object> childOnlineMap=null;
		List<Map<String, Object>> childSummaryMapList=positionDao.summaryOnlineEntity();
		int totalSum = 0;
		for (Map<String, Object> childSummaryEleMap : childSummaryMapList) {
			childMap = new HashMap<String, Object>();
			childSummaryMap = new HashMap<String, Object>();
			childOnlineMap = new HashMap<String, Object>();
			
			String name = childSummaryEleMap.get("name").toString();
			int areaId = Integer.valueOf(childSummaryEleMap.get("areaId").toString());
			int total = Integer.valueOf(childSummaryEleMap.get("total").toString());
			totalSum+=total;
			
			childOnlineMap.put("total", total);
			
			childSummaryMap.put("online", childOnlineMap);
			
			childMap.put("summary", childSummaryMap);
			childMap.put("name", name);
			childMap.put("areaId", areaId);
			
			childrenMapList.add(childMap);
		}
		
		onlineMap.put("total", totalSum);
		
		summaryMap.put("online", onlineMap);
		
		resultMap.put("summary", summaryMap);
		resultMap.put("children", childrenMapList);
		resultMap.put("name", "总图");
		
		return resultMap;
	}
}
