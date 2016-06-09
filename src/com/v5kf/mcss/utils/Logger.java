package com.v5kf.mcss.utils;

import com.v5kf.mcss.config.Config;

import android.util.Log;

/**
 * 
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-1 下午7:32:52
 * @package com.v5kf.mcss.config of MCSS-Native
 * @file Logger.java 
 *
 */
public class Logger {
	
	public static final int ERROR = 1;
	public static final int WARN = 2;
	public static final int INFO = 3;
	public static final int DEBUG = 4;
	public static final int VERBOS = 5;
	
	/*
	 * between 0 ~ 6
	 */
	public static int LOG_LEVEL = Config.LOG_LEVEL;
 
	public static void e(String tag, String msg){
		if(LOG_LEVEL > ERROR)
			Log.e(tag, "<v5kf>" + msg);
	}

	public static void w(String tag, String msg){
		if(LOG_LEVEL>WARN)
			Log.w(tag, "<v5kf>" + msg);
	}
	
	public static void i(String tag, String msg){
		if(LOG_LEVEL>INFO)
			Log.i(tag, "<v5kf>" + msg);
	}

	public static void d(String tag, String msg){
		if(LOG_LEVEL>DEBUG)
			Log.d(tag, "<v5kf>" + msg);
	}
	
	public static void v(String tag, String msg){
		if(LOG_LEVEL>VERBOS)
			Log.v(tag, "<v5kf>" + msg);
	}
}
