package com.positionPhZY3_1.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface StaffMapper {

	int add(@Param("staff")Staff staff, @Param("databaseName")String databaseName);

	int edit(@Param("staff")Staff staff, @Param("databaseName")String databaseName);

	List<Staff> queryList(@Param("databaseName")String databaseName);

	int getCountById(@Param("id")Integer id, @Param("databaseName")String databaseName);

	List<Staff> queryEIList(@Param("databaseName")String databaseName);
}
