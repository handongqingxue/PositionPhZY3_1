package com.positionPhZY3_1.service;

import com.positionPhZY3_1.entity.*;

public interface EpLoginClientService {

	int add(EpLoginClient elc);
	
	String getTokenByClientId(String clientId);
}
