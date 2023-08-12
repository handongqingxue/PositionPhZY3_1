package com.positionPhZY3_1.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.positionPhZY3_1.entity.*;

public interface KeyWarningService {

	int add(@Param("keyWarning")KeyWarning keyWarning, @Param("databaseName")String databaseName);

	List<KeyWarning> queryEAList(@Param("sync")int sync, @Param("databaseName")String databaseName);

	int syncByIds(@Param("syncIds")String syncIds, @Param("databaseName")String databaseName);

}
