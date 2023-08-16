package com.positionPhZY3_1.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.positionPhZY3_1.dao.*;
import com.positionPhZY3_1.entity.*;
import com.positionPhZY3_1.service.*;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	private DeviceMapper deviceDao;

	@Override
	public int add(List<Device> deviceList, String databaseName) {
		// TODO Auto-generated method stub
		int count=0;
		for (Device device : deviceList) {
			if(deviceDao.getCountById(device.getId(),databaseName)==0) {
				count+=deviceDao.add(device, databaseName);
			}
			else
				count+=deviceDao.edit(device, databaseName);
		}
		return count;
	}
}
