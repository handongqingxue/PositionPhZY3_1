package com.positionPhZY3_1.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface KeyWarningMapper {

	int add(@Param("keyWarning")KeyWarning keyWarning, @Param("databaseName")String databaseName);

	List<KeyWarning> queryEAList(@Param("sync")int sync, @Param("databaseName")String databaseName);

	int syncByIds(@Param("idList")List<String> idList, @Param("databaseName")String databaseName);
}
