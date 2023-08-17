package com.positionPhZY3_1.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY3_1.dao.*;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;

@Service
public class AreaServiceImpl implements AreaService {

	@Autowired
	private AreaMapper areaDao;
	
	@Override
	public int add(List<Area> areaList, String databaseName) {
		// TODO Auto-generated method stub
		int count=0;
		for (Area area : areaList) {
			if(areaDao.getCountById(area.getId(),databaseName)==0) {
				count+=areaDao.add(area, databaseName);
			}
			else
				count+=areaDao.edit(area, databaseName);
		}
		return count;
	}

	@Override
	public List<Area> querySelectData() {
		// TODO Auto-generated method stub
		return areaDao.querySelectData();
	}

}
