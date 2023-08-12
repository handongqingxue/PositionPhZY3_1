package com.positionPhZY3_1.service.serviceImpl;

import java.util.Arrays;
import java.util.List;

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
}
