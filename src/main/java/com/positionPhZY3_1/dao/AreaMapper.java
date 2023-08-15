package com.positionPhZY3_1.dao;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface AreaMapper {

	int add(@Param("area")Area area, @Param("databaseName")String databaseName);

	int edit(@Param("area")Area area, @Param("databaseName")String databaseName);

	int getCountById(@Param("id")Integer id, @Param("databaseName")String databaseName);
}
