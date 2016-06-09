package com.v5kf.mcss.utils;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WakeUtil {
	public static final String WLOCK_TAG = "com.v5kf.mcss.wakescreen";
	private static WakeLock mWklk;
	
	public static void acquireWakeLock(Context context) {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		mWklk = pm.newWakeLock(PowerManager.ON_AFTER_RELEASE|PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WLOCK_TAG);
		if (null != mWklk) {
			mWklk.setReferenceCounted(false);
			Logger.d("WakeUtil", "WLOCK_TAG [acquire] - before");
			mWklk.acquire(); //设置保持唤醒
			Logger.d("WakeUtil", "WLOCK_TAG [acquire] - after");
		}
	}
	
	public static void releaseWakeLock(Context context) {
		if (null != mWklk) {
			Logger.d("WakeUtil", "WLOCK_TAG [release] - before");
			mWklk.release();
			Logger.d("WakeUtil", "WLOCK_TAG [release] - after");
			mWklk = null;
		}
	}
}
