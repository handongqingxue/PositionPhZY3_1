package com.positionPhZY3_1.service;

import java.util.List;

import com.positionPhZY3_1.entity.*;

public interface AreaService {

	int add(List<Area> areaList, String databaseName);

	List<Area> querySelectData();
}
