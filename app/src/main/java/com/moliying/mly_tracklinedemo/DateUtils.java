package com.moliying.mly_tracklinedemo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转换的工具类
 */
public class DateUtils {

	private static SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String toDate(Date date){
		return sdf.format(date);
	}
}
