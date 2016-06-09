package com.v5kf.mcss.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** 首选项管理
  * @ClassName: SharePreferenceUtil
  * @Description: TODO
  * @author Chyrain
  * @date 2014-12-23 10:01
  */
public class SharePreferenceUtil {
	private SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String name) {
		mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}
	private static final String SHARED_KEY_NOTIFY = "shared_key_notify";
	private static final String SHARED_KEY_NOTIFY_WAIT = "shared_key_notify_wait";
	private static final String SHARED_KEY_VOICE = "shared_key_sound";
	private static final String SHARED_KEY_VIBRATE = "shared_key_vibrate";
	private static final String SHARED_KEY_AUTOBOOT = "shared_key_autoboot";
//	private static final String SHARED_KEY_AUTOLOGIN = "shared_key_autologin";
	private static final String SP_WS_REQUEST_STATISTICS = "websocket_statistics";
	
	// 是否允许推送通知
	public boolean isAllowPushNotify() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NOTIFY, true);
	}

	public void setPushNotifyEnable(boolean isChecked) {
		editor.putBoolean(SHARED_KEY_NOTIFY, isChecked);
		editor.commit();
	}

	// 是否允许推送排队中的客户消息通知
	public boolean isAllowPushWaitNotify() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NOTIFY_WAIT, true);
	}
	
	public void setPushWaitNotifyEnable(boolean isChecked) {
		editor.putBoolean(SHARED_KEY_NOTIFY_WAIT, isChecked);
		editor.commit();
	}

	// 允许声音
	public boolean isAllowVoice() {
		return mSharedPreferences.getBoolean(SHARED_KEY_VOICE, true);
	}

	public void setAllowVoiceEnable(boolean isChecked) {
		editor.putBoolean(SHARED_KEY_VOICE, isChecked);
		editor.commit();
	}

	// 允许震动
	public boolean isAllowVibrate() {
		return mSharedPreferences.getBoolean(SHARED_KEY_VIBRATE, true);
	}

	public void setAllowVibrateEnable(boolean isChecked) {
		editor.putBoolean(SHARED_KEY_VIBRATE, isChecked);
		editor.commit();
	}

	// 允许自启动
	public boolean isAllowAutoboot() {
		return mSharedPreferences.getBoolean(SHARED_KEY_AUTOBOOT, true);
	}
	
	public void setAllowAutoboot(boolean isChecked) {
		editor.putBoolean(SHARED_KEY_AUTOBOOT, isChecked);
		editor.commit();
	}

//	// 允许自动登录
//	public boolean isAllowAutoLogin() {
//		return mSharedPreferences.getBoolean(SHARED_KEY_AUTOLOGIN, true);
//	}
//	
//	public void setAllowAutoLogin(boolean isChecked) {
//		editor.putBoolean(SHARED_KEY_AUTOLOGIN, isChecked);
//		editor.commit();
//	}

	/**
	 * 保存SP_WS_REQUEST_STATISTICS
	 * @param num
	 */
	public void saveStatistics(int num) {
		editor.putInt(SP_WS_REQUEST_STATISTICS, num);
		editor.commit();
	}
	
	/**
	 * 增加统计值
	 * @param addStatistics WorkerSP 
	 * @return void
	 * @param add
	 */
	public void addStatistics(int add) {
		editor.putInt(SP_WS_REQUEST_STATISTICS, readStatistics() + add);
		editor.commit();
	}
	
	public void addStatistics() {
		addStatistics(1);
	}
	
	/**
	 * 读取num请求次数统计
	 * @return num
	 */
	public int readStatistics() {
		int num = mSharedPreferences.getInt(SP_WS_REQUEST_STATISTICS, 0);
		return num;
	}
}
