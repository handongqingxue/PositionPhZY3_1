package com.positionPhZY3_1.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface DeviceMapper {

	int add(@Param("device")Device device, @Param("databaseName")String databaseName);

	int edit(@Param("device")Device device, @Param("databaseName")String databaseName);

	int getCountById(@Param("id")Integer id, @Param("databaseName")String databaseName);
}
