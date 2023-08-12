package com.positionPhZY3_1.service.serviceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY3_1.dao.*;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;
import com.positionPhZY3_1.util.DateUtil;

@Service
public class KeyWarningServiceImpl implements KeyWarningService {

	@Autowired
	private KeyWarningMapper keyWarningDao;

	@Override
	public int add(KeyWarning keyWarning,String databaseName) {
		// TODO Auto-generated method stub
		Long raiseTime = keyWarning.getRaiseTime();
		if(!StringUtils.isEmpty(String.valueOf(raiseTime)))
			keyWarning.setRaiseTimeYMD(DateUtil.convertLongToString(raiseTime));
		int count=keyWarningDao.add(keyWarning,databaseName);
		return count;
	}

	@Override
	public List<KeyWarning> queryEAList(int sync,String databaseName) {
		// TODO Auto-generated method stub
		return keyWarningDao.queryEAList(sync,databaseName);
	}

	@Override
	public int syncByIds(String syncIds,String databaseName) {
		// TODO Auto-generated method stub
		List<String> idList = Arrays.asList(syncIds.split(","));
		return keyWarningDao.syncByIds(idList,databaseName);
	}

}
