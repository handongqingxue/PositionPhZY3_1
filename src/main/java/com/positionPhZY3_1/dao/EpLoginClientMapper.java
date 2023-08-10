package com.positionPhZY3_1.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface EpLoginClientMapper {

	int add(EpLoginClient elc);

	int edit(EpLoginClient elc);

	String getTokenByClientId(@Param("client_id")String client_id);

	int getCountByClientId(@Param("client_id")String client_id);
}
