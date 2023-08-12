package com.positionPhZY3_1.service;

import java.util.List;

import com.positionPhZY3_1.entity.*;

public interface PositionService {

	int add(Position position, String databaseName);

	/**
	 * 查询平台同步人员位置的信息列表
	 * @param databaseName
	 * @return
	 */
	List<Position> queryELList(String databaseName);

}
