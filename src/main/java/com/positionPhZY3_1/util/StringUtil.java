package com.positionPhZY3_1.util;

import java.util.UUID;

public class StringUtil {

	/**
	 * 生成36位UUID
	 * @return
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}
}