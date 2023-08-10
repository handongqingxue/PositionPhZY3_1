package com.positionPhZY3_1.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY3_1.dao.*;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;

@Service
public class EpLoginClientServiceImpl implements EpLoginClientService {

	@Autowired
	private EpLoginClientMapper epLoginClientDao;

	@Override
	public int add(EpLoginClient elc) {
		// TODO Auto-generated method stub
		int count=epLoginClientDao.getCountByClientId(elc.getClient_id());
		if(count==0)
			count=epLoginClientDao.add(elc);
		else
			count=epLoginClientDao.edit(elc);
		return count;
	}

	@Override
	public String getTokenByClientId(String clientId) {
		// TODO Auto-generated method stub
		return epLoginClientDao.getTokenByClientId(clientId);
	}
}
