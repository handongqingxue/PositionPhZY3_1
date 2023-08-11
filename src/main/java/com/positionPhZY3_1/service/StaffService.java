package com.positionPhZY3_1.service;

import java.util.List;

import com.positionPhZY3_1.entity.*;

public interface StaffService {

	int add(List<Staff> staffList, String databaseName);

	List<Staff> queryList(String databaseName);

	/**
	 * 查询省平台所需的员工数据
	 * @return
	 */
	List<Staff> queryEIList(String databaseName);

}